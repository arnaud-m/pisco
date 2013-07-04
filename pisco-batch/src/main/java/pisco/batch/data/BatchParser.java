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

import java.util.Arrays;

import parser.absconparseur.tools.UnsupportedConstraintException;
import parser.instances.AbstractTextParser;

public class BatchParser extends AbstractTextParser {

	private BJob[] jobs;
	private int capacity;

	public final int getCapacity() {
		return capacity;
	}

	public final BJob[] getJobs() {
		return jobs;
	}

	@Override
	public void cleanup() {
		super.cleanup();
		jobs = null;
		capacity = Integer.MIN_VALUE;
	}

	


	@Override
	public void parse(boolean displayInstance)
			throws UnsupportedConstraintException {
		final int nbJobs = nextInt();
		capacity = nextInt();
		jobs = new BJob[nbJobs];
		for (int i = 0; i < nbJobs; i++) {
			jobs[i] = new BJob(i+1, nextInt(), nextInt(), nextInt(), nextInt());
		}
		close();
		if(displayInstance) {
			LOGGER.info(Arrays.toString(jobs)+"\nCapacity="+capacity+"\n");
		}
	}


}