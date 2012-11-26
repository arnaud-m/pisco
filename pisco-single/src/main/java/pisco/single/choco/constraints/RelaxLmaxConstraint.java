package pisco.single.choco.constraints;

import static choco.cp.common.util.preprocessor.detector.scheduling.DisjunctiveSModel.*;
import static choco.Choco.MAX_UPPER_BOUND;
import static choco.Choco.MIN_LOWER_BOUND;
import static pisco.common.JobUtils.modifyDueDates;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntProcedure;
import gnu.trove.TLinkableAdapter;
import gnu.trove.TLinkedList;
import gnu.trove.TObjectProcedure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;

import pisco.common.ITJob;
import pisco.common.JobUtils;
import pisco.common.PDR1Scheduler;
import pisco.common.PJob;
import pisco.common.Pmtn1Scheduler;
import pisco.common.PDR1Scheduler.Proc1PrecLmax;
import pisco.common.Pmtn1Scheduler.Proc1Lmax;
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
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.global.scheduling.AbstractTaskSConstraint;
import choco.kernel.solver.search.integer.IntVarValPair;
import choco.kernel.solver.search.integer.VarValPairSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;
import choco.kernel.visu.VisuFactory;

public class RelaxLmaxConstraint extends AbstractTaskSConstraint implements VarValPairSelector {

	public static boolean canFailOnSolutionRecording = false;

	protected final Abstract1MachineProblem problem;

	private final int[] savedDueDates;

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

		savedDueDates = problem.getConfiguration().readBoolean(SingleMachineSettings.MODIFY_DUE_DATES) ? null : new int[jobs.length];
		//savedDueDates = new int[jobs.length];


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
	public void awakeOnSup(int varIdx) throws ContradictionException {
		this.constAwake(false);
	}


	//	@Override
	//	public Boolean isEntailed() {
	//		return super.isEntailed();
	//	}

	@SuppressWarnings("unchecked")
	@Override
	public void awake() throws ContradictionException {
		PreProcessCPSolver ppsolver = ( (PreProcessCPSolver) problem.getSolver());
		disjSMod = ppsolver.getDisjSModel();
		//Generate Precedence Graph
		BitSet[] graph = disjSMod.generatePrecGraph();
		//Compute and propagate Transitive Closure
		floydMarshallClosure(graph);
		BitSet[] toPropagate = copy(graph);
		andNot(toPropagate, graph);
		for (int i = 0; i < toPropagate.length; i++) {
			//From currently fixed disjunctions
			for (int j = toPropagate[i].nextSetBit(0); j >= 0; j = toPropagate[i]
					.nextSetBit(j + 1)) {
				if(disjSMod.containsEdge(i, j)) {
					final ITemporalSRelation rel = disjSMod.getConstraint(i, j);
					if(rel == null) {
						disjSMod.getConstraint(j, i).getDirection().instantiate(0, this, false);
					} else {
						rel.getDirection().instantiate(1, this, false);
					}
				}
			}
		}
		//Compute and convert reduction graph for further uses
		floydMarshallReduction(graph);
		precReductionGraph = convertToLists(graph);
		disjunctList = disjSMod.getEdges();
		super.awake();
	}

