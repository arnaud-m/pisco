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

import pisco.batch.data.BJob;
import pisco.batch.heuristics.PDRScheduler;

public final class TaskPackingLmaxCF extends AbstractLmaxPackingCF {
	
	private final BJob taskPool[];
	
	private TaskSList taskL;
	
	public TaskPackingLmaxCF(PBatchRelaxSConstraint cstr) {
		super(cstr);
		this.taskL = (TaskSList) cstr.relaxF;
		final int n = cstr.problem.getN();
		final int m = cstr.problem.getM();
		taskPool = new BJob[n];
		for (int i = 0; i < n; i++) {
			taskPool[i]= new BJob(m+i);
		}
	}

	@Override
	protected int computeRelaxedLmax(int nbNewBatches) {
		assert nbNewBatches <= nbAvailableJobs;
		final int offset = nbAvailableJobs - nbNewBatches;
		for (int j = 0; j < nbNewBatches; j++) {
			taskPool[j].setDuration(newBatchDurations[j]);
			taskPool[j].setDueDate(newBatchDueDates[offset+j]);
		}
		// DONE 8 nov. 2011 - do not copy the array- created 5 nov. 2011 by Arnaud Malapert
		return PDRScheduler.insert(taskL.taskList, taskL.size, 
				cstr.problem.getPriorityDispatchingRule(),
				taskPool, nbNewBatches);
		
	}
	
	
}
