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

import static choco.Choco.MAX_UPPER_BOUND;
import static choco.Choco.constant;
import static pisco.common.JobComparators.getCompositeComparator;
import static pisco.common.JobComparators.getDecreasingSize;
import static pisco.common.JobComparators.getWeightedShortestProcessingTime;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import parser.absconparseur.tools.UnsupportedConstraintException;
import parser.instances.ResolutionStatus;
import pisco.batch.visu.BatchingChartFactory;
import pisco.common.PDR1Scheduler;
import pisco.common.PriorityDispatchingRule;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Configuration;

public class PBatchWFlow extends AbstractBatchingProblem {

	protected int computedHorizon;

	public PBatchWFlow(Configuration settings) {
		super( settings);
	}
	@Override
	public PriorityDispatchingRule getPriorityDispatchingRule() {
		return PDR1Scheduler.WSPT;
	}
	
	
	@Override
	public void initialize() {
		super.initialize();
		computedHorizon = MAX_UPPER_BOUND;
	}
	
	
	@Override
	public void load(File fichier) throws UnsupportedConstraintException {
		super.load(fichier);
		preprocessData();
	
	}
	
	protected void preprocessData() {
		data.preprocess(getCompositeComparator(getDecreasingSize(), Collections.reverseOrder(getWeightedShortestProcessingTime())));
	}
	
	@Override
	public Boolean preprocess() {
		final Boolean r = super.preprocess();
		// TODO - Improve Lower bound computation - created 12 oct. 2011 by Arnaud Malapert
		setComputedLowerBound(0);
		return r;
	}
	// DONE 12 oct. 2011 - Remove Due Date constraints - created 12 oct. 2011 by Arnaud Malapert
	@Override
	protected void setDueDateConstraints(Model model) {
		batchDueDates = new IntegerVariable[getM()];
		Arrays.fill(batchDueDates, constant(MAX_UPPER_BOUND));
	}

	
	
	@Override
	protected String getPrefixMsg() {
		return super.getPrefixMsg() + (data.testUnitWeights() ? " FLOWTIME" : " WEIGHTED_FLOWTIME") ;
	}

	@Override
	protected Object makeSolutionChart() {
		final ResolutionStatus s = getStatus();
		return s == ResolutionStatus.SAT || s == ResolutionStatus.OPTIMUM ?  
				BatchingChartFactory.createWFlowChart(bestSolution, getInstanceName()+" - WFlow="+getObjectiveValue(), data.capacity) : null;

	}

}
