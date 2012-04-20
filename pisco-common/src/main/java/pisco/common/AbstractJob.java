package pisco.common;

import gnu.trove.TObjectProcedure;

import java.util.Arrays;

import choco.Choco;
import choco.kernel.common.util.iterators.ArrayIterator;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.solver.variables.scheduling.AbstractTask;
import choco.kernel.solver.variables.scheduling.ITimePeriodList;


public abstract class AbstractJob extends AbstractTask implements ITJob, IHook {


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

	//Precedence Graph
	private int predecessorCount;
	private ITJob[] predecessors = new ITJob[10];

	private int successorCount;
	private ITJob[] successors = new ITJob[10];

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
	@Override
	public final void resetDimensions() {
		duration=0;
		size=0;
	}

	@Override
	public final void resetTimeWindow() {
		releaseDate = 0;
		deadline = Choco.MAX_UPPER_BOUND;
	}


	@Override
	public final void resetCostParameters() {
		weight = 0;
		dueDate = Choco.MAX_UPPER_BOUND;

	}

	public final void resetPrecedences() {
		predecessorCount = 0;
		successorCount = 0;
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

	@Override
	public final int getHook() {
		return hook;
	}

	@Override
	public final void setHook(int hook) {
		this.hook = hook;
	}

	@Override
	public final int incHook() {
		return ++hook;
	}

	@Override
	public final int decHook() {
		return --hook;
	}


	////////////////////////////////////////////////////////////////////
	///////////////////// Precedence Graph  ////////////////////////////
	////////////////////////////////////////////////////////////////////


	
	@Override
	public final void forEachPredecessor(IJobProcedure procedure) {
		for (int i = 0; i < predecessorCount; i++) {
			procedure.execute(predecessors[i]);
		}
	}

	@Override
	public final void forEachSuccessor(IJobProcedure procedure) {
		for (int i = 0; i < successorCount; i++) {
			procedure.execute(successors[i]);
		}
	}

	@Override
	public final int getPredecessorCount() {
		return predecessorCount;
	}

	@Override
	public final int getSuccessorCount() {
		return successorCount;
	}

	@Override
	public final void setPredecessor(ITJob pred) {
		if (predecessorCount >= predecessors.length) {
			int newCapacity = (predecessors.length * 3)/2 + 1;
			predecessors = Arrays.copyOf(predecessors, newCapacity);
		}
		predecessors[predecessorCount++]=pred;
	}

	public final void unsetPredecessor(ITJob pred) {
		for (int i = predecessorCount-1; i >=0; i--) {
			if(predecessors[i].equals(pred)) {
				predecessors[i] = predecessors[--predecessorCount];
				return;
			}
		}
	}

	@Override
	public final void addPredecessor(ITJob pred) {
		setPredecessor(pred);
		pred.setSuccessor(this);
	}


	public final void removePredecessor(ITJob pred) {
		unsetPredecessor(pred);
		pred.unsetSuccessor(this);
	}



	@Override
	public final void setSuccessor(ITJob succ) {
		if (successorCount >= successors.length) {
			int newCapacity = (successors.length * 3)/2 + 1;
			successors = Arrays.copyOf(successors, newCapacity);
		}
		successors[successorCount++]=succ;
			
	}

	@Override
	public final void addSuccessor(ITJob succ) {
		setSuccessor(succ);
		succ.setPredecessor(this);
	}

	public final void unsetSuccessor(ITJob succ) {
		for (int i = successorCount-1; i >=0; i--) {
			if(successors[i].equals(succ)) {
				successors[i] = successors[--successorCount];
				return;
			}
		}
	}

	public final void removeSuccessor(ITJob succ) {
		unsetSuccessor(succ);
		succ.unsetPredecessor(this);
	}

	
	////////////////////////////////////////////////////////////////////
	///////////////////// Scheduling  //////////////////////////////////
	////////////////////////////////////////////////////////////////////

	@Override
	public DisposableIterator<ITJob> getPredIterator() {
		return ArrayIterator.getIterator(predecessors, predecessorCount);
	}


	@Override
	public DisposableIterator<ITJob> getSuccIterator() {
		return ArrayIterator.getIterator(successors, successorCount);
	}


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
		forEachSuccessor(new IJobProcedure() {
			
			@Override
			public void execute(ITJob arg) {
				b.append(getID()).append("->");
				b.append(arg.getID()).append(";\n");
				
			}
		});
		return b.toString();
	}


}
