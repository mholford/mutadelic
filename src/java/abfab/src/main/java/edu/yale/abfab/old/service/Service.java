package edu.yale.med.krauthammerlab.abfab.old.service;

import org.semanticweb.owlapi.model.OWLIndividual;

public interface Service {
	
	OWLIndividual exec(OWLIndividual input) throws AbfabServiceException;
	double cost();
}
