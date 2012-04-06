package pisco.common;

import choco.kernel.solver.variables.scheduling.TimePeriodList;

public class PJob extends AbstractJob {

	private final TimePeriodList _timePeriodList;

	public PJob(int id) {
		super(id, new TimePeriodList());
		_timePeriodList = (TimePeriodList) getTimePeriodList();
	}

	@Override
	public boolean isPreemptionAllowed() {
		return true;
	}

	
	@Override
	public int getEST() {
		return _timePeriodList.isEmpty() ? getReleaseDate() : _timePeriodList.getPeriodFirst();
	}
	
	@Override
	public int getECT() {
		return _timePeriodList.isEmpty() ? super.getECT() : _timePeriodList.getPeriodLast() + getRemainingDuration();
	}

	@Override
	public int getLST() {
		return _timePeriodList.isEmpty() ? super.getLST() : _timePeriodList.getPeriodFirst();
	}

	@Override
	public int getLCT() {
		return isScheduled() ? _timePeriodList.getPeriodLast() : getDeadline();
	}

	public final int scheduleIn(int start, int end) {
		assert( end > start && getRemainingDuration() > 0);
		end = Math.min(end, start + getRemainingDuration());
		_timePeriodList.addTimePeriod(start, end);
		return end;
	}

	public final void scheduleFromTo(int start, int end) {
		assert( getRemainingDuration() > end - start && end > start);
		_timePeriodList.addTimePeriod(start, end);
	}

	
	@Override
	public void scheduleFrom(int startingTime) {
		final int length = getRemainingDuration();
		assert(startingTime >= getReleaseDate() && length > 0);
		_timePeriodList.addTimeLength(startingTime, length);
	}

	@Override
	public void scheduleTo(int endingTime) {
		final int start = endingTime - getRemainingDuration();
		assert(start >= getReleaseDate() && start < endingTime);
		_timePeriodList.addTimePeriod(start, endingTime);
	}

	@Override
	public String toString() {
		return getName() + (isPartiallyScheduled() ? _timePeriodList.toString() + (isScheduled() ? "" : "["+ getRemainingDuration()+"]") : "["+desc2str()+"]");
	}

	
}
