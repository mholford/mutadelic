package edu.yale.dlgen.owlapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClass;
import edu.yale.dlgen.DLClassExpression;
import edu.yale.dlgen.DLDataPropertyExpression;
import edu.yale.dlgen.DLEntity;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.DLLiteral;
import edu.yale.dlgen.DLObjectPropertyExpression;
import edu.yale.dlgen.controller.HermitDLController;
import edu.yale.dlgen.controller.OWLAPIDLController;

public class Pellet2DLControllerTest {

	private OWLAPIDLController dl;
	private OWLDataFactory df;
	private final String NS = "http://krauthammerlab.med.yale.edu/test#";

	@Before
	public void setUp() throws Exception {
		 //dl = new Pellet2DLController();
		dl = new HermitDLController();
		//dl = new ElkDLController();
		dl.load(new InputStreamReader(Pellet2DLControllerTest.class
				.getClassLoader().getResourceAsStream("test.manchester")),
				"Manchester");
		df = dl.getDataFactory();
	}

	@Test
	public void testAddAxiom() {
		dl.addAxiom(new DLAxiom<OWLAxiom>(df.getOWLSubClassOfAxiom(
				df.getOWLClass(IRI.create(NS + "Sitar")),
				df.getOWLClass(IRI.create(NS + "Instrument")))));
		Collection<DLAxiom> axioms = dl.getAxioms();
		assertEquals(27, axioms.size());
	}

	@Test
	public void testAddAxioms() {
		Set<DLAxiom<?>> ax = new HashSet<>();
		ax.add(new DLAxiom<>(df.getOWLSubClassOfAxiom(
				df.getOWLClass(IRI.create(NS + "Sitar")),
				df.getOWLClass(IRI.create(NS + "Instrument")))));
		ax.add(new DLAxiom<>(df.getOWLSubClassOfAxiom(
				df.getOWLClass(IRI.create(NS + "Drums")),
				df.getOWLClass(IRI.create(NS + "Instrument")))));
		dl.addAxioms(ax);
		assertEquals(28, dl.getAxioms().size());
	}

	@Test
	public void testRemoveAxioms() {
		Set<DLAxiom<?>> ax = new HashSet<>();
		ax.add(new DLAxiom<>(df.getOWLSubClassOfAxiom(
				df.getOWLClass(IRI.create(NS + "Sitar")),
				df.getOWLClass(IRI.create(NS + "Instrument")))));
		ax.add(new DLAxiom<>(df.getOWLSubClassOfAxiom(
				df.getOWLClass(IRI.create(NS + "Drums")),
				df.getOWLClass(IRI.create(NS + "Instrument")))));
		dl.addAxioms(ax);
		assertEquals(28, dl.getAxioms().size());

		dl.removeAxioms(ax);
		assertEquals(26, dl.getAxioms().size());
	}

	@Test
	public void testContainsAxiom() {
		DLAxiom<?> ax = new DLAxiom<>(df.getOWLSubClassOfAxiom(
				df.getOWLClass(IRI.create(NS + "Guitarist")),
				df.getOWLClass(IRI.create(NS + "Musician"))));
		DLAxiom<?> ax2 = new DLAxiom<>(df.getOWLSubClassOfAxiom(
				df.getOWLClass(IRI.create(NS + "Dar")),
				df.getOWLClass(IRI.create(NS + "Instrument"))));
		assertEquals(true, dl.containsAxiom(ax));
		assertEquals(false, dl.containsAxiom(ax2));
		dl.addAxiom(ax2);
		assertEquals(true, dl.containsAxiom(ax2));

		assertEquals(
				true,
				dl.containsAxiom(dl.individualType(
						dl.individual(NS + "JoeSmith"),
						dl.clazz(NS + "Guitarist"))));
		assertEquals(
				false,
				dl.containsAxiom(dl.individualType(
						dl.individual(NS + "JoeSmith"),
						dl.notClass(dl.clazz(NS + "Guitarist")))));
	}

	@Test
	public void testGetIRI() {
		OWLClass person = df.getOWLClass(IRI.create(NS + "Person"));
		String iri = dl.getIRI(new DLClass<>(person));
		assertEquals(iri, String.format("%sPerson", NS));
	}

	@Test
	public void testCheckEntailed() {
		OWLSubClassOfAxiom oax = df.getOWLSubClassOfAxiom(
				df.getOWLClass(IRI.create(NS + "Guitarist")),
				df.getOWLClass(IRI.create(NS + "Person")));
		boolean entailed = dl.checkEntailed(new DLAxiom<>(oax));
		assertEquals(true, entailed);

		OWLSubClassOfAxiom oax2 = df.getOWLSubClassOfAxiom(
				df.getOWLClass(IRI.create(NS + "Person")),
				df.getOWLClass(IRI.create(NS + "Guitarist")));
		entailed = dl.checkEntailed(new DLAxiom<>(oax2));
		assertEquals(false, entailed);
	}

