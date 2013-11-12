package edu.yale.seqtree;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Comparator;

public class SrutiNode2 extends IntervalNode {
	public static final int SIZE = 72;
	public static final int START_PT = 0;
	public static final int END_PT = 8;
	public static final int CHR_PT = 16;
	public static final int MIN_PT = 20;
	public static final int MAX_PT = 28;
	public static final int LEFT_PT = 36;
	public static final int RIGHT_PT = 44;
	public static final int PARENT_PT = 52;
	public static final int COLOR_PT = 60;
	public static final int ID_PT = 64;
	public static final int RED = 0;
	public static final int BLACK = 1;
	private boolean dbg = false;

	private long start = -1L, end = -1L, min = -1L, max = -1L, left = -1L,
			right = -1L, parent = -1L;
	private int chr = -1;
	private int color = -1;
	private long ID = -1;
	private Interval interval;
	@SuppressWarnings("unchecked")
	private Comparator endpointComparator;
	// private byte[] bytes;
	private long address;
	private TreeStore<byte[]> persister;

	@SuppressWarnings("unchecked")
	public SrutiNode2(Interval interval, int chr,
			Comparator endpointComparator, long address,
			TreeStore persister) {
		this.interval = interval;
		this.endpointComparator = endpointComparator;
		this.address = address;
		this.persister = persister;
		setStart((Long) interval.getLowEndpoint());
		setEnd((Long) interval.getHighEndpoint());
		setChr(chr);
		setColor(RBColor.RED);
		setID(address);
		setLeft(null);
		setRight(null);
		setParent(null);
	}

	@SuppressWarnings("unchecked")
	public SrutiNode2(Comparator endpointComparator, long address,
			TreeStore<byte[]> persister) {
		this.endpointComparator = endpointComparator;
		this.address = address;
		this.persister = persister;
		// this.color = RED;
	}

	public byte[] toByteBuffer() {
		byte[] buf = new byte[SIZE];
		getBytesForLong(buf, start, 0);
		getBytesForLong(buf, end, 8);
		getBytesForInt(buf, chr, 16);
		getBytesForLong(buf, min, 20);
		getBytesForLong(buf, max, 28);
		getBytesForLong(buf, left, 36);
		getBytesForLong(buf, right, 44);
		getBytesForLong(buf, parent, 52);
		getBytesForInt(buf, color, 60);
		getBytesForLong(buf, ID, 64);
		return buf;
	}

	public static final int getIntForBytes(byte[] bytes, int index) {
		return ((bytes[index + 0] & 0xff) << 24)
				| ((bytes[index + 1] & 0xff) << 16)
				| ((bytes[index + 2] & 0xff) << 8)
				| ((bytes[index + 3] & 0xff));
	}

	public static final long getLongForBytes(byte[] bytes, int index) {
		return ((bytes[index + 0] & 0xffL) << 56)
				| ((bytes[index + 1] & 0xffL) << 48)
				| ((bytes[index + 2] & 0xffL) << 40)
				| ((bytes[index + 3] & 0xffL) << 32)
				| ((bytes[index + 4] & 0xffL) << 24)
				| ((bytes[index + 5] & 0xffL) << 16)
				| ((bytes[index + 6] & 0xffL) << 8)
				| ((bytes[index + 7] & 0xffL));
	}

	public final void getBytesForInt(byte[] bytes, int value, int index) {
		bytes[index + 0] = (byte) (value >> 24);
		bytes[index + 1] = (byte) (value >> 16);
		bytes[index + 2] = (byte) (value >> 8);
		bytes[index + 3] = (byte) value;
	}

	public final void getBytesForLong(byte[] bytes, long value, int index) {
		bytes[index + 0] = (byte) (value >> 56);
		bytes[index + 1] = (byte) (value >> 48);
		bytes[index + 2] = (byte) (value >> 40);
		bytes[index + 3] = (byte) (value >> 32);
		bytes[index + 4] = (byte) (value >> 24);
		bytes[index + 5] = (byte) (value >> 16);
		bytes[index + 6] = (byte) (value >> 8);
		bytes[index + 7] = (byte) value;
	}

	@Override
	public Interval getInterval() {
		if (interval != null) {
			return interval;
		}
		return new Interval(getStart(), getEnd());
	}

