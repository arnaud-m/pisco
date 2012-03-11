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
/**
 *
 */
package pisco.shop;


import static choco.Choco.MAX_UPPER_BOUND;

import java.io.File;

import parser.absconparseur.tools.UnsupportedConstraintException;
import parser.instances.BasicSettings;
import pisco.shop.ChocoshopSettings.Branching;
import pisco.shop.heuristics.CrashHeuristics;
import pisco.shop.heuristics.ICrashLearning;
import pisco.shop.parsers.IShopData;
import choco.Choco;
import choco.Options;
import choco.cp.common.util.preprocessor.detector.scheduling.DisjunctiveSModel;
import choco.cp.model.CPModel;
import choco.cp.solver.configure.RestartFactory;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.cp.solver.preprocessor.PreProcessConfiguration;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.common.util.tools.TaskUtils;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.global.scheduling.IResource;
import choco.kernel.solver.variables.scheduling.TaskVar;
import choco.kernel.visu.VisuFactory;
import choco.visu.components.chart.ChocoChartFactory;


/**
 * @author Arnaud Malapert
 *
 */
public class GenericShopProblem extends AbstractDisjunctiveProblem {

	public int nbMachines;

	public int[][] processingTimes;

	protected TaskVariable[][] tasks;

	public Constraint[] jobs;

	public Constraint[] machines;

	public Constraint[] forbIntJobs;

	public Constraint[] forbIntMachines;

	protected ICrashLearning crashLearning;

	public GenericShopProblem(IShopData parser, BasicSettings settings) {
		super(parser, settings);
		setChartManager(ChocoChartFactory.getJFreeChartManager());
		//	settings.putBoolean(BasicSettings.PREPROCESSING_HEURISTICS, false);
		//		settings.putBoolean(BasicSettings.SOLUTION_REPORT, true);
		//		settings.putBoolean(BasicSettings.SOLUTION_EXPORT, true);
		//		settings.putBoolean(BasicSettings.LIGHT_MODEL, true);
		//	settings.putBoolean(BasicSettings.SOLUTION_EXPORT, true);
	}



	//****************************************************************//
	//********* Getters/Setters *******************************************//
	//****************************************************************//


	public final ICrashLearning getCrashLearning() {
		return crashLearning;
	}

	public final void setCrashLearning(ICrashLearning crashLearning) {
		this.crashLearning = crashLearning;
	}


	protected void initCrashLearning() {
		LOGGER.config("shop...[heuristicsParamLearning][FAIL]");
	}


	public CrashHeuristics getCrashHeuristics() {
		if (getHeuristic() instanceof CrashHeuristics) {
			return (CrashHeuristics) getHeuristic();
		}
		return null;
	}


	public final TaskVariable getTask(int machine,int job) {
		return tasks[machine][job];
	}


	public final int getNbMachines() {
		return nbMachines;
	}

	protected final int getHorizon() {
		return isFeasible() == Boolean.TRUE ? objective.intValue() - 1 : MathUtils.sum(processingTimes) - 1;
	}

	@Override
	public void initialize() {
		super.initialize();
		nbMachines = 0;
		processingTimes = null;
		tasks = null;
		jobs = null;
		forbIntJobs = null;
		machines = null;
		forbIntMachines = null;
	}




	@Override
	public void load(File fichier) throws UnsupportedConstraintException {
		super.load(fichier);
		IShopData parser = (IShopData) this.parser;
		nbMachines = parser.getNbMachines(); //number of lines
		nbJobs = parser.getNbJobs(); //number of columns
		processingTimes = parser.getProcessingTimes();
		setHeuristic(new CrashHeuristics(this));
	}




	private final int computeLoadLowerBound() {
		//compute loads
		int[] jload=new int[this.nbJobs];
		int[] mload=new int[this.nbMachines];
		for (int i = 0; i < nbMachines; i++) {
			for (int j = 0; j < nbJobs; j++) {
				jload[j]+=this.processingTimes[i][j];
				mload[i]+=this.processingTimes[i][j];
			}
		}
		//find maximum load
		int max=Integer.MIN_VALUE;
		for (int i = 0; i < nbJobs; i++) {
			max= Math.max(max, jload[i]);
		}
		for (int i = 0; i < nbMachines; i++) {
			max= Math.max(max, mload[i]);
		}
		return max;
	}

	@Override
	public Boolean preprocess() {
		setComputedLowerBound(computeLoadLowerBound());
		if( defaultConf.readBoolean(BasicSettings.PREPROCESSING_HEURISTICS) ) {
			final CrashHeuristics heur = getCrashHeuristics();
			if( defaultConf.readBoolean(ChocoshopSettings.HEURISTICS_LEARNING) ) {
				initCrashLearning();
				if( crashLearning != null) {
					heur.setTimeLimit(crashLearning.getLearnedTimeLimit(this));
					heur.setIterationLimit(crashLearning.getLearnedIterationLimit(this));
				} else ChocoshopSettings.setLimits(defaultConf, heur);
			}else ChocoshopSettings.setLimits(defaultConf, heur);
			return super.preprocess();
		} else return null;
	}



	//****************************************************************//
	//********* Modeling *******************************************//
	//****************************************************************//


	protected final void addMachineResources(CPModel model) {
		machines = new Constraint[nbMachines];
		forbIntMachines = new Constraint[nbMachines];
		for (int i = 0; i < nbMachines; i++) {
			machines[i] = Choco.disjunctive("M_"+i, tasks[i]);
			forbIntMachines[i] = Choco.forbiddenIntervals("ForbInt_M_"+i, tasks[i]);
		}
		model.addConstraints(machines);
		model.addConstraints(forbIntMachines);
	}

