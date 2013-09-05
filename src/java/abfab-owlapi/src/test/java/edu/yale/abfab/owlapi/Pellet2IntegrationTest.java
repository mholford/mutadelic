package edu.yale.abfab.owlapi;

import static org.junit.Assert.*;

import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.Test;

import edu.yale.abfab.IndividualPlus;
import edu.yale.abfab.Path;
import edu.yale.dlgen.controller.DLController;

public class Pellet2IntegrationTest {

	private OWLAPIAbductor abductor;
	private DLController dl;
	private final String NS = "http://krauthammerlab.med.yale.edu/test#";

	@Before
	public void setUp() throws Exception {
		abductor = new HermitAbductor("");
		abductor.setNamespace(NS);
		dl = abductor.getDLController();
	}

	@Test
	public void testSimpleCondition() {
		try {
			dl.load(new InputStreamReader(Pellet2AbductorTest.class
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
			dl.load(new InputStreamReader(Pellet2AbductorTest.class
					.getClassLoader().getResourceAsStream(
							"integration-abduct2.owl")), "Manchester");
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Mutation")));
			Path p = abductor
					.getBestPath(ip, dl.clazz(NS + "FinishedMutation"));
			assertEquals(String.format("[(%s%s & %s%s) -> %s%s]", NS, "SIFS", NS,
					"GENS", NS, "FINS"), p.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
