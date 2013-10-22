package edu.yale.abfab.pipeline;

import static edu.yale.abfab.NS.NS;
import static edu.yale.abfab.NS.SIO;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import edu.yale.abfab.Abductor;
import edu.yale.abfab.service.Service;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClass;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.DLLiteral;
import edu.yale.dlgen.controller.DLController;

public abstract class AbstractPipelineService implements Service {

	public Set<DLAxiom<?>> annotatedResult(DLController dl,
			DLIndividual<?> input, DLClass<?> referrant,
			DLIndividual<?> citation, Object value) {
		Set<DLAxiom<?>> output = new HashSet<>();
		
		String valID = "val" + UUID.randomUUID().toString();
		String descID = "desc" + UUID.randomUUID().toString();
		output.add(dl.individualType(dl.individual(NS + descID),
				dl.clazz(SIO + "Description")));
		output.add(dl.individualType(dl.individual(NS + valID), referrant));
		output.add(dl.newObjectFact(dl.individual(NS + descID),
				dl.objectProp(SIO + "cites"), citation));
		output.add(dl.newDataFact(dl.individual(NS + valID),
				dl.dataProp(SIO + "has_value"), getLiteral(dl, value)));
		output.add(dl.newObjectFact(dl.individual(NS + descID),
				dl.objectProp(SIO + "refers_to"), dl.individual(NS + valID)));
		output.add(dl.newObjectFact(input,
				dl.objectProp(SIO + "is_described_by"),
				dl.individual(NS + descID)));

		return output;
	}

	private DLLiteral<?> getLiteral(DLController dl, Object value) {
		if (value instanceof Boolean) {
			return dl.asLiteral(((Boolean) value).booleanValue());
		} else if (value instanceof String) {
			return dl.asLiteral((String) value);
		} else if (value instanceof Double) {
			return dl.asLiteral(((Double) value).doubleValue());
		}
		return null;
	}
}
