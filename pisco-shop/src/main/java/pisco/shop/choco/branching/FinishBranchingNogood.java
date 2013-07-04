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
package pisco.shop.choco.branching;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import pisco.common.DisjunctiveSettings;
import pisco.common.choco.branching.FinishBranchingGraph;
import pisco.shop.choco.branching.nogood.BoolConstraintFactory;
import pisco.shop.choco.branching.nogood.BoolVariableFactory;
import pisco.shop.choco.branching.nogood.NogoodUtils;
import pisco.shop.choco.branching.nogood.Term;
import choco.cp.common.util.preprocessor.detector.scheduling.DisjunctiveSModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.scheduling.precedence.ITemporalSRelation;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.cp.solver.search.integer.branching.domwdeg.DomWDegUtils;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.search.ISolutionMonitor;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;



final class LogCtFactory implements BoolConstraintFactory {

	public final static BoolConstraintFactory SINGLOTON = new LogCtFactory();

	private final static Logger LOGGER = ChocoLogging.getBranchingLogger();


	private LogCtFactory() {
		super();
	}


	@Override
	public void postBoolConstraint(IntDomainVar[] posLits,
			IntDomainVar[] negLits) {
		LOGGER.info("CLAUSE POS"+Arrays.toString(posLits) + " NEG"+Arrays.toString(negLits));
	}


}

final class NogoodFactory implements BoolConstraintFactory {

	public final CPSolver solver;


	public NogoodFactory(CPSolver solver) {
		super();
		this.solver = solver;
	}

	@Override
	public void postBoolConstraint(IntDomainVar[] posLits,
			IntDomainVar[] negLits) {
		solver.addNogood(posLits, negLits);	
	}


}

class StaticBoolVarFactory implements BoolVariableFactory {

	private Solver solver;

	private int currentIndex;

	private final IntDomainVar[] boolVarPool;


	public StaticBoolVarFactory(Solver solver,IntDomainVar[] boolVarPool) {
		super();
		this.solver=solver;
		this.currentIndex=0;
		this.boolVarPool = boolVarPool;

	}


	@Override
	public int getVariableCount() {
		return currentIndex;
	}


	@Override
	public IntDomainVar makeBoolVar() {
		if(currentIndex < boolVarPool.length) {
			return boolVarPool[currentIndex++];
		}
		throw new SolverException("The pool of boolean variable is empty");
		//return null;
	}


	@Override
	public void validate() {
		ChocoLogging.flushLogs();
		try {
			for (int i = currentIndex; i < boolVarPool.length; i++) {
				//	for (int i = 0; i < boolVarPool.length; i++) {
				if( !boolVarPool[i].isInstantiated()) 
					boolVarPool[i].instantiate(0, null,false);
				else System.err.println("Bizarre");
			}
			for (int i = 0; i < currentIndex; i++) {
				if( !boolVarPool[i].isInstantiated()) {
					//ChocoLogging.getBranchingLogger().severe("INST "+boolVarPool[i]);
					//ChocoLogging.flushLogs();
					boolVarPool[i].instantiate(0, null,false);
					solver.propagate();
				}
			}
		} catch (ContradictionException e) {
			ChocoLogging.flushLogs();
			ChocoLogging.getBranchingLogger().severe(solver.pretty());
			throw new SolverException("Contradiction raised by the boolean variables pool.");
		}
	}



}


/**
 * Finish the shop branching in a backtrack-free way
 */
public final class FinishBranchingNogood extends FinishBranchingGraph  implements ISolutionMonitor {


	private final BoolConstraintFactory ctFactory;

	private final BoolVariableFactory boolFactory;

	private final IBlockManager blockManager;

	public FinishBranchingNogood(PreProcessCPSolver solver,
			DisjunctiveSModel disjSMod, Constraint ignoredCut) {
		super(solver,disjSMod, ignoredCut);
		ctFactory = new NogoodFactory(solver);
		int n = 10000;
		IntDomainVar[] bvarPool = new IntDomainVar[n];
		for (int i = 0; i < n; i++) {
			bvarPool[i]=solver.createBoundIntVar(StringUtils.randomName(), 0, 1);
			DomWDegUtils.addVariableExtension(bvarPool[i]);
		}
		boolFactory = new StaticBoolVarFactory(solver, bvarPool);
		blockManager = disjSMod.getNbArcs() == 0 ? 
				new OSBlockManager() : new JFSBlockManager();
	}




	@Override
	protected void doFakeBranching() throws ContradictionException {
		super.doFakeBranching();
		boolFactory.validate();
	}


	@Override
	public void recordSolution(Solver solver) {
		final LinkedList<TaskVar> criticalPath = sol2path(getTopologicalOrder(), solver.getMakespanValue());
		final LinkedList<LinkedList<Term>> dnf = blockManager.block2dnf(criticalPath);
		if(LOGGER.isLoggable(Level.CONFIG)) {
			LOGGER.log(Level.CONFIG, "CRITICAL_PATH {0}", criticalPath);
			//if(LOGGER.isLoggable(Level.CONFIG)) {
				LOGGER.config("DNF "+NogoodUtils.dnf2string(dnf));
			//}
			ChocoLogging.flushLogs();

		}
		final LinkedList<LinkedList<Term>> cnf = NogoodUtils.dnf2cnf(dnf, boolFactory);
		if(LOGGER.isLoggable(Level.CONFIG)) {
			LOGGER.config("CNF "+NogoodUtils.cnf2string(cnf));
			ChocoLogging.flushLogs();

		}
		NogoodUtils.formula2constraints(cnf, ctFactory);
	}


	public LinkedList<TaskVar> sol2path(int[] order, int makespan) {
		//Find one of the last task of the schedule which belongs necessarily to a critical path
		int i = tasks.length-1;
		while(i >= 0) {
			if(tasks[order[i]].end().getVal() == makespan) {
				break;
			} else i--;
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
	private void block2dnf(LinkedList<LinkedList<Term>> dnf, LinkedList<TaskVar> block) {
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



	private static interface IBlockManager {

		LinkedList<LinkedList<Term>> block2dnf(LinkedList<TaskVar> criticalPath);

	}

	private final class OSBlockManager implements IBlockManager {

		@Override
		public LinkedList<LinkedList<Term>> block2dnf(LinkedList<TaskVar> criticalPath) {
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
							FinishBranchingNogood.this.block2dnf(dnf,block);
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
							FinishBranchingNogood.this.block2dnf(dnf,block);
							block.clear();
							block.add(t1);
							block.add(t2);
							machineBlock=true;
						}
					}

				}
				FinishBranchingNogood.this.block2dnf(dnf, block);
			}
			return dnf;
		}

	}



	private final class JFSBlockManager implements IBlockManager {


		@Override
		public LinkedList<LinkedList<Term>> block2dnf(LinkedList<TaskVar> criticalPath) {
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
							FinishBranchingNogood.this.block2dnf(dnf,block);
							block.clear();
							machineBlock=false;
						}
					}else {
						if(job(t1) != job(t2)) {
							assert machine(t1) == machine(t2);
							block.clear();
							block.add(t1);
							block.add(t2);
							machineBlock=true;
						}
					}

				}
				if(machine(t1) == machine(t2)) {
					FinishBranchingNogood.this.block2dnf(dnf, block);
				}
			}
			return dnf;
		}

	}

}






