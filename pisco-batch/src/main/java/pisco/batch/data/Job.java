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
package pisco.batch.data;

import static choco.Choco.MAX_UPPER_BOUND;

public class Job {

	// TODO - reset the schedule when the duration of the job has changed to reset . - created 4 nov. 2011 by Arnaud Malapert
	public final int id;
	protected int duration;
	protected int size;
	protected int weight;
	protected int dueDate;
	protected int startingTime;
	protected int completionTime;

	public Job(int id) {
		this(id,0,0,0, MAX_UPPER_BOUND);
	}
	
	public Job(int id, int duration, int dueDate) {
		this(id, duration, 1, 1, dueDate);
	}
	
	public Job(int id, int duration, int weight, int dueDate) {
		this(id, duration, 1, weight, dueDate);
	}
	
	public Job(int id, int duration, int size, int weight, int dueDate)
	{
		this.id = id;
		this.duration = duration;
		this.size = size;
		this.weight = weight;
		this.dueDate = dueDate;
	}

	public void clear()
	{
		this.startingTime = (this.completionTime = 0);
	}
	
	public final int getId() {
		return this.id;
	}

	public final int getDuration()
	{
		return this.duration;
	}

	public final void setDuration(int duration) {
		this.duration = duration;
	}

	public final int getSize() {
		return this.size;
	}

	public final void setSize(int size) {
		this.size = size;
	}

	public final int getWeight() {
		return this.weight;
	}

	public final void setWeight(int weight) {
		this.weight = weight;
	}

	public final int getDueDate()
	{
		return this.dueDate;
	}

	public final void setDueDate(int dueDate) {
		this.dueDate = dueDate;
	}
	
	public int getCardinality() {
		return 1;
	}
	

	public final int getCompletionTime() {
		return this.completionTime;
	}

	public final int getStartingTime()
	{
		return this.startingTime;
	}

	public final void setStartingTime(int startingTime) {
		this.startingTime = startingTime;
		this.completionTime = (startingTime + this.duration);
	}

	protected final void setCompletionTime(int completionTime) {
		this.completionTime = completionTime;
		this.startingTime = (this.completionTime - this.duration);
	}

	public final int getLateness() {
		return getCompletionTime() - getDueDate();
	}

	public final int getTardiness()
	{
		final int t = getLateness();
		return t > 0 ? t : 0;
	}

	public final int getWeightedCompletionTime() {
		return getWeight() * getCompletionTime();
	}

	
	
	public void combine(Job j1, Job j2) {
		duration = Math.max(j1.getDuration(), j2.getDuration());
		size = j1.getSize() + j2.getSize();
		weight = j1.getWeight() + j2.getWeight();
		dueDate = Math.min(j1.getDueDate(), j2.getDueDate());
	}
	
	public void pack(Job job) {
		if (duration < job.duration) duration = job.duration;
		if (dueDate > job.dueDate) dueDate = job.dueDate;
		weight += job.weight;
		size += job.size;
	}
	
	@Override
	public String toString()
	{
		String descr = this.id + "( p=" + this.duration + ", w=" + this.weight + ", d=" + this.dueDate + (this.size == 0 ? ")" : new StringBuilder(")[").append(this.size).append("]").toString());
		return this.completionTime > 0 ? "{TW=[" + this.startingTime + ", " + this.completionTime + "] ," + descr + "}" : descr;
	}

}
