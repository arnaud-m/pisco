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

import java.util.Comparator;


public final class JobComparatorFactory {

	private JobComparatorFactory() {}

	public static Comparator<Job> getLongestProcessingTime() {return LongestProcessingTime.SINGLOTON;}

	public static Comparator<Job> getShortestProcessingTime() {return ShortestProcessingTime.SINGLOTON;}

	public static Comparator<Job> getWeightedShortestProcessingTime() {return WeightedShortestProcessingTime.SINGLOTON;}

	public static Comparator<Job> getEarliestDueDate() {return EarliestDueDate.SINGLOTON;}

	public static Comparator<Job> getMinimalSlackTime() {return MinimalSlackTime.SINGLOTON;}

	public static Comparator<Job> getDecreasingSize() {return DecreasingSize.SINGLOTON;}

	public static Comparator<Job> getDecreasingParallelUnitWeight() {return DecreasingParallelUnitWeight.SINGLOTON;}

	public static Comparator<Job> getCompositeComparator(final Comparator<Job> comp1, final Comparator<Job> comp2) {
		return new Comparator<Job>() {

			@Override
			public int compare(Job o1, Job o2) {
				final int val = comp1.compare(o1, o2);
				return val == 0 ? comp2.compare(o1, o2) : val;
			}


		};
	}

}


final class LongestProcessingTime implements Comparator<Job> {

	public final static LongestProcessingTime SINGLOTON = new LongestProcessingTime();

	private LongestProcessingTime() {}

	@Override
	public int compare(Job o1, Job o2) {
		return (o1.duration > o2.duration ? -1 : (o1.duration == o2.duration ? 0 : 1));
	}

}

final class ShortestProcessingTime implements Comparator<Job> {

	public final static ShortestProcessingTime SINGLOTON = new ShortestProcessingTime();

	private ShortestProcessingTime() {}

	@Override
	public int compare(Job o1, Job o2) {
		return (o1.duration < o2.duration ? -1 : (o1.duration == o2.duration ? 0 : 1));
	}

}

final class WeightedShortestProcessingTime implements Comparator<Job> {

	public final static WeightedShortestProcessingTime SINGLOTON = new WeightedShortestProcessingTime();

	private WeightedShortestProcessingTime() {}

	@Override
	public int compare(Job o1, Job o2) {
		final int v1 = 	o2.weight * o1.duration;
		final int v2 = 	o1.weight * o2.duration;
		return (v1 < v2? -1 : (v1 == v2 ? 0 : 1));
	}

}

final class EarliestDueDate implements Comparator<Job> {

	public final static EarliestDueDate SINGLOTON = new EarliestDueDate();

	private EarliestDueDate() {}

	@Override
	public int compare(Job o1, Job o2) {
		return (o1.dueDate < o2.dueDate ? -1 : (o1.dueDate == o2.dueDate ? 0 : 1));
	}

}


final class MinimalSlackTime implements Comparator<Job> {

	public final static MinimalSlackTime SINGLOTON = new MinimalSlackTime();

	private MinimalSlackTime() {}

	@Override
	public int compare(Job o1, Job o2) {
		final int v1 = 	o1.dueDate - o1.duration;
		final int v2 = 	o2.dueDate - o2.duration;
		return (v1 > v2? 1 : (v1 == v2 ? 0 : -1));
	}

}

final class DecreasingSize implements Comparator<Job> {

	public final static DecreasingSize SINGLOTON = new DecreasingSize();

	private DecreasingSize() {}

	@Override
	public int compare(Job o1, Job o2) {
		return (o1.size > o2.size ? -1 : (o1.size == o2.size ? 0 : 1));
	}
}

final class DecreasingParallelUnitWeight implements Comparator<Job> {

	public final static DecreasingParallelUnitWeight SINGLOTON = new DecreasingParallelUnitWeight();

	private DecreasingParallelUnitWeight() {}

	@Override
	public int compare(Job o1, Job o2) {
		final int v1 = 	o1.size * o1.duration * o2.weight;
		final int v2 = 	o2.size * o2.duration * o1.weight;
		return (v1 < v2? -1 : (v1 == v2 ? 0 : 1));
	}

}
