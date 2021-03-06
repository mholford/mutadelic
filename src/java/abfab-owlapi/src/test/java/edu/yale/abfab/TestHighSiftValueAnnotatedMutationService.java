package edu.yale.abfab;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import edu.yale.abfab.Abductor;
import edu.yale.abfab.IndividualPlus;
import edu.yale.abfab.pipeline.AbstractPipelineService;
import edu.yale.abfab.service.AbfabServiceException;
import edu.yale.abfab.service.Service;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.controller.DLController;
import static edu.yale.abfab.NS.*;

public class TestHighSiftValueAnnotatedMutationService extends AbstractPipelineService {

	@Override
	public IndividualPlus exec(IndividualPlus input, Abductor abductor)
			throws AbfabServiceException {
		double litVal = TestVals.sift;

		DLController dl = abductor.getDLController();
		Set<DLAxiom<?>> axioms = input.getAxioms();

		// Disconnect existing axioms about Sift
		Collection<DLIndividual> descs = dl.getObjectPropertyValues(
				input.getIndividual(), dl.objectProp(SIO + "is_described_by"));
		for (DLIndividual<?> desc : descs) {
			Collection<DLIndividual> refs = dl.getObjectPropertyValues(desc,
					dl.objectProp(SIO + "refers_to"));
			for (DLIndividual<?> ref : refs) {
				if (dl.getTypes(ref).contains(dl.clazz(NS + "SiftValue"))) {
					axioms.remove(dl.newObjectFact(input.getIndividual(),
							dl.objectProp(SIO + "is_described_by"), desc));
				}
			}
		}

		String csID = UUID.randomUUID().toString();
		String descID = "desc" + UUID.randomUUID().toString();
		axioms.add(dl.individualType(dl.individual(NS + descID),
				dl.clazz(SIO + "Description")));
		axioms.add(dl.individualType(dl.individual(NS + csID),
				dl.clazz(NS + "SiftValue")));
		axioms.add(dl.newObjectFact(dl.individual(NS + descID),
				dl.objectProp(SIO + "cites"), dl.individual(NS + "VEP")));
		axioms.add(dl.newDataFact(dl.individual(NS + csID),
				dl.dataProp(SIO + "has_value"), dl.asLiteral(litVal)));
		axioms.add(dl.newDataFact(dl.individual(NS + csID),
				dl.dataProp(NS + "has_level"), dl.asLiteral("High")));
		axioms.add(dl.newObjectFact(dl.individual(NS + descID),
				dl.objectProp(SIO + "refers_to"), dl.individual(NS + csID)));
		axioms.add(dl.newObjectFact(input.getIndividual(),
				dl.objectProp(SIO + "is_described_by"),
				dl.individual(NS + descID)));
		return input;
	}

	@Override
	public double cost() {
		return 1.0;
	}

}
