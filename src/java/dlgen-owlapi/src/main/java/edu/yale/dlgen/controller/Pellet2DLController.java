package edu.yale.dlgen.controller;


import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class Pellet2DLController extends OWLAPIDLController {

	@Override
	public OWLReasoner initReasoner() {
		PelletReasonerFactory rf = new PelletReasonerFactory();
		PelletReasoner reasoner = rf.createNonBufferingReasoner(getOntology());
		manager.addOntologyChangeListener(reasoner);
		return reasoner;
	}

}
