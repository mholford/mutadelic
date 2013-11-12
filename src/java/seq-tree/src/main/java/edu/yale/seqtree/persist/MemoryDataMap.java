package edu.yale.seqtree.persist;

import java.util.HashMap;
import java.util.Map;

import edu.yale.seqtree.Sequence;

public class MemoryDataMap implements DataMap<String> {

	private Map<String, String> nameToID;
	private Map<String, String> IDToName;

	@Override
	public boolean check(String name) {
		return nameToID.containsKey(name);
	}

	@Override
	public void close() {

	}
	@Override
	public void init(boolean build) {
		nameToID = new HashMap<String, String>();
		IDToName = new HashMap<String, String>();
	}

	@Override
	public void postLoad() {

	}

	@Override
	public long size() {
		return nameToID.size();
	}

//	@Override
//	public String getIDFromName(String name) {
//		return nameToID.get(name);
//	}

	@Override
	public String getNameFromID(String ID) {
		return IDToName.get(ID);
	}

	@Override
	public void putIDToName(String name, String ID) {
		IDToName.put(ID, name);
	}

//	@Override
//	public void putNameToID(String ID, String name) {
//		nameToID.put(name, ID);
//	}

}
