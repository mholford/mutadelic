package edu.yale.abfab.pipeline;

import java.util.Random;
import java.util.Set;

import edu.yale.abfab.Abductor;
import edu.yale.abfab.IndividualPlus;
import edu.yale.abfab.service.AbfabServiceException;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.controller.DLController;
import static edu.yale.abfab.NS.*;

public class AAChangedService extends AbstractPipelineService {

	@Override
	public IndividualPlus exec(IndividualPlus input, Abductor abductor)
			throws AbfabServiceException {
		String result = new Random().nextBoolean() ? "Synonymous" : "NonSynonymous";
		DLController dl = abductor.getDLController();
		Set<DLAxiom<?>> annotation = annotatedResult(dl, input.getIndividual(),
				dl.clazz(NS + "VariationOutcome"), dl.individual(NS + "Mutadelic"),
				result);
		input.getAxioms().addAll(annotation);
		return input;
	}

	@Override
	public double cost() {
		return 1.0;
	}

}