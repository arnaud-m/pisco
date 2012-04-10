/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
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

package pisco.single.choco.constraints;

import choco.cp.solver.constraints.global.scheduling.precedence.AbstractPrecedenceSConstraint;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;

/**
 *
 * Let b be a boolean variables; x1, x2 be two integer variables and k1, k2 two integers.
 * This constraint enforce x1 before x2 if b is true or x2 before x1 if b is false.
 * b = 1 <=>  (x1 + k1 <= x2) => (x1 + k1 = x2)
 * b = 0 <=> (x2 + k2 <= x1) => (x2 + k2 := x1)
 **/
public final class ModifyDueDateConstraint extends AbstractPrecedenceSConstraint {

	public ModifyDueDateConstraint(IntDomainVar x1, int k1, IntDomainVar x2, int k2, IntDomainVar b) {
		super( new IntDomainVar[]{b, x1, x2});
		this.k1 = k1;
		this.k2 = k2;
	}
	
	/**
	 * propagate if(vars[idx1] + k1 <= vars[idx2])
	 */
	protected final void weakPropagate(final int idx1, final int k1, final int idx2) throws ContradictionException {
		vars[idx2].updateInf(vars[idx1].getInf() + k1, this, false);
		vars[idx1].updateSup(vars[idx2].getSup() - k1, this, false);
	}

	
	@Override
	public final void propagateP1() throws ContradictionException {
		weakPropagate(1, k1, 2);
	}

	@Override
	public final void propagateP2() throws ContradictionException {
		weakPropagate(2, k2, 1);
	}


	/**
	 * isEntailed vars[idx1] + k1 <= vars[idx2]
	 */

	protected final Boolean isWeakEntailed(final int idx1, final int k1, final int idx2) {
//		if (vars[idx1].getSup() + k1 <= vars[idx2].getInf())
//			return Boolean.TRUE;
		if (vars[idx1].getInf() + k1 > vars[idx2].getSup())
			return Boolean.FALSE;
		return null;
	}


	
	@Override
	public final Boolean isP1Entailed() {
		return isWeakEntailed(1, k1, 2);
	}

	@Override
	public final Boolean isP2Entailed() {
		return isWeakEntailed(2, k2, 1);
	}

	@Override
	public boolean isSatisfied(int[] tuple) {
		return tuple[0] == 1 ? tuple[1] + k1 <= tuple[2] : tuple[2] + k2 <= tuple[1];
	}
	
	@Override
	public String pretty() {
		return pretty( "Modify Due Date", pretty(1, k1, 2), pretty(2, k2, 1) );
	}

	@Override
	public String toString() {
		return pretty();
	}

}
