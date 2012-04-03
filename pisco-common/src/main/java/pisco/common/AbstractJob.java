package pisco.common;

import java.awt.Point;

import choco.Choco;

public abstract class AbstractJob {

	public final int id;

	private int duration = 0;
	private int size = 1;

	//time windows
	private int releaseDate = 0;
	private int deadline = Choco.MAX_UPPER_BOUND;

	//objective
	private int weight = 1;
	private int dueDate = Choco.MAX_UPPER_BOUND;

	
	public AbstractJob(int id) {
		super();
		this.id = id;
	}

	public final int getDuration() {
		return duration;
	}

	public final void setDuration(int duration) {
		this.duration = duration;
	}

	public final int getSize() {
		return size;
	}

	public final void setSize(int size) {
		this.size = size;
	}

	public final int getReleaseDate() {
		return releaseDate;
	}

	public final void setReleaseDate(int releaseDate) {
		this.releaseDate = releaseDate;
	}

	public final int getDeadline() {
		return deadline;
	}

	public final void setDeadline(int deadline) {
		this.deadline = deadline;
	}

	public final int getWeight() {
		return weight;
	}

	public final void setWeight(int weight) {
		this.weight = weight;
	}

	public final int getDueDate() {
		return dueDate;
	}

	public final void setDueDate(int dueDate) {
		this.dueDate = dueDate;
	}

	public final int getId() {
		return id;
	}

	public abstract int getStartingTime();

	public abstract int getCompletionTime();

	public abstract void scheduleAt(int startingTime);

	public abstract void scheduleTo(int endingTime);

	public abstract void scheduleBetween(int start, int end);
	
	public abstract void scheduleIn(int start, int end);

	public abstract boolean isPartiallyScheduled();
	
	public abstract boolean isScheduled();	

	public final boolean isPreempted() {
		return getCompletionTime() - getStartingTime() > duration;
	}

	public abstract int getRemainingDuration();
	
	public abstract int getTimePeriodCount(); 

	public abstract Point getTimePeriod(int i);

	public abstract int getPeriodStart(int i);

	public abstract int getPeriodEnd(int i);

}
