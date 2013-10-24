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

public class SiftService extends AbstractPipelineService {

	
	@Override
	public IndividualPlus exec(IndividualPlus input, Abductor abductor)
			throws AbfabServiceException {
		double result = TestValues.SIFT;
		DLController dl = abductor.getDLController();
		DLClass<?> siftScore = dl.clazz(SIFT_SCORE);
		if (valueFilled(dl, input.getIndividual(), siftScore)) {
			return input;
		}
		Set<DLAxiom<?>> annotation = annotatedResult(dl, input.getIndividual(),
				siftScore, dl.individual(MUTADELIC),
				result);
		input.getAxioms().addAll(annotation);
		return input;
	}

	@Override
	public double cost() {
		return 1.0;
	}

}
