package pisco.shop.parsers;

import choco.kernel.common.util.tools.MathUtils;
import parser.absconparseur.tools.UnsupportedConstraintException;

public class AirlandParser extends AbstractTextParser {

	public int nbJobs;
	
	public int freezeTime;
	
	public int[] appearanceDates;
	
	public int[] releaseDates;
	
	public int[] dueDates;
	
	public int[] deadlines;
	
	public int[] earlinessPenalties;
	
	public int[] tardinessPenalties;
	
	public int[][] setupTimes;
	@Override
	public void cleanup() {
		super.cleanup();
		releaseDates = dueDates = deadlines = earlinessPenalties = tardinessPenalties = null;
		setupTimes = null;
	}


	@Override
	public void parse(boolean displayInstance)
			throws UnsupportedConstraintException {
		nbJobs = readInteger();
		freezeTime = readInteger(); // useless freeze time (for online algorithms
		appearanceDates= new int[nbJobs];
		releaseDates = new int[nbJobs];
		dueDates = new int[nbJobs];
		deadlines = new int[nbJobs];
		earlinessPenalties = new int[nbJobs];
		tardinessPenalties = new int[nbJobs];
		setupTimes = new int[nbJobs][nbJobs];
		for (int i = 0; i < nbJobs; i++) {
			appearanceDates[i] = readInteger();  
			releaseDates[i] = readInteger();
			dueDates[i] = readInteger();
			deadlines[i] = readInteger();
			// TODO - convert from double to int - created 11 mars 2012 by A. Malapert
			earlinessPenalties[i] = readInteger(); 
			tardinessPenalties[i] = readInteger();
			for (int j = 0; j < nbJobs; j++) {
				setupTimes[i][j] = readInteger();
			}
		}
	}

}
