package edu.yale.abfab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.yale.dlgen.DLClassExpression;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.controller.DLController;

public class Condition extends Step {
	Set<Path> paths;
	DLController dl;

	public Condition(Abductor abductor) {
		super(abductor);
	}

	public Condition(Collection<Step> steps, IndividualPlus initialInput,
			Abductor abductor) {
		super(abductor);
		dl = abductor.getDLController();
		paths = new HashSet<>();
		for (Step s : steps) {
			if (s instanceof SimpleStep) {
				SimpleStep simp = (SimpleStep) s;
				for (IndividualPlus output : simp.getOutput()) {
					DLClassExpression<?> unionType = dl.getIntersectingType(output
							.getIndividual());
					Path p = abductor.getBestPath(initialInput, unionType);
					paths.add(p);
				}
			} else if (s instanceof Branch) {
				Path newPath = new Path(initialInput, abductor);
				Set<Collection<DLIndividual<?>>> toAdd = new HashSet<>();
				toAdd.add(((Branch) s).getServices());
				paths.add(newPath);
			}
		}
	}

	@Override
	public Condition copy() {
		Condition out = new Condition(getAbductor());
		Set<Path> newPaths = new HashSet<>();
		for (Path p : paths) {
			newPaths.add(p.copy());
		}
		return out;
	}

	@Override
	public double getCost() {
		// Return lowest cost
		double min = Double.MIN_VALUE;
		for (Path p : paths) {
			if (p.getCost() >= min) {
				min = p.getCost();
			}
		}
		return min;
	}

	@Override
	public IndividualPlus exec(IndividualPlus input) {
		Path pathToUse = null;
		Abductor ab = getAbductor();
		for (Path p : paths) {
			if (ab.matchesInput(input, p)) {
				pathToUse = p;
				break;
			}
		}
		if (pathToUse == null) {
			return null;
		} else {
			return pathToUse.exec(input);
		}
	}

	@Override
	public Collection<IndividualPlus> getInput() {
		Set<IndividualPlus> outcomes = new HashSet<>();
		for (Path p : paths) {
			for (IndividualPlus ip : p.getLastInput()) {
				outcomes.add(ip);
			}
		}
		return outcomes;
	}

	@Override
	public Collection<IndividualPlus> getOutput() {
		Set<IndividualPlus> outcomes = new HashSet<>();
		for (Path p : paths) {
			for (IndividualPlus ip : p.getLastOutput()) {
				outcomes.add(ip);
			}
		}
		return outcomes;
	}

	@Override
	public Collection<DLClassExpression> getDLClasses() {
		// Quite possibly wrong
		Set<DLClassExpression> output = new HashSet<>();
		for (Path p : paths) {
			output.addAll(p.getTopStepDLClasses());
		}
		return output;
	}

	@Override
	public DLClassExpression<?> getUnifiedClass() {
		DLClassExpression<?> output;
		Collection<DLClassExpression> dlClasses = getDLClasses();
		if (dlClasses.size() == 1) {
			output = dlClasses.iterator().next();
		} else {
			output = dl.orClass(dlClasses.toArray(new DLClassExpression[dlClasses.size()]));
		}
		return output;
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
				sb.append(" || ");
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
		Condition other = (Condition) obj;
		if (paths == null) {
			if (other.paths != null)
				return false;
		} else if (!paths.equals(other.paths))
			return false;
		return true;
	}

}
