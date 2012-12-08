package pisco.shop.choco.branching.nogood;

import choco.kernel.solver.variables.integer.IntDomainVar;

public interface BoolConstraintFactory {

	void postBoolConstraint(IntDomainVar[] posLits, IntDomainVar[] negLits);
}
