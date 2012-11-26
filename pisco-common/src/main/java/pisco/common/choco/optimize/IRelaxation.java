package pisco.common.choco.optimize;


public interface IRelaxation<D>  {

	/**
	 * builds the instance R(D) from any current domain

	 */
	void buildRelaxation();

	/**
	 * returns the optimal value of this instance.
	 * @return the optimal value of this instance.
	 */
	int bound();

	void buildDecisions();
	
	boolean hasNextDecision();

	D getNextDecision();
	
	
	void buildRelaxation(D decision);
	
	void revertRelaxation(D decision);


	/**
	 * builds instance R(Dk ) then returns (bound(Dk ) > Zmax ) as a boolean. 
	 * @param decision
	 * @param zmax
	 * @return
	 */
	boolean probeDecision(D decision, int zmax);

	
	/**
	 * applyDecision(D, Dk ) returns a restricted domain D with Dk.
	 * @param decision
	 */
	void applyDecision(D decision);
	
	/**
	 * pruneDecision(D, Dk ) returns a restricted domain D with D\Dk.
	 * @param decision
	 */
	void pruneDecision(D decision);
	
	
	/**
	 * returns the sublist of decisionList(D) of decisions Dk such that probeDecision(Dk , Zmax ) is true.
	 */
	void filter(int zmax);

	
}
