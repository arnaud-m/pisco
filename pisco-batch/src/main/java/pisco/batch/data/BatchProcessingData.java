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
package pisco.batch.data;

import java.util.Arrays;
import java.util.Comparator;

import pisco.common.IJob;
import choco.kernel.common.util.comparator.IPermutation;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.common.util.tools.PermutationUtils;

public class BatchProcessingData {

	public final int nbJobs;

	/**
	 * jobs in orignal order
	 */
	public final BJob[] ojobs;

	/**
	 * jobs sorted according decreasing size. Ties are broken with decreasing lateness minimal (p_i -d_i).
	 */
	public final BJob[] sjobs;
	
	
	private final static int MIN=0; 
	private final static int MAX=1; 
	private final static int SUM=2;

	private final static int P=0; //processing time
	private final static int S=1; //size
	private final static int W=2; //weights
	private final static int D=3; //due dates
	private final static int DIM=4; //weights

	private final int[][] data;

	private final int[][] dataPP;

	public final int capacity;

	private int[] indicesSortedByDueDate;

	public BatchProcessingData(BJob[] jobs, int capacity) {
		super();
		//store original array
		this.nbJobs = jobs.length;
		ojobs = jobs;
		//store sorted array and initialize 
		sjobs = Arrays.copyOf(ojobs, nbJobs);
		data=new int[DIM][nbJobs];
		dataPP=new int[DIM][3];
		this.capacity = capacity;
		indicesSortedByDueDate = new int[nbJobs];
	}
	
	public void forceUnitWeights() {
		for (int i = 0; i < ojobs.length; i++) {
			ojobs[i].setWeight(1);
			sjobs[i].setWeight(1);
			data[W][i] = sjobs[i].getWeight();
		}
	}
	
	public final boolean testUnitWeights() {
		for (int i = 0; i < data[W].length; i++) {
			if(data[W][i] != 1) return false;
		}
		return true;
	}
	
	public void preprocess(Comparator<IJob> priorityRule) {
		if(priorityRule != null) {Arrays.sort(sjobs, priorityRule);}
		for (int i = 0; i < sjobs.length; i++) {
			data[P][i] = sjobs[i].getDuration();
			data[S][i] = sjobs[i].getSize();
			data[W][i] = sjobs[i].getWeight();
			data[D][i] = sjobs[i].getDueDate();

		}
		for (int t = 0; t < DIM; t++) {
			preprocess(t, data[t]);
		}
		// FIXME - Use SortUtils - created 22 sept. 2011 by Arnaud Malapert
		IPermutation sortByDueDates = PermutationUtils.getSortingPermuation(getDueDates());
		indicesSortedByDueDate = sortByDueDates.applyPermutation(ArrayUtils.zeroToN(nbJobs));
		if( getMaxSize() > capacity) {
			throw new IllegalArgumentException("Invalid Instance Format : some sizes are strictly greater than the capacity");
		}
	}

	private void preprocess(int t, int[] data) {
		dataPP[t][MIN] = data[0];
		dataPP[t][MAX] = data[0];
		dataPP[t][SUM] = data[0];
		for (int i = 1; i < data.length; i++) {
			if(dataPP[t][MIN] > data[i]) {dataPP[t][MIN]=data[i];}
			if(dataPP[t][MAX] < data[i]) {dataPP[t][MAX]=data[i];}
			dataPP[t][SUM] += data[i];
		}
	}

	
	public final BJob getJob(int i) {
		return sjobs[i];
	}

	public final BJob[] getJobs() {
		return sjobs;
	}
		
	public final int getNbJobs() {
		return nbJobs;
	}

	public final int[] getDurations() {
		return data[P];
	}

	public final int[] getSizes() {
		return data[S];
	}

	public final int[] getWeights() {
		return data[W];
	}

	public final int[] getDueDates() {
		return data[D];
	}

	public final int getDuration(int i) {
		return data[P][i];
	}

	public final int getSize(int i) {
		return data[S][i];
	}

	public final int getWeight(int i) {
		return data[W][i];
	}

	public final int getDueDate(int i) {
		return data[D][i];
	}

	
	public final int getMinDuration() {
		return dataPP[P][MIN];
	}

	public final int getMaxDuration() {
		return dataPP[P][MAX];
	}

	public final int getTotalDuration() {
		return dataPP[P][SUM];
	}

	public final int getMinSize() {
		return dataPP[S][MIN];
	}

	public final int getMaxSize() {
		return dataPP[S][MAX];
	}

	public final int getTotalSize() {
		return dataPP[S][SUM];
	}

	public final int getMinWeight() {
		return dataPP[W][MIN];
	}

	public final int getMaxWeight() {
		return dataPP[W][MAX];
	}

	public final int getTotalWeight() {
		return dataPP[W][SUM];
	}

	public final int getMinDueDate() {
		return dataPP[D][MIN];
	}

	public final int getMaxDueDate() {
		return dataPP[D][MAX];
	}

	public final int getCapacity() {
		return capacity;
	}

	
	public final int[] getIndicesSortedByDueDate() {
		return indicesSortedByDueDate;
	}

	private String displayInterval(int t) {
		return "["+dataPP[t][MIN]+","+dataPP[t][MAX]+"]";
	}
	
	@Override
	public String toString() {
		return displayInterval(P) + " DURATIONS    "+
				displayInterval(S) + " SIZES    " +
				displayInterval(W) + " WEIGHTS    " +
				displayInterval(D) + " DUE_DATES";
	}





}
