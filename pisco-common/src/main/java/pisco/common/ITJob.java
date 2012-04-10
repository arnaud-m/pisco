package pisco.common;

import gnu.trove.TObjectProcedure;
import choco.kernel.solver.variables.scheduling.ITask;

public interface ITJob extends IJob, ITask, ICostFunctions, IHook {


	////////////////////////////////////////////////////////////////////
	///////////////////// Precedence Graph  ////////////////////////////
	////////////////////////////////////////////////////////////////////
	void resetPrecedences();

	public void forEachPredecessor(TObjectProcedure<TJobAdapter> procedure);

	public void forEachSuccessor(TObjectProcedure<TJobAdapter> procedure);

	public int getPredecessorCount();

	public int getSuccessorCount();

	public void setPredecessor(ITJob pred);

	public void addPredecessor(ITJob pred);

	public void setSuccessor(ITJob succ);

	public void addSuccessor(ITJob succ);


	////////////////////////////////////////////////////////////////////
	///////////////////// Scheduling Decisions /////////////////////////
	////////////////////////////////////////////////////////////////////
	void resetSchedule();

	public void scheduleFrom(int startingTime);

	public void scheduleTo(int endingTime);

	public void scheduleFromTo(int start, int end);

	public int scheduleIn(int start, int end);

	public int getRemainingDuration();

	public boolean isScheduledInTimeWindow();
	////////////////////////////////////////////////////////////////////
	///////////////////// Merge and combination  ///////////////////////
	////////////////////////////////////////////////////////////////////
	
	void setValues(IJob j);
	
	void parallelCombinaison(IJob j1, IJob j2);

	void parallelMerge(IJob j);

	void serialCombinaison(IJob j1, IJob j2);

	void serialMerge(IJob j);

	
		
	
}