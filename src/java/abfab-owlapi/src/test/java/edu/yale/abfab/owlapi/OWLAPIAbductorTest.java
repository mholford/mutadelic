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
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import edu.yale.abfab.Abductor;
import edu.yale.abfab.IndividualPlus;
import edu.yale.abfab.Path;
import edu.yale.abfab.Path2;
import edu.yale.abfab.TestVals;
import edu.yale.abfab.Utils;
import edu.yale.abfab.mazes.MazeGenerator;
import edu.yale.abfab.mazes.MazeGenerator.Branch;
import edu.yale.abfab.mazes.MazeGenerator.Maze;
import edu.yale.abfab.mazes.MazeGenerator.Node;
import edu.yale.abfab.mazes.MazeGenerator2;

//import edu.yale.abfab.mazes.MazeGenerator2.Branch;
//import edu.yale.abfab.mazes.MazeGenerator2.Maze;
//import edu.yale.abfab.mazes.MazeGenerator2.Node;
import edu.yale.abfab.mazes.MazeTransformer;
import edu.yale.abfab.mazes.MazeTransformer2;
import edu.yale.abfab.owlapi.HermitAbductor;
import edu.yale.abfab.owlapi.OWLAPIAbductor;
import edu.yale.abfab.pipeline.TestValues;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClass;
import edu.yale.dlgen.DLClassExpression;
import edu.yale.dlgen.DLDataPropertyExpression;
import edu.yale.dlgen.DLEntity;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.DLLiteral;
import edu.yale.dlgen.controller.DLController;
import edu.yale.dlgen.controller.OWLAPIDLController;

public class OWLAPIAbductorTest {

	private static OWLAPIAbductor abductor;
	private DLController dl;

	@BeforeClass
	public static void beforeClass() {
		abductor = new HermitAbductor("");
		abductor.setNamespace(NS);
	}

	@Before
	public void setUp() throws Exception {
		dl = abductor.getDLController();
		TestValues.revert();
	}

	@Test
	public void testTBoxFindTerminal() {
		try {
			abductor.clearCaches();
			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream(
							"pipeline-tbox-unexpanded.owl")), "Manchester");

			Collection<DLClassExpression<?>> ts = abductor.findTerminals2(dl
					.clazz(NS + "FinishedVariant"));
			assertEquals(dl.clazz(NS + "FinishedVariantService"), ts.iterator()
					.next());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testTBoxSubsumption() {
		try {
			abductor.clearCaches();
			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream(
							"pipeline-tbox-unexpanded.owl")), "Manchester");

			OWLClassExpression oce = (OWLClassExpression) dl.clazz(
					NS + "FinishedVariantService").get();
			OWLOntology ont = ((OWLAPIDLController) dl).getOntology();
			Set<OWLEquivalentClassesAxiom> equivalentClassesAxioms = ont
					.getEquivalentClassesAxioms((OWLClass) oce);

