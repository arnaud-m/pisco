package pisco.single;

import parser.instances.AbstractInstanceModel;
import pisco.common.DisjunctiveSettings;
import choco.kernel.solver.Configuration;

public class SingleMachineSettings extends DisjunctiveSettings {

	private static final long serialVersionUID = 3142246489721398046L;


	public static enum PropagagationLevel{ 
		NONE, OBJ, SWEEP, FULL;

		public boolean isOn() {
			return ordinal() > NONE.ordinal();
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


	public static final PropagagationLevel readPmtnLevel(AbstractInstanceModel problem) {
		return problem.getConfiguration().readEnum(RELAX_PMTN_PROPAGATION, PropagagationLevel.class);
	}

	public static final PropagagationLevel readPrecLevel(AbstractInstanceModel problem) {
		return problem.getConfiguration().readEnum(RELAX_PREC_PROPAGATION, PropagagationLevel.class);
	}

	public static final boolean stateRelaxationConstraint(AbstractInstanceModel problem) {
		return readPmtnLevel(problem).isOn() || readPrecLevel(problem).isOn();
	}
	
	public static String getInstModelMsg(Configuration conf) {
		StringBuilder b = new StringBuilder();
		if(conf.readBoolean(MODIFY_DUE_DATES)) b.append(" MODIFY_DUE_DATES    ");
		if(conf.readBoolean(TASK_ORDERING)) b.append(" TASK_ORDERING    ");
		if(conf.readBoolean(TASK_WEAK_ORDERING)) b.append(" TASK_WEAK_ORDERING    ");
		PropagagationLevel level = conf.readEnum(RELAX_PMTN_PROPAGATION, PropagagationLevel.class);
		b.append(level).append(" PMTN_RELAX    ");
		level = conf.readEnum(RELAX_PREC_PROPAGATION, PropagagationLevel.class);
		b.append(level).append(" PREC_RELAX    ");
		return b.toString();
	}

//	public static void main(String[] args) throws IOException {
//		SingleMachineSettings s = new SingleMachineSettings();
//		s.storeDefault(new File("/tmp/single.properties"), null);
//	}


}
