package edu.yale.seqtree.persist;

import edu.yale.seqtree.Sequence;

public interface DataMap<T> {

	void init(boolean build);

	boolean check(T node);
	
	void putNameToID(String name, String ID);
	
//	void putIDToName(String ID, String name);
	
	String getNameFromID(String ID);
	
//	String getIDFromName(String name);

//	void add(T node, Sequence sequence, long addr, int index);

//	void addToNodeDB(T node, Sequence sequence);

//	void addToExtentDB(T node, Sequence sequence, long addr, int index);

	long size();

//	T extentToNode(int extent);

//	Object[] nodeToExtent(T n);

	void postLoad();

	void close();
}
