package pisco.shop.choco.constraints;

import static choco.kernel.common.util.tools.VariableUtils.getIntVar;
import static choco.kernel.common.util.tools.VariableUtils.getTaskVar;

import java.util.List;

import pisco.shop.AbstractAirlandProblem;
import choco.cp.model.managers.MixedConstraintManager;
import choco.cp.solver.CPSolver;
import choco.kernel.model.variables.Variable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;

public class RelaxConstraint_1_prec_rj_Lmax_Manager extends
MixedConstraintManager {

	@Override
	public SConstraint makeConstraint(Solver solver, Variable[] variables,
			Object parameters, List<String> options) {
		if (solver instanceof CPSolver) {
			if (parameters instanceof AbstractAirlandProblem) {
				AbstractAirlandProblem problem = (AbstractAirlandProblem) parameters;
				TaskVar[] tvars = getTaskVar(solver, variables, 0, problem.getNbJobs());
				IntDomainVar[] dvars = getIntVar(solver, variables, problem.getNbJobs(), variables.length - 1);
				IntDomainVar ovar = solver.getVar(variables[variables.length - 1]);
				return new RelaxConstraint_1_prec_rj_Lmax(problem, tvars, dvars, ovar);
			}
		}
		return null;
	}

}
