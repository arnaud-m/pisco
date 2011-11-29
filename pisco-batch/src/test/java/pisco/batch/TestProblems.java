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

import static choco.kernel.common.util.tools.ArrayUtils.append;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import parser.absconparseur.tools.UnsupportedConstraintException;
import pisco.batch.BatchCmd.Problem;
import pisco.batch.BatchSettings.PropagagationLevel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.ConstraintType;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;

public class TestProblems {

	private static final String PATH = "./src/main/benchmarks/daste/";
	private static final BatchCmd BCMD = new BatchCmd();

	private final static String[] CMD_PREFIX = { "--seed", "0" };

	private final static PropagagationLevel[] FAST_PROPAG = {PropagagationLevel.JOBS, PropagagationLevel.PACK };


	@BeforeClass
	public final static void setUp() {
		ChocoLogging.toQuiet();
		ChocoLogging.toSearch();
	}

	@AfterClass
	public final static void tearDown() {
		ChocoLogging.flushLogs();
		ChocoLogging.toSilent();
	}

	private void testInstances(boolean useRandom, PropagagationLevel[] levels,
			String input, String... wildcardPatterns) {
		final String[] cmdArgs = append(CMD_PREFIX,
				new String[] { "-f", input }, wildcardPatterns);
		//for (BatchCmd.Problem pb: BatchCmd.Problem.values()) {
		Problem pb = Problem.LMAX;
//		for (PropagagationLevel slevel : levels) {
//			for (PropagagationLevel plevel : levels) {
				PropagagationLevel plevel = PropagagationLevel.OBJ;
				PropagagationLevel slevel = PropagagationLevel.OBJ;
				final String[] cmdArgs2 = append(cmdArgs, new String[] {"-pb", pb.toString(), 
						"-single", slevel.toString(), 
						"-parallel", plevel.toString()
				});

				//			BCMD.doMain(append(cmdArgs2, new String[] { "-pb", pb.toString(), "-var", "DOM", "-val",
				//			"BF" }));
				BCMD.doMain(append(cmdArgs2, new String[] { "-pb", pb.toString(), "-var", "LEX", "-val",
				"FF" }));
				//			if (useRandom)
				//				BCMD.doMain(append(cmdArgs2, new String[] { "-pb", pb.toString(), "-var", "RANDOM",
				//						"-val", "FF" }));
//			}
//		}
		//}

	}

	@Test
	public void testN6() {
		testInstances(true, PropagagationLevel.values(), "./src/test/resources/bp06-01.txt");
	}

	@Test
	public void testSingleN10() {
		testInstances(true, PropagagationLevel.values(), "./src/test/resources/bp10-43.txt");
	}

	@Test
	public void testN10() {
		testInstances(true, PropagagationLevel.values(), PATH + "N_10",
				"bp10-*");
	}

	@Test
	public void testN20() {
		// testInstances(false, PropagagationLevel.values(),
		// "./src/benchmarks/dasteN_20/", "bp20-0*");
		testInstances(true, FAST_PROPAG, PATH, "bp20-0*", "bp20-2*");
	}

	@Test
	@Ignore
	public void testN50() {
		testInstances(false,
				new PropagagationLevel[] { PropagagationLevel.PACK },
				// "./src/benchmarks/dasteN_50/",
				// "bp50-01*","bp50-05*","bp50-06*","bp50-07*","bp50-09*");
				PATH + "N_50", "bp50-01*", "bp50-05*", "bp50-06*");
	}



}