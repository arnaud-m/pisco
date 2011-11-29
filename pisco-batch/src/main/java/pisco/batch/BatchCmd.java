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
/**
 * 
 */
package pisco.batch;

import java.io.File;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;

import parser.instances.AbstractMinimizeModel;
import parser.instances.checker.SCheckFactory;
import pisco.batch.BatchSettings.PropagagationLevel;
import pisco.batch.BatchSettings.ValSel;
import pisco.batch.BatchSettings.VarSel;
import cli.AbstractBenchmarkCmd;

/**
 * @author Arnaud Malapert</br> 
 * @since 18 avr. 2009 version 2.1.0</br>
 * @version 2.1.0</br>
 */
public class BatchCmd extends AbstractBenchmarkCmd {

		
	public static enum Problem {
		LMAX, FLOW, WFLOW
	}
	
	@Option(name="-pb",aliases={"--problem"},usage="set the p-batch problem optimality criterion : Max. Lateness, Flowtime, Weighted flowtime",required=true)
	protected Problem problemType;
	
	@Option(name="-var",aliases={"--varsel"},usage="set the variable selection heuristics")
	protected VarSel varSelector;
	
	@Option(name="-val",aliases={"--valsel"},usage="set the value selection heuristics")
	protected ValSel valSelector;
	
	@Option(name="-single",aliases={"--singleMachine"},usage="set the propagation level of the single machine relaxation")
	protected PropagagationLevel singleLevel;
	
	@Option(name="-parallel",aliases={"--parallelMachines"},usage="set the propagation level of the parallel machines relaxation")
	protected PropagagationLevel parallelLevel;
	
	// TODO - Add command line options for single and prallel relaxation - created 4 nov. 2011 by Arnaud Malapert
	/**
	 * @param settings
	 */
	public BatchCmd() {
		super(new BatchSettings());
	}
	
	@Override
	protected void checkData() throws CmdLineException {
		super.checkData();
		if( varSelector != null) settings.putEnum(BatchSettings.VAR_SEL, varSelector);
		if( valSelector != null) settings.putEnum(BatchSettings.VAL_SEL, valSelector);
		if(singleLevel != null) settings.putEnum(BatchSettings.SINGLE_PROPAGATION, singleLevel);
		if(parallelLevel != null) settings.putEnum(BatchSettings.PARALLEL_PROPAGATION, parallelLevel);
		// DONE 15 nov. 2011 - depends on the problem type - created 10 nov. 2011 by Arnaud Malapert
		if(problemType == Problem.LMAX) {
			SCheckFactory.load("/batch-processing-cplex.properties");
		}

	}

	@Override
	public AbstractMinimizeModel createInstance() {
		switch (problemType) {
		case LMAX:return new PBatchLmax(settings);
		case FLOW:return new PBatchFlow(settings);
		case WFLOW:return new PBatchWFlow(settings);
		default:
			return null;
		}
	}
	
	@Override
	public boolean execute(File file) {
		instance.solveFile(file);
		return instance.getStatus().isValidWithOptimize();
	}

	public static void main( String[] args )   {
		final BatchCmd cmd = new BatchCmd();
		if(args.length==0) {cmd.help();}
		else {cmd.doMain(args);}
	}
}
