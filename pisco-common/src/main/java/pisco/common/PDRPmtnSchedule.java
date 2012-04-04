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

	public final static void schedule(AbstractJob[] jobs) {
		//initialize
		for (int i = 0; i < jobs.length; i++) {
			jobs[i].setHook(jobs[i].getPredecessorCount());
		}
		AbstractJob[] tjobs = Arrays.copyOf(jobs, jobs.length);
		Arrays.sort(tjobs, JobComparators.getEarliestReleaseDate());
		final PriorityQueue<AbstractJob> pendingJobs= new PriorityQueue<AbstractJob>(10, JobComparators.getEarliestDueDate());
		int nextTime = tjobs[0].getReleaseDate();
		int i = 0;
		do {
			final int time = nextTime;
			do { 
				while( 	i < jobs.length && tjobs[i].getReleaseDate() == time) {
					if(tjobs[i].getPredecessorCount() == 0) {
						pendingJobs.add(tjobs[i]);
					}
					i++;
				}
			} while(pendingJobs.isEmpty());
			int currentTime = time;
			nextTime =  i < jobs.length ? tjobs[i].getReleaseDate() : MAX_UPPER_BOUND;
			while(currentTime < nextTime && ! pendingJobs.isEmpty() ) {
				currentTime = pendingJobs.peek().scheduleIn(currentTime, nextTime);
				if(pendingJobs.peek().isScheduled()) {
					pendingJobs.remove().forEachSuccessor(new TObjectProcedure<TJobAdapter>() {

						@Override
						public boolean execute(TJobAdapter object) {
							if( object.target.decHook() == 0) {
								if(object.target.getReleaseDate() <= time) {
									pendingJobs.add(object.target);
								}
							}
							return true;
						}
					});

				}
			}
		} while(i < jobs.length);
		assert(isScheduled(tjobs));
	}

	public final static void schedule2(AbstractJob[] jobs) {
		//initialize
		AbstractJob[] tjobs = Arrays.copyOf(jobs, jobs.length);
		Arrays.sort(tjobs, JobComparators.getEarliestReleaseDate());
		final PriorityQueue<AbstractJob> pendingJobs= new PriorityQueue<AbstractJob>(10, JobComparators.getShortestRemainingProcessingTime());
		int nextTime = tjobs[0].getReleaseDate();
		int i = 0;
		do {
			final int time = nextTime;
			while( 	i < jobs.length && tjobs[i].getReleaseDate() == time) {
				pendingJobs.add(tjobs[i]);
				i++;
			}
			int currentTime = time;
			nextTime =  i < jobs.length ? tjobs[i].getReleaseDate() : MAX_UPPER_BOUND;
			while(currentTime < nextTime && ! pendingJobs.isEmpty() ) {
				for (AbstractJob job : pendingJobs) {
					System.out.print(job.getID()+":"+job.getRemainingDuration()+ " ");
				}
				System.out.println();
				currentTime = pendingJobs.peek().scheduleIn(currentTime, nextTime);
				System.out.println(pendingJobs.peek());
				if(pendingJobs.peek().isScheduled()) {
					pendingJobs.remove();
				}
			}
		} while(i < jobs.length);
		assert(isScheduled(tjobs));
	}

	public final static void ScheduleLawlerLmax(AbstractJob[] jobs) {
		//initialize
		int currentTime = 0;
		final PriorityQueue<AbstractJob> pendingJobs= new PriorityQueue<AbstractJob>(10, JobComparators.getLatestDueDate());
		for (int i = 0; i < jobs.length; i++) {
			currentTime += jobs[i].getDuration();
			jobs[i].setHook(jobs[i].getSuccessorCount());
			if(jobs[i].getHook() == 0) {
				pendingJobs.add(jobs[i]);
			}
		}
		while( ! pendingJobs.isEmpty()) {
			final AbstractJob job = pendingJobs.remove();
			job.scheduleTo(currentTime);
			currentTime = job.getEST();
			job.forEachPredecessor(new TObjectProcedure<TJobAdapter>() {

				@Override
				public boolean execute(TJobAdapter object) {
					if( object.target.decHook() == 0) {
						pendingJobs.add(object.target);
					}
					return true;
				}
			});
		}
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


