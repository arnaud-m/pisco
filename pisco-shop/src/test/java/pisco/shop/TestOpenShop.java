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
package pisco.shop;

import static choco.kernel.common.util.tools.ArrayUtils.append;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import pisco.common.SchedulingBranchingFactory;
import pisco.common.SchedulingBranchingFactory.Branching;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;

public class TestOpenShop {

	private final ShopCmd cmd = new ShopCmd();

	private final static String PATH = "./src/main/benchmarks/instances/open-shop/";

	private final static String[] CMD_PREFIX = {"--seed","0","-t", "OSP"};

	private static final int NB_CONFS = 6;

	@BeforeClass
	public final static void setUp() {
		ChocoLogging.setVerbosity(Verbosity.QUIET);
	}

	@AfterClass
	public final static void tearDown() {
		ChocoLogging.setVerbosity(Verbosity.SILENT);
	}

	private void testInstances(String input, String... wildcardPatterns) {
		testInstances(input,SchedulingBranchingFactory.Branching.values(), wildcardPatterns);
	}

	private void testInstances(String input, String[] wildcardPatterns,SchedulingBranchingFactory.Branching br, int prop) {
		cmd.doMain(
				append(CMD_PREFIX, new String[] {
						"-f",input, 
						"-b", br.toString(), 
						"-p" , "./src/test/resources/shop"+prop+".properties"}, 
						wildcardPatterns
						)
				);
	}

	private void testInstances(String input,SchedulingBranchingFactory.Branching[] branchings, String... wildcardPatterns) {
		for (SchedulingBranchingFactory.Branching br : branchings) {
			if(br != SchedulingBranchingFactory.Branching.USR) {
				for (int i = 1; i <= NB_CONFS; i++) {
					testInstances(input, wildcardPatterns, br, i);
				}
			}
		}
		// FIXME - Bug Profile  - created 16 sept. 2011 by Arnaud Malapert
	}

	@Test
	public void testGP() {
		testInstances(PATH+"gueret-prins/", "GP04-*");
	}

	@Test
	public void testTai() {
		testInstances(PATH+"taillard/", "tai04_04_02.txt", "tai05_05_02.txt");
	}

	@Test
	public void testJ() {
		testInstances(PATH+"/brucker/", "j3-per*","j4-per*");
	}

	@Test
	public void testBug() {
		//ChocoLogging.setVerbosity(Verbosity.VERBOSE);
		testInstances(PATH+"/gueret-prins/", new SchedulingBranchingFactory.Branching[]{SchedulingBranchingFactory.Branching.ST}, "GP04-01*");
	}

	@Test
	public void testLargeInstances() {
		testInstances(PATH+"taillard/", "tai15_15_05.txt");
	}
}

