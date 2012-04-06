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
package pisco.batch.choco.constraints;

import static choco.Choco.MAX_UPPER_BOUND;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntProcedure;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import pisco.batch.data.BJob;
import choco.kernel.common.IDotty;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.visu.VisuFactory;

public class ParallelLmaxFlowGraph implements IDotty {

	protected final int capacity;
	private int currentTask = 0;
	protected final int[] dueDates;
	protected final int[] timeLengths;

	protected TIntArrayList[] neighbourLists;

	/** capacity matrix (must be n by n) */
	protected int[][] capacities;

	private int[] parents;

	private int[] capacityOfPathToNode;

	/** Residual capacity from u to v is C[u][v] - F[u][v] */
	private int[][] residualCapacities;

	/** BFS queue */
	private Queue<Integer> queueBFS = new LinkedList<Integer>();

	private final static int SOURCE = 0;
	private final static int SINK = 1;
	private final static int OFFSET = 2;


	public ParallelLmaxFlowGraph(int[] dueDates, int capacity) {
		super();
		TIntHashSet set = new TIntHashSet(dueDates.length);
		for (int i : dueDates) {
			set.add(i);
		}
		this.dueDates = set.toArray();
		Arrays.sort(this.dueDates);

		this.capacity = capacity;
		timeLengths = new int[this.dueDates.length];
		timeLengths[0] = this.dueDates[0];
		for (int i = 1; i < this.dueDates.length; i++) {
			timeLengths[i] = this.dueDates[i] - this.dueDates[i-1];
		}

		final int nb = OFFSET + this.dueDates.length + dueDates.length * capacity;
		neighbourLists = new TIntArrayList[nb];
		parents = new int[nb];
		residualCapacities = new int[nb][nb];
		capacities = new int[nb][nb];
		capacityOfPathToNode = new int[nb];
		currentTask = OFFSET + this.dueDates.length;
		for (int i = 0; i < neighbourLists.length; i++) {
			neighbourLists[i]=new TIntArrayList(dueDates.length);
		}

		//potential integer overflow
		for (int i = OFFSET; i < currentTask; i++) {
			neighbourLists[i].add(SINK);
			capacities[i][SINK]= capacity * timeLengths[i - OFFSET];
		}
	}

	public int computeSourceCutCapacity() {
		final int n = neighbourLists[SOURCE].size();
		int totCapa = 0;
		for (int i = 0; i < n; i++) {
			totCapa += capacities[SOURCE][neighbourLists[SOURCE].getQuick(i)];
		}
		return totCapa;
	}

	public void reset() {
		currentTask = OFFSET + dueDates.length;
		neighbourLists[SOURCE].resetQuick();
		setMaximalLateness(0);
	}

	public void close() {
		for (int i = currentTask; i < neighbourLists.length; i++) {
			neighbourLists[i].resetQuick();
		}
	}

	public void setMaximalLateness(int value) {
		timeLengths[0] = value + dueDates[0];
		if(timeLengths[0] > Integer.MAX_VALUE/capacity) {
			//ChocoLogging.getSearchLogger().warning("Integer overflow: "+this.getClass().getCanonicalName());
			capacities[OFFSET][SINK]= MAX_UPPER_BOUND;
		} else capacities[OFFSET][SINK]= capacity * timeLengths[0];
	}

	public void addJob(BJob job) {
		final int dd = job.getDueDate();
		final int s = currentTask + job.getSize();
		final int p = job.getDuration();
		while(currentTask < s) {
			neighbourLists[SOURCE].add(currentTask);
			capacities[SOURCE][currentTask]= p;
			neighbourLists[currentTask].resetQuick();
			int idx = 0;
			do {
				final int timeIndex = OFFSET + idx;
				neighbourLists[currentTask].add(timeIndex);
				capacities[currentTask][timeIndex] = timeLengths[idx];
				idx++;
			} while( idx < dueDates.length && dd  >= dueDates[idx]);
			currentTask++;
		}
	}

	@Override
	public String toDotty() {
		final StringBuilder b = new StringBuilder();
		b.append(SOURCE).append(" [shape=house, label=\"Source\"];\n");
		b.append(SINK).append(" [shape=house, label=\"Sink\"];\n");
		for (int i = 0; i < dueDates.length; i++) {
			b.append(OFFSET+i).append(" [shape=rectangle, label=\"").append(dueDates[i]).append("\"];\n");
		}
		for (int i = 0; i < neighbourLists.length; i++) {
			final int idx = i;
			neighbourLists[i].forEach(new TIntProcedure() {
				@Override
				public boolean execute(int arg0) {
					b.append(idx).append("->").append(arg0);
					b.append(" [label=\"").append(capacities[idx][arg0]).append("\"];\n");
					return true;
				}
			});
		}
		return b.toString();
	}


	/**
	 * From http://en.wikibooks.org/wiki/Algorithm_Implementation/Graphs/Maximum_flow/Edmonds-Karp
	 * Finds the maximum flow in a flow network.
	 * @param E neighbour lists
	 * @return maximum flow
	 */
	public int edmondsKarp() {
		for (int i = 0; i < residualCapacities.length; i++) {
			Arrays.fill(residualCapacities[i], 0);
		}
		while (true) {
			Arrays.fill(parents, -1);
			parents[SOURCE] = SOURCE;
			Arrays.fill(capacityOfPathToNode, Integer.MAX_VALUE);
			// BFS queue
			queueBFS.clear();
			queueBFS.offer(SOURCE);
			LOOP:
				while (!queueBFS.isEmpty()) {
					int u = queueBFS.poll();
					for (int i = 0; i < neighbourLists[u].size(); i++) {
						int v = neighbourLists[u].getQuick(i);
						//for (int v : E[u]) {
						// There is available capacity,
						// and v is not seen before in search
						if (capacities[u][v] - residualCapacities[u][v] > 0 && parents[v] == -1) {
							parents[v] = u;
							capacityOfPathToNode[v] = Math.min(capacityOfPathToNode[u], capacities[u][v] - residualCapacities[u][v]);
							if (v != SINK)
								queueBFS.offer(v);
							else {
								// Backtrack search, and write flow
								while (parents[v] != v) {
									u = parents[v];
									residualCapacities[u][v] += capacityOfPathToNode[SINK];
									residualCapacities[v][u] -= capacityOfPathToNode[SINK];
									v = u;
								}
								break LOOP;
							}
						}
					}
				}
			if (parents[SINK] == -1) { // We did not find a path to t
					int sum = 0;
					for (int x : residualCapacities[SOURCE])
						sum += x;
					return sum;
			}
		}
	}


	public static void main(String[] args) {
		//System.out.println(Integer.MAX_VALUE);
		int[] dueDates = new int[] { 10, 15, 20, 12, 35};
		ParallelLmaxFlowGraph graph = new ParallelLmaxFlowGraph(dueDates, 10);
		graph.setMaximalLateness(20);
		graph.addJob(new BJob(1, 10, dueDates[2]));
		graph.addJob(new BJob(2, 5, dueDates[1]));
		graph.addJob(new BJob(3, 15, 3, 1, dueDates[4]));
		VisuFactory.getDotManager().show(graph);
		System.out.println(graph.edmondsKarp());
	}

}
