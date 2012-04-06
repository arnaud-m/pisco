package pisco.common;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

import choco.visu.components.chart.ChocoChartFactory;

public class TestCommon {


	private static PJob[] buildInstance() {
		final int[] d =  {10, 6, 8, 4, 6};
		final int[] rd =  {0, 3, 5 , 7, 8};
		final int[] dd =  {15, 10, 25, 14, 12};
		final int n = d.length;
		PJob[] jobs = new PJob[n];
		for (int i = 0; i < n; i++) {
			jobs[i] = new PJob(i);
			jobs[i].setDuration(d[i]);
			jobs[i].setReleaseDate(rd[i]);
			jobs[i].setDueDate(dd[i]);
			jobs[i].resetSchedule();
		}
		jobs[0].addSuccessor(jobs[3]);
		jobs[4].addSuccessor(jobs[1]);
		return jobs;
	}
	
	private static void checkPermutation(int[] permutation, AbstractJob[] jobs) {
		for (int i = 0; i < jobs.length; i++) {
			Assert.assertEquals("check perm. index "+i, permutation[i], jobs[i].getID());
		}
	}
	
	@Test
	public void testSorting() {
		AbstractJob[] jobs = buildInstance();
		Arrays.sort(jobs, JobComparators.getShortestProcessingTime());
		checkPermutation(new int[] {3, 1, 4, 2, 0}, jobs);
		
		jobs = buildInstance();
		Arrays.sort(jobs, JobComparators.getEarliestDueDate());
		checkPermutation(new int[] {1, 4, 3, 0, 2}, jobs);
		
		jobs = buildInstance();
		Arrays.sort(jobs, JobComparators.getEarliestReleaseDate());
		checkPermutation(new int[] {0, 1, 2, 3, 4}, jobs);
	}
	

	@Test
	public void testPreemptiveSchedule() {
		PJob[] jobs = buildInstance();
		System.out.println(Arrays.toString(jobs));
		Pmtn1Scheduler.schedule(jobs);
		ChocoChartFactory.createAndShowGUI("Test", ChocoChartFactory.createGanttChart("Test", jobs));
		
		//PDRPmtnSchedule.schedule2(jobs);
		//PDRPmtnSchedule.ScheduleLawlerLmax(jobs);
		System.out.println(Arrays.toString(jobs));
	}

	public static void main(String[] args) {
		PJob[] jobs = buildInstance();
		System.out.println(Arrays.toString(jobs));
		Pmtn1Scheduler.schedule(jobs);
		System.out.println(Arrays.toString(jobs));
		ChocoChartFactory.createAndShowGUI("Test", ChocoChartFactory.createGanttChart("Test", jobs));
		
	}
}
