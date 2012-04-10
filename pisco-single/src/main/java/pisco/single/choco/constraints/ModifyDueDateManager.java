/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
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

package pisco.single.choco.constraints;

import java.util.List;

import choco.cp.model.managers.MixedConstraintManager;
import choco.cp.solver.CPSolver;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;



/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 11 août 2008
 * Time: 16:49:22
 */
public final class ModifyDueDateManager extends MixedConstraintManager {


	protected final int getConstantValue( Solver s, Variable var) {
		final IntDomainVar v = s.getVar( (IntegerVariable) var);
		if( v.isInstantiated()) {
			return v.getVal();
		}else {
			throw new SolverException(var+" should be constant");
		}
	}
	@Override
	public final SConstraint makeConstraint(Solver solver, Variable[] variables,
			Object parameters, List<String> options) {
		if(solver instanceof CPSolver){
			final CPSolver s = (CPSolver) solver;
			final IntDomainVar x1 = solver.getVar((IntegerVariable) variables[0]);
			final int k1 = getConstantValue(s, variables[1]);
			final IntDomainVar x2 = solver.getVar((IntegerVariable) variables[2]);
			final int k2 = getConstantValue(s, variables[3]);
			final IntDomainVar dir = solver.getVar((IntegerVariable)variables[4]);
			if(dir.getDomain().isBoolean()) {
				return new ModifyDueDateConstraint(x1, k1, x2, k2, dir);
			} 
		}
		return fail("Modify Due Date");
	}

	@Override
	public int[] getFavoriteDomains(List<String> options) {
		return getBCFavoriteIntDomains();
	}

}
