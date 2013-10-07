package edu.yale.abfab.owlapi;

import static edu.yale.abfab.NS.*;
import static org.junit.Assert.*;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import edu.yale.abfab.IndividualPlus;
import edu.yale.abfab.Path;
import edu.yale.abfab.TestVals;
import edu.yale.abfab.Utils;
import edu.yale.abfab.mazes.MazeGenerator;
import edu.yale.abfab.mazes.MazeGenerator.Branch;
import edu.yale.abfab.mazes.MazeGenerator.Maze;
import edu.yale.abfab.mazes.MazeGenerator.Node;
import edu.yale.abfab.mazes.MazeTransformer;
import edu.yale.abfab.owlapi.HermitAbductor;
import edu.yale.abfab.owlapi.OWLAPIAbductor;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClassExpression;
import edu.yale.dlgen.DLDataPropertyExpression;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.DLLiteral;
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
	public void testSimpleStaging() {
		try {
			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("test1.owl")),
					"Manchester");
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Mutation")));
			Path p = abductor
					.getBestPath(ip, dl.clazz(NS + "FinishedMutation"));
			assertEquals(String.format("[%s%s -> %s%s]", NS, "GMS", NS, "FMS"),
					p.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testBranchedStaging() {
		try {
			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("test2.owl")),
					"Manchester");
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Mutation")));
			Path p = abductor
					.getBestPath(ip, dl.clazz(NS + "FinishedMutation"));

			System.out.println(p.toString());

			assertEquals(String.format("[([%s%s] & [%s%s]) -> %s%s]", NS,
					"GMS", NS, "SVS", NS, "FMS"), p.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testBranchedStaging2() {
		try {
			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("test4.owl")),
					"Manchester");
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"), dl.thing()));
			Path p = abductor
					.getBestPath(ip, dl.clazz(NS + "FinishedMutation"));

			System.out.println(p.toString());

			assertEquals(String.format("[%s%s -> ([%s%s] & [%s%s]) -> %s%s]",
					NS, "MS", NS, "GMS", NS, "SVS", NS, "FMS"), p.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test3WayBranchedStaging() {
		try {
			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("test3.owl")),
					"Manchester");
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Mutation")));
			Path p = abductor
					.getBestPath(ip, dl.clazz(NS + "FinishedMutation"));

			System.out.println(p.toString());

			boolean matches = p.toString().equals(
					String.format("[([%s%s] & [%s%s] & [%s%s]) -> %s%s]", NS,
							"GMS", NS, "PMS", NS, "SVS", NS, "FMS"));

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
								System.out
										.println("Oops; more than one sift value");
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
								System.out
										.println("Oops; more than one sift value");
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
	public void testPermute() {
		String[] s = new String[] { "1", "2", "3", "4", "5" };

		Set<Set<String>> setPermutations = Utils.getNTuplePermutations(
				Arrays.asList(s), 4);
		// for (Set<String> p : setPermutations) {
		// System.out.println(p);
		// }
		assertEquals(5, setPermutations.size());
	}

	@Test
	public void testMazeStaging() {
		try {
			dl.load(new InputStreamReader(OWLAPIAbductor.class.getClassLoader()
					.getResourceAsStream("skel.owl")), "Manchester");
			int numNodes = 20;
			List<String> mazeNodes = new ArrayList<>();
			int i = 0;
			while (i <= numNodes) {
				mazeNodes.add(String.format("%sT%S", NS, String.valueOf(++i)));
			}
			int randomOut = new Random().nextInt(numNodes) + 1;
			Object randomNode = mazeNodes.get(randomOut);

			MazeGenerator mg = new MazeGenerator();
			mg.setNodePool(mazeNodes);
			Maze m = mg.createDAG(mazeNodes, 0.0, 0.0, -1);
			MazeTransformer mt = new MazeTransformer();
			Set<DLAxiom<?>> ax = mt.transform(m);
			dl.addAxioms(ax);

			abductor.debug();

			DLIndividual<?> test = dl.individual(NS + "test");
			IndividualPlus ip = new IndividualPlus(test);
			DLClassExpression<?> ipType = dl.clazz(m.getRoot().getName()
					+ "Input");
			ip.getAxioms().add(dl.individualType(test, ipType));

			DLClassExpression<?> goalClass = dl.clazz(String
					.valueOf(randomNode) + "Output");
			Path p = abductor.getBestPath(ip, goalClass);

			String solution = mg.solveRandomDAG(m, String.valueOf(randomNode));

			assertEquals(solution, p.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testMazeStaging2() {
		try {
			dl.load(new InputStreamReader(OWLAPIAbductor.class.getClassLoader()
					.getResourceAsStream("skel.owl")), "Manchester");
			int numNodes = 10;
			List<String> mazeNodes = new ArrayList<>();
			int i = 0;
			while (i <= numNodes) {
				mazeNodes.add(String.format("%sT%S", NS, String.valueOf(++i)));
			}
			int randomOut = new Random().nextInt(numNodes) + 1;
			Object randomNode = mazeNodes.get(randomOut);

			MazeGenerator mg = new MazeGenerator();
			mg.setNodePool(mazeNodes);
			Maze m = mg.createDAG(mazeNodes, 0.2, 0.0, -1);
			MazeTransformer mt = new MazeTransformer();
			Set<DLAxiom<?>> ax = mt.transform(m);
			dl.addAxioms(ax);
			String mdump = m.dump();

			System.out.println(mdump);
			String solution = mg.solveRandomDAG(m, String.valueOf(randomNode));

			System.out.println("SOLUTION");
			System.out.println(solution);

			abductor.debug();
			System.out.println("ABFAB Solution");

			DLIndividual<?> test = dl.individual(NS + "test");
			IndividualPlus ip = new IndividualPlus(test);
			DLClassExpression<?> ipType = dl.clazz(m.getRoot().getName()
					+ "Input");
			ip.getAxioms().add(dl.individualType(test, ipType));

			DLClassExpression<?> goalClass = dl.clazz(String
					.valueOf(randomNode) + "Output");
			Path p = abductor.getBestPath(ip, goalClass);
			System.out.println(p);

			assertEquals(solution, p.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testMazeStaging3() {
		try {
			dl.load(new InputStreamReader(OWLAPIAbductor.class.getClassLoader()
					.getResourceAsStream("skel.owl")), "Manchester");
			MazeGenerator mg = new MazeGenerator();
			Node n1 = new Node("1");
			Maze m = new Maze(n1);
			Maze m1 = new Maze(new Node("1-1"));
			m1.addNode(new Node("2-1"));
			Maze m2 = new Maze(new Node("1-2"));
			m2.addNode(new Node("2-2"));
			m.addNode(new Branch("2", n1, Arrays.asList(new Maze[] { m1, m2 })));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testConditionalBranchingExec() {
		dl.load(new InputStreamReader(OWLAPIAbductor.class.getClassLoader()
				.getResourceAsStream("integration-abduct-exec6.owl")),
				"Manchester");

		IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
		ip.getAxioms().add(
				dl.individualType(dl.individual(NS + "test"),
						dl.clazz(NS + "Mutation")));
		Path p = abductor.getBestPath(ip, dl.clazz(NS + "FinishedMutation"));

		IndividualPlus output = abductor.exec(ip, p);

		String level;
		Collection<DLIndividual> descs;
		try {
			// Test results
			dl.addAxioms(output.getAxioms());
			level = null;
			descs = dl.getObjectPropertyValues(output.getIndividual(),
					dl.objectProp(SIO + "is_described_by"));
			for (DLIndividual<?> desc : descs) {
				Collection<DLIndividual> refs = dl.getObjectPropertyValues(
						desc, dl.objectProp(SIO + "refers_to"));
				for (DLIndividual<?> ref : refs) {
					if (dl.getTypes(ref).contains(dl.clazz(NS + "SiftValue"))) {
						Collection<DLLiteral> vals = dl.getDataPropertyValues(
								ref, dl.dataProp(NS + "has_level"));
						for (DLLiteral<?> val : vals) {
							if (level != null) {
								System.out.println("Oops; more than one level");
								fail();
							}
							level = dl.getLiteralValue(val);

						}
					}
				}
			}

			assertEquals("High", level);
		} finally {
			dl.removeAxioms(output.getAxioms());
		}
	}

	@Test
	public void testConditionalBranchingExec2() {
		dl.load(new InputStreamReader(OWLAPIAbductor.class.getClassLoader()
				.getResourceAsStream("integration-abduct-exec7.owl")),
				"Manchester");

		IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
		ip.getAxioms().add(
				dl.individualType(dl.individual(NS + "test"),
						dl.clazz(NS + "Mutation")));
		Path p = abductor.getBestPath(ip, dl.clazz(NS + "CompletedMutation"));

		IndividualPlus output = abductor.exec(ip, p);

		Collection<DLIndividual> descs;
		try {
			// Test results
			dl.addAxioms(output.getAxioms());
			Boolean finished = false;
			descs = dl.getObjectPropertyValues(output.getIndividual(),
					dl.objectProp(SIO + "is_described_by"));
			for (DLIndividual<?> desc : descs) {
				Collection<DLIndividual> refs = dl.getObjectPropertyValues(
						desc, dl.objectProp(SIO + "refers_to"));
				for (DLIndividual<?> ref : refs) {
					if (dl.getTypes(ref).contains(
							dl.clazz(NS + "CompletionStatus"))) {
						Collection<DLLiteral> vals = dl.getDataPropertyValues(
								ref, dl.dataProp(SIO + "has_value"));
						for (DLLiteral<?> val : vals) {
							finished = Boolean.valueOf(dl.getLiteralValue(val));
						}
					}
				}
			}

			assertEquals(true, finished);
		} finally {
			dl.removeAxioms(output.getAxioms());
		}

		TestVals.sift = 0.05;

		ip = new IndividualPlus(dl.individual(NS + "test"));
		ip.getAxioms().add(
				dl.individualType(dl.individual(NS + "test"),
						dl.clazz(NS + "Mutation")));
		p = abductor.getBestPath(ip, dl.clazz(NS + "CompletedMutation"));

		output = abductor.exec(ip, p);

		try {
			// Test results
			dl.addAxioms(output.getAxioms());
			Boolean finished = false;
			descs = dl.getObjectPropertyValues(output.getIndividual(),
					dl.objectProp(SIO + "is_described_by"));
			for (DLIndividual<?> desc : descs) {
				Collection<DLIndividual> refs = dl.getObjectPropertyValues(
						desc, dl.objectProp(SIO + "refers_to"));
				for (DLIndividual<?> ref : refs) {
					if (dl.getTypes(ref).contains(
							dl.clazz(NS + "CompletionStatus"))) {
						Collection<DLLiteral> vals = dl.getDataPropertyValues(
								ref, dl.dataProp(NS + "has_value"));
						for (DLLiteral<?> val : vals) {
							if (finished != null) {
								System.out.println("Oops; more than one level");
								fail();
							}
							finished = Boolean.valueOf(dl.getLiteralValue(val));

						}
					}
				}
			}

			assertEquals(false, finished);
		} finally {
			dl.removeAxioms(output.getAxioms());
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

			// Run again with uninteresting value
			TestVals.sift = 0.05;

			IndividualPlus ip2 = new IndividualPlus(dl.individual(NS + "test2"));
			ip2.getAxioms().add(
					dl.individualType(dl.individual(NS + "test2"),
							dl.clazz(NS + "Mutation")));
			Path p2 = abductor.getBestPath(ip2,
					dl.clazz(NS + "FinishedMutation"));

			IndividualPlus output2 = abductor.exec(ip2, p2);
			assertEquals(null, output2);

			TestVals.sift = 0.5;

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
