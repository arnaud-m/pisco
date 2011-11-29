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

import java.util.List;

import pisco.batch.AbstractBatchingProblem;
import pisco.batch.BatchSettings;
import pisco.batch.BatchSettings.PropagagationLevel;
import choco.cp.model.managers.IntConstraintManager;
import choco.cp.solver.CPSolver;
import choco.kernel.memory.IEnvironment;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

public class BatchManager extends IntConstraintManager {

	public BatchManager() {}

	@Override
	public SConstraint makeConstraint(Solver solver, IntegerVariable[] variables,
			Object parameters, List<String> options) {
		if (solver instanceof CPSolver) {
			if (parameters instanceof AbstractBatchingProblem) {
				final AbstractBatchingProblem problem= (AbstractBatchingProblem) parameters;
				final IntDomainVar[] vars = solver.getVar(variables);
				final IEnvironment env = solver.getEnvironment();
				return BatchSettings.getSinglePropagationLevel(problem) == PropagagationLevel.DECOMP && 
						BatchSettings.getParallelPropagationLevel(problem) == PropagagationLevel.DECOMP ?
							new PBatchSConstraint(env, vars,problem) :
								new PBatchRelaxSConstraint(env, vars, problem);
			}
		}
		return null;
	}

	@Override
	public int[] getFavoriteDomains(List<String> options) {
		return getBCFavoriteIntDomains();
	}
}
