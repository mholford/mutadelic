package edu.yale.mutadelic.pipeline.service;

import java.util.Random;
import java.util.Set;

import com.mongodb.BasicDBList;
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
import static edu.yale.mutadelic.mongo.MongoConnection.*;

public class AlleleFrequencyService extends AbstractPipelineService {

	@Override
	public IndividualPlus exec(IndividualPlus input, Abductor abductor)
			throws AbfabServiceException {
		DLController dl = abductor.getDLController();
		DLClass<?> frequency = dl.clazz(ALLELE_FREQUENCY);
		if (valueFilled(dl, input.getIndividual(), frequency)) {
			return input;
		}
		Variant v = Variant.fromOWL(dl, input);
		double result = findMAFForVariant(v);
		Double cacheProxy = (result <= 0.01) ? 0.0 : 0.99;

		Set<DLAxiom<?>> annotation = annotatedResult(dl, input.getIndividual(),
				frequency, dl.individual(MUTADELIC), result, cacheProxy);
		input.getAxioms().addAll(annotation);
		return input;
	}

	private double findMAFForVariant(Variant v) {
		String vkey = variantOneKKey(v);
		DBObject q = new BasicDBObject();
		q.put("_id", vkey);
		DBCollection onek = MongoConnection.instance().getOneKTable();
		DBObject r = onek.findOne(q);
		double out = highestMAF(r);
		return out;
	}

	private String variantOneKKey(Variant v) {
		String chr = v.getChromosome().substring(3);
		String ref = v.getReference();
		if (ref.length() > 5) {
			ref = ref.substring(0, 5);
		}
		String obs = v.getObserved();
		if (obs.length() > 5) {
			obs = obs.substring(0, 5);
		}
		return String.format("%s_%d_%s_%s", chr, v.getStartPos(), ref, obs);
	}

	private double highestMAF(DBObject r) {
		if (r != null) {
			double maf = Double.parseDouble((String) r.get(ONE_K_MAF));
			double afr = Double.parseDouble((String) r.get(ONE_K_AFR_MAF));
			double amr = Double.parseDouble((String) r.get(ONE_K_AMR_MAF));
			double asn = Double.parseDouble((String) r.get(ONE_K_ASN_MAF));
			double eur = Double.parseDouble((String) r.get(ONE_K_EUR_MAF));

			double max = Math.max(maf,
					Math.max(afr, Math.max(amr, Math.max(asn, eur))));
			return max;
		}

		return 0.0d;
	}

	@Override
	public double cost() {
		return 1.0;
	}

}
