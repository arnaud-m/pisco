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
package pisco.common.choco.branching;

import java.util.logging.Level;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.branch.AbstractIntBranchingStrategy;
import choco.kernel.solver.search.IntBranchingDecision;

/**
 * a "fake" branching which does not branch but apply a polynomial rule to instantiate all remaining integer variables.
 * @author Arnaud Malapert</br> 
 * @since 9 avr. 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 */
public abstract class AbstractFakeBranching extends AbstractIntBranchingStrategy {

	public final Solver solver;

	public AbstractFakeBranching(Solver solver) {
		super();
		this.solver = solver;
	}


	@Override
	public final boolean finishedBranching(IntBranchingDecision decision) {
		return true;
	}


	@Override
	public final String getDecisionLogMessage(IntBranchingDecision decision) {
		return "FAKE";
	}


	@Override
	public final void goDownBranch(IntBranchingDecision decision)
			throws ContradictionException {
		throw new SolverException("Fake branching");

	}


	@Override
	public final void goUpBranch(IntBranchingDecision decision)
			throws ContradictionException {
		throw new SolverException("Fake branching");

	}


	@Override
	public final void setFirstBranch(IntBranchingDecision decision) {
		throw new SolverException("Fake branching");
	}


	@Override
	public final void setNextBranch(IntBranchingDecision decision) {
		throw new SolverException("Fake branching");
	}


	protected void fakeBranchingError(String msg, Object...objects) {
		LOGGER.log(Level.SEVERE,msg, objects);
		throw new SolverException("Fake Branching Internal Error");
	}


	protected abstract void setUp();

	protected abstract void doFakeBranching() throws ContradictionException;

	protected abstract void tearDown();

	@Override
	public final Object selectBranchingObject() throws ContradictionException {
		setUp();
		try {
			doFakeBranching();
			solver.propagate();
		} catch (ContradictionException e) {
			LOGGER.log(Level.SEVERE, "Fake branching raise a contradiction caused by "+e.getCause(), e);
			throw new SolverException("Fake branching raises a contradiction !");
		}
		tearDown();
		return null;
	}





}
