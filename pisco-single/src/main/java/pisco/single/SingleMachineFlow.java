package pisco.single;
import static choco.Choco.MAX_UPPER_BOUND;
import static choco.Choco.MIN_LOWER_BOUND;
import static choco.Choco.clause;
import static choco.Choco.constantArray;
import static choco.Choco.disjunctive;
import static choco.Choco.eq;
import static choco.Choco.makeBooleanVar;
import static choco.Choco.precedenceDisjoint;
import static choco.Choco.sum;

import java.util.Arrays;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.visu.components.chart.ChocoChartFactory;
import parser.instances.BasicSettings;
import pisco.common.JobUtils;
import pisco.common.PDR1Scheduler;
import pisco.single.parsers.Abstract1MachineParser;


public class SingleMachineFlow extends Abstract1MachineProblem {

	
	public SingleMachineFlow(BasicSettings settings,
			Abstract1MachineParser parser) {
		super(settings, parser);
	}

	@Override
	public Boolean preprocess() {
		setComputedLowerBound( PDR1Scheduler.schedule1Flow(Arrays.copyOf(jobs, jobs.length)));
		return super.preprocess();
	}

	@Override
	public Model buildModel() {
		Model model = super.buildModel();
		IntegerVariable obj = buildObjective("flowtime",MAX_UPPER_BOUND);
		model.addConstraint( eq(obj, sum(VariableUtils.getEndVariables(tasks))));
		return model;
	}
	


}
