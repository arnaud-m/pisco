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
package pisco.pack.choco.branching;

import choco.cp.solver.constraints.global.pack.PackSConstraint;
import choco.cp.solver.search.integer.branching.AssignVar;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.propagation.PropagationEngine;
import choco.kernel.solver.search.AbstractGlobalSearchStrategy;
import choco.kernel.solver.search.IntBranchingDecision;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.variables.integer.IntDomain;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;

public class BinSymBreakDecreasing extends AssignVar
{
  public final PackSConstraint pack;

  public BinSymBreakDecreasing(Solver s, ValSelector<IntDomainVar> valSelector, PackSConstraint pack)
  {
    super(new StaticVarOrder(s, pack.getBins()), valSelector);
    this.pack = pack;
  }

  protected final void fail() throws ContradictionException {
    getManager().solver.getPropagationEngine().raiseContradiction(this);
  }

  protected void removeFromBin(IntDomainVar bin, int b) throws ContradictionException {
    bin.remVal(b);
  }

  protected void removeFromEmptyBins(IntDomainVar bin) throws ContradictionException {
    DisposableIntIterator iter = bin.getDomain().getIterator();
    try {
      while (iter.hasNext()) {
        int b = iter.next();
        if (this.pack.isEmpty(b))
          removeFromBin(bin, b);
      }
    }
    finally {
      iter.dispose();
    }
  }

  protected void removeFromEquivalentBins(IntDomainVar bin, int bup) throws ContradictionException {
    DisposableIntIterator iter = bin.getDomain().getIterator();
    int space = this.pack.getRemainingSpace(bup);
    try {
      while (iter.hasNext()) {
        int b = iter.next();
        if (this.pack.getRemainingSpace(b) == space)
          removeFromBin(bin, b);
      }
    }
    finally {
      iter.dispose();
    }
  }

  public final void goUpBranch(IntBranchingDecision decision) throws ContradictionException
  {
    super.goUpBranch(decision);

    int reuseVal = decision.getBranchingValue();

    if (this.pack.svars[reuseVal].isInstantiated())
    {
      fail();
    } else if (this.pack.isEmpty(reuseVal))
    {
      removeFromEmptyBins(decision.getBranchingIntVar());
    }
    else
      removeFromEquivalentBins(decision.getBranchingIntVar(), reuseVal);
  }
}

