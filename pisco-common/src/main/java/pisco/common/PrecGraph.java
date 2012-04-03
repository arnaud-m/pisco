package pisco.common;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntProcedure;
import choco.kernel.common.IDotty;

public class PrecGraph implements IDotty {

	public final int n;

	private int m = 0;

	private final TIntArrayList[] predecessors;
	private final TIntArrayList[] successors;


	public PrecGraph(final int n) {
		super();
		this.n=n;
		predecessors = new TIntArrayList[n];
		successors = new TIntArrayList[n];
		for (int i = 0; i < n; i++) {
			predecessors[i] = new TIntArrayList();
			successors[i] = new TIntArrayList();
		}
	}

	public void addQuick(final int i,final int j) {
		successors[i].add(j);
		predecessors[j].add(i);
		m++;
	}

	public boolean add(final int i,final int j) {
		if( contains(i, j)) {
			return false;
		} else {
			add(i, j);
			return true;
		}
	}

	public boolean remove(final int i,final int j) {
		final int idx = successors[i].indexOf(j);
		if(idx < 0) {
			return false;
		} else {
			successors[i].remove(idx);
			return true;
		}
	}




	/**
	 * @see choco.kernel.common.IDotty#toDotty()
	 */
	@Override
	public String toDotty() {
		DotProcedure proc = new DotProcedure();
		return proc.toDotty(successors);
	}

	final class DotProcedure implements TIntProcedure {

		public final StringBuilder buffer = new StringBuilder();

		public int origin = 0;

		protected String toDotty(TIntArrayList[] graph) {
			for (origin = 0; origin < graph.length; origin++) {
				graph[origin].forEach(this);
			}
			return new String(buffer);
		}

		@Override
		public boolean execute(int arg0) {
			buffer.append(origin).append("->").append(arg0).append(";\n");
			return true;
		}
	}


	public boolean isDisconnected(int i) {
		return !hasPredecessor(i) && !hasSuccessor(i);
	}

	public final boolean hasPredecessor(final int i) {
		return ! predecessors[i].isEmpty();
	}

	public final boolean hasSuccessor(final int i) {
		return !successors[i].isEmpty();
	}

	public final int getNbPredecessors(final int i) {
		return predecessors[i].size();
	}

	public final int getNbSuccessors(final int i) {
		return successors[i].size();
	}

	public final boolean contains(final int i, final int j) {
		return successors[i].contains(j);
	}

	public final int getN() {
		return n;
	}


	public final int getM() {
		return m;
	}


	public final boolean isEmpty() {
		return m == 0;
	}

	public final void modifyDueDates(final AbstractJob[] jobs, final int[] tempSuccCounts) {
		final TIntArrayList currentIndices = new TIntArrayList();
		for (int i = 0; i < n; i++) {
			tempSuccCounts[i] = successors[i].size();
			if( successors[i].isEmpty() ) {
				currentIndices.add(i);
			}
		}
		assert( ! currentIndices.isEmpty());
		while( ! currentIndices.isEmpty()) {
			final int i = currentIndices.remove(currentIndices.size() - 1);
			predecessors[i].forEach(new TIntProcedure() {

				@Override
				public boolean execute(int arg0) {
					int modifiedDueDate = jobs[i].getDueDate() - jobs[i].getDuration();
					if(modifiedDueDate < jobs[i].getDueDate() ) {
						jobs[i].setDueDate(modifiedDueDate);
					}
					tempSuccCounts[arg0]--;
					if(tempSuccCounts[arg0] == 0) {
						currentIndices.add(arg0);
					}
					return true;
				}
			});
		}
	}
}
