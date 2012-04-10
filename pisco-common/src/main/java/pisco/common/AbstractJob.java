package pisco.common;

import static pisco.common.TJobAdapter.*;
import gnu.trove.TLinkedList;
import gnu.trove.TObjectProcedure;
import choco.Choco;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.solver.variables.scheduling.AbstractTask;
import choco.kernel.solver.variables.scheduling.ITimePeriodList;


public abstract class AbstractJob extends AbstractTask implements ITJob, IHook {


	private final static TLinkedList<TJobAdapter> ADAPTER_POOL = new TLinkedList<TJobAdapter>();


	public final static void addJob(TLinkedList<TJobAdapter> list, ITJob job) {
		list.add(make(job));
	}

	public final static TJobAdapter makeTJobAdapter(ITJob job) {
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

	
	protected AbstractJob(int id, ITimePeriodList timePeriodList) {
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
		weight = 0;
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
		assert(duration >= 0);
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
	/* (non-Javadoc)
	 * @see pisco.common.IHook#getHook()
	 */
	@Override
	public final int getHook() {
		return hook;
	}

	/* (non-Javadoc)
	 * @see pisco.common.IHook#setHook(int)
	 */
	@Override
	public final void setHook(int hook) {
		this.hook = hook;
	}

	/* (non-Javadoc)
	 * @see pisco.common.IHook#incHook()
	 */
	@Override
	public final int incHook() {
		return ++hook;
	}

	/* (non-Javadoc)
	 * @see pisco.common.IHook#decHook()
	 */
	@Override
	public final int decHook() {
		return --hook;
	}


	////////////////////////////////////////////////////////////////////
	///////////////////// Precedence Graph  ////////////////////////////
	////////////////////////////////////////////////////////////////////
	/* (non-Javadoc)
	 * @see pisco.common.ITemp#forEachPredecessor(gnu.trove.TObjectProcedure)
	 */
	@Override
	public final void forEachPredecessor(TObjectProcedure<TJobAdapter> procedure) {
		predecessors.forEachValue(procedure);
	}

	/* (non-Javadoc)
	 * @see pisco.common.ITemp#forEachSuccessor(gnu.trove.TObjectProcedure)
	 */
	@Override
	public final void forEachSuccessor(TObjectProcedure<TJobAdapter> procedure) {
		successors.forEachValue(procedure);
	}

	@Override
	public final int getPredecessorCount() {
		return predecessors.size();
	}

	@Override
	public final int getSuccessorCount() {
		return successors.size();
	}

	
	@Override
	public final void setPredecessor(ITJob pred) {
		add(predecessors, pred);
		
	}

	@Override
	public final void setSuccessor(ITJob succ) {
		add(successors, succ);		
	}

	@Override
	public final void addPredecessor(ITJob pred) {
		setPredecessor(pred);
		pred.setSuccessor(this);
	}

	@Override
	public final void addSuccessor(ITJob succ) {
		setSuccessor(succ);
		succ.setPredecessor(this);
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

	@Override
	public final int getRemainingDuration() {
		return getMinDuration() - Math.max(0, getTimePeriodList().getExpendedDuration());
	}


	@Override
	public boolean isScheduledInTimeWindow() {
		return getReleaseDate() <= getEST() && getLCT() <= getDeadline();
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
	///////////////////// Cost functions ///////////////////////////////
	////////////////////////////////////////////////////////////////////

	@Override
	public int getCompletionTime() {
		return getECT();
	}

	@Override
	public int getWeightedCompletionTime() {
		return weight * getECT();
	}

	@Override
	public int getLateness() {
		return getECT() - dueDate;
	}

	@Override
	public int getDeviation() {
		return Math.abs(getECT() - dueDate);
	}

	@Override
	public int getEarliness() {
		return Math.max( dueDate - getECT(), 0);
	}

	@Override
	public int getTardiness() {
		return Math.max( getECT() - dueDate, 0);
	}
	////////////////////////////////////////////////////////////////////
	///////////////////// Combination and merge  ///////////////////////
	////////////////////////////////////////////////////////////////////
	
	

	@Override
	public void setValues(IJob j) {
		duration = j.getDuration();
		size = j.getSize();
		releaseDate = j.getReleaseDate();
		deadline = j.getDeadline();
		weight=j.getWeight();
		dueDate = j.getDueDate();
		
	}

	
	private void combinaison(IJob j1, IJob j2) {
		size = j1.getSize() + j2.getSize();
		releaseDate= Math.max(j1.getReleaseDate(), j2.getReleaseDate());
		deadline= Math.min(j1.getDeadline(), j2.getDeadline());
		weight = j1.getWeight() + j2.getWeight();
		dueDate = Math.min(j1.getDueDate(), j2.getDueDate());
	}

	private void merge(IJob j) {
		size += j.getSize();
		if(releaseDate < j.getReleaseDate()) {releaseDate = j.getReleaseDate();}
		if(deadline > j.getDeadline()) {deadline = j.getDeadline();}
		weight+=j.getWeight();
		if(dueDate < j.getDueDate()) {dueDate = j.getDueDate();}
		
	}

	
	@Override
	public void parallelCombinaison(IJob j1, IJob j2) {
		duration = Math.max(j1.getDuration(), j2.getDuration());
		combinaison(j1, j2);
		
	}

	@Override
	public void parallelMerge(IJob j) {
		if(duration < j.getDuration()) {duration = j.getDuration();}
		merge(j);
	}
	
	

	@Override
	public void serialCombinaison(IJob j1, IJob j2) {
		duration = j1.getDuration() + j2.getDuration();
		combinaison(j1, j2);
		
	}

	@Override
	public void serialMerge(IJob j) {
		duration += j.getDuration(); 
		merge(j);		
	}
	
	////////////////////////////////////////////////////////////////////
	///////////////////// toString and toDotty  ////////////////////////
	////////////////////////////////////////////////////////////////////
	public String desc2str() {
		return "p=" + duration +
				", s=" + this.size+ 
				", w=" + this.weight + 
				", d=" + this.dueDate +
				", tw=[" + this.releaseDate + ","+this.deadline+"]";	
	}


	@Override
	public String toString()
	{
		return isPartiallyScheduled() ? super.toString() : getName()+"["+desc2str()+"]";
	}

	@Override
	public String toDotty() {
		final StringBuilder b = new StringBuilder();
		b.append(super.toDotty());
		successors.forEachValue(new TObjectProcedure<TJobAdapter>() {

			@Override
			public boolean execute(TJobAdapter object) {
				b.append(getID()).append("->");
				b.append(object.target.getID()).append(";\n");
				return true;
			}
		});
		return b.toString();
	}
	
	
}
