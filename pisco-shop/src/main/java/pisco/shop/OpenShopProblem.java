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


import static choco.Choco.leq;
import static choco.Choco.minus;
import static choco.Choco.mult;

import java.awt.Point;

import parser.instances.BasicSettings;
import pisco.shop.choco.constraints.ShopSymBreakManager;
import pisco.shop.heuristics.ICrashLearning;
import pisco.shop.parsers.OpenShopParser;
import choco.Choco;
import choco.cp.model.CPModel;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;

/**
 * @author Arnaud Malapert
 */
public class OpenShopProblem extends GenericShopProblem {


	public final static ICrashLearning HLEARNING_OS = new HeuristicsOS();

	public static enum CutOS {
		OFF, LST1, LST2, PREC
	}
	
	/**
	 * @param input
	 */
	public OpenShopProblem(BasicSettings settings) {
		super(new OpenShopParser(), settings);
	}


	@Override
	protected void initCrashLearning() {
		this.crashLearning = HLEARNING_OS;
	}


	protected Point getLongestTask() {
		Point t = new Point();
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < nbMachines; i++) {
			for (int j = 0; j < nbJobs; j++) {
				if (processingTimes[i][j] > max) {
					max = processingTimes[i][j];
					t.x = i;
					t.y = j;
				}
			}
		}
		return t;
	}

	protected Constraint getStartingTimeCut1() {
		Point p = getLongestTask();
		 // 2 * start_i <= makespan - ( duration_i - 1)
		return leq(mult(2, tasks[p.x][p.y].start()), minus(this.makespan, processingTimes[p.x][p.y] - 1));
	}

	protected Constraint getStartingTimeCut2() {
		Point p = getLongestTask();
		return new ComponentConstraint( ShopSymBreakManager.class, null, new Variable[] { tasks[p.x][p.y] , makespan});
	}


	protected Constraint getPrecedenceCut() {
		int max = Integer.MIN_VALUE;
		Constraint p = null;
		for (int i = 0; i < nbMachines; i++) {
			for (int j = 0; j < nbJobs; j++) {
				int p1 = processingTimes[i][j];
				//same job
				for (int k = 0; k < nbMachines; k++) {
					int p2 = processingTimes[i][k];
					if (k != j & p1 >= p2 && p1 + p2 > max) {
						max = p1 + p2;
						p = Choco.startsAfterEnd(tasks[i][k], tasks[i][j]);
					}
				}
				//same machine
				for (int k = 0; k < nbJobs; k++) {
					int p2 = processingTimes[k][j];
					if (k != i && p1 >= p2 && p1 + p2 > max) {
						max = p1 + p2;
						p = Choco.startsAfterEnd(tasks[k][j], tasks[i][j]);
					}
				}
			}
		}
		return p;
	}

	protected Constraint getInitialCut() {
		switch ( ChocoshopSettings.getInitialCut(defaultConf)) {
		case LST1:
			//getShopSettings().setFinishBranching(FinishBranching.NAIVE);
			return getStartingTimeCut1();
		case LST2:
			return getStartingTimeCut2();
		case PREC:
			return getPrecedenceCut();
		default:
			LOGGER.warning("no initial cut !");
		return null;
		}		
	}
	
	/**
	 * @see pisco.shop.problem.AbstractChocoProblem#buildModel()
	 */
	@Override
	public Model buildModel() {
		CPModel model = (CPModel) super.buildModel();
		addJobResources(model);
		addMachineResources(model);
		constraintCut = getInitialCut();
		if(constraintCut != null) model.addConstraint(constraintCut);
		return model;
	}


	private static class HeuristicsOS implements ICrashLearning {


		@Override
		public int getLearnedIterationLimit(GenericShopProblem shop) {
			if (shop.nbJobs <= 6) {
				switch (shop.nbJobs) {
				case 2:
				case 3:
					return 5;
				case 4:
					return 25;
				case 5:
					return 50;
				case 6:
					return 1000;
				default:
					LOGGER.severe("internal heuristics configuration error");
				return 1000;
				}
			} else if (shop.nbJobs < 10) {
				return 10000;
			} else {
				return 25000;
			}
		}

		@Override
		public int getLearnedTimeLimit(GenericShopProblem shop) {
			return 20;
		}


	}


}
