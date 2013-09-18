package edu.yale.abfab;

import java.io.File;
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
	private int execStep;

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

	public void add(Collection<IndividualPlus> inds) {
		if (inds == null || inds.size() == 0) {
			throw new RuntimeException("Invalid addition to path");
		}
		if (inds.size() == 1) {
			add(inds.iterator().next().getIndividual());
		} else {
			List<DLIndividual<?>> l = new ArrayList<>();
			for (IndividualPlus ip : inds) {
				l.add(ip.getIndividual());
			}
			steps.add(0, new Branch(l, initialInput, abductor));
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

	public Step nextStep() {
		return steps.get(execStep + 1);
	}

	public IndividualPlus exec(IndividualPlus input) {
		execStep = -1;
		IndividualPlus out = null;
		Set<DLAxiom<?>> ax = new HashSet<>();
		try {
			ax.addAll(input.getAxioms());
			dl.addAxioms(ax);
			Iterator<Step> stepIter = steps.iterator();
			IndividualPlus in = input;
			while (stepIter.hasNext()) {
				Step step = stepIter.next();

				// if (!abductor.individualMatchesType(in, step.getInput())) {
				// return null;
				// }
				++execStep;
				out = step.exec(in);
				if (out == null) {
					return null;
				}
				in = out;
			}
		} finally {
			dl.removeAxioms(ax);
		}
		return out;
	}

	private void debug() {
		try {
			dl.setOutputFile(new File(
					"/home/matt/sw/abfab-integration-output.owl"));
			dl.saveOntology();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	public Collection<DLClassExpression> getTopStepDLClasses() {
		return steps.get(0).getDLClasses();
	}

	public IndividualPlus getLastOutput() {
		return steps.get(0).getOutput();
	}

	public Collection<IndividualPlus> getLastInput() {
		return steps.get(0).getInput();
	}
}
