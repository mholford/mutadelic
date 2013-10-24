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

public class AAChangedService extends AbstractPipelineService {

	@Override
	public IndividualPlus exec(IndividualPlus input, Abductor abductor)
			throws AbfabServiceException {
		String result = TestValues.AA_CHANGE;
		DLController dl = abductor.getDLController();
		DLClass<?> variationOutcome = dl.clazz(VARIATION_OUTCOME);
		if (valueFilled(dl, input.getIndividual(), variationOutcome)) {
			return input;
		}
		Set<DLAxiom<?>> annotation = annotatedResult(dl, input.getIndividual(),
				dl.clazz(VARIATION_OUTCOME), dl.individual(MUTADELIC),
				result);
		input.getAxioms().addAll(annotation);
		return input;
	}

	@Override
	public double cost() {
		return 1.0;
	}

}
