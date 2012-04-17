package pisco.common.choco.branching;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;

public class LexMaxFakeBranching extends AbstractFakeBranching {

	private final IntDomainVar[] vars;

	public LexMaxFakeBranching(Solver solver) {
		this(solver, null);
	}
	
	public LexMaxFakeBranching(Solver solver, IntDomainVar[] vars) {
		super(solver);
		this.vars = vars;
	}

	@Override
	protected void setUp() {}

	@Override
	protected void doFakeBranching() throws ContradictionException {
		if(vars == null) {
			final int n = solver.getNbIntVars();
			for (int i = 0; i < n ; i++) {
				final IntDomainVar v = solver.getIntVarQuick(i);
				if( ! v.isInstantiated() ) {
					
					v.instantiate(v.getSup(), null, false);
				}
			}
		} else {
			for (IntDomainVar v : vars) {
				if( ! v.isInstantiated() ) {
					v.instantiate(v.getSup(), null, false);
				}
			}
		}
	}

	@Override
	protected void tearDown() {

	}

}
