package pisco.common;

import choco.Choco;

public class Job {
	
	public final int id;
	
	private int duration = 0;
	private int size = 1;
	
	//time windows
	private int releaseDate = 0;
	private int deadline = Choco.MAX_UPPER_BOUND;
	
	//objective
	private int weight = 1;
	private int dueDate = Choco.MAX_UPPER_BOUND;
	
	//schedule
	protected int startingTime;
	protected int completionTime;
	
	public Job(int id) {
		super();
		this.id = id;
	}
	
	

}
