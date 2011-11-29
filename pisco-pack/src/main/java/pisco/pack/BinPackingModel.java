/**
*  Copyright (c) 2011, Arnaud Malapert
*  All rights reserved.
*  Redistribution and use in source and binary forms, with or without
*  modification, are permitted provided that the following conditions are met:
*
*      * Redistributions of source code must retain the above copyright
*        notice, this list of conditions and the following disclaimer.
*      * Redistributions in binary form must reproduce the above copyright
*        notice, this list of conditions and the following disclaimer in the
*        documentation and/or other materials provided with the distribution.
*      * Neither the name of the Arnaud Malapert nor the
*        names of its contributors may be used to endorse or promote products
*        derived from this software without specific prior written permission.
*
*  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
*  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
*  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
*  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
*  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
*  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
*  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
*  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
*  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
*  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package pisco.pack;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.constraints.global.pack.PackSConstraint;
import choco.cp.solver.search.BranchingFactory;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.kernel.common.opres.heuristics.IHeuristic;
import choco.kernel.common.opres.pack.LowerBoundFactory;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.pack.PackModel;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.branch.AbstractIntBranchingStrategy;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.visu.components.chart.ChocoChartFactory;
import gnu.trove.TIntArrayList;
import parser.instances.AbstractMinimizeModel;
import parser.instances.BasicSettings;
import parser.instances.ReportFormatter;
import pisco.pack.choco.branching.BinSymBreakDecreasing;
import pisco.pack.choco.branching.ItemSymBreakDecreasing;
import pisco.pack.choco.valselector.BestFit;
import pisco.pack.choco.valselector.DynBestFit;
import pisco.pack.choco.valselector.DynWorstFit;
import pisco.pack.choco.valselector.WorstFit;

public class BinPackingModel extends AbstractMinimizeModel
{
  private int nbBins;
  private PackModel modeler;
  private Constraint pack;

  public BinPackingModel(Configuration configuration)
  {
    super(new BinPackingParser(), configuration);
    setChartManager(ChocoChartFactory.getJFreeChartManager());
  }

  public void initialize()
  {
    super.initialize();
    cancelHeuristic();
    this.nbBins = 0;
    this.modeler = null;
    this.pack = null;
  }

  public Boolean preprocess()
  {
    BinPackingParser p = (BinPackingParser)this.parser;
    TIntArrayList items = new TIntArrayList(p.sizes);

    items.sort();
    CompositeHeuristics1BP h = new CompositeHeuristics1BP(items, p.capacity);
    setHeuristic(h);
    Boolean b = super.preprocess();
    if ((b != null) && (Boolean.valueOf(b.booleanValue()).booleanValue())) {
      this.nbBins = getHeuristic().getObjectiveValue().intValue();

      setComputedLowerBound(LowerBoundFactory.memComputeAllMDFF(items, p.capacity, this.nbBins));
      this.nbBins -= 1;
    } else {
      setComputedLowerBound(0);
      this.nbBins = items.size();
    }
    return b;
  }

  public Model buildModel()
  {
    CPModel m = new CPModel();
    BinPackingParser pr = (BinPackingParser)this.parser;
    this.modeler = new PackModel("", pr.sizes, this.nbBins, pr.capacity);
    this.pack = Choco.pack(this.modeler, new String[] { "cp:pack:last_bins_empty" });
    if (!this.defaultConf.readBoolean("tools.cp.model.light")) {
      this.pack.addOption("cp:pack:additional_rules");
      this.pack.addOption("cp:pack:dynamic_lower_bound");
    }
    if (this.defaultConf.readBoolean("tools.model.symmetry.completion")) {
      this.pack.addOption("cp:pack:fill_bins");
    }
    m.addConstraint(this.pack);
    int nbLI = this.modeler.packLargeItems(m);

    if (this.defaultConf.readBoolean("tools.model.symmetry.order"))
    {
      this.modeler.orderEqualSizedItems(m, nbLI);

      this.modeler.decreasingLoads(m, nbLI);
    }

    this.modeler.nbNonEmpty.setLowB(getComputedLowerBound());
    this.modeler.nbNonEmpty.addOption("cp:objective");
    return m;
  }

  private ValSelector<IntDomainVar> createValueSelector(PackSConstraint ct, int capa)
  {
    switch (BinPackingSettings.getValSel(getConfiguration())) { 
    	case RAND:
      return new RandomIntValSelector(getSeed());
    case FF:
      return new MinVal();
    case BF:
      return new BestFit(ct);
    case WF:
      return new WorstFit(ct);
    case DBF:
      return new DynBestFit(ct);
    case DWF:
      return new DynWorstFit(ct);
    }
    throw new SolverException("Value selection heuristic is not yet implemented");
  }

  private AbstractIntBranchingStrategy createBranching(Solver s)
  {
    PackSConstraint ct = (PackSConstraint)s.getCstr(this.pack);
    IntDomainVar[] vars = ct.getBins();

    ValSelector valSel = createValueSelector(ct, this.modeler.getMaxCapacity());
    switch (BinPackingSettings.getBranching(getConfiguration()))
    { case CD:
      return BranchingFactory.lexicographic(s, vars, valSel);
    case CDB:
      return new BinSymBreakDecreasing(s, valSel, ct);
    case CDBI:
      return new ItemSymBreakDecreasing(s, valSel, ct);
    }
    return null;
  }

  public Solver buildSolver()
  {
    Solver s = super.buildSolver();
    s.read(this.model);
    s.addGoal(createBranching(s));
    s.clearGoals();
    s.generateSearchStrategy();
    return s;
  }

  protected void logOnConfiguration()
  {
    super.logOnConfiguration();

    this.logMsg.storeConfiguration(((BinPackingParser)getParser()).getInstanceMessage());

    if (this.solver != null) {
      this.logMsg.storeConfiguration(BinPackingSettings.getBranchingMsg(getConfiguration()));
      this.logMsg.storeConfiguration(BinPackingSettings.getSymBreakMsg(getConfiguration()) + BasicSettings.getInstModelMsg(getConfiguration()));
    }
  }

  public String getValuesMessage()
  {
    if ((this.solver != null) && (this.solver.existsSolution()))
      return ((PackSConstraint)this.solver.getCstr(this.pack)).getSolutionMsg();
    return "";
  }

  protected Object makeSolutionChart()
  {
    return (this.solver != null) && (this.solver.existsSolution()) ? 
      ChocoChartFactory.createPackChart(getInstanceName() + " : " + getStatus(), (PackSConstraint)this.solver.getCstr(this.pack)) : null;
  }
}

/* Location:           /home/nono/recovery/pack/
 * Qualified Name:     pisco.pack.BinPackingModel
 * JD-Core Version:    0.6.0
 */