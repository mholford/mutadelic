package edu.yale.dlgen.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ReaderDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNamedObject;
import org.semanticweb.owlapi.model.OWLNaryBooleanClassExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLPropertyRange;
import org.semanticweb.owlapi.model.OWLQuantifiedRestriction;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

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

public abstract class OWLAPIDLController implements DLController {

	protected OWLOntologyManager manager;
	private OWLOntology ontology;
	private File outputFile;
	private OWLReasoner reasoner;
	private OWLDataFactory df;
	private Map<String, DLVisitor<?>> visitors;

	public OWLAPIDLController() {
		manager = OWLManager.createOWLOntologyManager();
		df = manager.getOWLDataFactory();
		visitors = new HashMap<>();
	}

	public abstract OWLReasoner initReasoner();

	@Override
	public boolean load(Reader reader) {
		boolean loaded = true;
		try {
			ontology = manager
					.loadOntologyFromOntologyDocument(new ReaderDocumentSource(
							reader));
			reasoner = initReasoner();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			loaded = false;
		}
		return loaded;
	}

	@Override
	public boolean load(Reader reader, String type) {
		// OWLAPI sniffs out the syntax type?
		return load(reader);
	}

	@Override
	public void addAxiom(DLAxiom<?> axiom) {
		OWLAxiom ax = (OWLAxiom) axiom.get();
		manager.addAxiom(ontology, ax);
	}

	@Override
	public void addAxioms(Set<DLAxiom<?>> axioms) {
		Set<OWLAxiom> ax = CollUtils.cast(axioms);
		manager.addAxioms(ontology, ax);
	}

	@Override
	public void removeAxiom(DLAxiom<?> axiom) {
		manager.removeAxiom(ontology, (OWLAxiom) axiom.get());
	}

	@Override
	public void removeAxioms(Set<DLAxiom<?>> axioms) {
		Set<OWLAxiom> ax = CollUtils.cast(axioms);
		manager.removeAxioms(ontology, ax);
	}

	@Override
	public Collection<DLAxiom> getAxioms() {
		return CollUtils.wrap(ontology.getAxioms(), DLAxiom.class);
	}

	@Override
	public boolean containsAxiom(DLAxiom<?> ax) {
		return ontology.containsAxiom((OWLAxiom) ax.get());
	}

