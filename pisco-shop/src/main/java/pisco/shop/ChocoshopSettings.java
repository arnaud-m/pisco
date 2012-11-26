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
/**
 *
 */
package pisco.shop;

import java.io.File;
import java.io.IOException;

import pisco.common.DisjunctiveSettings;
import pisco.common.SchedulingBranchingFactory;
import pisco.shop.OpenShopProblem.CutOS;
import choco.kernel.common.opres.heuristics.AbstractRandomizedHeuristic;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.Configuration.Default;

public final class ChocoshopSettings extends DisjunctiveSettings {
	private static final long serialVersionUID = 936045358571219202L;

	public static enum Heuristics {SPT, LPT, CROSH, LEX}

	//TODO Add settings for forbidden intervals


	//****************************************************************//
	//********* Heuristics *******************************************//
	//****************************************************************//


	/**
	 * <br/><b>Goal</b>: heuristics type.
	 * <br/><b>Type</b>: Heuristics
	 * <br/><b>Default value</b>: LPT
	 */
	@Default(value ="LPT")
	public static final String HEURISTICS_TYPE = "tools.preprocessing.heuristics.type";

	/**
	 * <br/><b>Goal</b>: maximum number of iterations: heuristics.
	 * <br/><b>Type</b>: int
	 * <br/><b>Default value</b>: 1000
	 */
	@Default(value = "1000")
	public static final String HEURISTICS_ITERATIONS = "tools.preprocessing.heuristics.limit.iteration";

	/**
	 * <br/><b>Goal</b>: indicates if we should use some learned parameters for the heuristics.
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: false
	 */
	@Default(value = VALUE_FALSE)
	public static final String HEURISTICS_LEARNING  = "tools.preprocessing.heuristics.learning";

	/**
	 * <br/><b>Goal</b>: initial cut (open shop only).
	 * <br/><b>Type</b>: CutOS
	 * <br/><b>Default value</b>: OFF
	 */
	@Default(value = VALUE_OFF)
	public static final String INITIAL_CUT = "tools.cp.model.initial_cut";
	
	/**
	 * <br/><b>Goal</b>: Enable nogood recording from solution.
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: false
	 */
	@Default(value = VALUE_FALSE)
	public static final String NOGOOD_RECORDING_FROM_SOLUTION = "cp.solution.nogood_recording";

//*****************************************************************//
//*******************  Constructors ******************************//
//***************************************************************//

	public ChocoshopSettings() {
		super();
	}

	public static final void setLimits(Configuration conf, AbstractRandomizedHeuristic heuristics) {
		heuristics.setIterationLimit(conf.readInt(HEURISTICS_ITERATIONS));
		heuristics.setTimeLimit(conf.readInt(PREPROCESSING_TIME_LIMIT));
	}
	
	public  static final CutOS getInitialCut(Configuration conf) {
		return conf.readEnum(INITIAL_CUT, CutOS.class);
	}
	
	public  static final Heuristics getHeuristics(Configuration conf) {
		return conf.readEnum(HEURISTICS_TYPE, Heuristics.class);
	}
	
	


	//****************************************************************//
	//********* toString *******************************************//
	//****************************************************************//


	public  static String getHeuristicsMsg(Configuration conf) {
		StringBuilder b = new StringBuilder();
		Heuristics h = getHeuristics(conf);
		b.append(h); 
		if( h == Heuristics.CROSH) {
			if( ! conf.readBoolean(HEURISTICS_LEARNING) ) {
				b.append('(').append(conf.readString(PREPROCESSING_TIME_LIMIT)).append(", ");
				b.append(conf.readString(HEURISTICS_ITERATIONS)).append(')');
			}
		}
		b.append(" HEURISTICS    ");
		return new String(b);
	}
	
	public static String getBranchingMsg(Configuration conf) {
		StringBuilder b = new StringBuilder();
		b.append(DisjunctiveSettings.getBranchingMsg(conf));
		b.append(conf.readString(INITIAL_CUT)).append(" INITIAL_CUT");
		return new String(b);
	}
	
	

//	public static void main(String[] args) throws IOException {
//		ChocoshopSettings s = new ChocoshopSettings();
//		s.storeDefault(new File("/tmp/shop1.properties"), null);
//	}

}


