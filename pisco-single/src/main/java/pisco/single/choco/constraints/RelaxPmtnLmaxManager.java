package pisco.single.choco.constraints;

import static choco.kernel.common.util.tools.VariableUtils.getIntVar;
import static choco.kernel.common.util.tools.VariableUtils.getTaskVar;

import java.util.List;

import pisco.single.Abstract1MachineProblem;
import choco.cp.model.managers.MixedConstraintManager;
import choco.cp.solver.CPSolver;
import choco.kernel.model.variables.Variable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;

public class RelaxPmtnLmaxManager extends
MixedConstraintManager {

	@Override
	public SConstraint makeConstraint(Solver solver, Variable[] variables,
			Object parameters, List<String> options) {
		if (solver instanceof CPSolver) {
			if (parameters instanceof Abstract1MachineProblem) {
				Abstract1MachineProblem problem = (Abstract1MachineProblem) parameters;
				TaskVar[] tvars = getTaskVar(solver, variables, 0, problem.getNbJobs());
				IntDomainVar[] dvars = getIntVar(solver, variables, problem.getNbJobs(), variables.length - 1);
				IntDomainVar ovar = solver.getVar(variables[variables.length - 1]);
				return new RelaxPmtnLmaxConstraint(problem, tvars, dvars, ovar);
			}
		}
		return null;
	}

}
