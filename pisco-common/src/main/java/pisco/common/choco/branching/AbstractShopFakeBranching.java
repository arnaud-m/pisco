/**
*  Copyright (c) 2011, Arnaud Malapert
*  All rights reserved.
*  Redistribution and use in source and binary forms, with or without
*  modification, are permitted provided that the following conditions are met:
*
*      * Redistributions of source code must retain the above copyright
*        notice, this list of conditions and the following disclaimer.
*      * Redistributions in binary form must reproduce the above copyright
*        notice, this list of conditions and the following disclaimer in the
*        documentation and/or other materials provided with the distribution.
*      * Neither the name of the Arnaud Malapert nor the
*        names of its contributors may be used to endorse or promote products
*        derived from this software without specific prior written permission.
*
*  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
*  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
*  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
*  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
*  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
*  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
*  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
*  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
*  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
*  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package pisco.common.choco.branching;

import java.util.logging.Level;

import choco.kernel.model.constraints.Constraint;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.variables.scheduling.TaskVar;

public abstract class AbstractShopFakeBranching extends FakeBranching {

	protected final TaskVar[] tasks;

	protected final AbstractSConstraint symBreakConstraint;
	
	public AbstractShopFakeBranching(Solver solver, Constraint symBreakConstraint) {
		super(solver);
		this.tasks = new TaskVar[ solver.getNbTaskVars()];
		for (int i = 0; i < tasks.length; i++) {
			tasks[i] = solver.getTaskVar(i);
		}
		if(symBreakConstraint != null) {
			this.symBreakConstraint = (AbstractSConstraint) solver.getCstr(symBreakConstraint);
		}else {
			this.symBreakConstraint = null;
		}
	}
	protected abstract void schedule() throws ContradictionException;

	/**
	 * disable the optional symmetry breaking constraint because it can raise a contradiction if the makespan is not instantiated.
	 * Then, we schedule all remaining tasks with {@link AbstractShopFakeBranching#schedule()}.
	 * Finally, we propagate to check the schedule.
	 */
	@Override
	protected void doFakeBranching() {
		if(symBreakConstraint != null) symBreakConstraint.setPassive(); //dont check the symmetry breaking constraint
		try {
			schedule();
			solver.propagate();
		} catch (ContradictionException e) {
			LOGGER.log(Level.SEVERE, "Fake branching raise a contradiction caused by "+e.getCause(), e);
			throw new SolverException("Fake branching raises a contradiction !");
		}
		if(symBreakConstraint != null) symBreakConstraint.setActive(); //dont check the symmetry breaking constraint
	}


}
