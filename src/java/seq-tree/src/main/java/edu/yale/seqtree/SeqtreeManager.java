package edu.yale.seqtree;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import edu.yale.seqtree.persist.MemoryDataMap;
import edu.yale.seqtree.persist.DataMap;

@SuppressWarnings("unused")
public class SeqtreeManager {
	private static SeqtreeManager INSTANCE;
	private DataMap<String> dataMap;
	private IntervalTree[] itrees;
	public static int GETS = 0;
	public static int PUTS = 0;

	private SeqtreeManager() {

	}

	public static SeqtreeManager instance() {
		if (INSTANCE == null) {
			INSTANCE = new SeqtreeManager();
		}
		return INSTANCE;
	}

	public void init(DataMap dataMap, boolean buildNew) {
		this.dataMap = dataMap;
		Chromosomes.init();
		int numChrom = Chromosomes.count();

		dataMap.init(buildNew);

		itrees = new IntervalTree[numChrom];

		for (int i = 0; i < numChrom; i++) {
			File f = new File("/vol/Users/meh46/nio/NIOFile" + i + ".nio");
			if (!f.exists()) {
				f = new File("/home/matt/nio/NIOFile" + i + ".nio");
			}
			itrees[i] = new SrutiTree(new SimpleComparator(), f, buildNew);
			((SrutiTree) itrees[i]).setDataMap(dataMap);
//			itrees[i] = new IntervalTree(new SimpleComparator());
		}
	}

	public void addExtent(String node, Sequence extent) {
		int idx = Chromosomes.getChromIndex(extent.getChromosome());
		if (idx < 0) {
			return;
		}
//		if (!dataMap.check(node)) {
			long addr = itrees[idx].insert(extent);
			String id = String.format("%d|%d", idx, addr);
			String name = extent.getName();
//			dataMap.putIDToName(name, id);
			dataMap.putNameToID(id, name);
//			persister.addToExtentDB(node, extent, addr, idx);
//		}
//		itrees[idx].insert(extent);
	}

	private int getChromIndex(String s) {
		return Chromosomes.getChromIndex(s);
	}

	public Iterator<Sequence> touches(Sequence extent) {
		int idx = getChromIndex(extent.getChromosome());
		return itrees[idx].touchesNodeIterator(extent);
	}

	public Iterator<Sequence> after(Sequence extent) {
		int idx = getChromIndex(extent.getChromosome());
		return itrees[idx].afterNodeIterator(extent);
	}

	public Iterator<Sequence> before(Sequence extent) {
		int idx = getChromIndex(extent.getChromosome());
		return itrees[idx].beforeNodeIterator(extent);
	}

	public Iterator<Sequence> contains(Sequence extent) {
		int idx = getChromIndex(extent.getChromosome());
		return itrees[idx].containsNodeIterator(extent);
	}

	public Iterator<Sequence> during(Sequence extent) {
		int idx = getChromIndex(extent.getChromosome());
		return itrees[idx].duringNodeIterator(extent);
	}

	public Iterator<Sequence> finishedBy(Sequence extent) {
		int idx = getChromIndex(extent.getChromosome());
		return itrees[idx].finishedByNodeIterator(extent);
	}

	public Iterator<Sequence> finishes(Sequence extent) {
		int idx = getChromIndex(extent.getChromosome());
		return itrees[idx].finishesNodeIterator(extent);
	}

	public Iterator<Sequence> meets(Sequence extent) {
		int idx = getChromIndex(extent.getChromosome());
		return itrees[idx].meetsNodeIterator(extent);
	}

	public Iterator<Sequence> metBy(Sequence extent) {
		int idx = getChromIndex(extent.getChromosome());
		return itrees[idx].metByNodeIterator(extent);
	}

	public Iterator<Sequence> overlappedBy(Sequence extent) {
		int idx = getChromIndex(extent.getChromosome());
		return itrees[idx].overlappedByNodeIterator(extent);
	}

	public Iterator<Sequence> overlaps(Sequence extent) {
		int idx = getChromIndex(extent.getChromosome());
		return itrees[idx].overlapsNodeIterator(extent);
	}

	public Iterator<Sequence> startedBy(Sequence extent) {
		int idx = getChromIndex(extent.getChromosome());
		return itrees[idx].startedByNodeIterator(extent);
	}

	public Iterator<Sequence> starts(Sequence extent) {
		int idx = getChromIndex(extent.getChromosome());
		return itrees[idx].startsNodeIterator(extent);
	}

	public Iterator<Sequence> equalTo(Sequence extent) {
		int idx = getChromIndex(extent.getChromosome());
		return itrees[idx].equalToNodeIterator(extent);
	}
	
	public void postLoad() {
		dataMap.postLoad();
	}
	
	public void close() {
		dataMap.close();
		try {
			for (IntervalTree i : itrees) {
				SrutiTree s = (SrutiTree) i;
				s.close();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
