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
package pisco.shop.heuristics;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import pisco.shop.ChocoshopSettings;
import pisco.shop.GenericShopProblem;
import pisco.shop.ChocoshopSettings.Heuristics;


import choco.kernel.common.opres.heuristics.AbstractRandomizedHeuristic;




public class CrashHeuristics extends AbstractRandomizedHeuristic {

	protected final static Comparator<BTask> LPT_CMP = new DurationComp();

	protected final static Comparator<BTask> SPT_CMP = Collections.reverseOrder(LPT_CMP);

	protected final static int LPT = 0;

	protected final static int SPT = 1;

	protected final GenericShopProblem shop;

	//the set of task of the problem
	protected BSolution osol;

	protected IBPrecFactory factory;

	public CrashHeuristics(final GenericShopProblem shop) {
		super();
		this.shop = shop;
		osol = new BSolution(shop);
	}


	public final void setFactory(IBPrecFactory factory) {
		if(this.factory != null && this.factory.equals(factory)) {
			LOGGER.finest("factory is already set.");
		}else {
			this.factory = factory;
			osol.initAllPrec(factory);
			LOGGER.finest("set precedence factory.");

		}
	}

	@Override
	public int getLowerBound() {
		return shop.getComputedLowerBound();
	}



	@Override
	public void execute() {
		switch ( ChocoshopSettings.getHeuristics( shop.getConfiguration()) ) {
		case LPT: applyLPT();break;
		case SPT: applySPT();break;
		case CROSH: {
			apply(new Random(shop.getSeed()));break;
		}
		default: {
			LOGGER.warning("CRASH heuristics [FAIL]");
			this.reset();
			osol.reinitSol();
		}
		}
	}



	public final BSolution getSolution() {
		return osol;
	}


	public final String getDescription() {
		return "Shop heuristics (LPT, SPT, CROSH): "+this.getObjectiveValue()+" in "+getTimeCount()+"s and "+getIterationCount()+" iterations";
	}


	public final void apply(Heuristics heuristics, int timeLimit, int iterationLimit) {
		switch (heuristics) {
		case LPT: applyLPT();break;
		case SPT: applySPT();break;
		case CROSH: {
			setTimeLimit(timeLimit);
			setIterationLimit(iterationLimit);
			apply(new Random(shop.getSeed()));break;
		}
		default: {
			LOGGER.log(Level.WARNING, "unknown heuristics {0} [FAIL]",heuristics);
			this.reset();
			osol.reinitSol();
			break;}
		}
	}


	public final  int applyLPT() {
		return applySingleIteration(LPT,0);
	}

	public final void applySPT() {
		applySingleIteration(SPT,0);
	}

	@Override
	protected final int apply(int iteration, int bestsol, int seed) {
		osol.reinitSol();
		final List<BTask> tlist = osol.createBTaskList();
		switch (iteration) {
		case LPT: Collections.sort(tlist,LPT_CMP);break;
		case SPT: Collections.sort(tlist,SPT_CMP);break;
		default	: Collections.shuffle(tlist, new Random(seed));	break;
		}
		apply(tlist, bestsol);
		return osol.obj;
	}


	public final void apply(final List<BTask> tlist,final int bestsol) {
		int[] eom = new int[shop.nbMachines];
		int[] eoj = new int[shop.nbJobs];
		while (!tlist.isEmpty()) {
			int tmin = Integer.MAX_VALUE;
			final LinkedList<BTask> firstAvailableTasks = new LinkedList<BTask>();
			BTask criticalTask = null;
			for (BTask task : tlist) {  // compute D(tmin)
				if (task.isNowPrecedenceFree()) {
					final int t = Math.max(eom[task.machine], eoj[task.job]);
					if (t < tmin) {
						tmin = t;
						firstAvailableTasks.clear();
						firstAvailableTasks.add(task);
					} else if (t == tmin) {
						firstAvailableTasks.add(task);
					}
				}
			}
			criticalTask = firstAvailableTasks.getFirst();
			osol.assignTask(criticalTask, tmin);
			if(osol.obj >= bestsol) {
				//stop searching
				osol.obj = Integer.MAX_VALUE;
				break;
			}
			criticalTask.updateBefore();
			eom[criticalTask.machine] = tmin + criticalTask.getDuration();
			eoj[criticalTask.job] = tmin + criticalTask.getDuration();
			tlist.remove(criticalTask);
		}
	}

}


/**
 * sort task by decreasing duration
 * @author Arnaud Malapert
 *
 */
class DurationComp implements Comparator<BTask> {
	public int compare(final BTask o, final BTask o1) {
		final int bta1 = o.getDuration();
		final int bta2 = o1.getDuration();
		if (bta1 > bta2) {
			return -1;
		} else if (bta1 == bta2) {
			return 0;
		} else {
			return 1;
		}

	}
}
