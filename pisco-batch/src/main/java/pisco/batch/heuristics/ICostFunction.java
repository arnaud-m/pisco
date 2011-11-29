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
package pisco.batch.heuristics;

import pisco.batch.data.Job;

public interface ICostFunction {

	int getCost(Job job);
	
	int getCost(Job job, int completionTime);
}


final class Lateness implements ICostFunction {

	public final static Lateness SINGLOTON = new Lateness();

	private Lateness() {
		super();
	}

	@Override
	public int getCost(Job j) {
		return j.getCompletionTime() - j.getDueDate();
	}
	
	@Override
	public int getCost(Job j, int completionTime) {
		return completionTime - j.getDueDate();
	}

}

final class CompletionTime implements ICostFunction {

	public final static CompletionTime SINGLOTON = new CompletionTime();

	private CompletionTime() {
		super();
	}

	@Override
	public int getCost(Job j) {
		return j.getCompletionTime();
	}

	@Override
	public int getCost(Job j, int completionTime) {
		return completionTime;
	}
}


final class WeightedCompletionTime implements ICostFunction {

	public final static WeightedCompletionTime SINGLOTON = new WeightedCompletionTime();

	private WeightedCompletionTime() {
		super();
	}

	@Override
	public int getCost(Job j) {
		return j.getWeight() * j.getCompletionTime();
	}

	@Override
	public int getCost(Job j, int completionTime) {
		return j.getWeight() * completionTime;
	}
}



