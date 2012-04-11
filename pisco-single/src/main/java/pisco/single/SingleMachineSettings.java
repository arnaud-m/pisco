package pisco.single;

import java.io.File;
import java.io.IOException;

import parser.instances.AbstractInstanceModel;
import pisco.common.DisjunctiveSettings;

public class SingleMachineSettings extends DisjunctiveSettings {

	private static final long serialVersionUID = 3142246489721398046L;


	public static enum PropagagationLevel{ 
		DECOMP, OBJ, SWEEP, FULL;

		public boolean isOn() {
			return ordinal() > DECOMP.ordinal();
		}
	}

	/**
	 * <br/><b>Goal</b>: propagation level of the 1|rj;pmtn;prec|Lmax and 1|rj;pmtn|sumCi.
	 * <br/><b>Type</b>: String
	 * <br/><b>Default value</b>: DECOMP (deactivated)
	 */
	@Default(value = "DECOMP")
	public static final String RELAX_PMTN_PROPAGATION = "tools.cp.propagation.relaxation.single.preemption";

	/**
	 * <br/><b>Goal</b>: propagation level of the 1|prec|Lmax.
	 * <br/><b>Type</b>: String
	 * <br/><b>Default value</b>: DECOMP (deactivated)
	 */
	@Default(value = "DECOMP")
	public static final String RELAX_PREC_PROPAGATION = "tools.cp.propagation.relaxation.single.prec";


	/**false
	 * <br/><b>Goal</b>: 
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: true
	 */
	@Default(value = VALUE_TRUE)
	public static final String MODIFY_DUE_DATES = "tools.cp.constraint.modifyDueDate";


	/**
	 * <br/><b>Goal</b>: 
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: true
	 */
	@Default(value = VALUE_TRUE)
	public static final String TASK_ORDERING= "tools.cp.constraint.ordering";

	/**
	 * <br/><b>Goal</b>: 
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: false
	 */
	@Default(value = VALUE_TRUE)
	public static final String TASK_WEAK_ORDERING = "tools.cp.constraint.ordering.weak";


	/**
	 * <br/><b>Goal</b>: 
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: false
	 */
	@Default(value = VALUE_TRUE)
	public static final String INITIAL_LOWER_BOUND= "tools.cp.preprocess.lb";


	public static final PropagagationLevel getPmtnLevel(AbstractInstanceModel problem) {
		return problem.getConfiguration().readEnum(RELAX_PMTN_PROPAGATION, PropagagationLevel.class);
	}

	public static final PropagagationLevel getPrecLevel(AbstractInstanceModel problem) {
		return problem.getConfiguration().readEnum(RELAX_PREC_PROPAGATION, PropagagationLevel.class);
	}

	public static final boolean stateRelaxationConstraint(AbstractInstanceModel problem) {
		return getPmtnLevel(problem).isOn() || getPrecLevel(problem).isOn();
	}

//	public static void main(String[] args) throws IOException {
//		SingleMachineSettings s = new SingleMachineSettings();
//		s.storeDefault(new File("/tmp/single.properties"), null);
//	}


}
