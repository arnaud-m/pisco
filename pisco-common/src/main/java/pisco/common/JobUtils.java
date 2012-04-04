package pisco.common;

public final class JobUtils {

	private JobUtils() {}
	
	public final boolean shiftLeftReleaseDates(IJob... jobs) {
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
	
	public final int minDuration(IJob... jobs) {
		int min = Integer.MAX_VALUE;
		for (IJob job : jobs) {
			if(job.getDuration() < min) {
				min = job.getDuration();
			}
		}
		return min;
	}
	
	public final int maxDuration(IJob... jobs) {
		int max = Integer.MIN_VALUE;
		for (IJob job : jobs) {
			if(job.getDuration() > max) {
				max = job.getDuration();
			}
		}
		return max;
	}
	

	public final int sumDurations(IJob... jobs) {
		int sum = 0;
		for (IJob job : jobs) {
			sum += job.getDuration();
		}
		return sum;
	}
	
	public final int[] durations(IJob... jobs) {
		int[] tab = new int[jobs.length];
		for (int i = 0; i < tab.length; i++) {
			tab[i] = jobs[i].getDuration();
		}
		return tab;
	}
	
	public final int minSize(IJob... jobs) {
		int min = Integer.MAX_VALUE;
		for (IJob job : jobs) {
			if(job.getSize() < min) {
				min = job.getSize();
			}
		}
		return min;
	}
	
	public final int maxSize(IJob... jobs) {
		int max = Integer.MIN_VALUE;
		for (IJob job : jobs) {
			if(job.getSize() > max) {
				max = job.getSize();
			}
		}
		return max;
	}
	

	public final int sumSizes(IJob... jobs) {
		int sum = 0;
		for (IJob job : jobs) {
			sum += job.getReleaseDate();
		}
		return sum;
	}
	
	public final int[] sizes(IJob... jobs) {
		int[] tab = new int[jobs.length];
		for (int i = 0; i < tab.length; i++) {
			tab[i] = jobs[i].getSize();
		}
		return tab;
	}
	
	public final int minReleaseDate(IJob... jobs) {
		int min = Integer.MAX_VALUE;
		for (IJob job : jobs) {
			if(job.getReleaseDate() < min) {
				min = job.getReleaseDate();
			}
		}
		return min;
	}
	
	public final int maxReleaseDate(IJob... jobs) {
		int max = Integer.MIN_VALUE;
		for (IJob job : jobs) {
			if(job.getReleaseDate() > max) {
				max = job.getReleaseDate();
			}
		}
		return max;
	}
	
	public final int[] releaseDates(IJob... jobs) {
		int[] tab = new int[jobs.length];
		for (int i = 0; i < tab.length; i++) {
			tab[i] = jobs[i].getReleaseDate();
		}
		return tab;
	}
	
	public final int minDeadline(IJob... jobs) {
		int min = Integer.MAX_VALUE;
		for (IJob job : jobs) {
			if(job.getDeadline() < min) {
				min = job.getDeadline();
			}
		}
		return min;
	}
	
	public final int maxDeadline(IJob... jobs) {
		int max = Integer.MIN_VALUE;
		for (IJob job : jobs) {
			if(job.getDeadline() > max) {
				max = job.getDeadline();
			}
		}
		return max;
	}
	
	public final int[] deadlines(IJob... jobs) {
		int[] tab = new int[jobs.length];
		for (int i = 0; i < tab.length; i++) {
			tab[i] = jobs[i].getDeadline();
		}
		return tab;
	}
	public final int minWeight(IJob... jobs) {
		int min = Integer.MAX_VALUE;
		for (IJob job : jobs) {
			if(job.getWeight() < min) {
				min = job.getWeight();
			}
		}
		return min;
	}
	
	public final int maxWeight(IJob... jobs) {
		int max = Integer.MIN_VALUE;
		for (IJob job : jobs) {
			if(job.getWeight() > max) {
				max = job.getWeight();
			}
		}
		return max;
	}
	

	public final int sumWeights(IJob... jobs) {
		int sum = 0;
		for (IJob job : jobs) {
			sum += job.getWeight();
		}
		return sum;
	}
	
	public final int[] weights(IJob... jobs) {
		int[] tab = new int[jobs.length];
		for (int i = 0; i < tab.length; i++) {
			tab[i] = jobs[i].getWeight();
		}
		return tab;
	}
	
	public final int minDueDate(IJob... jobs) {
		int min = Integer.MAX_VALUE;
		for (IJob job : jobs) {
			if(job.getDueDate() < min) {
				min = job.getDueDate();
			}
		}
		return min;
	}
	
	public final int maxDueDate(IJob... jobs) {
		int max = Integer.MIN_VALUE;
		for (IJob job : jobs) {
			if(job.getDueDate() > max) {
				max = job.getDueDate();
			}
		}
		return max;
	}
	
	public final int[] dueDates(IJob... jobs) {
		int[] tab = new int[jobs.length];
		for (int i = 0; i < tab.length; i++) {
			tab[i] = jobs[i].getDueDate();
		}
		return tab;
	}
}
