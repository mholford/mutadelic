package edu.yale.mutadelic.pipeline.service;

import static edu.yale.mutadelic.mongo.MongoConnection.PHYLOP_VALUES;

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
import edu.yale.mutadelic.mongo.MongoConnection;
import edu.yale.mutadelic.pipeline.model.Variant;

public class PhylopService extends AbstractPipelineService {

	@Override
	public IndividualPlus exec(IndividualPlus input, Abductor abductor)
			throws AbfabServiceException {
//		double result = DefaultValues.PHYLOP;
		DLController dl = abductor.getDLController();
		Variant v = Variant.fromOWL(dl, input);
		Double result = getPhylopScore(v);
		DLClass<?> phylopScore = dl.clazz(PHYLOP_SCORE);
		if (valueFilled(dl, input.getIndividual(), phylopScore)) {
			return input;
		}
		
		Double cacheValueProxy = Double.MIN_VALUE;
		if (result != null) {
			cacheValueProxy = (result > 0.0) ? 10d : -10d;
		}
		Set<DLAxiom<?>> annotation = annotatedResult(dl, input.getIndividual(),
				phylopScore, dl.individual(MUTADELIC), result, cacheValueProxy);
		input.getAxioms().addAll(annotation);
		return input;
	}

	public Double getPhylopScore(Variant v) {
		DBCollection table = MongoConnection.instance().getPhylopTable();
		String key = phylopKey(v);

		DBObject q = new BasicDBObject();
		q.put("_id", key);
		DBObject r = table.findOne(q);

		if (r != null) {
			String vals = (String) r.get(PHYLOP_VALUES);
			String[] ss = vals.split(",");
			int idx = phylopIndex(v);
			String preOut = ss[idx];
			return preOut.length() > 0 ? Double.parseDouble(preOut) : null;
		}
		return null;
	}

	private int phylopIndex(Variant v) {
		int pst = phylopStart(v);
		return v.getStartPos() - pst;
	}

	private int phylopStart(Variant v) {
		int pos = v.getStartPos();
		int rs = PhyloPMongoLoader.RANGE_SIZE;
		int pst = (((pos - 1) / rs) * rs) + 1;
		return pst;
	}

	private String phylopKey(Variant v) {
		String chrNo = v.getChromosome().substring(3);
		int ps = phylopStart(v);
		return String.format("%s_%d", chrNo, ps);
	}

	@Override
	public double cost() {
		return 1.0;
	}

}
