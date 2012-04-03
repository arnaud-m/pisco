package pisco.common;

import gnu.trove.TIntArrayList;

import java.awt.Point;

public class JobPmtn extends AbstractJob {

	private final TIntArrayList timePeriods = new TIntArrayList();

	private int remainingDuration;

	public JobPmtn(int id) {
		super(id);
	}


	@Override
	public int getStartingTime() {
		return timePeriods.get(0);
	}

	@Override
	public int getCompletionTime() {
		return timePeriods.get(timePeriods.size() - 1);
	}

	public final int scheduleIn(int start, int end) {
		assert( end - start > 0 && remainingDuration > 0);
		final int length = end - start;
		timePeriods.add(start);
		if(length > remainingDuration) {
			end = start + remainingDuration;
			timePeriods.add(end);
		} else {
			timePeriods.add(end);
			remainingDuration -= length;
		}
		return end;
	}

	public final void scheduleBetween(int start, int end) {
		assert( end - start <= remainingDuration && remainingDuration > 0);
		timePeriods.add(start);
		timePeriods.add(end);
		remainingDuration -= (end - start);
	}

	
	@Override
	public void scheduleAt(int startingTime) {
		assert(startingTime >= 0 && remainingDuration > 0);
		timePeriods.add(startingTime);
		timePeriods.add(startingTime + remainingDuration);
		remainingDuration = 0;
	}

	@Override
	public void scheduleTo(int endingTime) {
		assert(endingTime > remainingDuration  && remainingDuration > 0);
		timePeriods.add(endingTime - remainingDuration);
		timePeriods.add(endingTime);
		remainingDuration = 0;
	}

	@Override
	public int getRemainingDuration() {
		return remainingDuration;
	}

	@Override
	public boolean isPartiallyScheduled() {
		return remainingDuration < getDuration();
	}

	public final boolean isScheduled() {
		return getRemainingDuration() == 0;
	}	

	public final int getTimePeriodCount() {
		return timePeriods.size()/2;
	}

	public final Point getTimePeriod(int i) {
		final int offset = 2 * i;
		return new Point(timePeriods.get(offset), timePeriods.get(offset + 1));
	}

	public final int getPeriodStart(int i) {
		return timePeriods.get(2 * i);
	}

	public final int getPeriodEnd(int i) {
		return timePeriods.get(2 * i + 1);
	}

}
