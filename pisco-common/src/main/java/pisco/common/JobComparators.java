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
package pisco.common;

import java.util.Comparator;


public final class JobComparators {

	private JobComparators() {}

	public static Comparator<IJob> getShortestProcessingTime() {return ShortestProcessingTime.SINGLOTON;}

	public static Comparator<IJob> getLongestProcessingTime() {return LongestProcessingTime.SINGLOTON;}

	public static Comparator<AbstractJob> getShortestRemainingProcessingTime() {return ShortestRemainingProcessingTime.SINGLOTON;}
	
	public static Comparator<AbstractJob> getLongestRemainingProcessingTime() {return LongestRemainingProcessingTime.SINGLOTON;}

	public static Comparator<IJob> getWeightedShortestProcessingTime() {return WeightedShortestProcessingTime.SINGLOTON;}

	public static Comparator<IJob> getEarliestReleaseDate() {return EarliestReleaseDate.SINGLOTON;}
	
	public static Comparator<IJob> getEarliestDueDate() {return EarliestDueDate.SINGLOTON;}

	public static Comparator<IJob> getLatestDueDate() {return LatestDueDate.SINGLOTON;}

	public static Comparator<IJob> getMinimalSlackTime() {return MinimalSlackTime.SINGLOTON;}

	public static Comparator<IJob> getDecreasingSize() {return DecreasingSize.SINGLOTON;}

	public static Comparator<IJob> getDecreasingParallelUnitWeight() {return DecreasingParallelUnitWeight.SINGLOTON;}

	public static Comparator<IJob> getCompositeComparator(final Comparator<IJob> comp1, final Comparator<IJob> comp2) {
		return new Comparator<IJob>() {

			@Override
			public int compare(IJob o1, IJob o2) {
				final int val = comp1.compare(o1, o2);
				return val == 0 ? comp2.compare(o1, o2) : val;
			}


		};
	}

}
final class ShortestProcessingTime implements Comparator<IJob> {

	public final static ShortestProcessingTime SINGLOTON = new ShortestProcessingTime();

	private ShortestProcessingTime() {}

	@Override
	public int compare(IJob o1, IJob o2) {
		final int v1 = 	o1.getDuration();
		final int v2 = 	o2.getDuration();
		return (v1 < v2 ? -1 : (v1 == v2) ? 0 : 1);
	}
}


final class LongestProcessingTime implements Comparator<IJob> {

	public final static LongestProcessingTime SINGLOTON = new LongestProcessingTime();

	private LongestProcessingTime() {}

	@Override
	public int compare(IJob o1, IJob o2) {
		final int v1 = 	o1.getDuration();
		final int v2 = 	o2.getDuration();
		return (v1 > v2 ? -1 : (v1 == v2) ? 0 : 1);
	}

}


final class ShortestRemainingProcessingTime implements Comparator<AbstractJob> {

	public final static ShortestRemainingProcessingTime SINGLOTON = new ShortestRemainingProcessingTime();

	private ShortestRemainingProcessingTime() {}

	@Override
	public int compare(AbstractJob o1, AbstractJob o2) {
		final int v1 = 	o1.getRemainingDuration();
		final int v2 = 	o2.getRemainingDuration();
		return (v1 < v2 ? -1 : (v1 == v2) ? 0 : 1);
	}

}

final class LongestRemainingProcessingTime implements Comparator<AbstractJob> {

	public final static LongestRemainingProcessingTime SINGLOTON = new LongestRemainingProcessingTime();

	private LongestRemainingProcessingTime() {}

	@Override
	public int compare(AbstractJob o1, AbstractJob o2) {
		final int v1 = 	o1.getRemainingDuration();
		final int v2 = 	o2.getRemainingDuration();
		return (v1 > v2 ? -1 : (v1 == v2) ? 0 : 1);
	}

}


final class WeightedShortestProcessingTime implements Comparator<IJob> {

	public final static WeightedShortestProcessingTime SINGLOTON = new WeightedShortestProcessingTime();

	private WeightedShortestProcessingTime() {}

	@Override
	public int compare(IJob o1, IJob o2) {
		final int v1 = 	o2.getWeight()* o1.getDuration();
		final int v2 = 	o1.getWeight() * o2.getDuration();
		return (v1 < v2 ? -1 : (v1 == v2 ? 0 : 1));
	}

}

final class EarliestReleaseDate implements Comparator<IJob> {

	public final static EarliestReleaseDate SINGLOTON = new EarliestReleaseDate();

	private EarliestReleaseDate() {}

	@Override
	public int compare(IJob o1, IJob o2) {
		final int v1 = 	o1.getReleaseDate();
		final int v2 = 	o2.getReleaseDate();
		return (v1 < v2 ? -1 : (v1 == v2) ? 0 : 1);
	}

}

final class EarliestDueDate implements Comparator<IJob> {

	public final static EarliestDueDate SINGLOTON = new EarliestDueDate();

	private EarliestDueDate() {}

	@Override
	public int compare(IJob o1, IJob o2) {
		final int v1 = 	o1.getDueDate();
		final int v2 = 	o2.getDueDate();
		return (v1 < v2 ? -1 : (v1 == v2) ? 0 : 1);
	}

}


final class LatestDueDate implements Comparator<IJob> {

	public final static LatestDueDate SINGLOTON = new LatestDueDate();

	private LatestDueDate() {}

	@Override
	public int compare(IJob o1, IJob o2) {
		final int v1 = 	o1.getDueDate();
		final int v2 = 	o2.getDueDate();
		return (v1 > v2 ? -1 : (v1 == v2) ? 0 : 1);
	}

}

final class MinimalSlackTime implements Comparator<IJob> {

	public final static MinimalSlackTime SINGLOTON = new MinimalSlackTime();

	private MinimalSlackTime() {}

	@Override
	public int compare(IJob o1, IJob o2) {
		final int v1 = 	o1.getDueDate() - o1.getDuration();
		final int v2 = 	o2.getDueDate() - o2.getDuration();
		return (v1 > v2? 1 : (v1 == v2 ? 0 : -1));
	}

}

final class DecreasingSize implements Comparator<IJob> {

	public final static DecreasingSize SINGLOTON = new DecreasingSize();

	private DecreasingSize() {}

	@Override
	public int compare(IJob o1, IJob o2) {
		final int v1 = 	o1.getSize();
		final int v2 = 	o2.getSize();
		return (v1 > v2 ? -1 : (v1 == v2) ? 0 : 1);
	}
}


final class DecreasingParallelUnitWeight implements Comparator<IJob> {

	public final static DecreasingParallelUnitWeight SINGLOTON = new DecreasingParallelUnitWeight();

	private DecreasingParallelUnitWeight() {}

	@Override
	public int compare(IJob o1, IJob o2) {
		final int v1 = 	o1.getSize() * o1.getDuration() * o2.getWeight();
		final int v2 = 	o2.getSize() * o2.getDuration() * o1.getWeight();
		return (v1 < v2? -1 : (v1 == v2 ? 0 : 1));
	}

}