	public long getStart() {
		if (start >= 0) {
			return start;
		}
		try {
			start = persister.getLong(address + START_PT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return start;
	}

	public long getEnd() {
		if (end >= 0) {
			return end;
		}
		try {
			end = persister.getLong(address + END_PT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return end;
	}

	public int getChromosome() {
		if (chr >= 0) {
			return chr;
		}
		try {
			chr = persister.getInt(address + CHR_PT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return chr;
	}

	public long getID() {
		if (ID >= 0) {
			return ID;
		}
		try {
			ID = persister.getLong(address + ID_PT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ID;
	}

	@Override
	public RBColor getColor() {
		int tmpColor = -1;
		if (color >= 0) {
			tmpColor = color;
		} else {
			try {
				tmpColor = persister.getInt(address + COLOR_PT);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		switch (tmpColor) {
		case RED:
			return RBColor.RED;
		case BLACK:
			return RBColor.BLACK;
		default:
			throw new Error("Wrong Color!! " + tmpColor);
		}
	}

	@Override
	public RBNode getLeft() {
		long tmpLeft = -1L;
		if (left >= 0) {
			tmpLeft = left;
		} else {
			try {
				tmpLeft = persister.getLong(address + LEFT_PT);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (tmpLeft < 0) {
			return null;
		}
//		left = tmpLeft;
		return new SrutiNode2(endpointComparator, tmpLeft, persister);
	}

	@Override
	public SrutiNode2 getParent() {
		long tmpParent = -1;
		if (parent >= 0) {
			tmpParent = parent;
		} else {
			try {
				tmpParent = persister.getLong(address + PARENT_PT);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (tmpParent < 0) {
			return null;
		}
//		parent = tmpParent;
		return new SrutiNode2(endpointComparator, tmpParent, persister);
	}

	@Override
	public RBNode getRight() {
		long tmpRight = -1;
		if (right >= 0) {
			tmpRight = right;
		} else {
			try {
				tmpRight = persister.getLong(address + RIGHT_PT);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (tmpRight < 0) {
			return null;
		}
//		right = tmpRight;
		return new SrutiNode2(endpointComparator, tmpRight, persister);
	}

	@Override
	public void setColor(RBColor color) {
		if (color.equals(RBColor.RED)) {
			this.color = RED;
		} else if (color.equals(RBColor.BLACK)) {
			this.color = BLACK;
		} else {
			throw new Error("Unexpected Color!!");
		}
		try {
			persister.putInt(this.color, address + COLOR_PT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setLeft(RBNode left) {
		if (left == null) {
			this.left = -1L;
		} else {
			this.left = ((SrutiNode2) left).getAddress();
		}
		try {
			persister.putLong(this.left, address + LEFT_PT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setParent(RBNode parent) {
		if (parent == null) {
			this.parent = -1L;
		} else {
			this.parent = ((SrutiNode2) parent).getAddress();
		}
		try {
			persister.putLong(this.parent, address + PARENT_PT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setRight(RBNode right) {
		if (right == null) {
			this.right = -1L;
		} else {
			this.right = ((SrutiNode2) right).getAddress();
		}
		try {
			persister.putLong(this.right, address + RIGHT_PT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Object getMaxEndpoint() {
		if (max >= 0) {
			return max;
		}
		try {
			max = persister.getLong(address + MAX_PT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return max;
	}

	public void setMaxEndpoint(long max) {
		this.max = max;
		try {
			persister.putLong(this.max, address + MAX_PT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Object getMinEndpoint() {
		if (min >= 0) {
			return min;
		}
		try {
			min = persister.getLong(address + MIN_PT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return min;
	}

	public void setMinEndpoint(long min) {
		this.min = min;
		try {
			persister.putLong(this.min, address + MIN_PT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setStart(long start) {
		this.start = start;
		try {
			persister.putLong(this.start, address + START_PT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setEnd(long end) {
		this.end = end;
		try {
			persister.putLong(this.end, address + END_PT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setChr(int chr) {
		this.chr = chr;
		try {
			persister.putInt(this.chr, address + CHR_PT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setID(long ID) {
		this.ID = ID;
		try {
			persister.putLong(this.ID, address + ID_PT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public long getAddress() {
		return address;
	}

	@Override
	public Object computeMinEndpoint() {
		IntervalNode left = (IntervalNode) getLeft();
		if (left != null) {
			return left.getMinEndpoint();
		}
		return getInterval().getLowEndpoint();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object computeMaxEndpoint() {
		Object curMax = getInterval().getHighEndpoint();
		if (getLeft() != null) {
			IntervalNode left = (IntervalNode) getLeft();
			if (endpointComparator.compare(left.getMaxEndpoint(), curMax) > 0) {
				curMax = left.getMaxEndpoint();
			}
		}
		if (getRight() != null) {
			IntervalNode right = (IntervalNode) getRight();
			if (endpointComparator.compare(right.getMaxEndpoint(), curMax) > 0) {
				curMax = right.getMaxEndpoint();
			}
		}
		return curMax;
	}

	@Override
	public boolean update() {
		if (dbg) {
			System.err.println(this + " update ");
		}
		long newMax = (Long) computeMaxEndpoint();
		long newMin = (Long) computeMinEndpoint();
		if (dbg) {
			System.err.println("New Max: " + newMax);
			System.err.println("New Min: " + newMin);
		}
		if ((max != newMax) || (min != newMin)) {
			if (dbg) {
				System.err.println("Update true");
			}
			setMaxEndpoint(newMax);
			setMinEndpoint(newMin);
			return true;
		}
		if (dbg) {
			System.err.println("Update false");
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format("[ %d, %d)", start, end, chr);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		SrutiNode2 other = (SrutiNode2) obj;
		if (this == other) {
			return true;
		}
		return address == other.getAddress();
	}

	@Override
	public int hashCode() {
		return toByteBuffer().hashCode();
	}

}
