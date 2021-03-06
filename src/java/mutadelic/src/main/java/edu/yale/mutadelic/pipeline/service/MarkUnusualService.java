package edu.yale.mutadelic.pipeline.service;

import java.util.HashSet;
import java.util.Set;

import edu.yale.abfab.Abductor;
import edu.yale.abfab.IndividualPlus;
import edu.yale.abfab.SCCIndividual;
import edu.yale.abfab.SCCKey;
import edu.yale.abfab.service.AbfabServiceException;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClass;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.controller.DLController;
import static edu.yale.abfab.NS.*;

public class MarkUnusualService extends AbstractPipelineService {

	@Override
	public IndividualPlus exec(IndividualPlus input, Abductor abductor)
			throws AbfabServiceException {
		String result = getResult(input, abductor);
		DLController dl = abductor.getDLController();
		DLClass<?> statusMarker = dl.clazz(STATUS_MARKER);
		Set<DLAxiom<?>> annotation = annotatedResult(dl, input.getIndividual(),
				statusMarker, dl.individual(MUTADELIC), result);
		input.getAxioms().addAll(annotation);
		return input;
	}

	private String getResult(IndividualPlus input, Abductor abductor) {
		// Check if input fits the defined Service
		String result = null;
		DLController dl = abductor.getDLController();
		Set<DLAxiom<?>> newAx = new HashSet<>();
		for (DLAxiom<?> ax : input.getAxioms()) {
			if (!dl.getAxioms().contains(ax)) {
				newAx.add(ax);
			}
		}
		try {
			dl.addAxioms(newAx);

			// DLClass<?> serviceClass = dl.clazz(NS +
			// "MarkedUnusualVariantService");
			// Set<DLIndividual> serviceClassOutputs = new HashSet<>();
			// Set<IndividualPlus> serviceClassOutputIPs = new HashSet<>();
			// for (DLIndividual<?> serviceClassInstance : dl
			// .getInstances(serviceClass)) {
			// serviceClassOutputs.addAll(dl.getObjectPropertyValues(
			// serviceClassInstance, dl.objectProp(NS + "has_output")));
			// }
			// for (DLIndividual serviceClassOutput : serviceClassOutputs) {
			// serviceClassOutputIPs.add(new
			// IndividualPlus(serviceClassOutput));
			// }
			// IndividualPlus ipOut =
			// abductor.mergeIndividuals(serviceClassOutputIPs);
			// SCCIndividual scOut = abductor.createSCCIndividual(ipOut);
			// SCCIndividual scIn = abductor.createSCCIndividual(input);
			// SCCKey key = abductor.createSCCKey(serviceClass, scIn, scOut);
			// boolean output = abductor.checkSCCache(key, true);

			boolean output = dl.checkEntailed(dl.individualType(
					input.getIndividual(), dl.clazz(NS + "UnusualVariant")));
			if (!output)
				input.setStop(true);
			result = output ? "Unusual" : "NotUnusual";
			System.out.println(result);
		} finally {
			dl.removeAxioms(newAx);
		}

		return result;
	}

	@Override
	public double cost() {
		return 1.0;
	}

}
