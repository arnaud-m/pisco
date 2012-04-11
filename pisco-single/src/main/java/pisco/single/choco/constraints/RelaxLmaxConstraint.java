package pisco.single.choco.constraints;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntProcedure;
import gnu.trove.TLinkableAdapter;
import gnu.trove.TLinkedList;
import gnu.trove.TObjectProcedure;

import java.util.Arrays;
import java.util.Collections;

import pisco.common.ITJob;
import pisco.common.JobUtils;
import pisco.common.PDR1Scheduler;
import pisco.common.PJob;
import pisco.common.Pmtn1Scheduler;
import pisco.single.Abstract1MachineProblem;
import pisco.single.SingleMachineSettings.PropagagationLevel;
import choco.cp.solver.constraints.global.scheduling.precedence.ITemporalSRelation;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.global.scheduling.AbstractTaskSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;

public class RelaxLmaxConstraint extends AbstractTaskSConstraint {


	protected final Abstract1MachineProblem problem;

	protected final ITJob[] jobs;

	protected final ITJob[] schedule;

	private TIntArrayList[] precReductionGraph;

	//	private StoredBipartiteSet<ITemporalSRelation> disjunctList;
	//
	//	private IStateBool fireDisjunctList;

	private ITemporalSRelation[] disjunctList;

	private final static int START = 0;
	private final static int END = 1;

	private PropagagationLevel pmtnLevel;

	private PropagagationLevel precLevel;

	private SweepEvent[][] sweepEventMap;

	protected final TLinkedList<SweepEvent> sweepEventList = new TLinkedList<SweepEvent>();

	protected final TLinkedList<SweepEvent> sweepCurrentList = new TLinkedList<SweepEvent>();

	protected final TLinkedList<SweepEvent> sweepEndingList = new TLinkedList<SweepEvent>();

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
		super.awake();
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


	private void buildJobs() {
		for (int i = 0; i < taskvars.length; i++) {
			jobs[i].resetSchedule();
			jobs[i].resetPrecedences();
			jobs[i].setReleaseDate(taskvars[i].getEST());
			jobs[i].setDueDate(problem.jobs[i].getDueDate());
			jobs[i].setDeadline(taskvars[i].getLCT());
		}
	}


	private void buildPrecedence() {
		//Add Reduced Precedence to jobs
		for (int i = 0; i < precReductionGraph.length; i++) {
			addSuccessors(i);
		}
		// FIXME - Brute force implementation (not incremental at all) - created 10 avr. 2012 by A. Malapert
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
	}

	private void buildPmtnEventLists() {
		sweepEventList.clear();
		for (int i = 0; i < jobs.length; i++) {
			if(jobs[i].isInterrupted()) {
				sweepEventMap[i][START].setCoordinate(jobs[i].getEST());
				sweepEventList.add(sweepEventMap[i][START]);
				sweepEventMap[i][END].setCoordinate(jobs[i].getLCT());
				sweepEventList.add(sweepEventMap[i][END]);
			} 
		}
		Collections.sort(sweepEventList);
	}


	private void buildPrecEventLists() {
		sweepEventList.clear();
		for (int i = 0; i < jobs.length; i++) {
			if(! jobs[i].isScheduledInTimeWindow()) {
				sweepEventMap[i][START].setCoordinate(jobs[i].getReleaseDate());
				sweepEventList.add(sweepEventMap[i][START]);
				sweepEventMap[i][END].setCoordinate(jobs[i].getDeadline());
				sweepEventList.add(sweepEventMap[i][END]);
			}

		}
		Collections.sort(sweepEventList);
	}


	private abstract class AbstractSweepProcedure implements TObjectProcedure<SweepEvent> {

		public SweepEvent evt1;

	}

	private final class SweepPmtnProcedure extends AbstractSweepProcedure {

		@Override
		public boolean execute(final SweepEvent evt2) {
			return tryDisjunct(evt1, evt2);
		}
	}

	private final class SweepPrecProcedure extends AbstractSweepProcedure {

		@Override
		public boolean execute(final SweepEvent evt2) {
			return tryDisjunct(evt1, evt2);
		}
	}

	private final SweepPmtnProcedure sweepPmtnProcedure=  new SweepPmtnProcedure();

	private final SweepPrecProcedure sweepPrecProcedure=  new SweepPrecProcedure();

	private void pmtnBruteForce() {
		for (ITemporalSRelation rel : disjunctList) {
			if( ! rel.isFixed()) {
				ITJob j1 = jobs[rel.getOrigin().getID()];
				ITJob j2 = jobs[rel.getDestination().getID()];
				propagatePmtnPrecedence(j1, j2);
				propagatePmtnPrecedence(j2, j1);
			}
		}
	}


	private void precBruteForce() {
		for (ITemporalSRelation rel : disjunctList) {
			if( ! rel.isFixed()) {
				ITJob j1 = jobs[rel.getOrigin().getID()];
				ITJob j2 = jobs[rel.getDestination().getID()];
				propagatePrecPrecedence(j1, j2);
				propagatePrecPrecedence(j2, j1);
			}
		}
	}

