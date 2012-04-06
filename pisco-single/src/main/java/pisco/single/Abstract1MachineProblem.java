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
package pisco.single;


import static choco.Choco.MAX_UPPER_BOUND;
import static choco.Choco.clause;
import static choco.Choco.constantArray;
import static choco.Choco.disjunctive;
import static choco.Choco.makeBooleanVar;
import static choco.Choco.precedenceDisjoint;

import java.io.File;
import java.util.Arrays;

import parser.absconparseur.tools.UnsupportedConstraintException;
import parser.instances.BasicSettings;
import pisco.common.AbstractDisjunctiveProblem;
import pisco.common.DisjunctiveSettings;
import pisco.common.ITJob;
import pisco.common.JobUtils;
import pisco.single.parsers.Abstract1MachineParser;
import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.cp.solver.preprocessor.PreProcessConfiguration;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.comparator.TaskComparators;
import choco.kernel.common.util.tools.TaskUtils;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.global.scheduling.IResource;
import choco.kernel.solver.variables.scheduling.TaskVar;
import choco.visu.components.chart.ChocoChartFactory;


/**
 * @author Arnaud Malapert
 *
 */
public abstract class Abstract1MachineProblem extends AbstractDisjunctiveProblem {


	public ITJob[] jobs;

	public int[][] setupTimes;

	protected TaskVariable[] tasks;

	protected IntegerVariable[] disjuncts;

	public Constraint machine;

