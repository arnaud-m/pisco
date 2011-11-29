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
package pisco.batch.choco.constraints;

import pisco.batch.data.Job;
import pisco.batch.heuristics.PDRScheduler;
import choco.kernel.solver.ContradictionException;

public class TaskSList implements IBatchFilteringRule {

	protected final PBatchRelaxSConstraint cstr;

	public final Job[] taskPool;

	public final Job[] taskList;

	public int size;

	public TaskSList(PBatchRelaxSConstraint cstr) {
		super();
		this.cstr=cstr;
		final int m = cstr.problem.getM();
		taskList = new Job[m];
		taskPool = new Job[m];
		for (int i = 0; i < m; i++) {
			taskPool[i] = new Job(i);
		}
	}

	@Override
	public void reset() {
		size=0;
	}


	@Override
	public void addBatch(int b) {
		taskList[size]=taskPool[b];
		taskList[size].setDuration(cstr.getBDuration(b).getInf());
		taskList[size].setWeight(cstr.getBWeight(b).getInf());
		taskList[size].setDueDate(cstr.getBDueDate(b).getSup());
		size++;
	}

	@Override
	public void addJob(int j) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setUp() {}

	
	protected final void filterSingleMachine() throws ContradictionException {
		if(size() > 0 ) {
			final int lb = PDRScheduler.schedule(
					taskList, size, 
					cstr.problem.getPriorityDispatchingRule()
					);
			cstr.updateInfObj(lb);
		}
	}
	
	@Override
	public void filter() throws ContradictionException {
		filterSingleMachine();
	}

	public final int size() {
		return size;
	}


}
