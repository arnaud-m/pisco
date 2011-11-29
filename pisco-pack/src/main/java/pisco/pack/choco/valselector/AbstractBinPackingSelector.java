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
package pisco.pack.choco.valselector;

import choco.cp.solver.constraints.global.pack.PackSConstraint;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.variables.integer.IntDomain;
import choco.kernel.solver.variables.integer.IntDomainVar;

public abstract class AbstractBinPackingSelector
  implements ValSelector<IntDomainVar>
{
  public final PackSConstraint pack;

  public AbstractBinPackingSelector(PackSConstraint cstr)
  {
    this.pack = cstr;
  }

  protected abstract int getFitness(int paramInt);

  public int getBestVal(IntDomainVar x) {
    DisposableIntIterator iter = x.getDomain().getIterator();
    int bin = iter.next();
    int bestFitness = getFitness(bin);
    try {
      while (iter.hasNext()) {
        int b = iter.next();
        int fitness = getFitness(b);
        if (fitness < bestFitness)
        {
          bestFitness = fitness;
          bin = b;
        }
        if (this.pack.isEmpty(bin)) {
          break;
        }
      }
    }
    finally
    {
      iter.dispose();
    }
    return bin;
  }
}

/* Location:           /home/nono/recovery/pack/
 * Qualified Name:     pisco.pack.choco.valselector.AbstractBinPackingSelector
 * JD-Core Version:    0.6.0
 */