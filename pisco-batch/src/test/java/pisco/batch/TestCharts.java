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

import org.jfree.chart.ChartPanel;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.junit.Test;

import pisco.batch.data.Batch;
import pisco.batch.data.BJob;
import pisco.batch.visu.BatchingChartFactory;
import pisco.common.PDR1Scheduler;

public class TestCharts {

	private final static int SLEEP=500;
	
	private final static BJob[] JOBS = new BJob[] {
			new BJob(1, 5, 2, 1, 7),
			new BJob(2, 6, 3, 1, 8),
			new BJob(3, 7,4, 1, 12),
			new BJob(4, 4, 1, 1, 9),
			new BJob(5, 3,2, 1, 15)
	};
	
	private final static Batch[] BATCHES = new Batch[3];
	
	static {
		BATCHES[0] = new Batch(0);
		BATCHES[0].parallelMerge(JOBS[0]);
		BATCHES[0].parallelMerge(JOBS[1]);
		BATCHES[1] = new Batch(1);
		BATCHES[1].parallelMerge(JOBS[2]);
		BATCHES[1].parallelMerge(JOBS[3]);
		BATCHES[2] = new Batch(2);
		BATCHES[2].parallelMerge(JOBS[4]);
	}
	
	@Test
	public void testWFlow() throws InterruptedException{
		PDR1Scheduler.schedule1WFlow(BATCHES);
		//frame
		ApplicationFrame demo = new ApplicationFrame("Batch Processing Demo");
		ChartPanel chartPanel = new ChartPanel( BatchingChartFactory.createWFlowChart(BATCHES, "W. Flowtime", -1));
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 300));
		demo.setContentPane(chartPanel);
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);
		Thread.sleep(SLEEP);
	}
	

	@Test
	public void testLmaxFlow() throws InterruptedException{
		PDR1Scheduler.schedule1Lmax(BATCHES);
		ApplicationFrame demo = new ApplicationFrame("Batch Processing Demo");
		ChartPanel chartPanel = new ChartPanel( BatchingChartFactory.createLmaxChart(BATCHES, "Lmax", 10));
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 300));
		demo.setContentPane(chartPanel);
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);
		Thread.sleep(SLEEP);
	}
}
