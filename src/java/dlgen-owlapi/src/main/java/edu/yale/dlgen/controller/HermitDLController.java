package edu.yale.dlgen.controller;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class HermitDLController extends OWLAPIDLController {

	@Override
	public OWLReasoner initReasoner() {
		OWLReasoner reasoner = new Reasoner(getOntology());
		return reasoner;
	}

}
