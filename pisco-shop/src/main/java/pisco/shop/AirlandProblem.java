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
import static choco.Choco.MIN_LOWER_BOUND;
import static choco.Choco.clause;
import static choco.Choco.constantArray;
import static choco.Choco.disjunctive;
import static choco.Choco.eq;
import static choco.Choco.makeBooleanVar;
import static choco.Choco.precedenceDisjoint;
import static choco.Choco.sum;

import java.io.File;

import parser.absconparseur.tools.UnsupportedConstraintException;
import parser.instances.BasicSettings;
import pisco.shop.ChocoshopSettings.Branching;
import pisco.shop.parsers.AirlandParser;
import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.cp.solver.preprocessor.PreProcessConfiguration;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.common.util.tools.TaskUtils;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.global.scheduling.IResource;
import choco.kernel.visu.VisuFactory;
import choco.visu.components.chart.ChocoChartFactory;


/**
 * @author Arnaud Malapert
 *
 */
public class AirlandProblem extends AbstractDisjunctiveProblem {


	public int[] processingTimes;

	public int[] releaseDates;

	public int[] dueDates;

	public int[] deadlines;

	public int[][] setupTimes;


	protected TaskVariable[] tasks;

	protected IntegerVariable[][] disjuncts;

	public Constraint machine;

	public AirlandProblem(BasicSettings settings) {
		super(new AirlandParser(), settings);
		setChartManager(ChocoChartFactory.getJFreeChartManager());
		//	settings.putBoolean(BasicSettings.PREPROCESSING_HEURISTICS, false);
		//settings.putBoolean(BasicSettings.SOLUTION_REPORT, true);
		//		settings.putBoolean(BasicSettings.SOLUTION_EXPORT, true);
		//		settings.putBoolean(BasicSettings.LIGHT_MODEL, true);
		//	settings.putBoolean(BasicSettings.SOLUTION_EXPORT, true);
	}



	//****************************************************************//
	//********* Getters/Setters *******************************************//
	//****************************************************************//

	public final TaskVariable getTask(int job) {
		return tasks[job];
	}


	protected final int getHorizon() {
		// TODO - getHorizon - created 11 mars 2012 by A. Malapert
		return MAX_UPPER_BOUND;
	}

	@Override
	public void initialize() {
		super.initialize();
		processingTimes = null;
		releaseDates = null;
		dueDates = null;
		deadlines = null;
		tasks = null;
	}




	@Override
	public void load(File fichier) throws UnsupportedConstraintException {
		super.load(fichier);
		AirlandParser parser = (AirlandParser) this.parser;
		nbJobs = parser.nbJobs;
		releaseDates = parser.releaseDates;
		dueDates = parser.dueDates;
		deadlines = parser.deadlines;
		setupTimes = parser.setupTimes;
		//preprocess data
		processingTimes = new int[nbJobs];
		for (int i = 0; i < nbJobs; i++) {
			processingTimes[i] = MathUtils.min(setupTimes[i]);
			dueDates[i] += processingTimes[i];
			deadlines[i] += processingTimes[i];
			for (int j = 0; j < nbJobs; j++) {
				setupTimes[i][j] -= processingTimes[i];
			}
		}
	}




	//	@Override
	//	public Boolean preprocess() {
	//		super.preprocess();
	//		setComputedLowerBound(computeLoadLowerBound());
	//		if( defaultConf.readBoolean(BasicSettings.PREPROCESSING_HEURISTICS) ) {
	//			final CrashHeuristics heur = getCrashHeuristics();
	//			if( defaultConf.readBoolean(ChocoshopSettings.HEURISTICS_LEARNING) ) {
	//				initCrashLearning();
	//				if( crashLearning != null) {
	//					heur.setTimeLimit(crashLearning.getLearnedTimeLimit(this));
	//					heur.setIterationLimit(crashLearning.getLearnedIterationLimit(this));
	//				} else ChocoshopSettings.setLimits(defaultConf, heur);
	//			}else ChocoshopSettings.setLimits(defaultConf, heur);
	//			return super.preprocess();
	//		} else return null;
	//	}



	//****************************************************************//
	//********* Modeling *******************************************//
	//****************************************************************//



