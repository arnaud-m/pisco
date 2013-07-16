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

import static choco.Choco.MAX_UPPER_BOUND;
import pisco.batch.AbstractBatchingProblem;
import pisco.batch.BatchSettings.PropagagationLevel;
import pisco.batch.data.BatchProcessingData;
import pisco.batch.data.BJob;
import pisco.common.ICostAggregator;
import pisco.common.PDR1Scheduler;
import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;


public class PBatchSConstraint extends AbstractLargeIntSConstraint{

	public final AbstractBatchingProblem problem;
	
	public final BatchProcessingData data;

	protected final int nbBatches;
	
	protected final int nbBatchesX2;

	protected final int nbBatchesX3;

	protected final int nbBIndex;

	protected final int objIndex;

	protected final IStateInt nbInstVarsB;

	public PBatchSConstraint(IEnvironment environment, IntDomainVar[] vars, AbstractBatchingProblem problem) {
		super(ConstraintEvent.VERY_SLOW, vars);
		this.problem = problem;
		this.data = this.problem.getData();
		this.nbBatches = this.problem.getM();
		this.nbBatchesX2 = 2*nbBatches;
		//initialize offsets
		nbBatchesX3 =  3 * nbBatches;
		objIndex = vars.length - 1;
		nbBIndex = objIndex - 1;
		nbInstVarsB = environment.makeInt();

	}

	@Override
	public int getFilteredEventMask(int idx) {
		return idx < nbBatchesX3 ? IntVarEvent.INSTINT_MASK : 0; //durations, weights, and due dates
	}

	protected final int getObjSup() {
		return vars[objIndex].getSup();
	}

	public final IntDomainVar getObjective() {
		return vars[objIndex];
	}

	public final IntDomainVar getNbBatches() {
		return vars[nbBIndex];
	}

	public final IntDomainVar getBDuration(int idx) {
		return vars[idx];
	}
	public final IntDomainVar getBWeight(int idx) {
		return vars[nbBatches + idx];
	}
	
	public final IntDomainVar getBDueDate(int idx) {
		return vars[nbBatchesX2 + idx];
	}
	
	public final IntDomainVar getJBatch(int idx) {
		return vars[nbBatchesX3 + idx];
	}
	
	protected final void updateInfObj(int val) throws ContradictionException {
		vars[objIndex].updateInf(val, this, false);
	}

	protected final void instantiateObjTo(int val) throws ContradictionException {
		vars[objIndex].instantiate( val, this, false);
	}

	protected final void updateInfNbB(int val) throws ContradictionException {
		vars[nbBIndex].updateInf( val, this, false);
	}
	
	protected final void updateSupNbB(int val) throws ContradictionException {
		vars[nbBIndex].updateSup( val, this, false);
	}

	protected final void instantiateNbBTo(int val) throws ContradictionException {
		vars[nbBIndex].instantiate( val, this, false);
	}


	@Override
	public void awake()	throws ContradictionException
	{
		int cpt = 0;
		for (int i = 0; i < this.nbBatchesX3; i++) {
			if (this.vars[i].isInstantiated()) {cpt++;}
		}
		this.nbInstVarsB.add(cpt);
		super.awake();
	}

	
	@Override
	public void awakeOnBounds(int varIndex) throws ContradictionException {
		this.constAwake(false);
	}

	@Override
	public void awakeOnInf(int varIdx) throws ContradictionException {
		this.constAwake(false);
	}

	@Override
	public void awakeOnInst(int idx) throws ContradictionException
	{
		if(idx < nbBatchesX3) {
			this.nbInstVarsB.increment();
		}
		this.constAwake(false);
	}
	
	/*
	 * nothing to do
	 */
	@Override
	public void awakeOnRem(int varIdx, int val) throws ContradictionException {
		this.constAwake(false);
	}

	/**
	 * nothing to do
	 */
	@Override
	public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain)
	throws ContradictionException {
		this.constAwake(false);
	}

	@Override
	public void awakeOnSup(int varIdx) throws ContradictionException {
		this.constAwake(false);
	}




	@Override
	public void propagate() throws ContradictionException
	{
		if (this.nbInstVarsB.get() == this.nbBatchesX3) {
			makeEntailed();
		}
		assert (this.nbInstVarsB.get() <= this.nbBatchesX3);
	}

	@Override
	public boolean isConsistent() {
		return false;
	}
	
	// TODO - store the array for child constraints - created 3 nov. 2011 by Arnaud Malapert
	protected final BJob[] makeBatches() {
		// FIXME - Number of batches is not always instantiated - created 23 sept. 2011 by Arnaud Malapert
		BJob[] batches = new BJob[getNbBatches().getSup()];
		int idx = 0;
		for (int i = 0; i < nbBatches; i++) {
			if( this.getBDuration(i).getVal() > 0) {
				assert idx < batches.length;
				batches[idx++] = new BJob(i+1, getBDuration(i).getVal(),getBWeight(i).getVal(), getBDueDate(i).getVal());
			}
		}
		for (int i = idx; i < batches.length; i++) {
			batches[idx++] = new BJob(i, 0, MAX_UPPER_BOUND);
		}
		return batches;
	}

	protected final BJob[] makeBatches(int[] tuple) {
		final int m = tuple[nbBIndex];
		final BJob[] batches = new BJob[m];
		int idx=0;
		for (int i = 0; i < nbBatches; i++) {
			if( tuple[i] > 0) {
				batches[idx++] = new BJob(i+1, tuple[i], tuple[nbBatches + i], tuple[nbBatchesX2 + i]);
			}
			if(idx >= nbBIndex) return null;
		}
		return batches;
	}

	
	@Override
	public final boolean isSatisfied(int[] tuple) {
		final BJob[] jobs = makeBatches(tuple);
		return jobs != null && tuple[objIndex] == PDR1Scheduler.schedule(jobs, jobs.length, problem.getPriorityDispatchingRule());
	}


	protected final void makeEntailed() throws ContradictionException {
		//durations and due Dates are instantiated
		final BJob[] batches = makeBatches();
		instantiateObjTo(PDR1Scheduler.schedule(batches, batches.length, problem.getPriorityDispatchingRule()));
		// FIXME - Set the number of batches - created 12 oct. 2011 by Arnaud Malapert
		//instantiateNbBTo(batches.length);
		setEntailed();
	}
}
