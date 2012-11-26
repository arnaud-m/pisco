package pisco.common.choco.optimize;

public interface IRelaxationSolution<S> {
	
	boolean isFeasible();
	
	void updateIncumbent(int zmax);
	
}
