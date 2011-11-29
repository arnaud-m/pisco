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
package pisco.batch.choco.constraints.set;

import choco.cp.solver.variables.set.SetVarEvent;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.set.AbstractMixedSetIntSConstraint;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;

public class SetIntCombEq extends AbstractMixedSetIntSConstraint {

	public final SetVar svar;
	public int[] coeffs;
	public final IntDomainVar ivar;
	
	public SetIntCombEq(SetVar svar, IntDomainVar ivar, int[] coeffs) {
		super(new Var[]{svar, ivar});
		this.svar = svar;
		this.coeffs = coeffs;
		this.ivar= ivar;
	}



	@Override
	public int getFilteredEventMask(int idx) {
		if(idx == 0) return SetVarEvent.REMENV_MASK + SetVarEvent.ADDKER_MASK;
		else return 0;
	}


	@Override
	public void awakeOnKer(int varIdx, int x) throws ContradictionException {
		ivar.updateInf(ivar.getInf() + coeffs[x], this, false);
	}



	@Override
	public void awakeOnEnv(int varIdx, int x) throws ContradictionException {
		ivar.updateSup(ivar.getSup() - coeffs[x], this, false);

	}


	@Override
	public void awakeOnInst(int varIdx) throws ContradictionException {
		//// FIXME - Bug on the propagation of events ? - created 12 oct. 2011 by Arnaud Malapert
		//super.awakeOnInst(varIdx);
	}



	@Override
	public void awakeOnEnvRemovals(int idx, DisposableIntIterator deltaDomain)
			throws ContradictionException {
		super.awakeOnEnvRemovals(idx, deltaDomain);
	}



	@Override
	public void awakeOnkerAdditions(int idx, DisposableIntIterator deltaDomain)
			throws ContradictionException {
		super.awakeOnkerAdditions(idx, deltaDomain);
	}


	@Override
	public void propagate() throws ContradictionException {
		final DisposableIntIterator iter = svar.getDomain().getEnveloppeIterator();
		int ksum = 0;
		int esum = 0;
		while(iter.hasNext()) {
			final int x = iter.next();
			if(svar.isInDomainKernel(x)) ksum+=coeffs[x];
			else esum += coeffs[x];
		}
		iter.dispose();
		ivar.updateInf(ksum, this, false);
		ivar.updateSup(ksum + esum, this, false);
	}

	@Override
	public boolean isSatisfied() {
		final DisposableIntIterator iter = svar.getDomain().getKernelIterator();
		int sum = 0;
		while(iter.hasNext()) {
			sum+= coeffs[iter.next()];
		}
		iter.dispose();
		return ivar.getVal() == sum;
	}



}
