package edu.yale.abfab;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClassExpression;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.controller.DLController;

public class Path {

	private List<Step> steps;
	private DLController dl;
	private Abductor abductor;
	private IndividualPlus initialInput;

	public Path(IndividualPlus initialInput, Abductor abductor) {
		this.abductor = abductor;
		dl = abductor.getDLController();
		this.initialInput = initialInput;
		steps = new ArrayList<>();
	}

	public Path copy() {
		Path out = new Path(initialInput, abductor);
		List<Step> sc = new ArrayList<>();
		for (Step s : steps) {
			sc.add(s.copy());
		}
		out.steps = sc;
		return out;
	}

	public void add(Collection<DLIndividual<?>> inds) {
		if (inds == null || inds.size() == 0) {
			throw new RuntimeException("Invalid addition to path");
		}
		if (inds.size() == 1) {
			add(inds.iterator().next());
		} else {
			steps.add(0, new Branch(inds, initialInput, abductor));
		}
	}

	public void add(DLIndividual<?> ind) {
		steps.add(0, new SimpleStep(ind, abductor));
	}

	public double getCost() {
		double sum = 0.0;
		for (Step s : steps) {
			sum += s.getCost();
		}
		return sum;
	}

	public IndividualPlus exec(IndividualPlus input) {
		IndividualPlus out = null;
		Set<DLAxiom<?>> ax = new HashSet<>();
		try {
			ax.addAll(input.getAxioms());
			dl.addAxioms(ax);
			Iterator<Step> stepIter = steps.iterator();
			IndividualPlus in = input;
			while (stepIter.hasNext()) {
				out = stepIter.next().exec(in);
				in = out;
			}
		} finally {
			dl.removeAxioms(ax);
		}
		return out;
	}

	@Override
	public String toString() {
		List<Step> reversePaths = new ArrayList<Step>(steps);
		Collections.reverse(reversePaths);

		StringBuilder sb = new StringBuilder();
		sb.append("[");
		Iterator<Step> stepIter = steps.iterator();
		while (stepIter.hasNext()) {
			sb.append(stepIter.next().toString());
			if (stepIter.hasNext()) {
				sb.append(" -> ");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((steps == null) ? 0 : steps.hashCode());
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
		Path other = (Path) obj;
		if (steps == null) {
			if (other.steps != null)
				return false;
		} else if (!steps.equals(other.steps))
			return false;
		return true;
	}

	IndividualPlus getLastOutput() {
		return steps.get(0).getOutput();
	}

	IndividualPlus getLastInput() {
		return steps.get(0).getInput();
	}
}
