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
package pisco.batch.visu;

import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.TableXYDataset;

import pisco.batch.data.Batch;


public class BatchProcessingDataset extends AbstractIntervalXYDataset implements TableXYDataset {

	private static final long serialVersionUID = -8883933973187993475L;

	public final Batch[] batches;

	private int seriesCount;
	
		
	public BatchProcessingDataset(Batch[] batches) {
		this.batches = batches;
		for (Batch b : this.batches) {
			seriesCount = Math.max(seriesCount, b.getCardinality());
		}
	}

	@Override
	public int getSeriesCount() {
		return seriesCount;
	}

	@Override
	public Comparable getSeriesKey(int series) {
		return Integer.valueOf(series);
	}

	@Override
	public Number getEndX(int series, int item) {
		return 	series < batches[item].getCardinality() ? batches[item].getStartingTime() + batches[item].getJob(series).getDuration() : null;
	}

	@Override
	public Number getEndY(int series, int item) {
		return  getY(series, item);
	}

	@Override
	public Number getStartX(int series, int item) {
		return getX(series, item);
	}

	@Override
	public Number getStartY(int series, int item) {
		return getY(series, item);
	}

	@Override
	public int getItemCount(int series) {
		return getItemCount();
	}

	@Override
	public Number getX(int series, int item) {
		return batches[item].getStartingTime();
	}

	@Override
	public Number getY(int series, int item) {
		return series < batches[item].getCardinality() ? batches[item].getJob(series).getSize() : null;
	}

	@Override
	public int getItemCount() {
		return batches.length;
	}


	

	
}
