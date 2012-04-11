package pisco.common;

import static choco.Choco.MAX_UPPER_BOUND;
import static choco.Choco.MIN_LOWER_BOUND;
import static pisco.common.JobUtils.isScheduled;
import gnu.trove.TObjectProcedure;

import java.util.Arrays;
import java.util.PriorityQueue;

public final class Pmtn1Scheduler {

	private Pmtn1Scheduler() {
		super();
	}


	public final static int schedule1Lmax(ITJob[] jobs) {
		//initialize
		int lmax = MIN_LOWER_BOUND;
		for (int i = 0; i < jobs.length; i++) {
			jobs[i].setHook(jobs[i].getPredecessorCount());
		}
		Arrays.sort(jobs, JobComparators.getEarliestReleaseDate());
		final PriorityQueue<ITJob> pendingJobs= new PriorityQueue<ITJob>(10, JobComparators.getEarliestDueDate());
		int time, nextTime;
		int i = 0;
		//Start scheduling jobs
		do {
			//System.out.println(Arrays.toString(jobs));
			// Add new pending jobs with a release date equals to time
			do { 
				time =  jobs[i].getReleaseDate();
				while( 	i < jobs.length && jobs[i].getReleaseDate() == time) {
					if(jobs[i].getHook() == 0) {
						pendingJobs.add(jobs[i]);
					}
					i++;
				}
				assert(!pendingJobs.isEmpty() || i < jobs.length);
			} while(pendingJobs.isEmpty());
			nextTime =  i < jobs.length ? jobs[i].getReleaseDate() : MAX_UPPER_BOUND;
			//Schedule pending jobs selected by EDD-rule until the next release date;
			while(time < nextTime && ! pendingJobs.isEmpty() ) {
				time = pendingJobs.peek().scheduleIn(time, nextTime);
				if(pendingJobs.peek().isScheduled()) {
					//Job is entirely scheduled : compute cost
					final int lateness = pendingJobs.peek().getLateness();
					if(lmax < lateness) { lmax = lateness;}
					//Update predecessor's counts and new pending jobs
					final int ctime = time;
					pendingJobs.remove().forEachSuccessor(new TObjectProcedure<TJobAdapter>() {
						@Override
						public boolean execute(TJobAdapter object) {
							if( object.target.decHook() == 0) {
								if(object.target.getReleaseDate() < ctime) {
									pendingJobs.add(object.target);
								}
							}
							return true;
						}
					});

				}
			}
		} while(i < jobs.length);
		assert(isScheduled(jobs));
		return lmax;
	}

	public final static int schedule1rjFlow(AbstractJob[] jobs) {
		//initialize
		int sumCi = 0;
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
					sumCi += pendingJobs.remove().getCompletionTime();
				}
			}
		} while(i < jobs.length);
		assert(isScheduled(tjobs));
		return sumCi;
	}



}


