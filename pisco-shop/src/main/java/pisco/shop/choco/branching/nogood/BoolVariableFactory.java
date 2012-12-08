package pisco.shop.choco.branching.nogood;

import choco.kernel.solver.variables.integer.IntDomainVar;

public interface BoolVariableFactory {

	int getVariableCount();
	
	IntDomainVar makeBoolVar();
	
	void validate();	
}
