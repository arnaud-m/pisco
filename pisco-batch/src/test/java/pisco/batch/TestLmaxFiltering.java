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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import parser.absconparseur.tools.UnsupportedConstraintException;
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

public class TestLmaxFiltering {

	
	@BeforeClass
	public final static void setUp() {
		ChocoLogging.setVerbosity(Verbosity.QUIET);
		//ChocoLogging.toSearch();
	}

	@AfterClass
	public final static void tearDownp() {
		ChocoLogging.setVerbosity(Verbosity.SILENT);
	}

	@AfterClass
	public final static void tearDown() {
		ChocoLogging.flushLogs();
		ChocoLogging.setVerbosity(Verbosity.SILENT);
	}

	private static final AbstractBatchingProblem LMAX_PB = new PBatchLmax(
			new BatchSettings());

	private static void load(String path) throws UnsupportedConstraintException {
		LMAX_PB.initialize();
		LMAX_PB.load(new File(path));
		LMAX_PB.preprocess();
		LMAX_PB.cancelHeuristic();
	}
	
	private Solver initSolver(PropagagationLevel level, boolean noSymBreak) {
		LMAX_PB.getConfiguration().putEnum(BatchSettings.SINGLE_PROPAGATION, level);
		final Model model = LMAX_PB.buildModel();
		if (noSymBreak) {
			final Iterator<Constraint> iter = model
					.getConstraintByType(ConstraintType.EQ);
			while (iter.hasNext()) {
				model.removeConstraint(iter.next());
			}
		}
		final Solver solver = new CPSolver();
		solver.read(model);
		return solver;
	}

	
	
	@Test
	public void testArticle1() throws ContradictionException, UnsupportedConstraintException {
		load("./src/test/resources/bp04-01.txt");
		final Solver solver = initSolver(PropagagationLevel.OBJ, false);
		solver.propagate();
		IntDomainVar lmax = (IntDomainVar) solver.getObjective();
		assertEquals(10, lmax.getInf());
	}

	@Test
	public void testArticle2() throws ContradictionException,UnsupportedConstraintException {
		load("./src/test/resources/bp04-01.txt");
		final Solver solver = initSolver(PropagagationLevel.JOBS, false);
		solver.propagate();
		final IntDomainVar lmax = (IntDomainVar) solver.getObjective();
		IntDomainVar nbB = solver.getVar(LMAX_PB.nonEmptyB);
		IntDomainVar b3 = solver.getVar(LMAX_PB.jobInB[3]);
		assertEquals(10, lmax.getInf());
		assertEquals(4, solver.getVar(LMAX_PB.jobInB[3]).getDomainSize());

		lmax.setSup(18);
		solver.propagate();
		assertEquals(3, b3.getDomainSize());
		assertFalse(b3.canBeInstantiatedTo(3));
		assertTrue(nbB.isInstantiatedTo(3));

		//		lmax.setSup(14);
		//		solver.propagate();
		//		assertEquals(2, b3.getDomainSize());
		//		assertFalse(b3.canBeInstantiatedTo(2));

		//		lmax.setSup(13);
		//		solver.propagate();
		//		assertEquals(1, b3.getDomainSize());
		//		assertFalse(b3.canBeInstantiatedTo(2));
	}

	@Test(expected = ContradictionException.class)
	public void testArticle3() throws ContradictionException, UnsupportedConstraintException {
		load("./src/test/resources/bp04-01.txt");
		final Solver solver = initSolver(PropagagationLevel.JOBS, false);
		final IntDomainVar lmax = (IntDomainVar) solver.getObjective();
		lmax.setSup(11);
		solver.propagate();
		//		Iterator<Constraint> iter = solver.getModel().getConstraintByType(ConstraintType.EQ);
		//		while (iter.hasNext()) {
		//			solver.eraseConstraint(solver.getCstr(iter.next()));
		//		}
	}

	@Test
	public void testArticle4() throws ContradictionException, UnsupportedConstraintException {
		load("./src/test/resources/bp04-01.txt");
		final Solver solver = initSolver(PropagagationLevel.PACK, true);
		final IntDomainVar lmax = (IntDomainVar) solver.getObjective();
		solver.propagate();
		assertEquals(10, lmax.getInf());
		lmax.setSup(18);
		solver.propagate();
		final IntDomainVar nbB = solver.getVar(LMAX_PB.nonEmptyB);
		assertTrue(nbB.isInstantiatedTo(3));
	}

	@Test(expected = ContradictionException.class)
	public void testArticle5() throws ContradictionException, UnsupportedConstraintException {
		load("./src/test/resources/bp04-01.txt");
		final Solver solver = initSolver(PropagagationLevel.PACK, true);
		final IntDomainVar lmax = (IntDomainVar) solver.getObjective();
		lmax.setSup(9);
		solver.propagate();
	}

