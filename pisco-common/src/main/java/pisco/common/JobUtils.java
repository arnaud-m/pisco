package pisco.common;

import java.util.Iterator;
import java.util.ListIterator;

import choco.kernel.solver.variables.scheduling.ITask;

public final class JobUtils {

	private JobUtils() {}

	public final static boolean shiftLeftReleaseDates(IJob... jobs) {
		final int delta = minReleaseDate(jobs);
		if(delta > 0) {
			for (IJob job : jobs) {
				job.setReleaseDate(job.getReleaseDate() - delta);
				job.setDueDate(job.getDueDate() - delta);
				job.setDeadline(job.getDeadline() - delta);
			}
			return true;
		}
		return false;
	}



	
	public static final class ProcModDueDate extends DefaultJobProcedure {

		private int modifiedDueDate;

		private ListIterator<ITJob> iterator;
		
		public final ListIterator<ITJob> getIterator() {
			return iterator;
		}

		public final void initIterator() {
			this.iterator = getList().listIterator();
		}

		public final int getModifiedDueDate() {
			return modifiedDueDate;
		}

		public final void setModifiedDueDate(int modifiedDueDate) {
			this.modifiedDueDate = modifiedDueDate;
		}

		public final void setModifiedDueDate(final ITJob job) {
			this.modifiedDueDate = job.getDueDate() - job.getDuration();
		}

		@Override
		public void execute(ITJob arg) {
			if(arg.decHook() == 0) {
				iterator.add(arg);
				iterator.previous();
			}
			if(modifiedDueDate < arg.getDueDate() ) {
				arg.setDueDate(modifiedDueDate);
			}
		}
	}

	public final static void modifyDueDates(final ITJob[] jobs) {
		modifyDueDates(jobs, new ProcModDueDate());
	}

	public final static void modifyDueDates(final ITJob[] jobs, final ProcModDueDate procedure) {
		//initialize
		initSuccessorHooks(jobs, procedure);
		//compute topological order and modify due dates
		procedure.initIterator();
		ListIterator<ITJob> iter = procedure.getIterator();
		while(iter.hasNext()) {
			final ITJob current = iter.next();
			procedure.setModifiedDueDate(current);
			current.forEachPredecessor(procedure);
		}
	}
	public final static void modifyDeadlines(int horizon, ITJob... jobs) {
		for (ITJob j : jobs) {
			if(j.getDeadline() > horizon) j.setDeadline(horizon);
		}
	}


	
	protected final static void initSuccessorHooks(final ITJob[] jobs, final DefaultJobProcedure procedure) {
		procedure.pendingJobs.clear();
		for (int i = 0; i < jobs.length; i++) {
			final int val = jobs[i].getSuccessorCount();
			jobs[i].setHook(val);
			if(val == 0) {
				procedure.pendingJobs.add(jobs[i]);
			}
		}
		assert(! procedure.pendingJobs.isEmpty());
	}

	protected final static void initPredecessorHooks(final ITJob[] jobs, final DefaultJobProcedure procedure) {
		procedure.pendingJobs.clear();
		for (int i = 0; i < jobs.length; i++) {
			final int val = jobs[i].getPredecessorCount();
			jobs[i].setHook(val);
			if(val == 0) {
				procedure.pendingJobs.add(jobs[i]);
			}
		}
		assert(! procedure.pendingJobs.isEmpty());
	}

	public final static void resetSchedule(ITJob... jobs) {
		for (ITJob j : jobs) {
			j.resetSchedule();
		}

	}

	public final static boolean isScheduled(ITask... jobs) {
		for (ITask j : jobs) {
			if( ! j.isScheduled() ) return false;
		}
		return true;
	}

	public final static boolean isInterrupted(ITask... jobs) {
		for (ITask j : jobs) {
			if( j.isInterrupted() ) return true;
		}
		return false;
	}

	public final static boolean isScheduledInTimeWindows(ITJob... jobs) {
		for (ITJob j : jobs) {
			if( ! j.isScheduledInTimeWindow() ) return false;
		}
		return true;
	}


	public final static int minDuration(IJob... jobs) {
		int min = Integer.MAX_VALUE;
		for (IJob job : jobs) {
			if(job.getDuration() < min) {
				min = job.getDuration();
			}
		}
		return min;
	}

	public final static int maxDuration(IJob... jobs) {
		int max = Integer.MIN_VALUE;
		for (IJob job : jobs) {
			if(job.getDuration() > max) {
				max = job.getDuration();
			}
		}
		return max;
	}


	public final static int sumDurations(IJob... jobs) {
		int sum = 0;
		for (IJob job : jobs) {
			sum += job.getDuration();
		}
		return sum;
	}

