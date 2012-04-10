package pisco.single;

import static choco.Choco.*;
import static choco.Choco.eq;
import static choco.Choco.makeBooleanVar;
import static choco.Choco.makeIntVarArray;
import static choco.Choco.max;
import static choco.Choco.minus;
import static choco.Choco.precedenceDisjoint;

import java.util.Arrays;

import net.sf.cglib.transform.impl.AddPropertyTransformer;

import parser.instances.BasicSettings;
import static pisco.common.JobUtils.*;
import pisco.common.CostFactory;
import pisco.common.ICostFunction;
import pisco.common.ITJob;
import pisco.common.JobUtils;
import pisco.common.Pmtn1Scheduler;
import static pisco.common.JobComparators.*;
import pisco.common.PDR1Scheduler;
import pisco.single.choco.constraints.ModifyDueDateManager;
import pisco.single.choco.constraints.RelaxPmtnLmaxManager;
import pisco.single.parsers.Abstract1MachineParser;
import choco.Options;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.visu.components.chart.ChocoChartFactory;

public class SingleMachineLmax extends Abstract1MachineProblem {

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
		ITJob[] lbjobs = Arrays.copyOf(jobs, jobs.length);
		setComputedLowerBound(PDR1Scheduler.schedule1Lmax(lbjobs));
		if(JobUtils.isScheduledInTimeWindows(lbjobs)) {

		} else {
			setComputedLowerBound(Pmtn1Scheduler.schedule1Lmax(lbjobs));
			if(JobUtils.isInterrupted(lbjobs)) {
				
			}
		}

		return super.preprocess();
	}


	@Override
	public Model buildModel() {
		Model model = super.buildModel();
		IntegerVariable obj = buildObjective("Lmax",MAX_UPPER_BOUND);
		///////////
		//Create Due date Variables 
		IntegerVariable[] dueDates = new IntegerVariable[nbJobs];
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
				//				model.addConstraint( new ComponentConstraint( ModifyDueDateManager.class, null, 
				//						new IntegerVariable[]{dueDates[i], constant(jobs[j].getDuration()), 
				//					dueDates[j], constant(jobs[i].getDuration()), disjuncts[idx]})
				//						);
				idx++;
			}
		}

		///////////
		//state lateness constraints
		IntegerVariable[] lateness = makeIntVarArray("L", nbJobs, 
				- maxDueDate(jobs), MAX_UPPER_BOUND, 
				Options.V_BOUND, Options.V_NO_DECISION);
		for (int i = 0; i < nbJobs; i++) {
			model.addConstraint(eq(lateness[i], minus(tasks[i].end(), jobs[i].getDueDate())));
		}
		///////////
		//create objective constraints
		model.addConstraints(
				max(lateness, obj),
				geq( obj, minus(makespan,maxDueDate(jobs)))
				);

		////////////
		//Add relaxation constraint
		// TODO - Set parameters to jobs - created 6 avr. 2012 by A. Malapert
		//		model.addConstraints(
		//				new ComponentConstraint(RelaxPmtnLmaxManager.class, 
		//						this, 
		//						ArrayUtils.append(tasks, disjuncts, new IntegerVariable[]{obj}))				
		//				);

		if( ! hasSetupTimes() ) {
			////////////
			//Add pre-ordering constraints from dominance conditions
			final ITJob[] sjobs = Arrays.copyOf(jobs, nbJobs);
			Arrays.sort(sjobs, getCompositeComparator(getShortestProcessingTime(), getEarliestDueDate()));
			for (int i = 0; i < nbJobs - 1; i++) {
				final int d = sjobs[i].getDuration();
				int j = i +1;
				while(j < nbJobs && sjobs[j].getDuration() == d) {
					if(sjobs[i].getDeadline() <= sjobs[j].getDeadline() && 
							sjobs[i].getDueDate() <= sjobs[j].getDueDate()) {
						// i precedes j
						final int ti = sjobs[i].getID();
						final int tj = sjobs[j].getID();
						//model.addConstraint(precedence(tasks[ti], tasks[tj], setupTimes[ti][tj]));
					}
					j++;
				}
			}
		}
		return model;
	}


	@Override
	protected Object makeSolutionChart() {
		return solver != null && solver.existsSolution() ?
				ChocoChartFactory.createGanttChart("", solver.getVar(tasks), dueDates(jobs)) : null;
	}



}