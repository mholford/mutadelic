package edu.yale.mutadelic.pipeline.service;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import edu.yale.abfab.Abductor;
import edu.yale.abfab.IndividualPlus;
import edu.yale.abfab.service.AbfabServiceException;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClass;
import edu.yale.dlgen.controller.DLController;
import edu.yale.mutadelic.mongo.MongoConnection;
import edu.yale.mutadelic.ncbivr.NCBIVariationReporter;
import edu.yale.mutadelic.pipeline.model.Variant;
import static edu.yale.abfab.NS.*;
import static edu.yale.mutadelic.mongo.MongoConnection.*;
import static edu.yale.mutadelic.ncbivr.NCBIVariationReporter.*;
import static edu.yale.mutadelic.pipeline.service.SiftService.*;

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
			// result = getAlignmentForVariantFromNCBIVR(v);
			result = getAlignmentForVariantFromSift(v);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Set<DLAxiom<?>> annotation = annotatedResult(dl, input.getIndividual(),
				hgvs, dl.individual(MUTADELIC), result, true);
		input.getAxioms().addAll(annotation);
		return input;
	}

	private String getAlignmentForVariantFromSift(Variant v) {
		String alignment = "";
		DBCollection table = MongoConnection.instance().getSiftTable();
		String key = siftKey(v);

		DBObject q = new BasicDBObject();
		q.put("_id", key);
		DBObject r = table.findOne(q);

		if (r != null) {
			String vals = (String) r.get(SIFT_VALUES);
			String[] ss = vals.split(";", -1);
			int idx = siftIndex(v);
			String siftData = ss[idx];

			String[] sds = siftData.split(",", -1);
			String transcript = sds[4];
			String tPos = sds[5];
			alignment = String.format("%s:c.%s%s>%s", transcript, tPos,
					v.getReference(), v.getObserved());
		}

		return alignment;
	}

	private String getAlignmentForVariantFromNCBIVR(Variant v) throws Exception {
		String alignment = "";
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