	private void check(Solver solver, int lmaxSup, int... remVals)
			throws ContradictionException {
		final IntDomainVar lmax = (IntDomainVar) solver.getObjective();
		final IntDomainVar ljob = solver.getVar(LMAX_PB.jobInB[9]);
		final int dsize = ljob.getDomainSize();
		lmax.setSup(lmaxSup);
		solver.propagate();
		//System.out.println(lmax.pretty()+"\t"+ljob.pretty());
		assertEquals("Domain size of " + ljob.pretty(), dsize - remVals.length,
				ljob.getDomainSize());
		for (int i : remVals) {
			assertFalse(ljob.pretty() + " <- " + i, ljob.canBeInstantiatedTo(i));
		}
	}

	@Test
	public void testSingleJobInsertion1()
			throws UnsupportedConstraintException, ContradictionException {
		load("./src/test/resources/bp10-41.txt");
		final Solver solver = initSolver(PropagagationLevel.JOBS, false);
		final IntDomainVar lmax = (IntDomainVar) solver.getObjective();
		solver.propagate();
		// Buckets -> end, Lmax with last job
		// (13, 2)[26]( b:{7, 8} , j:{}) -> end=13 7= 8= 1=(12 + 26=38) 2=
		// (9+26=35)
		// (21, 10)[13]( b:{4, 5, 6} , j:{}) -> end=34 4= 5= 6= 3=(13 + 10 + 13
		// = 36) 4=(13 + 8 + 13 = 34) 5=(13 + 12 + 13 = 38)
		// (3, 13)[-8]( b:{3} , j:{}) -> end=37 3=(34 + 14 - 8 = 40)
		// (5, 19)[-11]( b:{2} , j:{}) -> end=42 Lmax=( 37 + 12 - 11 = 38)
		// (12, 28)[-16]( b:{0, 1} , j:{}) -> end=54 8=( 42 + 8 - 16 = 34) 9=(
		// 42 + 14 - 16 = 40)
		// (0, 30)[-30]( b:{} , j:{9})] -> end=54 Lmax=(54 + 17 - 30 = 41)
		final IntDomainVar ljob = solver.getVar(LMAX_PB.jobInB[9]);
		assertEquals(26, lmax.getInf());
		assertEquals(10, ljob.getDomainSize());
		check(solver, 40, 9);
		check(solver, 39, 5, 8);
		check(solver, 37, 1, 4, 6);
		check(solver, 35, 3);
		check(solver, 34, 0);
	}

	@Test(expected = ContradictionException.class)
	public void testInsertionFail1() throws ContradictionException,
	UnsupportedConstraintException {
		load("./src/test/resources/bp10-41.txt");
		final Solver solver = initSolver(PropagagationLevel.JOBS, false);
		solver.propagate();
		((IntDomainVar) solver.getObjective()).setSup(33);
		solver.propagate();
	}

	@Test
	public void testSingleJobInsertion2()
			throws UnsupportedConstraintException, ContradictionException {
		load("./src/test/resources/bp10-42.txt");
		final Solver solver = initSolver(PropagagationLevel.JOBS, false);
		solver.propagate();
		// Buckets -> end, Lmax with last job
		// (13, 2)[26]( b:{7, 8} , j:{}) -> end=13 7= 8= 1=(12 + 26=38) 2=
		// (9+26=35)
		// (21, 10)[13]( b:{4, 5, 6, 9} , j:{}) -> end=34 3=(13 + 10 + 13 = 36)
		// 4=(13 + 8 + 13 = 34) 5=(13 + 12 + 13 = 38) 9=(13 + 17 + 13 = 43)
		// (3, 13)[-8]( b:{3} , j:{}) -> end=37 3=max(34 + 17 -10, 34+ 17
		// -11)=41
		// (5, 19)[-11]( b:{2} , j:{}) -> end=42 2= max(34 + 17 -10, 37 + 17 -
		// 13, 42 + 12 -19)=41
		// (12, 28)[-16]( b:{0, 1} , j:{}) -> end=54 8=max(34 + 17 -10, 37 +17
		// -13, 42+17-19, 8 -16)= 41
		final IntDomainVar lmax = (IntDomainVar) solver.getObjective();
		final IntDomainVar lastJob = solver.getVar(LMAX_PB.jobInB[9]);
		assertEquals(26, lmax.getInf());
		assertEquals(10, lastJob.getDomainSize());
		check(solver, 42, 9);
		check(solver, 40, 5, 6, 7, 8);
		check(solver, 37, 1, 4);
		check(solver, 35, 3);
		check(solver, 34, 0);
	}

	@Test(expected = ContradictionException.class)
	public void testInsertionFail2() throws ContradictionException,
	UnsupportedConstraintException {
		load("./src/test/resources/bp10-42.txt");
		final Solver solver = initSolver(PropagagationLevel.JOBS, false);
		solver.propagate();
		((IntDomainVar) solver.getObjective()).setSup(33);
		solver.propagate();
	}

}