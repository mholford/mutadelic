package edu.yale.abfab;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClassExpression;
import edu.yale.dlgen.DLDataPropertyExpression;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.DLLiteral;
import edu.yale.dlgen.DLObjectPropertyExpression;
import edu.yale.dlgen.controller.DLController;

public abstract class Step implements Comparable {

	private Abductor abductor;

	public Step(Abductor abductor) {
		this.abductor = abductor;
	}

	public abstract double getCost();

	public abstract IndividualPlus exec(IndividualPlus input, Path contextPath);

	public abstract Collection<IndividualPlus> getInput();

	public abstract Collection<IndividualPlus> getOutput();

	public abstract Step copy();

	public abstract Collection<DLClassExpression> getDLClasses();

	public abstract DLClassExpression<?> getUnifiedClass();

	public Abductor getAbductor() {
		return abductor;
	}

	public IndividualPlus mergeIndividuals(Collection<IndividualPlus> inds) {
		DLController dl = abductor.getDLController();
		String NS = abductor.getNamespace();
		Set<DLAxiom<?>> oldAx = new HashSet<>();
		Set<DLAxiom<?>> newAx = new HashSet<>();
		String name = "merge" + UUID.randomUUID().toString();
		IndividualPlus output = null;
		if (inds.size() > 1) {

			try {
				for (IndividualPlus ind : inds) {
					if (ind.getAxioms() != null) {
						oldAx.addAll(ind.getAxioms());
						newAx.addAll(ind.getAxioms());
						dl.addAxioms(ind.getAxioms());
					}
				}
				Map<DLDataPropertyExpression<?>, Collection<DLLiteral>> dpvs = new HashMap<>();
				Map<DLObjectPropertyExpression<?>, Collection<DLIndividual>> opvs = new HashMap<>();
				Set<DLClassExpression<?>> types = new HashSet<>();
				Set<DLIndividual<?>> diffIndivs = new HashSet<>();
				Set<DLIndividual<?>> sameIndivs = new HashSet<>();

				for (IndividualPlus ip : inds) {
					DLIndividual<?> i = ip.getIndividual();
					for (DLDataPropertyExpression<?> odpe : dl
							.getDataProperties(i)) {
						if (!dpvs.containsKey(odpe)) {
							dpvs.put(odpe, new HashSet<DLLiteral>());
						}
						dpvs.get(odpe)
								.addAll(dl.getDataPropertyValues(i, odpe));
					}
					for (DLObjectPropertyExpression<?> oope : dl
							.getObjectProperties(i)) {
						if (!opvs.containsKey(oope)) {
							opvs.put(oope, new HashSet<DLIndividual>());
						}
						opvs.get(oope).addAll(
								dl.getObjectPropertyValues(i, oope));
					}
					for (DLClassExpression<?> oce : dl.getTypes(i)) {
						types.add(oce);
					}
					for (DLIndividual<?> oi : dl.getDifferentIndividuals(i)) {
						diffIndivs.add(oi);
					}
					for (DLIndividual<?> oi : dl.getSameIndividuals(i)) {
						sameIndivs.add(oi);
					}
					diffIndivs.add(i);
					sameIndivs.add(i);
				}

				newAx.add(dl.newIndividual(NS + name, dl.thing()));
				for (DLClassExpression<?> t : types) {
					newAx.add(dl.individualType(dl.individual(NS + name), t));
				}
				for (DLDataPropertyExpression<?> odpe : dpvs.keySet()) {
					for (DLLiteral<?> owls : dpvs.get(odpe)) {
						newAx.add(dl.newDataFact(dl.individual(NS + name),
								odpe, owls));
					}
				}
				for (DLObjectPropertyExpression<?> oope : opvs.keySet()) {
					for (DLIndividual<?> oi : opvs.get(oope)) {
						newAx.add(dl.newObjectFact(dl.individual(NS + name),
								oope, oi));
					}
				}

				output = new IndividualPlus(dl.individual(NS + name), newAx);
			} finally {
				dl.removeAxioms(oldAx);
			}
		} else if (inds.size() == 1) {
			output = inds.iterator().next();
		}

		return output;
	}
}
