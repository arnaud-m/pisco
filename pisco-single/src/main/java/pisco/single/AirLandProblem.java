package pisco.single;

import static choco.Choco.*;
import static choco.Choco.abs;
import static choco.Choco.eq;
import static choco.Choco.makeIntVarArray;
import static choco.Choco.max;
import static choco.Choco.minus;
import static choco.Choco.plus;
import static choco.Choco.scalar;

import gnu.trove.TIntArrayList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import parser.absconparseur.tools.UnsupportedConstraintException;
import parser.instances.BasicSettings;
import pisco.single.parsers.AirlandParser;
import choco.Options;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.cp.solver.search.BranchingFactory;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;

public class AirLandProblem extends Abstract1MachineProblem {

	public int[][] setupTimes;
	public int[] earlinessPenalties;
	public int[] tardinessPenalties;

	public AirLandProblem(BasicSettings settings) {
		super(settings, new AirlandParser());
	}

	
	@Override
	public void initialize() {
		super.initialize();
		earlinessPenalties = tardinessPenalties = null;
	}

	@Override
	public void load(File fichier) throws UnsupportedConstraintException {
		super.load(fichier);
		AirlandParser parser = (AirlandParser) this.parser;

	}

	@Override
	public Boolean preprocess() {
		setComputedLowerBound(0);
		return super.preprocess();
	}


	@Override
	public Model buildModel() {
		Model model = super.buildModel();
		IntegerVariable obj = buildObjective("obj", MAX_UPPER_BOUND);
		List<IntegerVariable> penalties = new ArrayList<IntegerVariable>(2 * nbJobs);
		TIntArrayList coefficients = new TIntArrayList(2 * nbJobs);
		for (int i = 0; i < nbJobs; i++) {
			if(earlinessPenalties[i] == tardinessPenalties[i]) {
				IntegerVariable deviation = makeIntVar("D"+i, 0, obj.getUppB(), Options.V_BOUND, Options.V_NO_DECISION);
				model.addConstraint(eq(deviation, abs(minus(tasks[i].end(),jobs[i].getDueDate()))));
				penalties.add(deviation);
				coefficients.add(earlinessPenalties[i]);
			} else {
				IntegerVariable earliness = makeIntVar("E", 0, obj.getUppB(), Options.V_BOUND, Options.V_NO_DECISION);
				IntegerVariable tardiness = makeIntVar("T", 0, obj.getUppB(), Options.V_BOUND, Options.V_NO_DECISION);
				model.addConstraints(
						eq(earliness, max(0, minus(jobs[i].getDueDate(), tasks[i].end()))),
						eq(tardiness, max(0, minus(tasks[i].end(), jobs[i].getDueDate())))
						);
				penalties.add(earliness);
				coefficients.add(earlinessPenalties[i]);
				penalties.add(tardiness);
				coefficients.add(tardinessPenalties[i]);
			}
		}
		model.addConstraint(eq(obj, scalar(penalties.toArray(new IntegerVariable[penalties.size()]), coefficients.toNativeArray())));
		return model;
	}


	protected void setSecondaryGoals(PreProcessCPSolver solver) {
		// FIXME - Awful and inactive branching : use at least a due date oriented branching! - created 14 mars 2012 by A. Malapert
		solver.addGoal( BranchingFactory.minDomMinVal(solver, solver.getVar(VariableUtils.getStartVariables(tasks))));
	}



}
