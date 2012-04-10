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
package pisco.batch.heuristics;

import static pisco.common.JobComparators.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import pisco.batch.AbstractBatchingProblem;
import pisco.batch.data.BJob;
import pisco.batch.data.Batch;
import choco.kernel.common.opres.heuristics.AbstractRandomizedHeuristic;


public class BatchProcessingRHeuristics extends AbstractRandomizedHeuristic {

	private final AbstractBatchingProblem bpb;

	private BJob[] jobs;

	protected Batch[] batches;

	private final Random random = new Random();

	public BatchProcessingRHeuristics(AbstractBatchingProblem bpp) {
		super();
		this.bpb = bpp;
	}

	public final Batch[] getSolution() {
		int l = batches.length - 1;
		while(l >= 0 && batches[l].getDuration() == 0) {l--;}
		return Arrays.copyOf(batches, l + 1);
	}

	private final void resetBatches(int begin) {
		for (int k = begin; k < batches.length; k++) {
			batches[k].clear();
		}
	}

	@Override
	public void reset() {
		super.reset();
		bpb.getPriorityDispatchingRule().globalCostFunction.reset();
		final int n = bpb.getN();
		jobs = new BJob[n];
		if(batches != null  && batches.length == n) {
			resetBatches(0);
		} else {
			batches = new Batch[n];
			for (int k = 0; k < n; k++) {
				batches[k] = new Batch(k);
			}
		}
	}

	// DONE 21 sept. 2011 - Improve by packing and then OPTIMALLY Scheduling batches ! - created 21 sept. 2011 by Arnaud Malapert
	public final int apply(BJob[] jobs, int bestsol) {
		final int n = bpb.getN();
		int nbB = 0;
		if( n > 0) {
			final int c = bpb.getData().getCapacity();
			int j = 0;
			do {
				batches[nbB].clear();
				do {
					batches[nbB].parallelMerge(jobs[j++]);
				} while( j < n && batches[nbB].canPack(jobs[j], c));
				nbB++;
			}while(j < n);
		}
		resetBatches(nbB);
		// TODO - No more scheduling step ! - created 3 avr. 2012 by A. Malapert
//		if(  PDRScheduler.lazySequence(batches, nbB, bpb.getPriorityDispatchingRule(), bestsol)) {
//			bpb.getPriorityDispatchingRule().getGlobalCostFunction().getTotalCost();
//		} else {
//			resetBatches(0);
			return Integer.MAX_VALUE;
//		}
	}


	@Override
	protected int apply(int iteration, int bestsol, int seed) {
		// DONE 21 sept. 2011 - Need to copy the original array to be able to retrieve the best solution by considering only the current iteration and seed. - created 21 sept. 2011 by Arnaud Malapert
		System.arraycopy(bpb.getData().sjobs, 0, jobs, 0, bpb.getN());
		switch (iteration) {
		case 0: break; //order defined by the data manager
		case 1: Arrays.sort(jobs, getShortestProcessingTime());break;
		case 2: Arrays.sort(jobs, getLongestProcessingTime());break;
		case 3: Arrays.sort(jobs, getWeightedShortestProcessingTime());break;
		case 4: Arrays.sort(jobs, getDecreasingSize());break;
		case 5: Arrays.sort(jobs, getEarliestDueDate());break;
		case 6: Arrays.sort(jobs, getMinimalSlackTime());break;
		default	: random.setSeed(seed); Collections.shuffle(Arrays.asList(jobs), random);	break;
		}
		return apply(jobs, bestsol);
	}

	@Override
	public int getLowerBound() {
		return bpb.getComputedLowerBound();
	}

	@Override
	public void execute() {
		apply(new Random(bpb.getSeed()));
	}

	@Override
	public String toString() {
		return "Batch Processing Randomized List Heuristics";
	}



}
