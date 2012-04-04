package pisco.common;

public interface IJob {

	public void resetDimensions();

	public void resetTimeWindow();

	public void resetCostParameters();

	//Dimensions
	public int getDuration();

	public void setDuration(int duration);

	public int getSize();

	public void setSize(int size);

	//Time window
	public int getReleaseDate();

	public void setReleaseDate(int releaseDate);

	public int getDeadline();

	public void setDeadline(int deadline);

	//Cost parameters
	public int getWeight();

	public void setWeight(int weight);

	public int getDueDate();

	public void setDueDate(int dueDate);

	
}