package pisco.shop;

import static choco.Choco.MAX_UPPER_BOUND;
import static choco.Choco.abs;
import static choco.Choco.eq;
import static choco.Choco.makeIntVarArray;
import static choco.Choco.max;
import static choco.Choco.minus;
import static choco.Choco.plus;
import static choco.Choco.scalar;
import parser.instances.BasicSettings;
import choco.Options;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.cp.solver.search.BranchingFactory;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;

public class AirLandProblem extends AbstractAirlandWeightedProblem {

	public AirLandProblem(BasicSettings settings) {
		super(settings);
	}

	@Override
	public Boolean preprocess() {
		setComputedLowerBound(0);
		return super.preprocess();
	}


	@Override
	public Model buildModel() {
		Model model = super.buildModel();
		IntegerVariable obj = buildObjective("obj",MAX_UPPER_BOUND);
		
		if(hasSymmetricPenalties) {
			IntegerVariable[] deviations = makeIntVarArray("D", nbJobs, 0, obj.getUppB(), Options.V_BOUND, Options.V_NO_DECISION);
			for (int i = 0; i < nbJobs; i++) {
				model.addConstraint(eq(deviations[i], abs(minus(tasks[i].end(),dueDates[i]))));
			}
			model.addVariable(obj);
			model.addConstraint(eq(obj, scalar(earlinessPenalties, deviations)));
		} else {
			IntegerVariable[] earliness = makeIntVarArray("E", nbJobs, 0, obj.getUppB(), Options.V_BOUND, Options.V_NO_DECISION);
			IntegerVariable[] tardiness = makeIntVarArray("T", nbJobs, 0, obj.getUppB(), Options.V_BOUND, Options.V_NO_DECISION);
			for (int i = 0; i < nbJobs; i++) {
				model.addConstraint(eq(earliness[i], max(0, minus(dueDates[i], tasks[i].end()))));
				model.addConstraint(eq(tardiness[i], max(0, minus(tasks[i].end(), dueDates[i]))));
			}
			model.addConstraint(eq(obj, plus(scalar(earlinessPenalties, earliness), scalar(tardinessPenalties, tardiness))));
		}
		return model;
	}

	@Override
	protected void setSecondaryGoals(PreProcessCPSolver solver) {
		// TODO - Awful branching : use at least a due date oriented branching! - created 14 mars 2012 by A. Malapert
		solver.addGoal( BranchingFactory.minDomMinVal(solver, solver.getVar(VariableUtils.getStartVariables(tasks))));
		super.setSecondaryGoals(solver);
	}

	

}
