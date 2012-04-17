package pisco.single.choco.constraints;

import static choco.Choco.MAX_UPPER_BOUND;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntProcedure;
import gnu.trove.TLinkableAdapter;
import gnu.trove.TLinkedList;
import gnu.trove.TObjectProcedure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;

import pisco.common.ITJob;
import pisco.common.JobUtils;
import pisco.common.PDR1Scheduler;
import pisco.common.PJob;
import pisco.common.Pmtn1Scheduler;
import pisco.common.TCollections;
import pisco.single.Abstract1MachineProblem;
import pisco.single.SingleMachineSettings;
import pisco.single.SingleMachineSettings.PropagagationLevel;
import choco.cp.common.util.preprocessor.detector.scheduling.DisjunctiveSModel;
import choco.cp.solver.constraints.global.scheduling.precedence.ITemporalSRelation;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.DottyBean;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.comparator.TaskComparators;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.global.scheduling.AbstractTaskSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;
import choco.kernel.visu.VisuFactory;

public class RelaxLmaxConstraint extends AbstractTaskSConstraint {

	public static boolean canFailOnSolutionRecording = false;

	protected final Abstract1MachineProblem problem;

	protected final ITJob[] jobs;

	protected final ITJob[] tempJobs;

	private DisjunctiveSModel disjSMod;

	private TIntArrayList[] precReductionGraph;

	private ITemporalSRelation[] disjunctList;

	private final static int START = 0;
	private final static int END = 1;

	private int solutionStamp = -1;

	private int backtrackStamp = -1;

	private final IRelaxationFilter pmtnRelaxation;

	private final IRelaxationFilter precRelaxation;

	public RelaxLmaxConstraint(Abstract1MachineProblem problem, TaskVar[] taskvars, IntDomainVar[] disjuncts, IntDomainVar lmax) {
		super(taskvars, disjuncts, lmax);
		this.problem = problem;
		jobs = new ITJob[taskvars.length];
		for (int i = 0; i < jobs.length; i++) {
			jobs[i] = new PJob(taskvars[i].getID());
			jobs[i].setDuration(problem.jobs[i].getDuration());
			assert(taskvars[i].getID() == i && taskvars[i].getID() == problem.jobs[i].getID());
		}
		tempJobs = Arrays.copyOf(jobs, jobs.length);

		pmtnRelaxation = new PmtnRelaxationFilter(SingleMachineSettings.readPmtnLevel(problem));
		precRelaxation = new PrecRelaxationFilter(SingleMachineSettings.readPrecLevel(problem));

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
		return true;
	}


	@Override
	public void awakeOnInf(int varIdx) throws ContradictionException {
		this.constAwake(false);
	}


	@Override
	public void awakeOnSup(int varIdx) throws ContradictionException {}


	//	@Override
	//	public Boolean isEntailed() {
	//		return super.isEntailed();
	//	}

	@SuppressWarnings("unchecked")
	@Override
	public void awake() throws ContradictionException {
		PreProcessCPSolver ppsolver = ( (PreProcessCPSolver) problem.getSolver());
		disjSMod = ppsolver.getDisjSModel();
		precReductionGraph = ppsolver.getDisjModel().convertPrecGraph();
		disjunctList = disjSMod.getEdges();
		super.awake();
	}

	private void buildJobs() {
		for (int i = 0; i < taskvars.length; i++) {
			jobs[i].resetSchedule();
			jobs[i].resetPrecedences();
			jobs[i].setReleaseDate(taskvars[i].getEST());
			jobs[i].setDueDate(problem.jobs[i].getDueDate());
			jobs[i].setDeadline(taskvars[i].getLCT());
		}
	}

	class TSuccprocedure implements TIntProcedure {

		private int origin;

		@Override
		public boolean execute(int arg0) {
			jobs[origin].addSuccessor(jobs[arg0]);
			return true;
		}
	}

	private TSuccprocedure succProc = new TSuccprocedure();

