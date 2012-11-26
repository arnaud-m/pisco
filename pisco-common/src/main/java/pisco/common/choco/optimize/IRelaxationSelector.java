package pisco.common.choco.optimize;

public interface IRelaxationSelector<D> {
	
	void clear();
	
	void inspect(D decision, int zmax);
	
	D selectBestDecision();
	
//	D getBestRepairDecision();
//	
//	D getBestCostDecision();
//	
//	D getBestRegretDecision();
	
}