	@Override
	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	@Override
	public void saveOntology() throws IOException {
		try {
			manager.saveOntology(ontology,
					new ManchesterOWLSyntaxOntologyFormat(),
					new FileOutputStream(outputFile));
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

	@Override
	public String getIRI(DLEntity<?> entity) {
		OWLNamedObject ono = (OWLNamedObject) entity.get();
		return ono.getIRI().toString();
	}

	@Override
	public boolean checkEntailed(DLAxiom<?> axiom) {
		OWLAxiom ax = (OWLAxiom) axiom.get();
		return reasoner.isEntailed(ax);
	}

	@Override
	public boolean checkConsistency() {
		reasoner.flush();
		return reasoner.isConsistent();
	}

	@Override
	public Collection<DLDataPropertyExpression> getDataProperties(
			DLIndividual<?> individual) {
		OWLIndividual i = (OWLIndividual) individual.get();
		Map<OWLDataPropertyExpression, Set<OWLLiteral>> dpv = i
				.getDataPropertyValues(ontology);

		Set<OWLDataPropertyExpression> dps = dpv.keySet();
		Set<DLDataPropertyExpression> wrap = CollUtils.wrap(dps,
				DLDataPropertyExpression.class);
		return wrap;
	}

	@Override
	public Collection<DLLiteral> getDataPropertyValues(
			DLIndividual<?> individual, DLDataPropertyExpression<?> prop) {
		OWLIndividual ind = (OWLIndividual) individual.get();
		OWLDataProperty dp = (OWLDataProperty) prop.get();
		Set<OWLLiteral> dpvs = ind.getDataPropertyValues(dp, ontology);
		return CollUtils.wrap(dpvs, DLLiteral.class);
	}

	@Override
	public Collection<DLObjectPropertyExpression> getObjectProperties(
			DLIndividual<?> individual) {
		OWLIndividual i = (OWLIndividual) individual.get();
		Map<OWLObjectPropertyExpression, Set<OWLIndividual>> opv = i
				.getObjectPropertyValues(ontology);
		Set<OWLObjectPropertyExpression> ops = opv.keySet();
		Set<DLObjectPropertyExpression> wrap = CollUtils.wrap(ops,
				DLObjectPropertyExpression.class);
		return wrap;
	}

	@Override
	public Collection<DLIndividual> getObjectPropertyValues(
			DLIndividual<?> individual, DLObjectPropertyExpression<?> prop) {
		OWLIndividual ind = (OWLIndividual) individual.get();
		OWLObjectProperty op = (OWLObjectProperty) prop.get();
		Set<OWLIndividual> opvs = ind.getObjectPropertyValues(op, ontology);
		return CollUtils.wrap(opvs, DLIndividual.class);
	}

	@Override
	public Collection<DLClassExpression> getTypes(DLIndividual<?> individual) {
		OWLIndividual ind = (OWLIndividual) individual.get();
		return CollUtils.wrap(ind.getTypes(ontology), DLClassExpression.class);
	}

	@Override
	public Collection<DLIndividual> getInstances(DLClassExpression<?> clz) {
		OWLClass oc = (OWLClass) clz.get();
		return CollUtils.wrap(oc.getIndividuals(ontology), DLIndividual.class);
	}

	@Override
	public Collection<DLIndividual> getHavingPropertyValue(
			DLClassExpression<?> type, DLObjectPropertyExpression<?> prop,
			DLIndividual<?> value) {
		OWLClass tc = (OWLClass) type.get();
		OWLObjectProperty op = (OWLObjectProperty) prop.get();
		OWLNamedIndividual iv = (OWLNamedIndividual) value.get();
		Set<OWLIndividual> inds = new HashSet<>();

		for (OWLIndividual i : tc.getIndividuals(ontology)) {
			for (OWLIndividual ii : i.getObjectPropertyValues(op, ontology)) {
				if (ii instanceof OWLNamedIndividual) {
					OWLNamedIndividual nii = (OWLNamedIndividual) ii;
					if (nii.getIRI().toString().equals(iv.getIRI().toString())) {
						inds.add(i);
					}
				}
			}
		}

		return CollUtils.wrap(inds, DLIndividual.class);
	}

	@Override
	public Collection<DLIndividual> getDifferentIndividuals(
			DLIndividual<?> individual) {
		OWLIndividual ind = (OWLIndividual) individual.get();
		return CollUtils.wrap(ind.getDifferentIndividuals(ontology),
				DLIndividual.class);
	}

	@Override
	public Collection<DLIndividual> getSameIndividuals(
			DLIndividual<?> individual) {
		OWLIndividual ind = (OWLIndividual) individual.get();
		return CollUtils.wrap(ind.getSameIndividuals(ontology),
				DLIndividual.class);
	}

	@Override
	public String getLiteralValue(DLLiteral<?> literal) {
		OWLLiteral ol = (OWLLiteral) literal.get();
		return ol.getLiteral();
	}
	
	@Override
	public DLLiteral<?> asLiteral(boolean val) {
		return new DLLiteral<>(df.getOWLLiteral(val));
	}

	@Override
	public DLLiteral<?> asLiteral(double val) {
		return new DLLiteral<>(df.getOWLLiteral(val));
	}

	@Override
	public DLLiteral<?> asLiteral(int val) {
		return new DLLiteral<>(df.getOWLLiteral(val));
	}

	@Override
	public DLLiteral<?> asLiteral(float val) {
		return new DLLiteral<>(df.getOWLLiteral(val));
	}

	@Override
	public DLLiteral<?> asLiteral(String val) {
		return new DLLiteral<>(df.getOWLLiteral(val));
	}

	@Override
	public DLIndividual<?> individual(String name) {
		return new DLIndividual<OWLIndividual>(df.getOWLNamedIndividual(IRI
				.create(name)));
	}

	@Override
	public DLClass<?> clazz(String name) {
		return new DLClass<OWLClass>(df.getOWLClass(IRI.create(name)));
	}

	@Override
	public DLClassExpression<?> notClass(DLClassExpression<?> clz) {
		OWLClassExpression c = (OWLClassExpression) clz.get();
		return new DLClassExpression<>(df.getOWLObjectComplementOf(c));
	}

	@Override
	public DLClassExpression<?> andClass(DLClassExpression<?>... clz) {
		Set<OWLClassExpression> toJoin = new HashSet<>();
		for (DLClassExpression<?> c:clz) {
			toJoin.add((OWLClassExpression) c.get());
		}
		return new DLClassExpression<>(df.getOWLObjectIntersectionOf(toJoin));
	}

	@Override
	public DLDataPropertyExpression<?> dataProp(String name) {
		return new DLDataPropertyExpression<OWLDataProperty>(
				df.getOWLDataProperty(IRI.create(name)));
	}

	@Override
	public DLObjectPropertyExpression<?> objectProp(String name) {
		return new DLObjectPropertyExpression<OWLObjectProperty>(
				df.getOWLObjectProperty(IRI.create(name)));
	}

	@Override
	public DLAxiom<?> newIndividual(String name, DLClassExpression<?> clz) {
		OWLIndividual ind = df.getOWLNamedIndividual(IRI.create(name));
		return _indivAssertionAxiom(clz, ind);
	}

	private DLAxiom<?> _indivAssertionAxiom(DLClassExpression<?> clz,
			OWLIndividual ind) {
		OWLClassExpression oce = (OWLClassExpression) clz.get();
		OWLClassAssertionAxiom ax = df.getOWLClassAssertionAxiom(oce, ind);
//		manager.addAxiom(ontology, ax);
		return new DLAxiom<OWLAxiom>(ax);
	}

	@Override
	public DLAxiom<?> individualType(DLIndividual<?> individual,
			DLClassExpression<?> clz) {
		return _indivAssertionAxiom(clz, (OWLIndividual) individual.get());
	}

	@Override
	public DLAxiom<?> newDataFact(DLIndividual<?> individual,
			DLDataPropertyExpression<?> prop, DLLiteral<?> value) {
		OWLIndividual ind = (OWLIndividual) individual.get();
		OWLDataPropertyExpression dp = (OWLDataPropertyExpression) prop.get();
		OWLLiteral lit = (OWLLiteral) value.get();
		OWLDataPropertyAssertionAxiom ax = df.getOWLDataPropertyAssertionAxiom(
				dp, ind, lit);
//		manager.addAxiom(ontology, ax);
		return new DLAxiom<OWLAxiom>(ax);
	}

	@Override
	public DLAxiom<?> newObjectFact(DLIndividual<?> individual,
			DLObjectPropertyExpression<?> prop, DLIndividual<?> value) {
		OWLIndividual ind = (OWLIndividual) individual.get();
		OWLObjectPropertyExpression op = (OWLObjectPropertyExpression) prop
				.get();
		OWLIndividual val = (OWLIndividual) value.get();
		OWLObjectPropertyAssertionAxiom ax = df
				.getOWLObjectPropertyAssertionAxiom(op, ind, val);
//		manager.addAxiom(ontology, ax);
		return new DLAxiom<OWLAxiom>(ax);
	}

	@Override
	public Collection<DLClassExpression> getSubclasses(DLClass<?> clz) {
		OWLClass oc = (OWLClass) clz.get();
		Set<OWLClass> ocset = reasoner.getSubClasses(oc, false).getFlattened();
		ocset.remove(df.getOWLNothing());
		return CollUtils.wrap(ocset, DLClassExpression.class);
	}

	@Override
	public boolean isSubclass(DLClassExpression<?> sub, DLClassExpression<?> sup) {
		OWLClassExpression osub = (OWLClassExpression) sub.get();
		OWLClassExpression osup = (OWLClassExpression) sup.get();
		Set<OWLClass> ocset = reasoner.getSubClasses(osup, false)
				.getFlattened();
		return ocset.contains(osub);
	}

	@Override
	public Collection<DLClassExpression> getEquivalentClasses(
			DLClassExpression<?> clz) {
		OWLClassExpression oce = (OWLClassExpression) clz.get();
		Set<OWLClass> ocset = reasoner.getEquivalentClasses(oce).getEntities();
		return CollUtils.wrap(ocset, DLClassExpression.class);
	}

	@Override
	public void registerVisitor(String name, DLVisitor<?> visitor) {
		visitor.setDLController(this);
		visitor.init();
		visitors.put(name, visitor);
	}

	@Override
	public DLVisitor<?> getVisitor(String name) {
		return visitors.get(name);
	}

	@Override
	public void addVisitor(DLVisitor<?> visitor, DLEntity<?> entity) {
		OWLObjectVisitor v = (OWLObjectVisitor) entity.get();
		OWLObject ob = (OWLObject) entity.get();
		ob.accept(v);
	}

	@Override
	public Collection<DLEntity> getTerms(DLClassExpression<?> clz) {
		OWLNaryBooleanClassExpression onbce = (OWLNaryBooleanClassExpression) clz
				.get();
		Set<OWLClassExpression> operands = onbce.getOperands();
		return CollUtils.wrap(operands, DLEntity.class);
	}

	@Override
	public DLClassExpression<?> getQualification(DLClassExpression<?> clz) {
		OWLQuantifiedRestriction oqr = (OWLQuantifiedRestriction) clz.get();
		OWLClassExpression filler = (OWLClassExpression) oqr.getFiller();
		return new DLClassExpression<>(filler);
	}

	@Override
	public DLClassExpression<?> thing() {
		return new DLClassExpression<>(df.getOWLThing());
	}

	public OWLOntology getOntology() {
		return ontology;
	}

	public OWLDataFactory getDataFactory() {
		return df;
	}

}
