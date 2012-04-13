package pisco.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;


public interface IJobProcedure {

	void execute(ITJob arg);

}

class DefaultJobProcedure implements IJobProcedure {

	protected final static int DEFAULT_CAPACITY = 10;
	protected final Collection<ITJob> pendingJobs;

	
	public DefaultJobProcedure() {
		this( new ArrayList<ITJob>(DEFAULT_CAPACITY));
	}
	
	
	public final PriorityQueue<ITJob> getPriorityQueue() {
		if (pendingJobs instanceof PriorityQueue) {
			return (PriorityQueue<ITJob>) pendingJobs;
		} else return null;
	}


	public final List<ITJob> getList() {
		if (pendingJobs instanceof List) {
			return (List<ITJob>) pendingJobs;
		} else return null;
	}
	public DefaultJobProcedure(final Collection<ITJob> pendingJobs) {
		super();
		this.pendingJobs = pendingJobs;
	}




	@Override
	public void execute(ITJob arg) {
		if(arg.decHook() == 0) {
			pendingJobs.add(arg);
		}

	}



}
