package edu.yale.abfab.service;

import edu.yale.abfab.Abductor;
import edu.yale.abfab.IndividualPlus;

public interface Service {

	IndividualPlus exec(IndividualPlus input, Abductor abductor)
			throws AbfabServiceException;

	double cost();
}
