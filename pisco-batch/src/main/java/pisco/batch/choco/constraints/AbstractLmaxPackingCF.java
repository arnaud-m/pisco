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
package pisco.batch.choco.constraints;

import java.util.Arrays;

import choco.kernel.solver.ContradictionException;

public abstract class AbstractLmaxPackingCF implements IBatchPackingCF {

	public final PBatchRelaxSConstraint cstr;
	protected final int[] newBatchDurations;
	protected final int[] newBatchDueDates;
	protected int nbAvailableJobs;

	public AbstractLmaxPackingCF(PBatchRelaxSConstraint cstr) {
		super();
		this.cstr = cstr;
		newBatchDurations = new int[cstr.problem.getN()];
		newBatchDueDates = new int[cstr.problem.getN()];
	}

	@Override
	public void reset() {
		nbAvailableJobs = 0;
	}

	@Override
	public final void addBatch(int b) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void addJob(int j) {
		newBatchDurations[nbAvailableJobs] = cstr.data.getDuration(j);
		newBatchDueDates[nbAvailableJobs] = cstr.data.getDueDate(j);
		nbAvailableJobs++;
	}

	@Override
	public final boolean isEntailed() {
		return cstr.getNbBatches().getSup() - cstr.getNbNonEmpty() <= 1;
	}

	@Override
	public final void filterNaive() throws ContradictionException {
		final int nInf = cstr.getNbNonEmpty();
		cstr.updateInfNbB(nInf);
		cstr.updateSupNbB(nInf + nbAvailableJobs);
	}

	@Override
	public void setUp() {
		Arrays.sort(newBatchDurations, 0, nbAvailableJobs);
		Arrays.sort(newBatchDueDates, 0, nbAvailableJobs);
	}

	@Override
	public final void filter() throws ContradictionException {
		final int nInf = cstr.getNbNonEmpty();
		int nbB = cstr.getNbBatches().getInf() - nInf;
		if(nbB > 0) cstr.updateInfObj( computeRelaxedLmax(nbB));
		nbB = Math.min( nbAvailableJobs, cstr.getNbBatches().getSup() - nInf);
		final int lmaxSup = cstr.getObjSup();
		while( nbB > 1 &&  computeRelaxedLmax(nbB) > lmaxSup) {
			nbB--;
		}
		cstr.updateSupNbB(nInf + nbB);
	}

	protected abstract int computeRelaxedLmax(int nbNewBatches);

	private String toString(int[] tab) {
		if (nbAvailableJobs == 0)
	            return "[]";
	
	        StringBuilder b = new StringBuilder();
	        b.append('[').append(tab[0]);
	        for (int i = 1; i < nbAvailableJobs; i++) {
	            b.append(", ").append(tab[i]);
	        }
	        return b.append(']').toString();
	}

	@Override
	public String toString() {
		return nbAvailableJobs+"( d:"+ toString(newBatchDurations)+", dd:"+toString(newBatchDueDates)+" )";
	}
}