package edu.yale.seqtree;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.Iterator;

import edu.yale.seqtree.persist.DataMap;

public class SrutiTree extends IntervalTree {

	@SuppressWarnings("unchecked")
	private TreeStore treeStore;
	private DataMap dataMap;
	private long rootAddr;
	private boolean newBuild;

	@SuppressWarnings("unchecked")
	public SrutiTree(Comparator endpointComparator, File f, boolean newBuild) {
		super(endpointComparator);
		this.newBuild = newBuild;
		treeStore = new NIOTreeStore(f, newBuild);
		long tempRootAddr = -1;
		try {
			if (!newBuild)
				tempRootAddr = getRootAddr();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rootAddr = newBuild ? -1L : tempRootAddr;
	}

	public void close() throws IOException {
		if (newBuild) {
			putRootAddr();
		}
		treeStore.close();
	}

	public long insert(Interval interval, int chr) {
		SrutiNode2 node = new SrutiNode2(interval, chr, endpointComparator,
				treeStore.newAddress(), treeStore);
		insertNode(node);
		return node.getAddress();
	}

	public void putRootAddr() throws IOException {
		long addr = treeStore.newAddress();
		treeStore.putLong(rootAddr, addr);
		treeStore.setAddress(addr + 8);
	}

	public long getRootAddr() throws IOException {
		if (treeStore.getSize() >= 8) {
			long psize = treeStore.getSize();
			long rootaddr = treeStore.getLong(psize - 8);
			return rootaddr;
		} else {
			return -1;
		}
	}

	public long insert(Sequence locus) {
		return insert(getInterval(locus), Chromosomes.getChromIndex(locus
				.getChromosome()));
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
			SrutiNode2 nx = (SrutiNode2) internal.next();
			Sequence seq = new Sequence(nx.getStart(), nx.getEnd(), Chromosomes
					.getChromosome(nx.getChromosome()));
			String lookupID = String.format("%d|%d", nx.getChromosome(), nx
					.getAddress());
			String name = dataMap.getNameFromID(lookupID);
			seq.setName(name);
			return seq;
		}

		@Override
		public void remove() {
			throw new RuntimeException("Not supported");
		}

	}

	@Override
	public Iterator<Sequence> getSequenceIterator(
			Iterator<IntervalNode> internal) {
		return new SequenceIterator(internal);
	}

	@Override
	public RBNode getRoot() {
		if (rootAddr < 0) {
			return null;
		}
		if (root != null) {
			return root;
		}
		return new SrutiNode2(endpointComparator, rootAddr, treeStore);
	}



	@Override
	public void setRoot(RBNode root) {
		super.setRoot(root);
		rootAddr = ((SrutiNode2) root).getAddress();
	}

	@SuppressWarnings("unchecked")
	public TreeStore getTreeStore() {
		return treeStore;
	}

	public void setDataMap(DataMap dataMap) {
		this.dataMap = dataMap;
	}
}
