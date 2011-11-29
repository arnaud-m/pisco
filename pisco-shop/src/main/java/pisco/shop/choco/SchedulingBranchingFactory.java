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
package pisco.shop.choco;

import choco.cp.common.util.preprocessor.detector.scheduling.DisjunctiveSModel;
import choco.cp.solver.search.integer.branching.AssignOrForbidIntVarValPair;
import choco.cp.solver.search.task.OrderingValSelector;
import choco.cp.solver.search.task.ordering.CentroidOrdering;
import choco.cp.solver.search.task.ordering.LexOrdering;
import choco.cp.solver.search.task.profile.ProfileSelector;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.global.scheduling.IResource;

public final class SchedulingBranchingFactory {

	private SchedulingBranchingFactory() {
		super();
	}
	
	//*****************************************************************//
	//*******************  Profile Heuristics **********************//
	//***************************************************************//

	public static AssignOrForbidIntVarValPair profile(Solver solver,  DisjunctiveSModel disjSModel) {
		return profile(solver, disjSModel, new LexOrdering());
	}
	
	public static AssignOrForbidIntVarValPair profile(Solver solver, DisjunctiveSModel disjSModel, long seed) {
		return profile(solver, disjSModel, new CentroidOrdering(seed));
	}
	
	public static AssignOrForbidIntVarValPair profile(Solver solver, DisjunctiveSModel disjSModel, OrderingValSelector valSel) {
		return new AssignOrForbidIntVarValPair(new ProfileSelector(solver, disjSModel, valSel));
	}
	
	
	public static AssignOrForbidIntVarValPair profile(Solver solver, IResource<?>[] resources, DisjunctiveSModel disjSModel) {
		return profile(solver, resources, disjSModel, new LexOrdering());
	}
	public static AssignOrForbidIntVarValPair profile(Solver solver, IResource<?>[] resources, DisjunctiveSModel disjSModel, long seed) {
		return profile(solver, resources, disjSModel, new CentroidOrdering(seed));
	}
	
	public static AssignOrForbidIntVarValPair profile(Solver solver, IResource<?>[] resources, DisjunctiveSModel disjSModel, OrderingValSelector valSel) {
		return new AssignOrForbidIntVarValPair(new ProfileSelector(solver, resources, disjSModel, valSel));
	}

}
