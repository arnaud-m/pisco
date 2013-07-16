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

import static choco.cp.solver.variables.integer.IntVarEvent.BOUNDS_MASK;
import static choco.cp.solver.variables.integer.IntVarEvent.DECSUP_MASK;
import static choco.cp.solver.variables.integer.IntVarEvent.INCINF_MASK;
import static choco.cp.solver.variables.integer.IntVarEvent.INSTINT_MASK;
import static choco.cp.solver.variables.integer.IntVarEvent.REMVAL_MASK;
import static pisco.batch.BatchSettings.PropagagationLevel.DECOMP;
import static pisco.batch.BatchSettings.PropagagationLevel.JOBS;
import static pisco.batch.BatchSettings.PropagagationLevel.OBJ;
import pisco.batch.AbstractBatchingProblem;
import pisco.batch.BatchSettings;
import pisco.batch.BatchSettings.PropagagationLevel;
import pisco.batch.PBatchLmax;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBool;
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.IStateIntVector;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.variables.integer.IntDomainVar;

public class PBatchRelaxSConstraint extends
PBatchSConstraint {

	/**
	 * the list of remaining jobs
	 */
	protected final IStateIntVector candidateJobs;
	/**
	 * the list of neither empty nor full batches 
	 */
	protected final IStateIntVector candidateBatches;

	protected final IStateInt lastNonEmptyBatchIndex;

	protected final boolean filterJobAssignments;

	protected final IStateBool filterNewBatches;

	protected final IBatchFilteringRule relaxF;	

	protected final IBatchFilteringRule assignCF;

	protected final IBatchPackingCF packCF;

	public PBatchRelaxSConstraint(IEnvironment environment, IntDomainVar[] vars,
			AbstractBatchingProblem problem) {
		super(environment, vars, problem);
		final PropagagationLevel slevel = BatchSettings.getSinglePropagationLevel(problem);
		final PropagagationLevel plevel = BatchSettings.getParallelPropagationLevel(problem);
		if( ! slevel.isOn() && ! plevel.isOn() ) {
			throw new SolverException("Constraint Manager : Decomposition Level [FAIL]");
		}
		//initialize lists and arrays
		candidateBatches = environment.makeBipartiteIntList(ArrayUtils.zeroToN(nbBatches));
		candidateJobs = environment.makeBipartiteIntList(ArrayUtils.zeroToN(data.nbJobs));
		lastNonEmptyBatchIndex = environment.makeInt(-1);
		filterJobAssignments = slevel.ordinal() > OBJ.ordinal();
		filterNewBatches = environment.makeBool(slevel.ordinal() > JOBS.ordinal());
		
		
		if (problem instanceof PBatchLmax) {
			if(BatchSettings.useBuckets(problem)) {
				if(plevel.isOn()) {
					throw new SolverException("No parallel relaxation with the buckets");
				}
				relaxF = new BucketList(this);
				assignCF = new BucketAssignCF(this);
				packCF = new BucketPackingCF(this);
			} else {
				relaxF = ( 
						plevel.isOn() ?   
								( BatchSettings.usePunitRelaxation(problem) ?	
										new TaskPunitLmaxF(this, slevel.isOn()) : 
											new TaskPpmtnLmaxF(this, slevel.isOn()) ):
												new TaskSList(this)
						);
				assignCF = new TaskAssignCF(this);
				packCF = new TaskPackingLmaxCF(this);
			}
		} else {
			relaxF = ( 
					plevel.isOn() ?   
							( BatchSettings.usePunitRelaxation(problem) ?	
									new TaskPunitWFlowF(this, slevel.isOn()) : 
										new TaskPpmtnWFlowF(this, slevel.isOn()) ):
											new TaskSList(this)
					);
			assignCF = new TaskAssignCF(this);
			packCF = new TaskPackingWFlowCF(this);
		}
	}

	@Override
	public int getFilteredEventMask(int idx) {
		if(idx <  nbBatchesX2) { return INCINF_MASK + INSTINT_MASK;}// Durations, Weights
		else if(idx <  nbBatchesX3) { return DECSUP_MASK + INSTINT_MASK;}//due Dates
		else if(idx <  nbBIndex) {return REMVAL_MASK + INSTINT_MASK;} //job assignment
		else if(idx < objIndex){return BOUNDS_MASK;} //number of batches 
		else return DECSUP_MASK; //obj
	}

	protected final void updateSupDuration(int batch, int maxDelta) throws ContradictionException {
		vars[batch].updateSup(vars[batch].getInf() + maxDelta, this, false);
			
	}

	protected final int getNbNonEmpty() {
		return lastNonEmptyBatchIndex.get() + 1;
	}

	protected void fireLastNonEmptyBatch(int job) {
		final IntDomainVar v = getJBatch(job);
		if(v.getVal() > lastNonEmptyBatchIndex.get()) {
			lastNonEmptyBatchIndex.set(v.getVal());
		}
	}

	protected void fireCandidateJob(int job) {
		final DisposableIntIterator iter = candidateJobs.getIterator();
		while(iter.hasNext()) {
			if(iter.next() == job ) {
				iter.remove();
				break;
			}
		}
		iter.dispose();
	}

	protected final void removeAssignment(int job, int batch) throws ContradictionException {
		final IntDomainVar v = getJBatch(job);
		if( v.removeVal(batch, this, false) && v.isInstantiated()) {
			fireLastNonEmptyBatch(job);
			fireCandidateJob(job);
		}

	}

	protected final boolean deleteAssignment(int job, int batch) throws ContradictionException {
		final IntDomainVar v = getJBatch(job);
		if( v.removeVal(batch, this, false) && v.isInstantiated()) {
			fireLastNonEmptyBatch(job);
			return true;
		} else return false;

	}

	protected final void removeFromEmptyBatches(int job)
			throws ContradictionException {
		if( deleteFromEmptyBatches(job)) {
			fireCandidateJob(job);
		}
	}

	protected final boolean deleteFromEmptyBatches(int job)
			throws ContradictionException {
		final IntDomainVar v = getJBatch(job);
		return v.updateSup(lastNonEmptyBatchIndex.get(), this, false) && v.isInstantiated();
	}


	@Override
	public void awake() throws ContradictionException {
		DisposableIntIterator iter = candidateJobs.getIterator();
		while(iter.hasNext()) {
			final int j = iter.next();
			if(getJBatch(j).isInstantiated()) {
				fireLastNonEmptyBatch(j);
				iter.remove();
			}
		}
		iter.dispose();
		super.awake();
	}


	@Override
	public void awakeOnInst(int idx) throws ContradictionException {
		if(idx < nbBIndex && idx >= this.nbBatchesX3) {
			final int job = idx - nbBatchesX3;
			fireLastNonEmptyBatch(job);
			fireCandidateJob(job);
		}
		super.awakeOnInst(idx);
	}


	protected void addCandidateBatches() {
		DisposableIntIterator iter = candidateBatches.getIterator();
		while(iter.hasNext()) {
			final int b = iter.next();
			final IntDomainVar dv = getBDuration(b);
			if(dv.getInf() > 0) {
				relaxF.addBatch(b);
			}else if(dv.getSup() == 0) {
				iter.remove();
			}
		}
		iter.dispose();
	}

	protected void addAvailableJobs() {
		DisposableIntIterator iter = candidateJobs.getIterator();
		while(iter.hasNext()) {
			final int j = iter.next();
			if( getJBatch(j).getSup() > lastNonEmptyBatchIndex.get()) {
				packCF.addJob(j);
			}
		}
		iter.dispose();
	}

	@Override
	public void propagate() throws ContradictionException {
		//ChocoLogging.flushLogs();
		super.propagate();
		relaxF.reset();
		addCandidateBatches();
		//compute pricing value
		relaxF.setUp(); // Parallel Machine -> add candidate jobs 
		//update objVar variable
		relaxF.filter();
		//System.out.println(bucketL);
		if(filterJobAssignments) { 
			assignCF.reset();
			assignCF.setUp(); //add candidate jobs to buckets
			//filter all assignments
			assignCF.filter();
			if(filterNewBatches.get() ) {
				packCF.reset();
				addAvailableJobs();
				packCF.filterNaive();
				if(packCF.isEntailed()) {
					filterNewBatches.set(false);
				}else {
					packCF.setUp();
					packCF.filter();
					if(packCF.isEntailed()) {
						filterNewBatches.set(false);
					}
				}
			}
			super.propagate(); //check if entailed
		}
	}
}