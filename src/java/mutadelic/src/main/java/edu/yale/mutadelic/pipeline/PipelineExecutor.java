package edu.yale.mutadelic.pipeline;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.yale.abfab.Abductor;
import edu.yale.abfab.IndividualPlus;
//import edu.yale.abfab.OldPath;
import edu.yale.abfab.Path;
import edu.yale.abfab.owlapi.HermitAbductor;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.DLLiteral;
import edu.yale.dlgen.controller.DLController;
import edu.yale.mutadelic.pipeline.model.Variant;
import static edu.yale.abfab.NS.*;
import static org.junit.Assert.fail;

public class PipelineExecutor {

	public class PipelineResult {
		String result;
		Set<DLAxiom<?>> axioms;

		public PipelineResult() {

		}

		public PipelineResult(String result, Set<DLAxiom<?>> axioms) {
			this.result = result;
			this.axioms = axioms;
		}

		public String getResult() {
			return result;
		}

		public void setResult(String result) {
			this.result = result;
		}

		public Set<DLAxiom<?>> getAxioms() {
			return axioms;
		}

		public void setAxioms(Set<DLAxiom<?>> axioms) {
			this.axioms = axioms;
		}
	}

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

		// dl.load(new InputStreamReader(PipelineExecutor.class.getClassLoader()
		// .getResourceAsStream("pipeline-stage.owl")), "Manchester");
		try {
			stagingDoc.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
		dl.load(stagingDoc, stagingDocFormat);
		Path p = abductor.getBestPath(ip, dl.clazz(NS + "FinishedVariant"));
		// dl.load(new InputStreamReader(PipelineExecutor.class.getClassLoader()
		// .getResourceAsStream("pipeline.owl")), "Manchester");
		try {
			execDoc.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
		dl.load(execDoc, execDocFormat);
		IndividualPlus output = abductor.exec(ip, p);
		return output;
	}

	public PipelineResult getResult(IndividualPlus ip, String outputTypeName) {
		Set<DLAxiom<?>> resultAxioms = new HashSet<>();
		dl.addAxioms(ip.getAxioms());
		PipelineResult output = null;
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
					Collection<DLIndividual> cits = dl.getObjectPropertyValues(
							desc, dl.objectProp(SIO + "cites"));
					for (DLIndividual<?> cit : cits) {
						resultAxioms.add(dl.newObjectFact(desc,
								dl.objectProp(SIO + "cites"), cit));
					}
					resultAxioms.add(dl.newObjectFact(ip.getIndividual(),
							dl.objectProp(SIO + "is_described_by"), desc));

					resultAxioms.add(dl.individualType(desc,
							dl.clazz(SIO + "Description")));
					resultAxioms.add(dl.newObjectFact(desc,
							dl.objectProp(SIO + "refers_to"), ref));
					resultAxioms.add(dl.individualType(ref,
							dl.clazz(outputTypeName)));

					output = new PipelineResult();
					output.setResult(dl.getIRI(ref));
					output.setAxioms(resultAxioms);
				}

			}
		}
		return output;
	}

	public PipelineResult getLiteralResult(IndividualPlus ip,
			String outputTypeName) {
		Set<DLAxiom<?>> resultAxioms = new HashSet<>();
		dl.addAxioms(ip.getAxioms());
		PipelineResult output = null;
		// abductor.debug();
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
						Collection<DLIndividual> cits = dl
								.getObjectPropertyValues(desc,
										dl.objectProp(SIO + "cites"));
						for (DLIndividual<?> cit : cits) {
							resultAxioms.add(dl.newObjectFact(desc,
									dl.objectProp(SIO + "cites"), cit));
						}
						resultAxioms.add(dl.newObjectFact(ip.getIndividual(),
								dl.objectProp(SIO + "is_described_by"), desc));
						resultAxioms.add(dl.newObjectFact(desc,
								dl.objectProp(SIO + "refers_to"), ref));
						resultAxioms.add(dl.individualType(ref,
								dl.clazz(outputTypeName)));
						resultAxioms.add(dl.newDataFact(ref,
								dl.dataProp(SIO + "has_value"), val));

						output = new PipelineResult();
						output.setResult(dl.getLiteralValue(val));
						output.setAxioms(resultAxioms);
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
