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
package pisco.shop;

import java.io.File;
import java.util.Random;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;

import parser.instances.AbstractMinimizeModel;
import parser.instances.BasicSettings;
import parser.instances.checker.SCheckFactory;
import pisco.common.SchedulingBranchingFactory;
import pisco.common.SchedulingBranchingFactory.Branching;
import pisco.shop.ChocoshopSettings.Heuristics;
import choco.kernel.solver.Configuration;
import cli.AbstractBenchmarkCmd;

public class ShopCmd extends AbstractBenchmarkCmd {

	public enum ShopType {
		OSP("Open-Shop Problem"),
		JSP("Job-Shop Problem"),
		FSP("Flow-Shop Problem"),
		AFP("Airland Flowtime Problem"),
		AWFP("Airland Weighted Flowtime Problem"),
		ALP("Airland Lmax Problem"),
		AP("Airland Problem");
		
		private final String name;

		private ShopType(String name) {
			this.name = name;
		}

		public final String getName() {
			return name;
		}
	}


	@Option(name="-t",aliases={"--type"},usage="type of shop problems", required=true)
	protected ShopType type;
	
	@Option(name="-n",aliases={"--nbSolve"},usage="number of execution per instance (>0)")
	protected int nbSolve = 1;
	
	
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


	/**
	 * the heuristic which computes the initial upper bound on the makespan.
	 */
	@Option(name="-h",aliases={"--heuristic"},usage="select the heuristics which computes the initial upper bound")
	protected Heuristics heuristics;
	
	private Random seeder;
	
	public ShopCmd() {
		super(new ChocoshopSettings());
	}



	public ChocoshopSettings getChocoshopSettings() {
		return (ChocoshopSettings) settings;
	}

	@Override
	protected void checkData() throws CmdLineException {
		super.checkData();
		seeder =  new Random(seed);
		final ChocoshopSettings set = getChocoshopSettings();
		if(branching != null) set.putEnum(ChocoshopSettings.BRANCHING_TYPE, branching);
		if( heuristics != null) set.putEnum(ChocoshopSettings.HEURISTICS_TYPE, heuristics);
		if(lightModel != null) set.putTrue(BasicSettings.LIGHT_MODEL);
		//load status checkers
		switch (type) {
		case OSP: {
			SCheckFactory.load("/open-shop-tai.properties",
					"/open-shop-gp.properties",
					"/open-shop-j.properties");
			break;
		}
		case FSP: SCheckFactory.load("/flow-shop-tai.properties");break;
		case JSP: SCheckFactory.load("/job-shop-tai.properties");break;
		default:
			break;
		}
	}



	@Override
	public AbstractMinimizeModel createInstance() {
		final ChocoshopSettings cs =getChocoshopSettings();
		switch (type) {
		case FSP: return new FlowShopProblem(cs);
		case JSP: return new JobShopProblem(cs);
		case OSP: return new OpenShopProblem(cs);
		case ALP: return new AirLandLmax(cs);
		case AFP: return new AirLandFlow(cs);
		case AWFP: return new AirLandWFlow(cs);
		case AP: return new AirLandProblem(cs);
		default : 	LOGGER.severe("unknown shop problem.");return null;
		}
	}
	
	
	@Override
	public boolean execute(File file) {
		for (int i = 0; i < nbSolve; i++) {
			instance.solveFile(file);
			if( ! instance.getStatus().isValidWithOptimize())  return false;
			instance.getConfiguration().putLong(Configuration.RANDOM_SEED, seeder.nextInt()); //renew seed
		}
		return true;
	}

	
	public static void main( String[] args )   {
		final ShopCmd cmd = new ShopCmd();
		if(args.length==0) {cmd.help();}
		else {cmd.doMain(args);}
	}
}
