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
/* ======================================
 * JFreeChart : a free Java chart library
 * ======================================
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 * Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 * (C) Copyright 2000-2003, by Object Refinery Limited and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * --------------------
 * XYBarChartDemo3.java
 * --------------------
 * (C) Copyright 2002, 2003, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: XYBarChartDemo3.java,v 1.4 2003/11/28 10:57:34 mungady Exp $
 *
 * Changes
 * -------
 * 20-Jun-2002 : Version 1 (DG);
 * 02-Jul-2002 : Removed unnecessary imports (DG);
 * 24-Aug-2002 : Set preferred size for ChartPanel (DG);
 * 11-Oct-2002 : Fixed issues reported by Checkstyle (DG);
 * 05-Feb-2003 : Renamed VerticalXYBarChartDemo --> VerticalXYBarChartDemo3 (DG);
 *
 */

package pisco.batch.visu;

import static choco.visu.components.chart.ChocoChartFactory.CHOCO_THEME;
import static choco.visu.components.chart.ChocoChartFactory.createCapacityMarker;
import static choco.visu.components.chart.ChocoChartFactory.createDateAxis;
import static choco.visu.components.chart.ChocoChartFactory.createIntegerAxis;
import static choco.visu.components.chart.ChocoChartFactory.createMarker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.util.Date;
import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.gantt.XYTaskDataset;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.Layer;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.TextAnchor;

import pisco.batch.data.Batch;
import pisco.batch.data.BJob;
import pisco.common.CostFactory;
import pisco.common.ICostAggregator;
import pisco.common.PDR1Scheduler;
import choco.visu.components.chart.ChocoChartFactory;
import choco.visu.components.chart.ChocoColor;


/**
 * A simple demonstration application showing how to create a vertical bar chart.
 *
 * @author David Gilbert
 */
public final class BatchingChartFactory {

	private final static Color B_RED = new Color(255, 51, 51);


	private BatchingChartFactory() {
		super();
	}

	public static TimeSeriesCollection createWFlowtimeDataset(Batch[] batches) {
		TimeSeries series = new TimeSeries("Cumulated Weighted Flowtime");
		ICostAggregator globalCostFunction = CostFactory.makeSumCosts();
		series.add(new Millisecond(new Date(0)),0);
		for (int i = 0; i < batches.length; i++) {
			globalCostFunction.addCost(batches[i].getWeightedCompletionTime());
			series.add(new Millisecond(new Date(batches[i].getCompletionTime())), globalCostFunction.getTotalCost());
		}
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(series);
		return dataset;
	}

	public static XYDataset createLatenessDataset(Batch[] batches) {
		final TaskSeriesCollection coll = new TaskSeriesCollection();
		for (int i = 0; i < batches.length; i++) {
			TaskSeries series = new TaskSeries(String.valueOf(i));
			final int dd = batches[i].getDueDate();
			final int compl = batches[i].getCompletionTime();
			TimePeriod p =  dd < compl ? new SimpleTimePeriod(dd, compl) : new SimpleTimePeriod(compl, dd);
			series.add( new Task("B"+i, p));
			coll.add(series );
		}
		final XYTaskDataset dataset = new XYTaskDataset(coll);
		dataset.setTransposed(true);
		return dataset;

	}


	protected static void makeDueDateMarkers(XYPlot plot, Batch[] batches, Paint[] palette) {
		for (int k = 0; k < batches.length; k++) {
			final List<BJob> jobs = batches[k].getJobs();
			for (BJob job : jobs) {
				plot.addDomainMarker( createMarker(
						job.getDueDate(), "d"+job.getId(), (Color) palette[k], 
						TextAnchor.TOP_CENTER, LengthAdjustmentType.EXPAND)
						);
			}
		}


	}

