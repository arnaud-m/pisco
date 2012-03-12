package pisco.shop;

import static choco.Choco.MAX_UPPER_BOUND;
import static choco.Choco.*;
import static choco.Choco.makeIntVar;
import static choco.Choco.sum;

import java.io.File;
import java.util.Arrays;

import choco.Options;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.comparator.TaskComparators;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.common.util.tools.TaskUtils;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.variables.scheduling.TaskVar;
import choco.visu.components.chart.ChocoChartFactory;
import parser.absconparseur.tools.UnsupportedConstraintException;
import parser.instances.BasicSettings;
import pisco.shop.parsers.AirlandParser;

public class AirLandProblem extends AbstractAirlandProblem {

	public int[] earlinessPenalties;

	public int[] tardinessPenalties;

	public boolean hasRealPenalties;

	public boolean hasSymmetricPenalties;


	public AirLandProblem(BasicSettings settings) {
		super(settings);
	}


	@Override
	public void initialize() {
		super.initialize();
		earlinessPenalties = tardinessPenalties = null;
		hasSymmetricPenalties = false;
		hasRealPenalties = false;
	}

	@Override
	public void load(File fichier) throws UnsupportedConstraintException {
		super.load(fichier);
		AirlandParser parser = (AirlandParser) this.parser;
		// Preprocess penalties
		earlinessPenalties = new int[nbJobs];
		tardinessPenalties = new int[nbJobs];
		hasSymmetricPenalties = true;
		for (int i = 0; i < nbJobs; i++) {
			if(parser.earlinessPenalties[i] != parser.tardinessPenalties[i]) {
				hasSymmetricPenalties = false; break;
			}
		}
		hasRealPenalties = false;
		for (int i = 0; i < nbJobs; i++) {
			if( ! MathUtils.isInt(parser.earlinessPenalties[i]) ||
					! MathUtils.isInt(parser.earlinessPenalties[i]) ) {
				hasRealPenalties = true; break;
			}
		}
		if(hasRealPenalties) {
			//Approximate penalties from double to int
			for (int i = 0; i < nbJobs; i++) {
				earlinessPenalties[i] = (int) Math.round( parser.earlinessPenalties[i] * 100);
				tardinessPenalties[i] = (int) Math.round( parser.earlinessPenalties[i] * 100);
			}	
		} else {
			for (int i = 0; i < nbJobs; i++) {
				earlinessPenalties[i] = (int) parser.earlinessPenalties[i];
				tardinessPenalties[i] = (int) parser.tardinessPenalties[i];
			}
		}

	}


	@Override
	public Boolean preprocess() {
		setComputedLowerBound(0);
		return super.preprocess();
	}


	@Override
	public Model buildModel() {
		Model model = super.buildModel();
		IntegerVariable obj = buildObjective("obj",MAX_UPPER_BOUND);
		model.setDefaultExpressionDecomposition(true);
		if(hasSymmetricPenalties) {
			IntegerVariable[] deviations = makeIntVarArray("D", nbJobs, 0, obj.getUppB(), Options.V_BOUND, Options.V_NO_DECISION);
			for (int i = 0; i < nbJobs; i++) {
				model.addConstraint(eq(deviations[i], abs(minus(tasks[i].end(),dueDates[i]))));
			}
			model.addVariable(obj);
			//model.addConstraint(eq(obj, scalar(earlinessPenalties, deviations)));
		} else {
			IntegerVariable[] earliness = makeIntVarArray("E", nbJobs, 0, obj.getUppB(), Options.V_BOUND, Options.V_NO_DECISION);
			IntegerVariable[] tardiness = makeIntVarArray("T", nbJobs, 0, obj.getUppB(), Options.V_BOUND, Options.V_NO_DECISION);
			for (int i = 0; i < nbJobs; i++) {
				model.addConstraint(eq(earliness[i], max(0, minus(dueDates[i], tasks[i].end()))));
				model.addConstraint(eq(tardiness[i], max(0, minus(tasks[i].end(), dueDates[i]))));
			}
			model.addConstraint(eq(obj, plus(scalar(earlinessPenalties, earliness), scalar(tardinessPenalties, tardiness))));
		}
		return model;
	}



	@Override
	protected Object makeSolutionChart() {
		ChocoLogging.flushLogs();
		return solver != null && solver.existsSolution() ?
				ChocoChartFactory.createGanttChart("", solver.getVar(tasks), releaseDates, computeSolutionSetups(), dueDates) : null;
	}


}
