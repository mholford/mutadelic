package edu.yale.abfab.v2;

import java.util.HashSet;
import java.util.Set;

import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLIndividual;

public class IndividualPlus {

	DLIndividual<?> individual;
	Set<DLAxiom<?>> axioms;

	public IndividualPlus(DLIndividual<?> individual, Set<DLAxiom<?>> axioms) {
		this.individual = individual;
		this.axioms = axioms;
	}

	public IndividualPlus(DLIndividual<?> individual) {
		this(individual, new HashSet<DLAxiom<?>>());
	}

	public DLIndividual<?> getIndividual() {
		return individual;
	}

	public void setIndividual(DLIndividual<?> individual) {
		this.individual = individual;
	}

	public Set<DLAxiom<?>> getAxioms() {
		return axioms;
	}

	public void setAxioms(Set<DLAxiom<?>> axioms) {
		this.axioms = axioms;
	}

	@Override
	public String toString() {
		return String.format("name=%s; axioms=%s", individual.get().toString(),
				axioms.toString());
	}

}
