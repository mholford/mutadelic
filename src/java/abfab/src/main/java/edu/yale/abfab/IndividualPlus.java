package edu.yale.abfab;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((axioms == null) ? 0 : axioms.hashCode());
		result = prime * result
				+ ((individual == null) ? 0 : individual.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IndividualPlus other = (IndividualPlus) obj;
		if (axioms == null) {
			if (other.axioms != null)
				return false;
		} else if (!axioms.equals(other.axioms))
			return false;
		if (individual == null) {
			if (other.individual != null)
				return false;
		} else if (!individual.equals(other.individual))
			return false;
		return true;
	}

}
