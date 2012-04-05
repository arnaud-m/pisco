package pisco.single;
import static choco.Choco.MAX_UPPER_BOUND;
import static choco.Choco.eq;
import static choco.Choco.scalar;
import parser.instances.BasicSettings;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;


public class SingleMachineWFlow extends Abstract1MachineWeightedProblem {

	public SingleMachineWFlow(BasicSettings settings) {
		super(settings);
	}
	
	private int getWeight(int i) {
		return earlinessPenalties[i];
	}
	@Override
	public Boolean preprocess() {
		int lb = 0;
		for (int i = 0; i < nbJobs; i++) {
			lb+= getWeight(i) * processingTimes[i];
		}
		setComputedLowerBound(lb);
		return super.preprocess();
	}

	@Override
	public Model buildModel() {
		Model model = super.buildModel();
		IntegerVariable obj = buildObjective("weighted-flowtime",MAX_UPPER_BOUND);
		model.addConstraint( eq(obj, scalar(earlinessPenalties, VariableUtils.getEndVariables(tasks))));
		return model;
	}
	
	

}
