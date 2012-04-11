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

public class TestInstances {

	private final SingleMachineCmd cmd = new SingleMachineCmd();

	private final static String PATH = "./src/main/benchmarks/instances/Ti/";

	private final static String[] CMD_PREFIX = {"--seed","0","-t", "LP"};

	//private final static String[] CONFS = {"basic", "pmtn", "prec"};
	private final static String[] CONFS = {"basic"};
	
		
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
		testInstances(input,SchedulingBranchingFactory.Branching.values(), wildcardPatterns);
	}
	
	private void testInstances(String input, String[] wildcardPatterns,SchedulingBranchingFactory.Branching br, int prop) {
		cmd.doMain(
				append(CMD_PREFIX, new String[] {
						"-f",input, 
						"-b", br.toString(), 
						"-p" , "./src/main/resources/"+CONFS[prop]+".properties"}, 
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
	public void test20() {
		testInstances(PATH, branchings, "p20_2*.dat");
	}
	@Test
	public void testModifyDueDate() throws ContradictionException {
		ChocoLogging.toVerbose();
		Model m =new CPModel();
		final IntegerVariable dir = makeBooleanVar("b");
		m.addConstraint(new ComponentConstraint(ModifyDueDateManager.class, null,
				new IntegerVariable[]{makeIntVar("D1", 2,20), constant(10), makeIntVar("D2", 3,18), constant(5), dir}
				));
		
		Solver s = new CPSolver();
		s.read(m);
		s.propagate();
		s.getVar(dir).instantiate(1, null, false);	
		s.propagate();
		//System.out.println(s.pretty());
	}
	}
