package edu.yale.abfab.owlapi;

import static edu.yale.abfab.NS.*;
import static org.junit.Assert.*;

import java.io.InputStreamReader;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import edu.yale.abfab.IndividualPlus;
import edu.yale.abfab.Path;
import edu.yale.abfab.owlapi.HermitAbductor;
import edu.yale.abfab.owlapi.OWLAPIAbductor;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.controller.DLController;

public class OWLAPIAbductorTest {

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
							"test1.owl")), "Manchester");
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Mutation")));
			Path p = abductor
					.getBestPath(ip, dl.clazz(NS + "FinishedMutation"));
			assertEquals(
					String.format("[%s%s -> %s%s]", NS, "GMS", NS, "FMS"),
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
							"test2.owl")), "Manchester");
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Mutation")));
			Path p = abductor
					.getBestPath(ip, dl.clazz(NS + "FinishedMutation"));

			System.out.println(p.toString());

			boolean matches = p.toString().equals(
					String.format("[([%s%s] & [%s%s]) -> %s%s]", NS, "SVS",
							NS, "GMS", NS, "FMS"))
					|| p.toString().equals(
							String.format("[([%s%s] & [%s%s]) -> %s%s]", NS,
									"GMS", NS, "SVS", NS, "FMS"));
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
}
