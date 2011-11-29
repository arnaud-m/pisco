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
import choco.kernel.solver.variables.integer.IntDomainVar;

public final class BucketList implements IBatchFilteringRule {

	public final PBatchRelaxSConstraint cstr;

	private final Bucket[] bucketDueDateMap;

	public final Bucket[] buckets;

	public BucketList(PBatchRelaxSConstraint cstr) {
		super();
		this.cstr = cstr;
		bucketDueDateMap = new Bucket[cstr.data.getMaxDueDate() + 1];
		final Bucket[] tmp = new Bucket[cstr.data.nbJobs];
		int cpt = 0;
		for (int idx: cstr.data.getIndicesSortedByDueDate()) {
			final int dd = cstr.data.getDueDate(idx);
			if(bucketDueDateMap[dd] == null) {
				tmp[cpt] = new Bucket(dd);
				bucketDueDateMap[dd] = tmp[cpt];
				cpt++;
			}
		}
		buckets = cpt == tmp.length ? tmp : Arrays.copyOf(tmp, cpt);
	}

	public int getNbBuckets() {
		return buckets.length;
	}

	public Bucket getBucket(int idx) {
		return buckets[idx];
	}

	public Bucket getBucketAt(IntDomainVar dueDate) {
		return bucketDueDateMap[dueDate.getSup()];
	}

	public int getMakespan() {
		return buckets[0].totalDurationIfFirst;
	}

	public int getStartingTime(Bucket bucket) {
		return getMakespan() - bucket.totalDurationIfFirst;
	}

	public int getCompletionTime(Bucket bucket) {
		return getStartingTime(bucket) + bucket.duration;
	}

	public int getMaxLateness(Bucket bucket) {
		return getStartingTime(bucket) + bucket.latenessIfFirst;
	}

	public int getMaxLatenessAt(int dueDate) {
		return getMaxLateness(bucketDueDateMap[dueDate]);
	}

	public int getMaxLateness() {
		return buckets[0].getLatenessIfFirst();
	}

	@Override
	public void addJob(int job) {
		bucketDueDateMap[cstr.data.getDueDate(job)].addJob(job);
	}

	@Override
	public void addBatch(int b) {
		final IntDomainVar dv = cstr.getBDuration(b);
		final IntDomainVar ddv = cstr.getBDueDate(b);
		final Bucket c = bucketDueDateMap[ddv.getSup()];
		c.addContribution(dv.getInf());
		if( ! dv.isInstantiated() || ! ddv.isInstantiated()) {
			//can still prune the batch
			c.addBatch(b);
		}
	}

	@Override
	public final void reset() {
		for (Bucket c : buckets) {
			c.reset();
		}
	}

	@Override
	public void setUp() {
		//compute pricing value
		buckets[buckets.length - 1].insertLast();
		for (int i = buckets.length - 2 ; i >= 0; i--) {
			buckets[i].insertBefore(buckets[i+1]);
		}
	}

	@Override
	public void filter() throws ContradictionException {
		cstr.updateInfObj( getMaxLateness());
	}

	@Override
	public String toString() {
		return Arrays.toString(buckets);
	}


}
