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

import java.io.File;
import java.io.IOException;

import parser.instances.BasicSettings;
import choco.kernel.solver.Configuration;

public final class BinPackingSettings extends BasicSettings
{
  private static final long serialVersionUID = 2602077886748398026L;

  @Configuration.Default("CD")
  public static final String BRANCHING_TYPE = "tools.branching.type";

  @Configuration.Default("FF")
  public static final String BRANCHING_VALSEL = "tools.branching.valsel";

  @Configuration.Default("false")
  public static final String SYMBREAK_ORDER = "tools.model.symmetry.order";

  @Configuration.Default("false")
  public static final String SYMBREAK_COMPLETION = "tools.model.symmetry.completion";

  public static final Branching getBranching(Configuration conf)
  {
	  final Branching br = conf.readEnum(BRANCHING_TYPE, Branching.class);
	  return br;
  }

  public static final ValSel getValSel(Configuration conf) {
	  final ValSel valsel = conf.readEnum(BRANCHING_VALSEL, ValSel.class);
	  return valsel;
  }

  public static String getBranchingMsg(Configuration conf)
  {
    StringBuilder b = new StringBuilder();
    b.append(getBranching(conf)).append(" BRANCHING    ");
    b.append(getValSel(conf)).append(" VALSEL    ");
    return new String(b);
  }

  public static String getSymBreakMsg(Configuration conf) {
    StringBuilder b = new StringBuilder();
    if (conf.readBoolean("tools.model.symmetry.order")) b.append("ORDERING    ");
    if (conf.readBoolean("tools.model.symmetry.completion")) b.append("COMPLETION");
    return new String(b);
  }

  public static void main(String[] args) throws IOException
  {
    BinPackingSettings s = new BinPackingSettings();
    s.storeDefault(new File("/tmp/bin-packing.properties"), null);
  }

  public static enum Branching
  {
    CD, 
    CDB, 
    CDBI;
  }

  public static enum ValSel {
    RAND, 
    FF, 
    BF, 
    WF, 
    DBF, 
    DWF;
  }
  
}

/* Location:           /home/nono/recovery/pack/
 * Qualified Name:     pisco.pack.BinPackingSettings
 * JD-Core Version:    0.6.0
 */