package pisco.single;

import static pisco.common.JobComparators.getCompositeComparator;
import static pisco.common.JobComparators.getEarliestDueDate;
import static pisco.common.JobComparators.getEarliestReleaseDate;
import static pisco.common.JobComparators.getShortestProcessingTime;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;

import pisco.common.ITJob;
import pisco.common.JobUtils;
import pisco.common.PDR1Scheduler;
import choco.kernel.common.opres.heuristics.AbstractRandomizedHeuristic;

public final class SingleMachineRHeuristic extends AbstractRandomizedHeuristic {


	private final Abstract1MachineProblem problem;
	private final Random random = new Random();

	private ITJob[] jobs;

	public SingleMachineRHeuristic(Abstract1MachineProblem problem) {
		this.problem = problem;
		// TODO - Add problem's settings - created 10 avr. 2012 by A. Malapert
		setIterationLimit(100);
	}


	public void storeSolution(ITJob[] schedule, int obj) {
		this.jobs = schedule;
		forceStoreSolution(obj);
	}

	

	@Override
	public void reset() {
		super.reset();
	}


	@Override
	protected int apply(int iteration, int bestsol, int seed) {
		System.arraycopy(problem.jobs, 0, jobs, 0, problem.getNbJobs());
		JobUtils.resetSchedule(jobs);
		switch (iteration) {
		case 0: Arrays.sort(jobs, getCompositeComparator(getEarliestReleaseDate(), getEarliestDueDate()));break;
		case 1: Arrays.sort(jobs, getCompositeComparator(getEarliestReleaseDate(), getShortestProcessingTime()));break;
		default	: {
			random.setSeed(seed); 
			PDR1Scheduler.shuffle(jobs, 10, random);
		}
		}
		return PDR1Scheduler.lazySequenceWithReleaseDates(jobs, problem.getCostFunction(), problem.getGlobalCostFunction(), bestsol);
	}


	@Override
	public int getLowerBound() {
		return problem.getComputedLowerBound();
	}

	@Override
	public void execute() {
		if(! existsSolution()) {
			//otherwise, previously stored solution given by the lower bounds
			jobs = new ITJob[problem.getNbJobs()];
			if( problem.hasDeadlines()) {
				LOGGER.log(Level.FINE, "heuristics...[dealines-only]");
				System.arraycopy(problem.jobs, 0, jobs, 0, problem.getNbJobs());
				forceStoreSolution( PDR1Scheduler.deadlineUpperBound(jobs, problem.getCostFunction(), problem.getGlobalCostFunction()));				
			} else {
				apply(new Random(problem.getSeed()));
			}
		}
	}

	@Override
	public String toString() {
		return "Single Machine Randomized List Heuristic";
	}


}
