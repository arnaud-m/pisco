package pisco.common;

import gnu.trove.TObjectProcedure;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.solver.variables.scheduling.ITask;

public interface ITJob extends IJob, ITask, ICostFunctions, IHook {


	////////////////////////////////////////////////////////////////////
	///////////////////// Precedence Graph  ////////////////////////////
	////////////////////////////////////////////////////////////////////
	void resetPrecedences();


	int getPredecessorCount();

	int getSuccessorCount();

	void setPredecessor(ITJob pred);

	void unsetPredecessor(ITJob pred);

	void addPredecessor(ITJob pred);

	void removePredecessor(ITJob pred);
	
	void setSuccessor(ITJob succ);

	void unsetSuccessor(ITJob succ);
	
	void addSuccessor(ITJob succ);
	
	void removeSuccessor(ITJob succ);

	// TODO - Remove forEach(Pred|Succ) - created 20 avr. 2012 by A. Malapert
	void forEachPredecessor(IJobProcedure procedure);

	void forEachSuccessor(IJobProcedure procedure);

	DisposableIterator<ITJob> getPredIterator();
	
	DisposableIterator<ITJob> getSuccIterator();
	
	////////////////////////////////////////////////////////////////////
	///////////////////// Scheduling Decisions /////////////////////////
	////////////////////////////////////////////////////////////////////
	void resetSchedule();

	void scheduleFrom(int startingTime);

	void scheduleTo(int endingTime);

	void scheduleFromTo(int start, int end);

	int scheduleIn(int start, int end);

	int getRemainingDuration();

	boolean isScheduledInTimeWindow();
	////////////////////////////////////////////////////////////////////
	///////////////////// Merge and combination  ///////////////////////
	////////////////////////////////////////////////////////////////////
	
	void setValues(IJob j);
	
	void parallelCombinaison(IJob j1, IJob j2);

	void parallelMerge(IJob j);

	void serialCombinaison(IJob j1, IJob j2);

	void serialMerge(IJob j);

	
		
	
}