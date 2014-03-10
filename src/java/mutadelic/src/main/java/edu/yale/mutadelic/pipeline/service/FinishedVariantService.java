package edu.yale.mutadelic.pipeline.service;

import java.util.HashSet;
import java.util.Set;

import edu.yale.abfab.Abductor;
import edu.yale.abfab.Abductor.SCCIndividual;
import edu.yale.abfab.Abductor.SCCKey;
import edu.yale.abfab.IndividualPlus;
import edu.yale.abfab.service.AbfabServiceException;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClass;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.controller.DLController;
import static edu.yale.abfab.NS.*;

public class FinishedVariantService extends AbstractPipelineService {

	@Override
	public IndividualPlus exec(IndividualPlus input, Abductor abductor)
			throws AbfabServiceException {
		boolean result = getResult(input, abductor);
		DLController dl = abductor.getDLController();
		DLClass<?> completionStatus = dl.clazz(COMPLETION_STATUS);
		if (valueFilled(dl, input.getIndividual(), completionStatus)) {
			return input;
		}
		Set<DLAxiom<?>> annotation = annotatedResult(dl, input.getIndividual(),
				completionStatus, dl.individual(MUTADELIC),
				String.valueOf(result));
		input.getAxioms().addAll(annotation);
		return input;
	}

	private boolean getResult(IndividualPlus input, Abductor abductor) {
		// Check if input fits the defined Service
		if (input.isStop()) {
			return false;
		}
		DLController dl = abductor.getDLController();
		DLClass<?> serviceClass = dl.clazz(NS + "FinishedVariantService");
		Set<DLIndividual> serviceClassOutputs = new HashSet<>();
		Set<IndividualPlus> serviceClassOutputIPs = new HashSet<>();
		for (DLIndividual<?> serviceClassInstance : dl
				.getInstances(serviceClass)) {
			serviceClassOutputs.addAll(dl.getObjectPropertyValues(serviceClassInstance,
					dl.objectProp(NS + "has_output")));
		}
		for (DLIndividual serviceClassOutput:serviceClassOutputs) {
			serviceClassOutputIPs.add(new IndividualPlus(serviceClassOutput));
		}
		IndividualPlus ipOut = abductor.mergeIndividuals(serviceClassOutputIPs);
		SCCIndividual scOut = abductor.createSCCIndividual(ipOut);
		SCCIndividual scIn = abductor.createSCCIndividual(input);
		SCCKey  key = abductor.createSCCKey(serviceClass, scIn, scOut);
		boolean output = abductor.checkSCCache(key, true);
		
		return output;
	}

	@Override
	public double cost() {
		return 1.0;
	}

}
