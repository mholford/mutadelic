package edu.yale.abfab.v1.owlapi;

import static org.junit.Assert.*;
import static edu.yale.abfab.old.NS.*;

import java.io.InputStreamReader;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import edu.yale.abfab.TestVals;
import edu.yale.abfab.old.IndividualPlus;
import edu.yale.abfab.old.Path;
import edu.yale.abfab.owlapi.HermitAbductor;
import edu.yale.abfab.owlapi.OWLAPIAbductor;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.DLLiteral;
import edu.yale.dlgen.controller.DLController;

public class OWLAPIIntegrationTest {

	private OWLAPIAbductor abductor;
	private DLController dl;

	@Before
	public void setUp() throws Exception {
		abductor = new HermitAbductor("");
		abductor.setNamespace(NS);
		dl = abductor.getDLController();
	}

	@Test
	public void testSimpleCondition() {
		try {
			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream(
							"integration-abduct.owl")), "Manchester");
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Mutation")));
			Path p = abductor
					.getBestPath(ip, dl.clazz(NS + "FinishedMutation"));
			assertEquals(
					String.format("[%s%s -> %s%s]", NS, "GENS", NS, "FINS"),
					p.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testBranchingCondition() {
		try {
			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream(
							"integration-abduct2.owl")), "Manchester");
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Mutation")));
			Path p = abductor
					.getBestPath(ip, dl.clazz(NS + "FinishedMutation"));

			System.out.println(p.toString());

			boolean matches = p.toString().equals(
					String.format("[([%s%s] & [%s%s]) -> %s%s]", NS, "SIFS",
							NS, "GENS", NS, "FINS"))
					|| p.toString().equals(
							String.format("[([%s%s] & [%s%s]) -> %s%s]", NS,
									"GENS", NS, "SIFS", NS, "FINS"));
			assertEquals(true, matches);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testSimpleExec() {
		try {
			dl.load(new InputStreamReader(OWLAPIAbductor.class.getClassLoader()
					.getResourceAsStream("integration-abduct-exec.owl")),
					"Manchester");

			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Mutation")));
			Path p = abductor
					.getBestPath(ip, dl.clazz(NS + "FinishedMutation"));

			IndividualPlus output = abductor.exec(ip, p);

			// Test results
			dl.addAxioms(output.getAxioms());
			String gene = null;
			Collection<DLIndividual> descs = dl.getObjectPropertyValues(
					output.getIndividual(),
					dl.objectProp(SIO + "is_described_by"));
			for (DLIndividual<?> desc : descs) {
				Collection<DLIndividual> refs = dl.getObjectPropertyValues(
						desc, dl.objectProp(SIO + "refers_to"));
				for (DLIndividual<?> ref : refs) {
					if (dl.getTypes(ref).contains(dl.clazz(SO + "Gene"))) {
						if (gene != null) {
							System.out.println("Oops; more than one gene");
							fail();
						}
						gene = dl.getIRI(ref);
					}
				}
			}

			assertEquals(NS + "Gene123", gene);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testSimpleExecDP() {
		try {
			dl.load(new InputStreamReader(OWLAPIAbductor.class.getClassLoader()
					.getResourceAsStream("integration-abduct-exec2.owl")),
					"Manchester");

			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Mutation")));
			Path p = abductor
					.getBestPath(ip, dl.clazz(NS + "FinishedMutation"));

			IndividualPlus output = abductor.exec(ip, p);

			// Test results
			dl.addAxioms(output.getAxioms());
			Double sift = null;
			Collection<DLIndividual> descs = dl.getObjectPropertyValues(
					output.getIndividual(),
					dl.objectProp(SIO + "is_described_by"));
			for (DLIndividual<?> desc : descs) {
				Collection<DLIndividual> refs = dl.getObjectPropertyValues(
						desc, dl.objectProp(SIO + "refers_to"));
				for (DLIndividual<?> ref : refs) {
					if (dl.getTypes(ref).contains(dl.clazz(NS + "SiftValue"))) {
						Collection<DLLiteral> vals = dl.getDataPropertyValues(
								ref, dl.dataProp(SIO + "has_value"));
						for (DLLiteral<?> val : vals) {
							if (sift != null) {
								System.out.println("Oops; more than one sift value");
								fail();
							}
							String preSift = dl.getLiteralValue(val);
							sift = Double.parseDouble(preSift);
						}
					}
				}
			}
			dl.removeAxioms(output.getAxioms());

			assertEquals(new Double(0.5), sift);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testSimpleExecFDP() {
		try {
			dl.load(new InputStreamReader(OWLAPIAbductor.class.getClassLoader()
					.getResourceAsStream("integration-abduct-exec4.owl")),
					"Manchester");

			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Mutation")));
			Path p = abductor
					.getBestPath(ip, dl.clazz(NS + "FinishedMutation"));

			IndividualPlus output = abductor.exec(ip, p);

			// Test results
			dl.addAxioms(output.getAxioms());
			Double sift = null;
			Collection<DLIndividual> descs = dl.getObjectPropertyValues(
					output.getIndividual(),
					dl.objectProp(SIO + "is_described_by"));
			for (DLIndividual<?> desc : descs) {
				Collection<DLIndividual> refs = dl.getObjectPropertyValues(
						desc, dl.objectProp(SIO + "refers_to"));
				for (DLIndividual<?> ref : refs) {
					if (dl.getTypes(ref).contains(dl.clazz(NS + "SiftValue"))) {
						Collection<DLLiteral> vals = dl.getDataPropertyValues(
								ref, dl.dataProp(SIO + "has_value"));
						for (DLLiteral<?> val : vals) {
							if (sift != null) {
								System.out.println("Oops; more than one sift value");
								fail();
							}
							String preSift = dl.getLiteralValue(val);
							sift = Double.parseDouble(preSift);
						}
					}
				}
			}
			dl.removeAxioms(output.getAxioms());

			assertEquals(new Double(0.5), sift);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testConditionalExec() {
		try {
			dl.load(new InputStreamReader(OWLAPIAbductor.class.getClassLoader()
					.getResourceAsStream("integration-abduct-exec5.owl")),
					"Manchester");

			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Mutation")));
			Path p = abductor.getBestPath(ip,
					dl.clazz(NS + "FinishedMutation"));

			IndividualPlus output = abductor.exec(ip, p);

			// Test results
			dl.addAxioms(output.getAxioms());
			Double sift = null;
			Collection<DLIndividual> descs = dl.getObjectPropertyValues(
					output.getIndividual(),
					dl.objectProp(SIO + "is_described_by"));
			for (DLIndividual<?> desc : descs) {
				Collection<DLIndividual> refs = dl.getObjectPropertyValues(
						desc, dl.objectProp(SIO + "refers_to"));
				for (DLIndividual<?> ref : refs) {
					if (dl.getTypes(ref).contains(dl.clazz(NS + "SiftValue"))) {
						Collection<DLLiteral> vals = dl.getDataPropertyValues(
								ref, dl.dataProp(SIO + "has_value"));
						for (DLLiteral<?> val : vals) {
							if (sift != null) {
								System.out.println("Oops; more than one gene");
								fail();
							}
							String preSift = dl.getLiteralValue(val);
							sift = Double.parseDouble(preSift);
						}
					}
				}
			}

			assertEquals(new Double(0.5), sift);

			String gene = null;
			descs = dl.getObjectPropertyValues(output.getIndividual(),
					dl.objectProp(SIO + "is_described_by"));
			for (DLIndividual<?> desc : descs) {
				Collection<DLIndividual> refs = dl.getObjectPropertyValues(
						desc, dl.objectProp(SIO + "refers_to"));
				for (DLIndividual<?> ref : refs) {
					if (dl.getTypes(ref).contains(dl.clazz(SO + "Gene"))) {
						if (gene != null) {
							System.out.println("Oops; more than one gene");
							fail();
						}
						gene = dl.getIRI(ref);
					}
				}
			}
			dl.removeAxioms(output.getAxioms());
			assertEquals(NS + "Gene123", gene);
			
			
			//Run again with uninteresting value
			TestVals.sift = 0.05;
			
			IndividualPlus ip2 = new IndividualPlus(dl.individual(NS + "test2"));
			ip2.getAxioms().add(
					dl.individualType(dl.individual(NS + "test2"),
							dl.clazz(NS + "Mutation")));
			Path p2 = abductor.getBestPath(ip2,
					dl.clazz(NS + "FinishedMutation"));

			IndividualPlus output2 = abductor.exec(ip2, p2);
			assertEquals(null, output2);

			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testBranchingExec() {
		try {
			dl.load(new InputStreamReader(OWLAPIAbductor.class.getClassLoader()
					.getResourceAsStream("integration-abduct-exec3.owl")),
					"Manchester");

			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Mutation")));
			Path p = abductor
					.getBestPath(ip, dl.clazz(NS + "FinishedMutation"));

			IndividualPlus output = abductor.exec(ip, p);

			// Test results
			dl.addAxioms(output.getAxioms());
			Double sift = null;
			Collection<DLIndividual> descs = dl.getObjectPropertyValues(
					output.getIndividual(),
					dl.objectProp(SIO + "is_described_by"));
			for (DLIndividual<?> desc : descs) {
				Collection<DLIndividual> refs = dl.getObjectPropertyValues(
						desc, dl.objectProp(SIO + "refers_to"));
				for (DLIndividual<?> ref : refs) {
					if (dl.getTypes(ref).contains(dl.clazz(NS + "SiftValue"))) {
						Collection<DLLiteral> vals = dl.getDataPropertyValues(
								ref, dl.dataProp(SIO + "has_value"));
						for (DLLiteral<?> val : vals) {
							if (sift != null) {
								System.out.println("Oops; more than one gene");
								fail();
							}
							String preSift = dl.getLiteralValue(val);
							sift = Double.parseDouble(preSift);
						}
					}
				}
			}

			assertEquals(new Double(0.5), sift);

			String gene = null;
			descs = dl.getObjectPropertyValues(output.getIndividual(),
					dl.objectProp(SIO + "is_described_by"));
			for (DLIndividual<?> desc : descs) {
				Collection<DLIndividual> refs = dl.getObjectPropertyValues(
						desc, dl.objectProp(SIO + "refers_to"));
				for (DLIndividual<?> ref : refs) {
					if (dl.getTypes(ref).contains(dl.clazz(SO + "Gene"))) {
						if (gene != null) {
							System.out.println("Oops; more than one gene");
							fail();
						}
						gene = dl.getIRI(ref);
					}
				}
			}

			dl.removeAxioms(output.getAxioms());
			assertEquals(NS + "Gene123", gene);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
