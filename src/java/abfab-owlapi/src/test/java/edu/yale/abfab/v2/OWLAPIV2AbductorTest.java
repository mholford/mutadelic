package edu.yale.abfab.v2;

import static edu.yale.abfab.v2.NS.*;
import static org.junit.Assert.*;

import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.Test;

import edu.yale.abfab.owlapi.OWLAPIAbductorTest;
import edu.yale.abfab.v2.owlapi.HermitAbductor;
import edu.yale.abfab.v2.owlapi.OWLAPIAbductor;
import edu.yale.dlgen.controller.DLController;

public class OWLAPIV2AbductorTest {

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
			dl.load(new InputStreamReader(OWLAPIV2AbductorTest.class
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
}
