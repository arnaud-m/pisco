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
package pisco.shop.heuristics;

import java.util.LinkedList;
import java.util.List;

import parser.instances.AbstractInstanceModel;
import choco.IPretty;
import choco.kernel.solver.variables.scheduling.TaskVar;

/**
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: 16 dc. 2007
 * Time: 17:38:55
 * To change this template use File | Settings | File Templates.
 */
public class BTask implements IPretty {

	public AbstractInstanceModel pb;
	public TaskVar task;

	//attribute of a task
	public int job;
	public int machine;

	public int idxt; 	//global index
	public int idxM;    //mapping with disjonctive, relative index on its machine
	public int idxJ;	//mapping with disjonctive, relative index in its job

	//variable denoted its start time in the
	//computed heuristic solution
	public int startd;
	public int end;
	public int posJ; //the position of the task wihin the order in the job
	public int posM; //the position of the task wihin the order in the machine


	//dealing with precedences
	//the list of tasks that have to be after this one
	public List<BTask> afterlist;

	//the number of non instantiated tasks that have to be before this
	//this one (dynamic counter updated during search)
	public int nbBefore;

	public int processtime;

	public BTask(final int job, final int machine, final int idx, final int processtime) {
		this.idxt = idx;

		this.job = job;
		this.machine = machine;

		this.idxM = job;    //the internal indexes are just reversed ? (todo: check)
		this.idxJ = machine;

		this.afterlist = new LinkedList<BTask>();
		this.processtime = processtime;
	}


	public int getDuration() {
		return processtime;//getMinDuration();
	}

	public void resetPrecList() {
		afterlist.clear();
		nbBefore=0;
	}

	public void addAfterTask(BTask t) {
		afterlist.add(t);
		t.nbBefore++;
	}

	public void updateBefore() {
		for (BTask t : afterlist) {
			t.nbBefore--;
		}
	}

	public void affectTask(final int start) {
		startd = start;
		end = start + getDuration();
	}

	@Override
	public String toString() {
		return "O(" + machine + "," + job + ")";// + duration + "[" + startd + "," + end + "]";
	}

	public String pretty() {
		return toString();
	}

	public boolean isNowPrecedenceFree() {
		return nbBefore == 0;
	}


	@Override
	public boolean equals(final Object object) {
		final BTask t = (BTask) object;
		return t.machine == machine && t.job == job;
	}

}