package pisco.single;

import java.io.File;

import parser.absconparseur.tools.UnsupportedConstraintException;
import parser.instances.BasicSettings;
import pisco.single.parsers.AirlandParser;
import choco.kernel.common.util.tools.MathUtils;

public abstract class AbstractAirlandWeightedProblem extends
		AbstractAirlandProblem {

	public int[] earlinessPenalties;
	public int[] tardinessPenalties;
	public boolean hasRealPenalties;
	public boolean hasSymmetricPenalties;

	public AbstractAirlandWeightedProblem(BasicSettings settings) {
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
	protected void logOnConfiguration() {
		super.logOnConfiguration();
		StringBuilder b = new StringBuilder();
		if(hasSymmetricPenalties) {
			b.append("SYMMETRIC    ");
		}
		if(hasRealPenalties) {
			b.append("REAL    ");
		}
		if(b.length() > 0) {
			b.insert(0, "PENALTIES    ");
		}
		logMsg.storeConfiguration(b.toString());
	}
	
	

}