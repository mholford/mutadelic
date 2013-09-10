package edu.yale.abfab;

import java.util.Set;
import java.util.UUID;

import edu.yale.abfab.service.AbfabServiceException;
import edu.yale.abfab.service.Service;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.controller.DLController;

import static edu.yale.abfab.NS.*;

public class TestFinishedMutationService implements Service {

	@Override
	public IndividualPlus exec(IndividualPlus input, Abductor abductor)
			throws AbfabServiceException {
		DLController dl = abductor.getDLController();
		Set<DLAxiom<?>> axioms = input.getAxioms();
		String csID = "cs" + UUID.randomUUID().toString();
		String descID = "desc" + UUID.randomUUID().toString();
		axioms.add(dl.individualType(dl.individual(descID),
				dl.clazz(SIO + "Description")));
		axioms.add(dl.individualType(dl.individual(csID),
				dl.clazz(NS + "CompletionStatus")));
		axioms.add(dl.newObjectFact(dl.individual(descID),
				dl.objectProp(SIO + "cites"), dl.individual(NS + "VEP")));
		axioms.add(dl.newDataFact(dl.individual(descID),
				dl.dataProp(SIO + "has_value"), dl.asLiteral(true)));
		axioms.add(dl.newObjectFact(dl.individual(descID),
				dl.objectProp(SIO + "refers_to"), dl.individual(csID)));
		axioms.add(dl.newObjectFact(input.getIndividual(),
				dl.objectProp(SIO + "is_described_by"), dl.individual(descID)));
		return input;
	}

	@Override
	public double cost() {
		return 1.0;
	}

}