	@Test
	public void testCheckConsistency() {
		boolean consistent = dl.checkConsistency();
		assertEquals(true, consistent);

		// Make the KB inconsistent - Joe is a guitarist but not a musician
		dl.addAxiom(new DLAxiom<>(df.getOWLClassAssertionAxiom(
				df.getOWLObjectComplementOf(df.getOWLClass(IRI.create(NS
						+ "Musician"))),
				df.getOWLNamedIndividual(IRI.create(NS + "JoeSmith")))));
		consistent = dl.checkConsistency();
		assertEquals(false, consistent);
	}

	@Test
	public void testGetDataProperties() {
		Collection<DLDataPropertyExpression> dataProperties = dl
				.getDataProperties(new DLIndividual<>(df
						.getOWLNamedIndividual(IRI.create(NS + "JoeSmith"))));
		assertEquals(3, dataProperties.size());
	}

	@Test
	public void testGetDataPropertyValue() {
		DLIndividual<OWLNamedIndividual> ind = new DLIndividual<>(
				df.getOWLNamedIndividual(IRI.create(NS + "JoeSmith")));
		DLDataPropertyExpression<OWLDataProperty> prop = new DLDataPropertyExpression<>(
				df.getOWLDataProperty(IRI.create(NS + "hasAge")));

		Collection<DLLiteral> dpv = dl.getDataPropertyValues(ind, prop);
		DLLiteral next = dpv.iterator().next();
		OWLLiteral lit = (OWLLiteral) next.get();
		assertEquals(33, lit.parseInteger());
	}

	@Test
	public void testGetLiteralValue() {
		DLIndividual<OWLNamedIndividual> ind = new DLIndividual<>(
				df.getOWLNamedIndividual(IRI.create(NS + "JoeSmith")));
		DLDataPropertyExpression<OWLDataProperty> prop = new DLDataPropertyExpression<>(
				df.getOWLDataProperty(IRI.create(NS + "hasAge")));

		Collection<DLLiteral> dpv = dl.getDataPropertyValues(ind, prop);
		DLLiteral next = dpv.iterator().next();
		assertEquals("33", dl.getLiteralValue(next));
	}

	@Test
	public void testBooleanAsLiteral() {
		DLLiteral<?> lit = dl.asLiteral(true);
		assertEquals("true", dl.getLiteralValue(lit));
	}

	@Test
	public void testDoubleAsLiteral() {
		DLLiteral<?> lit = dl.asLiteral(1.234d);
		assertEquals("1.234", dl.getLiteralValue(lit));
	}

	@Test
	public void testIntAsLiteral() {
		DLLiteral<?> lit = dl.asLiteral(12);
		assertEquals("12", dl.getLiteralValue(lit));
	}

	@Test
	public void testFloatAsLiteral() {
		DLLiteral<?> lit = dl.asLiteral(12.3f);
		assertEquals("12.3", dl.getLiteralValue(lit));
	}

	@Test
	public void testStringAsLiteral() {
		DLLiteral<?> lit = dl.asLiteral("hello");
		assertEquals("hello", dl.getLiteralValue(lit));
	}

	@Test
	public void testGetObjectProperties() {
		Collection<DLObjectPropertyExpression> objectProperties = dl
				.getObjectProperties(new DLIndividual<>(df
						.getOWLNamedIndividual(IRI.create(NS + "JoeSmith"))));
		assertEquals(2, objectProperties.size());
	}

	@Test
	public void testGetObjectPropertyValue() {
		DLIndividual<OWLNamedIndividual> ind = new DLIndividual<>(
				df.getOWLNamedIndividual(IRI.create(NS + "JoeSmith")));
		DLObjectPropertyExpression<OWLObjectProperty> prop = new DLObjectPropertyExpression<>(
				df.getOWLObjectProperty(IRI.create(NS + "hasChild")));

		Collection<DLIndividual> opv = dl.getObjectPropertyValues(ind, prop);
		DLIndividual next = opv.iterator().next();
		OWLNamedIndividual i = (OWLNamedIndividual) next.get();
		assertEquals(NS + "RodSmith", i.getIRI().toString());
	}

	@Test
	public void testGetTypes() {
		DLIndividual<OWLNamedIndividual> ind = new DLIndividual<>(
				df.getOWLNamedIndividual(IRI.create(NS + "JoeSmith")));
		Collection<DLClassExpression> types = dl.getTypes(ind);
		assertEquals(1, types.size());
		OWLClassExpression oce = (OWLClassExpression) types.iterator().next()
				.get();
		assertEquals(NS + "Guitarist", ((OWLClass) oce).getIRI().toString());
	}