	public Abstract1MachineProblem(BasicSettings settings, Abstract1MachineParser parser) {
		super(parser, settings);
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

	public final int getDisjunctCount() {
		return (nbJobs * (nbJobs -1)) /2;
	}

	public final TaskVariable getTask(int job) {
		return tasks[job];
	}


	@Override
	public void initialize() {
		super.initialize();
		jobs = null;
		setupTimes = null;
		tasks = null;
		disjuncts = null;
		machine = null;
	}




	@Override
	public void load(File fichier) throws UnsupportedConstraintException {
		super.load(fichier);
		Abstract1MachineParser parser = (Abstract1MachineParser) this.parser;
		nbJobs = parser.nbJobs;
		jobs = parser.jobs;
		setupTimes = parser.setupTimes;
		/////////////////
		JobUtils.shiftLeftReleaseDates(jobs);
	}


	//****************************************************************//
	//********* Modelling *******************************************//
	//****************************************************************//


	public final static int getDisjunct(final int i,final int j, final int n) {
		//Flatten strictly upper triangular matrix (without diagonal)
		if(i < j) {
			final int row = n * i - (i * (i +1)) /2 ;
			final int column = j-i-1;
			return row + column;
		} else return -1;


	}

	/**
	 * @see pisco.shop.problem.AbstractChocoProblem#buildModel()
	 */
	@Override
	public Model buildModel() {
		CPModel model =new CPModel( nbJobs * nbJobs, 6 * nbJobs, 10, 10, 2 * nbJobs, 10, nbJobs );
		model.setDefaultExpressionDecomposition(true);
		makespan = Choco.makeIntVar("makespan",0 , MAX_UPPER_BOUND,
				Options.V_MAKESPAN,Options.V_BOUND, Options.V_NO_DECISION);
		model.addVariables(makespan);

		tasks = Choco.makeTaskVarArray("T", JobUtils.releaseDates(jobs), JobUtils.deadlines(jobs), constantArray(JobUtils.durations(jobs)), Options.V_BOUND);
		for (int i = 0; i < tasks.length; i++) {
			if( jobs[i].getDuration() == 0) model.addConstraint(Choco.eq(tasks[i].start(), jobs[i].getReleaseDate()));
			tasks[i].end().addOption(Options.V_NO_DECISION);

		}
		disjuncts = new IntegerVariable[getDisjunctCount()];
		int idx=0;
		for (int i = 0; i < tasks.length; i++) {
			for (int j = i+1; j < tasks.length; j++) {
				disjuncts[idx] = makeBooleanVar("b"+i+"_"+j);
				model.addConstraint( precedenceDisjoint(tasks[i], tasks[j], disjuncts[idx], setupTimes[i][j], setupTimes[j][i]));
				idx++;
			}
		}

		for (int i = 0; i < tasks.length; i++) {
			for (int j = i+1; j < tasks.length; j++) {
				//System.out.println(i+ "," + j + " -> "+ getDisjunct(i, j, nbJobs));
				IntegerVariable bij = disjuncts[getDisjunct(i, j, nbJobs)];
				for (int k = j+1; k < tasks.length; k++) {
					IntegerVariable bjk = disjuncts[getDisjunct(j, k, nbJobs)];
					IntegerVariable bik = disjuncts[getDisjunct(i, k, nbJobs)];
					model.addConstraints( 
							clause(new IntegerVariable[]{bij, bjk}, new IntegerVariable[]{bik}), 
							clause(new IntegerVariable[]{bik}, new IntegerVariable[]{bij,bjk})
							);
				}
			}
		}
		machine = disjunctive(tasks, Options.C_DISJ_NFNL, Options.C_DISJ_EF, Options.C_NO_DETECTION);
		if( ! defaultConf.readBoolean(BasicSettings.LIGHT_MODEL) ) {
			model.addConstraint(machine);
		}
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
		defaultConf.putFalse(PreProcessConfiguration.DMD_USE_TIME_WINDOWS);
		//defaultConf.putFalse(PreProcessConfiguration.DMD_REMOVE_DISJUNCTIVE);
		solver.read(model);
		//		solver.setLubyRestart(nbJobs/10+1, 3);
		//		solver.setRecordNogoodFromRestart(true);
		//		solver.setRestartLimit(5);
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
		ChocoLogging.flushLogs();
		return super.solve();
	}



	@Override
	protected void logOnConfiguration() {
		super.logOnConfiguration();
		logMsg.storeConfiguration(DisjunctiveSettings.getBranchingMsg(defaultConf));
		if (parser instanceof Abstract1MachineParser) {
			logMsg.storeConfiguration(((Abstract1MachineParser) parser).getParserMsg());
		}
	}





	@Override
	public String getValuesMessage() {
		if(solver != null && solver.existsSolution()) {
			final StringBuilder b = new StringBuilder();
			TaskVar[] tvars = solver.getVar(tasks);
			Arrays.sort(tvars, TaskComparators.makeEarliestStartingTimeCmp());
			for (int i = 0; i < tvars.length; i++) {
				b.append(tvars[i].pretty()).append(' ');
			}
			return b.toString();
		}else return "";
	}

	protected int[] computeSolutionSetups() {
		int[] setups = new int[nbJobs];
		TaskVar[] tvars = solver.getVar(tasks);
		boolean ordered = true;
		for (int i = 0; i < tvars.length; i++) {
			if(tvars[i].getID() != i) {
				return setups;
			}
		}
		Arrays.sort(tvars, TaskComparators.makeEarliestStartingTimeCmp());
		for (int i = 0; i < tvars.length - 1; i++) {
			final int j = tvars[i].getID();
			final int k = tvars[i+1].getID();
			if( tvars[i].getLCT() + setupTimes[j][k] > jobs[k].getReleaseDate()) {
				//the next task is postponed because of the setup time 
				setups[j] = setupTimes[j][k]; 
			}
		}
		return setups;
	}

	@Override
	public void makeReports() {
		super.makeReports();
		//		if( defaultConf.readBoolean(BasicSettings.SOLUTION_REPORT) ) {
		//			displayChart(disjSModel, VisuFactory.getDotManager());
		//		}
	}

	@Override
	protected Object makeSolutionChart() {
		return solver != null && solver.existsSolution() ?
				ChocoChartFactory.createGanttChart("", solver.getVar(tasks)) : null;
	}

}


