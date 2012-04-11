package pisco.single;

import static choco.Choco.constant;
import static choco.Choco.makeBooleanVar;
import static choco.Choco.makeIntVar;
import static choco.kernel.common.util.tools.ArrayUtils.append;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import pisco.common.SchedulingBranchingFactory;
import pisco.single.choco.constraints.ModifyDueDateManager;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;

public class TestSingleMachine {

	private final SingleMachineCmd cmd = new SingleMachineCmd();

	private final static String PATH = "./src/main/benchmarks/instances/Ti/";

	private final static String[] CMD_PREFIX = {"--seed","0","-t", "LP"};

	private final static String[] CONFS = {"basic", "pmtn", "prec"};
	//private final static String[] CONFS = {"basic"};
	
		
	@BeforeClass
	public final static void setUp() {
		//ChocoLogging.setVerbosity(Verbosity.QUIET);
		ChocoLogging.setVerbosity(Verbosity.VERBOSE);
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
						"-p" , "./src/test/resources/"+CONFS[prop]+".properties"}, 
						wildcardPatterns
				)
		);
	}
	
	private void testInstances(String input,SchedulingBranchingFactory.Branching[] branchings, String... wildcardPatterns) {
		for (int i = 0; i < CONFS.length; i++) {
			for (SchedulingBranchingFactory.Branching br : branchings) {
				testInstances(input, wildcardPatterns, br, i);
			}
		}
		// FIXME - Bug Profile  - created 16 sept. 2011 by Arnaud Malapert
	}

	private SchedulingBranchingFactory.Branching[] branchings = new SchedulingBranchingFactory.Branching[]{
			SchedulingBranchingFactory.Branching.RAND, 
			SchedulingBranchingFactory.Branching.LEX
			};
	
	
	@Test
	public void testN10() {
		testInstances(PATH, branchings, "p10_1*.dat");
	}

	@Test
	public void testN20() {
		testInstances(PATH, branchings, "p20_2.dat", "p20_200.dat", "p20_205.dat", "p20_23*.dat");
	}
	
	@Test
	public void testRecordSolutionBug() {
		String arguments = "-t LP -f src/main/benchmarks/instances/Ti/  p20_233.dat " +
				"-b RAND -p src/test/resources/pmtn.properties -s -1930858313";
		cmd.doMain(arguments.split("\\s"));
	}
	
}
	
