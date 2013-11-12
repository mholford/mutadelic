package edu.yale.seqtree;

import java.util.Comparator;

@SuppressWarnings("unchecked")
public class SimpleComparator implements Comparator {

	@Override
	public int compare(Object o1, Object o2) {
		Long i1 = (Long) o1;
		Long i2 = (Long) o2;
		if (i1 < i2) {
			return -1;
		} else if (i1 > i2) {
			return 1;
		} else {
			return 0;
		}
	}
}
