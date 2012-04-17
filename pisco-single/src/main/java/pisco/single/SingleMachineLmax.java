package pisco.single;

import static choco.Choco.*;
import static choco.Choco.eq;
import static choco.Choco.makeBooleanVar;
import static choco.Choco.makeIntVarArray;
import static choco.Choco.max;
import static choco.Choco.minus;
import static choco.Choco.precedenceDisjoint;

import java.util.Arrays;

import org.jfree.ui.HorizontalAlignment;

import net.sf.cglib.transform.impl.AddPropertyTransformer;

import parser.instances.BasicSettings;
import static pisco.common.JobUtils.*;
import pisco.common.CostFactory;
import pisco.common.DisjunctiveSettings;
import pisco.common.ICostFunction;
import pisco.common.ITJob;
import pisco.common.JobComparators;
import pisco.common.JobUtils;
import pisco.common.Pmtn1Scheduler;
import pisco.common.SchedulingBranchingFactory;
import static pisco.common.JobComparators.*;
import pisco.common.PDR1Scheduler;
import pisco.common.choco.branching.MaxFakeBranching;
import pisco.single.choco.constraints.ModifyDueDateManager;
import pisco.single.choco.constraints.RelaxLmaxConstraint;
import pisco.single.choco.constraints.RelaxLmaxManager;
import pisco.single.parsers.Abstract1MachineParser;
import choco.Options;
import choco.cp.solver.CPSolver;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.cp.solver.search.task.SetTimes;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.visu.VisuFactory;
import choco.visu.components.chart.ChocoChartFactory;

public class SingleMachineLmax extends Abstract1MachineProblem {

	private IntegerVariable objVar;

	private IntegerVariable[] dueDates;

	public SingleMachineLmax(BasicSettings settings,
			Abstract1MachineParser parser) {
		super(settings, parser, CostFactory.makeMaxCosts());
	}


	@Override
	public ICostFunction getCostFunction() {
		return CostFactory.getLateness();
	}

	@Override
	public Boolean preprocess() {
		if(defaultConf.readBoolean(SingleMachineSettings.INITIAL_LOWER_BOUND)) {
			final ITJob[] lbjobs = Arrays.copyOf(jobs, jobs.length);
			setComputedLowerBound(PDR1Scheduler.schedule1Lmax(lbjobs));
			SingleMachineRHeuristic heuristic = (SingleMachineRHeuristic) getHeuristic();
			if(JobUtils.isScheduledInTimeWindows(lbjobs)) {
				heuristic.storeSolution(lbjobs, getComputedLowerBound());
			} else {
				JobUtils.resetSchedule(lbjobs);
				final int lb = Pmtn1Scheduler.schedule1Lmax(lbjobs);

				if(! JobUtils.isInterrupted(lbjobs)) {
					setComputedLowerBound(lb);
					heuristic.storeSolution(lbjobs, getComputedLowerBound());	
				} else if(lb > getComputedLowerBound()) {
					setComputedLowerBound(lb);
				}
			}
		} else {
			setComputedLowerBound( JobUtils.minSlackTime(jobs));
		}
		return super.preprocess();
	}



	@Override
	public void initialize() {
		super.initialize();
		objVar = null;
		dueDates = null;
	}


	@Override
	protected int getHorizon() {
		return isFeasible() == Boolean.TRUE ? JobUtils.maxDueDate(jobs)  + objective.intValue(): maxReleaseDate(jobs) + sumDurations(jobs);
	}


	@Override
	public Model buildModel() {
		final Model model = super.buildModel();
		objVar = buildObjective("Lmax",MAX_UPPER_BOUND);
		if(defaultConf.readBoolean(SingleMachineSettings.MODIFY_DUE_DATES)) {
			///////////
			//Create Due date Variables 
			dueDates = new IntegerVariable[nbJobs];
			final int minDueDate = minDueDate(jobs) - maxDuration(jobs);
			for (int i = 0; i < tasks.length; i++) {
				dueDates[i] = makeIntVar("D"+i, minDueDate, jobs[i].getDueDate(), 
						Options.V_BOUND, Options.V_NO_DECISION);
			}
			///////////
			//Add constraints which modify Due Dates on the fly
			int idx=0;
			for (int i = 0; i < tasks.length; i++) {
				for (int j = i+1; j < tasks.length; j++) {
					// FIXME - Mistery - created 10 avr. 2012 by A. Malapert
					model.addConstraint( new ComponentConstraint( ModifyDueDateManager.class, null, 
							new IntegerVariable[]{dueDates[i], constant(jobs[j].getDuration()), 
						dueDates[j], constant(jobs[i].getDuration()), disjuncts[idx]})
							);
					idx++;
				}
			}
		}
		///////////
		//state lateness constraints
		final int maxDueDate = maxDueDate(jobs);
		IntegerVariable[] lateness = makeIntVarArray("L", nbJobs, 
				- maxDueDate(jobs), makespan.getUppB() - minDueDate(jobs), 
				Options.V_BOUND, Options.V_NO_DECISION);
		for (int i = 0; i < nbJobs; i++) {
			model.addConstraint(eq(lateness[i], minus(tasks[i].end(), jobs[i].getDueDate())));
		}
		///////////
		//create objective constraints
		model.addConstraints(
				max(lateness, objVar),
				geq( objVar, minus(makespan,maxDueDate(jobs)))
				);



		if( defaultConf.readBoolean(SingleMachineSettings.TASK_ORDERING) && ! hasSetupTimes() ) {
			////////////
			//Add pre-ordering constraints from dominance conditions
			final ITJob[] sjobs = Arrays.copyOf(jobs, nbJobs);
			Arrays.sort(sjobs, getCompositeComparator(getShortestProcessingTime(), JobComparators.getEarliestReleaseDate()));
			for (int i = 0; i < nbJobs - 1; i++) {
				final int d = sjobs[i].getDuration();
				int j = i +1;
				while(j < nbJobs && sjobs[j].getDuration() == d) {
					if(	sjobs[i].getDeadline() <= sjobs[j].getDeadline() && 
							sjobs[i].getDueDate() <= sjobs[j].getDueDate()) {
						// i precedes j
						final int ti = sjobs[i].getID();
						final int tj = sjobs[j].getID();
						model.addConstraint(precedence(tasks[ti], tasks[tj], setupTimes[ti][tj]));
					}
					j++;
				}
			}
		}


		return model;
	}




	@Override
	protected void setGoals(PreProcessCPSolver solver) {
		super.setGoals(solver);
		if(dueDates != null) {
			solver.addGoal(new MaxFakeBranching(solver, solver.getVar(dueDates)));
		}
	}

	@Override
	public Solver buildSolver() {
		CPSolver s = (CPSolver) super.buildSolver();
		if(SingleMachineSettings.stateRelaxationConstraint(this)) {
			////////////
			//Add relaxation constraint
			RelaxLmaxConstraint.canFailOnSolutionRecording = DisjunctiveSettings.getBranching(s.getConfiguration()) == SchedulingBranchingFactory.Branching.ST;
			// FIXME - Awful : can not really postponed until the disjunctive model is built - created 10 avr. 2012 by A. Malapert
			s.addConstraint(
					new ComponentConstraint(RelaxLmaxManager.class, 
							this, 
							ArrayUtils.append(tasks, new IntegerVariable[]{objVar})));				

		}
		return s;
	}


	@Override
	protected Object makeSolutionChart() {
		return solver != null && solver.existsSolution() ?
				ChocoChartFactory.createGanttChart("", solver.getVar(tasks), dueDates(jobs)) : null;
	}



}
