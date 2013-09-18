package edu.yale.abfab.v1.owlapi;

import static org.junit.Assert.*;

import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import edu.yale.abfab.old.IndividualPlus;
import edu.yale.abfab.old.Path;
import edu.yale.abfab.owlapi.HermitAbductor;
import edu.yale.abfab.owlapi.OWLAPIAbductor;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.controller.DLController;

public class OWLAPIAbductorTest {

	private OWLAPIAbductor abductor;
	private DLController dl;
	private final String NS = "http://krauthammerlab.med.yale.edu/test#";

	@Before
	public void setUp() throws Exception {
		abductor = new HermitAbductor("");
		abductor.setNamespace(NS);
		dl = abductor.getDLController();
		dl.load(new InputStreamReader(OWLAPIAbductorTest.class
				.getClassLoader().getResourceAsStream("test-abduct.manchester")),
				"Manchester");
	}

	@Test
	public void testGetTerminals() {
		try {
			Collection<DLIndividual> terminals = abductor.getTerminals(dl
					.clazz(NS + "C"));
			assertEquals(1, terminals.size());
			DLIndividual<?> ind = (DLIndividual<?>) terminals.iterator().next();
			assertEquals(NS + "Service2", dl.getIRI(ind));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testMatchesInput() {
		try {
			Set<DLAxiom<?>> ax = new HashSet<>();
			ax.add(dl.individualType(dl.individual(NS + "test"),
					dl.clazz(NS + "B")));
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"),
					ax);

			boolean m = abductor.matchesInput(ip,
					new IndividualPlus(dl.individual(NS + "BSI")));
			assertEquals(true, m);

			m = abductor.matchesInput(ip,
					new IndividualPlus(dl.individual(NS + "ASI")));
			assertEquals(false, m);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testMatchesOutput() {
		try {
			Set<DLAxiom<?>> ax = new HashSet<>();
			ax.add(dl.individualType(dl.individual(NS + "test"),
					dl.clazz(NS + "B")));
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"),
					ax);

			boolean m = abductor.matchesOutput(ip,
					new IndividualPlus(dl.individual(NS + "BSO")));
			assertEquals(true, m);

			m = abductor.matchesOutput(ip,
					new IndividualPlus(dl.individual(NS + "CSO")));
			assertEquals(false, m);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testExtendPath() {
		try {
			Path p = new Path(new IndividualPlus(dl.individual(NS + "test")),
					abductor);
			p.add(dl.individual(NS + "Service2"));

			Collection<Path> eps = abductor.extendPath(p);

			assertEquals(1, eps.size());
			Path ep = eps.iterator().next();
			assertEquals(String.format("[%s%s -> %s%s]", NS, "Service1", NS,
					"Service2"), ep.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testBestPathToServices() {
		try {
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "A")));
			Collection<DLIndividual> services = new HashSet<>();
			services.add(dl.individual(NS + "Service2"));

			Path p = abductor.getBestPathToServices(ip, services);

			assertEquals(String.format("[%s%s -> %s%s]", NS, "Service1", NS,
					"Service2"), p.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testBestPath() {
		try {
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "A")));

			Path p = abductor.getBestPath(ip, dl.clazz(NS + "C"));

			assertEquals(String.format("[%s%s -> %s%s]", NS, "Service1", NS,
					"Service2"), p.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testChooseBestPath() {
		try {
			Path p1 = new Path(new IndividualPlus(dl.individual(NS + "test")),
					abductor);
			Path p2 = new Path(new IndividualPlus(dl.individual(NS + "test")),
					abductor);
			p1.add(dl.individual(NS + "Service1"));
			p2.add(dl.individual(NS + "Service2"));
			Set<Path> paths = new HashSet<>();
			paths.add(p1);
			paths.add(p2);

			Path best = abductor.chooseBestPath(paths);
			assertEquals(best, p1);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