			boolean b = dl.checkEntailed(dl.subClass(
					dl.andClass(dl.clazz(NS + "RareVariant"),
							dl.clazz(NS + "ConservedVariant")),
					dl.clazz(NS + "InterestingVariant")));
			assertEquals(true, b);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testTBoxSimpleStaging() {
		System.out.println("TEST SIMPLE STAGING");
		try {
			abductor.clearCaches();
			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("test1a.owl")),
					"Manchester");
			abductor.debug();
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Mutation")));
			Path2 p = abductor.getBestPath2(ip,
					dl.clazz(NS + "FinishedMutation"));
			assertEquals(String.format("[%s%s -> %s%s]", NS,
					"GeneAnnotatedMutationService", NS,
					"FinishedMutationService"), p.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testSimpleStaging() {
		System.out.println("TEST SIMPLE STAGING");
		try {
			abductor.clearCaches();
			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("test1.owl")),
					"Manchester");
			abductor.debug();
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
	public void testTBoxBranchedStaging() {
		System.out.println("TEST BRANCHED STAGING");
		try {
			abductor.clearCaches();
			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("test2a.owl")),
					"Manchester");
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Mutation")));
			Path2 p = abductor.getBestPath2(ip,
					dl.clazz(NS + "FinishedMutation"));

			System.out.println(p.toString());

			assertEquals(String.format("[([%s%s] & [%s%s]) -> %s%s]", NS,
					"GeneAnnotatedMutationService", NS,
					"SiftValueAnnotatedMutationService", NS,
					"FinishedMutationService"), p.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testBranchedStaging() {
		System.out.println("TEST BRANCHED STAGING");
		try {
			abductor.clearCaches();
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
	public void testTBoxBranchedStaging2() {
		System.out.println("TEST BRANCHED STAGING 2");
		try {
			abductor.clearCaches();
			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("test4a.owl")),
					"Manchester");
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"), dl.thing()));
			Path2 p = abductor.getBestPath2(ip,
					dl.clazz(NS + "FinishedMutation"));

			System.out.println(p.toString());

			assertEquals(String.format("[%s%s -> ([%s%s] & [%s%s]) -> %s%s]",
					NS, "MutationService", NS, "GeneAnnotatedMutationService",
					NS, "SiftValueAnnotatedMutationService", NS,
					"FinishedMutationService"), p.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testBranchedStaging2() {
		System.out.println("TEST BRANCHED STAGING 2");
		try {
			abductor.clearCaches();
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
		System.out.println("TEST 3-WAY BRANCHED STAGING");
		try {
			abductor.clearCaches();
			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("test3.owl")),
					"Manchester");
			abductor.debug();
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
	public void testTBox3WayBranchedStaging() {
		System.out.println("TEST 3-WAY BRANCHED STAGING");
		try {
			abductor.clearCaches();
			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("test3a.owl")),
					"Manchester");
			abductor.debug();
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Mutation")));
			Path2 p = abductor.getBestPath2(ip,
					dl.clazz(NS + "FinishedMutation"));

			System.out.println(p.toString());

			boolean matches = p.toString().equals(
					String.format("[([%s%s] & [%s%s] & [%s%s]) -> %s%s]", NS,
							"GeneAnnotatedMutationService", NS,
							"ProteinAnnotatedMutationService", NS,
							"SiftValueAnnotatedMutationService", NS,
							"FinishedMutationService"));

			assertEquals(true, matches);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testPipelineStaging() {
		System.out.println("TEST PIPELINE STAGING");
		try {
			abductor.clearCaches();
			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("pipeline-stage.owl")),
					"Manchester");
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Variant")));
			Path p = abductor.getBestPath(ip, dl.clazz(NS + "FinishedVariant"));

			System.out.println(p.toString());
			String expected = "[([http://krauthammerlab.med.yale.edu/test#AVS -> "
					+ "http://krauthammerlab.med.yale.edu/test#DBKS] || "
					+ "[([http://krauthammerlab.med.yale.edu/test#AVS -> "
					+ "http://krauthammerlab.med.yale.edu/test#RVFS] & "
					+ "[([http://krauthammerlab.med.yale.edu/test#AVS -> "
					+ "http://krauthammerlab.med.yale.edu/test#TLSS] || "
					+ "[http://krauthammerlab.med.yale.edu/test#AVS -> "
					+ "http://krauthammerlab.med.yale.edu/test#TLPS -> "
					+ "http://krauthammerlab.med.yale.edu/test#AANS -> "
					+ "http://krauthammerlab.med.yale.edu/test#PPCS] || "
					+ "[([http://krauthammerlab.med.yale.edu/test#AVS -> "
					+ "http://krauthammerlab.med.yale.edu/test#TLPS] & "
					+ "[http://krauthammerlab.med.yale.edu/test#IPIS]) -> "
					+ "http://krauthammerlab.med.yale.edu/test#CDMS] || "
					+ "[http://krauthammerlab.med.yale.edu/test#AVS -> "
					+ "http://krauthammerlab.med.yale.edu/test#TLPS -> "
					+ "http://krauthammerlab.med.yale.edu/test#AANS -> "
					+ "http://krauthammerlab.med.yale.edu/test#SSS] || "
					+ "[http://krauthammerlab.med.yale.edu/test#AVS -> "
					+ "http://krauthammerlab.med.yale.edu/test#TLPS -> "
					+ "http://krauthammerlab.med.yale.edu/test#AANS -> "
					+ "http://krauthammerlab.med.yale.edu/test#ICDS]) -> "
					+ "http://krauthammerlab.med.yale.edu/test#MUVS]) -> "
					+ "http://krauthammerlab.med.yale.edu/test#MRUVS]) -> "
					+ "http://krauthammerlab.med.yale.edu/test#FS]";

			assertEquals(expected, p.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testTBoxPipelineStaging() {
		System.out.println("TEST PIPELINE STAGING");
		try {
			abductor.clearCaches();
			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader()
					.getResourceAsStream("pipeline-stage2.owl")), "Manchester");
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Variant")));
			Path2 p = abductor.getBestPath2(ip,
					dl.clazz(NS + "FinishedVariant"));

			System.out.println(p.toString());
			String expected = String
					.format("[([%s -> %s] || [([%s -> %s] & [([%s -> %s] || "
							+ "[%s -> %s -> %s -> %s] || [%s -> %s "
							+ "-> %s -> %s] || [%s -> %s -> %s -> %s] || [([%s -> %s] & [%s]) "
							+ "-> %s]) -> %s]) -> %s]) -> %s]", NS
							+ "AlignVariantService", NS
							+ "DBKnownMutationKnownService", NS
							+ "AlignVariantService", NS
							+ "VariantFrequencyRareService", NS
							+ "AlignVariantService", NS
							+ "TranscriptLocaleSpliceService", NS
							+ "AlignVariantService", NS
							+ "TranscriptLocaleProteinCodingService", NS
							+ "AAChangeNonSynonymousService", NS
							+ "SiftSevereService", NS + "AlignVariantService", NS
							+ "TranscriptLocaleProteinCodingService", NS
							+ "AAChangeNonSynonymousService", NS
							+ "CriticalDomainInService", NS + "AlignVariantService",
							NS + "TranscriptLocaleProteinCodingService", NS
									+ "AAChangeNonSynonymousService", NS
									+ "PhylopConservedService", NS
									+ "AlignVariantService", NS
									+ "TranscriptLocaleProteinCodingService",
							NS + "IndelOrPointIndelService", NS
									+ "CriticalDomainMissingIsService", NS
									+ "MarkedUnusualVariantService", NS
									+ "MarkedRareAndUnusualVariantService", NS
									+ "FinishedVariantService");
			System.out.println("****************************************");
			System.out.println(expected);
			System.out.println("****************************************");
			System.out.println(p.toString());
			assertEquals(expected, p.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testPipelineExec1() {
		System.out.println("TEST PIPELINE EXEC 1");
		try {
			abductor.clearCaches();
			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("pipeline-stage.owl")),
					"Manchester");
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Variant")));
			Path p = abductor.getBestPath(ip, dl.clazz(NS + "FinishedVariant"));

			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("pipeline.owl")),
					"Manchester");

			IndividualPlus output;

			TestValues.RCMDB_KNOWN = true;
			output = abductor.exec(ip.copy(ip), p.copy());
			String known = getLiteralResult(output,
					dl.clazz(NS + "DatabasePresence"));
			assertEquals(known, "true");

			// Assure that other condition does not execute
			String prefreq = getLiteralResult(output,
					dl.clazz(NS + "AlleleFrequency"));
			assertNull(prefreq);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testTBoxPipelineExec1() {
		System.out.println("TEST PIPELINE EXEC 1");
		try {
			abductor.clearCaches();
			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("pipeline-stage2.owl")),
					"Manchester");
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Variant")));
			Path2 p = abductor.getBestPath2(ip, dl.clazz(NS + "FinishedVariant"));

			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("pipeline.owl")),
					"Manchester");

			IndividualPlus output;

			TestValues.RCMDB_KNOWN = true;
			output = abductor.exec2(ip.copy(ip), p.copy());
			String known = getLiteralResult(output,
					dl.clazz(NS + "DatabasePresence"));
			assertEquals(known, "true");

			// Assure that other condition does not execute
			String prefreq = getLiteralResult(output,
					dl.clazz(NS + "AlleleFrequency"));
			assertNull(prefreq);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testPipelineExec2() {
		System.out.println("TEST PIPELINE EXEC 2");
		try {
			abductor.clearCaches();
			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("pipeline-stage.owl")),
					"Manchester");
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Variant")));
			Path p = abductor.getBestPath(ip, dl.clazz(NS + "FinishedVariant"));

			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("pipeline.owl")),
					"Manchester");

			IndividualPlus output;

			TestValues.ALLELE_FREQUENCY = 0.001d;
			TestValues.TRANSCRIPT_LOCALE = "SpliceSite";
			output = abductor.exec(ip.copy(ip), p.copy());
			String locale = getLiteralResult(output,
					dl.clazz(NS + "VariationLocation"));
			assertEquals(locale, "SpliceSite");

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testPipelineExec3() {
		System.out.println("TEST PIPELINE EXEC 3");
		try {
			abductor.clearCaches();
			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("pipeline-stage.owl")),
					"Manchester");
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Variant")));
			Path p = abductor.getBestPath(ip, dl.clazz(NS + "FinishedVariant"));

			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("pipeline.owl")),
					"Manchester");

