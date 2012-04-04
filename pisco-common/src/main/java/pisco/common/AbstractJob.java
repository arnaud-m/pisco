package pisco.common;

import gnu.trove.TLinkableAdapter;
import gnu.trove.TLinkedList;
import gnu.trove.TObjectProcedure;
import choco.Choco;
import choco.kernel.solver.variables.scheduling.AbstractTask;
import choco.kernel.solver.variables.scheduling.ITimePeriodList;


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

public abstract class AbstractJob extends AbstractTask implements IJob {


	private final static TLinkedList<TJobAdapter> ADAPTER_POOL = new TLinkedList<TJobAdapter>();


	public final static void addJob(TLinkedList<TJobAdapter> list, AbstractJob job) {
		list.add(makeTJobAdapter(job));
	}

	public final static TJobAdapter makeTJobAdapter(AbstractJob job) {
		if(ADAPTER_POOL.isEmpty() ) {
			return new TJobAdapter(job);
		}
		else {
			ADAPTER_POOL.getFirst().setTarget(job);
			return ADAPTER_POOL.removeFirst(); 
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
	private int size = 0;

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

	public AbstractJob(int id, ITimePeriodList timePeriodList) {
		super(timePeriodList);
		this.id = id;
	}

	////////////////////////////////////////////////////////////////////
	///////////////////// Reset  ///////////////////////////////////////
	////////////////////////////////////////////////////////////////////

	/* (non-Javadoc)
	 * @see pisco.common.IJob#resetDimensions()
	 */
	@Override
	public final void resetDimensions() {
		duration=0;
		size=0;
	}

	/* (non-Javadoc)
	 * @see pisco.common.IJob#resetTimeWindow()
	 */
	@Override
	public final void resetTimeWindow() {
		releaseDate = 0;
		deadline = Choco.MAX_UPPER_BOUND;
	}

	/* (non-Javadoc)
	 * @see pisco.common.IJob#resetCostParameters()
	 */
	@Override
	public final void resetCostParameters() {
		weight = 1;
		dueDate = Choco.MAX_UPPER_BOUND;

	}

	public final void resetPrecedences() {
		free(predecessors);
		free(successors);
	}

	public void resetSchedule() {
		getTimePeriodList().reset();
	}

	public void resetOthers() {

	}

	public final void reset() {
		resetSchedule();
		resetDimensions();
		resetTimeWindow();
		resetCostParameters();
		resetPrecedences();
		resetOthers();
	}


	////////////////////////////////////////////////////////////////////
	///////////////////// Getters/Setters  /////////////////////////////
	////////////////////////////////////////////////////////////////////

	@Override
	public final int getDuration() {
		return duration;
	}

	@Override
	public final void setDuration(int duration) {
		this.duration = duration;
	}

	@Override
	public final int getSize() {
		return size;
	}

	@Override
	public final void setSize(int size) {
		this.size = size;
	}

	@Override
	public final int getReleaseDate() {
		return releaseDate;
	}

	@Override
	public final void setReleaseDate(int releaseDate) {
		this.releaseDate = releaseDate;
	}

	@Override
	public final int getDeadline() {
		return deadline;
	}

	@Override
	public final void setDeadline(int deadline) {
		this.deadline = deadline;
	}

	@Override
	public final int getWeight() {
		return weight;
	}

	@Override
	public final void setWeight(int weight) {
		this.weight = weight;
	}

	@Override
	public final int getDueDate() {
		return dueDate;
	}

	@Override
	public final void setDueDate(int dueDate) {
		this.dueDate = dueDate;
	}

	////////////////////////////////////////////////////////////////////
	/////////////////////// Hook  //////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	public final int getHook() {
		return hook;
	}

	public final void setHook(int hook) {
		this.hook = hook;
	}

	public final int incHook() {
		return ++hook;
	}

	public final int decHook() {
		return --hook;
	}


	////////////////////////////////////////////////////////////////////
	///////////////////// Precedence Graph  ////////////////////////////
	////////////////////////////////////////////////////////////////////
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

	////////////////////////////////////////////////////////////////////
	///////////////////// Scheduling  //////////////////////////////////
	////////////////////////////////////////////////////////////////////

	public abstract void scheduleFrom(int startingTime);

	public abstract void scheduleTo(int endingTime);

	public abstract void scheduleFromTo(int start, int end);

	public abstract int scheduleIn(int start, int end);

	public final int getRemainingDuration() {
		return getMinDuration() - Math.max(0, getTimePeriodList().getExpendedDuration());
	}


	////////////////////////////////////////////////////////////////////
	///////////////////// ITask  ///////////////////////////////////////
	////////////////////////////////////////////////////////////////////

	@Override
	public final int getID() {
		return id;
	}

	@Override
	public final int getMinDuration() {
		return duration;
	}

	@Override
	public final int getMaxDuration() {
		return duration;
	}

	////////////////////////////////////////////////////////////////////
	///////////////////// toString and toDotty  ////////////////////////
	////////////////////////////////////////////////////////////////////
	public String desc2str() {
		return "p=" + duration +
				", s=" + this.size+ 
				", w=" + this.weight + 
				", d=" + this.dueDate;
	}

	@Override
	public String toString()
	{
		return isPartiallyScheduled() ? super.toString() : getName()+"["+desc2str()+"]";
	}
}