	public final static int[] durations(IJob... jobs) {
		int[] tab = new int[jobs.length];
		for (int i = 0; i < tab.length; i++) {
			tab[i] = jobs[i].getDuration();
		}
		return tab;
	}

	public final static int minSize(IJob... jobs) {
		int min = Integer.MAX_VALUE;
		for (IJob job : jobs) {
			if(job.getSize() < min) {
				min = job.getSize();
			}
		}
		return min;
	}

	public final static int maxSize(IJob... jobs) {
		int max = Integer.MIN_VALUE;
		for (IJob job : jobs) {
			if(job.getSize() > max) {
				max = job.getSize();
			}
		}
		return max;
	}


	public final static int sumSizes(IJob... jobs) {
		int sum = 0;
		for (IJob job : jobs) {
			sum += job.getReleaseDate();
		}
		return sum;
	}

	public final static int[] sizes(IJob... jobs) {
		int[] tab = new int[jobs.length];
		for (int i = 0; i < tab.length; i++) {
			tab[i] = jobs[i].getSize();
		}
		return tab;
	}

	public final static int minReleaseDate(IJob... jobs) {
		int min = Integer.MAX_VALUE;
		for (IJob job : jobs) {
			if(job.getReleaseDate() < min) {
				min = job.getReleaseDate();
			}
		}
		return min;
	}

	public final static int maxReleaseDate(IJob... jobs) {
		int max = Integer.MIN_VALUE;
		for (IJob job : jobs) {
			if(job.getReleaseDate() > max) {
				max = job.getReleaseDate();
			}
		}
		return max;
	}

	public final static int[] releaseDates(IJob... jobs) {
		int[] tab = new int[jobs.length];
		for (int i = 0; i < tab.length; i++) {
			tab[i] = jobs[i].getReleaseDate();
		}
		return tab;
	}

	public final static int minDeadline(IJob... jobs) {
		int min = Integer.MAX_VALUE;
		for (IJob job : jobs) {
			if(job.getDeadline() < min) {
				min = job.getDeadline();
			}
		}
		return min;
	}

	public final static int maxDeadline(IJob... jobs) {
		int max = Integer.MIN_VALUE;
		for (IJob job : jobs) {
			if(job.getDeadline() > max) {
				max = job.getDeadline();
			}
		}
		return max;
	}

	public final static int[] deadlines(IJob... jobs) {
		int[] tab = new int[jobs.length];
		for (int i = 0; i < tab.length; i++) {
			tab[i] = jobs[i].getDeadline();
		}
		return tab;
	}
	public final static int minWeight(IJob... jobs) {
		int min = Integer.MAX_VALUE;
		for (IJob job : jobs) {
			if(job.getWeight() < min) {
				min = job.getWeight();
			}
		}
		return min;
	}

	public final static int maxWeight(IJob... jobs) {
		int max = Integer.MIN_VALUE;
		for (IJob job : jobs) {
			if(job.getWeight() > max) {
				max = job.getWeight();
			}
		}
		return max;
	}


	public final static int sumWeights(IJob... jobs) {
		int sum = 0;
		for (IJob job : jobs) {
			sum += job.getWeight();
		}
		return sum;
	}

	public final static int[] weights(IJob... jobs) {
		int[] tab = new int[jobs.length];
		for (int i = 0; i < tab.length; i++) {
			tab[i] = jobs[i].getWeight();
		}
		return tab;
	}

	public final static int minDueDate(IJob... jobs) {
		int min = Integer.MAX_VALUE;
		for (IJob job : jobs) {
			if(job.getDueDate() < min) {
				min = job.getDueDate();
			}
		}
		return min;
	}

	public final static int maxDueDate(IJob... jobs) {
		int max = Integer.MIN_VALUE;
		for (IJob job : jobs) {
			if(job.getDueDate() > max) {
				max = job.getDueDate();
			}
		}
		return max;
	}

	public final static int[] dueDates(IJob... jobs) {
		int[] tab = new int[jobs.length];
		for (int i = 0; i < tab.length; i++) {
			tab[i] = jobs[i].getDueDate();
		}
		return tab;
	}

	public final static int minSlackTime(IJob... jobs) {
		int min = Integer.MAX_VALUE;
		for (IJob job : jobs) {
			final int v = job.getDuration() - job.getDueDate();
			if(v < min) {
				min = v;
			}
		}
		return min;
	}

	public final static int maxSlackTime(IJob... jobs) {
		int max = Integer.MIN_VALUE;
		for (IJob job : jobs) {
			final int v = job.getDuration() - job.getDueDate();
			if(v > max) {
				max = v;
			}
		}
		return max;
	}
}
