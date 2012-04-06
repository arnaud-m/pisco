package pisco.common;

import static pisco.common.CostFactory.getCTime;
import static pisco.common.CostFactory.getLateness;
import static pisco.common.CostFactory.getWeightedCTime;
import static pisco.common.CostFactory.makeMaxCosts;
import static pisco.common.CostFactory.makeSumCosts;
import static pisco.common.JobComparators.getEarliestDueDate;
import static pisco.common.JobComparators.getShortestProcessingTime;
import static pisco.common.JobComparators.getWeightedShortestProcessingTime;

import java.util.Arrays;

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

	public static int sequence(final ITJob[] jobs, final PriorityDispatchingRule rule) {
		return sequence(jobs, jobs.length, rule);
	}

	public static int sequence(final ITJob[] jobs, final int n, final PriorityDispatchingRule rule) {
		assert n<= jobs.length;
		jobs[0].scheduleFrom(0);
		rule.globalCostFunction.setCost(rule.costFunction.getCost(jobs[0]));
		for (int i = 1; i < n; i++) {
			jobs[i].scheduleFrom(jobs[i-1].getLCT());
			rule.globalCostFunction.addCost(rule.costFunction.getCost(jobs[i]));	
		}
		return rule.globalCostFunction.getTotalCost();		
	}

	
	public static int schedule(final ITJob[] jobs, final PriorityDispatchingRule rule) {
		Arrays.sort(jobs, rule.priorityRule);
		return sequence(jobs, rule);
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

	
}
