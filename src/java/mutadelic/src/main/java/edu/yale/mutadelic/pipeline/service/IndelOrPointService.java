package edu.yale.mutadelic.pipeline.service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

import edu.yale.abfab.Abductor;
import edu.yale.abfab.IndividualPlus;
import edu.yale.abfab.service.AbfabServiceException;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClass;
import edu.yale.dlgen.controller.DLController;
import edu.yale.mutadelic.pipeline.model.Variant;
import static edu.yale.abfab.NS.*;

public class IndelOrPointService extends AbstractPipelineService {

	@Override
	public IndividualPlus exec(IndividualPlus input, Abductor abductor)
			throws AbfabServiceException {
		Variant v = Variant.fromOWL(abductor.getDLController(), input);
		String result = "Point";
		List seqs = Arrays.asList("A", "C", "G", "T");
		if (v.getObserved().length() != 1 || v.getReference().length() != 1
				|| !(seqs.contains(v.getObserved()))
				|| !(seqs.contains(v.getReference()))) {
			result = "Indel";
		}
		DLController dl = abductor.getDLController();
		DLClass<?> variationType = dl.clazz(VARIATION_TYPE);
		if (valueFilled(dl, input.getIndividual(), variationType)) {
			return input;
		}
		Set<DLAxiom<?>> annotation = annotatedResult(dl, input.getIndividual(),
				variationType, dl.individual(MUTADELIC), result);
		input.getAxioms().addAll(annotation);
		return input;
	}

	@Override
	public double cost() {
		return 1.0;
	}

}
