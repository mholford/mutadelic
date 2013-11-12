/*
 * Copyright (c) 2000, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 */

package edu.yale.seqtree;

/** An interval is an immutable data structure defined by its two
 endpoints. */

import java.util.Comparator;

public class Interval {
	private Object lowEndpoint;
	private Object highEndpoint;

	/**
	 * It is required that the low endpoint be less than or equal to the high
	 * endpoint according to the Comparator which will be passed into the
	 * overlaps() routines.
	 */
	public Interval(Object lowEndpoint, Object highEndpoint) {
		this.lowEndpoint = lowEndpoint;
		this.highEndpoint = highEndpoint;
	}

	public Object getLowEndpoint() {
		return lowEndpoint;
	}

	public Object getHighEndpoint() {
		return highEndpoint;
	}

	@SuppressWarnings("unchecked")
	public boolean after(Interval arg, Comparator endpointComparator) {
		return after(arg.getLowEndpoint(), arg.getHighEndpoint(),
				endpointComparator);
	}

	@SuppressWarnings("unchecked")
	private boolean after(Object otherLowEndpoint, Object otherHighEndpoint,
			Comparator endpointComparator) {
		if (endpointComparator.compare(lowEndpoint, otherHighEndpoint) <= 0) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean before(Interval arg, Comparator endpointComparator) {
		return before(arg.getLowEndpoint(), arg.getHighEndpoint(),
				endpointComparator);
	}

	@SuppressWarnings("unchecked")
	private boolean before(Object otherLowEndpoint, Object otherHighEndpoint,
			Comparator endpointComparator) {
		if (endpointComparator.compare(highEndpoint, otherLowEndpoint) >= 0) {
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean overlaps(Interval arg, Comparator endpointComparator) {
		return overlaps(arg.getLowEndpoint(), arg.getHighEndpoint(),
				endpointComparator);
	}

	@SuppressWarnings("unchecked")
	private boolean overlaps(Object otherLowEndpoint, Object otherHighEndpoint,
			Comparator endpointComparator) {
		if (endpointComparator.compare(lowEndpoint, otherLowEndpoint) >= 0) {
			return false;
		}
		if (endpointComparator.compare(highEndpoint, otherLowEndpoint) <= 0) {
			return false;
		}
		if (endpointComparator.compare(highEndpoint, otherHighEndpoint) >= 0) {
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean overlappedBy(Interval arg, Comparator endpointComparator) {
		return overlappedBy(arg.getLowEndpoint(), arg.getHighEndpoint(),
				endpointComparator);
	}

	@SuppressWarnings("unchecked")
	private boolean overlappedBy(Object otherLowEndpoint, Object otherHighEndpoint,
			Comparator endpointComparator) {
		if (endpointComparator.compare(otherLowEndpoint, lowEndpoint) >= 0) {
			return false;
		}
		if (endpointComparator.compare(otherHighEndpoint, lowEndpoint) <= 0) {
			return false;
		}
		if (endpointComparator.compare(otherHighEndpoint, highEndpoint) >= 0) {
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean meets(Interval arg, Comparator endpointComparator) {
		return meets(arg.getLowEndpoint(), arg.getHighEndpoint(),
				endpointComparator);
	}

	@SuppressWarnings("unchecked")
	private boolean meets(Object otherLowEndpoint, Object otherHighEndpoint,
			Comparator endpointComparator) {
		if (endpointComparator.compare(highEndpoint, otherLowEndpoint) != 0) {
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean metBy(Interval arg, Comparator endpointComparator) {
		return metBy(arg.getLowEndpoint(), arg.getHighEndpoint(),
				endpointComparator);
	}

	@SuppressWarnings("unchecked")
	private boolean metBy(Object otherLowEndpoint, Object otherHighEndpoint,
			Comparator endpointComparator) {
		if (endpointComparator.compare(otherHighEndpoint, lowEndpoint) != 0) {
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean starts(Interval arg, Comparator endpointComparator) {
		return starts(arg.getLowEndpoint(), arg.getHighEndpoint(),
				endpointComparator);
	}

	@SuppressWarnings("unchecked")
	private boolean starts(Object otherLowEndpoint, Object otherHighEndpoint,
			Comparator endpointComparator) {
		if (endpointComparator.compare(lowEndpoint, otherLowEndpoint) != 0) {
			return false;
		}
		if (endpointComparator.compare(highEndpoint, otherHighEndpoint) >= 0) {
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean startedBy(Interval arg, Comparator endpointComparator) {
		return startedBy(arg.getLowEndpoint(), arg.getHighEndpoint(),
				endpointComparator);
	}

	@SuppressWarnings("unchecked")
	private boolean startedBy(Object otherLowEndpoint, Object otherHighEndpoint,
			Comparator endpointComparator) {
		if (endpointComparator.compare(otherLowEndpoint, lowEndpoint) != 0) {
			return false;
		}
		if (endpointComparator.compare(otherHighEndpoint, highEndpoint) >= 0) {
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean during(Interval arg, Comparator endpointComparator) {
		return during(arg.getLowEndpoint(), arg.getHighEndpoint(),
				endpointComparator);
	}

	@SuppressWarnings("unchecked")
	private boolean during(Object otherLowEndpoint, Object otherHighEndpoint,
			Comparator endpointComparator) {
		if (endpointComparator.compare(lowEndpoint, otherLowEndpoint) <= 0) {
			return false;
		}
		if (endpointComparator.compare(highEndpoint, otherHighEndpoint) >= 0) {
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean contains(Interval arg, Comparator endpointComparator) {
		return contains(arg.getLowEndpoint(), arg.getHighEndpoint(),
				endpointComparator);
	}

	@SuppressWarnings("unchecked")
	private boolean contains(Object otherLowEndpoint, Object otherHighEndpoint,
			Comparator endpointComparator) {
		if (endpointComparator.compare(otherLowEndpoint, lowEndpoint) <= 0) {
			return false;
		}
		if (endpointComparator.compare(otherHighEndpoint, highEndpoint) >= 0) {
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean finishes(Interval arg, Comparator endpointComparator) {
		return finishes(arg.getLowEndpoint(), arg.getHighEndpoint(),
				endpointComparator);
	}

	@SuppressWarnings("unchecked")
	private boolean finishes(Object otherLowEndpoint, Object otherHighEndpoint,
			Comparator endpointComparator) {
		if (endpointComparator.compare(highEndpoint, otherHighEndpoint) != 0) {
			return false;
		}
		if (endpointComparator.compare(lowEndpoint, otherLowEndpoint) <= 0) {
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean finishedBy(Interval arg, Comparator endpointComparator) {
		return finishedBy(arg.getLowEndpoint(), arg.getHighEndpoint(),
				endpointComparator);
	}

	@SuppressWarnings("unchecked")
	private boolean finishedBy(Object otherLowEndpoint, Object otherHighEndpoint,
			Comparator endpointComparator) {
		if (endpointComparator.compare(highEndpoint, otherHighEndpoint) != 0) {
			return false;
		}
		if (endpointComparator.compare(lowEndpoint, otherLowEndpoint) >= 0) {
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean equalTo(Interval arg, Comparator endpointComparator) {
		return equalTo(arg.getLowEndpoint(), arg.getHighEndpoint(),
				endpointComparator);
	}

	@SuppressWarnings("unchecked")
	private boolean equalTo(Object otherLowEndpoint, Object otherHighEndpoint,
			Comparator endpointComparator) {
		if (endpointComparator.compare(highEndpoint, otherHighEndpoint) != 0) {
			return false;
		}
		if (endpointComparator.compare(lowEndpoint, otherLowEndpoint) != 0) {
			return false;
		}
		return true;
	}

	/**
	 * This takes the Interval to compare against as well as a Comparator which
	 * will be applied to the low and high endpoints of the given intervals.
	 */
	@SuppressWarnings("unchecked")
	public boolean touches(Interval arg, Comparator endpointComparator) {
		return touches(arg.getLowEndpoint(), arg.getHighEndpoint(),
				endpointComparator);
	}

	/**
	 * Routine which can be used instead of the one taking an interval, for the
	 * situation where the endpoints are being retrieved from different data
	 * structures
	 */
	@SuppressWarnings("unchecked")
	public boolean touches(Object otherLowEndpoint, Object otherHighEndpoint,
			Comparator endpointComparator) {
		if (endpointComparator.compare(highEndpoint, otherLowEndpoint) < 0) {
			return false;
		}
		if (endpointComparator.compare(lowEndpoint, otherHighEndpoint) > 0) {
			return false;
		}
		return true;
	}

	public String toString() {
		return "[ " + getLowEndpoint().toString() + ", "
				+ getHighEndpoint().toString() + ")";
	}
}
