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
package pisco.shop;

import java.io.File;

import parser.absconparseur.tools.UnsupportedConstraintException;
import parser.instances.BasicSettings;
import pisco.shop.heuristics.BPrecedence;
import pisco.shop.heuristics.BTask;
import pisco.shop.heuristics.IBPrecFactory;
import pisco.shop.parsers.JobShopParser;
import choco.Choco;
import choco.cp.model.CPModel;
import choco.kernel.model.Model;
import choco.kernel.model.variables.scheduling.TaskVariable;


class BprecFactoryJS  implements IBPrecFactory {

	class BPrecJS extends BPrecedence {

		public BPrecJS(BTask t1, BTask t2,boolean satisfied) {
			super(t1, t2);
		}

		@Override
		public boolean isChecked() {
			return isSameJob();
		}

		@Override
		public boolean isSatisfied() {
			return isSameJob() && t1.machine<t2.machine;
		}
	}

	@Override
	public BPrecedence makeBPrecedence(BTask t1, BTask t2) {
		return new BPrecJS(t1,t2,true);
	}
}




/**
 * @author Arnaud Malapert
 *
 */
public class JobShopProblem extends GenericShopProblem implements IBPrecFactory {

	/**
	 * The operation (machine , job) is the postions[job][machine]-th operation of job.
	 */
	protected int[][] positions;

	/**
	 * The i-th operation of job j is processed on machine machines[i][j].
	 */
	protected int[][] taimachines;


	public JobShopProblem(BasicSettings settings) {
		super(new JobShopParser(), settings);
	}

	
	
	
	@Override
	public void initialize() {
		super.initialize();
		positions = null;
		taimachines = null;
	}




	@Override
	public void load(File fichier) throws UnsupportedConstraintException {
		super.load(fichier);
		positions= ( (JobShopParser) parser).positions;
		taimachines = ( (JobShopParser) parser).taimachines;
		getCrashHeuristics().setFactory(this);
	}



	@Override
	public Model buildModel() {
		CPModel model = (CPModel) super.buildModel();
		addMachineResources(model);
		//operations of a job are ordered on machines
		for (int j = 0; j < nbJobs; j++) {
			for (int m = 1; m < nbMachines; m++) {
				TaskVariable t1 = tasks[taimachines[j][m-1]-1][j];
				TaskVariable t2 = tasks[taimachines[j][m]-1][j];
				model.addConstraint(Choco.startsAfterEnd(t2,t1));
			}
		}
		return model;
	}

	@Override
	public String solutionToString() {
		StringBuilder b = new StringBuilder(super.solutionToString());
		if(b.length()>0) {
			b.append("\n#Positions\n");
			for (int i = 0; i < nbMachines; i++) {
				for (int j = 0; j < nbJobs; j++) {
					b.append(positions[i][j]).append(' ');
				}
				b.append('\n');
			}
		}
		return b.toString();
	}


	class BPrecJS extends BPrecedence {

		public BPrecJS(BTask t1, BTask t2) {
			super(t1, t2);
		}

		@Override
		public boolean isChecked() {
			return isSameJob();
		}

		@Override
		public boolean isSatisfied() {
			return isSameJob() &&  positions[t1.job][t1.machine]<positions[t2.job][t2.machine];
		}
	}

	@Override
	public BPrecedence makeBPrecedence(BTask t1, BTask t2) {
		return new BPrecJS(t1,t2);
	}

}
