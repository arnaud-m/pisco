package pisco.common;

import static pisco.common.SchedulingBranchingFactory.generateDisjunctBranching;
import static pisco.common.SchedulingBranchingFactory.generateDisjunctSubBranching;
import static pisco.common.SchedulingBranchingFactory.generateProfile;
import static pisco.common.SchedulingBranchingFactory.generateSetTimes;
import parser.instances.AbstractMinimizeModel;
import parser.instances.BasicSettings;
import parser.instances.InstanceFileParser;
import choco.cp.solver.configure.RestartFactory;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Configuration;
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

	protected abstract IResource<?>[] generateFakeResources();


	@Override
	public void initialize() {
		super.initialize();
		nbJobs = 0;
		makespan = null;
		constraintCut = null;
		cancelHeuristic();
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
				solver.addGoal(generateProfile(solver, generateFakeResources(), null, getSeed()));
			} else {
			solver.addGoal(generateDisjunctBranching(solver, null, getSeed()));
			}
			solver.addGoal(generateDisjunctSubBranching(solver, constraintCut));

		}
	}

}