package pisco.common;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

public class TestCommon {


	private static AbstractJob[] buildInstance() {
		final int[] d =  {};
		final int[] rd =  {};
		final int[] dd =  {};
		final int n = d.length;
		AbstractJob[] jobs = new JobPmtn[n];
		for (int i = 0; i < n; i++) {
			jobs[i] = new JobPmtn(i);
			jobs[i].setDuration(d[i]);
			jobs[i].setReleaseDate(rd[i]);
			jobs[i].setDueDate(dd[i]);
		}
		return jobs;
	}
	
	private static void checkPermutation(int[] permutation, AbstractJob[] jobs) {
		for (int i = 0; i < jobs.length; i++) {
			Assert.assertEquals("check perm. index "+i, permutation[i], jobs[i].getId());
		}
	}
	
	@Test
	public void testJob() {
		AbstractJob[] jobs = buildInstance();
		int[] permutation = {};
		Arrays.sort(jobs, JobComparators.getShortestProcessingTime());
		checkPermutation(permutation, jobs);
	}

}
