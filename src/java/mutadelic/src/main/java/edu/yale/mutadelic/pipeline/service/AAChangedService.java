package edu.yale.mutadelic.pipeline.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
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
import edu.yale.mutadelic.pipeline.model.Variant;
import static edu.yale.abfab.NS.*;
import static edu.yale.mutadelic.pipeline.service.SiftService.*;
import static edu.yale.mutadelic.mongo.MongoConnection.*;

public class AAChangedService extends AbstractPipelineService {

	class Result {
		String type;
		String hgvsp;
	}

	@Override
	public IndividualPlus exec(IndividualPlus input, Abductor abductor)
			throws AbfabServiceException {
		DLController dl = abductor.getDLController();
		Variant v = Variant.fromOWL(dl, input);
		Result result = getResult(v);

		DLClass<?> variationOutcome = dl.clazz(VARIATION_OUTCOME);
		if (valueFilled(dl, input.getIndividual(), variationOutcome)) {
			return input;
		}
		Set<DLAxiom<?>> annotation = annotatedResult(dl, input.getIndividual(),
				dl.clazz(VARIATION_OUTCOME), dl.individual(MUTADELIC),
				result.type);
		annotation.addAll(annotatedResult(dl, input.getIndividual(),
				dl.clazz(HGVSP_NOTATION), dl.individual(MUTADELIC),
				result.hgvsp, true));
		input.getAxioms().addAll(annotation);
		return input;
	}

	private Result getResult(Variant v) {
		Result output = new Result();
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
			if (sds.length > 1) {
				Map<String, String> obsAAChgMap = new HashMap<>();
				String obs = sds[3];
				String ensp = sds[8];
				String pposPre = sds[9];
				String aa = sds[6];
				String aaChgs = sds[7];
				String[] os = obs.split("\\|", -1);
				String[] scs = aaChgs.split("\\|", -1);
				for (int i = 0; i < os.length; i++) {
					obsAAChgMap.put(os[i], scs[i]);
				}
				String aaChg;
				if (obsAAChgMap.containsKey(v.getObserved())) {
					aaChg = obsAAChgMap.get(v.getObserved());
					if (aaChg.equals(aa)) {
						output.type = SYNONYMOUS;
					} else if (aaChg.equals("*")) {
						// output.output = STOP_GAINED;
						output.type = NON_SYNONYMOUS;
					} else {
						output.type = NON_SYNONYMOUS;
					}
					
				} else {
					// Not deciding the AA change, but want to assign HGVSP so
					// the protein-relative position can be used by the domain
					// colocation service
					aaChg = "??";
					output.type = "NA";
				}
				output.hgvsp = String.format("%s:p.%d%s>%s", ensp,
						Integer.parseInt(pposPre), aa, aaChg);
			}
		}

		return output;
	}

	@Override
	public double cost() {
		return 1.0;
	}

}
