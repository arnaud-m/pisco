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
import choco.cp.solver.constraints.global.pack.IPackSConstraint;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.opres.nosum.NoSumList;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;

public abstract class AbstractTaskPList extends TaskSList {

	private static interface IFilteringWrapper {
		void filter(AbstractTaskPList o) throws ContradictionException;
	}

	private final static IFilteringWrapper PARALLEL = new IFilteringWrapper() {

		@Override
		public void filter(AbstractTaskPList o) throws ContradictionException {
			o.filterParallelMachines();
		}
	};

	private final static IFilteringWrapper MIXED = new IFilteringWrapper() {

		@Override
		public void filter(AbstractTaskPList o) throws ContradictionException {
			o.filterSingleMachine();
			o.filterParallelMachines();
		}
	};

	public int psize;

	public final BJob[] taskPList;

	private final IFilteringWrapper filteringWrapper;

	public AbstractTaskPList(PBatchRelaxSConstraint cstr, boolean singleAndParallel) {
		super(cstr);
		taskPList = new BJob[cstr.problem.getM() + cstr.problem.getN()];
		filteringWrapper = singleAndParallel ? MIXED : PARALLEL;
	}


	@Override
	public void reset() {
		super.reset();
		psize=0;
	}

	@Override
	public final void addBatch(int b) {
		super.addBatch(b);
		IPackSConstraint ct = (IPackSConstraint) cstr.problem.getSolver().getCstr(cstr.problem.getPackCstr());
		final int capa = cstr.data.getCapacity();
		final NoSumList s = ct.getStatus(b);
		taskList[size-1].setSize(
				s.getNbCandidates() == 0 ? capa :
					s.getRequiredLoad() + capa - ct.getLoads()[b].getSup()
				);
	}


	@Override
	public final void addJob(int j) {
		taskPList[psize++]=cstr.data.getJob(j);
	}

	@Override
	public void setUp() {
		System.arraycopy(taskList, 0, taskPList, 0, size);
		psize=size;	
		final DisposableIntIterator iter = cstr.candidateJobs.getIterator();
		while(iter.hasNext()) {
			addJob(iter.next());
		}
		iter.dispose();
	}

	protected abstract void filterParallelMachines() throws ContradictionException;


	@Override
	public final void filter() throws ContradictionException {
		filteringWrapper.filter(this);
	}

	public final int parallelSize() {
		return psize;
	}


}
