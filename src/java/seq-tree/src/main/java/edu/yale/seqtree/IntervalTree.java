/*
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
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

/** Derived from the example in Section 15.3 of CLR. */

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class IntervalTree extends RBTree {
	@SuppressWarnings("unchecked")
	protected Comparator endpointComparator;

	/**
	 * This constructor takes only one comparator: one which operates upon the
	 * endpoints of the Intervals this tree will store. It constructs an
	 * internal "interval comparator" out of this one.
	 */
	@SuppressWarnings("unchecked")
	public IntervalTree(Comparator endpointComparator) {
		super(new IntervalComparator(endpointComparator));
		this.endpointComparator = endpointComparator;
	}

	public long insert(Interval interval, Object data) {
		IntervalNode node = new IntervalNode(interval, endpointComparator, data);
		insertNode(node);
		return 0;
	}

	public long insert(Sequence locus) {
		return insert(getInterval(locus), locus);
	}

	public void delete(Interval interval, Object data) {
		IntervalNode node = new IntervalNode(interval, endpointComparator, data);
		deleteNode(node);
	}

	public void delete(Sequence locus) {
		delete(getInterval(locus), locus);
	}

	/**
	 * Returns a List&lt;IntervalNode&gt; indicating which nodes' intervals were
	 * intersected by the given query interval. It is guaranteed that these
	 * nodes will be returned sorted by increasing low endpoint.
	 */
	@SuppressWarnings("unchecked")
	public List findIntervalTouches(Interval interval) {
		List retList = new ArrayList();
		searchForIntervalTouches((IntervalNode) getRoot(), interval, retList);
		return retList;
	}

	@SuppressWarnings("unchecked")
	public List findIntervalOverlaps(Interval interval) {
		List retList = new ArrayList();
		searchForIntervalOverlaps((IntervalNode) getRoot(), interval, retList);
		return retList;
	}

	@SuppressWarnings("unchecked")
	public List findIntervalOverlappedBy(Interval interval) {
		List retList = new ArrayList();
		searchForIntervalOverlappedBy((IntervalNode) getRoot(), interval,
				retList);
		return retList;
	}

	@SuppressWarnings("unchecked")
	public List findIntervalMeets(Interval interval) {
		List retList = new ArrayList();
		searchForIntervalMeets((IntervalNode) getRoot(), interval, retList);
		return retList;
	}

	@SuppressWarnings("unchecked")
	public List findIntervalMetBy(Interval interval) {
		List retList = new ArrayList();
		searchForIntervalMetBy((IntervalNode) getRoot(), interval, retList);
		return retList;
	}

	@SuppressWarnings("unchecked")
	public List findIntervalBefore(Interval interval) {
		List retList = new ArrayList();
		searchForIntervalBefore((IntervalNode) getRoot(), interval, retList);
		return retList;
	}

	@SuppressWarnings("unchecked")
	public List findIntervalAfter(Interval interval) {
		List retList = new ArrayList();
		searchForIntervalAfter((IntervalNode) getRoot(), interval, retList);
		return retList;
	}

	@SuppressWarnings("unchecked")
	public List findIntervalStarts(Interval interval) {
		List retList = new ArrayList();
		searchForIntervalStarts((IntervalNode) getRoot(), interval, retList);
		return retList;
	}

	@SuppressWarnings("unchecked")
	public List findIntervalStartedBy(Interval interval) {
		List retList = new ArrayList();
		searchForIntervalStartedBy((IntervalNode) getRoot(), interval, retList);
		return retList;
	}

	@SuppressWarnings("unchecked")
	public List findIntervalDuring(Interval interval) {
		List retList = new ArrayList();
		searchForIntervalDuring((IntervalNode) getRoot(), interval, retList);
		return retList;
	}

	@SuppressWarnings("unchecked")
	public List findIntervalContains(Interval interval) {
		List retList = new ArrayList();
		searchForIntervalContains((IntervalNode) getRoot(), interval, retList);
		return retList;
	}

	@SuppressWarnings("unchecked")
	public List findIntervalFinishes(Interval interval) {
		List retList = new ArrayList();
		searchForIntervalFinishes((IntervalNode) getRoot(), interval, retList);
		return retList;
	}

	@SuppressWarnings("unchecked")
	public List findIntervalFinishedBy(Interval interval) {
		List retList = new ArrayList();
		searchForIntervalFinishedBy((IntervalNode) getRoot(), interval, retList);
		return retList;
	}

	@SuppressWarnings("unchecked")
	public List findIntervalEqualTo(Interval interval) {
		List retList = new ArrayList();
		searchForIntervalEqualTo((IntervalNode) getRoot(), interval, retList);
		return retList;
	}

	@SuppressWarnings("unchecked")
	public Iterator<Sequence> touchesNodeIterator(Sequence extent) {
		Iterator<IntervalNode> internal = findIntervalTouches(
				getInterval((Sequence) extent)).iterator();
		return getSequenceIterator(internal);
	}

	@SuppressWarnings("unchecked")
	public Iterator<Sequence> afterNodeIterator(Sequence extent) {
		Iterator<IntervalNode> internal = findIntervalAfter(
				getInterval((Sequence) extent)).iterator();
		return getSequenceIterator(internal);
	}

	@SuppressWarnings("unchecked")
	public Iterator<Sequence> beforeNodeIterator(Sequence extent) {
		Iterator<IntervalNode> internal = findIntervalBefore(
				getInterval((Sequence) extent)).iterator();
		return getSequenceIterator(internal);
	}

	@SuppressWarnings("unchecked")
	public Iterator<Sequence> containsNodeIterator(Sequence extent) {
		Iterator<IntervalNode> internal = findIntervalContains(
				getInterval((Sequence) extent)).iterator();
		return getSequenceIterator(internal);
	}

	@SuppressWarnings("unchecked")
	public Iterator<Sequence> duringNodeIterator(Sequence extent) {
		Iterator<IntervalNode> internal = findIntervalDuring(
				getInterval((Sequence) extent)).iterator();
		return getSequenceIterator(internal);
	}

	@SuppressWarnings("unchecked")
	public Iterator<Sequence> finishedByNodeIterator(Sequence extent) {
		Iterator<IntervalNode> internal = findIntervalFinishedBy(
				getInterval((Sequence) extent)).iterator();
		return getSequenceIterator(internal);
	}

	@SuppressWarnings("unchecked")
	public Iterator<Sequence> finishesNodeIterator(Sequence extent) {
		Iterator<IntervalNode> internal = findIntervalFinishes(
				getInterval((Sequence) extent)).iterator();
		return getSequenceIterator(internal);
	}

	@SuppressWarnings("unchecked")
	public Iterator<Sequence> meetsNodeIterator(Sequence extent) {
		Iterator<IntervalNode> internal = findIntervalMeets(
				getInterval((Sequence) extent)).iterator();
		return getSequenceIterator(internal);
	}

	@SuppressWarnings("unchecked")
	public Iterator<Sequence> metByNodeIterator(Sequence extent) {
		Iterator<IntervalNode> internal = findIntervalMetBy(
				getInterval((Sequence) extent)).iterator();
		return getSequenceIterator(internal);
	}

	@SuppressWarnings("unchecked")
	public Iterator<Sequence> overlappedByNodeIterator(Sequence extent) {
		Iterator<IntervalNode> internal = findIntervalOverlappedBy(
				getInterval((Sequence) extent)).iterator();
		return getSequenceIterator(internal);
	}

	@SuppressWarnings("unchecked")
	public Iterator<Sequence> overlapsNodeIterator(Sequence extent) {
		Iterator<IntervalNode> internal = findIntervalOverlaps(
				getInterval((Sequence) extent)).iterator();
		return getSequenceIterator(internal);
	}

	@SuppressWarnings("unchecked")
	public Iterator<Sequence> startedByNodeIterator(Sequence extent) {
		Iterator<IntervalNode> internal = findIntervalStartedBy(
				getInterval((Sequence) extent)).iterator();
		return getSequenceIterator(internal);
	}

	@SuppressWarnings("unchecked")
	public Iterator<Sequence> startsNodeIterator(Sequence extent) {
		Iterator<IntervalNode> internal = findIntervalStarts(
				getInterval((Sequence) extent)).iterator();
		return getSequenceIterator(internal);
	}

	@SuppressWarnings("unchecked")
	public Iterator<Sequence> equalToNodeIterator(Sequence extent) {
		Iterator<IntervalNode> internal = findIntervalEqualTo(
				getInterval((Sequence) extent)).iterator();
		return getSequenceIterator(internal);
	}

	class SequenceIterator implements Iterator<Sequence> {
		private Iterator<IntervalNode> internal;

		SequenceIterator(Iterator<IntervalNode> internal) {
			this.internal = internal;
		}

		@Override
		public boolean hasNext() {
			return internal.hasNext();
		}

		@Override
		public Sequence next() {
			IntervalNode nx = internal.next();
			return (Sequence) nx.getData();
		}

		@Override
		public void remove() {
			throw new RuntimeException("Not supported");
		}

	}
	
	public Iterator<Sequence> getSequenceIterator(Iterator<IntervalNode> internal) {
		return new SequenceIterator(internal);
	}

	public void print() {
		printOn(System.out);
	}

	public void printOn(PrintStream tty) {
		printFromNode(getRoot(), tty, 0);
	}

	// ----------------------------------------------------------------------
	// Overridden internal functionality

	protected Object getNodeValue(RBNode node) {
		return ((IntervalNode) node).getInterval();
	}

	protected void verify() {
		super.verify();
		verifyFromNode(getRoot());
	}

	// ----------------------------------------------------------------------
	// Internals only below this point
	//

	protected Interval getInterval(Sequence locus) {
		return new Interval(locus.getStart(), locus.getEnd());
	}

	private void verifyFromNode(RBNode node) {
		System.err.println("Verify " + node);
		if (node == null) {
			return;
		}

		// We already know that the red/black structure has been verified.
		// What we need to find out is whether this node has been updated
		// properly -- i.e., whether its notion of the maximum endpoint is
		// correct.
		IntervalNode intNode = (IntervalNode) node;
		if (!intNode.getMaxEndpoint().equals(intNode.computeMaxEndpoint())) {
			throw new RuntimeException(
					"Node's max endpoint was not updated properly");
		}
		if (!intNode.getMinEndpoint().equals(intNode.computeMinEndpoint())) {
			throw new RuntimeException(
					"Node's min endpoint was not updated properly");
		}

		verifyFromNode(node.getLeft());
		verifyFromNode(node.getRight());
	}

	@SuppressWarnings("unchecked")
	static class IntervalComparator implements Comparator {
		private Comparator endpointComparator;

		public IntervalComparator(Comparator endpointComparator) {
			this.endpointComparator = endpointComparator;
		}

		public int compare(Object o1, Object o2) {
			Interval i1 = (Interval) o1;
			Interval i2 = (Interval) o2;
			return endpointComparator.compare(i1.getLowEndpoint(), i2
					.getLowEndpoint());
		}
	}

	@SuppressWarnings("unchecked")
	private void searchForIntervalTouches(IntervalNode node, Interval interval,
			List resultList) {
		if (node == null) {
			return;
		}

		// Inorder traversal (very important to guarantee sorted order)

		// Check to see whether we have to traverse the left subtree
		IntervalNode left = (IntervalNode) node.getLeft();
		if ((left != null)
				&& (endpointComparator.compare(left.getMaxEndpoint(), interval
						.getLowEndpoint()) > 0)) {
			searchForIntervalTouches(left, interval, resultList);
		}

		// Check for intersection with current node
		if (node.getInterval().touches(interval, endpointComparator)) {
			resultList.add(node);
		}

		// Check to see whether we have to traverse the left subtree
		IntervalNode right = (IntervalNode) node.getRight();
		if ((right != null)
				&& (endpointComparator.compare(interval.getHighEndpoint(),
						right.getMinEndpoint()) > 0)) {
			searchForIntervalTouches(right, interval, resultList);
		}
	}

	@SuppressWarnings("unchecked")
	private void searchForIntervalOverlaps(IntervalNode node,
			Interval interval, List resultList) {
		if (node == null) {
			return;
		}

		// Inorder traversal (very important to guarantee sorted order)

		// Check to see whether we have to traverse the left subtree
		IntervalNode left = (IntervalNode) node.getLeft();
		if ((left != null)
				&& (endpointComparator.compare(left.getMaxEndpoint(), interval
						.getLowEndpoint()) > 0)) {
			searchForIntervalOverlaps(left, interval, resultList);
		}

		// Check for intersection with current node
		if (node.getInterval().overlaps(interval, endpointComparator)) {
			resultList.add(node);
		}

		// Check to see whether we have to traverse the left subtree
		IntervalNode right = (IntervalNode) node.getRight();
		if ((right != null)
				&& (endpointComparator.compare(interval.getHighEndpoint(),
						right.getMinEndpoint()) > 0)) {
			searchForIntervalOverlaps(right, interval, resultList);
		}
	}

	@SuppressWarnings("unchecked")
	private void searchForIntervalOverlappedBy(IntervalNode node,
			Interval interval, List resultList) {
		if (node == null) {
			return;
		}

		// Inorder traversal (very important to guarantee sorted order)

		// Check to see whether we have to traverse the left subtree
		IntervalNode left = (IntervalNode) node.getLeft();
		if ((left != null)
				&& (endpointComparator.compare(left.getMaxEndpoint(), interval
						.getLowEndpoint()) > 0)) {
			searchForIntervalOverlappedBy(left, interval, resultList);
		}

		// Check for intersection with current node
		if (node.getInterval().overlappedBy(interval, endpointComparator)) {
			resultList.add(node);
		}

		// Check to see whether we have to traverse the left subtree
		IntervalNode right = (IntervalNode) node.getRight();
		if ((right != null)
				&& (endpointComparator.compare(interval.getHighEndpoint(),
						right.getMinEndpoint()) > 0)) {
			searchForIntervalOverlappedBy(right, interval, resultList);
		}
	}

	@SuppressWarnings("unchecked")
	private void searchForIntervalMeets(IntervalNode node, Interval interval,
			List resultList) {
		if (node == null) {
			return;
		}

		IntervalNode left = (IntervalNode) node.getLeft();
		if ((left != null)
				&& (endpointComparator.compare(left.getMinEndpoint(), interval
						.getLowEndpoint()) < 0)) {
			searchForIntervalMeets(left, interval, resultList);
		}
		// Check for intersection with current node
		if (node.getInterval().meets(interval, endpointComparator)) {
			resultList.add(node);
		}

		IntervalNode right = (IntervalNode) node.getRight();
		if ((right != null)
				&& (endpointComparator.compare(right.getMinEndpoint(), interval
						.getLowEndpoint()) < 0)) {
			searchForIntervalMeets(right, interval, resultList);
		}
	}

	@SuppressWarnings("unchecked")
	private void searchForIntervalMetBy(IntervalNode node, Interval interval,
			List resultList) {
		if (node == null) {
			return;
		}

		IntervalNode left = (IntervalNode) node.getLeft();
		if ((left != null)
				&& (endpointComparator.compare(left.getMaxEndpoint(), interval
						.getHighEndpoint()) > 0)) {
			searchForIntervalMetBy(left, interval, resultList);
		}

		if (node.getInterval().metBy(interval, endpointComparator)) {
			resultList.add(node);
		}

		IntervalNode right = (IntervalNode) node.getRight();
		if ((right != null)
				&& (endpointComparator.compare(right.getMaxEndpoint(), interval
						.getHighEndpoint()) > 0)) {
			searchForIntervalMetBy(right, interval, resultList);
		}
	}

	@SuppressWarnings("unchecked")
	private void searchForIntervalBefore(IntervalNode node, Interval interval,
			List resultList) {
		if (node == null) {
			return;
		}

		IntervalNode left = (IntervalNode) node.getLeft();
		if ((left != null)
				&& (endpointComparator.compare(left.getMinEndpoint(), interval
						.getLowEndpoint()) < 0)) {
			searchForIntervalBefore(left, interval, resultList);
		}

		if (node.getInterval().before(interval, endpointComparator)) {
			resultList.add(node);
		}

		IntervalNode right = (IntervalNode) node.getRight();
		if ((right != null)
				&& (endpointComparator.compare(right.getMinEndpoint(), interval
						.getLowEndpoint()) < 0)) {
			searchForIntervalBefore(right, interval, resultList);
		}
	}

	@SuppressWarnings("unchecked")
	private void searchForIntervalAfter(IntervalNode node, Interval interval,
			List resultList) {
		if (node == null) {
			return;
		}

		IntervalNode left = (IntervalNode) node.getLeft();
		if ((left != null)
				&& (endpointComparator.compare(left.getMaxEndpoint(), interval
						.getHighEndpoint()) > 0)) {
			searchForIntervalAfter(left, interval, resultList);
		}

		if (node.getInterval().after(interval, endpointComparator)) {
			resultList.add(node);
		}

		IntervalNode right = (IntervalNode) node.getRight();
		if ((right != null)
				&& (endpointComparator.compare(right.getMaxEndpoint(), interval
						.getHighEndpoint()) > 0)) {
			searchForIntervalAfter(right, interval, resultList);
		}
	}

	@SuppressWarnings("unchecked")
	private void searchForIntervalStarts(IntervalNode node, Interval interval,
			List resultList) {
		if (node == null) {
			return;
		}

		// Inorder traversal (very important to guarantee sorted order)

		// Check to see whether we have to traverse the left subtree
		IntervalNode left = (IntervalNode) node.getLeft();
		if ((left != null)
				&& (endpointComparator.compare(left.getMaxEndpoint(), interval
						.getLowEndpoint()) > 0)) {
			searchForIntervalStarts(left, interval, resultList);
		}

		// Check for intersection with current node
		if (node.getInterval().starts(interval, endpointComparator)) {
			resultList.add(node);
		}

		// Check to see whether we have to traverse the left subtree
		IntervalNode right = (IntervalNode) node.getRight();
		if ((right != null)
				&& (endpointComparator.compare(interval.getHighEndpoint(),
						right.getMinEndpoint()) > 0)) {
			searchForIntervalStarts(right, interval, resultList);
		}
	}

	@SuppressWarnings("unchecked")
	private void searchForIntervalEqualTo(IntervalNode node, Interval interval,
			List resultList) {
		if (node == null) {
			return;
		}

		// Inorder traversal (very important to guarantee sorted order)

		// Check to see whether we have to traverse the left subtree
		IntervalNode left = (IntervalNode) node.getLeft();
		if ((left != null)
				&& (endpointComparator.compare(left.getMaxEndpoint(), interval
						.getLowEndpoint()) > 0)) {
			searchForIntervalEqualTo(left, interval, resultList);
		}

		// Check for intersection with current node
		if (node.getInterval().equalTo(interval, endpointComparator)) {
			resultList.add(node);
		}

		// Check to see whether we have to traverse the left subtree
		IntervalNode right = (IntervalNode) node.getRight();
		if ((right != null)
				&& (endpointComparator.compare(interval.getHighEndpoint(),
						right.getMinEndpoint()) > 0)) {
			searchForIntervalEqualTo(right, interval, resultList);
		}
	}

	@SuppressWarnings("unchecked")
	private void searchForIntervalStartedBy(IntervalNode node,
			Interval interval, List resultList) {
		if (node == null) {
			return;
		}

		// Inorder traversal (very important to guarantee sorted order)

		// Check to see whether we have to traverse the left subtree
		IntervalNode left = (IntervalNode) node.getLeft();
		if ((left != null)
				&& (endpointComparator.compare(left.getMaxEndpoint(), interval
						.getLowEndpoint()) > 0)) {
			searchForIntervalStartedBy(left, interval, resultList);
		}

		// Check for intersection with current node
		if (node.getInterval().startedBy(interval, endpointComparator)) {
			resultList.add(node);
		}

		// Check to see whether we have to traverse the left subtree
		IntervalNode right = (IntervalNode) node.getRight();
		if ((right != null)
				&& (endpointComparator.compare(interval.getHighEndpoint(),
						right.getMinEndpoint()) > 0)) {
			searchForIntervalStartedBy(right, interval, resultList);
		}
	}

	@SuppressWarnings("unchecked")
	private void searchForIntervalDuring(IntervalNode node, Interval interval,
			List resultList) {
		if (node == null) {
			return;
		}

		// Inorder traversal (very important to guarantee sorted order)

		// Check to see whether we have to traverse the left subtree
		IntervalNode left = (IntervalNode) node.getLeft();
		if ((left != null)
				&& (endpointComparator.compare(left.getMaxEndpoint(), interval
						.getLowEndpoint()) > 0)) {
			searchForIntervalDuring(left, interval, resultList);
		}

		// Check for intersection with current node
		if (node.getInterval().during(interval, endpointComparator)) {
			resultList.add(node);
		}

		// Check to see whether we have to traverse the left subtree
		IntervalNode right = (IntervalNode) node.getRight();
		if ((right != null)
				&& (endpointComparator.compare(interval.getHighEndpoint(),
						right.getMinEndpoint()) > 0)) {
			searchForIntervalDuring(right, interval, resultList);
		}
	}

	@SuppressWarnings("unchecked")
	private void searchForIntervalContains(IntervalNode node,
			Interval interval, List resultList) {
		if (node == null) {
			return;
		}

		// Inorder traversal (very important to guarantee sorted order)

		// Check to see whether we have to traverse the left subtree
		IntervalNode left = (IntervalNode) node.getLeft();
		if ((left != null)
				&& (endpointComparator.compare(left.getMaxEndpoint(), interval
						.getLowEndpoint()) > 0)) {
			searchForIntervalContains(left, interval, resultList);
		}

		// Check for intersection with current node
		if (node.getInterval().contains(interval, endpointComparator)) {
			resultList.add(node);
		}

		// Check to see whether we have to traverse the left subtree
		IntervalNode right = (IntervalNode) node.getRight();
		if ((right != null)
				&& (endpointComparator.compare(interval.getHighEndpoint(),
						right.getMinEndpoint()) > 0)) {
			searchForIntervalContains(right, interval, resultList);
		}
	}

	@SuppressWarnings("unchecked")
	private void searchForIntervalFinishes(IntervalNode node,
			Interval interval, List resultList) {
		if (node == null) {
			return;
		}

		// Inorder traversal (very important to guarantee sorted order)

		// Check to see whether we have to traverse the left subtree
		IntervalNode left = (IntervalNode) node.getLeft();
		if ((left != null)
				&& (endpointComparator.compare(left.getMaxEndpoint(), interval
						.getLowEndpoint()) > 0)) {
			searchForIntervalFinishes(left, interval, resultList);
		}

		// Check for intersection with current node
		if (node.getInterval().finishes(interval, endpointComparator)) {
			resultList.add(node);
		}

		// Check to see whether we have to traverse the left subtree
		IntervalNode right = (IntervalNode) node.getRight();
		if ((right != null)
				&& (endpointComparator.compare(interval.getHighEndpoint(),
						right.getMinEndpoint()) > 0)) {
			searchForIntervalFinishes(right, interval, resultList);
		}
	}

	@SuppressWarnings("unchecked")
	private void searchForIntervalFinishedBy(IntervalNode node,
			Interval interval, List resultList) {
		if (node == null) {
			return;
		}

		// Inorder traversal (very important to guarantee sorted order)

		// Check to see whether we have to traverse the left subtree
		IntervalNode left = (IntervalNode) node.getLeft();
		if ((left != null)
				&& (endpointComparator.compare(left.getMaxEndpoint(), interval
						.getLowEndpoint()) > 0)) {
			searchForIntervalFinishedBy(left, interval, resultList);
		}

		// Check for intersection with current node
		if (node.getInterval().finishedBy(interval, endpointComparator)) {
			resultList.add(node);
		}

		// Check to see whether we have to traverse the left subtree
		IntervalNode right = (IntervalNode) node.getRight();
		if ((right != null)
				&& (endpointComparator.compare(interval.getHighEndpoint(),
						right.getMinEndpoint()) > 0)) {
			searchForIntervalFinishedBy(right, interval, resultList);
		}
	}

	/** Debugging */
	private void printFromNode(RBNode node, PrintStream tty, int indentDepth) {
		for (int i = 0; i < indentDepth; i++) {
			tty.print(" ");
		}

		tty.print("-");
		if (node == null) {
			tty.println();
			return;
		}

		tty.println(" " + node + " (min "
				+ ((IntervalNode) node).getMinEndpoint() + ", max "
				+ ((IntervalNode) node).getMaxEndpoint() + ")"
				+ ((node.getColor() == RBColor.RED) ? " (red)" : " (black)"));
		if (node.getLeft() != null)
			printFromNode(node.getLeft(), tty, indentDepth + 2);
		if (node.getRight() != null)
			printFromNode(node.getRight(), tty, indentDepth + 2);
	}
}
