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

public class TestModels {

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
