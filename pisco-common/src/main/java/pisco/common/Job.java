package pisco.common;

import choco.Choco;

public class Job extends AbstractJob {

	//schedule
	private int est = Choco.MIN_LOWER_BOUND;
	private int lct = Choco.MIN_LOWER_BOUND;

	public Job(int id) {
		super(id);
	}

	
	@Override
	public boolean isPreemptionAllowed() {
		return false;
	}


	
	@Override
	public final int getEST() {
		return est;
	}


	@Override
	public final int getLCT() {
		return lct;
	}


	@Override
	public void resetSchedule() {
		super.resetSchedule();
		est = getReleaseDate();
		lct = getDeadline();
	}

	
	public final int scheduleIn(int start, int end) {
		est = start;
		lct = start + getDuration();
		if(lct > end) {
			throw new IllegalArgumentException("Preemption not allowed");
		}
		assert(est >= getReleaseDate());
		return lct;
	}

	public final void scheduleFromTo(int start, int end) {
		if(end - start == getDuration()) {
			est = start;
			lct = end;
		} else {
			throw new IllegalArgumentException("Preemption not allowed");
		}
		assert(est >= getReleaseDate());
	}

	public final void scheduleTo(int endingTime) {
		lct = endingTime;
		est = lct - getDuration();
		assert(est >= getReleaseDate());
	}

	public final void scheduleFrom(int startingTime) {
		this.est = startingTime;
		lct = startingTime + getDuration();
		assert(this.est >= getReleaseDate());
	}


}
