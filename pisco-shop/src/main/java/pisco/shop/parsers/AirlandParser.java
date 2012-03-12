package pisco.shop.parsers;

import parser.absconparseur.tools.UnsupportedConstraintException;

public class AirlandParser extends AbstractTextParser {

	public int nbJobs;
	
	public int freezeTime;
	
	public int[] appearanceDates;
	
	public int[] releaseDates;
	
	public int[] dueDates;
	
	public int[] deadlines;
	
	public double[] earlinessPenalties;
	
	public double[] tardinessPenalties;
	
	public int[][] setupTimes;
	@Override
	public void cleanup() {
		super.cleanup();
		releaseDates = dueDates = deadlines = null;
		earlinessPenalties = tardinessPenalties = null;
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
		earlinessPenalties = new double[nbJobs];
		tardinessPenalties = new double[nbJobs];
		setupTimes = new int[nbJobs][nbJobs];
		for (int i = 0; i < nbJobs; i++) {
			appearanceDates[i] = readInteger();  
			releaseDates[i] = readInteger();
			dueDates[i] = readInteger();
			deadlines[i] = readInteger();
			// TODO - convert from double to int - created 11 mars 2012 by A. Malapert
			earlinessPenalties[i] = readDouble(); 
			tardinessPenalties[i] = readDouble();
			for (int j = 0; j < nbJobs; j++) {
				setupTimes[i][j] = readInteger();
			}
		}
	}

}
