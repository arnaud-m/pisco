package pisco.common;

import gnu.trove.TLinkableAdapter;
import gnu.trove.TLinkedList;
import gnu.trove.TObjectProcedure;

import java.awt.Point;

import choco.Choco;


class TJobAdapter extends TLinkableAdapter {

	private static final long serialVersionUID = 5224076152233404931L;

	public AbstractJob target;

	public TJobAdapter(AbstractJob target) {
		super();
		this.target = target;
	}

	public final AbstractJob getTarget() {
		return target;
	}

	public final void setTarget(AbstractJob target) {
		this.target = target;
	} 	
}

public abstract class AbstractJob {


	private final static TLinkedList<TJobAdapter> ADAPTER_POOL = new TLinkedList<TJobAdapter>();

	
	public final static void addJob(TLinkedList<TJobAdapter> list, AbstractJob job) {
		list.add(makeTJobAdapter(job));
	}
	
	public final static TJobAdapter makeTJobAdapter(AbstractJob job) {
		if(ADAPTER_POOL.isEmpty() ) {
			ADAPTER_POOL.getFirst().setTarget(job);
			return ADAPTER_POOL.removeFirst();
		}
		else {
			return new TJobAdapter(job); 
		}
	}
	
	public final static void free(TLinkedList<TJobAdapter> list) {
		while( ! list.isEmpty()) {
			free(list.removeFirst());
		}
	}
	
	public final static void free(TJobAdapter adapter) {
		ADAPTER_POOL.add(adapter);
	}
	
	public final int id;

	//dimensions
	private int duration = 0;
	private int size = 1;

	//time windows
	private int releaseDate = 0;
	private int deadline = Choco.MAX_UPPER_BOUND;

	//objective
	private int weight = 1;
	private int dueDate = Choco.MAX_UPPER_BOUND;

	//Precedence Graphs
	private TLinkedList<TJobAdapter> predecessors = new TLinkedList<TJobAdapter>() ;
	private TLinkedList<TJobAdapter> successors = new TLinkedList<TJobAdapter>();


	//algorithm 
	public int hook;
	
	public AbstractJob(int id) {
		super();
		this.id = id;
	}

	public final void resetPrecedences() {
		free(predecessors);
		free(successors);
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

	public final int getHook() {
		return hook;
	}

	public final void setHook(int hook) {
		this.hook = hook;
	}
	
	public final int incHook() {
		return hook++;
	}
	
	public final int decHook() {
		return hook--;
	}
	
	public final void forEachPredecessor(TObjectProcedure<TJobAdapter> procedure) {
		predecessors.forEachValue(procedure);
	}
	
	public final void forEachSuccessor(TObjectProcedure<TJobAdapter> procedure) {
		successors.forEachValue(procedure);
	}
	
	public final int getPredecessorCount() {
		return predecessors.size();
	}
	
	public final int getSuccessorCount() {
		return successors.size();
	}
	
	public final void addPredecessor(AbstractJob pred) {
		addJob(predecessors, pred);
		addJob(pred.successors, this);
	}

	public final void addSuccessor(AbstractJob succ) {
		addJob(successors, succ);
		addJob(succ.predecessors, this);
	}

	private static boolean remove(TLinkedList<TJobAdapter> list, AbstractJob job) {
		if( ! list.isEmpty()) {
			TJobAdapter current = list.getFirst();
			do {
				if(current.target == job) {
					list.remove(current);
					free(current);
					return true;
				}
			} while( ( current = list.getNext(current)) != null) ;
		}
		return false;
	}

	public final void removePredecessor(AbstractJob pred) {
		remove(predecessors, pred);
		remove(pred.successors, this);
	}

	public final void removeSuccessor(AbstractJob succ) {
		remove(successors, succ);
		remove(succ.predecessors, this);
	}
	
	
	public abstract int getStartingTime();

	public abstract int getCompletionTime();

	public abstract void scheduleAt(int startingTime);

	public abstract void scheduleTo(int endingTime);

	public abstract void scheduleBetween(int start, int end);

	public abstract int scheduleIn(int start, int end);

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
