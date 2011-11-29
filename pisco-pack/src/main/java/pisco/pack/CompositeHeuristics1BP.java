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
package pisco.pack;

import choco.kernel.common.opres.heuristics.AbstractHeuristic;
import choco.kernel.common.opres.pack.AbstractHeuristic1BP;
import choco.kernel.common.opres.pack.BestFit1BP;
import choco.kernel.common.opres.pack.FirstFit1BP;
import gnu.trove.TIntArrayList;

public class CompositeHeuristics1BP extends AbstractHeuristic
{
  private final AbstractHeuristic1BP ff;
  private final AbstractHeuristic1BP bf;

  public CompositeHeuristics1BP(TIntArrayList sizes, int capacity)
  {
    this.ff = new FirstFit1BP();
    this.bf = new BestFit1BP();
    this.ff.setItems(sizes);
    this.bf.setItems(sizes);
    this.ff.setCapacity(capacity);
    this.bf.setCapacity(capacity);
  }

  protected int apply()
  {
    return Math.min(this.ff.apply(), this.bf.apply());
  }

  public int getIterationCount()
  {
    return 2;
  }
}

/* Location:           /home/nono/recovery/pack/
 * Qualified Name:     pisco.pack.CompositeHeuristics1BP
 * JD-Core Version:    0.6.0
 */