package edu.yale.mutadelic.pipeline;

import java.io.InputStreamReader;
import java.util.Collection;

import edu.yale.abfab.Abductor;
import edu.yale.abfab.IndividualPlus;
import edu.yale.abfab.Path;
import edu.yale.abfab.owlapi.HermitAbductor;
import edu.yale.abfab.owlapi.OWLAPIAbductorTest;
import edu.yale.dlgen.DLClass;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.DLLiteral;
import edu.yale.dlgen.controller.DLController;
import edu.yale.mutadelic.pipeline.model.Variant;
import static edu.yale.abfab.NS.*;
import static org.junit.Assert.fail;

public class PipelineExecutor {

	private Abductor abductor;
	private DLController dl;

	public PipelineExecutor() {
		init();
	}

	public void init() {
		abductor = new HermitAbductor("");
		abductor.setNamespace(NS);
		dl = abductor.getDLController();
	}

	public IndividualPlus execute(Variant v) {
		IndividualPlus ip = v.toOWL(dl);

		dl.load(new InputStreamReader(PipelineExecutor.class.getClassLoader()
				.getResourceAsStream("pipeline-stage.owl")), "Manchester");
		Path p = abductor.getBestPath(ip, dl.clazz(NS + "FinishedVariant"));
		dl.load(new InputStreamReader(PipelineExecutor.class.getClassLoader()
				.getResourceAsStream("pipeline.owl")), "Manchester");
		IndividualPlus output = abductor.exec(ip, p);
		return output;
	}

	public String getResult(IndividualPlus ip, String outputTypeName) {
		dl.addAxioms(ip.getAxioms());
		String output = null;
		Collection<DLIndividual> descs = dl.getObjectPropertyValues(
				ip.getIndividual(), dl.objectProp(SIO + "is_described_by"));
		for (DLIndividual<?> desc : descs) {
			Collection<DLIndividual> refs = dl.getObjectPropertyValues(desc,
					dl.objectProp(SIO + "refers_to"));
			for (DLIndividual<?> ref : refs) {
				if (dl.getTypes(ref).contains(dl.clazz(outputTypeName))) {
					if (output != null) {
						System.out.println("Oops; more than one result");
						fail();
					}
					output = dl.getIRI(ref);
				}
			}
		}
		return output;
	}

	public String getLiteralResult(IndividualPlus ip, String outputTypeName) {
		dl.addAxioms(ip.getAxioms());
		String output = null;
		//abductor.debug();
		Collection<DLIndividual> descs = dl.getObjectPropertyValues(
				ip.getIndividual(), dl.objectProp(SIO + "is_described_by"));
		for (DLIndividual<?> desc : descs) {
			Collection<DLIndividual> refs = dl.getObjectPropertyValues(desc,
					dl.objectProp(SIO + "refers_to"));
			for (DLIndividual<?> ref : refs) {
				if (dl.getTypes(ref).contains(dl.clazz(outputTypeName))) {
					Collection<DLLiteral> vals = dl.getDataPropertyValues(ref,
							dl.dataProp(SIO + "has_value"));
					for (DLLiteral<?> val : vals) {
						if (output != null) {
							System.out.println("Oops; more than one result");
							fail();
						}
						output = dl.getLiteralValue(val);
					}
				}
			}
		}
		return output;
	}
}
