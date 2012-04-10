package pisco.single;

import static pisco.common.JobComparators.getCompositeComparator;
import static pisco.common.JobComparators.getEarliestDueDate;
import static pisco.common.JobComparators.getEarliestReleaseDate;
import static pisco.common.JobComparators.getShortestProcessingTime;

import java.util.Arrays;
import java.util.Random;

import pisco.common.ITJob;
import pisco.common.PDR1Scheduler;
import choco.kernel.common.opres.heuristics.AbstractRandomizedHeuristic;

public final class SingleMachineRHeuristic extends AbstractRandomizedHeuristic {

	
	private final Abstract1MachineProblem problem;
	private final Random random = new Random();
	private ITJob[] jobs;

	public SingleMachineRHeuristic(Abstract1MachineProblem problem) {
		this.problem = problem;
	}

	

	@Override
	public void reset() {
		super.reset();
		jobs = new ITJob[problem.getNbJobs()];
	}



	@Override
	protected int apply(int iteration, int bestsol, int seed) {
		System.arraycopy(problem.jobs, 0, jobs, 0, problem.getNbJobs());
		switch (iteration) {
		case 0: break; //order defined by the data manager
		case 1: Arrays.sort(jobs, getCompositeComparator(getEarliestReleaseDate(), getEarliestDueDate()));break;
		case 2: Arrays.sort(jobs, getCompositeComparator(getEarliestReleaseDate(), getShortestProcessingTime()));break;
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
		if( ! problem.hasDeadlines()) {
			apply(new Random(problem.getSeed()));
		}
	}

	@Override
	public String toString() {
		return "Single Machine Randomized List Heuristic";
	}


}
