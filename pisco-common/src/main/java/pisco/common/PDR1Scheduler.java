package pisco.common;

import static choco.Choco.MAX_UPPER_BOUND;
import static choco.Choco.MIN_LOWER_BOUND;
import static pisco.common.CostFactory.getCTime;
import static pisco.common.CostFactory.getLateness;
import static pisco.common.CostFactory.getWeightedCTime;
import static pisco.common.CostFactory.makeMaxCosts;
import static pisco.common.CostFactory.makeSumCosts;
import static pisco.common.JobComparators.getEarliestDueDate;
import static pisco.common.JobComparators.getShortestProcessingTime;
import static pisco.common.JobComparators.getWeightedShortestProcessingTime;
import static pisco.common.JobUtils.isScheduled;

import gnu.trove.TObjectProcedure;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Random;

public final class PDR1Scheduler {

	//*****************************************************************//
	//*******************  Priority dispatching rules ****************//
	//***************************************************************//

	public final static PriorityDispatchingRule EDD = new PriorityDispatchingRule(
			getEarliestDueDate(), 
			getLateness(), 
			makeMaxCosts()
			);
	public final static PriorityDispatchingRule SPT = new PriorityDispatchingRule(
			getShortestProcessingTime(), 
			getCTime(), 
			makeSumCosts()
			);
	public final static PriorityDispatchingRule WSPT = new PriorityDispatchingRule(
			getWeightedShortestProcessingTime(), 
			getWeightedCTime(), 
			makeSumCosts()
			);

	private PDR1Scheduler() {
		super();
	}

	public static PriorityDispatchingRule getRuleEDD() {
		return EDD;
	}

	public static final PriorityDispatchingRule getRuleSPT() {
		return SPT;
	}

	public static final PriorityDispatchingRule getRuleWSPT() {
		return WSPT;
	}

	public static int sequence(final ITJob[] jobs, final int n, final PriorityDispatchingRule rule) {
		return sequence(jobs, n, rule.costFunction, rule.globalCostFunction);
	}

	public static int sequence(final ITJob[] jobs, final int n, final ICostFunction costFunction, final ICostAggregator globalCostFunction) {
		assert n<= jobs.length;
		jobs[0].scheduleFrom(0);
		globalCostFunction.setCost(costFunction.getCost(jobs[0]));
		for (int i = 1; i < n; i++) {
			jobs[i].scheduleFrom(jobs[i-1].getLCT());
			globalCostFunction.addCost(costFunction.getCost(jobs[i]));	
		}
		return globalCostFunction.getTotalCost();		
	}


	public static int schedule(final ITJob[] jobs, final PriorityDispatchingRule rule) {
		Arrays.sort(jobs, rule.priorityRule);
		return sequence(jobs, jobs.length, rule);
	}

	public static int schedule(final ITJob[] jobs, final int n, final PriorityDispatchingRule rule) {
		Arrays.sort(jobs, 0, n, rule.priorityRule);
		return sequence(jobs, n, rule);		
	}

	//*****************************************************************//
	//*******************  Classical PDR *****************************//
	//***************************************************************//


	public static int schedule1Lmax(ITJob[] jobs) {
		return schedule(jobs,jobs.length, EDD);
	}

	public static int schedule1Flow(ITJob[] jobs) {
		return schedule(jobs, jobs.length, SPT);
	}

	public static int schedule1WFlow(ITJob[] jobs) {
		return schedule(jobs, jobs.length, WSPT);
	}

	////////////////////////////////////////////////////////////////////
	///////////////////// Lawler algorithms : 1|prec|Fmax //////////////
	////////////////////////////////////////////////////////////////////

