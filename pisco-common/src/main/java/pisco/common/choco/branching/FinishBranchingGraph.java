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
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import pisco.common.DisjunctiveSettings;
import choco.cp.common.util.preprocessor.detector.scheduling.DisjunctiveSModel;
import choco.cp.solver.constraints.global.scheduling.precedence.ITemporalSRelation;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;


/**
 * Finish the shop branching in a backtrack-free way
 */
public final class FinishBranchingGraph extends AbstractShopFakeBranching {

	protected final DisjunctiveSModel disjSMod;

	protected final ITemporalSRelation[] precedences;

	private final TIntArrayList[] outgoing;

	private final TIntArrayList[] incoming;

	protected final TopologicalOrder topOrder;

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
		for (int i = 0; i < solver.getNbTaskVars(); i++) {
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
			outgoing[i].clear();
			incoming[i].clear();
		}
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
	 * First, we compute the topolical order, then the longest path from the starting task node to each concrete task.
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
		solver.propagate();// check that the schedule is feasible


		final LinkedList<TaskVar> criticalPath = sol2path(topOrder.topologicalOrder, makespan);
		LOGGER.info("CRITICAL_PATH "+criticalPath);
		LOGGER.info("CRITICAL_PATH_LENGTH "  + criticalPath.size());
		final LinkedList<LinkedList<Term>> dnf = block2dnf(criticalPath);
		LOGGER.info("DNF "  + dnf2string(dnf));
		LOGGER.info("DNF_LENGTH "  + dnf.size());
		boolVars.clear();
		final LinkedList<LinkedList<Term>> cnf = dnf2cnf(solver, dnf);
		LOGGER.info("CNF "  + cnf2string(cnf));
		LOGGER.info("CNF_LENGTH "  + cnf.size());
		cnf2clauses(solver, cnf);
		LOGGER.info("BOOL_VARS "+boolVars);
		ChocoLogging.flushLogs();
	}

	public LinkedList<TaskVar> sol2path(int[] order, int makespan) {
		//Find one of the last task of the schedule which belongs necessarily to a critical path
		int i = tasks.length-1;
		while(i >= 0) {
			if(tasks[order[i]].end().getVal() == makespan) {
				break;
			}
		}
		assert(i >= 0);
		// Follow the critical path in reverse order 
		final LinkedList<TaskVar> criticalPath = new LinkedList<TaskVar>();
		boolean predfound  = true;
		while(predfound) {
			predfound = false;
			final TaskVar t = tasks[order[i]];
			criticalPath.addFirst(t);
			final int start1= t.start().getVal();
			final int m1 = machine(order[i]);
			final int j1 = job(order[i]);
			//find the next task to add to the critical path
			while(i > 0) {
				i--;
				final int end2 = tasks[order[i]].end().getVal();
				final int m2 = machine(order[i]);
				final int j2 = job(order[i]);
				if( start1 == end2 && (m1 == m2 || j1 == j2) ) {
					predfound=true;
					break;
				}
			}
		}
		//Check that the critical path is complete
		assert criticalPath.getFirst().start().getVal() == 0 && criticalPath.getLast().end().getVal() == makespan;
		return criticalPath;
	}

	private LinkedList<LinkedList<Term>> block2dnf(LinkedList<TaskVar> criticalPath) {
		final LinkedList<LinkedList<Term>> dnf = new LinkedList<LinkedList<Term>>();
		if(criticalPath.size() > 1) {
			final LinkedList<TaskVar> block = new LinkedList<TaskVar>();
			final ListIterator<TaskVar> iter = criticalPath.listIterator();
			//init
			TaskVar t1 = iter.next();
			TaskVar t2 = iter.next();
			block.add(t1);
			block.add(t2);
			boolean machineBlock =  machine(t1) == machine(t2);
			assert machineBlock || job(t1) == job(t2);
			//Loop over block
			while(iter.hasNext()) {
				t1 = t2;
				t2 = iter.next();
				if(machineBlock) {
					if(machine(t1) == machine(t2)) {
						block.add(t2);
					} else {
						assert job(t1) == job(t2);
						block2dnf(dnf,block);
						block.clear();
						block.add(t1);
						block.add(t2);
						machineBlock=false;
					}
				}else {
					if(job(t1) == job(t2)) {
						block.add(t2);
					} else {
						assert machine(t1) == machine(t2);
						block2dnf(dnf,block);
						block.clear();
						block.add(t1);
						block.add(t2);
						machineBlock=true;
					}
				}

			}
			block2dnf(dnf, block);
		}
		return dnf;
	}

	private void block2dnf(LinkedList<LinkedList<Term>> dnf, LinkedList<TaskVar> block) {
		LOGGER.info("BLOCK "+block);
		if(block.size() == 2) {
			final TaskVar t1 = block.getFirst();
			final TaskVar t2 = block.getLast();
			final LinkedList<Term> and = new LinkedList<Term>();
			and.add(litteral(t2, t1));
			dnf.add(and);
		} else {
			final int n = block.size() -1;
			//Before First
			final TaskVar first = block.removeFirst();
			for (int i = 0; i < n; i++) {
				final TaskVar current = block.removeFirst();
				final LinkedList<Term> and = new LinkedList<Term>();
				//Current Before 
				for (TaskVar t : block) {
					and.add(litteral(current, t));
				}
				and.add(litteral(current, first));
				dnf.add(and);
				block.addLast(current);
			}
			block.addFirst(first); //revert
			// The previous procedure must preserve the block order
			//After Last
			final TaskVar last = block.removeLast();
			for (int i = 0; i < n; i++) {
				final TaskVar current = block.removeFirst();
				final LinkedList<Term> and = new LinkedList<Term>();
				//Current Before 
				for (TaskVar t : block) {
					and.add(litteral(t,current));
				}
				and.add(litteral(last, current));
				dnf.add(and);
				block.addLast(current);
			}
			block.addLast(last); //revert
		}
		// The method must preserve the block order
	}


	private final static LinkedList<LinkedList<Term>> dnf2cnf(Term lit, LinkedList<Term> and) {
		LinkedList<LinkedList<Term>> cnf = new LinkedList<LinkedList<Term>>();
		for (Term clit: and) {
			LinkedList<Term> or = new LinkedList<Term>();
			or.add(lit);
			or.add(clit);
			cnf.add(or);
		}
		return cnf;
	}


	
	public LinkedList<LinkedList<Term>> dnf2cnf(Solver solver, LinkedList<LinkedList<Term>> dnf) {
		if(dnf.isEmpty()) return new LinkedList<LinkedList<Term>>();
		LinkedList<LinkedList<Term>> cnf = null;
		LinkedList<Term> p = dnf.removeFirst();
		assert p.size() > 0;
		if(p.size() == 1) {
			if(dnf.size() == 1) {
				cnf = dnf2cnf(p.getFirst(), dnf.removeFirst());
			} else {
				cnf = dnf2cnf(solver,dnf);
				for (LinkedList<Term> or : cnf) {
					or.addFirst(p.getFirst());
				}
			}

		} else {
			IntDomainVar b= makeBoolVar();
			Term z = new Term(b,true);
			Term nz = new Term(b, false);
			if(dnf.size() == 1) {
				cnf = dnf2cnf(nz, dnf.removeFirst());
			} else {
				cnf = dnf2cnf(solver, dnf);
				for (LinkedList<Term> or : cnf) {
					or.addFirst(nz);
				}
			}
			//Less insertions, but at the beginning of the list for readability
			cnf.addAll(0, dnf2cnf(z,p));
		}
		return cnf;
	}

	List<IntDomainVar> boolVars = new LinkedList<IntDomainVar>();
	
	private IntDomainVar makeBoolVar() {
		final IntDomainVar b= solver.createBooleanVar(StringUtils.randomName());
		boolVars.add(b);
		return b; 
	}

	public static void cnf2clauses(Solver solver, LinkedList<LinkedList<Term>> cnf) {
		for (LinkedList<Term> or : cnf) {
			int npos=0;
			for (Term term : or) {
				if(term.isPositive()) npos++;
			}
			IntDomainVar[] posLits = new IntDomainVar[npos];
			IntDomainVar[] negLits = new IntDomainVar[or.size() - npos];
			int pidx=0;
			int nidx=0;
			for (Term term : or) {
				if(term.isPositive()) posLits[pidx++]=term.getLitteral();
				else negLits[nidx++]=term.getLitteral();
			}
			LOGGER.info("CLAUSE POS"+Arrays.toString(posLits) + " NEG"+Arrays.toString(negLits));
		}
		
	}


	/**
	 * the litteral is true if t1 precedes t2.
	 * @param t1
	 * @param t2
	 * @return 
	 */
	private Term litteral(TaskVar t1, TaskVar t2) {
		ITemporalSRelation rel = disjSMod.getConstraint(t1, t2);
		if(rel == null) {
			rel = disjSMod.getConstraint(t2, t1);
			return new Term(rel.getDirection(), false);
		} else return new Term(rel.getDirection(),true);

	}

	private String formula2string(LinkedList<LinkedList<Term>> formula, String termSep, String elemSep) {
		StringBuilder b =new StringBuilder();
		if(! formula.isEmpty() ) {	
		for (LinkedList<Term> and : formula) {
			if(and.size() == 1) {
				b.append(and.getFirst());
			} else {
				b.append('(');
				for (Term litteral : and) {
					b.append(litteral).append(termSep);
				}
				b.delete(b.length() -termSep.length(), b.length());
				b.append(")");
			}
			b.append(elemSep);
		}
		b.delete(b.length() -elemSep.length(), b.length());
		}
		return b.toString();
	}

	
	private String dnf2string(LinkedList<LinkedList<Term>> dnf) {
		return formula2string(dnf, " & ", " |\n");
//		StringBuilder b =new StringBuilder();
//		if(! dnf.isEmpty() ) {	
//		for (LinkedList<Term> and : dnf) {
//			if(and.size() == 1) {
//				b.append(and.getFirst());
//			} else {
//				b.append('(');
//				for (Term litteral : and) {
//					b.append(litteral).append(" & ");
//				}
//				b.delete(b.length() -3, b.length());
//				b.append(")");
//			}
//			b.append(" | ");
//		}
//		b.delete(b.length() -3, b.length());
//		}
//		return b.toString();
	}

	private String cnf2string(LinkedList<LinkedList<Term>> cnf) {
		return formula2string(cnf, " | ", " &\n");
//		StringBuilder b =new StringBuilder();
//		if(! cnf.isEmpty() ) {
//			for (LinkedList<Term> and : cnf) {
//				if(and.size() == 1) {
//					b.append(and.getFirst());
//				} else {
//					b.append('(');
//					for (Term litteral : and) {
//						b.append(litteral).append(" | ");
//					}
//					b.delete(b.length() -3, b.length());
//					b.append(")");
//				}
//				b.append(" & ");
//			}
//			b.delete(b.length() -3, b.length());
//		}
//		return b.toString();
	}

	private void handleBlock(LinkedList<LinkedList<Term>> dnf, LinkedList<TaskVar> block) {
		LOGGER.info("BLOCK "+block);
		if(block.size() == 2) {
			TaskVar t1 = block.getFirst();
			TaskVar t2 = block.getLast();
			LinkedList<Term> conjonction = new LinkedList<Term>();
			conjonction.add(litteral(t2, t1));
			dnf.add(conjonction);
		} else {
			final int n = block.size() -1;
			//Before First
			TaskVar first = block.removeFirst();
			for (int i = 0; i < n; i++) {
				TaskVar current = block.removeFirst();
				LinkedList<Term> and = new LinkedList<Term>();
				//Current Before 
				for (TaskVar t : block) {
					and.add(litteral(current, t));
				}
				and.add(litteral(current, first));
				dnf.add(and);
				block.addLast(current);
			}
			block.addFirst(first); //revert
			// The previous procedure must preserve the block order
			//After Last
			TaskVar last = block.removeLast();
			for (int i = 0; i < n; i++) {
				TaskVar current = block.removeFirst();
				LinkedList<Term> and = new LinkedList<Term>();
				//Current Before 
				for (TaskVar t : block) {
					and.add(litteral(t,current));
				}
				and.add(litteral(last, current));
				dnf.add(and);
				block.addLast(current);
			}
			block.addLast(last); //revert
		}
		// The method must preserve the block order
	}
	private static final int job(TaskVar t) {
		return t.getExtension(DisjunctiveSettings.JOB_EXTENSION).get();
	}

	private final int job(int i) {
		return tasks[i].getExtension(DisjunctiveSettings.JOB_EXTENSION).get();
	}

	private final int machine(int i) {
		return tasks[i].getExtension(DisjunctiveSettings.MACHINE_EXTENSION).get();
	}

	private static final int machine(TaskVar t) {
		return t.getExtension(DisjunctiveSettings.MACHINE_EXTENSION).get();
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


final class Term {


	public final IntDomainVar litteral;

	public final boolean positive;

	public Term(IntDomainVar value, boolean positive) {
		super();
		this.litteral = value;
		this.positive = positive;
	}


	public final IntDomainVar getLitteral() {
		return litteral;
	}


	public final boolean isPositive() {
		return positive;
	}


	@Override
	public String toString() {
		return (positive ? "" : "~")+ litteral;
	}


}

