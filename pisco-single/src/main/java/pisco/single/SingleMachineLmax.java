package pisco.single;

import static choco.Choco.MAX_UPPER_BOUND;
import static choco.Choco.eq;
import static choco.Choco.makeBooleanVar;
import static choco.Choco.makeIntVarArray;
import static choco.Choco.max;
import static choco.Choco.minus;
import static choco.Choco.precedenceDisjoint;

import java.util.Arrays;

import parser.instances.BasicSettings;
import pisco.common.JobUtils;
import pisco.common.PDR1Scheduler;
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
		super(settings, parser);
	}

	@Override
	public Boolean preprocess() {
		setComputedLowerBound( PDR1Scheduler.schedule1Lmax(Arrays.copyOf(jobs, jobs.length)));
		return super.preprocess();
	}


	@Override
	public Model buildModel() {
		Model model = super.buildModel();
		IntegerVariable obj = buildObjective("Lmax",MAX_UPPER_BOUND);
		//Add constraints which modify Due Dates on the fly
//		IntegerVariable[] dueDates = makeIntVarArray("D", nbJobs, 
//				JobUtils.minDueDate(jobs) - JobUtils.maxDuration(jobs), 
//				JobUtils.maxDueDate(jobs), 
//				Options.V_BOUND, Options.V_NO_DECISION);
//		int idx=0;
//		for (int i = 0; i < tasks.length; i++) {
//			for (int j = i+1; j < tasks.length; j++) {
//				model.addConstraint( precedenceDisjoint(dueDates[i], jobs[j].getDuration(), dueDates[j], jobs[i].getDuration(), disjuncts[idx]));
//				idx++;
//			}
//		}

		//Compute latenesses
		IntegerVariable[] lateness = makeIntVarArray("L", nbJobs, 
				- JobUtils.maxDueDate(jobs), MAX_UPPER_BOUND, 
				Options.V_BOUND, Options.V_NO_DECISION);
		for (int i = 0; i < nbJobs; i++) {
			model.addConstraint(eq(lateness[i], minus(tasks[i].end(), jobs[i].getDueDate())));
			//model.addConstraint(eq(lateness[i], minus(tasks[i].end(), dueDates[i])));
		}
		model.addConstraint(max(lateness, obj));

		//		model.addConstraints(
		//				new ComponentConstraint(RelaxPmtnLmaxManager.class, 
		//						this, 
		//						ArrayUtils.append(tasks, disjuncts, new IntegerVariable[]{obj}))				
		//				);
		return model;
	}


	@Override
	protected Object makeSolutionChart() {
		return solver != null && solver.existsSolution() ?
				ChocoChartFactory.createGanttChart("", solver.getVar(tasks), JobUtils.dueDates(jobs)) : null;
	}



}
