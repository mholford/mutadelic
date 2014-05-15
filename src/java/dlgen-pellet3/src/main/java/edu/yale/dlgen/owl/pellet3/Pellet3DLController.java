package edu.yale.dlgen.owl.pellet3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.iri.IRI;

import com.clarkparsia.pellet.api.io.OntologySyntax;
import com.clarkparsia.pellet.api.kb.KnowledgeBase;
import com.clarkparsia.pellet.api.kb.Ontology;
import com.clarkparsia.pellet.api.kb.OntologyFactory;
import com.clarkparsia.pellet.api.term.NamedTerm;
import com.clarkparsia.pellet.api.term.Term;
import com.clarkparsia.pellet.api.term.TermFactory;
import com.clarkparsia.pellet.api.term.TermSet;
import com.clarkparsia.pellet.api.term.axiom.Assertion;
import com.clarkparsia.pellet.api.term.axiom.Axiom;
import com.clarkparsia.pellet.api.term.axiom.DataPropertyAssertion;
import com.clarkparsia.pellet.api.term.axiom.ObjectPropertyAssertion;
import com.clarkparsia.pellet.api.term.axiom.TypeAssertion;
import com.clarkparsia.pellet.api.term.builtins.Classes;
import com.clarkparsia.pellet.api.term.entity.ClassExpression;
import com.clarkparsia.pellet.api.term.entity.DataProperty;
import com.clarkparsia.pellet.api.term.entity.Individual;
import com.clarkparsia.pellet.api.term.entity.Literal;
import com.clarkparsia.pellet.api.term.entity.NamedClass;
import com.clarkparsia.pellet.api.term.entity.NamedDataProperty;
import com.clarkparsia.pellet.api.term.entity.NamedEntity;
import com.clarkparsia.pellet.api.term.entity.NamedIndividual;
import com.clarkparsia.pellet.api.term.entity.NamedObjectProperty;
import com.clarkparsia.pellet.api.term.entity.ObjectProperty;
import com.clarkparsia.pellet.api.term.entity.QualifiedRestriction;
import com.clarkparsia.pellet.api.term.impl.axiom.DeclarationImpl;
import com.clarkparsia.pellet.api.term.query.AtomicQueryAtom;
import com.clarkparsia.pellet.api.term.visitor.TermVisitor;
import com.clarkparsia.pellet.hierarchy.Hierarchy;
import com.clarkparsia.pellet.hierarchy.HierarchyNode;
import com.clarkparsia.pellet.tableau.TableauKnowledgeBaseFactory;
import com.clarkparsia.pellet.util.Vars;

import static com.clarkparsia.pellet.api.term.TermFactory.*;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClass;
import edu.yale.dlgen.DLClassExpression;
import edu.yale.dlgen.DLDataPropertyExpression;
import edu.yale.dlgen.DLEntity;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.DLLiteral;
import edu.yale.dlgen.DLObjectPropertyExpression;
import edu.yale.dlgen.DLVisitor;
import edu.yale.dlgen.controller.DLController;
import edu.yale.dlgen.util.CollUtils;

public class Pellet3DLController implements DLController {

	KnowledgeBase kb;
	File outputFile;
	Map<String, DLVisitor<?>> visitors;
	private Map<String, OntologySyntax> syntaxMap;

	public Pellet3DLController() {
		kb = TableauKnowledgeBaseFactory.getInstance().create();
		visitors = new HashMap<>();
		syntaxMap = initSyntaxMap();
	}

	private Map<String, OntologySyntax> initSyntaxMap() {
		Map<String, OntologySyntax> out = new HashMap<>();
		out.put("Manchester", OntologySyntax.MANCHESTER);
		return out;
	}

	@Override
	public void addAxiom(DLAxiom<?> axiom) {
		kb.add((Axiom) axiom.get());
	}

	@Override
	public boolean containsAxiom(DLAxiom<?> ax) {
		return kb.contains((Axiom) ax.get());
	}

	@Override
	public void addAxioms(Set<DLAxiom<?>> axioms) {
		Set<Axiom> p3axioms = CollUtils.cast(axioms);
		kb.add(p3axioms);
	}
	
