package pisco.shop.choco.constraints;

import gnu.trove.TIntArrayList;

import java.util.Arrays;

import pisco.shop.AbstractAirlandProblem;

import choco.cp.common.util.preprocessor.detector.scheduling.DisjunctiveSModel;
import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.comparator.IPermutation;
import choco.kernel.common.util.comparator.TaskComparators;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.common.util.tools.PermutationUtils;
import choco.kernel.memory.IStateInt;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.global.scheduling.AbstractTaskSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;

public class RelaxConstraint_1_prec_rj_Lmax extends AbstractTaskSConstraint {

	protected final TaskVar[] tasks;

	protected final int[][] disjuncts;

	protected final IStateInt[] predecessorCount;
	protected final IStateInt[] successorCount;

	protected final int[] _predecessorCount;
	protected final int[] _successorCount;
	protected final int[] modifiedDueDates;

	//protected final TIntArrayList topologicalOrder;
	protected final TIntArrayList currentIndices;

	protected final AbstractAirlandProblem problem;

	public RelaxConstraint_1_prec_rj_Lmax(AbstractAirlandProblem problem, TaskVar[] taskvars, IntDomainVar[] disjuncts, IntDomainVar lmax) {
		super(taskvars, disjuncts, lmax);
		this.problem = problem;
		tasks = Arrays.copyOf(taskvars, taskvars.length);
		predecessorCount = new IStateInt[taskvars.length];
		successorCount = new IStateInt[taskvars.length];
		_successorCount= new int[taskvars.length];
		_predecessorCount= new int[taskvars.length];
		modifiedDueDates = new int[taskvars.length];
		//topologicalOrder = new TIntArrayList(taskvars.length);
		currentIndices = new TIntArrayList(taskvars.length);
		this.disjuncts = new int[disjuncts.length][2];
		int idx = 0;
		for (int i = 0; i < taskvars.length; i++) {
			for (int j = i+1; j < taskvars.length; j++) {
				this.disjuncts[idx][0]=i;
				this.disjuncts[idx][1]=j;
				idx++;
			}
		}
	}


	@Override
	public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain)
			throws ContradictionException {}


	@Override
	public void awakeOnRem(int varIdx, int val) throws ContradictionException {}


	@Override
	public boolean isTaskConsistencyEnforced() {
		return false;
	}

	@Override
	public void awakeOnHypDomMod(int varIdx) throws ContradictionException {}


	@Override
	public void awakeOnInst(int idx) throws ContradictionException {
		if(idx >= taskIntVarOffset && idx < vars.length - 1) {
			idx -= taskIntVarOffset;
			if(vars[idx].isInstantiatedTo(1)) {
				predecessorCount[disjuncts[idx][1]].increment();
				successorCount[disjuncts[idx][0]].increment();
			} else {
				predecessorCount[disjuncts[idx][0]].increment();
				successorCount[disjuncts[idx][1]].increment();
			}

		}
	}


	@Override
	public void awakeOnBounds(int varIndex) throws ContradictionException {}





	@Override
	public boolean isSatisfied(int[] tuple) {
		// TODO Auto-generated method stub
		return true;
	}


	@Override
	public void awakeOnInf(int varIdx) throws ContradictionException {
		this.constAwake(false);
	}


	@Override
	public void awakeOnSup(int varIdx) throws ContradictionException {}


	@Override
	public Boolean isEntailed() {
		return super.isEntailed();
	}


	@Override
	public void awake() throws ContradictionException {
		for (int i = 0; i < taskvars.length; i++) {
			assert(i == taskvars[i].getID());
			predecessorCount[i] = problem.getSolver().getEnvironment().makeInt(0);
			successorCount[i] = problem.getSolver().getEnvironment().makeInt(0);
		}
		super.awake();
	}


	
	private void handlePrecedence(int i, int j) {
		final int mdd = modifiedDueDates[j] - problem.processingTimes[j];
		if(mdd < modifiedDueDates[i]) {
			modifiedDueDates[i] = mdd;
		}
		
		_successorCount[i]--;
		if(_successorCount[i] == 0) {
			currentIndices.add(j);
		}
	}
	
	@Override
	public void propagate() throws ContradictionException {
		//init data structures
		currentIndices.resetQuick();
		for (int i = 0; i < taskvars.length; i++) {
			_predecessorCount[i] = predecessorCount[i].get();
			_successorCount[i] = successorCount[i].get();
			if(_successorCount[i] == 0) {
				currentIndices.add(i);
			}
			modifiedDueDates[i] = problem.dueDates[i];
		}
		// compute modified due dates
		assert( ! currentIndices.isEmpty());
		while( ! currentIndices.isEmpty()) {
			final int i = currentIndices.remove(currentIndices.size() - 1);
			for (int j = 0; j < i; j++) {
				final int idx = taskIntVarOffset + AbstractAirlandProblem.getDisjunct(j, i, taskvars.length);
				if(vars[idx].isInstantiatedTo(1)) {
					handlePrecedence(j, i);
				} else if(vars[idx].isInstantiatedTo(0)) {
					handlePrecedence(i, j);
				}
			}
			for (int j = i+1; j < taskvars.length; j++) {
				final int idx = taskIntVarOffset + AbstractAirlandProblem.getDisjunct(i, j, taskvars.length);
				if(vars[idx].isInstantiatedTo(1)) {
					handlePrecedence(i, j);
				} else if(vars[idx].isInstantiatedTo(0)) {
					handlePrecedence(j, i);
				}
			}
		}
		// schedule tasks
		IPermutation permutation = PermutationUtils.getSortingPermuation(modifiedDueDates);
		Arrays.sort(tasks, TaskComparators.makeEarliestStartingTimeCmp());
		int time = tasks[0].getEST();
		for (int i = 0; i < taskvars.length; i++) {
			if(_predecessorCount[i] == 0) {
				currentIndices.add(i);
			}
		}
		
		
	}


	@Override
	public int getFilteredEventMask(int idx) {
		if(idx < startOffset) return IntVarEvent.INCINF_MASK;
		else if(idx < endOffset) return 0;
		else if(idx < taskIntVarOffset) return IntVarEvent.INCINF_MASK;
		else if(idx == vars.length - 1) return IntVarEvent.DECSUP_MASK;
		else return IntVarEvent.INSTINT_MASK;
	}


}