	@Test
	public void testGetInstances() {
		DLClass<OWLClass> clz = new DLClass<>(df.getOWLClass(IRI.create(NS
				+ "Guitarist")));
		Collection<DLIndividual> instances = dl.getInstances(clz);
		assertEquals(1, instances.size());
		OWLNamedIndividual oi = (OWLNamedIndividual) instances.iterator()
				.next().get();
		assertEquals(NS + "JoeSmith", oi.getIRI().toString());
	}

	@Test
	public void testGetHavingPropertyValue() {
		DLClass<OWLClass> clz = new DLClass<OWLClass>(df.getOWLClass(IRI
				.create((NS + "Guitarist"))));
		DLIndividual<OWLNamedIndividual> ind = new DLIndividual<>(
				df.getOWLNamedIndividual(IRI.create(NS + "RodSmith")));
		DLObjectPropertyExpression<OWLObjectProperty> op = new DLObjectPropertyExpression<OWLObjectProperty>(
				df.getOWLObjectProperty(IRI.create(NS + "hasChild")));
		Collection<DLIndividual> inds = dl.getHavingPropertyValue(clz, op, ind);
		assertEquals(1, inds.size());
		OWLNamedIndividual oi = (OWLNamedIndividual) inds.iterator().next()
				.get();
		assertEquals(NS + "JoeSmith", oi.getIRI().toString());
	}

	@Test
	public void testGetDifferentIndividuals() {
		DLIndividual<OWLNamedIndividual> ind = new DLIndividual<>(
				df.getOWLNamedIndividual(IRI.create(NS + "JoeSmith")));
		Collection<DLIndividual> diffs = dl.getDifferentIndividuals(ind);
		assertEquals(2, diffs.size());
	}

	@Test
	public void testGetSameIndividuals() {
		DLIndividual<OWLNamedIndividual> ind = new DLIndividual<>(
				df.getOWLNamedIndividual(IRI.create(NS + "JoeSmith")));
		Collection<DLIndividual> sames = dl.getSameIndividuals(ind);
		assertEquals(1, sames.size());
	}

	@Test
	public void testIndividual() {
		DLIndividual<?> ind = dl.individual(NS + "TestInd");
		assertEquals(NS + "TestInd", ((OWLNamedIndividual) ind.get()).getIRI()
				.toString());
	}

	@Test
	public void testClazz() {
		DLClass<?> clz = dl.clazz(NS + "TestClz");
		assertEquals(NS + "TestClz", ((OWLClass) clz.get()).getIRI().toString());
	}

	@Test
	public void testNotClass() {
		DLClassExpression<?> clz = dl.notClass(dl.clazz(NS + "Guitarist"));
		assertEquals(String.format("ObjectComplementOf(<%sGuitarist>)", NS),
				clz.get().toString());
	}

	@Test
	public void testAndClass() {
		DLClassExpression<?> clz = dl.andClass(dl.clazz(NS + "Person"),
				dl.clazz(NS + "Guitarist"));
		boolean b1 = String.format(
				"ObjectIntersectionOf(<%sPerson> <%sGuitarist>)", NS, NS)
				.equals(clz.get().toString());
		boolean b2 = String.format(
				"ObjectIntersectionOf(<%sGuitarist> <%sPerson>)", NS, NS)
				.equals(clz.get().toString());
		assertEquals(true, b1 || b2);
	}

	@Test
	public void testDataProp() {
		DLDataPropertyExpression<?> dp = dl.dataProp(NS + "hasProp");
		assertEquals(NS + "hasProp", ((OWLDataProperty) dp.get()).getIRI()
				.toString());
	}

	@Test
	public void testObjectProp() {
		DLObjectPropertyExpression<?> op = dl.objectProp(NS + "hasSpouse");
		assertEquals(NS + "hasSpouse", ((OWLObjectProperty) op.get()).getIRI()
				.toString());
	}

	@Test
	public void testNewIndividual() {
		DLAxiom<?> ax = dl.newIndividual(NS + "BillLee",
				dl.clazz(NS + "Guitarist"));
		dl.addAxiom(ax);
		Collection<DLClassExpression> types = dl.getTypes(dl.individual(NS
				+ "BillLee"));
		assertEquals(NS + "Guitarist", ((OWLClass) types.iterator().next()
				.get()).getIRI().toString());
	}

	@Test
	public void testIndividualType() {
		DLAxiom<?> ax = dl.individualType(dl.individual(NS + "RodSmith"),
				dl.clazz(NS + "Guitarist"));
		dl.addAxiom(ax);
		Collection<DLClassExpression> types = dl.getTypes(dl.individual(NS
				+ "RodSmith"));
		assertEquals(NS + "Guitarist", ((OWLClass) types.iterator().next()
				.get()).getIRI().toString());
	}