	@Override
	public void removeAxiom(DLAxiom<?> axiom) {
		kb.remove((Axiom) axiom.get());
	}

	@Override
	public void removeAxioms(Set<DLAxiom<?>> axioms) {
		Set<Axiom> p3axioms = CollUtils.cast(axioms);
		kb.remove(p3axioms);
	}

	@Override
	public Collection<DLAxiom> getAxioms() {
		return CollUtils.wrap(kb.getAxioms(), DLAxiom.class);
	}

	@Override
	public void saveOntology() throws IOException {
		kb.write(new FileWriter(outputFile), OntologySyntax.MANCHESTER);
	}

	@Override
	public String getIRI(DLEntity<?> entity) {
		NamedTerm namedTerm = (NamedTerm) entity.get();
		return namedTerm.getName();
	}

	@Override
	public boolean checkEntailed(DLAxiom<?> axiom) {
		AtomicQueryAtom ax = (AtomicQueryAtom) axiom.get();
		return kb.ask(ax);
	}

	@Override
	public boolean checkConsistency() {
		return kb.isConsistent();
	}

	@Override
	public Collection<DLDataPropertyExpression> getDataProperties(
			DLIndividual<?> individual) {
		Individual ind = (Individual) individual.get();
		Set<NamedDataProperty> dps = new HashSet<>();
		for (Axiom ax : kb.getAxioms()) {
			if (ax instanceof DataPropertyAssertion) {
				DataPropertyAssertion dpa = (DataPropertyAssertion) ax;
				Individual subj = dpa.getSubject();
				DataProperty prop = dpa.getProperty();
				if (subj.getName().equals(ind.getName())) {
					if (prop instanceof NamedDataProperty) {
						NamedDataProperty nprop = (NamedDataProperty) prop;
						dps.add(nprop);
					}
				}
			}
		}
		return CollUtils.wrap(dps, DLDataPropertyExpression.class);
	}

	@Override
	public Collection<DLLiteral> getDataPropertyValues(
			DLIndividual<?> individual, DLDataPropertyExpression<?> prop) {
		Individual ind = (Individual) individual.get();
		DataProperty dp = (DataProperty) prop.get();
		Iterable<Literal> lits = kb.selectLiteral(ind.fact(dp, Vars.LIT));
		return CollUtils.wrap(lits, DLLiteral.class);
	}

	@Override
	public Collection<DLObjectPropertyExpression> getObjectProperties(
			DLIndividual<?> individual) {
		Individual ind = (Individual) individual.get();
		Set<NamedObjectProperty> ops = new HashSet<>();
		for (Axiom ax : kb.getAxioms()) {
			if (ax instanceof ObjectPropertyAssertion) {
				ObjectPropertyAssertion opa = (ObjectPropertyAssertion) ax;
				Individual subj = opa.getSubject();
				ObjectProperty prop = opa.getProperty();
				if (subj.getName().equals(ind.getName())) {
					if (prop instanceof NamedObjectProperty) {
						NamedObjectProperty nprop = (NamedObjectProperty) prop;
						ops.add(nprop);
					}
				}
			}
		}
		return CollUtils.wrap(ops, DLObjectPropertyExpression.class);
	}

	@Override
	public Collection<DLIndividual> getObjectPropertyValues(
			DLIndividual<?> individual, DLObjectPropertyExpression<?> prop) {
		Individual ind = (Individual) individual.get();
		ObjectProperty op = (ObjectProperty) prop.get();
		Iterable<Individual> inds = kb.selectIndividual(ind.fact(op, Vars.IND));
		return CollUtils.wrap(inds, DLIndividual.class);
	}

