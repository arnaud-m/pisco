package pisco.single;
import static choco.Choco.MAX_UPPER_BOUND;
import static choco.Choco.eq;
import static choco.Choco.scalar;

import java.util.Arrays;

import parser.instances.BasicSettings;
import pisco.common.JobUtils;
import pisco.common.PDR1Scheduler;
import pisco.single.parsers.Abstract1MachineParser;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;


public class SingleMachineWFlow extends Abstract1MachineProblem {

	public SingleMachineWFlow(BasicSettings settings,
			Abstract1MachineParser parser) {
		super(settings, parser);
	}


	@Override
	public Boolean preprocess() {
		setComputedLowerBound( PDR1Scheduler.schedule1WFlow(Arrays.copyOf(jobs, jobs.length)));
		return super.preprocess();
	}

	@Override
	public Model buildModel() {
		Model model = super.buildModel();
		IntegerVariable obj = buildObjective("weighted-flowtime",MAX_UPPER_BOUND);
		model.addConstraint( eq(obj, scalar(JobUtils.weights(jobs), VariableUtils.getEndVariables(tasks))));
		return model;
	}



}
