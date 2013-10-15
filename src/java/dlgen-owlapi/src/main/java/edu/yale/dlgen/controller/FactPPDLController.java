package edu.yale.dlgen.controller;

import org.semanticweb.owlapi.reasoner.OWLReasoner;

import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;

public class FactPPDLController extends OWLAPIDLController {

	@Override
	public OWLReasoner initReasoner() {
		return new FaCTPlusPlusReasonerFactory().createReasoner(getOntology());
	}

}