	/**
	 * Lawler algorithm : build a sequence in backward order.
	 */
	public final static int schedule1PrecLmax(AbstractJob[] jobs) {
		//initialize
		int currentTime = 0;
		int lmax = MIN_LOWER_BOUND;
		final PriorityQueue<ITJob> pendingJobs= new PriorityQueue<ITJob>(10, JobComparators.getLatestDueDate());
		for (int i = 0; i < jobs.length; i++) {
			currentTime += jobs[i].getDuration();
			jobs[i].setHook(jobs[i].getSuccessorCount());
			if(jobs[i].getHook() == 0) {
				pendingJobs.add(jobs[i]);
			}
		}
		//Lawler algorithm : build sequence in backward order P = sum pj
		while( ! pendingJobs.isEmpty()) {
			//schedule job with the latest due date (minimize min fj(P))
			final ITJob job = pendingJobs.remove();
			job.scheduleTo(currentTime);
			currentTime = job.getEST();
			//Compute lateness
			final int lateness = job.getLateness();
			if(lmax < lateness) { lmax = lateness;}
			//Update pending jobs
			job.forEachPredecessor(new TObjectProcedure<TJobAdapter>() {

				@Override
				public boolean execute(TJobAdapter object) {
					if( object.target.decHook() == 0) {
						pendingJobs.add(object.target);
					}
					return true;
				}
			});
		}
		assert(isScheduled(jobs));
		return lmax;
	}


	////////////////////////////////////////////////////////////////////
	///////////////////// Sequence algorithm : 1|rj|gamma //////////////
	////////////////////////////////////////////////////////////////////


	public static void shuffle(final ITJob[] jobs, final int maxShift, final Random rnd) {
		for (int i = 0; i < jobs.length-maxShift; i++) {
			final int position = i + rnd.nextInt(maxShift);
			final ITJob temp = jobs[i];
			jobs[i] = jobs[position];
			jobs[position] = temp;
		}
		for (int i = jobs.length-maxShift; i < jobs.length; i++) {
			final int position = i + rnd.nextInt(jobs.length - i);
			final ITJob temp = jobs[i];
			jobs[i] = jobs[position];
			jobs[position] = temp;
		}
	}

	public static int sequenceWithReleaseDates(final ITJob[] jobs, final ICostFunction costFunction, final ICostAggregator globalCostFunction) {
		jobs[0].scheduleFrom(jobs[0].getReleaseDate());
		globalCostFunction.setCost(costFunction.getCost(jobs[0]));
		for (int i = 1; i < jobs.length; i++) {
			jobs[i].scheduleFrom(Math.max(jobs[i-1].getLCT(), jobs[i].getReleaseDate()));
			globalCostFunction.addCost(costFunction.getCost(jobs[i]));	
		}
		return globalCostFunction.getTotalCost();
	}

	public static int lazySequenceWithReleaseDates(final ITJob[] jobs, final ICostFunction costFunction, final ICostAggregator globalCostFunction, final int ub) {
		jobs[0].scheduleFrom(jobs[0].getReleaseDate());
		globalCostFunction.setCost(costFunction.getCost(jobs[0]));
		for (int i = 1; i < jobs.length; i++) {
			jobs[i].scheduleFrom(Math.max(jobs[i-1].getLCT(), jobs[i].getReleaseDate()));
			globalCostFunction.addCost(costFunction.getCost(jobs[i]));	
			if(globalCostFunction.getTotalCost() >= ub) {
				return MAX_UPPER_BOUND;
			}
		}
		return globalCostFunction.getTotalCost();
	}



	////////////////////////////////////////////////////////////////////
	/////////// upper bound of gamma given by the deadlines ////////////
	////////////////////////////////////////////////////////////////////

	public static int deadlineUpperBound(final ITJob[] jobs, final ICostFunction costFunction, final ICostAggregator globalCostFunction) {
		globalCostFunction.reset();
		for (int i = 1; i < jobs.length; i++) {
			jobs[i].scheduleFrom(Math.max(jobs[i-1].getLCT(), jobs[i].getReleaseDate()));
			globalCostFunction.addCost(costFunction.getCost(jobs[i], jobs[i].getDeadline()));	
		}
		return globalCostFunction.getTotalCost();
	}
}