	@Override
	public Collection<DLClassExpression> getTypes(DLIndividual<?> individual) {
		Individual ind = (Individual) individual.get();
		Set<NamedClass> clzs = new HashSet<>();
		for (Axiom ax : kb.getAxioms()) {
			if (ax instanceof TypeAssertion) {
				TypeAssertion ta = (TypeAssertion) ax;
				Individual subj = ta.getIndividual();
				if (subj.getName().equals(ind.getName())) {
					ClassExpression type = ta.getType();
					if (type instanceof NamedClass) {
						NamedClass nc = (NamedClass) type;
						clzs.add(nc);
					}
				}
			}
		}
		return CollUtils.wrap(clzs, DLClassExpression.class);
	}
	
	@Override
	public DLClassExpression getIntersectingType(DLIndividual<?> individual) {
		DLClassExpression unionType;
		Collection<DLClassExpression> ce = getTypes(individual);
		if (ce.size() == 1) {
			unionType = ce.iterator().next();
		} else {
			unionType = andClass(ce
					.toArray(new DLClassExpression[ce.size()]));
		}
		
		return unionType;
	}

	@Override
	public Collection<DLIndividual> getHavingPropertyValue(
			DLClassExpression<?> type, DLObjectPropertyExpression<?> prop,
			DLIndividual<?> value) {
		/*
		 * Would prefer but does assertions and so doesn't match the OWLAPI
		 * stuff
		 */
		// ObjectProperty op = (ObjectProperty) prop.get();
		// Individual ind = (Individual) value.get();
		// Iterable<Individual> inds = kb.selectIndividual(assertion(Vars.IND,
		// op,
		// ind));
		// return CollUtils.wrap(inds, DLIndividual.class);

		NamedIndividual ind = (NamedIndividual) value.get();
		NamedObjectProperty op = (NamedObjectProperty) prop.get();
		Set<Individual> inds = new HashSet<>();
		for (Axiom ax : kb.getAxioms()) {
			if (ax instanceof ObjectPropertyAssertion) {
				ObjectPropertyAssertion opa = (ObjectPropertyAssertion) ax;
				Individual object = opa.getObject();
				ObjectProperty property = opa.getProperty();
				if (property instanceof NamedObjectProperty) {
					if (object instanceof NamedIndividual) {
						NamedObjectProperty nop = (NamedObjectProperty) property;
						NamedIndividual ni = (NamedIndividual) object;
						if (nop.getName().equals(op.getName())
								&& ni.getName().equals(ind.getName())) {
							inds.add(opa.getSubject());
						}
					}
				}
			}
		}
		return CollUtils.wrap(inds, DLIndividual.class);
	}

	@Override
	public Collection<DLIndividual> getInstances(DLClassExpression<?> clz) {
		NamedClass nc = (NamedClass) clz.get();
		Set<Individual> inds = new HashSet<>();
		for (Axiom ax : kb.getAxioms()) {
			if (ax instanceof TypeAssertion) {
				TypeAssertion ta = (TypeAssertion) ax;
				ClassExpression ce = ta.getType();
				if (ce instanceof NamedClass) {
					NamedClass nce = (NamedClass) ce;
					if (nce.getName().equals(nc.getName())) {
						Individual ni = ta.getIndividual();
						inds.add(ni);
					}
				}
			}
		}
		return CollUtils.wrap(inds, DLIndividual.class);
	}

	@Override
	public Collection<DLIndividual> getDifferentIndividuals(
			DLIndividual<?> individual) {
		Individual ind = (Individual) individual.get();
		Iterable<Individual> inds = kb.selectIndividual(ind
				.differentFrom(Vars.IND));
		return CollUtils.wrap(inds, DLIndividual.class);
	}

	@Override
	public Collection<DLIndividual> getSameIndividuals(
			DLIndividual<?> individual) {
		Individual ind = (Individual) individual.get();
		Set<Individual> inds = new HashSet<>();
		Iterable<Individual> preinds = kb
				.selectIndividual(ind.sameAs(Vars.IND));
		for (Individual p : preinds) {
			if (p.getName().equals(ind.getName())) {
				inds.add(p);
			}
		}
		return CollUtils.wrap(inds, DLIndividual.class);
	}

	@Override
	public String getLiteralValue(DLLiteral<?> literal) {
		Literal l = (Literal) literal.get();
		return l.getLexicalValue();
	}

