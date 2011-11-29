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




public final class BucketPackingCF extends AbstractLmaxPackingCF implements IBatchPackingCF {

	public final BucketList bucketL;

	public BucketPackingCF(PBatchRelaxSConstraint cstr) {
		super(cstr);
		this.bucketL = (BucketList) cstr.relaxF;
	}

	@Override
	public void setUp() {
		super.setUp();
		newBatchDueDates[0] = bucketL.getMaxLatenessAt(newBatchDueDates[0]);
		for (int i = 1; i < nbAvailableJobs; i++) {
			newBatchDurations[i] += newBatchDurations[i - 1];
			newBatchDueDates[i] = bucketL.getMaxLatenessAt(newBatchDueDates[i]);
		}
	}

	@Override
	public int computeRelaxedLmax(int nbNewBatches) {
		assert nbNewBatches <= nbAvailableJobs;
		int lmax = Integer.MIN_VALUE;
		final int offset = nbAvailableJobs - nbNewBatches;
		for (int i = 0; i < nbNewBatches; i++) {
			lmax = Math.max( lmax, newBatchDueDates[offset + i] + newBatchDurations[i]);
		}
		return lmax;
	}




}
