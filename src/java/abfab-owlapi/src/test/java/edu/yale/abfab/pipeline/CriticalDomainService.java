package edu.yale.abfab.pipeline;

import java.util.Random;
import java.util.Set;

import edu.yale.abfab.Abductor;
import edu.yale.abfab.IndividualPlus;
import edu.yale.abfab.service.AbfabServiceException;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClass;
import edu.yale.dlgen.controller.DLController;
import static edu.yale.abfab.NS.*;

public class CriticalDomainService extends AbstractPipelineService {

	

	@Override
	public IndividualPlus exec(IndividualPlus input, Abductor abductor)
			throws AbfabServiceException {
		boolean result = TestValues.CRITICAL_DOMAIN;
		DLController dl = abductor.getDLController();
		DLClass<?> inDomain = dl.clazz(DOMAIN_COLOCATION);
		if (valueFilled(dl, input.getIndividual(), inDomain)) {
			return input;
		}
		Set<DLAxiom<?>> annotation = annotatedResult(dl, input.getIndividual(),
				inDomain,
				dl.individual(NS + "Mutadelic"), String.valueOf(result));
		input.getAxioms().addAll(annotation);
		return input;
	}

	@Override
	public double cost() {
		return 1.0;
	}

}