	private void buildJobs() {
		for (int i = 0; i < taskvars.length; i++) {
			jobs[i].resetSchedule();
			jobs[i].resetPrecedences();
			jobs[i].setReleaseDate(taskvars[i].getEST());
			//jobs[i].setDueDate(jobs[i].getDueDate());
			jobs[i].setDueDate(vars[taskIntVarOffset + i].getSup());
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

		// FIXME - Brute force (not incremental at all) - created 10 avr. 2012 by A. Malapert
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
		//VisuFactory.getDotManager().show(new DottyBean(jobs));
		//Modify Due Dates
		//LOGGER.info(vars[vars.length-1].pretty());
		//modifyDueDates(tempJobs);
		if(savedDueDates != null) {
			modifyDueDates(tempJobs);
			for (int i = 0; i < jobs.length; i++) {
				savedDueDates[i] = jobs[i].getDueDate();
			}
		} //else all (due date) constraints are not always revised and therefore some due dates entirely modified
		////////////////
		if(pmtnRelaxation.filterObjective() || precRelaxation.filterObjective()  ||
				pmtnRelaxation.filterPrecedences() || precRelaxation.filterPrecedences()) {
			//an optimal solution has been found during propagation
			recordSolution();
		} else {
			pmtnRelaxation.flushUpdateLists();
			precRelaxation.flushUpdateLists();
		}

	}


	@Override
	public IntVarValPair selectVarValPair() throws ContradictionException {
		buildJobs();
		buildPrecedence();
		//VisuFactory.getDotManager().show(new DottyBean(jobs));
		//Modify Due Dates
		modifyDueDates(tempJobs);
		final ITemporalSRelation rel = pmtnRelaxation.getBestDecision();
		return rel == null ? null : new IntVarValPair(rel.getDirection(), 
				jobs[rel.getOrigin().getID()].getEST() < jobs[rel.getDestination().getID()].getEST() ? 1 : 0);
		
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
		setEntailed();
	} 




	private void recordUpperBound() {
		//	problem.getSolver().worldPushDuringPropagation();
		//	try {
		//		recordSolution();
		//		problem.getSolver().propagate();
		//		problem.getSolver().getSearchStrategy().recordSolution();
		//	} catch (ContradictionException e) {
		//		throw new SolverException("Unable to record new upper found.");
		//	}
		//	problem.getSolver().worldPopDuringPropagation();
	}



	private void restoreDueDates() {
		if(savedDueDates == null) {
			for (int i = 0; i < taskvars.length; i++) {
				jobs[i].setDueDate(vars[taskIntVarOffset + i].getSup());
			}
		} else {
			for (int i = 0; i < taskvars.length; i++) {
				jobs[i].setDueDate(savedDueDates[i]);
			}	
		}
	}



	@Override
	public int getFilteredEventMask(int idx) {
		if(idx < startOffset) return IntVarEvent.INCINF_MASK;
		else if(idx < endOffset) return 0;
		else if(idx < taskIntVarOffset) return IntVarEvent.INCINF_MASK;
		else if(idx < taskIntVarOffset + taskvars.length) return IntVarEvent.DECSUP;
		else if(idx == vars.length - 1) return IntVarEvent.DECSUP_MASK;
		else return IntVarEvent.INSTINT_MASK;
	}


	final class PmtnRelaxationFilter extends AbstractRelaxationFilter implements TObjectProcedure<SweepEvent> {

		private final Proc1Lmax procedure = new Proc1Lmax();
		public PmtnRelaxationFilter(PropagagationLevel propLevel) {
			super(propLevel);
		}

		@Override
		public int doPropagate() {
			return Pmtn1Scheduler.schedule1PrecLmax(tempJobs, procedure);
		}


		@Override
		public boolean isFeasibleSchedule() {
			return ! JobUtils.isInterrupted(jobs);
		}


	}


	final class PrecRelaxationFilter extends AbstractRelaxationFilter {

		private final Proc1PrecLmax procedure = new Proc1PrecLmax();

		public PrecRelaxationFilter(PropagagationLevel propLevel) {
			super(propLevel);
		}



		@Override
		public int doPropagate() {
			return PDR1Scheduler.schedule1PrecLmax(tempJobs, procedure);
		}

		@Override
		public boolean isFeasibleSchedule() {
			return JobUtils.isScheduledInTimeWindows(jobs);
		}


		@Override
		public boolean filterPrecedences() {
			switch (propLevel) {
			case SWAP: return swap();
			case SWEEP : return swap();
			default:return false;
			}
		}
	}


	abstract class AbstractRelaxationFilter implements IRelaxationFilter, TObjectProcedure<SweepEvent> {


		public final PropagagationLevel propLevel;

		private int lowerBound = MIN_LOWER_BOUND;
		private int upperBound = MAX_UPPER_BOUND;

		private final ArrayList<ITemporalSRelation> forwardUpdateList = new ArrayList<ITemporalSRelation>();

		private final ArrayList<ITemporalSRelation> backwardUpdateList = new ArrayList<ITemporalSRelation>();

		private final SweepEvent[][] sweepEventMap;

		private final TLinkedList<SweepEvent> sweepEventList = new TLinkedList<SweepEvent>();

		private final TLinkedList<SweepEvent> sweepCurrentList = new TLinkedList<SweepEvent>();

		public AbstractRelaxationFilter(PropagagationLevel level) {
			super();
			this.propLevel = level;
			sweepEventMap = new SweepEvent[jobs.length][2];
			for (int i = 0; i < jobs.length; i++) {
				sweepEventMap[i][START] = new SweepEvent(i, true);
				sweepEventMap[i][END] = new SweepEvent(i, false);
			}
		}


		@Override
		public final PropagagationLevel getPropagagationLevel() {
			return propLevel;
		}


		public final void flushUpdateLists() throws ContradictionException {
			vars[vars.length-1].updateSup(upperBound, RelaxLmaxConstraint.this, false);
			for (ITemporalSRelation rel : forwardUpdateList) {
				rel.getDirection().instantiate(1, RelaxLmaxConstraint.this, false);
				//LOGGER.info("f "+rel.toString());
			}
			for (ITemporalSRelation rel : backwardUpdateList) {
				rel.getDirection().instantiate(0, RelaxLmaxConstraint.this, false);
				//LOGGER.info("b "+rel.toString());
			}
			//ChocoLogging.flushLogs();
			clearUpdateLists();
		}



		public final void clearUpdateLists() {
			lowerBound = MIN_LOWER_BOUND;
			upperBound = MAX_UPPER_BOUND;
			forwardUpdateList.clear();
			backwardUpdateList.clear();
		}


		@Override
		public final void clearEventLists() {
			sweepCurrentList.clear();
			sweepEventList.clear();
		}


		@Override
		public final void buildEventLists() {
			for (int i = 0; i < jobs.length; i++) {
				sweepEventMap[i][START].setCoordinate(jobs[i].getEST());
				sweepEventList.add(sweepEventMap[i][START]);
				sweepEventMap[i][END].setCoordinate(jobs[i].getLCT());
				sweepEventList.add(sweepEventMap[i][END]);
			}
			TCollections.sort(sweepEventList);
		}


		public abstract int doPropagate();

		@Override
		public final boolean filterObjective() throws ContradictionException {
			if(propLevel.isOn()) {
				JobUtils.resetSchedule(tempJobs);
				lowerBound = doPropagate();
				//LOGGER.info("LB "+lb + " -> "+vars[vars.length-1].pretty());
				vars[vars.length-1].updateInf(lowerBound, RelaxLmaxConstraint.this, false);
				if(isFeasibleSchedule()) return true;
				else if(propLevel.ordinal() > PropagagationLevel.OBJ.ordinal()) {
					clearUpdateLists();
					clearEventLists();
					buildEventLists();	
				}
			}
			return false;
		}


		@Override
		public final Boolean propagatePrecedence(ITJob j1, ITJob j2) {
			//Add successor
			j1.addSuccessor(j2);
			modifyDueDates(j1, j2);
			JobUtils.resetSchedule(tempJobs);
			//Schedule
			final int cost = doPropagate();
			//Unset successor
			j1.removeSuccessor(j2);
			restoreDueDates();
			//Analyze results
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
					if(upperBound > cost ) {
						upperBound = cost;
						// FIXME - How to record an upper bound as solution ? - created 14 avr. 2012 by A. Malapert
						//recordUpperBound();
					}
				} 
			} //else a unfeasible schedule has been found
			return null;

		}


