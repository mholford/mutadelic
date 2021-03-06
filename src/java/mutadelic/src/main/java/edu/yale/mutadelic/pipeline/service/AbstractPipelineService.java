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
	public static final String HGVSP_NOTATION = NS + "HGVSPNotation";
	
	// Added for 3/2 paper submit
	public static final String GENE = NS + "Gene";
	
	public static final String PROTEIN_DOMAIN = NS + "ProteinDomain";
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

	/* Constants for enum-style values returned from services */
	public static final String INDEL = "Indel";
	public static final String POINT = "Point";
	public static final String SYNONYMOUS = "Synonymous";
	public static final String NON_SYNONYMOUS = "NonSynonymous";
	public static final String CDS = "CDS";
	public static final String SPLICE_SITE = "SpliceSite";
	public static final String INTRON = "Intron";
	public static final String INTERGENIC = "Intergenic";

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
		return annotatedResult(dl, input, referrant, citation, value, false,
				null);
	}

	public Set<DLAxiom<?>> annotatedResult(DLController dl,
			DLIndividual<?> input, DLClass<?> referrant,
			DLIndividual<?> citation, Object value, boolean cacheValueIgnore) {
		return annotatedResult(dl, input, referrant, citation, value,
				cacheValueIgnore, null);
	}

	public Set<DLAxiom<?>> annotatedResult(DLController dl,
			DLIndividual<?> input, DLClass<?> referrant,
			DLIndividual<?> citation, Object value, Object cacheValueProxy) {
		return annotatedResult(dl, input, referrant, citation, value, false,
				cacheValueProxy);
	}

	public Set<DLAxiom<?>> annotatedResult(DLController dl,
			DLIndividual<?> input, DLClass<?> referrant,
			DLIndividual<?> citation, Object value, boolean cacheValueIgnore,
			Object cacheValueProxy) {
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
			if (cacheValueIgnore) {
				output.add(dl.newDataFact(dl.individual(NS + valID),
						dl.dataProp(NS + "cache_value_ignore"),
						dl.asLiteral(SIO + "has_value")));
			}
			if (cacheValueProxy != null) {
				DLLiteral<?> asLiteral = dl.asLiteral(String.format("%s=%s",
						SIO + "has_value", cacheValueProxy));
				output.add(dl.newDataFact(dl.individual(NS + valID),
						dl.dataProp(NS + "cache_value_proxy"), asLiteral));
			}
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
