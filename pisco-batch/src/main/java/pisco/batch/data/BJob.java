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

import pisco.common.NPJob;

// TODO - Remove class - created 5 avr. 2012 by A. Malapert
public class BJob extends NPJob {

	

	public BJob(int id) {
		super(id);
	}
	
	public BJob(int id, int duration, int dueDate) {
		this(id, duration, 1, 1, dueDate);
	}
	
	public BJob(int id, int duration, int weight, int dueDate) {
		this(id, duration, 1, weight, dueDate);
	}
	
	public BJob(int id, int duration, int size, int weight, int dueDate)
	{
		super(id, duration);
		setSize(size);
		setWeight(weight);
		setDueDate(dueDate);
	}

	public void clear()
	{
		resetSchedule();
	}
	
	public final int getId() {
		return this.id;
	}
	
	public int getCardinality() {
		return 1;
	}
	

	public final int getCompletionTime() {
		return this.getLCT();
	}

	public final int getStartingTime()
	{
		return this.getEST();
	}

	public final void setStartingTime(int startingTime) {
		scheduleFrom(startingTime);
	}

	protected final void setCompletionTime(int completionTime) {
		scheduleTo(completionTime);
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

	
	
	
}
