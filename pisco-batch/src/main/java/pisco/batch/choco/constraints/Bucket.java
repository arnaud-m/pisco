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

import gnu.trove.TIntArrayList;
import choco.Choco;

public final class Bucket {

	public final int dueDate;

	protected int duration;

	protected int latenessAlone;

	protected int latenessIfFirst;

	protected int totalDurationIfFirst;

	protected final TIntArrayList batchAtBucket = new TIntArrayList();

	protected final TIntArrayList jobAtBucket = new TIntArrayList();

	public Bucket() {
		dueDate = Choco.MAX_UPPER_BOUND;
		duration = 0;
		latenessAlone = Choco.MIN_LOWER_BOUND;
		latenessIfFirst = Choco.MIN_LOWER_BOUND;
	}
	public Bucket(int dueDate) {
		super();
		this.dueDate = dueDate;
	}


	public final int getDueDate() {
		return dueDate;
	}
	public final int getLatenessAlone() {
		return latenessAlone;
	}

	public final int getLatenessIfFirst() {
		return latenessIfFirst;
	}

	public final int getTotalDurationIfFirst() {
		return totalDurationIfFirst;
	}

	public void addJob(int job) {
		jobAtBucket.add(job);
	}

	public void addBatch(int batch) {
		batchAtBucket.add(batch);
	}

	public void addContribution(int contrib) {
		duration += contrib;
	}

	public void reset() {
		duration = 0;
		totalDurationIfFirst=0;
		latenessAlone = Choco.MIN_LOWER_BOUND;
		latenessIfFirst = Choco.MIN_LOWER_BOUND;
		batchAtBucket.resetQuick();
		jobAtBucket.resetQuick();
	}

	public void insertLast() {
		latenessAlone = duration - dueDate;
		latenessIfFirst = latenessAlone;
		totalDurationIfFirst = duration;
	}

	public void insertBefore(Bucket next) {
		latenessAlone = duration - dueDate;
		latenessIfFirst = Math.max( latenessAlone , duration + next.latenessIfFirst);
		totalDurationIfFirst = next.totalDurationIfFirst + duration;
	}

	public int simulateLatenessIfFirst(int contribution, Bucket next) {
		final int dur = duration + contribution;
		return Math.max( dur -dueDate, dur + next.latenessIfFirst);
	}
	
	@Override
	public String toString() {
		return "("+duration+", "+dueDate+")["+latenessIfFirst+
		"]( b:" +batchAtBucket+" "+", j:"+jobAtBucket+")" ;
	}


}