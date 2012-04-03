package pisco.common;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public final class PDRPmtnSchedule {

	private PDRPmtnSchedule() {
		super();
	}

	
	
	public final static void schedule(JobPmtn[] jobs, PrecGraph precGraph) {
		JobPmtn[] tjobs = Arrays.copyOf(jobs, jobs.length);
		Arrays.sort(tjobs, JobComparators.getEarliestReleaseDate());
		List<JobPmtn> pending = new LinkedList<JobPmtn>();
		PriorityQueue<JobPmtn> currents = new PriorityQueue<JobPmtn>();
		
	}
}
