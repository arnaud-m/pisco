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
package pisco.single.parsers;


import parser.absconparseur.tools.UnsupportedConstraintException;
import pisco.common.IJob;
import pisco.common.Job;
import pisco.common.parsers.AbstractTextParser;

public class AirlandParser extends AbstractTextParser {

	public int nbJobs;
	
	public int freezeTime;
	
	public int[] appearanceDates;
	
	IJob[] jobs;
	
	public double[] earlinessPenalties;
	
	public double[] tardinessPenalties;
	
	public int[][] setupTimes;
	//private Job[] jobs;
	private int capacity;

	public final int getCapacity() {
		return capacity;
	}

	//public final Job[] getJobs() {
//		return jobs;
//	}

	@Override
	public void cleanup() {
		super.cleanup();
		nbJobs = 0;
		jobs = null;
		freezeTime = 0;
		appearanceDates = null;
		earlinessPenalties = tardinessPenalties = null;
		setupTimes = null;
	}

	@Override
	public void parse(boolean displayInstance)
			throws UnsupportedConstraintException {
		nbJobs = nextInt();
		freezeTime = nextInt(); // useless freeze time (for online algorithms
		appearanceDates= new int[nbJobs];
		jobs = new IJob[nbJobs];
		earlinessPenalties = new double[nbJobs];
		tardinessPenalties = new double[nbJobs];
		setupTimes = new int[nbJobs][nbJobs];
		for (int i = 0; i < nbJobs; i++) {
			appearanceDates[i] = nextInt();  
			jobs[i] = new Job(i);
			jobs[i].setReleaseDate(nextInt());
			jobs[i].setDueDate(nextInt());
			jobs[i].setDeadline(nextInt());
			// TODO - convert from double to int - created 11 mars 2012 by A. Malapert
			earlinessPenalties[i] = nextDouble(); 
			tardinessPenalties[i] = nextDouble();
			for (int j = 0; j < nbJobs; j++) {
				setupTimes[i][j] = nextInt();
			}
		}
		close();
		if(displayInstance) {
			//LOGGER.info(Arrays.toString(jobs)+"\nCapacity="+capacity+"\n");
		}
	}


}