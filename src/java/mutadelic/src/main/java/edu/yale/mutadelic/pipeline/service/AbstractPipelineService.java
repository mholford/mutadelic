package edu.yale.mutadelic.pipeline.service;

import static edu.yale.abfab.NS.*;
import static edu.yale.abfab.Logging.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import edu.yale.abfab.Abductor;
import edu.yale.abfab.IndividualPlus;
import edu.yale.abfab.service.AbfabServiceException;
import edu.yale.abfab.service.Service;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClass;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.DLLiteral;
import edu.yale.dlgen.controller.DLController;

public abstract class AbstractPipelineService implements Service {

	public static final String VARIATION_OUTCOME = NS + "VariationOutcome";
	public static final String MUTADELIC = NS + "Mutadelic";
	public static final String HGVS_NOTATION = NS + "HGVSNotation";
	public static final String ALLELE_FREQUENCY = NS + "AlleleFrequency";
	public static final String DOMAINS_MISSING = NS
			+ "VariationDomainsMissingStatus";

	public static final String DOMAIN_COLOCATION = NS
			+ "VariationDomainColocation";
	public static final String COMPLETION_STATUS = NS + "CompletionStatus";
	public static final String VARIATION_TYPE = NS + "VariationType";
	public static final String STATUS_MARKER = NS + "StatusMarker";
	public static final String PHYLOP_SCORE = NS + "PhylopScore";
	public static final String DATABASE_PRESENCE = NS + "DatabasePresence";
	public static final String SIFT_SCORE = NS + "SiftScore";
	public static final String VARIATION_LOCATION = NS + "VariationLocation";

	public abstract IndividualPlus exec(IndividualPlus ip, Abductor ab)
			throws AbfabServiceException;

	public IndividualPlus serviceExec(IndividualPlus ip, Abductor ab) {
		IndividualPlus output = null;
		long start = System.currentTimeMillis();
		System.out.println("Running service: " + getClass());
		try {
			output = exec(ip, ab);
		} catch (Exception e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		dbg(DBG_TIMING, "%s service executed in %d millis", getClass(), end
				- start);
		return output;
	}

	public Set<DLAxiom<?>> annotatedResult(DLController dl,
			DLIndividual<?> input, DLClass<?> referrant,
			DLIndividual<?> citation, Object value) {
		Set<DLAxiom<?>> output = new HashSet<>();

		if (value != null) {
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
		}
		return output;
	}

	public boolean valueFilled(DLController dl, DLIndividual<?> input,
			DLClass<?> referrant) {
		Collection<DLIndividual> descs = dl.getObjectPropertyValues(input,
				dl.objectProp(SIO + "is_described_by"));
		for (DLIndividual<?> desc : descs) {
			Collection<DLIndividual> refs = dl.getObjectPropertyValues(desc,
					dl.objectProp(SIO + "refers_to"));
			for (DLIndividual<?> ref : refs) {
				if (dl.getTypes(ref).contains(referrant)) {
					return true;
				}
			}
		}
		return false;
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
