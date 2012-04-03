package pisco.common;

import static choco.Choco.MAX_UPPER_BOUND;
import gnu.trove.TLinkedList;
import gnu.trove.TObjectProcedure;

import java.util.Arrays;
import java.util.PriorityQueue;

public final class PDRPmtnSchedule {

	private PDRPmtnSchedule() {
		super();
	}

	public final void modifyDueDates(final AbstractJob[] jobs) {
		final TLinkedList<TJobAdapter> pendingJobs = new TLinkedList<TJobAdapter>();
		//initialize
		for (int i = 0; i < jobs.length; i++) {
			jobs[i].setHook(jobs[i].getSuccessorCount());
			if(jobs[i].getHook() == 0) {
				AbstractJob.addJob(pendingJobs, jobs[i]);
			}
		}
		assert( ! pendingJobs.isEmpty());
		//compute topological order and modify due dates
		while( ! pendingJobs.isEmpty()) {
			final AbstractJob current = pendingJobs.getFirst().getTarget();
			AbstractJob.free(pendingJobs.removeFirst());
			current.forEachPredecessor( new TObjectProcedure<TJobAdapter>() {

				@Override
				public boolean execute(TJobAdapter object) {
					final int modifiedDueDate = current.getDueDate() - current.getDuration();
					if(modifiedDueDate < object.target.getDueDate() ) {
						object.target.setDueDate(modifiedDueDate);
					}
					if(object.target.decHook() == 0) {
						AbstractJob.addJob(pendingJobs, object.target);
					}
					return true;
				}
			});

		}
	}

	public final static void schedule(JobPmtn[] jobs) {
		//initialize
		for (int i = 0; i < jobs.length; i++) {
			jobs[i].setHook(jobs[i].getPredecessorCount());
		}
		JobPmtn[] tjobs = Arrays.copyOf(jobs, jobs.length);
		Arrays.sort(tjobs, JobComparators.getEarliestReleaseDate());
		PriorityQueue<JobPmtn> pendingJobs= new PriorityQueue<JobPmtn>(10, JobComparators.getEarliestDueDate());
		int time = tjobs[0].getReleaseDate();
		int nextTime = time;
		int i = 0;
		do { 
			while( 	tjobs[i].getReleaseDate() == time) {
				if(tjobs[i].getPredecessorCount() == 0) {
					pendingJobs.add(tjobs[i]);
				}
				i++;
			}
		} while(pendingJobs.isEmpty());
		int actualTime = time;
		nextTime =  i < jobs.length - 1 ? tjobs[i].getReleaseDate() : MAX_UPPER_BOUND;
		while(actualTime < nextTime && ! pendingJobs.isEmpty() ) {
			actualTime = pendingJobs.peek().scheduleIn(time, nextTime);
			if(pendingJobs.peek().isScheduled()) {
				// TODO - Update hooks - created 3 avr. 2012 by A. Malapert
				pendingJobs.remove();
			}
		}
		time = nextTime;
		assert(isScheduled(tjobs));
	}

	public final static boolean isScheduled(AbstractJob[] jobs) {
		for (int i = 0; i < jobs.length; i++) {
			if( ! jobs[i].isScheduled() ) {
				return false;
			}
		}
		return true;
	}

}


