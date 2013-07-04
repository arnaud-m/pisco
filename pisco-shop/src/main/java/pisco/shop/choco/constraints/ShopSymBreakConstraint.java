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
package pisco.shop.choco.constraints;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractBinIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;

public final class ShopSymBreakConstraint extends AbstractBinIntSConstraint {

	public final TaskVar task;

	public ShopSymBreakConstraint(TaskVar task, IntDomainVar makespan) {
		super(task.start(), makespan);
		this.task = task;
		if( ! task.duration().isInstantiated()) {
			LOGGER.warning("handle only task with fixed duration");
		}
	}

	public void checkMakespan() throws ContradictionException {
		if( ! v1.isInstantiated() 
				&& 2 * v0.getInf() + task.duration().getVal() > v1.getSup() + 1 ) {
			//LOGGER.warning("fail");
			this.fail();
		}
	}

	public void updateStartingTime() throws ContradictionException {
		int ub = (v1.getSup() - task.duration().getVal() + 1)/2;
		v0.updateSup(ub, this, false);
	}

	@Override
	public void awakeOnInf(int varIdx) throws ContradictionException {
		checkMakespan();
	}


	@Override
	public void awakeOnSup(int varIdx) throws ContradictionException {
		updateStartingTime();
	}


	@Override
	public int getFilteredEventMask(int idx) {
		return  idx == 0 ? IntVarEvent.INCINFbitvector : IntVarEvent.DECSUPbitvector;
	}


	@Override
	public void propagate() throws ContradictionException {
		checkMakespan();
		updateStartingTime();
	}

	private final static boolean IS_ALWAYS_SATISFIED = true;

	@Override
	public boolean isSatisfied(int[] tuple) {
		return IS_ALWAYS_SATISFIED ? true : 2 * tuple[0] + task.duration().getVal() <= tuple[1] + 1;
	}
	
	

}
