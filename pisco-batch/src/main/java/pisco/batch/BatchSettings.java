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

import parser.instances.AbstractInstanceModel;
import parser.instances.BasicSettings;
import choco.kernel.solver.Configuration;

public class BatchSettings extends BasicSettings {

	private static final long serialVersionUID = 159175319915710394L;


	public static enum PropagagationLevel{ 
		DECOMP, OBJ, JOBS, PACK;
		
		public boolean isOn() {
			return ordinal() > DECOMP.ordinal();
		}
	}
	
	public static enum VarSel { 
		DOM("MinDom"), 
		LEX("Lex"), 
		MMC("Max(Min-Cost)"),
		MSC("Max(Sum-Cost"),
		RANDOM("Random");

		private final String description;

		private VarSel(String description) {
			this.description = description;
		}

		public final String getDescription() {
			return description;
		}

	}

	public static enum ValSel { 
		FF("First-Fit"), 
		BF("Best-Fit"), 
		BBF("Batch-Fit"), 
		MC("Min-Cost"), 
		RANDOM("Random");

		private final String description;

		private ValSel(String description) {
			this.description = description;
		}

		public final String getDescription() {
			return description;
		}

		@Override
		public String toString() {
			return description;
		}

	}

	/**
	 * <br/><b>Goal</b>: branching with dynamic removals.
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: true
	 */
	@Default(value = VALUE_TRUE)
	public static final String DYNAMIC_REMOVALS = "tools.branching.dynamic_removals";


	/**
	 * <br/><b>Goal</b>: variable-selection heuristics.
	 * <br/><b>Type</b>: String
	 * <br/><b>Default value</b>: LEX
	 */
	@Default(value = "LEX")
	public static final String VAR_SEL = "tools.branching.selection.variable";

	/**
	 * <br/><b>Goal</b>: value-selection heuristics.
	 * <br/><b>Type</b>: String
	 * <br/><b>Default value</b>: FF
	 */
	@Default(value = "FF")
	public static final String VAL_SEL = "tools.branching.selection.value";

	/**
	 * <br/><b>Goal</b>: propagation level of the single machines relaxation
	 * <br/><b>Type</b>: String
	 * <br/><b>Default value</b>: OBJ
	 */
	@Default(value = "OBJ")
	public static final String SINGLE_PROPAGATION = "tools.cp.propagation.relaxation.single";

	/**
	 * <br/><b>Goal</b>: propagation level of the parallel machines relaxation.
	 * <br/><b>Type</b>: String
	 * <br/><b>Default value</b>: DECOMP (deactivated)
	 */
	@Default(value = "DECOMP")
	public static final String PARALLEL_PROPAGATION = "tools.cp.propagation.relaxation.parallel";

	/**
	 * <br/><b>Goal</b>: parallel relaxation with unit or preemptive tasks.
	 * <br/><b>Type</b>: String
	 * <br/><b>Default value</b>: false
	 */
	@Default(value = VALUE_FALSE)
	public static final String USE_PARALLEL_UNIT_RELAXATION= "tools.cp.propagation.relaxation.parallel.unit";
	
	/**
	 * <br/><b>Goal</b>: version of the Lmax filtering algorithms based on buckets.
	 * <br/><b>Type</b>: String
	 * <br/><b>Default value</b>: false
	 */
	@Default(value = VALUE_FALSE)
	public static final String USE_BUCKETS = "tools.cp.propagation.relaxation.lmax.bucket";

	public BatchSettings() {
		super();
	}

	public static VarSel getVarSel(Configuration conf) {
		return conf.readEnum(VAR_SEL, VarSel.class);
	}

	public static ValSel getValSel(Configuration conf) {
		return conf.readEnum(VAL_SEL, ValSel.class);
	}

	
	public static final PropagagationLevel getSinglePropagationLevel(AbstractInstanceModel problem) {
		return problem.getConfiguration().readEnum(SINGLE_PROPAGATION, PropagagationLevel.class);
	}
	
	public static final PropagagationLevel getParallelPropagationLevel(AbstractInstanceModel problem) {
		return problem.getConfiguration().readEnum(PARALLEL_PROPAGATION, PropagagationLevel.class);
	}
	
	public static boolean useBuckets(AbstractInstanceModel problem) {
		return problem.getConfiguration().readBoolean(USE_BUCKETS);
	}
	
	public static boolean usePunitRelaxation(AbstractInstanceModel problem) {
		return problem.getConfiguration().readBoolean(USE_PARALLEL_UNIT_RELAXATION);
	}

	public static String getFilteringMsg(Configuration conf, String prefix) {
		final StringBuilder b = new StringBuilder();
		b.append(prefix);
		PropagagationLevel slevel = conf.readEnum(SINGLE_PROPAGATION, PropagagationLevel.class);
		PropagagationLevel plevel = conf.readEnum(PARALLEL_PROPAGATION, PropagagationLevel.class);
		b.append("    SINGLE_MACHINE ").append(slevel);
		if(slevel.isOn() && conf.readBoolean(USE_BUCKETS) ) {
			b.append("(BUCKET)");
		}
		b.append("    PARALLEL_MACHINES ").append(plevel);
		if(plevel.isOn()) {
			b.append(conf.readBoolean(USE_PARALLEL_UNIT_RELAXATION) ? "(UNIT)" : "(PMTN)");
		}
		return new String(b);
	}
	public static String getBranchingMsg(Configuration conf) {
		final StringBuilder b = new StringBuilder();
		b.append("VARSEL ").append(getVarSel(conf).getDescription());
		b.append("    VALSEL ").append(getValSel(conf).getDescription());
		if( conf.readBoolean(DYNAMIC_REMOVALS)) {
			b.append("    DYN_REMOVALS");
		}
		return new String(b);
	}






}

