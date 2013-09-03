package edu.yale.med.krauthammerlab.abfab.old;

import org.semanticweb.owlapi.model.OWLClass;

public class SimpleStep extends Step {
	OWLClass clz;

	public SimpleStep(OWLClass clz) {
		this.clz = clz;
	}

	public OWLClass get() {
		return clz;
	}

}
