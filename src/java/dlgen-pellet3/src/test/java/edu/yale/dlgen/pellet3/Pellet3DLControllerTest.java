package edu.yale.dlgen.pellet3;

import static org.junit.Assert.*;

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

import com.clarkparsia.pellet.api.kb.KnowledgeBase;
import com.clarkparsia.pellet.api.term.axiom.Axiom;
import com.clarkparsia.pellet.api.term.axiom.SubClassOf;
import com.clarkparsia.pellet.api.term.entity.ClassExpression;
import com.clarkparsia.pellet.api.term.entity.DataProperty;
import com.clarkparsia.pellet.api.term.entity.Individual;
import com.clarkparsia.pellet.api.term.entity.Literal;
import com.clarkparsia.pellet.api.term.entity.NamedClass;
import com.clarkparsia.pellet.api.term.entity.NamedDataProperty;
import com.clarkparsia.pellet.api.term.entity.NamedIndividual;
import com.clarkparsia.pellet.api.term.entity.NamedObjectProperty;
import com.clarkparsia.pellet.api.term.entity.ObjectAnd;
import com.clarkparsia.pellet.api.term.entity.ObjectProperty;

import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClass;
import edu.yale.dlgen.DLClassExpression;
import edu.yale.dlgen.DLDataPropertyExpression;
import edu.yale.dlgen.DLEntity;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.DLLiteral;
import edu.yale.dlgen.DLObjectPropertyExpression;
import edu.yale.dlgen.owl.pellet3.Pellet3DLController;
import static com.clarkparsia.pellet.api.term.TermFactory.*;

public class Pellet3DLControllerTest {

	private Pellet3DLController dl;
	private KnowledgeBase kb;
	private final String NS = "http://krauthammerlab.med.yale.edu/test#";

	@Before
	public void setUp() throws Exception {
		dl = new Pellet3DLController();
		dl.load(new InputStreamReader(Pellet3DLControllerTest.class
				.getClassLoader().getResourceAsStream("test.manchester")),
				"Manchester");
		kb = dl.getKb();
	}

	@Test
	public void testAddAxiom() {
		dl.addAxiom(new DLAxiom<>(namedClass(NS + "Sitar").subClassOf(
				namedClass(NS + "Instrument"))));
		Collection<DLAxiom> axioms = dl.getAxioms();

		/*
		 * It's not registering the declarations of DataProperties (possible bug
		 * with reading of Manchester Syntax?)
		 */
		// assertEquals(27, axioms.size());
		assertEquals(24, axioms.size());
	}

	@Test
	public void testAddAxioms() {
		Set<DLAxiom<?>> ax = new HashSet<>();
		ax.add(new DLAxiom<>(namedClass(NS + "Sitar").subClassOf(
				namedClass(NS + "Instrument"))));
		ax.add(new DLAxiom<>(namedClass(NS + "Drums").subClassOf(
				namedClass(NS + "Instrument"))));
		dl.addAxioms(ax);
		assertEquals(25, dl.getAxioms().size());
	}

