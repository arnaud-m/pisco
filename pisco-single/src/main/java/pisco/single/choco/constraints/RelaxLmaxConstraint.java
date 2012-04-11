package pisco.single.choco.constraints;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntProcedure;
import gnu.trove.TLinkableAdapter;
import gnu.trove.TLinkedList;
import gnu.trove.TObjectProcedure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.global.scheduling.AbstractTaskSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;

public class RelaxLmaxConstraint extends AbstractTaskSConstraint {


	protected final Abstract1MachineProblem problem;

	protected final ITJob[] jobs;

	protected final ITJob[] schedule;

	private DisjunctiveSModel disjSMod;

	private TIntArrayList[] precReductionGraph;

	private ITemporalSRelation[] disjunctList;

	private final static int START = 0;
	private final static int END = 1;

	private int solutionStamp = -1;
	
	private int backtrackStamp = -1;
	
	private final IRelaxationFilter pmtnRelaxation;

	private final IRelaxationFilter precRelaxation;

	private SweepEvent[][] sweepEventMap;

	private final TLinkedList<SweepEvent> sweepEventList = new TLinkedList<SweepEvent>();

	private final TLinkedList<SweepEvent> sweepCurrentList = new TLinkedList<SweepEvent>();

	private final TLinkedList<SweepEvent> sweepEndingList = new TLinkedList<SweepEvent>();

