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
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.branch.Extension;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.variables.AbstractVar;
import choco.kernel.solver.variables.integer.IntDomainVar;
import java.util.ArrayList;

public class ItemSymBreakDecreasing extends BinSymBreakDecreasing
{
  private ArrayList<IntDomainVar> nextEqualSizedItems = new ArrayList();

  private static final int ABSTRACTVAR_EXTENSION = AbstractVar.getAbstractVarExtensionNumber("choco.cp.cpsolver.search.integer.branching.ItemSymBreakDecreasing");

  public ItemSymBreakDecreasing(Solver s, ValSelector<IntDomainVar> valSelector, PackSConstraint pack) {
    super(s, valSelector, pack);
    IntDomainVar[] bins = pack.getBins();
    for (int i = 0; i < bins.length; i++) {
      bins[i].addExtension(ABSTRACTVAR_EXTENSION);
      bins[i].getExtension(ABSTRACTVAR_EXTENSION).set(i);
    }
  }

  protected void findNextEqualSizedItems(IntDomainVar bin)
  {
    this.nextEqualSizedItems.clear();
    int idx = bin.getExtension(ABSTRACTVAR_EXTENSION).get() + 1;
    int n = this.pack.getNbItems();
    int[] sizes = this.pack.getSizes();
    IntDomainVar[] bins = this.pack.getBins();
    while ((idx < n) && (sizes[idx] == sizes[(idx - 1)]))
      this.nextEqualSizedItems.add(bins[(idx++)]);
  }

  protected void removeFromBin(IntDomainVar bin, int b)
    throws ContradictionException
  {
    super.removeFromBin(bin, b);
    for (IntDomainVar bvar : this.nextEqualSizedItems)
      bvar.remVal(b);
  }

  protected void removeFromEmptyBins(IntDomainVar bin)
    throws ContradictionException
  {
    findNextEqualSizedItems(bin);
    super.removeFromEmptyBins(bin);
  }

  protected void removeFromEquivalentBins(IntDomainVar bin, int bup) throws ContradictionException
  {
    findNextEqualSizedItems(bin);
    super.removeFromEquivalentBins(bin, bup);
  }
}