	/**
	 * @see pisco.shop.problem.AbstractChocoProblem#buildModel()
	 */
	@Override
	public Model buildModel() {
		CPModel model =new CPModel( nbJobs * nbJobs, 6 * nbJobs, 10, 10, 2 * nbJobs, 10, nbJobs );
		final int horizon = getHorizon();
		makespan = Choco.makeIntVar("makespan",0 , Math.min(getHorizon(), MAX_UPPER_BOUND), 
				Options.V_MAKESPAN,Options.V_BOUND, Options.V_NO_DECISION);
		model.addVariables(makespan);
		tasks = Choco.makeTaskVarArray("T", releaseDates, deadlines, constantArray(processingTimes), Options.V_BOUND);
		for (int i = 0; i < tasks.length; i++) {
			if( processingTimes[i] == 0) model.addConstraint(Choco.eq(tasks[i].start(), releaseDates[i]));
			tasks[i].end().addOption(Options.V_NO_DECISION);

		}
		disjuncts = new IntegerVariable[nbJobs][nbJobs];
		for (int i = 0; i < tasks.length; i++) {
			for (int j = i+1; j < tasks.length; j++) {
				disjuncts[i][j] = makeBooleanVar("b"+i+"_"+j);
				model.addConstraint( precedenceDisjoint(tasks[i], tasks[j], disjuncts[i][j], setupTimes[i][j], setupTimes[j][i]));
			}
		}

		// TODO - Validate transitive clauses - created 11 mars 2012 by A. Malapert
		for (int i = 0; i < tasks.length; i++) {
			for (int j = i+1; j < tasks.length; j++) {
				for (int k = j+1; k < tasks.length; k++) {
					model.addConstraints( 
							clause(new IntegerVariable[]{disjuncts[i][j], disjuncts[j][k]}, new IntegerVariable[]{disjuncts[i][k]}), 
							clause(new IntegerVariable[]{disjuncts[i][k]}, new IntegerVariable[]{disjuncts[i][j], disjuncts[j][k]})
					);
				}
			}
		}
		machine = disjunctive(tasks, Options.C_DISJ_NFNL, Options.C_DISJ_EF, Options.C_NO_DETECTION);
		if( ! defaultConf.readBoolean(BasicSettings.LIGHT_MODEL) ) {
			model.addConstraint(machine);
		}
		//TODO - add a factory for the objective variable  - created 11 mars 2012 by A. Malapert
		IntegerVariable obj = Choco.makeIntVar("flowtime",Math.max(getComputedLowerBound(), MIN_LOWER_BOUND), MAX_UPPER_BOUND, 
				Options.V_OBJECTIVE,Options.V_BOUND, Options.V_NO_DECISION);
		model.addConstraint( eq(obj, sum(VariableUtils.getEndVariables(tasks))));
		return model;
	}



	//****************************************************************//
	//********* Solver configuration *********************************//
	//****************************************************************//

	@Override
	protected IResource<?>[] generateFakeResources() {
		return TaskUtils.createFakeResources(solver, machine);
	}

	@Override
	public Solver buildSolver() {
		PreProcessCPSolver solver = new PreProcessCPSolver(this.defaultConf);
		BasicSettings.updateTimeLimit(solver.getConfiguration(),  - getPreProcTime());
		PreProcessConfiguration.cancelPreProcess(defaultConf);
		defaultConf.putTrue(PreProcessConfiguration.DISJUNCTIVE_MODEL_DETECTION);
		defaultConf.putTrue(PreProcessConfiguration.DMD_USE_TIME_WINDOWS);
		defaultConf.putFalse(PreProcessConfiguration.DMD_REMOVE_DISJUNCTIVE);
		solver.read(model);
		setGoals(solver);
		solver.generateSearchStrategy();
		return solver;
	}

	@Override
	public Boolean solve() {
//		//Print initial propagation		
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
		logMsg.storeConfiguration(ChocoshopSettings.getBranchingMsg(defaultConf));
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


	@Override
	public void makeReports() {
		super.makeReports();
		if( defaultConf.readBoolean(BasicSettings.SOLUTION_REPORT) ) {
			displayChart(disjSModel, VisuFactory.getDotManager());
		}
	}

	@Override
	protected Object makeSolutionChart() {
		// TODO - Solution Chart - created 11 mars 2012 by A. Malapert
		return null;
		//		return solver != null && solver.existsSolution() ?
		//				( defaultConf.readBoolean(BasicSettings.LIGHT_MODEL) ? 
		//						ChocoChartFactory.createUnaryHChart(getInstanceName()+" - Cmax="+objective, solver, tasks) :
		//							ChocoChartFactory.createUnaryHChart(getInstanceName()+" - Cmax="+objective, solver, machines)
		//						) : null;
	}

}


