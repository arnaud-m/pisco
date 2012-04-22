package pisco.common;

import static choco.Choco.MAX_UPPER_BOUND;
import static choco.Choco.MIN_LOWER_BOUND;
import static pisco.common.JobUtils.isScheduled;
import gnu.trove.TObjectProcedure;

import java.util.Arrays;
import java.util.PriorityQueue;

import choco.kernel.common.DottyBean;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.visu.VisuFactory;

public final class Pmtn1Scheduler {

	private Pmtn1Scheduler() {
		super();
	}


	public static final class Proc1Lmax extends DefaultJobProcedure {

		private int time;

		public Proc1Lmax() {
			super(new PriorityQueue<ITJob>(DEFAULT_CAPACITY, JobComparators.getEarliestDueDate()));
		}
		
		
		
		public final int getTime() {
			return time;
		}


		public final void setTime(int time) {
			this.time = time;
		}



		@Override
		public void execute(ITJob arg) {
			if( arg.decHook() == 0) {
				if(arg.getReleaseDate() < time) {
					pendingJobs.add(arg);
				}
			}
		}
		
		
		
	}

	public final static int schedule1PrecLmax(ITJob[] jobs) {
		return schedule1PrecLmax(jobs, new Proc1Lmax());
	}
	
	public final static int schedule1PrecLmax(ITJob[] jobs, Proc1Lmax procedure) {
		//initialize
		int lmax = MIN_LOWER_BOUND;
		for (int i = 0; i < jobs.length; i++) {
			jobs[i].setHook(jobs[i].getPredecessorCount());
		}
		Arrays.sort(jobs, JobComparators.getEarliestReleaseDate());
		final PriorityQueue<ITJob> pendingJobs= procedure.getPriorityQueue();
		pendingJobs.clear();
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
				assert(! pendingJobs.isEmpty() || i < jobs.length);
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
					procedure.setTime(time);
					pendingJobs.remove().forEachSuccessor(procedure);
				}
			}
		} while(i < jobs.length);
		if(!isScheduled(jobs)) {
			ChocoLogging.flushLogs();
			VisuFactory.getDotManager().show(new DottyBean(jobs));
			System.out.println(Arrays.toString(jobs));
		}
		assert(isScheduled(jobs));
		return lmax;
	}

	public final static int schedule1rjFlow(AbstractJob[] jobs) {
		//initialize
		int sumCi = 0;
		// TODO - Avoid copying - created 12 avr. 2012 by A. Malapert
		AbstractJob[] tjobs = Arrays.copyOf(jobs, jobs.length);
		Arrays.sort(tjobs, JobComparators.getEarliestReleaseDate());
		// TODO - Set optionally the queue as parameter to save memory - created 12 avr. 2012 by A. Malapert
		
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


