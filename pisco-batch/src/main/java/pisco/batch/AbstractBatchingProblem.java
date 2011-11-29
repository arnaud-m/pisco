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
package pisco.batch;

import static choco.Choco.MAX_UPPER_BOUND;
import static choco.Choco.constantArray;
import static choco.Choco.makeIntVar;
import static choco.Choco.makeIntVarArray;
import static choco.Choco.makeSetVarArray;
import static choco.Choco.max;
import static choco.Choco.min;
import static choco.Choco.pack;
import static choco.Options.*;
import static choco.Options.C_PACK_DLB;
import static choco.Options.C_PACK_LBE;
import static choco.Options.V_BOUND;
import static choco.Options.V_ENUM;
import static choco.Options.V_NO_DECISION;
import static choco.Options.V_OBJECTIVE;

import java.io.File;
import java.util.Comparator;

import parser.absconparseur.tools.UnsupportedConstraintException;
import parser.instances.AbstractMinimizeModel;
import parser.instances.BasicSettings;
import parser.instances.ResolutionStatus;
import pisco.batch.BatchSettings.ValSel;
import pisco.batch.BatchSettings.VarSel;
import pisco.batch.choco.branching.BatchDynRemovals;
import pisco.batch.choco.branching.BatchFit;
import pisco.batch.choco.constraints.BatchManager;
import pisco.batch.choco.constraints.set.SetIntCombManager;
import pisco.batch.data.Batch;
import pisco.batch.data.BatchParser;
import pisco.batch.data.BatchProcessingData;
import pisco.batch.data.Job;
import pisco.batch.heuristics.BatchProcessingRHeuristics;
import pisco.batch.heuristics.ICostAggregator;
import pisco.batch.heuristics.ICostFunction;
import pisco.batch.heuristics.PDRScheduler;
import pisco.batch.heuristics.PriorityDispatchingRule;
import pisco.batch.visu.BatchingChartFactory;
import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.constraints.global.pack.PackSConstraint;
import choco.cp.solver.search.integer.branching.AssignVar;
import choco.cp.solver.search.integer.valselector.BestFit;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.Model;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.pack.PackModel;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.Solver;
import choco.kernel.solver.branch.VarSelector;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.search.checker.SolutionCheckerException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.visu.components.chart.ChocoChartFactory;

public abstract class AbstractBatchingProblem extends AbstractMinimizeModel {

	protected BatchProcessingData data;

	protected SetVariable[] batches;
	protected IntegerVariable[] jobInB;
	protected IntegerVariable nonEmptyB;


	protected IntegerVariable[] batchDurations;
	protected IntegerVariable[] batchSizes;
	protected IntegerVariable[] batchWeights;
	protected IntegerVariable[] batchDueDates;

	protected IntegerVariable objVar;

	protected Constraint packCstr;

	private int computedNbBatches;
	protected Batch[] bestSolution;

	public AbstractBatchingProblem(Configuration settings) {
		super(new BatchParser(), settings);
		//settings.putTrue(BatchSettings.SOLUTION_REPORT);
		setChartManager(ChocoChartFactory.getJFreeChartManager());
		final BatchProcessingRHeuristics h = new BatchProcessingRHeuristics(this);
		h.setIterationLimit(1000);
		h.setTimeLimit(1000);
		setHeuristic(h);
	}

	@Override
	public void initialize() {
		super.initialize();
		data = null;
		batchDurations = null;
		batchSizes = null;
		batchWeights=null;
		batchDueDates = null;

		objVar = null;

		packCstr = null;
		computedNbBatches = Choco.MAX_UPPER_BOUND;
		bestSolution = null;
	}

	@Override
	public Boolean preprocess() {
		initM();
		return super.preprocess();
	}

	public final BatchProcessingData getData() {
		return data;
	}

	public final int getN() {
		return data.nbJobs;
	}

	protected final int initM() {
		return computedNbBatches = getN();
	}

	protected final int setM(int nbBatches) {
		return computedNbBatches = nbBatches;
	}

