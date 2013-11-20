package edu.yale.mutadelic.pipeline.service;

import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.yale.abfab.Abductor;
import edu.yale.abfab.IndividualPlus;
import edu.yale.abfab.service.AbfabServiceException;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClass;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.DLLiteral;
import edu.yale.dlgen.controller.DLController;
import static edu.yale.abfab.NS.*;
import static org.junit.Assert.fail;

public class TranscriptLocaleService extends AbstractPipelineService {

	@Override
	public IndividualPlus exec(IndividualPlus input, Abductor abductor)
			throws AbfabServiceException {
		DLController dl = abductor.getDLController();
		DLClass<?> variationLocation = dl.clazz(VARIATION_LOCATION);
		if (valueFilled(dl, input.getIndividual(), variationLocation)) {
			return input;
		}

		String result = getResult(input, dl);

		Set<DLAxiom<?>> annotation = annotatedResult(dl, input.getIndividual(),
				variationLocation, dl.individual(MUTADELIC), result);
		input.getAxioms().addAll(annotation);
		return input;
	}

	private String getResult(IndividualPlus ip, DLController dl) {
		/* Get HGVS from Variant */
		dl.addAxioms(ip.getAxioms());
		String hgvs = null;

		Collection<DLIndividual> descs = dl.getObjectPropertyValues(
				ip.getIndividual(), dl.objectProp(SIO + "is_described_by"));
		for (DLIndividual<?> desc : descs) {
			Collection<DLIndividual> refs = dl.getObjectPropertyValues(desc,
					dl.objectProp(SIO + "refers_to"));
			for (DLIndividual<?> ref : refs) {
				if (dl.getTypes(ref).contains(dl.clazz(HGVS_NOTATION))) {
					Collection<DLLiteral> vals = dl.getDataPropertyValues(ref,
							dl.dataProp(SIO + "has_value"));
					for (DLLiteral<?> val : vals) {
						if (hgvs != null) {
							System.out.println("Oops; more than one result");
							fail();
						}
						hgvs = dl.getLiteralValue(val);
					}
				}
			}
		}
		dl.removeAxioms(ip.getAxioms());

		/* Workaround til proper alignment of intergenic variants */
		if (hgvs.equals("INTERGENIC")) {
			return INTERGENIC;
		}

		/* Extract position portion using regex */
		Pattern p = Pattern.compile(".*:c\\.([^ACGT]+).*");
		Matcher m = p.matcher(hgvs);
		m.matches();
		String pos = m.group(1);

		if (pos.startsWith("-") || pos.startsWith("*")) {
			return INTERGENIC;
		} else if (pos.contains("-") || pos.contains("+")) {

			/*
			 * If within gene, get trailing int to determine if 'splice-site' or
			 * 'intronic'
			 */
			String offsetPre;
			if (pos.contains("-")) {
				offsetPre = pos.substring(pos.lastIndexOf("-") + 1);
			} else {
				offsetPre = pos.substring(pos.lastIndexOf("+") + 1);
			}
			int offset = Integer.parseInt(offsetPre);
			return offset <= 5 ? SPLICE_SITE : INTRON;
		} else {
			return CDS;
		}
	}

	@Override
	public double cost() {
		return 1.0;
	}

}
