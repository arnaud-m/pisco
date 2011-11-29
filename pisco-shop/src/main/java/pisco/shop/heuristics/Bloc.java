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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Bloc implements Comparable<Bloc>{

	protected LinkedList<BTask> bloc;
	protected int idxBloc;

	protected LinkedList<BTask> EBj;
	protected LinkedList<BTask> EAj;
	protected LinkedList<BPrecedence> Fj;
	protected LinkedList<BPrecedence> Lj;


	//is this bloc defined by the same machine or the same job ?
	//mach == Boolean.TRUE -> same machine
	//mach == Boolean.FALSE -> same job
	//mach = null -> unknown yet
	protected boolean mach;

	public Bloc(final boolean mach) {
		this.bloc = new LinkedList<BTask>();
		this.mach = mach;
	}

	protected boolean isEmpty() {
		return bloc.isEmpty();
	}

	protected void setMachineB() {
		mach = true;
	}


	public final List<BTask> getBloc() {
		return Collections.unmodifiableList(bloc);
	}

	/**
	 * Add a task in this bloc and update the bloc definition
	 * @param t
	 */
	public void addTask(final BTask t) {
		bloc.addLast(t);
	}

	public int compareTo(final Bloc b) {
		if (bloc.size() < b.bloc.size()) {
			return 1;
		} else if (bloc.size() == b.bloc.size()) {
			return 0;
		} else {
			return -1;
		}
	}

	/**
	 * @param t
	 * @return true if t is on the same machine or same job than the
	 * tasks of this bloc
	 */
	public boolean isInBloc(final BTask t) {
		return bloc.size() < 2
		|| (mach && t.machine == bloc.getLast().machine)
		|| (!mach && t.job     == bloc.getLast().job);
	}

	public void computeEAj() {
		EAj = new LinkedList<BTask>();
		EAj.addAll(bloc);
		EAj.removeLast();
		EAj.removeFirst();
	}

	public void computeEBj() {
		EBj = new LinkedList<BTask>();
		EBj.addAll(bloc);
		EBj.removeFirst();
	}

	public void computeFj(final BSolution osol) {
		Fj = new LinkedList<BPrecedence>();
		final Iterator<BTask> it = bloc.iterator();
		final BTask ti = it.next();
		if(ti.task!=null) {
			//no solver yet
			while(it.hasNext()) {
				final BPrecedence p = osol.allprec[ti.idxt][it.next().idxt];
				if(! p.isSatisfied()) {
					Fj.add(p);
				}
			}
		}
	}

	public void computeLj(final BSolution osol) {
		Lj = new LinkedList<BPrecedence>();
		final Iterator<BTask> it = EAj.iterator();
		final BTask tl = bloc.getLast();
		if(tl.task!=null) {
			while(it.hasNext()) {
				final BPrecedence p = osol.allprec[it.next().idxt][tl.idxt];
				if(!p.isSatisfied()) {
					Lj.add(p);
				}
			}
		}
	}

	@Override
	public String toString() {
		return "Bloc("+idxBloc+"):"+ bloc;
	}

}