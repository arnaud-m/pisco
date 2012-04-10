package pisco.common;

import static pisco.common.TJobAdapter.*;
import gnu.trove.TLinkedList;
import gnu.trove.TObjectProcedure;
import choco.kernel.common.IDotty;
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
	

	public final static void modifyDueDates(final ITJob[] jobs) {
		final TLinkedList<TJobAdapter> pendingJobs = new TLinkedList<TJobAdapter>();
		//initialize
		for (int i = 0; i < jobs.length; i++) {
			jobs[i].setHook(jobs[i].getSuccessorCount());
			if(jobs[i].getHook() == 0) {
				add(pendingJobs, jobs[i]);
			}
		}
		assert( ! pendingJobs.isEmpty());
		//compute topological order and modify due dates
		while( ! pendingJobs.isEmpty()) {
			final ITJob current = pendingJobs.getFirst().getTarget();
			AbstractJob.free(pendingJobs.removeFirst());
			current.forEachPredecessor( new TObjectProcedure<TJobAdapter>() {

				@Override
				public boolean execute(TJobAdapter object) {
					final int modifiedDueDate = current.getDueDate() - current.getDuration();
					if(modifiedDueDate < object.target.getDueDate() ) {
						object.target.setDueDate(modifiedDueDate);
					}
					if(object.target.decHook() == 0) {
						add(pendingJobs, object.target);
					}
					return true;
				}
			});

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
		return true;
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
}
