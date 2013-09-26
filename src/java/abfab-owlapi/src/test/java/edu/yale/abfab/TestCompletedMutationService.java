package edu.yale.abfab;

import edu.yale.abfab.service.AbfabServiceException;
import edu.yale.abfab.service.Service;

public class TestCompletedMutationService implements Service {

	@Override
	public IndividualPlus exec(IndividualPlus input, Abductor abductor)
			throws AbfabServiceException {
		return input;
	}

	@Override
	public double cost() {
		return 1.0;
	}

}
