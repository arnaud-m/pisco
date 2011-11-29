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

import gnu.trove.TLinkableAdapter;
import gnu.trove.TLinkedList;

import java.util.ListIterator;


import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;


final class BatchToPrune extends TLinkableAdapter {

	private static final long serialVersionUID = 3986220986249737667L;

	public final int batch;

	public int minDuration;

	public int latenessIfAbsent;

	public BatchToPrune(int batch) {
		super();
		this.batch = batch;
	}

	public final int getMinDuration() {
		return minDuration;
	}

	public final void setMinDuration(int minDuration) {
		this.minDuration = minDuration;
	}

	public final int getLatenessIfAbsent() {
		return latenessIfAbsent;
	}

	public final void setLatenessIfAbsent(int latenessIfAbsent) {
		this.latenessIfAbsent = latenessIfAbsent;
	}

	public final int getBatch() {
		return batch;
	}

	public void updateLatenessIfAbsent(Bucket bucket) {
		latenessIfAbsent = Math.max( bucket.latenessAlone, bucket.duration + latenessIfAbsent);
	}

	public void setLatenessIfAbsent(int minDuration, Bucket bucket) {
		this.minDuration = minDuration;
		this.latenessIfAbsent = bucket.latenessIfFirst - minDuration;
	}
	
	@Override
	public String toString() {
		return batch+":"+latenessIfAbsent;
	}
}

public final class BucketAssignCF implements IBatchFilteringRule {

	public final PBatchRelaxSConstraint cstr;
	
	public final BucketList bucketL;

	private final BatchToPrune[] batchToPruneList;

	private final TLinkedList<BatchToPrune> batchToPrune;

	public BucketAssignCF(PBatchRelaxSConstraint cstr) {
		super();
		this.cstr = cstr;
		this.bucketL = (BucketList) cstr.relaxF;
		batchToPruneList = new BatchToPrune[cstr.nbBatches];
		for (int i = 0; i < cstr.nbBatches; i++) {
			batchToPruneList[i] = new BatchToPrune(i);
		}
		batchToPrune = new TLinkedList<BatchToPrune>();
	}


	@Override
	public void reset() {
		batchToPrune.clear();
	}
	
	

	@Override
	public void addBatch(int b) {
		throw new UnsupportedOperationException();
	}


	@Override
	public void addJob(int j) {
		throw new UnsupportedOperationException();
	}


	@Override
	public void setUp() {
		final DisposableIntIterator iter = cstr.candidateJobs.getIterator();
		while(iter.hasNext()) {
			bucketL.addJob(iter.next());
		}
		iter.dispose();
	}


	private void checkExtensionAt(Bucket bucket, int job, int maxDelta) throws ContradictionException {
		final int n = bucket.batchAtBucket.size();
		for (int i = 0; i < n; i++) {
			final int b = bucket.batchAtBucket.getQuick(i);
			if( cstr.data.getDuration(job) - cstr.getBDuration(b).getInf() > maxDelta) {
				cstr.removeAssignment(job, b);
			}
		}
	}

	private void checkTransferAt(Bucket bucket, int job, int bucketCompletionTime) throws ContradictionException {
		final ListIterator<BatchToPrune> iter = batchToPrune.listIterator();
		final int lmaxSup = cstr.getObjSup();
		while(iter.hasNext()) {
			final BatchToPrune bToPrune = iter.next();
			if(bucket.dueDate < cstr.getBDueDate(bToPrune.batch).getInf()) {
				iter.remove();
			}else {
				final int newCompletionTime = bucketCompletionTime + Math.max(bToPrune.minDuration, cstr.data.getDuration(job));
				if( Math.max( newCompletionTime - bucket.dueDate, newCompletionTime + bToPrune.latenessIfAbsent) > lmaxSup) {
					cstr.removeAssignment(job, bToPrune.batch);
				}

			}
		}
	}

	private void backwardUpdate(Bucket bucket) {
		//update tardiness if absent for previous batches
		final ListIterator<BatchToPrune> iter = batchToPrune.listIterator();
		while(iter.hasNext()) {
			iter.next().updateLatenessIfAbsent(bucket);
		}
		//add bacthes in bucket to the batches to prune
		final int m = bucket.batchAtBucket.size();
		for (int i = 0; i < m; i++) {
			final int b = bucket.batchAtBucket.getQuick(i);
			batchToPruneList[b].setLatenessIfAbsent(cstr.getBDuration(b).getInf(), bucket);
			batchToPrune.add(batchToPruneList[b]);
		}
	}


	@Override
	public final void filter() throws ContradictionException {
		for (int i = bucketL.getNbBuckets() - 1; i >= 0; i--) {
			final Bucket bucket = bucketL.getBucket(i);
			final int maxDelta = cstr.getObjSup() - bucketL.getMaxLateness(bucket);
			//Update duration of batches
			final int nb = bucket.batchAtBucket.size();
			for (int b = 0; b < nb; b++) {
				cstr.updateSupDuration(bucket.batchAtBucket.getQuick(b), maxDelta);
			}
			//Check insertions of jobs in this bucket
			final int nj = bucket.jobAtBucket.size();
			final int bucketCompletionTime = bucketL.getCompletionTime(bucket);
			for (int j = 0; j < nj; j++) {
				final int job = bucket.jobAtBucket.getQuick(j);
				if(cstr.data.getDuration(job) > maxDelta) {
					cstr.removeFromEmptyBatches(job);
					checkExtensionAt(bucket, job, maxDelta);
				}
				checkTransferAt(bucket, job, bucketCompletionTime);
			}
			backwardUpdate(bucket);
		}
	}
	
	@Override
	public String toString() {
		return batchToPrune.toString();
	}
}
