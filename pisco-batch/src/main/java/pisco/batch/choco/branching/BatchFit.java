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
package pisco.batch.choco.branching;

import pisco.batch.data.BatchProcessingData;
import gnu.trove.TObjectIntHashMap;
import choco.cp.solver.constraints.global.pack.PackSConstraint;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

public class BatchFit implements ValSelector<IntDomainVar> {

	public final PackSConstraint pack;

	public final BatchProcessingData data;

	private final int maxDeltaDueDate;

	private final int maxDeltaDuration;

	private final IntDomainVar[] batchDurations; 

	private final IntDomainVar[] batchDueDates;

	private IntDomainVar lastVar = null;

	private int lastVarIndex = -1;

	private final TObjectIntHashMap<IntDomainVar> indexMap;

	public BatchFit(PackSConstraint cstr, IntDomainVar[] bins,IntDomainVar[] batchDurations, IntDomainVar[] batchDueDates, BatchProcessingData data) {
		super();
		this.pack = cstr;
		this.batchDurations = batchDurations;
		this.batchDueDates = batchDueDates;
		this.data=data;
		maxDeltaDueDate = data.getMaxDueDate() - data.getMinDueDate();
		maxDeltaDuration = data.getMaxDuration() - data.getMinDuration();
		indexMap = new TObjectIntHashMap<IntDomainVar>(bins.length);
		for (int i = 0; i < bins.length; i++) {
			indexMap.put(bins[i], i);
		}
	}

	private int getDelta(int a, int b) {
		return a < b ? b - a : a - b;
	}



	public final int getFitness(int j, int b) {
		return maxDeltaDueDate * getDelta(data.getDuration(j), batchDurations[b].getInf() )
		+ maxDeltaDuration* getDelta(data.getDueDate(j), batchDueDates[b].getSup());

	}

	protected final int getVarIndex(IntDomainVar x) {
		//fast check to avoid to search into the map
		if( x != lastVar) {
			lastVar = x;
			lastVarIndex = indexMap.get(x);
		}
		return lastVarIndex;
	}

	protected int getMinFitness(int j, DisposableIntIterator iter) {
		int batch= iter.next();
		int minFitness = getFitness(j, batch);
		while(iter.hasNext()) {
			int b =iter.next();
			final int fitness= getFitness(j, b);
			 if(fitness < minFitness) {
				batch= b;
				minFitness = fitness;
			}	
		} 
		return batch;
	}

	protected int getMinSpaceMinFitness(int j, DisposableIntIterator iter) {
		int batch= iter.next();
		int minFitness = getFitness(j, batch);
		int minSpace = pack.getRemainingSpace(batch);
		while(iter.hasNext()) {
			int b =iter.next();
			final int space=pack.getRemainingSpace(b);
			final int fitness= getFitness(j, b);
			if(space < minSpace) {
				batch = b;
				minFitness = fitness;
				minSpace = space;
			}else if( space == minSpace && fitness < minFitness) {
				batch= b;
				minFitness = fitness;
			}	
		} 
		return batch;
	}

	protected int getMinFitnessMinSpace(int j, DisposableIntIterator iter) {
		int batch= iter.next();
		int minFitness = getFitness(j, batch);
		int minSpace = pack.getRemainingSpace(batch);
		while(iter.hasNext()) {
			int b =iter.next();
			final int space=pack.getRemainingSpace(b);
			final int fitness= getFitness(j, b);
			if(fitness < minFitness) {
				batch = b;
				minFitness = fitness;
				minSpace = space;
			}else if(fitness == minFitness && space< minSpace) {
				batch= b;
				minSpace =space;
			}	
		} 
		return batch;
	}

	/**
	 * compute the "fitness" of a job into a batch using its size, duration and dueDate. 
	 */
	@Override
	public int getBestVal(IntDomainVar x) {
		//final int j = getVarIndex(x);
		//final DisposableIntIterator iter=x.getDomain().getIterator();
		//return getMinSpaceMinFitness(j, iter);
		//return getMinFitnessMinSpace(j, iter);
		return getMinFitness(getVarIndex(x), x.getDomain().getIterator());
	}


}