		@Override
		public final boolean swap() {
			if( ! sweepEventList.isEmpty()) {
				assert sweepEventList.getFirst().isStartEvent();
				SweepEvent pred = sweepEventList.getFirst();
				SweepEvent succ = pred;
				while( ( succ = sweepEventList.getNext(succ) ) != null) {
					if(succ.isStartEvent()) {
						final ITemporalSRelation rel = disjSMod.getEdgeConstraint(pred.index, succ.index);
						if( rel != null && //Model stated precedence
								! rel.isFixed()) { //Solver fixed precedence
							Boolean b = propagatePrecedence(jobs[succ.index], jobs[pred.index]);
							if (b == Boolean.TRUE) {
								return true;
							}
						}
						pred = succ;
					}
				}
			}
			return false;
		}

		//		@Override
		//		public boolean swap() {
		//			//On ne peut pas vérifier toutes les disjonctions, 
		//			//on ne peut pas être certain que la propagation des clauses de transitivité a eu lieu
		//			//Par contre, on peut inverser deux jobs consécutifs quand ils sont triés par date de début (même avec préemption)
		//			//on ne peut pas se heurter au problème de transitivité (au plus une precedence entre les deux taches)

		private SweepEvent evt;

		@Override
		public final boolean sweep() {
			if( ! sweepEventList.isEmpty()) {
				assert sweepEventList.getFirst().isStartEvent();
				do {
					evt = sweepEventList.removeFirst();
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
		public ITemporalSRelation getBestDecision() {
			//Re-propagate (safer to catch problem)
			JobUtils.resetSchedule(tempJobs);
			lowerBound = doPropagate();
			//LOGGER.info("LB "+lb + " -> "+vars[vars.length-1].pretty());
			try {
				vars[vars.length-1].updateInf(lowerBound, RelaxLmaxConstraint.this, false);
			} catch (ContradictionException e) {
				e.printStackTrace();
			}
			if(isFeasibleSchedule()) recordSolution();
			try {
				problem.getSolver().propagate();
			} catch (ContradictionException e) {
				e.printStackTrace();
			}
			clearUpdateLists();
			clearEventLists();
			buildEventLists();	
			////////
			ITemporalSRelation decision = null;
			int minDLateness = MIN_LOWER_BOUND;
			int maxDLateness = MIN_LOWER_BOUND;
			if( ! sweepEventList.isEmpty()) {
				assert sweepEventList.getFirst().isStartEvent();
				do {
					evt = sweepEventList.removeFirst();
					if(evt.isStartEvent()) {
						sweepCurrentList.add(evt);							
					} else {
						sweepCurrentList.remove(sweepEventMap[evt.index][START]);
						final Iterator<SweepEvent> iter = sweepCurrentList.iterator();
						int predLateness;
						int succLateness;
						while(iter.hasNext()) {
							final SweepEvent pevt = iter.next(); 
							predLateness = jobs[pevt.index].getLateness();
							succLateness = jobs[evt.index].getLateness();
							if(succLateness > predLateness && 
									succLateness > maxDLateness || 
									(succLateness == maxDLateness && predLateness > minDLateness) ) {
								minDLateness = predLateness;
								maxDLateness = succLateness;
								decision = disjSMod.getEdgeConstraint(taskvars[evt.index].getID(), taskvars[pevt.index].getID());
							} else if(		predLateness > maxDLateness || 
									(predLateness == maxDLateness && succLateness > minDLateness) ) {
								minDLateness = succLateness;
								maxDLateness = predLateness;
								decision = disjSMod.getEdgeConstraint(taskvars[evt.index].getID(), taskvars[pevt.index].getID());
							}
						}
					}
				}while(! sweepEventList.isEmpty());
			}

//			if(decision != null ) {
//				LOGGER.info(decision.toString());
//			}
			return decision;
		}


		@Override
		public final boolean execute(SweepEvent evt2) {
			return propagatePrecedence(jobs[evt2.index], jobs[evt.index]) != Boolean.TRUE 
					&& propagatePrecedence(jobs[evt.index], jobs[evt2.index]) != Boolean.TRUE;
			//return propagatePrecedence(jobs[evt2.index], jobs[evt.index]) != Boolean.TRUE;
		}

		@Override
		public boolean filterPrecedences() {
			assert ( forwardUpdateList.isEmpty() && backwardUpdateList.isEmpty());
			switch (propLevel) {
			case SWAP: return swap();
			case SWEEP : return swap() || sweep();
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
		return coordinate< o.coordinate? -1 : coordinate== o.coordinate ?  ( startEvent ? ( o.startEvent ? 0 : 1) : -1) : 1;
	}

	@Override
	public String toString() {
		return (startEvent ? "S" : "E" )+"("+coordinate + "," + index + ")";
	}



}



interface IRelaxationFilter {


	abstract PropagagationLevel getPropagagationLevel();

	abstract boolean isFeasibleSchedule();

	abstract boolean filterObjective() throws ContradictionException;

	abstract Boolean propagatePrecedence(ITJob j1, ITJob j2);

	abstract boolean swap();

	abstract void buildEventLists();

	abstract void clearEventLists();

	abstract boolean sweep();

	abstract boolean filterPrecedences();

	void flushUpdateLists() throws ContradictionException;

	void clearUpdateLists();

	ITemporalSRelation getBestDecision();
}
