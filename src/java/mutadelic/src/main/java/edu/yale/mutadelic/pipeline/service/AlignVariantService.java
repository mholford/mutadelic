package edu.yale.mutadelic.pipeline.service;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Set;

import edu.yale.abfab.Abductor;
import edu.yale.abfab.IndividualPlus;
import edu.yale.abfab.service.AbfabServiceException;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClass;
import edu.yale.dlgen.controller.DLController;
import edu.yale.mutadelic.ncbivr.NCBIVariationReporter;
import edu.yale.mutadelic.pipeline.model.Variant;
import static edu.yale.abfab.NS.*;
import static edu.yale.mutadelic.ncbivr.NCBIVariationReporter.*;

public class AlignVariantService extends AbstractPipelineService {

	@Override
	public IndividualPlus exec(IndividualPlus input, Abductor abductor)
			throws AbfabServiceException {
		DLController dl = abductor.getDLController();
		DLClass<?> hgvs = dl.clazz(HGVS_NOTATION);
		if (valueFilled(dl, input.getIndividual(), hgvs)) {
			return input;
		}
		Variant v = Variant.fromOWL(dl, input);
		String result = "";
		try {
			result = getAlignmentForVariant(v);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Set<DLAxiom<?>> annotation = annotatedResult(dl, input.getIndividual(),
				hgvs, dl.individual(MUTADELIC), result);
		input.getAxioms().addAll(annotation);
		return input;
	}

	private String getAlignmentForVariant(Variant v) throws Exception {
		String alignment = null;
		NCBIVariationReporter ncbivr = new NCBIVariationReporter();
		String q = var2NCBIVR(v);
		String r = ncbivr.analyze(HUMAN, DEFAULT_ASSEMBLY, q);
		BufferedReader br = new BufferedReader(new StringReader(r));
		String s;
		br.readLine();
		while ((s = br.readLine()) != null) {
			if (s.length() > 0 && !(s.startsWith("#"))) {
				String[] ss = s.split("\t");
				if (ss.length > 12) {
					alignment = ss[12];
					System.out.println("Alignment: " + alignment);
				}
			}

		}
		return alignment;
	}

	private String var2NCBIVR(Variant v) {
		String output = String.format("%s:g.%d%s>%s",
				CHR_NUC.get(v.getChromosome()), v.getStartPos(),
				v.getReference(), v.getObserved());
		return output;
	}

	@Override
	public double cost() {
		return 1.0;
	}

}