	public final int getM() {
		return computedNbBatches;
	}


	public final Constraint getPackCstr() {
		return packCstr;
	}

	// DONE 12 oct. 2011 - remove : related to s-batch- created 12 oct. 2011 by Arnaud Malapert

	public abstract PriorityDispatchingRule getPriorityDispatchingRule();

	@Override
	public void load(File fichier) throws UnsupportedConstraintException {
		super.load(fichier);
		BatchParser p = (BatchParser) getParser();
		data = new BatchProcessingData(p.getJobs(), p.getCapacity());
	}

	@Override
	public Model buildModel() {
		CPModel model = new CPModel(5* getN(), 10*getN(), getM(), 0, 5 * getN(), getN(), 0);
		jobInB = makeIntVarArray("J", getN(), 0, getM()-1,V_ENUM);
		batches= makeSetVarArray("B", getM(), 0, getN() - 1, V_BOUND,V_NO_DECISION);
		nonEmptyB = makeIntVar("m",0, getM(),V_BOUND);
		final int ub = (getStatus() == ResolutionStatus.SAT ? getInitialObjectiveValue().intValue() - 1 : MAX_UPPER_BOUND); 
		objVar = makeIntVar("obj", getComputedLowerBound(), ub, V_OBJECTIVE,V_BOUND, V_NO_DECISION);
		// FIXME - set constraints priorities - created 11 oct. 2011 by Arnaud Malapert
		setPBatchConstraints(model);
		setPBatchDurationConstraints(model);
		setWeightsConstraints(model);
		setDueDateConstraints(model);
		setSingleRelaxationConstraint(model);
		return model;
	}

	private final void setPBatchConstraints(Model model) {
		setPBatchDurationConstraints(model);
		batchSizes = makeIntVarArray("S", getM(), 0, data.getCapacity(), V_BOUND,V_NO_DECISION);
		PackModel packM = new PackModel(jobInB, constantArray(data.getSizes()), batches, batchSizes, nonEmptyB);
		packCstr =pack(packM, C_PACK_LBE);
		if( ! defaultConf.readBoolean(BasicSettings.LIGHT_MODEL) ) {
			packCstr.addOption(C_PACK_AR);
			packCstr.addOption(C_PACK_DLB);
		}
		model.addConstraint(packCstr);
		packM.packLargeItems(model);
	}

	private final void setPBatchDurationConstraints(Model model) {
		final int[] durations = new int[getN()+1] ;
		System.arraycopy(data.getDurations(), 0, durations, 1, getN());
		//duration is equal to 0 if the batch is empty
		batchDurations = makeIntVarArray("P", getM(), durations, V_ENUM, V_NO_DECISION);
		for (int i = 0; i < getM(); i++) {
			model.addConstraint( max(batches[i], constantArray(data.getDurations()), batchDurations[i], C_MINMAX_INF));
		}
	}


	public static Constraint sum(SetVariable set, int[] coeffs, IntegerVariable sum) {
		return new ComponentConstraint(SetIntCombManager.class, coeffs, new Variable[]{set, sum});
	}

	protected void setWeightsConstraints(Model model) {
		batchWeights = makeIntVarArray("W", computedNbBatches, 0, data.getTotalWeight(), V_BOUND, V_NO_DECISION);
		// DONE 21 sept. 2011 - Add method setWeightsConstraints - created 20 sept. 2011 by Arnaud Malapert
		for (int i = 0; i < this.computedNbBatches; i++) {
			model.addConstraint( sum(batches[i], data.getWeights(), batchWeights[i]));
		}
	}

	protected void setDueDateConstraints(Model model) {
		batchDueDates = makeIntVarArray("D", computedNbBatches, data.getMinDueDate(), data.getMaxDueDate(), V_ENUM, V_NO_DECISION);
		for (int i = 0; i < computedNbBatches; i++) {
			model.addConstraint( min(batches[i], constantArray(data.getDueDates()), batchDueDates[i], C_MINMAX_SUP));
		}
	}

