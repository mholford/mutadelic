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
import edu.yale.mutadelic.loaders.PhyloPMongoLoader;
import static edu.yale.mutadelic.loaders.SiftMongoLoader.*;
import edu.yale.mutadelic.mongo.MongoConnection;
import edu.yale.mutadelic.pipeline.model.Variant;
import static edu.yale.abfab.NS.*;
import static edu.yale.mutadelic.mongo.MongoConnection.*;

public class SiftService extends AbstractPipelineService {

	@Override
	public IndividualPlus exec(IndividualPlus input, Abductor abductor)
			throws AbfabServiceException {

		DLController dl = abductor.getDLController();
		Variant v = Variant.fromOWL(dl, input);
		Double result = getSiftScore(v);
		DLClass<?> siftScore = dl.clazz(SIFT_SCORE);
		if (valueFilled(dl, input.getIndividual(), siftScore)) {
			return input;
		}
		// String cacheValueProxy = "NULL";
		// if (result != null) {
		// cacheValueProxy = (result > 0.05) ? "HIGH" : "LOW";
		// }
		Double cacheValueProxy = Double.MAX_VALUE;
		if (result != null) {
			cacheValueProxy = (result > 0.05) ? 1d : 0d;
		}
		Set<DLAxiom<?>> annotation = annotatedResult(dl, input.getIndividual(),
				siftScore, dl.individual(MUTADELIC), result, cacheValueProxy);
		input.getAxioms().addAll(annotation);
		return input;
	}

	public Double getSiftScore(Variant v) {
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
				Map<String, String> obsScoreMap = new HashMap<>();
				String obs = sds[3];
				String scores = sds[10];
				String[] os = obs.split("\\|", -1);
				String[] scs = scores.split("\\|", -1);
				for (int i = 0; i < os.length; i++) {
					if (scs[i].length() > 0) {
						obsScoreMap.put(os[i], scs[i]);
					}
				}
				if (obsScoreMap.containsKey(v.getObserved())) {
					String score = obsScoreMap.get(v.getObserved());
					if (score.length() > 0) {
						return Double.parseDouble(score);
					}
				}
			}
		}
		return null;
	}

	public static int siftIndex(Variant v) {
		int pst = siftStart(v);
		return v.getStartPos() - pst;
	}

	public static int siftStart(Variant v) {
		int pos = v.getStartPos();
		int rs = RANGE_SIZE;
		int pst = (((pos - 1) / rs) * rs) + 1;
		return pst;
	}

	public static String siftKey(Variant v) {
		String chrNo = v.getChromosome();
		chrNo = chrNo.toUpperCase().startsWith("CHR") ? chrNo.substring(3)
				: chrNo;
		int ps = siftStart(v);
		return String.format("%s_%d", chrNo, ps);
	}

	@Override
	public double cost() {
		return 1.0;
	}

}