	@Override
	public DLLiteral<?> asLiteral(boolean val) {
		return new DLLiteral<>(literal(val));
	}

	@Override
	public DLLiteral<?> asLiteral(double val) {
		return new DLLiteral<>(literal(val));
	}

	@Override
	public DLLiteral<?> asLiteral(int val) {
		return new DLLiteral<>(literal(val));
	}

	@Override
	public DLLiteral<?> asLiteral(float val) {
		return new DLLiteral<>(literal(val));
	}

	@Override
	public DLLiteral<?> asLiteral(String val) {
		return new DLLiteral<>(literal(val));
	}

	@Override
	public DLAxiom<?> newIndividual(String name, DLClassExpression<?> clz) {
		return new DLAxiom<>(type(namedIndividual(name),
				(ClassExpression) clz.get()));
	}

	@Override
	public DLAxiom<?> individualType(DLIndividual<?> individual,
			DLClassExpression<?> clz) {
		return new DLAxiom<>(type((Individual) individual.get(),
				(ClassExpression) clz.get()));
	}

	@Override
	public DLAxiom<?> newDataFact(DLIndividual<?> individual,
			DLDataPropertyExpression<?> prop, DLLiteral<?> value) {
		Individual ind = (Individual) individual.get();
		DataProperty dp = (DataProperty) prop.get();
		Literal lit = (Literal) value.get();
		return new DLAxiom<>(assertion(ind, dp, lit));
	}

	@Override
	public DLAxiom<?> newObjectFact(DLIndividual<?> individual,
			DLObjectPropertyExpression<?> prop, DLIndividual<?> value) {
		Individual ind = (Individual) individual.get();
		ObjectProperty op = (ObjectProperty) prop.get();
		Individual val = (Individual) value.get();
		return new DLAxiom<>(assertion(ind, op, val));
	}

	@Override
	public Collection<DLClassExpression> getSubclasses(DLClass<?> clz) {
		Collection<DLClassExpression> out = new HashSet<>();
		Hierarchy<NamedClass> hier = kb.getClassHierarchy();
		Iterable<HierarchyNode<NamedClass>> subs = hier
				.getSubs((NamedClass) clz.get());
		for (HierarchyNode<NamedClass> hn : subs) {
			for (NamedClass nc : hn.getElements()) {
				if (!nc.equals(Classes.NOTHING)) {
					out.add(new DLClassExpression<>(nc));
				}
			}
		}
		return out;
	}

	@Override
	public boolean isSubclass(DLClassExpression<?> sub, DLClassExpression<?> sup) {
		Collection<ClassExpression> sc = new HashSet<>();
		ClassExpression subc = (ClassExpression) sub.get();
		Hierarchy<NamedClass> hier = kb.getClassHierarchy();
		Iterable<HierarchyNode<NamedClass>> subs = hier
				.getSubs((NamedClass) sup.get());
		for (HierarchyNode<NamedClass> hn : subs) {
			for (NamedClass nc : hn.getElements()) {
				sc.add(nc);
			}
		}
		return sc.contains(subc);
	}

	@Override
	public Collection<DLClassExpression> getEquivalentClasses(
			DLClassExpression<?> clz) {
		Hierarchy<NamedClass> hier = kb.getClassHierarchy();
		Set<NamedClass> equivalents = hier.getEquivalents((NamedClass) clz
				.get());
		return CollUtils.wrap(equivalents, DLClassExpression.class);
	}

	@Override
	public void addVisitor(DLVisitor<?> visitor, DLEntity<?> entity) {
		TermVisitor<?> tv = (TermVisitor<?>) visitor.get();
		Term t = (Term) entity.get();
		t.accept(tv);
	}

	@Override
	public void registerVisitor(String name, DLVisitor<?> visitor) {
		visitors.put(name, visitor);
	}

	@Override
	public DLVisitor<?> getVisitor(String name) {
		return visitors.get(name);
	}

