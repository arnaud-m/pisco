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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import pisco.shop.GenericShopProblem;

import choco.cp.solver.CPSolver;
import choco.kernel.solver.ContradictionException;



public class BSolution {

	public GenericShopProblem shop;

	public int nbM; // the number of machine
	public int nbJ; // the number of Jobs

	public final BPrecedence[][] allprec;
	protected List<BPrecedence> allprec_list;


	public final BTask[] tasks;
	public final BTask[][] tasks_mat;

	// The matrix specifying the order of the tasks
	// on each machine
	//DIM: nbMachines * nbJobs
	public BTask[][] machineSol;
	//the number of tasks assigned on each machine
	public int[] machineLevel;


	// The matrix specifying the order of the tasks
	// within each job
	//DIM: nbJobs * nbMachines
	public BTask[][] jobSol;
	//the number of tasks assigned on each job
	public int[] jobLevel;

	//the value of the objectif and the last task responsible
	//for it (last element of a critical path)
	public int obj = Integer.MIN_VALUE;
	public BTask ft;

	//A critical path of the solution
	public LinkedList<BTask> criticalPath;

	public List<Bloc> cblocs;


	public BSolution(final GenericShopProblem ops) {
		int cpt = 0;
		this.shop = ops;
		nbM = ops.nbMachines;
		nbJ = ops.nbJobs;
		tasks = new BTask[nbM * nbJ];
		tasks_mat = new BTask[nbM][nbJ];
		for (int i = 0; i < nbM; i++) {
			for (int j = 0; j < nbJ; j++) {
				tasks[cpt] = new BTask(j, i, cpt, ops.processingTimes[i][j]);
				tasks_mat[i][j] = tasks[cpt];
				cpt++;
			}
		}
		allprec = new BPrecedence[cpt][cpt];
		allprec_list = new LinkedList<BPrecedence>();

		machineSol   = new BTask[nbM][nbJ];
		machineLevel = new int[nbM];
		jobSol       = new BTask[nbJ][nbM];
		jobLevel     = new int[nbJ];
		criticalPath = new LinkedList<BTask>();
		cblocs       = new LinkedList<Bloc>();
	}

	public final void initAllPrec(IBPrecFactory factory) {
		if(factory!=null) {
			for (int i = 0; i < tasks.length; i++) {
				for (int j = i + 1; j < tasks.length; j++) {
					if(tasks[i].job == tasks[j].job || tasks[i].machine == tasks[j].machine ) {
						//same job or same machine
						allprec[i][j] = factory.makeBPrecedence(tasks[i], tasks[j]);
						allprec[j][i] = factory.makeBPrecedence(tasks[j], tasks[i]);
						allprec[i][j].setOpposite(allprec[j][i]);
						allprec[j][i].setOpposite(allprec[i][j]);
						if(allprec[i][j].isChecked()) {
							allprec_list.add(allprec[i][j]);
						}
					}
				}
			}
		}
	}


	public LinkedList<BTask> createBTaskList() {
		return new LinkedList<BTask>(Arrays.asList(tasks));
	}


	public void reinitSol() {
		obj = Integer.MIN_VALUE;
		ft = null;
		Arrays.fill(machineLevel,0);
		Arrays.fill(jobLevel,0);
		criticalPath.clear();
		cblocs.clear();
		for (int i = 0; i < tasks.length; i++) {
			tasks[i].resetPrecList();
		}
		for(BPrecedence p : allprec_list) {
			if (p.isSatisfied()) {
				p.t1.addAfterTask(p.t2);
			} else if (p.opposite.isSatisfied()) {
				p.t2.addAfterTask(p.t1);
			}
		}
	}

	public void setSolver(final CPSolver solver) {
		for (int i = 0; i < nbM; i++) {
			for (int j = 0; j < nbJ; j++) {
				tasks_mat[i][j].task= (solver).getVar(shop.getTask(i, j));
			}
		}
	}

	protected void noSolver() {
		for (int i = 0; i < nbM; i++) {
			for (int j = 0; j < nbJ; j++) {
				tasks_mat[i][j].task= null;
			}
		}
	}

