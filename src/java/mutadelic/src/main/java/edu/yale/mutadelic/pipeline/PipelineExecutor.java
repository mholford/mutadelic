package edu.yale.mutadelic.pipeline;

import java.io.Reader;
import java.util.Collection;

import edu.yale.abfab.Abductor;
import edu.yale.abfab.IndividualPlus;
import edu.yale.abfab.Path;
import edu.yale.abfab.owlapi.HermitAbductor;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.DLLiteral;
import edu.yale.dlgen.controller.DLController;
import edu.yale.mutadelic.pipeline.model.Variant;
import static edu.yale.abfab.NS.*;
import static org.junit.Assert.fail;

public class PipelineExecutor {

	private Abductor abductor;
	private DLController dl;
	private Reader stagingDoc;
	private String stagingDocFormat;
	private Reader execDoc;
	private String execDocFormat;

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

//		dl.load(new InputStreamReader(PipelineExecutor.class.getClassLoader()
//				.getResourceAsStream("pipeline-stage.owl")), "Manchester");
		dl.load(stagingDoc, stagingDocFormat);
		Path p = abductor.getBestPath(ip, dl.clazz(NS + "FinishedVariant"));
//		dl.load(new InputStreamReader(PipelineExecutor.class.getClassLoader()
//				.getResourceAsStream("pipeline.owl")), "Manchester");
		dl.load(execDoc, execDocFormat);
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

	public Reader getStagingDoc() {
		return stagingDoc;
	}

	public void setStagingDoc(Reader stagingDoc) {
		this.stagingDoc = stagingDoc;
	}

	public Reader getExecDoc() {
		return execDoc;
	}

	public void setExecDoc(Reader execDoc) {
		this.execDoc = execDoc;
	}

	public String getStagingDocFormat() {
		return stagingDocFormat;
	}

	public void setStagingDocFormat(String stagingDocFormat) {
		this.stagingDocFormat = stagingDocFormat;
	}

	public String getExecDocFormat() {
		return execDocFormat;
	}

	public void setExecDocFormat(String execDocFormat) {
		this.execDocFormat = execDocFormat;
	}
}
