package edu.yale.abfab.v1.service;

import edu.yale.abfab.old.Abductor;
import edu.yale.abfab.old.IndividualPlus;

public interface Service {

	IndividualPlus exec(IndividualPlus input, Abductor abductor)
			throws AbfabServiceException;

	double cost();
}
