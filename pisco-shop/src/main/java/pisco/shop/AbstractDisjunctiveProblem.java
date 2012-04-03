package pisco.shop;

import static choco.cp.solver.search.BranchingFactory.domDDeg;
import static choco.cp.solver.search.BranchingFactory.domDDegBin;
import static choco.cp.solver.search.BranchingFactory.domWDeg;
import static choco.cp.solver.search.BranchingFactory.domWDegBin;
import static choco.cp.solver.search.BranchingFactory.incDomWDegBin;
import static choco.cp.solver.search.BranchingFactory.lexicographic;
import static choco.cp.solver.search.BranchingFactory.maxPreserved;
import static choco.cp.solver.search.BranchingFactory.minPreserved;
import static choco.cp.solver.search.BranchingFactory.randomSearch;
import static choco.cp.solver.search.BranchingFactory.setTimes;
import static choco.cp.solver.search.BranchingFactory.slackWDeg;
import static pisco.shop.choco.SchedulingBranchingFactory.profile;

import org.jfree.ui.HorizontalAlignment;

import choco.cp.common.util.preprocessor.detector.scheduling.DisjunctiveSModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.configure.RestartFactory;
import choco.cp.solver.constraints.global.scheduling.precedence.ITemporalSRelation;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.cp.solver.preprocessor.PreProcessConfiguration;
import choco.cp.solver.search.BranchingFactory;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.task.ordering.CentroidOrdering;
import choco.cp.solver.search.task.ordering.LexOrdering;
import choco.cp.solver.search.task.ordering.MinPreservedOrdering;
import choco.kernel.common.util.comparator.TaskComparators;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.branch.AbstractIntBranchingStrategy;
import choco.kernel.solver.constraints.global.scheduling.IResource;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;
import parser.instances.AbstractMinimizeModel;
import parser.instances.BasicSettings;
import parser.instances.InstanceFileParser;
import pisco.shop.ChocoshopSettings.Branching;
import pisco.shop.choco.branching.FinishBranchingGraph;
import pisco.shop.choco.branching.FinishBranchingNaive;

public abstract class AbstractDisjunctiveProblem extends AbstractMinimizeModel {

	protected static final ValSelector<IntDomainVar> MIN_VAL = new MinVal();

	public int nbJobs;

	protected IntegerVariable makespan;

	protected Constraint constraintCut;

	public AbstractDisjunctiveProblem(InstanceFileParser parser,
			Configuration settings) {
		super(parser, settings);
	}

	public final int getNbJobs() {
		return nbJobs;
	}

	protected abstract IResource<?>[] generateFakeResources();


	@Override
	public void initialize() {
		super.initialize();
		nbJobs = 0;
		makespan = null;
		constraintCut = null;
		cancelHeuristic();
	}

	public final ITemporalSRelation[] getDisjuncts(PreProcessCPSolver solver) {
		final DisjunctiveSModel disjSMod = solver.getDisjSModel();
			return disjSMod == null ? null : disjSMod.getEdges();
	}

	protected AbstractIntBranchingStrategy generateDisjunctBranching(PreProcessCPSolver solver) {
		final Branching br = ChocoshopSettings.getBranching(defaultConf);
		final boolean breakTie = defaultConf.readBoolean(BasicSettings.RANDOM_TIE_BREAKING);
		switch (br) {
		case LEX: {
			return lexicographic(solver, solver.getBooleanVariables());
		}
		case RAND: {
			return randomSearch(solver, solver.getBooleanVariables(), getSeed());
		}
		case PROFILE: {
			if( defaultConf.readBoolean(BasicSettings.LIGHT_MODEL) ) {
				final IResource<?>[] resources = generateFakeResources();
				if(breakTie) return profile(solver, resources, solver.getDisjSModel(), getSeed());
				else return profile(solver, resources, solver.getDisjSModel());
			}else {
				if(breakTie) return profile(solver, solver.getDisjSModel(), getSeed());
				else return profile(solver, solver.getDisjSModel());
			}
		}
		case DDEG: {
			if( breakTie) {
				return domDDeg(solver, 
						solver.getBooleanVariables(),
						new RandomIntValSelector(getSeed()),
						getSeed()
						);
			}else {
				return domDDegBin(solver, solver.getBooleanVariables(), MIN_VAL);
			}
		}
		case WDEG: {
			if( breakTie) {
				return domWDeg(solver, 
						solver.getBooleanVariables(),
						new RandomIntValSelector(getSeed()),
						getSeed()
						);
			}else {
				return domWDegBin(solver, solver.getBooleanVariables(), MIN_VAL);
			}
		}
		case BWDEG: {
			if( breakTie) {
				return incDomWDegBin(solver, 
						solver.getBooleanVariables(),
						new RandomIntValSelector(getSeed()),
						getSeed()
						);
			}else {
				return incDomWDegBin(solver, solver.getBooleanVariables(), MIN_VAL);
			}
		}
		case SWDEG: {
			if( breakTie) {
				return slackWDeg(solver, getDisjuncts(solver), new CentroidOrdering(getSeed())); 
			} else {
				return slackWDeg(solver, getDisjuncts(solver), new LexOrdering());
			}
		}
		case PWDEG: {
			if( breakTie) {
				return slackWDeg(solver, getDisjuncts(solver), new MinPreservedOrdering(getSeed())); 
			} else {
				return slackWDeg(solver, getDisjuncts(solver), new LexOrdering());
			}
		}
		case MINPRES: { 
			if( breakTie) {
				return minPreserved(solver, getDisjuncts(solver), getSeed()); 
			} else {
				return minPreserved(solver, getDisjuncts(solver), new LexOrdering());
			}
		}
		case MAXPRES: { 
			if( breakTie) {
				return maxPreserved(solver, getDisjuncts(solver), getSeed()); 
			} else {
				return maxPreserved(solver, getDisjuncts(solver), new LexOrdering());
			}
		}
		default: {
			throw new SolverException("Invalid Search Strategy: "+br.getName());
		}
		}
	}

	protected void setSecondaryGoals(PreProcessCPSolver solver) {
		if(defaultConf.readBoolean(ChocoshopSettings.ASSIGN_BELLMAN)) {
			final ITemporalSRelation[] disjuncts = getDisjuncts(solver);
			if(disjuncts != null) {
				solver.addGoal(new FinishBranchingGraph(solver, getDisjuncts(solver), constraintCut));
				return;
			}
		}
		solver.addGoal(new FinishBranchingNaive(solver, constraintCut));
	}

	protected void setGoals(PreProcessCPSolver solver) {
		solver.clearGoals();
		final Branching br = ChocoshopSettings.getBranching(defaultConf);
		if( br == Branching.ST) {
			RestartFactory.unsetRecordNogoodFromRestart(solver);
			if(defaultConf.readBoolean(BasicSettings.RANDOM_TIE_BREAKING) ) {
				solver.addGoal(setTimes(solver, TaskComparators.makeEarliestStartingTimeCmp(), true));
			}else {
				solver.addGoal(setTimes(solver));
			}
		} else {
			solver.addGoal(generateDisjunctBranching(solver));
			setSecondaryGoals(solver);

		}
	}

}