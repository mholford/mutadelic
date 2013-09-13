package edu.yale.abfab.v2.service;

import edu.yale.abfab.v2.Abductor;
import edu.yale.abfab.v2.IndividualPlus;


public interface Service {

	IndividualPlus exec(IndividualPlus input, Abductor abductor)
			throws AbfabServiceException;

	double cost();
}
