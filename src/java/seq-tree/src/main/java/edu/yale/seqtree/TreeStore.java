package edu.yale.seqtree;

import java.io.IOException;

public interface TreeStore<T> {
	T retrieveNode(long addr) throws IOException;
	void putNode(T node, long addr) throws IOException;
	long newAddress();
	void setAddress(long addr);
	long getAddress();
	void putLong(long val, long addr) throws IOException;
	void close() throws IOException;
	long getSize() throws IOException;
	long getLong(long addr) throws IOException;
	int getInt(long addr) throws IOException;
	void putInt(int val, long addr) throws IOException;
}
