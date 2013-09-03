package edu.yale.dlgen;

public class DLObjectUnion<T> extends DLClassExpression<T> {

	public DLObjectUnion() {

	}

	public DLObjectUnion(T impl) {
		super(impl);
	}

}
