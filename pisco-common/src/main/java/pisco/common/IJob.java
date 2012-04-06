package pisco.common;


public interface IJob {

	void resetDimensions();

	void resetTimeWindow();

	void resetCostParameters();

	void reset();

	//Dimensions
	int getDuration();

	void setDuration(int duration);

	int getSize();

	void setSize(int size);

	//Time window
	int getReleaseDate();

	void setReleaseDate(int releaseDate);

	int getDeadline();

	void setDeadline(int deadline);

	//Cost parameters
	int getWeight();

	void setWeight(int weight);

	int getDueDate();

	void setDueDate(int dueDate);


}