	public static XYPlot createLatenessPlot(Batch[] batches) {
		final Paint[] palette = makePalette(batches.length);
		XYBarRenderer renderer = new XYBarRendererPaletteBySeries(palette);
		renderer.setUseYInterval(true);
		renderer.setDrawBarOutline(true);
		final LatenessLabelToolTipGenerator bpg = new LatenessLabelToolTipGenerator(batches);
		renderer.setBaseItemLabelGenerator(bpg);
		renderer.setBaseItemLabelsVisible(true);
		renderer.setBaseToolTipGenerator(bpg);
		renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER));
		XYPlot plot =  new XYPlot( createLatenessDataset(batches), null, createIntegerAxis("Batches"),renderer);
		plot.setRangeGridlinesVisible(false);
		makeDueDateMarkers(plot, batches, palette);
		return plot;
	}

	public static XYPlot createWFlowtimePlot(Batch[] batches) {
		ValueAxis timeAxis = new DateAxis("");
		NumberAxis valueAxis = new NumberAxis("Cum. WFlow");
		valueAxis.setAutoRangeIncludesZero(false);
		XYPlot plot = new XYPlot(createWFlowtimeDataset(batches), timeAxis, valueAxis, null);

		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
		renderer.setBaseToolTipGenerator(new WFlowLabelToolTipGenerator(batches));
		plot.setRenderer(renderer);
		return plot;
	}

	public static XYPlot createBatchPlot(Batch[] batches, int capacity) {
		final XYBarRenderer renderer = new StackedXYBarRendererPaletteByItems(makePalette(batches.length));
		renderer.setShadowVisible(false);
		renderer.setShadowXOffset(0);
		renderer.setDrawBarOutline(true);
		renderer.setBaseOutlineStroke(new BasicStroke(2));
		final BatchLabelToolTipGenerator lpg = new BatchLabelToolTipGenerator();
		renderer.setBaseItemLabelGenerator(lpg);
		renderer.setBaseItemLabelsVisible(true);
		renderer.setBaseToolTipGenerator(lpg);
		XYPlot plot =  new XYPlot(new BatchProcessingDataset(batches),null, createIntegerAxis("Load"),renderer);

		if(capacity>0) {
			Marker capaMarker = createCapacityMarker(capacity, "Capacity", B_RED);
			plot.addRangeMarker(0, capaMarker, Layer.FOREGROUND);
		}
		return plot;
	}

	private static JFreeChart createCombinedChart(String title, XYPlot batchplot, XYPlot objPlot) {
		CombinedDomainXYPlot plot = new CombinedDomainXYPlot(createDateAxis());
		plot.setGap(10.0);
		plot.add(objPlot, 1);
		plot.add(batchplot, 2);
		plot.setOrientation(PlotOrientation.VERTICAL);
		JFreeChart chart = new JFreeChart(title,JFreeChart.DEFAULT_TITLE_FONT, plot, false);
		CHOCO_THEME.apply(chart);
		return chart;	
	}


	public static JFreeChart createLmaxChart(Batch[] batches, String title, int capacity) {
		return createCombinedChart(title, createBatchPlot(batches, capacity), createLatenessPlot(batches));
	}

	public static JFreeChart createWFlowChart(Batch[] batches, String title, int capacity) {
		final XYPlot objPlot = createWFlowtimePlot(batches);
		final JFreeChart chart = createCombinedChart(title, createBatchPlot(batches, capacity), objPlot);
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) objPlot.getRenderer();
		renderer.setSeriesPaint(0, B_RED);
		renderer.setSeriesFillPaint(0, B_RED);
		return chart;
	}

	static final class BatchLabelToolTipGenerator implements XYItemLabelGenerator, XYToolTipGenerator {

		@Override
		public String generateLabel(XYDataset dataset, int series, int item) {
			if (dataset instanceof BatchProcessingDataset) {
				return "J"+( (BatchProcessingDataset) dataset).batches[item].getJob(series).getId();
			}
			return String.valueOf(series);
		}

		@Override
		public String generateToolTip(XYDataset dataset, int series, int item) {
			if (dataset instanceof BatchProcessingDataset) {
				return ( (BatchProcessingDataset) dataset).batches[item].getJob(series).toString();

			}
			return String.valueOf(series);
		}


	}

	private static void makeDurationToolTip(StringBuilder b, Batch batch) {
		final List<BJob> jobs = batch.getJobs();
		final int dur = batch.getDuration();
		b.append("p=").append(dur);
		for (BJob job : jobs) {
			if(job.getDuration() ==  dur) b.append("-J").append(job.getId());
		}
	}


	static final class LatenessLabelToolTipGenerator implements XYItemLabelGenerator, XYToolTipGenerator {

		Batch[] batches;

		public LatenessLabelToolTipGenerator(Batch[] batches) {
			this.batches =batches;
		}

		@Override
		public String generateLabel(XYDataset dataset, int series, int item) {
			return String.valueOf(batches[series].getLateness());
		}



		@Override
		public String generateToolTip(XYDataset dataset, int series, int item) {
			StringBuilder b = new StringBuilder();
			b.append("L=");
			b.append(batches[series].getLateness());
			b.append(" (");
			makeDurationToolTip(b, batches[series]);
			final List<BJob> jobs = batches[series].getJobs();
			final int dd = batches[series].getDueDate();
			b.append("d=").append(dd);
			for (BJob job : jobs) {
				if(job.getDueDate() ==  dd) b.append("-J").append(job.getId());
			}
			b.setCharAt(b.length()-1,')');
			return b.toString();
		}

	}


	static final class WFlowLabelToolTipGenerator implements XYItemLabelGenerator, XYToolTipGenerator {

		Batch[] batches;

		public WFlowLabelToolTipGenerator(Batch[] batches) {
			this.batches =batches;
		}

		@Override
		public String generateLabel(XYDataset dataset, int series, int item) {
			return String.valueOf(batches[series].getWeightedCompletionTime());
		}


		@Override
		public String generateToolTip(XYDataset dataset, int series, int item) {
			StringBuilder b = new StringBuilder();
			if(item > 0) {
				final int idx = item - 1;
				b.append("WxC=");
				b.append(batches[idx].getWeight()).append('x').append(batches[idx].getCompletionTime());
				b.append('=').append(batches[idx].getWeightedCompletionTime());
				b.append(" (");
				makeDurationToolTip(b, batches[idx]);
				b.append(")");
			}
			return b.toString();
		}
	}

	public final static Paint[] makePalette(int n) {
		final DrawingSupplier drawingSupplier = ChocoColor.createDefaultDrawingSupplier();
		Paint[] palette = new Color[n];
		for (int i = 0; i < n; i++) {
			palette[i]= drawingSupplier.getNextPaint();
		}
		return palette;
	}

	private static class XYBarRendererPaletteBySeries extends XYBarRenderer {
		private static final long serialVersionUID = -6248325861281106970L;
		public final Paint[] palette;


		public XYBarRendererPaletteBySeries(Paint[] palette) {
			super();
			this.palette = palette;
		}


		@Override
		public Paint getItemPaint(int row, int column) {
			if(row>= 0 && row < palette.length) return palette[row];
			else return super.getItemPaint(row, column);
		}
	}

	private static class StackedXYBarRendererPaletteByItems extends StackedXYBarRenderer {
		private static final long serialVersionUID = -4338906665755526642L;
		public final Paint[] palette;


		public StackedXYBarRendererPaletteByItems(Paint[] palette) {
			super();
			this.palette = palette;
		}


		@Override
		public Paint getItemPaint(int row, int column) {
			if(column>= 0 && column < palette.length) return palette[column];
			else return super.getItemPaint(row, column);
		}
	}


	/**
	 * Starting point for the demonstration application.
	 *
	 * @param args  ignored.
	 */
	public static void main(String[] args) {
		//data
		final BJob[] inst1 = new BJob[] {
				new BJob(1, 5, 2, 1, 7),
				new BJob(2, 6, 3, 1, 8),
				new BJob(3, 7,4, 1, 12),
				new BJob(4, 4, 1, 1, 9),
				new BJob(5, 3,2, 1, 15)
		};
		Batch[] batches = new Batch[3];
		batches[0] = new Batch(0);
		batches[0].parallelMerge(inst1[0]);
		batches[0].parallelMerge(inst1[1]);
		batches[1] = new Batch(1);
		batches[1].parallelMerge(inst1[2]);
		batches[1].parallelMerge(inst1[3]);
		batches[2] = new Batch(2);
		batches[2].parallelMerge(inst1[4]);
		//PDRScheduler.schedule1Lmax(batches);
		PDR1Scheduler.schedule1WFlow(batches);
		//frame
		ApplicationFrame demo = new ApplicationFrame("Batch Processing Demo");
		//ChartPanel chartPanel = new ChartPanel( createLmaxChart(batches, "test", 10));
		ChartPanel chartPanel = new ChartPanel( createWFlowChart(batches, "test", -1));
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 300));
		demo.setContentPane(chartPanel);
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

}
