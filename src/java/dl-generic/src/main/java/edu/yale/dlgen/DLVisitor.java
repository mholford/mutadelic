package edu.yale.dlgen;

import edu.yale.dlgen.controller.DLController;

public interface DLVisitor<T> {
	T getImpl();
	T get();
	Object getOutput();
	void setInput(Object... input);
	void setDLController(DLController dl);
	void init();
}
