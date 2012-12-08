package pisco.shop.choco.branching.nogood;

import choco.kernel.solver.variables.integer.IntDomainVar;

public final class Term {


	public final IntDomainVar litteral;

	public final boolean positive;

	public Term(IntDomainVar value, boolean positive) {
		super();
		this.litteral = value;
		this.positive = positive;
	}


	public final IntDomainVar getLitteral() {
		return litteral;
	}


	public final boolean isPositive() {
		return positive;
	}


	@Override
	public String toString() {
		return (positive ? "" : "~")+ litteral;
	}


}