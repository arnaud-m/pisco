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
package pisco.batch.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import parser.absconparseur.tools.UnsupportedConstraintException;
import parser.instances.InstanceFileParser;
import choco.kernel.common.logging.ChocoLogging;

public class BatchParser implements InstanceFileParser {

	public final static Logger LOGGER= ChocoLogging.getMainLogger();

	private final static Pattern COMMENT = Pattern.compile("(#|//).*");

	public File input;

	private Scanner sc;

	private Job[] jobs;
	private int capacity;

	public final int getCapacity() {
		return capacity;
	}

	public final Job[] getJobs() {
		return jobs;
	}

	@Override
	public void cleanup() {
		input = null;
		sc = null;
		jobs = null;
		capacity = Integer.MIN_VALUE;
	}

	private void skipComments() {
		while (sc.hasNext(COMMENT)) {
			sc.nextLine();
		}
	}

	private int nextInt() {
		skipComments();
		if(sc.hasNextInt()) {return sc.nextInt();}
		else throw new IllegalArgumentException("parser...[read-int][FAIL]");
	}


	@Override
	public final File getInstanceFile() {
		return input;
	}



	@Override
	public void parse(boolean displayInstance)
			throws UnsupportedConstraintException {
		final int nbJobs = nextInt();
		capacity = nextInt();
		jobs = new Job[nbJobs];
		for (int i = 0; i < nbJobs; i++) {
			jobs[i] = new Job(i+1, nextInt(), nextInt(), nextInt(), nextInt());
		}
		skipComments();
		if(sc.hasNext()) {
			sc.close();
			throw new IllegalArgumentException("parser...[close:can-read-more][FAIL]");
		}
		sc.close();
		if(displayInstance) {
			LOGGER.info(Arrays.toString(jobs)+"\nCapacity="+capacity+"\n");
		}
	}

	@Override
	public void loadInstance(File file) {
		try {
			input = file;
			sc = new Scanner(input);
		}
		catch(FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "parser...[loading][FAIL]", e);
			ChocoLogging.flushLogs();
			System.exit(2);
		}
	}


}