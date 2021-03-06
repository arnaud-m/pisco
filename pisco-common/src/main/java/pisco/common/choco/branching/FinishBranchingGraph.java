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
package pisco.common.choco.branching;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntProcedure;

import java.util.Arrays;
import java.util.BitSet;

import choco.cp.common.util.preprocessor.detector.scheduling.DisjunctiveSModel;
import choco.cp.solver.constraints.global.scheduling.precedence.ITemporalSRelation;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;



/**
 * Finish the shop branching in a backtrack-free way
 */
public class FinishBranchingGraph extends AbstractShopFakeBranching {

	protected final DisjunctiveSModel disjSMod;

	protected final ITemporalSRelation[] precedences;

	private final TIntArrayList[] outgoing;

	private final TIntArrayList[] incoming;

	private final TopologicalOrder topOrder;

	private final LongestPath longestPath;

	public FinishBranchingGraph(PreProcessCPSolver solver,
			DisjunctiveSModel disjSMod, Constraint ignoredCut) {
		super(solver, ignoredCut);
		this.disjSMod=disjSMod;
		this.precedences = disjSMod.getEdges();
		final int n = solver.getNbTaskVars();
		incoming = new TIntArrayList[n];
		outgoing = new TIntArrayList[n];
		topOrder = new TopologicalOrder(n);
		longestPath = new LongestPath(n);
		initialize();
	}



	private final void initialize() {
		final int n = solver.getNbTaskVars();
		for (int i = 0; i < n; i++) {
			incoming[i]=new TIntArrayList();
			outgoing[i]=new TIntArrayList();
			if(tasks[i].getID() != i) {
				LOGGER.severe("cant initalize Data structure: task ID Problem");
				throw new SolverException("cant initialize branching");	
			}
		}
	}

	protected final void buildGraph() {
		for (int i = 0; i < incoming.length; i++) {
			outgoing[i].resetQuick();
			incoming[i].resetQuick();
		}
		
		for (int i = 0; i < incoming.length; i++) {
			BitSet succ = disjSMod.getPrecSuccessors(i);
			for (int j = succ.nextSetBit(0); j >= 0; j = succ.nextSetBit(j + 1)) {
				outgoing[i].add(j);
				incoming[j].add(i);
			}
		}
		//Add fixed disjunctions
		for (int i = 0; i < precedences.length; i++) {
			final int lid = precedences[i].getOrigin().getID();
			final int rid = precedences[i].getDestination().getID();
			if(precedences[i].getDirection().isInstantiatedTo(1)) {
				outgoing[lid].add(rid);
				incoming[rid].add(lid);
			}else if(precedences[i].getDirection().isInstantiatedTo(0)) {
				outgoing[rid].add(lid);
				incoming[lid].add(rid);
			} else throw new SolverException("Precedence not fixed: "+precedences[i]);
		}
	}



	/**
	 * This version build the precedence graph and then schedule in a linear time with dynamic bellman algorithm.
	 * First, we compute the topological order, then the longest path from the starting task node to each concrete task.
	 * Finally, we instantiate all unscheduled starting time with the length of the computed longest path.
	 * The drawback is the need to initialize more complex data structures.
	 * The advantage is to schedule all tasks in linear time and to need a single propagation phase.
	 */
	@Override
	protected void doFakeBranching() throws ContradictionException {
		buildGraph();
		topOrder.initialize();
		topOrder.compute();
		longestPath.initialize();
		longestPath.compute();
		int makespan = Integer.MIN_VALUE;
		//		System.out.println(makespan.pretty());
		//		System.out.println(ChocoUtil.pretty(tasks));
		//		System.out.println("start"+Arrays.toString(longestPath.paths));
		//		System.out.println("top. index"+Arrays.toString(topOrder.topologicalIndex));
		//		System.out.println("top. order"+Arrays.toString(topOrder.topologicalOrder));
		for (int i = 0; i < tasks.length; i++) {
			int time = longestPath.paths[i];
			//System.out.println(tasks[i].pretty()+" - start:"+time);
			tasks[i].start().instantiate(time, null, true);
			time += tasks[i].duration().getVal();
			//System.out.println(tasks[i].pretty()+" - end:"+time);
			tasks[i].end().instantiate(time , null, true);
			if(time > makespan) {makespan = time;}
		}
		solver.getMakespan().setVal(makespan);
	}

	protected int[] getTopologicalOrder() {
		return topOrder.topologicalOrder;
	}

	private final class TopologicalOrder implements TIntProcedure {

		public final int[] topologicalOrder;

		public final int[] topologicalIndex;

		private final TIntArrayList noPred = new TIntArrayList();

		private final int[] predCounter;

		private int nbHasPred;

		public TopologicalOrder(int n) {
			topologicalOrder = new int[n];
			topologicalIndex = new int[n];
			predCounter = new int[n];
		}

		public void initialize() {
			noPred.resetQuick();
			nbHasPred = 0;
			for (int i = 0; i < predCounter.length; i++) {
				predCounter[i] = incoming[i].size();
				if(predCounter[i] == 0) {
					noPred.add(i);
				}else {
					nbHasPred++;
				}
			}			
		}


		public void compute() {
			//main loop
			int idx = 0;
			while( nbHasPred > 0) {
				final int task = noPred.remove(0);
				outgoing[task].forEach(this);
				topologicalIndex[task] = idx;
				topologicalOrder[idx++] = task;
			}
			//add remaining tasks
			for (int i = 0; i < noPred.size(); i++) {
				final int task = noPred.getQuick(i);
				topologicalIndex[task] = idx;
				topologicalOrder[idx++] = task;
			}
		}

		@Override
		public boolean execute(int arg0) {
			//decrement predecessors counter
			predCounter[arg0]--;
			//add the task to the list of tasks without predecessors
			if(predCounter[arg0] == 0) {
				noPred.add(arg0);
				nbHasPred--;				
			}
			return true;
		}

	}



	private final class LongestPath implements TIntProcedure {

		private int taskIndex;


		public final int[] paths;

		public final int[] predecessors;

		public LongestPath(int n) {
			paths = new int[n];
			predecessors = new int[n];
		}

		public void initialize() {
			Arrays.fill(paths, 0);
			Arrays.fill(predecessors, -1);	
		}

		public void compute() {
			for (int i = 0; i < paths.length; i++) {
				taskIndex = topOrder.topologicalOrder[i]; 
				incoming[taskIndex].forEach(this);
			}
		}


		@Override
		public boolean execute(int arg0) {
			final int l = paths[arg0] + tasks[arg0].duration().getVal();
			if( l > paths[taskIndex]) {
				paths[taskIndex] = l;
				predecessors[taskIndex] = arg0;
			}
			return true;
		}

	}		

}