	@Test
	public void testNewDataFact() {
		DLAxiom<?> axiom = dl.newDataFact(dl.individual(NS + "RodSmith"),
				dl.dataProp(NS + "hasAge"),
				new DLLiteral<>(df.getOWLLiteral(9)));
		dl.addAxiom(axiom);

		Collection<DLLiteral> dpv = dl.getDataPropertyValues(
				dl.individual(NS + "RodSmith"), dl.dataProp(NS + "hasAge"));
		DLLiteral next = dpv.iterator().next();
		OWLLiteral lit = (OWLLiteral) next.get();
		assertEquals(9, lit.parseInteger());
	}

	@Test
	public void testNewObjectFact() {
		DLAxiom<?> ax = dl.newObjectFact(dl.individual(NS + "JameSmith"),
				dl.objectProp(NS + "hasChild"), dl.individual(NS + "RodSmith"));
		dl.addAxiom(ax);

		Collection<DLIndividual> opv = dl
				.getObjectPropertyValues(dl.individual(NS + "JameSmith"),
						dl.objectProp(NS + "hasChild"));
		DLIndividual<OWLNamedIndividual> next = opv.iterator().next();
		assertEquals(NS + "RodSmith", next.get().getIRI().toString());
	}

	@Test
	public void testGetSubclasses() {
		Collection<DLClassExpression> sc = dl.getSubclasses(dl.clazz(NS
				+ "Person"));
		assertEquals(2, sc.size());
		DLClass<?> g = dl.clazz(NS + "Guitarist");
		assertEquals(true, sc.contains(g));
	}

	@Test
	public void testIsSubclass() {
		boolean sub = dl.isSubclass(dl.clazz(NS + "Guitarist"),
				dl.clazz(NS + "Person"));
		assertEquals(true, sub);
		sub = dl.isSubclass(dl.clazz(NS + "Person"), dl.clazz(NS + "Guitarist"));
		assertEquals(false, sub);
		sub = dl.isSubclass(dl.clazz(NS + "Guitarist"),
				dl.clazz(NS + "Guitarist"));
		assertEquals(false, sub);
	}

	@Test
	public void testGetEquivalentClasses() {
		Collection<DLClassExpression> ec = dl.getEquivalentClasses(dl.clazz(NS
				+ "Guitarist"));
		assertEquals(1, ec.size());
	}

	@Test
	public void testGetTerms() {
		Collection<DLEntity> terms = dl.getTerms(new DLClassExpression<>(df
				.getOWLObjectIntersectionOf(
						df.getOWLClass(IRI.create(NS + "Person")),
						df.getOWLClass(IRI.create(NS + "Guitarist")))));
		assertEquals(2, terms.size());
		DLClass<?> p = dl.clazz(NS + "Person");
		assertEquals(true, terms.contains(p));
		assertEquals(true, terms.contains(dl.clazz(NS + "Guitarist")));

		terms = dl.getTerms(new DLClassExpression<>(df.getOWLObjectUnionOf(
				df.getOWLClass(IRI.create(NS + "Person")),
				df.getOWLClass(IRI.create(NS + "Guitar")))));
		assertEquals(2, terms.size());
		assertEquals(true, terms.contains(dl.clazz(NS + "Person")));
		assertEquals(true, terms.contains(dl.clazz(NS + "Guitar")));
	}

	@Test
	public void testGetQualification() {
		DLClassExpression<?> q = dl.getQualification(new DLClassExpression<>(df
				.getOWLObjectSomeValuesFrom(
						df.getOWLObjectProperty(IRI.create(NS + "owns")),
						df.getOWLClass(IRI.create(NS + "Gun")))));
		assertEquals(NS + "Gun", ((OWLClass) q.get()).getIRI().toString());
	}

	@Test
	public void testThing() {
		DLClassExpression<?> thing = dl.thing();
		assertEquals(((OWLClass) thing.get()).getIRI().toString(),
				"http://www.w3.org/2002/07/owl#Thing");
	}

	@Test
	public void testSaveOntology() {
		dl.addAxiom(new DLAxiom<>(df.getOWLSubClassOfAxiom(
				df.getOWLClass(IRI.create(NS + "Guitar")),
				df.getOWLClass(IRI.create(NS + "Instrument")))));
		File saveFile = new File(String.format("%s/savedOntology.manchester",
				System.getProperty("java.io.tmpdir")));
		dl.setOutputFile(saveFile);
		try {
			dl.saveOntology();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		assertEquals(true, saveFile.exists());

		// Check the added axiom was written
		boolean wasWritten = false;
		try {
			BufferedReader br = new BufferedReader(new FileReader(saveFile));
			String s;
			String toMatch = String.format("Class: <%sInstrument>", NS);
			while ((s = br.readLine()) != null) {
				if (s.trim().equals(toMatch)) {
					wasWritten = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(true, wasWritten);
	}
}
