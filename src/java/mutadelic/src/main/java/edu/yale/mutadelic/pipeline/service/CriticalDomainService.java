package edu.yale.mutadelic.pipeline.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import static edu.yale.abfab.NS.*;
import static org.junit.Assert.fail;
import static edu.yale.mutadelic.mongo.MongoConnection.*;

public class CriticalDomainService extends AbstractPipelineService {

	class Result {
		String status;
		Set<String> domains;
	}

	@Override
	public IndividualPlus exec(IndividualPlus input, Abductor abductor)
			throws AbfabServiceException {

		DLController dl = abductor.getDLController();
		// Variant v = Variant.fromOWL(dl, input);
		Result result = getResult(input, dl);
		DLClass<?> inDomain = dl.clazz(DOMAIN_COLOCATION);
		DLClass<?> proteinDomain = dl.clazz(PROTEIN_DOMAIN);
		if (valueFilled(dl, input.getIndividual(), inDomain)) {
			return input;
		}
		Set<DLAxiom<?>> annotation = annotatedResult(dl, input.getIndividual(),
				inDomain, dl.individual(NS + "Mutadelic"), result.status);
		for (String domain : result.domains) {
			annotation.addAll(annotatedResult(dl, input.getIndividual(),
					proteinDomain, dl.individual(NS + "Mutadelic"), domain));
		}
		input.getAxioms().addAll(annotation);
		return input;
	}

	private Result getResult(IndividualPlus ip, DLController dl) {
		Result result = new Result();

		// Extract HGVSp from variant
		dl.addAxioms(ip.getAxioms());
		String hgvsp = null;

		Collection<DLIndividual> descs = dl.getObjectPropertyValues(
				ip.getIndividual(), dl.objectProp(SIO + "is_described_by"));
		for (DLIndividual<?> desc : descs) {
			Collection<DLIndividual> refs = dl.getObjectPropertyValues(desc,
					dl.objectProp(SIO + "refers_to"));
			for (DLIndividual<?> ref : refs) {
				if (dl.getTypes(ref).contains(dl.clazz(HGVSP_NOTATION))) {
					Collection<DLLiteral> vals = dl.getDataPropertyValues(ref,
							dl.dataProp(SIO + "has_value"));
					for (DLLiteral<?> val : vals) {
						if (hgvsp != null) {
							System.out.println("Oops; more than one result");
							fail();
						}
						hgvsp = dl.getLiteralValue(val);
					}
				}
			}
		}

		// Extract ENSP and position using regex
		if (hgvsp != null) {
			Pattern enspPattern = Pattern.compile("(.*):p.(\\d+).*");
			Matcher m = enspPattern.matcher(hgvsp);
			m.matches();
			String ensp = m.group(1);
			String posPre = m.group(2);
			int pos = Integer.parseInt(posPre);

			// Get the list of domains from Mongo (should be in order)
			DBCollection table = MongoConnection.instance().getPFAMTable();
			DBObject q = new BasicDBObject();
			q.put(MONGO_ID, ensp);
			DBObject r = table.findOne(q);
			if (r != null) {
				List<DBObject> domains = (List<DBObject>) r.get(PFAM_DOMAINS);

				// For each, if variant in range flag status as 'true' and add
				// domain to list
				Set<String> domainsFound = new HashSet<>();
				Iterator<DBObject> diter = domains.iterator();
				while (diter.hasNext()) {
					DBObject next = diter.next();
					int start = (int) next.get(PFAM_DOMAIN_START);
					if (start > pos) {
						break;
					}
					int end = (int) next.get(PFAM_DOMAIN_END);
					if (end >= pos) {
						String name = (String) next.get(PFAM_DOMAIN_NAME);
						domainsFound.add(name);
					}
				}
				result.status = (domainsFound.size() > 0) ? "true" : "false";
				result.domains = domainsFound;
			} else {
				result.status = "false";
				result.domains = new HashSet<>();
			}

		} else {
			result.status = "NA";
			result.domains = new HashSet<>();
		}
		return result;
	}

	@Override
	public double cost() {
		return 1.0;
	}

}