			IndividualPlus output;

			TestValues.ALLELE_FREQUENCY = 0.001d;
			TestValues.SIFT = 0.01d;
			output = abductor.exec(ip.copy(ip), p.copy());
			String preSift = getLiteralResult(output,
					dl.clazz(NS + "SiftScore"));
			Double sift = Double.parseDouble(preSift);
			assertEquals(sift, new Double(0.01));

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testPipelineExec4() {
		System.out.println("TEST PIPELINE EXEC 4");
		try {
			abductor.clearCaches();
			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("pipeline-stage.owl")),
					"Manchester");
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Variant")));
			Path p = abductor.getBestPath(ip, dl.clazz(NS + "FinishedVariant"));

			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("pipeline.owl")),
					"Manchester");

			IndividualPlus output;

			TestValues.ALLELE_FREQUENCY = 0.001d;
			TestValues.PHYLOP = 2.0d;
			output = abductor.exec(ip.copy(ip), p.copy());
			String prePhylop = getLiteralResult(output,
					dl.clazz(NS + "PhylopScore"));
			Double phylop = Double.parseDouble(prePhylop);
			assertEquals(phylop, new Double(2.0));

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testPipelineExec5() {
		System.out.println("TEST PIPELINE EXEC 5");
		try {
			abductor.clearCaches();
			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("pipeline-stage.owl")),
					"Manchester");
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Variant")));
			Path p = abductor.getBestPath(ip, dl.clazz(NS + "FinishedVariant"));

			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("pipeline.owl")),
					"Manchester");

			IndividualPlus output;

			TestValues.ALLELE_FREQUENCY = 0.001d;
			TestValues.CRITICAL_DOMAIN = true;
			output = abductor.exec(ip.copy(ip), p.copy());
			String criticalDomain = getLiteralResult(output,
					dl.clazz(NS + "VariationDomainColocation"));
			assertEquals(criticalDomain, "true");

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testPipelineExec6() {
		System.out.println("TEST PIPELINE EXEC 6");
		try {
			abductor.clearCaches();
			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("pipeline-stage.owl")),
					"Manchester");
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Variant")));
			Path p = abductor.getBestPath(ip, dl.clazz(NS + "FinishedVariant"));

			dl.load(new InputStreamReader(OWLAPIAbductorTest.class
					.getClassLoader().getResourceAsStream("pipeline.owl")),
					"Manchester");

			IndividualPlus output;

			TestValues.ALLELE_FREQUENCY = 0.001d;
			TestValues.INDEL_OR_POINT = "Indel";
			TestValues.CRITICAL_DOMAIN_MISSING = true;
			output = abductor.exec(ip.copy(ip), p.copy());
			String criticalDomain = getLiteralResult(output,
					dl.clazz(NS + "VariationDomainsMissingStatus"));
			assertEquals(criticalDomain, "true");

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testSimpleExec() {
		System.out.println("TEST SIMPLE EXEC");
		try {
			abductor.clearCaches();
			dl.load(new InputStreamReader(OWLAPIAbductor.class.getClassLoader()
					.getResourceAsStream("integration-abduct-exec.owl")),
					"Manchester");
			abductor.debug();
			IndividualPlus ip = new IndividualPlus(dl.individual(NS + "test"));
			ip.getAxioms().add(
					dl.individualType(dl.individual(NS + "test"),
							dl.clazz(NS + "Mutation")));
			Path p = abductor
					.getBestPath(ip, dl.clazz(NS + "FinishedMutation"));

			IndividualPlus output = abductor.exec(ip, p);

			// Test results
			String gene = getResult(output, dl.clazz(SO + "Gene"));
			assertEquals(NS + "Gene123", gene);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	private String getResult(IndividualPlus ip, DLClass<?> outputType) {
		dl.addAxioms(ip.getAxioms());
		String output = null;
		Collection<DLIndividual> descs = dl.getObjectPropertyValues(
				ip.getIndividual(), dl.objectProp(SIO + "is_described_by"));
		for (DLIndividual<?> desc : descs) {
			Collection<DLIndividual> refs = dl.getObjectPropertyValues(desc,
					dl.objectProp(SIO + "refers_to"));
			for (DLIndividual<?> ref : refs) {
				if (dl.getTypes(ref).contains(outputType)) {
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

	private String getLiteralResult(IndividualPlus ip, DLClass<?> outputType) {
		dl.addAxioms(ip.getAxioms());
		String output = null;
		Collection<DLIndividual> descs = dl.getObjectPropertyValues(
				ip.getIndividual(), dl.objectProp(SIO + "is_described_by"));
		for (DLIndividual<?> desc : descs) {
			Collection<DLIndividual> refs = dl.getObjectPropertyValues(desc,
					dl.objectProp(SIO + "refers_to"));
			for (DLIndividual<?> ref : refs) {
				if (dl.getTypes(ref).contains(outputType)) {
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

	@Test
	public void testSimpleExecDP() {
		System.out.println("TEST SIMPLE EXEC DP");
		try {
			abductor.clearCaches();
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
			String preSift = getLiteralResult(output,
					dl.clazz(NS + "SiftValue"));
			Double sift = Double.parseDouble(preSift);
			dl.removeAxioms(output.getAxioms());

			assertEquals(new Double(0.5), sift);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testBranchingExec() {
		System.out.println("TEST BRANCHING EXEC 2");
		try {
			abductor.clearCaches();
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
			String preSift = getLiteralResult(output,
					dl.clazz(NS + "SiftValue"));
			Double sift = Double.parseDouble(preSift);
			assertEquals(new Double(0.5), sift);

			String gene = getResult(output, dl.clazz(SO + "Gene"));

			dl.removeAxioms(output.getAxioms());
			assertEquals(NS + "Gene123", gene);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testSimpleExecFDP() {
		System.out.println("TEST SIMPLE EXEC FDP");
		try {
			abductor.clearCaches();
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
			String preSift = getLiteralResult(output,
					dl.clazz(NS + "SiftValue"));
			Double sift = Double.parseDouble(preSift);

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

//	@Test
//	public void testMazeStaging() {
//		System.out.println("TEST MAZE STAGING");
//		try {
//			abductor.clearCaches();
//			dl.load(new InputStreamReader(OWLAPIAbductor.class.getClassLoader()
//					.getResourceAsStream("skel.owl")), "Manchester");
//			int numNodes = 20;
//			List<String> mazeNodes = new ArrayList<>();
//			int i = 0;
//			while (i <= numNodes) {
//				mazeNodes.add(String.format("%sT%S", NS, String.valueOf(++i)));
//			}
//			int randomOut = new Random().nextInt(numNodes) + 1;
//			Object randomNode = mazeNodes.get(randomOut);
//
//			MazeGenerator mg = new MazeGenerator();
//			mg.setNodePool(mazeNodes);
//			Maze m = mg.createDAG(mazeNodes, 0.0, 0.0, -1);
//			MazeTransformer mt = new MazeTransformer();
//			Set<DLAxiom<?>> ax = mt.transform(m);
//			dl.addAxioms(ax);
//
//			// abductor.debug();
//
//			DLIndividual<?> test = dl.individual(NS + "test");
//			IndividualPlus ip = new IndividualPlus(test);
//			DLClassExpression<?> ipType = dl.clazz(m.getRoot().getName()
//					+ "Input");
//			ip.getAxioms().add(dl.individualType(test, ipType));
//
//			DLClassExpression<?> goalClass = dl.clazz(String
//					.valueOf(randomNode) + "Output");
//			Path p = abductor.getBestPath(ip, goalClass);
//
//			String solution = mg.solveRandomDAG(m, String.valueOf(randomNode));
//
//			assertEquals(solution, p.toString());
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail();
//		}
//	}
	
	@Test
	public void testTBoxMazeStaging() {
		System.out.println("TEST MAZE STAGING");
		try {
			abductor.clearCaches();
			dl.load(new InputStreamReader(OWLAPIAbductor.class.getClassLoader()
					.getResourceAsStream("skel.owl")), "Manchester");
			int numNodes = 50;
			List<String> mazeNodes = new ArrayList<>();
			int i = 0;
			while (i <= numNodes) {
				mazeNodes.add(String.format("%sT%S", NS, String.valueOf(++i)));
			}
			int randomOut = new Random().nextInt(numNodes) + 1;
			Object randomNode = mazeNodes.get(randomOut);

			MazeGenerator mg = new MazeGenerator();
			mg.setNodePool(mazeNodes);
			Maze m = mg.createDAG(mazeNodes, 0.1, 0.0, -1);
			MazeTransformer2 mt = new MazeTransformer2();
			Set<DLAxiom<?>> ax = mt.transform(m);
			dl.addAxioms(ax);

			// abductor.debug();

			DLIndividual<?> test = dl.individual(NS + "test");
			IndividualPlus ip = new IndividualPlus(test);
			DLClassExpression<?> ipType = dl.clazz(m.getRoot().getName()
					+ "Input");
			ip.getAxioms().add(dl.individualType(test, ipType));

			DLClassExpression<?> goalClass = dl.clazz(String
					.valueOf(randomNode) + "Output");
			Path2 p = abductor.getBestPath2(ip, goalClass);

			String solution = mg.solveRandomDAG(m, String.valueOf(randomNode));
			
			String pathString = p.toString().replace("Service", "");

			assertEquals(solution, pathString);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/*
	 * @Test public void testMazeStaging2() { try { dl.load(new
	 * InputStreamReader(OWLAPIAbductor.class.getClassLoader()
	 * .getResourceAsStream("skel.owl")), "Manchester"); int numNodes = 10;
	 * List<String> mazeNodes = new ArrayList<>(); int i = 0; while (i <=
	 * numNodes) { mazeNodes.add(String.format("%sT%S", NS,
	 * String.valueOf(++i))); } int randomOut = new Random().nextInt(numNodes) +
	 * 1; Object randomNode = mazeNodes.get(randomOut);
	 * 
	 * MazeGenerator mg = new MazeGenerator(); mg.setNodePool(mazeNodes); Maze m
	 * = mg.createDAG(mazeNodes, 0.3, 0.0, -1); MazeTransformer mt = new
	 * MazeTransformer(); Set<DLAxiom<?>> ax = mt.transform(m);
	 * dl.addAxioms(ax); String mdump = m.dump();
	 * 
	 * System.out.println(mdump); String solution = mg.solveRandomDAG(m,
	 * String.valueOf(randomNode));
	 * 
	 * System.out.println("SOLUTION"); System.out.println(solution);
	 * 
	 * // abductor.debug(); System.out.println("ABFAB Solution");
	 * 
	 * DLIndividual<?> test = dl.individual(NS + "test"); IndividualPlus ip =
	 * new IndividualPlus(test); DLClassExpression<?> ipType =
	 * dl.clazz(m.getRoot().getName() + "Input");
	 * ip.getAxioms().add(dl.individualType(test, ipType));
	 * 
	 * DLClassExpression<?> goalClass = dl.clazz(String .valueOf(randomNode) +
	 * "Output");
	 * 
	 * // Try each of 3 reasoners // List<Abductor> abductors = new
	 * ArrayList<>(); // abductors.add(new FactPPAbductor("")); //
	 * abductors.add(new HermitAbductor("")); // //abductors.add(new
	 * Pellet2Abductor()); // Random rand = new Random(); // // while
	 * (abductors.size() > 0) { // // long start = System.currentTimeMillis();
	 * // // int r = rand.nextInt(abductors.size()); // Abductor ab =
	 * abductors.get(r); // ab.setNamespace(NS); //
	 * System.out.println(String.format("Using %s", ab.getClass())); // dl =
	 * ab.getDLController(); // dl.load(new
	 * InputStreamReader(OWLAPIAbductor.class //
	 * .getClassLoader().getResourceAsStream("skel.owl")), // "Manchester"); //
	 * // dl.addAxioms(ax); // // DLIndividual<?> test = dl.individual(NS +
	 * "test"); // IndividualPlus ip = new IndividualPlus(test); //
	 * DLClassExpression<?> ipType = dl.clazz(m.getRoot().getName() // +
	 * "Input"); // ip.getAxioms().add(dl.individualType(test, ipType)); // //
	 * DLClassExpression<?> goalClass = dl.clazz(String // .valueOf(randomNode)
	 * + "Output"); // // Path p = ab.getBestPath(ip, goalClass); //
	 * System.out.println(p); // // assertEquals(solution, p.toString()); //
	 * long now = System.currentTimeMillis(); // double seconds = ((double) (now
	 * - start)) / 1000d; // System.out //
	 * .println(String.format("Done in %f seconds", seconds)); //
	 * abductors.remove(r); // }
	 * 
	 * Path p = abductor.getBestPath(ip, goalClass); System.out.println(p);
	 * 
	 * assertEquals(solution, p.toString()); } catch (Exception e) {
	 * e.printStackTrace(); fail(); } }
	 */

//	@Test
//	public void testMazeStaging3() {
//		System.out.println("TEST MAZE STAGING 3");
//		try {
//			abductor.clearCaches();
//			dl.load(new InputStreamReader(OWLAPIAbductor.class.getClassLoader()
//					.getResourceAsStream("skel.owl")), "Manchester");
//			MazeGenerator mg = new MazeGenerator();
//			Node n1 = new Node("1");
//			Maze m = new Maze(n1);
//
//			Node n11 = new Node("1-1");
//			Maze m1 = new Maze(n11, 0d, 1);
//			Node n21 = new Node("2-1", n11);
//			m1.addNode(n21);
//			n11.getBranches().add(n21);
//
//			Node n12 = new Node("1-2");
//			Maze m2 = new Maze(n12, 0d, 2);
//			Node n22 = new Node("2-2", n12);
//			m2.addNode(n22);
//			n12.getBranches().add(n22);
//
//			Branch n2 = new Branch("2", n1,
//					Arrays.asList(new Maze[] { m1, m2 }));
//			m.addNode(n2);
//			n1.getBranches().add(n2);
//
//			Node n3 = new Node("3", n2);
//			m.addNode(n3);
//			n2.getBranches().add(n3);
//
//			Node n13 = new Node("1-3");
//			Maze m3 = new Maze(n13, 0d, 3);
//			Node n23 = new Node("2-3", n13);
//			m3.addNode(n23);
//			n13.getBranches().add(n23);
//			Node n33 = new Node("3-3", n23);
//			m3.addNode(n33);
//			n23.getBranches().add(n33);
//			Node n43 = new Node("4-3", n33);
//			m3.addNode(n43);
//			n33.getBranches().add(n43);
//
//			Node n14 = new Node("1-4");
//			Maze m4 = new Maze(n14, 0d, 4);
//			Node n24 = new Node("2-4", n14);
//			m4.addNode(n24);
//			n14.getBranches().add(n24);
//			Node n34 = new Node("3-4", n24);
//			m4.addNode(n34);
//			n24.getBranches().add(n34);
//			Node n44 = new Node("4-4", n34);
//			m4.addNode(n44);
//			n34.getBranches().add(n44);
//
//			Branch n4 = new Branch("4", n3,
//					Arrays.asList(new Maze[] { m3, m4 }));
//			m.addNode(n4);
//			n3.getBranches().add(n4);
//
//			Node n5 = new Node("5", n4);
//			m.addNode(n5);
//			n4.getBranches().add(n5);
//
//			MazeTransformer mt = new MazeTransformer();
//			Set<DLAxiom<?>> ax = mt.transform(m);
//			dl.addAxioms(ax);
//			String mdump = m.dump();
//
//			System.out.println(mdump);
//			String solution = mg.solveRandomDAG(m, "5");
//
//			System.out.println("SOLUTION");
//			System.out.println(solution);
//
//			// abductor.debug();
//			System.out.println("ABFAB Solution");
//
//			DLIndividual<?> test = dl.individual(NS + "test");
//			IndividualPlus ip = new IndividualPlus(test);
//			DLClassExpression<?> ipType = dl.clazz(m.getRoot().getName()
//					+ "Input");
//			ip.getAxioms().add(dl.individualType(test, ipType));
//
//			DLClassExpression<?> goalClass = dl.clazz("5Output");
//			Path p = abductor.getBestPath(ip, goalClass);
//			System.out.println(p);
//
//			assertEquals(solution, p.toString());
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail();
//		}
//	}
	
	@Test
	public void testTBoxMazeStaging3() {
		System.out.println("TEST MAZE STAGING 3");
		try {
			abductor.clearCaches();
			dl.load(new InputStreamReader(OWLAPIAbductor.class.getClassLoader()
					.getResourceAsStream("skel.owl")), "Manchester");
			MazeGenerator mg = new MazeGenerator();
			Node n1 = new Node("1");
			Maze m = new Maze(n1);

			Node n11 = new Node("1-1");
			Maze m1 = new Maze(n11, 0d, 1);
			Node n21 = new Node("2-1", n11);
			m1.addNode(n21);
			n11.getBranches().add(n21);

			Node n12 = new Node("1-2");
			Maze m2 = new Maze(n12, 0d, 2);
			Node n22 = new Node("2-2", n12);
			m2.addNode(n22);
			n12.getBranches().add(n22);

			Branch n2 = new Branch("2", n1,
					Arrays.asList(new Maze[] { m1, m2 }));
			m.addNode(n2);
			n1.getBranches().add(n2);

			Node n3 = new Node("3", n2);
			m.addNode(n3);
			n2.getBranches().add(n3);

			Node n13 = new Node("1-3");
			Maze m3 = new Maze(n13, 0d, 3);
			Node n23 = new Node("2-3", n13);
			m3.addNode(n23);
			n13.getBranches().add(n23);
			Node n33 = new Node("3-3", n23);
			m3.addNode(n33);
			n23.getBranches().add(n33);
			Node n43 = new Node("4-3", n33);
			m3.addNode(n43);
			n33.getBranches().add(n43);

			Node n14 = new Node("1-4");
			Maze m4 = new Maze(n14, 0d, 4);
			Node n24 = new Node("2-4", n14);
			m4.addNode(n24);
			n14.getBranches().add(n24);
			Node n34 = new Node("3-4", n24);
			m4.addNode(n34);
			n24.getBranches().add(n34);
			Node n44 = new Node("4-4", n34);
			m4.addNode(n44);
			n34.getBranches().add(n44);

			Branch n4 = new Branch("4", n3,
					Arrays.asList(new Maze[] { m3, m4 }));
			m.addNode(n4);
			n3.getBranches().add(n4);

			Node n5 = new Node("5", n4);
			m.addNode(n5);
			n4.getBranches().add(n5);

			MazeTransformer2 mt = new MazeTransformer2();
			Set<DLAxiom<?>> ax = mt.transform(m);
			dl.addAxioms(ax);
			String mdump = m.dump();

			System.out.println(mdump);
			String solution = mg.solveRandomDAG(m, "5");

			System.out.println("SOLUTION");
			System.out.println(solution);

			// abductor.debug();
			System.out.println("ABFAB Solution");

			DLIndividual<?> test = dl.individual(NS + "test");
			IndividualPlus ip = new IndividualPlus(test);
			DLClassExpression<?> ipType = dl.clazz(m.getRoot().getName()
					+ "Input");
			ip.getAxioms().add(dl.individualType(test, ipType));

			DLClassExpression<?> goalClass = dl.clazz("5Output");
			Path2 p = abductor.getBestPath2(ip, goalClass);
			System.out.println(p);

			assertEquals(solution, p.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testConditionalBranchingExec() {
		System.out.println("TEST CONDITIONAL BRANCHING EXEC");
		abductor.clearCaches();
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
		System.out.println("TEST CONDITIONAL BRANCHING EXEC 2");
		abductor.clearCaches();
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
		System.out.println("TEST CONDITIONAL EXEC");
		try {
			abductor.clearCaches();
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
			assertEquals(true, output2.isStop());

			TestVals.sift = 0.5;

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
