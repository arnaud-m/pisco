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
package pisco.single;

import java.io.File;
import java.util.Random;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;

import parser.instances.AbstractMinimizeModel;
import parser.instances.BasicSettings;
import parser.instances.checker.SCheckFactory;
import pisco.common.DisjunctiveSettings;
import pisco.common.SchedulingBranchingFactory;
import pisco.single.parsers.AirlandParser;
import pisco.single.parsers.D1MachineParser;
import pisco.single.parsers.W1MachineParser;
import choco.kernel.solver.Configuration;
import cli.AbstractBenchmarkCmd;

public class SingleMachineCmd extends AbstractBenchmarkCmd {

	public enum SingleType {
		FP("Flowtime Problem"),
		WP("Weighted Flowtime Problem"),
		LP("Lmax Problem"),
		AP("Airland Problem");

		private final String name;

		private SingleType(String name) {
			this.name = name;
		}

		public final String getName() {
			return name;
		}
	}


	@Option(name="-t",aliases={"--type"},usage="type of single machine problem", required=true)
	protected SingleType type;

	@Option(name="-air",aliases={"--airland"},usage="always use airland instance format")
	protected boolean useAirlandParser;;

	/**
	 * the branching strategy
	 */
	@Option(name="-b",aliases={"--branching"},usage="set the branching strategy")
	protected SchedulingBranchingFactory.Branching branching;

	/**
	 * the branching strategy
	 */
	@Option(name="-l",aliases={"--light"},usage="activate global constraints decomposition")
	protected Boolean lightModel;



	private Random seeder;

	public SingleMachineCmd() {
		super(new SingleMachineSettings());
	}



	public SingleMachineSettings getSingleMachineSettings() {
		return (SingleMachineSettings) settings;
	}

	@Override
	protected void checkData() throws CmdLineException {
		super.checkData();
		seeder =  new Random(seed);
		final SingleMachineSettings set = getSingleMachineSettings();
		if(branching != null) set.putEnum(DisjunctiveSettings.BRANCHING_TYPE, branching);
		if(lightModel != null) set.putTrue(BasicSettings.LIGHT_MODEL);
		//load status checkers
				switch (type) {
				case LP: {
					SCheckFactory.load("/single-machine-lmax.properties");
					break;
				}
//				case FSP: SCheckFactory.load("/flow-shop-tai.properties");break;
//				case JSP: SCheckFactory.load("/job-shop-tai.properties");break;
				default:
					break;
				}
	}

	

	@Override
	public AbstractMinimizeModel createInstance() {
		final BasicSettings cs =getSingleMachineSettings();
				switch (type) {
				case LP: return new SingleMachineLmax( cs, useAirlandParser ? new AirlandParser() : new D1MachineParser());
				case FP: return new SingleMachineFlow(cs, useAirlandParser ? new AirlandParser() : new W1MachineParser());
				case WP: return new SingleMachineWFlow(cs, useAirlandParser ? new AirlandParser() : new W1MachineParser());
				case AP: return new AirLandProblem(cs);
				default : 	LOGGER.severe("unknown shop problem.");return null;
				}
	}


	@Override
	public boolean execute(File file) {
		instance.solveFile(file);
		if( ! instance.getStatus().isValidWithOptimize())  return false;
		instance.getConfiguration().putLong(Configuration.RANDOM_SEED, seeder.nextInt()); //renew seed
		return true;
	}


	public static void main( String[] args )   {
		final SingleMachineCmd cmd = new SingleMachineCmd();
		if(args.length==0) {cmd.help();}
		else {cmd.doMain(args);}
	}
}

