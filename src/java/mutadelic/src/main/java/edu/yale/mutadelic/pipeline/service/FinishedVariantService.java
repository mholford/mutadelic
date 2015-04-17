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
				boolean result = false;
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
							input.getIndividual(), dl.clazz(NS + "InterestingVariant")));
					if (!output)
						input.setStop(true);
					result = output;
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
