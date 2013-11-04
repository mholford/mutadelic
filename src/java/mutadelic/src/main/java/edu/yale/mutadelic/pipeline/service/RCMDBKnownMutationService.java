package edu.yale.mutadelic.pipeline.service;

import java.util.Collection;
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
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.DLLiteral;
import edu.yale.dlgen.controller.DLController;
import edu.yale.mutadelic.mongo.MongoConnection;
import edu.yale.mutadelic.pipeline.PipelineExecutor;
import static edu.yale.abfab.NS.*;
import static edu.yale.mutadelic.mongo.MongoConnection.*;
import static org.junit.Assert.fail;

public class RCMDBKnownMutationService extends AbstractPipelineService {

	@Override
	public IndividualPlus exec(IndividualPlus input, Abductor abductor)
			throws AbfabServiceException {
		DLController dl = abductor.getDLController();
		boolean result = getResult(dl, input);
		DLClass<?> databasePresence = dl.clazz(DATABASE_PRESENCE);
		if (valueFilled(dl, input.getIndividual(), databasePresence)) {
			return input;
		}
		Set<DLAxiom<?>> annotation = annotatedResult(dl, input.getIndividual(),
				databasePresence, dl.individual(MUTADELIC),
				String.valueOf(result));
		input.getAxioms().addAll(annotation);
		return input;
	}

	private boolean getResult(DLController dl, IndividualPlus input) {
		DBCollection table = MongoConnection.instance().getRCMDBTable();
		String alignment = null;
		dl.addAxioms(input.getAxioms());
		Collection<DLIndividual> descs = dl.getObjectPropertyValues(
				input.getIndividual(), dl.objectProp(SIO + "is_described_by"));
		for (DLIndividual<?> desc : descs) {
			Collection<DLIndividual> refs = dl.getObjectPropertyValues(desc,
					dl.objectProp(SIO + "refers_to"));
			for (DLIndividual<?> ref : refs) {
				if (dl.getTypes(ref).contains(dl.clazz(NS + "HGVSNotation"))) {
					Collection<DLLiteral> vals = dl.getDataPropertyValues(ref,
							dl.dataProp(SIO + "has_value"));
					for (DLLiteral<?> val : vals) {
						if (alignment != null) {
							System.out.println("Oops; more than one result");
							fail();
						}
						alignment = dl.getLiteralValue(val);
					}
				}
			}
		}
		dl.removeAxioms(input.getAxioms());
		DBObject q = new BasicDBObject();
		q.put(MONGO_ID, alignment);
		DBObject r = table.findOne(q);
		return r != null;
	}

	@Override
	public double cost() {
		return 1.0;
	}

}
