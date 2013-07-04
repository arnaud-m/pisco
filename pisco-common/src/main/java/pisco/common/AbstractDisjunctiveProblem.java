package pisco.common;

import static pisco.common.SchedulingBranchingFactory.*;
import static pisco.common.SchedulingBranchingFactory.generateDisjunctSubBranching;
import static pisco.common.SchedulingBranchingFactory.generateProfile;
import static pisco.common.SchedulingBranchingFactory.generateSetTimes;
import parser.instances.AbstractMinimizeModel;
import parser.instances.BasicSettings;
import parser.instances.InstanceFileParser;
import parser.instances.ResolutionStatus;
import choco.cp.solver.CPSolver;
import choco.cp.solver.configure.RestartFactory;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.automaton.penalty.IsoPenaltyFunction;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.branch.AbstractIntBranchingStrategy;
import choco.kernel.solver.constraints.global.scheduling.IResource;

public abstract class AbstractDisjunctiveProblem extends AbstractMinimizeModel {

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

	public String getPropertyDiagnostic() {
		return isFeasible() ?  
				getInstanceName() + "="+ 
				(getStatus() == ResolutionStatus.OPTIMUM ? objective : getComputedLowerBound()+":"+objective)
				+" PROPERTY_FORMAT"
				: "";
	}


	protected abstract IResource<?>[] generateFakeResources(CPSolver solver);


	@Override
	public void initialize() {
		super.initialize();
		nbJobs = 0;
		makespan = null;
		constraintCut = null;
		cancelHeuristic();
	}

	protected AbstractIntBranchingStrategy makeUserDisjunctBranching(PreProcessCPSolver solver, long seed) {
		throw new SolverException("Invalid Search Strategy: "+SchedulingBranchingFactory.Branching.USR);
	}
	
	protected AbstractIntBranchingStrategy makeDisjunctSubBranching(PreProcessCPSolver solver) {
		return generateDisjunctSubBranching(solver, constraintCut);
	}
	
	protected void setGoals(PreProcessCPSolver solver) {
		solver.clearGoals();
		final Configuration conf = solver.getConfiguration();
		final SchedulingBranchingFactory.Branching br = DisjunctiveSettings.getBranching(conf);
		if( br == SchedulingBranchingFactory.Branching.ST) {
			RestartFactory.unsetRecordNogoodFromRestart(solver);
			solver.addGoal(generateSetTimes(solver, getSeed()));
		} else {			
			if( conf.readBoolean(BasicSettings.LIGHT_MODEL) && br == SchedulingBranchingFactory.Branching.PROFILE) {
				solver.addGoal(generateProfile(solver, 
						generateFakeResources(solver), 
						null, getSeed()));
			} else if( br == SchedulingBranchingFactory.Branching.USR) {
				solver.addGoal(makeUserDisjunctBranching(solver, getSeed()));
			} else {
				solver.addGoal(generateDisjunctBranching(solver, null, getSeed()));
			}
			solver.addGoal(makeDisjunctSubBranching(solver));
			
		}
	}

}