package pisco.single.choco.constraints;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntProcedure;

import java.util.Arrays;

import pisco.common.ITJob;
import pisco.common.JobUtils;
import pisco.common.PJob;
import pisco.common.Pmtn1Scheduler;
import pisco.single.Abstract1MachineProblem;
import choco.cp.solver.constraints.global.scheduling.precedence.ITemporalSRelation;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.DottyBean;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.memory.IStateBool;
import choco.kernel.memory.structure.StoredBipartiteSet;
import choco.kernel.memory.structure.StoredIntBipartiteList;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.global.scheduling.AbstractTaskSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;
import choco.kernel.visu.VisuFactory;

public class RelaxLmaxConstraint extends AbstractTaskSConstraint {


	protected final Abstract1MachineProblem problem;

	private TIntArrayList[] precReductionGraph;

	//	private StoredBipartiteSet<ITemporalSRelation> disjunctList;
	//
	//	private IStateBool fireDisjunctList;

	private ITemporalSRelation[] disjunctList;

	protected final ITJob[] jobs;

	protected final ITJob[] schedule;

	public RelaxLmaxConstraint(Abstract1MachineProblem problem, TaskVar[] taskvars, IntDomainVar[] disjuncts, IntDomainVar lmax) {
		super(taskvars, disjuncts, lmax);
		this.problem = problem;
		jobs = new ITJob[taskvars.length];
		for (int i = 0; i < jobs.length; i++) {
			jobs[i] = new PJob(taskvars[i].getID());
			jobs[i].setDuration(problem.jobs[i].getDuration());
			assert(taskvars[i].getID() == problem.jobs[i].getID());
		}
		schedule = Arrays.copyOf(jobs, jobs.length);

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
		//		if(idx >= taskIntVarOffset && idx < vars.length - 1) {
		//			fireDisjunctList.set(true);
		//		}
		constAwake(false);
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

	@SuppressWarnings("unchecked")
	@Override
	public void awake() throws ContradictionException {
		PreProcessCPSolver ppsolver = ( (PreProcessCPSolver) problem.getSolver());
		precReductionGraph = ppsolver.getDisjModel().convertPrecGraph();
		disjunctList = ppsolver.getDisjSModel().getEdges();
		//		disjunctList = ppsolver.getEnvironment().makeStoredBipartiteList(ppsolver.getDisjSModel().getEdges());
		//		fireDisjunctList = ppsolver.getEnvironment().makeBool(true);
		super.awake();
	}

	private void updateDisjunctList() {
		//		if(fireDisjunctList.get()) {
		//			final DisposableIterator<ITemporalSRelation> iter = disjunctList.quickIterator();
		//			while(iter.hasNext()) {
		//				// FIXME - Iterator does not allow removals - created 10 avr. 2012 by A. Malapert
		//				if(iter.next().isFixed()) {iter.remove();}
		//			}
		//			iter.dispose();
		//			fireDisjunctList.set(false);
		//		}
	}

	private void addSuccessors(final int i) {
		// TODO - Avoid re-creating the procedure - created 10 avr. 2012 by A. Malapert
		precReductionGraph[i].forEach( new TIntProcedure() {

			@Override
			public boolean execute(int arg0) {
				jobs[i].addSuccessor(jobs[arg0]);
				return true;
			}
		});
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
		updateDisjunctList();
		//Add Reduced Precedence to jobs
		for (int i = 0; i < precReductionGraph.length; i++) {
			addSuccessors(i);
		}
		//Add fixed (during search) precedences
		//		final DisposableIterator<ITemporalSRelation> iter = disjunctList.quickIterator();
		//		while(iter.hasNext()) {
		//			final ITemporalSRelation rel = iter.next();
		//Brute force implementation (not incremental
		for (ITemporalSRelation rel : disjunctList) {
			assert(rel.isFixed());
			if(rel.isFixed()) {
				if(rel.getDirVal() == 1) {
					//forward
					jobs[rel.getOrigin().getID()].addSuccessor(jobs[rel.getDestination().getID()]);
				} else {
					//backward
					jobs[rel.getOrigin().getID()].addPredecessor(jobs[rel.getDestination().getID()]);
				}
			}
		}
		//iter.dispose();
		//Modify Due Dates 
		// TODO - Remove this step in further version by stating dedicated constraints  - created 6 avr. 2012 by A. Malapert
		//	VisuFactory.getDotManager().show(new DottyBean(schedule));
		//System.out.println(Arrays.toString(schedule));
	//	VisuFactory.getDotManager().show( new DottyBean(schedule));
		JobUtils.modifyDueDates(schedule);
		final int lb = Pmtn1Scheduler.schedule1Lmax(schedule);
		int cpt = 0;
		for (ITJob j : schedule) {
			if(j.isInterrupted() || ! j.isScheduledInTimeWindow()) {
				cpt++;
			}
		}
		System.out.println("CPT="+cpt);
		
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
