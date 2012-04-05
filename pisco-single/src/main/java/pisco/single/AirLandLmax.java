package pisco.single;

import static choco.Choco.MAX_UPPER_BOUND;
import static choco.Choco.eq;
import static choco.Choco.makeIntVarArray;
import static choco.Choco.max;
import static choco.Choco.minus;
import parser.instances.BasicSettings;
import pisco.single.choco.constraints.RelaxConstraint_1_prec_rj_Lmax_Manager;
import choco.Options;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.visu.components.chart.ChocoChartFactory;

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
		IntegerVariable[] lateness = makeIntVarArray("L", nbJobs, 
				- MathUtils.max(dueDates), 
				MathUtils.max(releaseDates) + MathUtils.sum(processingTimes), 
				Options.V_BOUND, Options.V_NO_DECISION);
		for (int i = 0; i < nbJobs; i++) {
			model.addConstraint(eq(lateness[i], minus(tasks[i].end(), dueDates[i])));
		}
		model.addConstraint(max(lateness, obj));
		model.addConstraints(
				new ComponentConstraint(RelaxConstraint_1_prec_rj_Lmax_Manager.class, 
						this, 
						ArrayUtils.append(tasks, disjuncts, new IntegerVariable[]{obj}))				
				);
		return model;
	}



	@Override
	protected Object makeSolutionChart() {
		ChocoLogging.flushLogs();
		return solver != null && solver.existsSolution() ?
				ChocoChartFactory.createGanttChart("", solver.getVar(tasks), releaseDates, computeSolutionSetups(), dueDates) : null;
	}


}
