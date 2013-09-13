package edu.yale.abfab.v2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClassExpression;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.controller.DLController;

public class Branch extends Step {
	Set<Path> paths;
	DLController dl;

	public Branch(Abductor abductor) {
		super(abductor);
	}

	public Branch(Collection<DLIndividual<?>> inds,
			IndividualPlus initialInput, Abductor abductor) {
		super(abductor);
		dl = abductor.getDLController();
		paths = new HashSet<>();
		for (DLIndividual<?> i : inds) {
			Path p = abductor.getBestPath(initialInput, new IndividualPlus(i));
			paths.add(p);
		}
	}

	public Branch copy() {
		Branch out = new Branch(getAbductor());
		Set<Path> newPaths = new HashSet<>();
		for (Path p : paths) {
			newPaths.add(p.copy());
		}
		return out;
	}

	@Override
	public double getCost() {
		double sum = 0.0;
		for (Path p : paths) {
			sum += p.getCost();
		}
		return sum;
	}

	@Override
	public IndividualPlus exec(IndividualPlus input) {
		Set<IndividualPlus> outcomes = new HashSet<>();
		IndividualPlus out = null;
		Set<DLAxiom<?>> ax = new HashSet<>();
		try {
			ax.addAll(input.getAxioms());
			dl.addAxioms(ax);

			// Run the cheapest path first so sort by cost
			List<Path> costSortedPaths = new ArrayList<>(paths);
			Collections.sort(costSortedPaths, new Comparator<Path>() {

				@Override
				public int compare(Path o1, Path o2) {
					Double d1 = o1.getCost();
					Double d2 = o2.getCost();
					return d1.compareTo(d2);
				}
			});

			for (Path p : paths) {
				outcomes.add(p.exec(input));
				// peek and check if passes next step
				// Abductor ab = getAbductor();
				// Step nextStep = ab.getExecutingPath().nextStep();
				// if (!ab.partMatchesInput(outcomes, nextStep.getInput())) {
				// return null;
				// }
			}
			out = mergeIndividuals(outcomes);
		} finally {
			dl.removeAxioms(ax);
		}
		return out;
	}

	public Set<Path> getPaths() {
		return paths;
	}

	public void setPaths(Set<Path> paths) {
		this.paths = paths;
	}

	@Override
	public Collection<IndividualPlus> getInput() {
		Set<IndividualPlus> outcomes = new HashSet<>();
		for (Path p : paths) {
			for (IndividualPlus ip : p.getLastInput()) {
				outcomes.add(ip);
			}
		}
		// return mergeIndividuals(outcomes);
		return outcomes;
	}

	@Override
	public IndividualPlus getOutput() {
		Set<IndividualPlus> outcomes = new HashSet<>();
		for (Path p : paths) {
			outcomes.add(p.getLastOutput());
		}
		return mergeIndividuals(outcomes);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		Iterator<Path> piter = paths.iterator();
		while (piter.hasNext()) {
			Path p = piter.next();
			sb.append(p.toString());
			if (piter.hasNext()) {
				sb.append(" & ");
			}
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((paths == null) ? 0 : paths.hashCode());
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
		Branch other = (Branch) obj;
		if (paths == null) {
			if (other.paths != null)
				return false;
		} else if (!paths.equals(other.paths))
			return false;
		return true;
	}
}
