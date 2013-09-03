package edu.yale.abfab.service;

import edu.yale.dlgen.DLIndividual;

public interface Service {
	
	DLIndividual exec(DLIndividual input) throws AbfabServiceException;
	double cost();
}
