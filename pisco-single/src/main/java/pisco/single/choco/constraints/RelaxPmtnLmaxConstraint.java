package pisco.single.choco.constraints;

import gnu.trove.TIntArrayList;

import java.util.Arrays;

import pisco.common.ITJob;
import pisco.common.JobUtils;
import pisco.common.PJob;
import pisco.common.Pmtn1Scheduler;
import pisco.single.Abstract1MachineProblem;
import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.DottyBean;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IStateBool;
import choco.kernel.memory.structure.StoredIntBipartiteList;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.global.scheduling.AbstractTaskSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;
import choco.kernel.visu.VisuFactory;

public class RelaxPmtnLmaxConstraint extends AbstractTaskSConstraint {

	protected final TaskVar[] tasks;

	protected final int[][] disjuncts;
	protected StoredIntBipartiteList remDisjunctList;
	private IStateBool fireDisjunctList;


	protected final TIntArrayList currentIndices;

	protected final Abstract1MachineProblem problem;

	protected final ITJob[] jobs;

	protected final ITJob[] schedule;

	
	public RelaxPmtnLmaxConstraint(Abstract1MachineProblem problem, TaskVar[] taskvars, IntDomainVar[] disjuncts, IntDomainVar lmax) {
		super(taskvars, disjuncts, lmax);
		this.problem = problem;
		tasks = Arrays.copyOf(taskvars, taskvars.length);
		jobs = new ITJob[taskvars.length];
		for (int i = 0; i < jobs.length; i++) {
			jobs[i] = new PJob(taskvars[i].getID());
			jobs[i].setDuration(problem.jobs[i].getDuration());
			assert(taskvars[i].getID() == problem.jobs[i].getID());
		}
		schedule = Arrays.copyOf(jobs, jobs.length);
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
			fireDisjunctList.set(true);
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
		final TIntArrayList remDisjuncts = new TIntArrayList(disjuncts.length);
		for (int i = taskIntVarOffset; i < vars.length -1; i++) {
			if(! vars[i].isInstantiated()) {
				remDisjuncts.add(i);
			}
		}
		remDisjunctList = (StoredIntBipartiteList) problem.getSolver().getEnvironment().makeBipartiteIntList(remDisjuncts.toNativeArray());
		fireDisjunctList = problem.getSolver().getEnvironment().makeBool(false);
		super.awake();
	}

	private void updateDisjunctList() {
		final DisposableIntIterator it = remDisjunctList.getIterator();
		while(it.hasNext()) {
			final int idx = it.next();
			if(vars[idx].isInstantiated()) {
				it.remove();
			} 
			it.dispose();

		}
	}

	@Override
	public void propagate() throws ContradictionException {
		for (int i = 0; i < taskvars.length; i++) {
			jobs[i].resetSchedule();
			jobs[i].resetPrecedences();
			jobs[i].setReleaseDate(taskvars[i].getEST());
			jobs[i].setDueDate(problem.jobs[i].getDueDate());
			jobs[i].setDeadline(taskvars[i].getLCT());
		}
		//Update instantiated disjuncts
		if(fireDisjunctList.get()) {
			updateDisjunctList();
		}
		//Add Precedence to jobs
		final DisposableIntIterator it = remDisjunctList.getRemIterator();
		while(it.hasNext()) {
			final int varIdx = it.next();
			final int idx = varIdx - taskIntVarOffset;
			int o = 0, d = 0;
			if(vars[varIdx].isInstantiatedTo(1)) {
				d = 1;
			} else {
				assert (vars[varIdx].isInstantiatedTo(0));
				o = 1;
			}
			jobs[disjuncts[idx][o]].addSuccessor(jobs[disjuncts[idx][d]]);
		}
		it.dispose();
		//Modify Due Dates 
		// TODO - Remove this step in further version by stating dedicated constraints  - created 6 avr. 2012 by A. Malapert
	//	VisuFactory.getDotManager().show(new DottyBean(schedule));
		//System.out.println(Arrays.toString(schedule));
		JobUtils.modifyDueDates(schedule);
		final int lb = Pmtn1Scheduler.schedule1Lmax(schedule);
		//System.out.println(Arrays.toString(schedule));
//		if(lb > vars[vars.length - 1].getSup()) {
//			ChocoLogging.getMainLogger().info(lb+ " > " + vars[vars.length-1].pretty());	
//		}
		//System.out.println("lb "+lb);
		vars[vars.length-1].updateInf(lb, this, false);
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