	protected final void addJobResources(CPModel model) {
		jobs = new Constraint[nbJobs];
		forbIntJobs = new Constraint[nbJobs];
		for (int i = 0; i < nbJobs; i++) {
			final TaskVariable[] jtasks = ArrayUtils.getColumn(tasks, i);
			jobs[i] = Choco.disjunctive("J_"+i, jtasks);
			forbIntJobs[i] = Choco.forbiddenIntervals("ForbInt_J_"+i, jtasks);
		}
		model.addConstraints(jobs);
		model.addConstraints(forbIntJobs);
	}

	/**
	 * @see pisco.shop.problem.AbstractChocoProblem#buildModel()
	 */
	@Override
	public Model buildModel() {
		final int n = nbJobs * nbMachines;
		CPModel model =new CPModel( 2 * n, 4 * n, 10, 10, n, 10, n );
		makespan = buildObjective("makespan", MathUtils.sum(processingTimes) - 1);
		makespan.addOption(Options.V_MAKESPAN);
		model.addVariables(makespan);
		//m.addConstraint( Choco.geq( makespan, getComputedLowerBound()));
		tasks = Choco.makeTaskVarArray("T", 0, makespan.getUppB(), processingTimes, Options.V_BOUND);
		for (int i = 0; i < tasks.length; i++) {
			for (int j = 0; j < tasks[i].length; j++) {
				if( processingTimes[i][j] == 0) model.addConstraint(Choco.eq(tasks[i][j].start(), 0));
				tasks[i][j].end().addOption(Options.V_NO_DECISION);
			}
			model.addVariables(tasks[i]);
		}
		return model;
	}



	//****************************************************************//
	//********* Solver configuration *********************************//
	//****************************************************************//

	@Override
	protected IResource<?>[] generateFakeResources() {
		// FIXME - uncompatible with job-shop and flow-shop ?  - created 4 juil. 2011 by Arnaud Malapert
		return TaskUtils.createFakeResources(solver, ArrayUtils.append(jobs, machines));
	}
	
	@Override
	public Solver buildSolver() {
		PreProcessCPSolver solver = new PreProcessCPSolver(this.defaultConf);
		BasicSettings.updateTimeLimit(solver.getConfiguration(),  - getPreProcTime());
		PreProcessConfiguration.cancelPreProcess(defaultConf);
		final Branching br = ChocoshopSettings.getBranching(defaultConf);
		if( br != ChocoshopSettings.Branching.ST ||
				( br == ChocoshopSettings.Branching.ST && defaultConf.readBoolean(BasicSettings.LIGHT_MODEL) ) ) {
			defaultConf.putTrue(PreProcessConfiguration.DISJUNCTIVE_MODEL_DETECTION);
			if( defaultConf.readBoolean(BasicSettings.LIGHT_MODEL) ) {
				defaultConf.putTrue(PreProcessConfiguration.DMD_REMOVE_DISJUNCTIVE);
			}
		}
		solver.read(model);
		setGoals(solver);
		solver.generateSearchStrategy();
		return solver;
	}
	
	@Override
	public Boolean solve() {
		//Print initial propagation		
		//		try {
		//			solver.propagate();
		//		} catch (ContradictionException e) {
		//			e.printStackTrace();
		//		}
		//		if( defaultConf.readBoolean(BasicSettings.SOLUTION_REPORT) ) {
		//			displayChart(disjSModel, VisuFactory.getDotManager());
		//		}
		return super.solve();
	}



	@Override
	protected void logOnConfiguration() {
		super.logOnConfiguration();
		logMsg.storeConfiguration(
				ChocoshopSettings.getHeuristicsMsg(defaultConf) + 
				ChocoshopSettings.getBranchingMsg(defaultConf)
				);
	}



	@Override
	public String getValuesMessage() {
		if(solver != null && solver.existsSolution()) {
			final StringBuilder b = new StringBuilder();
			final int n = solver.getNbTaskVars();
			for (int i = 0; i < n; i++) {
				b.append(solver.getTaskVar(i).pretty()).append(' ');
			}
			return b.toString();
		}else return "";
	}

	protected String makeSolutionLog() {
		if(solver != null && solver.existsSolution()) {
			final StringBuilder b1 = new StringBuilder();
			final StringBuilder b2 = new StringBuilder();
			b1.append("#m n\n").append(nbMachines).append(' ').append(nbJobs).append('\n');
			b1.append("#Durations\n");
			b2.append("#Starts\n");

			for (int i = 0; i < nbMachines; i++) {
				for (int j = 0; j < nbJobs; j++) {
					//b1.append(solver.getVar(tasks[i][j]).start().getVal()).append(' ');
					final TaskVar tij = this.solver.getVar(tasks[i][j]);
					b1.append(tij.duration().getVal()).append(' ');
					b2.append(tij.start().getVal()).append(' ');

				}
				b1.append('\n');
				b2.append('\n');

			}
			b1.append('\n').append(b2);
			return b1.toString();
		} else return null;
	}


	@Override
	public void makeReports() {
		super.makeReports();
		if( defaultConf.readBoolean(BasicSettings.SOLUTION_REPORT) ) {
			displayChart(disjSModel, VisuFactory.getDotManager());
			displayChart(makeSolutionLog(), VisuFactory.getSolManager());
		}
	}

	@Override
	protected Object makeSolutionChart() {
		return solver != null && solver.existsSolution() ?
				( defaultConf.readBoolean(BasicSettings.LIGHT_MODEL) ? 
						ChocoChartFactory.createUnaryHChart(getInstanceName()+" - Cmax="+objective, solver, tasks) :
							ChocoChartFactory.createUnaryHChart(getInstanceName()+" - Cmax="+objective, solver, machines)
						) : null;
	}

}