	protected void setSingleRelaxationConstraint(Model model) {
		model.addConstraint( new ComponentConstraint( BatchManager.class, this,
				ArrayUtils.<IntegerVariable>append(batchDurations, batchWeights, batchDueDates, jobInB, new IntegerVariable[]{nonEmptyB, objVar})
				));
	}

	



	protected void setGoal(Solver solver) {
		VarSelector<IntDomainVar> varSel = null;
		ValSelector<IntDomainVar> valSel=null;
		final IntDomainVar[] bins = solver.getVar(jobInB);
		final IntDomainVar[] bd = solver.getVar(batchDurations);
		final IntDomainVar[] bdd = solver.getVar(batchDueDates);
		PackSConstraint pack = (PackSConstraint) solver.getCstr(packCstr);
		switch (BatchSettings.getVarSel(defaultConf)) {
		case DOM: varSel = new MinDomain(solver, bins); break;
		case LEX: varSel = new StaticVarOrder(solver, bins); break;
		case MMC : throw new UnsupportedOperationException(VarSel.MMC.getDescription());
		case MSC : throw new UnsupportedOperationException(VarSel.MSC.getDescription());
		case RANDOM: varSel = new RandomIntVarSelector(solver, bins, getSeed());break;
		default:
			throw new ModelException("no variable-selection heuristics");
		}
		switch (BatchSettings.getValSel(defaultConf)) {
		case FF: valSel =new MinVal();break;
		case BF: valSel =new BestFit(pack);break;
		case BBF: valSel = new BatchFit(pack, bins, bd, bdd, data);break;
		case MC : throw new UnsupportedOperationException(ValSel.MC.getDescription());
		case RANDOM : throw new UnsupportedOperationException(ValSel.RANDOM.getDescription());
		default:
			throw new ModelException("no value-selection heuristics");
		}
		solver.clearGoals();
		solver.addGoal( defaultConf.readBoolean(BatchSettings.DYNAMIC_REMOVALS) ? 
				new BatchDynRemovals(varSel, valSel, pack) : new AssignVar(varSel, valSel));
	}

	@Override
	public Solver buildSolver() {
		Solver solver = super.buildSolver();
		solver.read(model);
		setGoal(solver);
		solver.generateSearchStrategy();
		return solver;
	}

	@Override
	public void checkSolution() throws SolutionCheckerException {
		super.checkSolution();
		if( solver != null && solver.existsSolution()) {
			//translate solver solution
			int nbB = solver.getVar(nonEmptyB).getVal();
			bestSolution = new Batch[nbB];
			for (int k = 0; k < bestSolution.length; k++) { bestSolution[k] = new Batch(k);}
			for (int j = 0; j < data.nbJobs; j++) {
				final int batch = solver.getVar( jobInB[j]).getVal();
				bestSolution[batch].pack( data.sjobs[j]);
			}
			PDRScheduler.schedule(bestSolution, nbB, getPriorityDispatchingRule());
		}else if(getHeuristic().existsSolution() ){
			// DONE 21 sept. 2011 - what happens when there is no heuristics - created 21 sept. 2011 by Arnaud Malapert
			bestSolution = ( (BatchProcessingRHeuristics) getHeuristic()).getSolution();
		} else bestSolution = null;
	}

	@Override
	public String getValuesMessage() {
		if(bestSolution != null) {
			final StringBuilder b = new StringBuilder();
			for (int i = 0; i < bestSolution.length; i++) {
				b.append(bestSolution[i]).append(" ");
			}
			return b.toString();
		} else return "";
	}


	protected String getPrefixMsg() {
		return "P-BATCH";
	}
	
	@Override
	protected void logOnConfiguration() {
		super.logOnConfiguration();
		logMsg.storeConfiguration(data.toString()+"    "+getM()+" BATCHES");
		logMsg.storeConfiguration(BatchSettings.getFilteringMsg(defaultConf,getPrefixMsg()));
		logMsg.storeConfiguration(BatchSettings.getBranchingMsg(defaultConf));

	}	

	@Override
	protected abstract Object makeSolutionChart();

}