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
package pisco.pack;

import static choco.kernel.common.util.tools.ArrayUtils.append;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;

public class TestBinPacking {

	private final BinPackingCmd cmd = new BinPackingCmd();

	private final static String PATH = "./src/main/benchmarks/instances/scholl/set_1/";

	private final static String[] CMD_PREFIX = {"--seed","0", "--timeLimit", "5"};
	
	@BeforeClass
	public final static void setUp() {
		ChocoLogging.setVerbosity(Verbosity.QUIET);
		//ChocoLogging.setVerbosity(Verbosity.VERBOSE);
	}
	
	@AfterClass
	public final static void tearDown() {
		ChocoLogging.setVerbosity(Verbosity.SILENT);
	}

		
	private void testInstances(String input, String... wildcardPatterns) {
		cmd.doMain(
				append(CMD_PREFIX, new String[] {
						"-f",input, 
						//// FIXME -  use different packing algorithms within junit - created 15 sept. 2011 by Arnaud Malapert 
						}, 
						wildcardPatterns
				)
		);
	}
	
	@Test
	public void testSmall() {
		testInstances(PATH, "N1C1W1_*");
	}

	@Test
	public void testMiddle() {
		testInstances(PATH, "N2C3W2_*.txt");
	}

	@Test
	public void testLarge() {
		testInstances(PATH,"N4C3W4_A.txt", "N4C3W4_B.txt", "N4C3W4_C.txt");
	}
	
	
}

