package pisco.shop;

import static choco.Choco.MAX_UPPER_BOUND;
import static choco.Choco.*;
import static choco.Choco.makeIntVar;
import static choco.Choco.sum;

import java.util.Arrays;

import choco.Options;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.comparator.TaskComparators;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.common.util.tools.TaskUtils;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.variables.scheduling.TaskVar;
import choco.visu.components.chart.ChocoChartFactory;
import parser.instances.BasicSettings;

public class AirLandLmax extends AbstractAirlandProblem {

	public AirLandLmax(BasicSettings settings) {
		super(settings);
	}

	@Override
	public Boolean preprocess() {
		setComputedLowerBound(- MathUtils.min(dueDates));
		return super.preprocess();
	}


	@Override
	public Model buildModel() {
		Model model = super.buildModel();
		IntegerVariable obj = buildObjective("Lmax",MAX_UPPER_BOUND);
		IntegerVariable[] lateness = makeIntVarArray("L", nbJobs, obj.getLowB(), obj.getUppB(), Options.V_BOUND, Options.V_NO_DECISION);
		for (int i = 0; i < nbJobs; i++) {
			model.addConstraint(eq(lateness[i], minus(tasks[i].end(), dueDates[i])));
		}
		model.addConstraint(max(lateness, obj));
		return model;
	}



	@Override
	protected Object makeSolutionChart() {
		ChocoLogging.flushLogs();
		return solver != null && solver.existsSolution() ?
				ChocoChartFactory.createGanttChart("", solver.getVar(tasks), releaseDates, computeSolutionSetups(), dueDates) : null;
	}


}
