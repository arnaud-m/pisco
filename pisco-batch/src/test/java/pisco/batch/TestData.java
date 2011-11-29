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
package pisco.batch;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import parser.absconparseur.tools.UnsupportedConstraintException;
import pisco.batch.data.BatchParser;
import pisco.batch.data.BatchProcessingData;
import pisco.batch.data.Job;
import pisco.batch.data.JobComparatorFactory;

public class TestData {

	BatchParser parser = new BatchParser();
	
	Job[] jobs = {new Job(1,5,5,1,10),  new Job(2,3,2,2,3), 
			new Job(3,4,7,3,5), new Job(4,6,6,4,10)};

	@Test
	public void testData() {
		BatchProcessingData data = new BatchProcessingData(jobs, 10);
		data.preprocess(JobComparatorFactory.getDecreasingSize());
		assertEquals(3, data.getMinDuration());
		assertEquals(6, data.getMaxDuration());
		assertEquals(18, data.getTotalDuration());

		assertEquals(3, data.getJob(0).getId());
		assertEquals(4, data.getJob(1).getId());
		assertEquals(1, data.getJob(2).getId());
		assertEquals(2, data.getJob(3).getId());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFormat1() throws UnsupportedConstraintException {
		parser.loadInstance(new File("./src/test/resources/bp03-W1.txt"));
		parser.parse(false);
	}
	@Test(expected=IllegalArgumentException.class)
	public void testFormat2() throws UnsupportedConstraintException {
		parser.loadInstance(new File("./src/test/resources/bp03-W2.txt"));
		parser.parse(false);
	}
}
