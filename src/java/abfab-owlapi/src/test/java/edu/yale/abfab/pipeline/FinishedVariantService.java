package edu.yale.abfab.pipeline;

import java.util.Set;

import edu.yale.abfab.Abductor;
import edu.yale.abfab.IndividualPlus;
import edu.yale.abfab.service.AbfabServiceException;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.controller.DLController;
import static edu.yale.abfab.NS.*;

public class FinishedVariantService extends AbstractPipelineService {

	@Override
	public IndividualPlus exec(IndividualPlus input, Abductor abductor)
			throws AbfabServiceException {
		DLController dl = abductor.getDLController();
		Set<DLAxiom<?>> annotation = annotatedResult(dl, input.getIndividual(),
				dl.clazz(NS + "CompletionStatus"),
				dl.individual(NS + "Mutadelic"), true);
		input.getAxioms().addAll(annotation);
		return input;
	}

	@Override
	public double cost() {
		return 1.0;
	}

}