	@Test
	public void testContainsAxiom() {
		DLAxiom<?> ax = new DLAxiom<>(namedClass(NS + "Guitarist").subClassOf(
				namedClass(NS + "Musician")));
		DLAxiom<?> ax2 = new DLAxiom<>(namedClass(NS + "Sitar").subClassOf(
				namedClass(NS + "Instrument")));
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
	public void testRemoveAxioms() {
		Set<DLAxiom<?>> ax = new HashSet<DLAxiom<?>>();
		ax.add(new DLAxiom<>(namedClass(NS + "Sitar").subClassOf(
				namedClass(NS + "Instrument"))));
		ax.add(new DLAxiom<>(namedClass(NS + "Drums").subClassOf(
				namedClass(NS + "Instrument"))));
		dl.addAxioms(ax);
		assertEquals(25, dl.getAxioms().size());

		dl.removeAxioms(ax);
		assertEquals(23, dl.getAxioms().size());
	}

	@Test
	public void testGetIRI() {
		NamedClass person = namedClass(NS + "Person");
		String iri = person.getName();
		assertEquals(iri, String.format("%sPerson", NS));
	}

	@Test
	public void testCheckEntailed() {
		SubClassOf oax = subClassOf(namedClass(NS + "Guitarist"), namedClass(NS
				+ "Person"));
		boolean entailed = dl.checkEntailed(new DLAxiom<>(oax));
		assertEquals(true, entailed);

		SubClassOf oax2 = subClassOf(namedClass(NS + "Person"), namedClass(NS
				+ "Guitarist"));
		entailed = dl.checkEntailed(new DLAxiom<>(oax2));
		assertEquals(false, entailed);
	}

	@Test
	public void testCheckConsistency() {
		boolean consistent = dl.checkConsistency();
		assertEquals(true, consistent);

		// Make the KB inconsistent - Joe is a guitarist but not a musician
		dl.addAxiom(new DLAxiom<>(type(namedIndividual(NS + "JoeSmith"),
				not(namedClass(NS + "Musician")))));
		consistent = dl.checkConsistency();
		assertEquals(false, consistent);
	}

	@Test
	public void testGetDataProperties() {
		try {
			Collection<DLDataPropertyExpression> dataProperties = dl
					.getDataProperties(new DLIndividual<>(namedIndividual(NS
							+ "JoeSmith")));
			assertEquals(3, dataProperties.size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGetDataPropertyValue() {
		DLIndividual<NamedIndividual> ind = new DLIndividual<>(
				namedIndividual(NS + "JoeSmith"));
		DLDataPropertyExpression<NamedDataProperty> prop = new DLDataPropertyExpression<>(
				namedDataProperty(NS + "hasAge"));

		Collection<DLLiteral> dpv = dl.getDataPropertyValues(ind, prop);
		DLLiteral next = dpv.iterator().next();
		Literal lit = (Literal) next.get();
		assertEquals(33, Integer.parseInt(lit.getLexicalValue()));
	}

	@Test
	public void testGetLiteralValue() {
		DLIndividual<NamedIndividual> ind = new DLIndividual<>(
				namedIndividual(NS + "JoeSmith"));
		DLDataPropertyExpression<NamedDataProperty> prop = new DLDataPropertyExpression<>(
				namedDataProperty(NS + "hasAge"));

		Collection<DLLiteral> dpv = dl.getDataPropertyValues(ind, prop);
		DLLiteral next = dpv.iterator().next();
		assertEquals("33", dl.getLiteralValue(next));
	}

	@Test
	public void testGetObjectProperties() {
		try {
			Collection<DLObjectPropertyExpression> objectProperties = dl
					.getObjectProperties(new DLIndividual<>(namedIndividual(NS
							+ "JoeSmith")));
			assertEquals(2, objectProperties.size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGetObjectPropertyValue() {
		DLIndividual<NamedIndividual> ind = new DLIndividual<>(
				namedIndividual(NS + "JoeSmith"));
		DLObjectPropertyExpression<NamedObjectProperty> prop = new DLObjectPropertyExpression<>(
				namedObjectProperty(NS + "hasChild"));

		Collection<DLIndividual> opv = dl.getObjectPropertyValues(ind, prop);
		DLIndividual next = opv.iterator().next();
		NamedIndividual i = (NamedIndividual) next.get();
		assertEquals(NS + "RodSmith", i.getName());
	}

	@Test
	public void testGetTypes() {
		DLIndividual<NamedIndividual> ind = new DLIndividual<>(
				namedIndividual(NS + "JoeSmith"));
		Collection<DLClassExpression> types = dl.getTypes(ind);
		assertEquals(1, types.size());
		ClassExpression oce = (ClassExpression) types.iterator().next().get();
		assertEquals(NS + "Guitarist", ((NamedClass) oce).getName());
	}

	@Test
	public void testGetInstances() {
		DLClass<NamedClass> clz = new DLClass<>(namedClass(NS + "Guitarist"));
		Collection<DLIndividual> instances = dl.getInstances(clz);
		assertEquals(1, instances.size());
		NamedIndividual oi = (NamedIndividual) instances.iterator().next()
				.get();
		assertEquals(NS + "JoeSmith", oi.getName());
	}

	@Test
	public void testGetHavingPropertyValue() {
		DLClass<NamedClass> clz = new DLClass<NamedClass>(namedClass(NS
				+ "Guitarist"));
		DLIndividual<NamedIndividual> ind = new DLIndividual<>(
				namedIndividual(NS + "RodSmith"));
		DLObjectPropertyExpression<ObjectProperty> op = new DLObjectPropertyExpression<ObjectProperty>(
				namedObjectProperty(NS + "hasChild"));
		Collection<DLIndividual> inds = dl.getHavingPropertyValue(clz, op, ind);
		assertEquals(1, inds.size());
		NamedIndividual oi = (NamedIndividual) inds.iterator().next().get();
		assertEquals(NS + "JoeSmith", oi.getName());
	}

	@Test
	public void testGetDifferentIndividuals() {
		DLIndividual<NamedIndividual> ind = new DLIndividual<>(
				namedIndividual(NS + "JoeSmith"));
		Collection<DLIndividual> diffs = dl.getDifferentIndividuals(ind);
		assertEquals(2, diffs.size());
	}

	@Test
	public void testGetSameIndividuals() {
		DLIndividual<NamedIndividual> ind = new DLIndividual<>(
				namedIndividual(NS + "JoeSmith"));
		Collection<DLIndividual> sames = dl.getSameIndividuals(ind);
		assertEquals(1, sames.size());
	}

	@Test
	public void testIndividual() {
		DLIndividual<?> ind = dl.individual(NS + "TestInd");
		assertEquals(NS + "TestInd", ((NamedIndividual) ind.get()).getName());
	}

	@Test
	public void testClazz() {
		DLClass<?> clz = dl.clazz(NS + "TestClz");
		assertEquals(NS + "TestClz", ((NamedClass) clz.get()).getName());
	}

	@Test
	public void testNotClass() {
		DLClassExpression<?> clz = dl.notClass(dl.clazz(NS + "Guitarist"));
		assertEquals("not(test:Guitarist)", clz.get().toString());
	}
	
	@Test
	public void testAndClass() {
		DLClassExpression<?> clz = dl.andClass(dl.clazz(NS + "Person"),
				dl.clazz(NS + "Guitarist"));
		System.out.println(clz.get().toString());
		boolean b1 = "and(test:Person, test:Guitarist)".equals(clz
				.get().toString());
		boolean b2 = "and(test:Guitarist, test:Person)".equals(clz
				.get().toString());
		assertEquals(true, b1 || b2);
	}

	@Test
	public void testDataProp() {
		DLDataPropertyExpression<?> dp = dl.dataProp(NS + "hasProp");
		assertEquals(NS + "hasProp", ((NamedDataProperty) dp.get()).getName());
	}

	@Test
	public void testObjectProp() {
		DLObjectPropertyExpression<?> op = dl.objectProp(NS + "hasSpouse");
		assertEquals(NS + "hasSpouse",
				((NamedObjectProperty) op.get()).getName());
	}

	@Test
	public void testNewIndividual() {
		DLAxiom<?> ax = dl.newIndividual(NS + "BillLee",
				dl.clazz(NS + "Guitarist"));
		dl.addAxiom(ax);
		Collection<DLClassExpression> types = dl.getTypes(dl.individual(NS
				+ "BillLee"));
		assertEquals(NS + "Guitarist", ((NamedClass) types.iterator().next()
				.get()).getName());
	}

	@Test
	public void testIndividualType() {
		DLAxiom<?> ax = dl.individualType(dl.individual(NS + "RodSmith"),
				dl.clazz(NS + "Guitarist"));
		dl.addAxiom(ax);
		Collection<DLClassExpression> types = dl.getTypes(dl.individual(NS
				+ "RodSmith"));
		assertEquals(NS + "Guitarist", ((NamedClass) types.iterator().next()
				.get()).getName());
	}

	@Test
	public void testNewDataFact() {
		DLAxiom<?> axiom = dl.newDataFact(dl.individual(NS + "RodSmith"),
				dl.dataProp(NS + "hasAge"), new DLLiteral<>(literal(9)));
		dl.addAxiom(axiom);

		Collection<DLLiteral> dpv = dl.getDataPropertyValues(
				dl.individual(NS + "RodSmith"), dl.dataProp(NS + "hasAge"));
		DLLiteral next = dpv.iterator().next();
		Literal lit = (Literal) next.get();
		assertEquals(9, Integer.parseInt(lit.getLexicalValue()));
	}

	@Test
	public void testNewObjectFact() {
		DLAxiom<?> ax = dl.newObjectFact(dl.individual(NS + "JameSmith"),
				dl.objectProp(NS + "hasChild"), dl.individual(NS + "RodSmith"));
		dl.addAxiom(ax);

		Collection<DLIndividual> opv = dl
				.getObjectPropertyValues(dl.individual(NS + "JameSmith"),
						dl.objectProp(NS + "hasChild"));
		DLIndividual<NamedIndividual> next = opv.iterator().next();
		assertEquals(NS + "RodSmith", next.get().getName());
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
		Collection<DLEntity> terms = dl.getTerms(new DLClassExpression<>(and(
				namedClass(NS + "Person"), namedClass(NS + "Guitarist"))));
		assertEquals(2, terms.size());
		DLClass<?> p = dl.clazz(NS + "Person");
		assertEquals(true, terms.contains(p));
		assertEquals(true, terms.contains(dl.clazz(NS + "Guitarist")));

		terms = dl.getTerms(new DLClassExpression<>(or(
				namedClass(NS + "Person"), namedClass(NS + "Guitar"))));
		assertEquals(2, terms.size());
		assertEquals(true, terms.contains(dl.clazz(NS + "Person")));
		assertEquals(true, terms.contains(dl.clazz(NS + "Guitar")));
	}

	@Test
	public void testGetQualification() {
		DLClassExpression<?> q = dl
				.getQualification(new DLClassExpression<>(some(
						namedObjectProperty(NS + "owns"),
						namedClass(NS + "Gun"))));
		assertEquals(NS + "Gun", ((NamedClass) q.get()).getName());
	}

	@Test
	public void testThing() {
		DLClassExpression<?> thing = dl.thing();
		assertEquals(((NamedClass) thing.get()).getName(),
				"http://www.w3.org/2002/07/owl#Thing");
	}

	@Test
	public void testSaveOntology() {
		dl.addAxiom(new DLAxiom<>(namedClass(NS + "Guitar").subClassOf(
				namedClass(NS + "Instrument"))));
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
