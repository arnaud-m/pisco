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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import choco.Choco;


public class Batch extends Job {

	protected final LinkedList<Job> jobs = new LinkedList<Job>();

	public Batch(int id) {
		super(id);
	}
	
	@Override
	public void clear() {
		super.clear();
		this.jobs.clear();
		this.duration = (this.size = this.weight = 0);
		this.dueDate = Choco.MAX_UPPER_BOUND;
	}

	@Override
	public final int getCardinality() {
		return this.jobs.size();
	}

	public final Job getJob(int idx) {
		return this.jobs.get(idx);
	}

	public final List<Job> getJobs() {
		return Collections.unmodifiableList(this.jobs);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(super.toString());
		b.append("/jobs{");
		for (Job j : this.jobs) {
			b.append(j.getId()).append(' ');
		}
		b.deleteCharAt(b.length() - 1);
		b.append("}");
		return b.toString();
	}



	public boolean canPack(Job job, int capacity) {
		return getSize() + job.getSize() <= capacity;
	}

	public void pack(Job job) {
		if (this.duration < job.duration) this.duration = job.duration;
		if (this.dueDate > job.dueDate) this.dueDate = job.dueDate;
		this.weight += job.weight;
		this.size += job.size;
		this.jobs.add(job);
	}


}
