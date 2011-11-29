/**
*  Copyright (c) 2011, Arnaud Malapert
*  All rights reserved.
*  Redistribution and use in source and binary forms, with or without
*  modification, are permitted provided that the following conditions are met:
*
*      * Redistributions of source code must retain the above copyright
*        notice, this list of conditions and the following disclaimer.
*      * Redistributions in binary form must reproduce the above copyright
*        notice, this list of conditions and the following disclaimer in the
*        documentation and/or other materials provided with the distribution.
*      * Neither the name of the Arnaud Malapert nor the
*        names of its contributors may be used to endorse or promote products
*        derived from this software without specific prior written permission.
*
*  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
*  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
*  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
*  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
*  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
*  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
*  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
*  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
*  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
*  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package pisco.batch.heuristics;

import static pisco.batch.heuristics.CostFactory.makeMaxCosts;

import gnu.trove.TIntArrayList;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import choco.kernel.common.util.objects.IPrioritizable;

import pisco.batch.AbstractBatchingProblem;
import pisco.batch.data.Job;
import pisco.batch.data.JobComparatorFactory;


public final class PDRScheduler {

	private PDRScheduler() {
		super();
	}

	//*****************************************************************//
	//*******************  Priority dispatching rules ****************//
	//***************************************************************//

	public final static PriorityDispatchingRule EDD = new PriorityDispatchingRule(
				JobComparatorFactory.getEarliestDueDate(), 
				CostFactory.getLateness(), 
				CostFactory.makeMaxCosts()
				);
	
	public final static PriorityDispatchingRule SPT = new PriorityDispatchingRule(
			JobComparatorFactory.getShortestProcessingTime(), 
			CostFactory.getCTime(), 
			CostFactory.makeSumCosts()
			);
	
	public final static PriorityDispatchingRule WSPT = new PriorityDispatchingRule(
			JobComparatorFactory.getWeightedShortestProcessingTime(), 
			CostFactory.getWeightedCTime(), 
			CostFactory.makeSumCosts()
			);
	
	public static PriorityDispatchingRule getRuleEDD() {
		return EDD;
	}
	
	public static final PriorityDispatchingRule getRuleSPT() {
		return SPT;
	}

	public static final PriorityDispatchingRule getRuleWSPT() {
		return WSPT;
	}

	
	//*****************************************************************//
	//*******************  one machine scheduling ********************//
	//***************************************************************//


	public static int sequence(Job[] jobs, int n, PriorityDispatchingRule rule) {
		assert n<= jobs.length;
		jobs[0].setStartingTime(0);
		rule.globalCostFunction.setCost(rule.costFunction.getCost(jobs[0]));
		for (int i = 1; i < n; i++) {
			jobs[i].setStartingTime(jobs[i-1].getCompletionTime());
			rule.globalCostFunction.addCost(rule.costFunction.getCost(jobs[i]));	
		}
		return rule.globalCostFunction.getTotalCost();		
	}



	public static int schedule(Job[] jobs, int n, PriorityDispatchingRule rule) {
		Arrays.sort(jobs, 0, n, rule.priorityRule);
		return sequence(jobs, n, rule);		
	}


	/**
	 * the algorithm fails as soon as the objective value of the schedule exceeds the upper bound.
	 * @return a potentially incomplete schedule.
	 */
	public static boolean lazySequence(Job[] jobs, int n, PriorityDispatchingRule rule, int uppBound) {
		assert n<= jobs.length;
		jobs[0].setStartingTime(0);
		rule.globalCostFunction.setCost(rule.costFunction.getCost(jobs[0]));
		for (int i = 1; i < n; i++) {
			jobs[i].setStartingTime(jobs[i-1].getCompletionTime());
			rule.globalCostFunction.addCost(rule.costFunction.getCost(jobs[i]));
			if(rule.globalCostFunction.getTotalCost() > uppBound) return false;
		}
		return true;		
	}

	public static boolean lazySchedule(Job[] jobs, int n, PriorityDispatchingRule rule, int uppBound) {
		Arrays.sort(jobs, 0, n, rule.priorityRule);
		return lazySequence(jobs, n, rule, uppBound);		
	}


	public static int replace(Job[] schedule,  int n, PriorityDispatchingRule rule, Job job) {
		int cpos = 0;
		final int id = job.getId();
		//find current position of the job
		while(schedule[cpos].getId() != id) {cpos++;}
		//find its new position
		int npos = cpos;
		while(npos -1 >= 0 && rule.priorityRule.compare(schedule[npos-1], job) > 0) {
			npos--;
		}
		while(npos + 1 < n && rule.priorityRule.compare(schedule[npos+1], job) < 0) {
			npos++;
		}
		//System.out.println(cpos+" "+npos);
		//re-schedule
		rule.globalCostFunction.reset();
		if(npos > cpos) {
			//forward transfer
			for (int i = 0; i < cpos; i++) {
				rule.globalCostFunction.addCost(rule.costFunction.getCost(schedule[i]));
			}
			int delta = schedule[cpos].getDuration();
			for (int i = cpos+1; i <= npos; i++) {
				rule.globalCostFunction.addCost(rule.costFunction.getCost(schedule[i], schedule[i].getCompletionTime() - delta));
			}
			delta = job.getDuration() - schedule[cpos].getDuration();
			rule.globalCostFunction.addCost(rule.costFunction.getCost(job, schedule[npos].getCompletionTime() + delta));
			for (int i = npos+1; i < n; i++) {
				rule.globalCostFunction.addCost(rule.costFunction.getCost(schedule[i], schedule[i].getCompletionTime()+ delta));
			}
		} else {
			//backward transfer
			for (int i = 0; i < npos; i++) {
				rule.globalCostFunction.addCost(rule.costFunction.getCost(schedule[i]));
			}
			int delta=job.getDuration();
			rule.globalCostFunction.addCost(rule.costFunction.getCost(job, schedule[npos].getStartingTime()+ delta));
			for (int i = npos; i < cpos; i++) {
				rule.globalCostFunction.addCost(rule.costFunction.getCost(schedule[i], schedule[i].getCompletionTime() + delta));
			}
			delta=job.getDuration() - schedule[cpos].getDuration();
			for (int i = cpos+1; i < n; i++) {
				rule.globalCostFunction.addCost(rule.costFunction.getCost(schedule[i],schedule[i].getCompletionTime() + delta));
			}
		}
		return rule.globalCostFunction.getTotalCost();
	}

	public static int insert(Job[] schedule,  int n, PriorityDispatchingRule rule, Job... jobs) {
		return insert(schedule, n, rule, jobs, jobs.length);
	}
	
	public static int insert(Job[] schedule,  int n, PriorityDispatchingRule rule, Job[] jobs, int p) {
		assert n<= schedule.length;
		int i = 0;
		int j = 0;
		int ctime=0;
		rule.globalCostFunction.reset();
		//merge schedules
		while( i < n && j < p) {
			if(rule.priorityRule.compare(schedule[i], jobs[j]) > 0) {
				ctime += jobs[j].getDuration();
				rule.globalCostFunction.addCost(rule.costFunction.getCost(jobs[j++], ctime));
			} else {
				ctime += schedule[i].getDuration();
				rule.globalCostFunction.addCost(rule.costFunction.getCost(schedule[i++], ctime));
			}
		}
		//schedule remaining jobs
		if( j < p) {
			do {
				ctime += jobs[j].getDuration();
				rule.globalCostFunction.addCost(rule.costFunction.getCost(jobs[j++], ctime));
			} while(j < p);
		} else {
			assert i < n;
			do {
				ctime += schedule[i].getDuration();
				rule.globalCostFunction.addCost(rule.costFunction.getCost(schedule[i++], ctime));	
			}while (i < n);
		}		
		return rule.globalCostFunction.getTotalCost();
	}

	//*****************************************************************//
	//*******************  Classical PDR *****************************//
	//***************************************************************//

	public static int schedule1Lmax(Job[] jobs) {
		return schedule(jobs,jobs.length, EDD);
	}

	public static int schedule1Flow(Job[] jobs) {
		return schedule(jobs, jobs.length, SPT);
	}

	public static int schedule1WFlow(Job[] jobs) {
		return schedule(jobs, jobs.length, WSPT);
	}

	//*****************************************************************//
	//*******************  parallel machine scheduling ***************//
	//***************************************************************//

	public static int parallelSequence(Job[] jobs,int n, int m, PriorityDispatchingRule rule) {
		assert n<= jobs.length;
		int j = 0;
		int[] machines = new int[m];
		rule.globalCostFunction.reset();
		for (int k = 0; k < m; k++) {
			if(j >= n) break;
			jobs[j].setStartingTime(machines[k]);
			rule.globalCostFunction.addCost(rule.costFunction.getCost(jobs[j]));
			machines[k] = jobs[j].getCompletionTime();
			j++;
		}
		return rule.globalCostFunction.getTotalCost();		
	}



	public static int parallelSchedule(Job[] jobs, int n, int m, PriorityDispatchingRule rule) {
		Arrays.sort(jobs, 0, n, rule.priorityRule);
		return parallelSequence(jobs, n, m, rule);		
	}

	public static int parallelUnitSizedLmaxSchedule(Job[] jobs, int n, int m) {
		Arrays.sort(jobs, 0, n, EDD.priorityRule);
		return parallelUnitSizedLmaxSequence(jobs, n, m);
	}
	
	public static int parallelUnitSizedLmaxSequence(Job[] jobs, int n, int m) {
		assert n<= jobs.length;
		EDD.globalCostFunction.reset();	
		int[] machines = new int[m];
		int k = 0;
		for (int j = 0; j < n; j++) {
			for (int p = 0; p < jobs[j].getDuration(); p++) {
				for (int s = 0; s < jobs[j].getSize(); s++) {
					machines[k]++; 
					k = k < m - 1 ? k+1 : 0;
				}
			}
			EDD.globalCostFunction.addCost( EDD.costFunction.getCost(jobs[j], machines[k == 0 ? m-1 : k - 1]));
		}
		return EDD.globalCostFunction.getTotalCost();		
	}
	
	public static boolean testParallelpmtnSizedLmax(Job[] jobs, int n, int m) {
		return false;
		//return schedule(jobs, getEarliestDueDate(), getLateness(), makeMaxCosts());
	}

	
	public static int parallelUnitSizedWFlowSchedule(Job[] jobs, int n, int m) {
		assert n<= jobs.length;
		Arrays.sort(jobs, 0, n, JobComparatorFactory.getDecreasingParallelUnitWeight());
		double totalCost = 0;	
		int[] machines = new int[m];
		int k = 0;
		for (int j = 0; j < n; j++) {
			final double w = ( (double) jobs[j].getWeight() ) / ( jobs[j].getDuration() * jobs[j].getSize());
			for (int p = 0; p < jobs[j].getDuration(); p++) {
				for (int s = 0; s < jobs[j].getSize(); s++) {
					machines[k]++; 
					totalCost += w * machines[k];
					k = k < m - 1 ? k+1 : 0;
				}
			}
		}
		return (int) Math.ceil(totalCost);	
	}

	
}

