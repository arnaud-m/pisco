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
package pisco.shop.choco.branching;

import choco.kernel.model.constraints.Constraint;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.scheduling.TaskVar;

/**
 * Finish the shop branching in a backtrack-free way
 */
public final class FinishBranchingNaive extends AbstractShopFakeBranching {


	private int selected;
	
	private int begin;
	
	
	public FinishBranchingNaive(Solver solver, Constraint constraintCut) {
		super(solver, constraintCut);
	}

	private final void swap(int i) {
		final TaskVar tmp = tasks[i];
		tasks[i]=tasks[begin];
		tasks[begin] = tmp;
		if(selected == begin) {selected=i;}
		begin++;
	}
	/**
	 * find the task with minimal EST and schedule it at EST, then propagate.
	 * We can not sort the tasks before scheduling them because the EST order can change by propagation.
	 * The only optimization is to iterate only on non instnatiated variables.
	 * This approach has the following drawbacks: many iterations over the set of tasks and propagation phases.
	 */
	@Override
	protected void schedule() throws ContradictionException {
		//<hca> weird it seems that some lower bound are not up to date
		//and each one needs to be propagated because instantiating the next one
		// maybe because of some symetry !!!
		// the instanciation order seems also important

		//Other problem: if the upper bound is too weak, then we can have acontradiction between precedence and LST cut !
		
		//Previous version: sort the tasks in EST, then schedule the tasks in the given order
		//PROBLEM: EST can be changed by propagation after scheduling a task.
		//			Arrays.sort(tasks, TaskComparators.makeEarliestStartingTimeCmp());
		//			for (TaskVar t : tasks) {
		//				//System.out.println(t.pretty());;
		//				if(!t.start().isInstantiated()) {
		//					t.start().instantiate(t.start().getInf(), VarEvent.NOCAUSE);
		//				}
		//				solver.propagate();
		//			}

		//FIX: select the task with the minimum EST then schedule the task (no sorting)
		begin = 0;
		while(begin < tasks.length) {
			int min = Integer.MAX_VALUE;
			selected = -1;
			int idx = begin;
			while( idx < tasks.length) {
				if(tasks[idx].start().isInstantiated()) {
					//swap with the first element to check
					swap(idx);
				}else {
					if(tasks[idx].getEST() < min) {
						min = tasks[idx].getEST();
						selected =idx;
					}
				}
				idx++;
			}
			if(selected > -1) {
				tasks[selected].start().instantiate(tasks[selected].getEST(), null, false);
				//System.out.println(tasks[selected].pretty());
				swap(selected);
				solver.propagate();
			}
		}	
	}
}
