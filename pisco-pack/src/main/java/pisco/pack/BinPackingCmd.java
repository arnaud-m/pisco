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

import choco.kernel.solver.Configuration;
import cli.AbstractBenchmarkCmd;
import java.io.File;
import java.util.Random;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import parser.instances.AbstractInstanceModel;
import parser.instances.ResolutionStatus;
import parser.instances.checker.SCheckFactory;

public class BinPackingCmd extends AbstractBenchmarkCmd
{

  @Option(name="-l", aliases={"--light"}, usage="light filtering model")
  protected boolean lightModel;

  public BinPackingCmd()
  {
    super(new BinPackingSettings());
  }

  protected AbstractInstanceModel createInstance()
  {
    return new BinPackingModel(this.settings);
  }

  protected void checkData() throws CmdLineException
  {
    super.checkData();
    this.seeder = new Random(this.seed);

    if (this.lightModel) this.settings.putTrue("tools.cp.model.light");

    SCheckFactory.load(new String[] { 
      "/instances.properties" });
  }

  public boolean execute(File file)
  {
    this.instance.solveFile(file);
    return this.instance.getStatus().isValidWithOptimize();
  }

  public static void main(String[] args) {
    BinPackingCmd cmd = new BinPackingCmd();
    if (args.length == 0)
      cmd.help();
    else
      cmd.doMain(args);
  }
}

/* Location:           /home/nono/recovery/pack/
 * Qualified Name:     pisco.pack.BinPackingCmd
 * JD-Core Version:    0.6.0
 */