	private void sweep(AbstractSweepProcedure procedure) {
		sweepCurrentList.clear();
		sweepEndingList.clear();
		SweepEvent evt = sweepEventList.removeFirst();
		assert evt.isStartEvent();
		sweepCurrentList.add(evt);
		int coordinate = evt.coordinate;
		do {
			if(evt.coordinate > coordinate) {
				coordinate = evt.coordinate;
				while( ! sweepEndingList.isEmpty()) {
					procedure.evt1 = sweepEndingList.removeFirst();
					sweepCurrentList.forEachValue(procedure);
					sweepEndingList.forEachValue(procedure);
				}
			}
			if(evt.isStartEvent()) {
				sweepCurrentList.add(evt);
			} else {
				sweepCurrentList.remove(sweepEventMap[evt.index][START]);
				sweepEndingList.add(evt);
			}
		}while((evt = sweepEventList.removeFirst()) != null);
	}


	private final boolean propagatePmtnPrecedence(ITJob origin, ITJob destination) {
		origin.addSuccessor(destination);
		ChocoLogging.getBranchingLogger().info(origin + " -> "+ destination);
		return true;
		// TODO - remove succ. - created 11 avr. 2012 by A. Malapert
	}

	private final boolean propagatePrecPrecedence(ITJob origin, ITJob destination) {
		origin.addSuccessor(destination);
		ChocoLogging.getBranchingLogger().info(origin + " -> "+ destination);
		return true;
		// TODO - remove succ. - created 11 avr. 2012 by A. Malapert
	}


	private final boolean tryDisjunct(SweepEvent evt1, SweepEvent evt2) {
		ChocoLogging.getBranchingLogger().info(jobs[evt1.index] + " -> "+ jobs[evt2.index]);
		return true;
	}

	@Override
	public void propagate() throws ContradictionException {
		buildJobs();
		buildPrecedence();
		//Modify Due Dates 
		// FIXME - Remove this step in further version by stating dedicated constraints  - created 6 avr. 2012 by A. Malapert
		JobUtils.modifyDueDates(schedule);
		////////////////
		// Preemptive relaxation
		switch (pmtnLevel) {
		case OBJ:{
			//Preemptive Relaxation
			final int lb = Pmtn1Scheduler.schedule1Lmax(schedule);
			vars[vars.length-1].updateInf(lb, this, false);
			buildPmtnEventLists();
			if(sweepEventList.isEmpty()) {
				recordSolution();
			}
			//continue ...
		}
		case SWEEP : {
			buildPmtnEventLists();
			sweep(sweepPmtnProcedure);
			break;
		}			
		case FULL: { 
			pmtnBruteForce();
			break;
		}
		default:
			break;
		}

		//////////////
		// Precedence relaxation
		switch (precLevel) {
		case OBJ:{
			//Preemptive Relaxation
			final int lb = PDR1Scheduler.schedule1PrecLmax(schedule);
			vars[vars.length-1].updateInf(lb, this, false);
			buildPrecEventLists();
			if(sweepEventList.isEmpty()) {
				recordSolution();
			}
			//continue ...
		}
		case SWEEP : {
			buildPrecEventLists();
			sweep(sweepPmtnProcedure);
			break;
		}			
		case FULL: { 
			precBruteForce();
			break;
		}
		default:
			break;
		}
	}

	private void recordSolution() {

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

		abstract boolean bruteForce();

		abstract void buildEventLists();

		abstract boolean sweep();

		abstract boolean filter() throws ContradictionException;


	}


	class PmtnRelaxationFilter extends AbstractRelaxationFilter {

		public PmtnRelaxationFilter(PropagagationLevel propLevel) {
			super(propLevel);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void filterObjective() throws ContradictionException {
			JobUtils.resetSchedule(schedule);
			final int lb = Pmtn1Scheduler.schedule1Lmax(schedule);
			vars[vars.length-1].updateInf(lb, RelaxLmaxConstraint.this, false);
		}

		@Override
		public boolean propagatePrecedence(ITJob j1, ITJob j2) {
			j1.addSuccessor(j2);
			JobUtils.resetSchedule(schedule);
			final int lb = Pmtn1Scheduler.schedule1Lmax(schedule);
			j1.removeSuccessor(j2);
			if(lb > vars[vars.length-1].getSup()) {
				
				
			}
			return true;
		}

		@Override
		public void buildEventLists() {
			sweepEventList.clear();
			for (int i = 0; i < jobs.length; i++) {
				if(jobs[i].isInterrupted()) {
					sweepEventMap[i][START].setCoordinate(jobs[i].getEST());
					sweepEventList.add(sweepEventMap[i][START]);
					sweepEventMap[i][END].setCoordinate(jobs[i].getLCT());
					sweepEventList.add(sweepEventMap[i][END]);
				} 
			}
			Collections.sort(sweepEventList);			
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

		public AbstractRelaxationFilter(PropagagationLevel propLevel) {
			super();
			this.propLevel = propLevel;
		}


		@Override
		public PropagagationLevel getPropagagationLevel() {
			return propLevel;
		}


		@Override
		public boolean bruteForce() {
			for (ITemporalSRelation rel : disjunctList) {
				if( ! rel.isFixed()) {
					ITJob j1 = jobs[rel.getOrigin().getID()];
					ITJob j2 = jobs[rel.getDestination().getID()];
					propagatePrecedence(j1, j2);
					propagatePrecedence(j2, j1);
				}
			}
			return true;
		}



		@Override
		public boolean sweep() {
			sweepCurrentList.clear();
			sweepEndingList.clear();
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
			switch (propLevel) {
			case OBJ:{
				//Preemptive Relaxation
				filterObjective();
				buildEventLists();
				if(sweepEventList.isEmpty()) {
					recordSolution();
				}
				//continue ...
			}
			case SWEEP : {
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
			return true;
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
