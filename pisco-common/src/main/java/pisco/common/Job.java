package pisco.common;

import java.awt.Point;

import choco.Choco;

public class Job extends AbstractJob {

	//schedule
	private int startingTime = Choco.MIN_LOWER_BOUND;
	private int completionTime = Choco.MIN_LOWER_BOUND;

	public Job(int id) {
		super(id);
	}

	public final int getStartingTime() {
		return startingTime;
	}

	public final int getCompletionTime() {
		return completionTime;
	}
	
	protected final void setStartingTime(int startingTime) {
		this.startingTime = startingTime;
	}

	protected final void setCompletionTime(int completionTime) {
		this.completionTime = completionTime;
	}

	public final void scheduleIn(int start, int end) {
		startingTime = start;
		completionTime = start + getDuration();
		if(completionTime > end) {
			throw new IllegalArgumentException("Preemption not allowed");
		}
		assert(startingTime >= 0);
	}

	public final void scheduleBetween(int start, int end) {
		if(end - start == getDuration()) {
			startingTime = start;
			completionTime = end;
		} else {
			throw new IllegalArgumentException("Preemption not allowed");
		}
		assert(startingTime >= 0);
	}

	public final void scheduleTo(int endingTime) {
		completionTime = endingTime;
		startingTime = completionTime - getDuration();
		assert(startingTime >= 0);
	}

	public final void scheduleAt(int startingTime) {
		this.startingTime = startingTime;
		completionTime = startingTime + getDuration();
		assert(this.startingTime >= 0);
	}

	
	@Override
	public boolean isPartiallyScheduled() {
		return false;
	}

	public final boolean isScheduled() {
		return completionTime - startingTime >= getDuration();
	}	

	@Override
	public int getRemainingDuration() {
		return isScheduled() ? 0 : getDuration();
	}

	public final int getTimePeriodCount() {
		return isScheduled() ? 1 : 0;
	}

	public final Point getTimePeriod(int i) {
		return isScheduled() && i == 0 ? new Point(startingTime, completionTime) : null;
	}

	public final int getPeriodStart(int i) {
		return i == 0 ? startingTime : Choco.MIN_LOWER_BOUND;
	}


	public final int getPeriodEnd(int i) {
		return i == 0 ? completionTime: Choco.MIN_LOWER_BOUND;
	}

}
