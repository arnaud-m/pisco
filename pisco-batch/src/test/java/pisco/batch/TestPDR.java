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

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

import choco.kernel.common.util.tools.ArrayUtils;

import pisco.batch.data.BJob;
import pisco.common.PDR1Scheduler;
import static org.junit.Assert.assertEquals;
import static pisco.batch.heuristics.PDRScheduler.*;
import static pisco.common.CostFactory.*;
import static pisco.common.JobComparators.*;

public class TestPDR {

	// TODO - Compare schedule and LazySchedule - created 6 nov. 2011 by Arnaud Malapert
	
	private final static BJob[] INST1 = new BJob[] {
			new BJob(1, 5, 3, 2, 4),
			new BJob(2, 6, 4, 3, 8),
			new BJob(3, 7, 5, 4, 12),
			new BJob(4, 4, 2, 1, 9),
			new BJob(5, 3, 3, 2, 15)
	};
	
	private final static BJob[] INST2 = new BJob[] {
			new BJob(1, 4, 5, 2, 4),
			new BJob(2, 5, 6, 3, 8),
			new BJob(3, 6, 2, 1, 18),
			new BJob(4, 7, 3, 2, 12),
			new BJob(5, 4, 1, 3, 9),
			new BJob(6, 3, 8, 2, 8),
			new BJob(7,12, 4, 1, 15)
	};
	
	private final static BJob[] copy(BJob[] jobs) {
		return Arrays.copyOf(jobs, jobs.length);
	}
	@Test
	public void testLmax1() {
		Assert.assertEquals("Lmax", 10, PDR1Scheduler.schedule1Lmax(copy(INST1)));
	}
	
	@Test
	public void testLmax2() {
		Assert.assertEquals("Lmax", 23, PDR1Scheduler.schedule1Lmax(copy(INST2)));
	}
	
	@Test
	public void testFlowTime1() {
		Assert.assertEquals("Lmax", 65, PDR1Scheduler.schedule1Flow(copy(INST1)));
	}
	
	@Test
	public void testFlowTime2() {
		Assert.assertEquals("Lmax", 129, PDR1Scheduler.schedule1Flow(copy(INST2)));
	}
	
	@Test
	public void testWFlowTime1() {
		Assert.assertEquals("Lmax", 161, PDR1Scheduler.schedule1WFlow(copy(INST1)));
	}
	
	@Test
	public void testWFlowTime2() {
		Assert.assertEquals("Lmax", 210, PDR1Scheduler.schedule1WFlow(copy(INST2)));
	}
	
	public void testReplacement(BJob[] schedule, BJob job) {
		final BJob[] rschedule = copy(schedule);
		rschedule[job.getId()-1]=job;
		final int vf = PDR1Scheduler.schedule1WFlow(rschedule);
		//System.out.println(Arrays.toString(rschedule));
		final BJob[] cschedule = copy(schedule);
		PDR1Scheduler.schedule1WFlow(cschedule);
		assertEquals(vf, replace(cschedule, cschedule.length, PDR1Scheduler.WSPT, job));
		
	}
	@Test
	public void testReplacements() {
		//no transfer
		testReplacement(INST2, new BJob(4,8,2,12));
		//backward transfer
		testReplacement(INST2, new BJob(3,17,12,18));
		//forward transfer
		testReplacement(INST2, new BJob(6,16,2,3,8));
		//transfer from first to last
		testReplacement(INST2, new BJob(5,30,2,9));
		//transfer from last to first 
		testReplacement(INST2, new BJob(7,1,3,15));
	}
	
	@Test
	public void testInsertions() {
		BJob[] newJobs = new BJob[] {
			new BJob(8, 2, 3, 10),
			new BJob(9, 2, 4, 10),
			new BJob(10, 13, 1, 10),
			new BJob(11, 25, 2, 10),
			new BJob(12, 13, 1, 10),
			new BJob(13, 8, 3, 10),
			new BJob(13, 6, 2, 10)
		};
		BJob[] nschedule = ArrayUtils.append(copy(INST2), newJobs);
		final int vf = PDR1Scheduler.schedule1WFlow(nschedule);
		//System.out.println(Arrays.toString(nschedule));
		final BJob[] cschedule = copy(INST2);
		PDR1Scheduler.schedule1WFlow(cschedule);
		Arrays.sort(newJobs, getWeightedShortestProcessingTime());
		assertEquals(vf, insert(cschedule, cschedule.length, PDR1Scheduler.WSPT, newJobs));
		
	}

	// DONE 14 nov. 2011 - Test Parallel Schedulers - created 6 nov. 2011 by Arnaud Malapert
	
	@Test
	public void testParallelUnitSizedLmaxSchedule() {
		assertEquals(-2, parallelUnitSizedLmaxSchedule(INST1, INST1.length, 10));
		assertEquals(0, parallelUnitSizedLmaxSchedule(INST2, INST2.length, 10));
	}
	
	@Test
	public void testParallelUnitSizedWFlowSchedule() {
		assertEquals(56, parallelUnitSizedWFlowSchedule(INST1, INST1.length, 10));
		assertEquals(78, parallelUnitSizedWFlowSchedule(INST2, INST2.length, 10));
	}

}