	public void assignTask(final BTask t, final int startdate) {
		t.affectTask(startdate);
		if (t.end > obj) {
			obj = t.end;
			ft = t;
		}
		t.posM = machineLevel[t.machine];
		machineSol[t.machine][machineLevel[t.machine]] = t;
		machineLevel[t.machine] += 1;

		t.posJ = jobLevel[t.job];
		jobSol[t.job][jobLevel[t.job]] = t;
		jobLevel[t.job] += 1;
	}

	//******************************************************************//
	//**************** Project the solution on the variables ***********//
	//******************************************************************//

	public void projectSolution() throws ContradictionException {
		for (int i = 0; i < tasks.length; i++) {
			tasks[i].task.start().instantiate(tasks[i].startd,-1);
			tasks[i].task.end().instantiate(tasks[i].end,-1);
		}
	}

	//******************************************************************//
	//**************** critical path computation ***********************//
	//******************************************************************//


	public void computeCriticalPath() {
		boolean prevtfound = true;
		BTask t = ft; //the final task causing the max makespan
		while(prevtfound) {
			prevtfound = false;
			criticalPath.addFirst(t);
			//find the next task to add in the path
			final int posj = t.posJ;
			final int posm = t.posM;
			if (posm != 0) {
				final BTask pmt = machineSol[t.machine][posm - 1];
				if (pmt.end == t.startd) {
					t = pmt;
					prevtfound = true;
				}
			}
			if (!prevtfound && posj != 0) {
				final BTask pjt = jobSol[t.job][posj - 1];
				if (pjt.end == t.startd) {
					t = pjt;
					prevtfound = true;
				}
			}
		}
	}

	//******************************************************************//
	//**************** Blocs computation *******************************//
	//******************************************************************//

	public void printBlocs() {
		for (Bloc b : cblocs) {
			System.out.println(b);
		}
	}

	public BTask initialize(final Bloc cblocM, final Bloc cblocJ, final Iterator<BTask> it) {
		final BTask t1 = it.next();
		final BTask t2 = it.next();
		if (t1.machine == t2.machine) {
			cblocM.addTask(t1);
			cblocM.addTask(t2);
		} else {
			cblocJ.addTask(t1);
			cblocJ.addTask(t2);
		}
		return t2;
	}


	public void addBloc(final Bloc bl, final int i) {
		cblocs.add(bl);
		bl.idxBloc = i;
		bl.computeEAj();
		bl.computeEBj();
		bl.computeFj(this);
		bl.computeLj(this);
	}


	/**
	 * Compute all blocs in a linear way maintaining
	 * the current Machine and Job open bloc
	 */
	public void computeBlocs() {
		int i = 0;
		final Iterator<BTask> it = criticalPath.iterator();
		Bloc cblocM = new Bloc(true);
		Bloc cblocJ = new Bloc(false);
		BTask tp = initialize(cblocM,cblocJ,it);
		while (it.hasNext()) {
			final BTask t = it.next();
			if (!cblocM.isEmpty()) {
				if (cblocM.isInBloc(t)) {
					cblocM.addTask(t);
				} else {
					addBloc(cblocM,++i);
					cblocM = new Bloc(true);
				}
			} else if (tp != null) {
				if (t.machine == tp.machine) {
					cblocM.addTask(tp);
					cblocM.addTask(t);
				}
			}
			if (!cblocJ.isEmpty()) {
				if (cblocJ.isInBloc(t)) {
					cblocJ.addTask(t);
				} else {
					addBloc(cblocJ,++i);
					cblocJ = new Bloc(false);
				}
			} else if (tp != null) {
				if (t.job == tp.job) {
					cblocJ.addTask(tp);
					cblocJ.addTask(t);
				}
			}
			tp = t;
		}
		if (!cblocJ.isEmpty()) {
			addBloc(cblocJ,++i);
		}
		if (!cblocM.isEmpty()) {
			addBloc(cblocM,++i);
		}
		Collections.sort(cblocs);

	}



}