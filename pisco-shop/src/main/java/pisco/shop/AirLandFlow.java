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
import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.visu.components.chart.ChocoChartFactory;
import parser.instances.BasicSettings;


public class AirLandFlow extends AbstractAirlandProblem {

	public AirLandFlow(BasicSettings settings) {
		super(settings);
	}
	
	@Override
	public Boolean preprocess() {
		setComputedLowerBound(MathUtils.sum(processingTimes));
		return super.preprocess();
	}

	@Override
	public Model buildModel() {
		Model model = super.buildModel();
		IntegerVariable obj = buildObjective("flowtime",MAX_UPPER_BOUND);
		model.addConstraint( eq(obj, sum(VariableUtils.getEndVariables(tasks))));
		return model;
	}
	
	@Override
	protected Object makeSolutionChart() {
		return solver != null && solver.existsSolution() ?
					ChocoChartFactory.createGanttChart("", solver.getVar(tasks), releaseDates) : null;
	}


}