	@Override
	public Collection<DLClassExpression> getTerms(DLClassExpression<?> clz) {
		TermSet<Term> ce = (TermSet<Term>) clz.get();
		Set<Term> args = ce.getArgs();
		return CollUtils.wrap(args, DLClassExpression.class);
	}

	@Override
	public DLClassExpression<?> getQualification(DLClassExpression<?> clz) {
		QualifiedRestriction qr = (QualifiedRestriction) clz.get();
		return new DLClassExpression<>((ClassExpression) qr.getQualification());
	}

	@Override
	public DLClassExpression<NamedClass> thing() {
		return new DLClassExpression<>(Classes.THING);
	}

	@Override
	public DLClass<?> clazz(String name) {
		return new DLClass<>(namedClass(name));
	}

	@Override
	public DLClassExpression<?> notClass(DLClassExpression<?> clz) {
		ClassExpression c = (ClassExpression) clz.get();
		return new DLClassExpression<>(not(c));
	}

	@Override
	public DLClassExpression<?> andClass(DLClassExpression<?>... clz) {
		Set<ClassExpression> toJoin = new HashSet<>();
		for (DLClassExpression<?> c : clz) {
			toJoin.add((ClassExpression) c.get());
		}
		return new DLClassExpression<>(and(toJoin));
	}

	@Override
	public DLIndividual<?> individual(String name) {
		return new DLIndividual<>(namedIndividual(name));
	}

	@Override
	public DLDataPropertyExpression<?> dataProp(String name) {
		return new DLDataPropertyExpression<DataProperty>(
				namedDataProperty(name));
	}

	@Override
	public DLObjectPropertyExpression<?> objectProp(String name) {
		return new DLObjectPropertyExpression<ObjectProperty>(
				namedObjectProperty(name));
	}

	@Override
	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	@Override
	public boolean load(Reader reader) {
		boolean loaded = true;
		try {
			Ontology ont = OntologyFactory.readOntology(reader);
			kb.load(ont);
		} catch (IOException e) {
			e.printStackTrace();
			loaded = false;
		}
		return loaded;
	}

	@Override
	public boolean load(Reader reader, String type) {
		boolean loaded = true;
		try {
			OntologySyntax syntax = syntaxMap.get(type);
			if (syntax == null) {
				throw new Exception("Syntax type not found");
			}
			Ontology ont = OntologyFactory.readOntology(reader, syntax);
			kb.load(ont);
		} catch (Exception e) {
			e.printStackTrace();
			loaded = false;
		}
		return loaded;
	}

	public KnowledgeBase getKb() {
		return kb;
	}

	@Override
	public void clearAddedAxioms() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveOntology(OutputStream os) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DLLiteral<?> asLiteral(long val) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DLClassExpression<?> orClass(DLClassExpression<?>... clz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DLAxiom<?> clazzRestriction(DLClassExpression<?> clz,
			DLObjectPropertyExpression<?> prop, DLClassExpression<?> restriction) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DLClassExpression<?> some(DLObjectPropertyExpression<?> prop,
			DLClassExpression<?> restriction) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DLAxiom<?> newClazz(DLClassExpression<?> c) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DLAxiom<?> equiv(DLClassExpression<?> c1, DLClassExpression<?> c2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DLAxiom<?> subClass(DLClassExpression<?> c1, DLClassExpression<?> c2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDisjoint(DLClassExpression<?> c1, DLClassExpression<?> c2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DLClassExpression<?> getObjectSomeFiller(DLClassExpression<?> clz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void newOntology() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean load(Reader reader, boolean initReasoner) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean load(Reader reader, String type, boolean initReasoner) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DLObjectPropertyExpression<?> getObjectSomeProperty(
			DLClassExpression<?> clz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DLLiteral<?> getDataValueFiller(DLClassExpression<?> clz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DLDataPropertyExpression<?> getDataValueProperty(
			DLClassExpression<?> clz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isIntersectionClass(DLClassExpression<?> clz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUnionClass(DLClassExpression<?> clz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DLClassExpression<?> value(DLDataPropertyExpression<?> prop,
			DLLiteral<?> value) {
		// TODO Auto-generated method stub
		return null;
	}
}
