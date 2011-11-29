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
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;

public class TaskAssignCF implements IBatchFilteringRule {

	public final PBatchRelaxSConstraint cstr;

	private final TaskSList taskL;

	private final Job[] taskPool;

	public TaskAssignCF(PBatchRelaxSConstraint cstr) {
		super();
		this.cstr = cstr;
		taskL= (TaskSList) cstr.relaxF;
		taskPool = new Job[cstr.problem.getM()];
		for (int i = 0; i < taskPool.length; i++) {
			taskPool[i] = new Job(i);
		}
	}

	@Override
	public void reset() {
	}

	@Override
	public void addBatch(int b) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addJob(int j) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setUp() {}

	@Override
	public void filter() throws ContradictionException {
		if(taskL.size() > 0) {
			final DisposableIntIterator iterj = cstr.candidateJobs.getIterator();
			try {
				while(iterj.hasNext()) {
					final int job = iterj.next();
					DisposableIntIterator iterb = cstr.getJBatch(job).getDomain().getIterator();
					try {
						while(iterb.hasNext()) {
							final int batch = iterb.next();
							// DONE 14 nov. 2011 - Problem with non empty batches - created 4 nov. 2011 by Arnaud Malapert
							if(cstr.getBDuration(batch).getInf() > 0) {
								taskPool[batch].combine(taskL.taskPool[batch], cstr.data.sjobs[job]);
								final int lb = PDRScheduler.replace(taskL.taskList, taskL.size, cstr.problem.getPriorityDispatchingRule(), 
										taskPool[batch]);
								// DONE 4 nov. 2011 - Compute the lower bound - created 4 nov. 2011 by Arnaud Malapert
								if(lb > cstr.getObjSup() && cstr.deleteAssignment(job, batch) ) {
									iterj.remove();
									break;
								}
							} else {
								final int lb = PDRScheduler.insert(taskL.taskList, taskL.size, cstr.problem.getPriorityDispatchingRule(),cstr.data.sjobs[job]);
								if(lb > cstr.getObjSup() && cstr.deleteFromEmptyBatches(job) ){
									iterj.remove();
									break;
								}
								break ; //next batches are also empty
							}
						}
					} finally {
						iterb.dispose();
					}
				}
			} finally {
				iterj.dispose();
			}
		}
	}

}
