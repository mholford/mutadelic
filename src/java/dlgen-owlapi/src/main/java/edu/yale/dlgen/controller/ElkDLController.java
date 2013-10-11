package edu.yale.dlgen.controller;

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class ElkDLController extends OWLAPIDLController {

	@Override
	public OWLReasoner initReasoner() {
		OWLReasonerFactory rf = new ElkReasonerFactory();
		OWLReasoner r = rf.createReasoner(getOntology());
		return r;
	}

}