	private void buildPrecedence() {
		//Add Reduced Precedence to jobs
		for (int i = 0; i < precReductionGraph.length; i++) {
			succProc.origin = i;
			precReductionGraph[i].forEach( succProc);
		}

		// FIXME - Brute force implementation (not incremental at all) - created 10 avr. 2012 by A. Malapert
		for (ITemporalSRelation rel : disjunctList) {
			//assert(rel.isFixed());
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
	}



	@Override
	public void propagate() throws ContradictionException {
		checkSolutionStamp();
		buildJobs();
		buildPrecedence();
		//Modify Due Dates 
		// FIXME - Remove this step in further version by stating dedicated constraints  - created 6 avr. 2012 by A. Malapert
		JobUtils.modifyDueDates(tempJobs);
		////////////////
		if(pmtnRelaxation.filterObjective() || precRelaxation.filterObjective()  ||
				pmtnRelaxation.filterPrecedences() || precRelaxation.filterPrecedences()) {
			//an optimal solution has been found during propagation
			pmtnRelaxation.clearUpdateLists();
			precRelaxation.clearUpdateLists();
			recordSolution();
		} else {
			pmtnRelaxation.flushUpdateLists();
			precRelaxation.flushUpdateLists();
		}
	}


	private void checkSolutionStamp() {
		if(backtrackStamp > 0 && 
				problem.getSolver().getBackTrackCount() > backtrackStamp
				&& problem.getSolver().getSolutionCount() <= solutionStamp) {
			ChocoLogging.flushLogs();
			throw new SolverException("Failed to record solution "+this.getClass().getSimpleName());
		} else {
			backtrackStamp = -1;
		}
	}

	private void recordSolution() {
		//Check Time Windows
		if(canFailOnSolutionRecording) {
			for (int i = 0; i < jobs.length; i++) {
				if( ! taskvars[i].start().canBeInstantiatedTo(jobs[i].getEST()) || 
						! taskvars[i].end().canBeInstantiatedTo(jobs[i].getLCT())) {
					return;
				}
			}
		}
		backtrackStamp = problem.getSolver().getBackTrackCount();
		solutionStamp = problem.getSolver().getSolutionCount();
		try {
			vars[vars.length - 1].instantiate(vars[vars.length - 1].getInf(), this, false);
			for (int i = 0; i < jobs.length; i++) {
				taskvars[i].start().instantiate(jobs[i].getEST(), this, false);
				taskvars[i].end().instantiate(jobs[i].getLCT(), this, false);
				assert(taskvars[i].isScheduled());
			}
		} catch (ContradictionException e) {
			throw new SolverException("can't record solution because of time windows");
		}
	} 




private void recordUpperBound() {
	problem.getSolver().worldPushDuringPropagation();
	try {
		recordSolution();
		problem.getSolver().propagate();
		problem.getSolver().getSearchStrategy().recordSolution();
	} catch (ContradictionException e) {
		throw new SolverException("Unable to record new upper found.");
	}
	problem.getSolver().worldPopDuringPropagation();
}

@Override
public int getFilteredEventMask(int idx) {
	if(idx < startOffset) return IntVarEvent.INCINF_MASK;
	else if(idx < endOffset) return 0;
	else if(idx < taskIntVarOffset) return IntVarEvent.INCINF_MASK;
	else if(idx == vars.length - 1) return IntVarEvent.DECSUP_MASK;
	else return IntVarEvent.INSTINT_MASK;
}


static interface IRelaxationFilter {


	abstract PropagagationLevel getPropagagationLevel();

	abstract boolean isFeasibleSchedule();

	abstract boolean filterObjective() throws ContradictionException;

	abstract Boolean propagatePrecedence(ITJob j1, ITJob j2);

	abstract boolean sequenceSwaps();

	abstract void buildEventLists();

	abstract void clearEventLists();

	abstract boolean sweep();

	abstract boolean filterPrecedences();

	void flushUpdateLists() throws ContradictionException;

	void clearUpdateLists();
}

final class PmtnRelaxationFilter extends AbstractRelaxationFilter implements TObjectProcedure<SweepEvent> {

	private final SweepEvent[][] sweepEventMap;

	protected final TLinkedList<SweepEvent> sweepEventList = new TLinkedList<SweepEvent>();

	private final TLinkedList<SweepEvent> sweepCurrentList = new TLinkedList<SweepEvent>();

	public PmtnRelaxationFilter(PropagagationLevel propLevel) {
		super(propLevel);
		sweepEventMap = new SweepEvent[jobs.length][2];
		for (int i = 0; i < jobs.length; i++) {
			sweepEventMap[i][START] = new SweepEvent(i, true);
			sweepEventMap[i][END] = new SweepEvent(i, false);
		}
	}



	@Override
	public int doPropagate() {
		return Pmtn1Scheduler.schedule1Lmax(tempJobs);
	}


	@Override
	public boolean isFeasibleSchedule() {
		return ! JobUtils.isInterrupted(jobs);
	}


	@Override
	public void clearEventLists() {
		sweepCurrentList.clear();
		sweepEventList.clear();
	}


	@Override
	public void buildEventLists() {
		for (int i = 0; i < jobs.length; i++) {
			sweepEventMap[i][START].setCoordinate(jobs[i].getEST());
			sweepEventList.add(sweepEventMap[i][START]);
			sweepEventMap[i][END].setCoordinate(jobs[i].getLCT());
			sweepEventList.add(sweepEventMap[i][END]);
		}
		TCollections.sort(sweepEventList);
	}

	private SweepEvent evt;

	@Override
	public final boolean sweep() {
		sequenceSwaps();
		if( ! sweepEventList.isEmpty()) {
			assert sweepEventList.getFirst().isStartEvent();
			do {
				final SweepEvent evt = sweepEventList.removeFirst();
				if(evt.isStartEvent()) {
					sweepCurrentList.add(evt);
				} else {
					sweepCurrentList.remove(sweepEventMap[evt.index][START]);
					if ( ! sweepCurrentList.forEachValue(this)) {
						return true;
					}
				}
			}while(! sweepEventList.isEmpty());
		}
		return false;
	}

	@Override
	public boolean execute(SweepEvent evt2) {
		return propagatePrecedence(jobs[evt.index], jobs[evt2.index]) != Boolean.TRUE;
	}

}


final class PrecRelaxationFilter extends AbstractRelaxationFilter {

	public PrecRelaxationFilter(PropagagationLevel propLevel) {
		super(propLevel);
	}


	@Override
	public boolean sweep() {
		return sequenceSwaps();
	}


	@Override
	public int doPropagate() {
		return PDR1Scheduler.schedule1PrecLmax(tempJobs);
	}

	@Override
	public boolean isFeasibleSchedule() {
		return JobUtils.isScheduledInTimeWindows(jobs);
	}

	@Override
	public void clearEventLists() {}


	@Override
	public void buildEventLists() {}




}


abstract class AbstractRelaxationFilter implements IRelaxationFilter {


	private final PropagagationLevel propLevel;

	protected final ITJob[] jobSequence;

	private int newUpperBound = MAX_UPPER_BOUND;

	private final ArrayList<ITemporalSRelation> forwardUpdateList = new ArrayList<ITemporalSRelation>();

	private final ArrayList<ITemporalSRelation> backwardUpdateList = new ArrayList<ITemporalSRelation>();

	public AbstractRelaxationFilter(PropagagationLevel level) {
		super();
		this.propLevel = level;
		jobSequence = Arrays.copyOf(jobs, jobs.length);
	}


	@Override
	public final PropagagationLevel getPropagagationLevel() {
		return propLevel;
	}


	public final void flushUpdateLists() throws ContradictionException {
		vars[vars.length-1].updateSup(newUpperBound, RelaxLmaxConstraint.this, false);
		for (ITemporalSRelation rel : forwardUpdateList) {
			rel.getDirection().instantiate(1, RelaxLmaxConstraint.this, false);
			//LOGGER.info("u "+rel.toString());
		}
		for (ITemporalSRelation rel : backwardUpdateList) {
			rel.getDirection().instantiate(0, RelaxLmaxConstraint.this, false);
			//LOGGER.info("u "+rel.toString());
		}
		clearUpdateLists();
	}



	public final void clearUpdateLists() {
		newUpperBound = MAX_UPPER_BOUND;
		forwardUpdateList.clear();
		backwardUpdateList.clear();
	}

	public abstract int doPropagate();

	@Override
	public final Boolean propagatePrecedence(ITJob j1, ITJob j2) {
		j1.addSuccessor(j2);
		JobUtils.resetSchedule(tempJobs);
		final int cost = doPropagate();
		j1.removeSuccessor(j2);
		if(cost > vars[vars.length-1].getSup()) {
			final int idx1 = j1.getID();
			final int idx2 = j2.getID();
			if( disjSMod.containsConstraint(idx1, idx2)) {
				backwardUpdateList.add(disjSMod.getConstraint(idx1, idx2));
			} else {
				forwardUpdateList.add(disjSMod.getConstraint(idx2, idx1));
			}
		} else if(isFeasibleSchedule() ){
			if(cost == vars[vars.length-1].getInf()) {
				//an optimal solution has been found
				return Boolean.TRUE;
			} else {
				//else a new upper bound has been found
				if(newUpperBound > cost ) {
					newUpperBound = cost;
					// FIXME - How to record an upper bound ? - created 14 avr. 2012 by A. Malapert
					//recordUpperBound();
				}
			} 
		} //else a unfeasible schedule has been found
		return null;

	}





	@Override
	public final boolean sequenceSwaps() {
		//On ne peut pas vérifier toutes les disjonctions, 
		//on ne peut pas être certain que la propagation des clauses de transitivité a eu lieu
		// par contre on peut inverser des paires de taches consecutives ?
		//			for (ITemporalSRelation rel : disjunctList) {
		//				if( ! rel.isFixed()) {
		//					//System.out.println(rel);
		//					ITJob j1 = jobs[rel.getOrigin().getID()];
		//					ITJob j2 = jobs[rel.getDestination().getID()];
		//					Boolean b = propagatePrecedence(j1, j2);
		//					if(b == null) {
		//						b = propagatePrecedence(j2, j1);
		//					}
		//					if (b == Boolean.TRUE) {
		//						return true;
		//					}
		//				}
		//			}
		//Par contre, on peut inverser deux jobs consécutifs quand ils sont triés par date de début (même avec préemption)
		//on ne peut pas se heurter au problème de transitivité (au plus une precedence entre les deux taches)
		for (int i = 1; i < jobSequence.length; i++) {
			final int pred = jobSequence[i-1].getID();
			final int succ = jobSequence[i].getID();
			final ITemporalSRelation rel = disjSMod.getEdgeConstraint(pred, succ);
			if( rel != null && //Model stated precedence
					! rel.isFixed()) { //Solver fixed precedence
				//System.out.println(rel);
				Boolean b = propagatePrecedence(jobSequence[i], jobSequence[i-1]);
				if (b == Boolean.TRUE) {
					return true;
				}
			}
		}
		return false;
	}



	@Override
	public final boolean filterObjective() throws ContradictionException {
		if(propLevel.isOn()) {
			JobUtils.resetSchedule(tempJobs);
			final int lb = doPropagate();
			//LOGGER.info("LB "+lb + " -> "+vars[vars.length-1].pretty());
			vars[vars.length-1].updateInf(lb, RelaxLmaxConstraint.this, false);
			if(propLevel == PropagagationLevel.SWEEP) {
				Arrays.sort(jobSequence, TaskComparators.makeEarliestStartingTimeCmp());
				clearEventLists();
				buildEventLists();	
			} else if (propLevel == PropagagationLevel.SWAP) {
				Arrays.sort(jobSequence, TaskComparators.makeEarliestStartingTimeCmp());
			}
			return isFeasibleSchedule();
		}
		return false;
	}

	@Override
	public final boolean filterPrecedences() {
		switch (propLevel) {
		case SWEEP : return sweep();
		case SWAP: return sequenceSwaps();
		default:return false;
		}
	}

}



}

final class SweepEvent extends TLinkableAdapter implements Comparable<SweepEvent> {

	private static final long serialVersionUID = -3804084435189896556L;

	public final int index;

	protected int coordinate;

	private final boolean startEvent;

	public SweepEvent(int index, boolean startEvent) {
		super();
		this.index = index;
		this.startEvent = startEvent;
	}

	public final int getIndex() {
		return index;
	}

	public final int getCoordinate() {
		return coordinate;
	}

	public final void setCoordinate(int coordinate) {
		this.coordinate = coordinate;
	}

	public final boolean isStartEvent() {
		return startEvent;
	}

	@Override
	public int compareTo(SweepEvent o) {
		return index < o.index ? -1 : index == o.index ? (startEvent ? -1 : o.startEvent ? 1 : 0) : 1;
	}



}
