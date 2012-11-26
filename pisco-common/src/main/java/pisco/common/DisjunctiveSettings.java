package pisco.common;

import parser.instances.BasicSettings;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.variables.scheduling.TaskVar;

public class DisjunctiveSettings extends BasicSettings {

	
	public final static int JOB_EXTENSION= TaskVar.getTaskVarExtensionNumber("pisco.common.DisjunctiveSettings.job");

	public final static int MACHINE_EXTENSION= TaskVar.getTaskVarExtensionNumber("pisco.common.DisjunctiveSettings.machine");
	
	private static final long serialVersionUID = -8592947447032416433L;
	/**
	 * <br/><b>Goal</b>: branching type.
	 * <br/><b>Type</b>: Branching
	 * <br/><b>Default value</b>: ST
	 */
	@Default(value = "ST")
	public static final String BRANCHING_TYPE = "tools.branching.type";
	/**
	 * <br/><b>Goal</b>: assign starting times with bellman algorithm.
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: false
	 */
	@Default(value = VALUE_FALSE)
	public static final String ASSIGN_BELLMAN = "tools.branching.assign.bellman";

	public static final SchedulingBranchingFactory.Branching getBranching(Configuration conf) {
		return conf.readEnum(BRANCHING_TYPE, SchedulingBranchingFactory.Branching.class);
	}

	public DisjunctiveSettings() {
		super();
	}
	
	public static String getBranchingMsg(Configuration conf) {
		StringBuilder b = new StringBuilder();
		b.append(getBranching(conf)).append(" BRANCHING    ");
		if(conf.readBoolean(BasicSettings.RANDOM_VALUE)) b.append(" RAND_VAL    ");
		else if(conf.readBoolean(BasicSettings.RANDOM_TIE_BREAKING)) b.append(" RANDOMIZED_VAL    ");
		return new String(b);
	}
}