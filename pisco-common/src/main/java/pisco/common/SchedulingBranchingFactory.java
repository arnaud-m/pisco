/**
 *  Copyright (c) 2011, Arnaud Malapert
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Arnaud Malapert nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package pisco.common;

import static choco.cp.solver.search.BranchingFactory.domDDegBin;
import static choco.cp.solver.search.BranchingFactory.domWDegBin;
import static choco.cp.solver.search.BranchingFactory.incDomWDegBin;
import static choco.cp.solver.search.BranchingFactory.lexicographic;
import static choco.cp.solver.search.BranchingFactory.maxPreserved;
import static choco.cp.solver.search.BranchingFactory.minPreserved;
import static choco.cp.solver.search.BranchingFactory.profile;
import static choco.cp.solver.search.BranchingFactory.randomSearch;
import static choco.cp.solver.search.BranchingFactory.setTimes;
import static choco.cp.solver.search.BranchingFactory.slackWDeg;
import parser.instances.BasicSettings;
import pisco.common.choco.branching.FinishBranchingGraph;
import pisco.common.choco.branching.FinishBranchingNaive;
import choco.cp.common.util.preprocessor.detector.scheduling.DisjunctiveSModel;
import choco.cp.solver.constraints.global.scheduling.precedence.ITemporalSRelation;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.cp.solver.search.integer.branching.AssignOrForbidIntVarValPair;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.task.OrderingValSelector;
import choco.cp.solver.search.task.SetTimes;
import choco.cp.solver.search.task.ordering.CentroidOrdering;
import choco.cp.solver.search.task.ordering.LexOrdering;
import choco.cp.solver.search.task.ordering.MaxPreservedOrdering;
import choco.cp.solver.search.task.ordering.MinPreservedOrdering;
import choco.cp.solver.search.task.ordering.RandomOrdering;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.branch.AbstractIntBranchingStrategy;
import choco.kernel.solver.constraints.global.scheduling.IResource;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

public final class SchedulingBranchingFactory {
	private static final ValSelector<IntDomainVar> MIN_VAL = new MinVal();

	private static final OrderingValSelector LEX_ORDER = new LexOrdering();

	public static enum Branching {
		RAND("Random", true),
		LEX("Lexicographic", true),
		ST("SetTimes"),
		PROFILE("profile", true),
		DDEG("Dom/DDEG", false),
		WDEG("Dom/WDEG", false),
		BWDEG("Bin-Dom/WDEG", false),
		SWDEG("Slack/WDEG", true),
		PWDEG("Pres/WDEG", true),
		MINPRES("Min-Preserved", true),
		MAXPRES("Max-Preserved", true),
		USR("User-disjunct-branching", true);

		private final String name;

		private final boolean precedenceBranching;

		private final boolean needDisjunctiveSModel;

		private Branching(String name) {
			this.name = name;
			this.precedenceBranching = false;
			this.needDisjunctiveSModel = false;
		}

		private Branching(String name, boolean needDisjunctiveModel) {
			this.name = name;
			this.precedenceBranching = true;
			this.needDisjunctiveSModel = needDisjunctiveModel;
		}

		private Branching(String name, boolean precedenceBranching,
				boolean precStoreNeeded) {
			this.name = name;
			this.precedenceBranching = precedenceBranching;
			this.needDisjunctiveSModel = precStoreNeeded;
		}



		public final String getName() {
			return name;
		}


		public final boolean isPrecedenceBranching() {
			return precedenceBranching;
		}


		public final boolean needDisjunctiveSModel() {
			return needDisjunctiveSModel;
		}
	}


	private SchedulingBranchingFactory() {
		super();
	}

	public final static ITemporalSRelation[] getDisjuncts(PreProcessCPSolver solver) {
		final DisjunctiveSModel disjSMod = solver.getDisjSModel();
		return disjSMod == null ? null : disjSMod.getEdges();
	}


	private static ValSelector<IntDomainVar> makeBoolValSel(boolean randVal, long seed) {
		return randVal ? new RandomIntValSelector(seed) : MIN_VAL; 
	}

	private static OrderingValSelector makeOrderValSel(OrderingValSelector userValSel, boolean randVal, boolean breakTie, OrderingValSelector breakValSel, long seed) {
		return userValSel == null ? 
				(randVal ? new RandomOrdering(seed) : 
					(breakTie ? (breakValSel == null ? new CentroidOrdering(seed) : breakValSel) : LEX_ORDER) ):
						userValSel;
	}

	public static AbstractIntBranchingStrategy generateDisjunctBranching(PreProcessCPSolver solver, OrderingValSelector userValSel, long seed) {
		final Configuration conf = solver.getConfiguration();
		final SchedulingBranchingFactory.Branching br = DisjunctiveSettings.getBranching(conf);
		final boolean randVal = conf.readBoolean(BasicSettings.RANDOM_VALUE);
		final boolean breakTie = conf.readBoolean(BasicSettings.RANDOM_TIE_BREAKING);
		switch (br) {
		case LEX: {
			return lexicographic(solver, VariableUtils.getBoolDecisionVars(solver));
		}
		case RAND: {
			return randomSearch(solver, VariableUtils.getBoolDecisionVars(solver), seed);
		}
		case PROFILE: {
			return profile(solver, solver.getDisjSModel(), makeOrderValSel(userValSel, randVal, breakTie, null, seed));
		}
		case DDEG: {
			return domDDegBin(solver, VariableUtils.getBoolDecisionVars(solver), makeBoolValSel(randVal, seed));
		}
		case WDEG: {
			return domWDegBin(solver, VariableUtils.getBoolDecisionVars(solver), makeBoolValSel(randVal, seed));
		}
		case BWDEG: {
			return incDomWDegBin(solver, VariableUtils.getBoolDecisionVars(solver), makeBoolValSel(randVal, seed));
		}
		case SWDEG: {
			return slackWDeg(solver, getDisjuncts(solver), makeOrderValSel(userValSel, randVal, breakTie, null, seed)); 
		}
		case PWDEG: {
			return slackWDeg(solver, getDisjuncts(solver), makeOrderValSel(userValSel, randVal, breakTie, new MinPreservedOrdering(seed), seed)); 
		}
		case MINPRES: { 
			return minPreserved(solver, getDisjuncts(solver), makeOrderValSel(userValSel, randVal, breakTie, new MinPreservedOrdering(seed), seed)); 
		}
		case MAXPRES: { 
			return maxPreserved(solver, getDisjuncts(solver), makeOrderValSel(userValSel, randVal, breakTie, new MaxPreservedOrdering(seed), seed));
		}
		default: {
			throw new SolverException("Invalid Search Strategy: "+br.getName());
		}
		}
	}

	public static AbstractIntBranchingStrategy generateDisjunctSubBranching(PreProcessCPSolver solver, Constraint ignoredCut) {
		if(solver.getConfiguration().readBoolean(DisjunctiveSettings.ASSIGN_BELLMAN)) {
			final DisjunctiveSModel disjSMod = solver.getDisjSModel();
			if(disjSMod != null) {
				return new FinishBranchingGraph(solver, disjSMod, ignoredCut);
			}
		}
		return new FinishBranchingNaive(solver, ignoredCut);
	}


	public static  AssignOrForbidIntVarValPair generateProfile(PreProcessCPSolver solver, IResource<?>[] fakeResources, OrderingValSelector userValSel, long seed) {
		final Configuration conf = solver.getConfiguration();
		final boolean randVal = conf.readBoolean(BasicSettings.RANDOM_VALUE);
		final boolean breakTie = conf.readBoolean(BasicSettings.RANDOM_TIE_BREAKING);
		return profile(solver, fakeResources, solver.getDisjSModel(), makeOrderValSel(userValSel, randVal, breakTie, null, seed));
	}

	public static SetTimes generateSetTimes(PreProcessCPSolver solver, long seed) {
		final Configuration conf = solver.getConfiguration();
		return  conf.readBoolean(BasicSettings.RANDOM_VALUE) || 
				conf.readBoolean(BasicSettings.RANDOM_TIE_BREAKING) ? 
						setTimes(solver, seed) : setTimes(solver);
	}


}
