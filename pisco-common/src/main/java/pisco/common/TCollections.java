package pisco.common;

import gnu.trove.TLinkable;
import gnu.trove.TLinkedList;


public final class TCollections {

	private TCollections() {}

	/**
	 * Insertion sort
	 */
	public final static <T extends TLinkable & Comparable<T>> void sort(TLinkedList<T> list ) {
		if( list.size() >1) {
			T current = list.getNext(list.getFirst());
			T next, previous;
			do {
				next = list.getNext(current);
				previous = list.getPrevious(current);
				while( previous != null && current.compareTo(previous) < 0) {
					previous = list.getPrevious(previous);
				} 
				
				if(previous == null) {
					list.remove(current);
					list.addFirst(current);
				} else if (previous != list.getPrevious(current) ) {
					list.remove(current);
					list.addAfter(previous, current);
				}
			} while( (current  = next)!= null);
		}
	}

}