	public RelaxLmaxConstraint(Abstract1MachineProblem problem, TaskVar[] taskvars, IntDomainVar[] disjuncts, IntDomainVar lmax) {
		super(taskvars, disjuncts, lmax);
		this.problem = problem;
		jobs = new ITJob[taskvars.length];
		sweepEventMap = new SweepEvent[jobs.length][2];
		for (int i = 0; i < jobs.length; i++) {
			jobs[i] = new PJob(taskvars[i].getID());
			jobs[i].setDuration(problem.jobs[i].getDuration());
			assert(taskvars[i].getID() == i && taskvars[i].getID() == problem.jobs[i].getID());
			sweepEventMap[i][START] = new SweepEvent(i, true);
			sweepEventMap[i][END] = new SweepEvent(i, false);
		}
		schedule = Arrays.copyOf(jobs, jobs.length);

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
		JobUtils.modifyDueDates(schedule);
		////////////////
		if( ! pmtnRelaxation.filter()) {
			precRelaxation.filter();
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

	private void recordSolution() throws ContradictionException {
		backtrackStamp = problem.getSolver().getBackTrackCount();
		solutionStamp = problem.getSolver().getSolutionCount();
		
		for (int i = 0; i < jobs.length; i++) {
			taskvars[i].start().instantiate(jobs[i].getEST(), this, false);
			taskvars[i].end().instantiate(jobs[i].getLCT(), this, false);
			assert(taskvars[i].isScheduled());
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


	static interface IRelaxationFilter extends TObjectProcedure<SweepEvent> {


		abstract PropagagationLevel getPropagagationLevel();

		abstract void filterObjective() throws ContradictionException;

		abstract boolean propagatePrecedence(ITJob j1, ITJob j2);

		abstract boolean bruteForce()throws ContradictionException;

		abstract void buildEventLists();

		abstract boolean sweep() throws ContradictionException;

		abstract boolean filter() throws ContradictionException;


	}

	final class PmtnRelaxationFilter extends AbstractRelaxationFilter {

		public PmtnRelaxationFilter(PropagagationLevel propLevel) {
			super(propLevel);
		}


		@Override
		public int doPropagate() {
			return Pmtn1Scheduler.schedule1Lmax(schedule);
		}

		@Override
		public void buildEventLists() {
			for (int i = 0; i < schedule.length; i++) {
				if( schedule[i].isInterrupted() ){
					sweepEventMap[schedule[i].getID()][START].setCoordinate(schedule[i].getEST());
					sweepEventList.add(sweepEventMap[i][START]);
					sweepEventMap[schedule[i].getID()][END].setCoordinate(schedule[i].getLCT());
					sweepEventList.add(sweepEventMap[i][END]);
				} 
			}
		}

		@Override
		public boolean execute(SweepEvent evt2) {
			// TODO Auto-generated method stub
			return false;
		}

	}


	final class PrecRelaxationFilter extends AbstractRelaxationFilter {

		public PrecRelaxationFilter(PropagagationLevel propLevel) {
			super(propLevel);
		}


		@Override
		public int doPropagate() {
			return PDR1Scheduler.schedule1PrecLmax(schedule);
		}

		@Override
		public void buildEventLists() {
			for (int i = 0; i < schedule.length; i++) {
				if( ! schedule[i].isScheduledInTimeWindow() ){
					sweepEventMap[schedule[i].getID()][START].setCoordinate(schedule[i].getReleaseDate());
					sweepEventList.add(sweepEventMap[i][START]);
					sweepEventMap[schedule[i].getID()][END].setCoordinate(schedule[i].getDeadline());
					sweepEventList.add(sweepEventMap[i][END]);
				} 
			}
		}

		@Override
		public boolean execute(SweepEvent evt2) {
			// TODO Auto-generated method stub
			return false;
		}

	}


	abstract class AbstractRelaxationFilter implements IRelaxationFilter {


		protected SweepEvent evt1;

		private final PropagagationLevel propLevel;

		private final ArrayList<ITemporalSRelation> forwardUpdateList = new ArrayList<ITemporalSRelation>();

		private final ArrayList<ITemporalSRelation> backwardUpdateList = new ArrayList<ITemporalSRelation>();

		public AbstractRelaxationFilter(PropagagationLevel level) {
			super();
			this.propLevel = level;
		}


		@Override
		public final PropagagationLevel getPropagagationLevel() {
			return propLevel;
		}
		@Override
		public void filterObjective() throws ContradictionException {
			JobUtils.resetSchedule(schedule);
			final int lb = doPropagate();
			//LOGGER.info("LB "+lb + " -> "+vars[vars.length-1].pretty());
			vars[vars.length-1].updateInf(lb, RelaxLmaxConstraint.this, false);
		}

		public abstract int doPropagate();


		public final void flushUpdateLists() throws ContradictionException {
			for (ITemporalSRelation rel : forwardUpdateList) {
				rel.getDirection().instantiate(1, RelaxLmaxConstraint.this, false);
				jobs[rel.getOrigin().getID()].addSuccessor(jobs[rel.getDestination().getID()]);
			}

			for (ITemporalSRelation rel : backwardUpdateList) {
				rel.getDirection().instantiate(0, RelaxLmaxConstraint.this, false);
				jobs[rel.getOrigin().getID()].addPredecessor(jobs[rel.getDestination().getID()]);
			}
		}



		@Override
		public final boolean propagatePrecedence(ITJob j1, ITJob j2) {
			j1.addSuccessor(j2);
			JobUtils.resetSchedule(schedule);
			final int lb = doPropagate();
			j1.removeSuccessor(j2);
			if(lb > vars[vars.length-1].getSup()) {
				final int idx1 = j1.getID();
				final int idx2 = j2.getID();
				if( disjSMod.containsConstraint(idx1, idx2)) {
					backwardUpdateList.add(disjSMod.getConstraint(idx1, idx2));
				} else {
					forwardUpdateList.add(disjSMod.getConstraint(idx2, idx1));
				}

			} else if(! JobUtils.isInterrupted(schedule) ){
				System.err.println("Not yet implemented");
				// TODO - Is a solution ? - created 11 avr. 2012 by A. Malapert
			}
			return true;

		}


		@Override
		public final boolean bruteForce() throws ContradictionException {
			for (ITemporalSRelation rel : disjunctList) {
				if( ! rel.isFixed()) {
					System.out.println(rel);
					ITJob j1 = jobs[rel.getOrigin().getID()];
					ITJob j2 = jobs[rel.getDestination().getID()];
					if ( ! propagatePrecedence(j1, j2) || ! propagatePrecedence(j2, j1)) {
						flushUpdateLists();
					}
				}
			}
			return true;
		}


		@Override
		public final boolean sweep() throws ContradictionException {
			SweepEvent evt = sweepEventList.removeFirst();
			assert evt.isStartEvent();
			sweepCurrentList.add(evt);
			int coordinate = evt.coordinate;
			do {
				if(evt.coordinate > coordinate) {
					coordinate = evt.coordinate;
					while( ! sweepEndingList.isEmpty()) {
						evt1 = sweepEndingList.removeFirst();
						sweepCurrentList.forEachValue(this);
						sweepEndingList.forEachValue(this);
						flushUpdateLists();
					}
				}
				if(evt.isStartEvent()) {
					sweepCurrentList.add(evt);
				} else {
					sweepCurrentList.remove(sweepEventMap[evt.index][START]);
					sweepEndingList.add(evt);
				}
			}while((evt = sweepEventList.removeFirst()) != null);
			return false;
		}


		@Override
		public boolean filter() throws ContradictionException {
			if(propLevel.isOn()) {
				filterObjective();
				sweepCurrentList.clear();
				sweepEndingList.clear();
				sweepEventList.clear();
				buildEventLists();
				if(sweepEventList.isEmpty()) {
					recordSolution();
					return true;

				} 
				switch (propLevel) {
				case SWEEP : {
					TCollections.sort(sweepEventList);
					sweep();
					break;
				}			
				case FULL: { 
					bruteForce();
					break;
				}
				default:
					break;
				}
			}
			return false;
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
