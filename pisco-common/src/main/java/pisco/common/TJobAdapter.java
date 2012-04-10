package pisco.common;

import choco.kernel.common.util.iterators.IStored;
import gnu.trove.TLinkableAdapter;
import gnu.trove.TLinkedList;

public final class TJobAdapter extends TLinkableAdapter {

	// TODO - Add ThreadLocal Pool - created 10 avr. 2012 by A. Malapert
	private final static TLinkedList<TJobAdapter> OBJPOOL = new TLinkedList<TJobAdapter>();


	public final static void add(TLinkedList<TJobAdapter> list, ITJob job) {
		list.add(make(job));
	}

	public final static TJobAdapter make(ITJob job) {
		if(OBJPOOL.isEmpty() ) {
			return new TJobAdapter(job);
		}
		else {
			OBJPOOL.getFirst().setTarget(job);
			return OBJPOOL.removeFirst(); 
		}
	}

	public final static void free(TLinkedList<TJobAdapter> list) {
		while( ! list.isEmpty()) {
			free(list.removeFirst());
		}
	}

	public final static void free(TJobAdapter adapter) {
		OBJPOOL.add(adapter);
	}

	private static final long serialVersionUID = 5224076152233404931L;

	public ITJob target;

	private boolean isStored;

	private boolean canBeReuse; // wether this object can be reused, or not

	public TJobAdapter(ITJob target) {
		super();
		this.target = target;
	}

	public final ITJob getTarget() {
		return target;
	}

	public final void setTarget(ITJob target) {
		this.target = target;
	}

	@Override
	public String toString() {
		return target == null ? "null" : toString();
	} 	


}