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

public class MarkRareUnusualService extends AbstractPipelineService {

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

	@Override
	public double cost() {
		return 1.0;
	}

	private String getResult(IndividualPlus input, Abductor abductor) {
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
			boolean output = dl.checkEntailed(dl.individualType(
					input.getIndividual(),
					dl.clazz(NS + "UnusualVariant")));
			abductor.debug();
			if (!output)
				input.setStop(true);
			result = output ? "RareAndUnusual" : "NotRareAndUnusual";
			System.out.println(result);
		} finally {
			dl.removeAxioms(newAx);
		}

		return result;
	}
}
