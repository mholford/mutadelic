package edu.yale.abfab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClassExpression;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.DLObjectPropertyExpression;
import edu.yale.dlgen.controller.DLController;
import static edu.yale.abfab.NS.*;
import static edu.yale.abfab.Logging.*;

public class Condition extends Step {
	Set<Path> paths;
	DLController dl;
	private DLObjectPropertyExpression<?> HAS_INPUT;
	private DLObjectPropertyExpression<?> HAS_OUTPUT;

	public Condition(Abductor abductor) {
		super(abductor);
		dl = abductor.getDLController();

		HAS_INPUT = dl.objectProp(NS + "has_input");
		HAS_OUTPUT = dl.objectProp(NS + "has_output");
	}

	public Condition(Collection<Step> steps, IndividualPlus initialInput,
			Abductor abductor) {
		super(abductor);
		dl = abductor.getDLController();
		HAS_INPUT = dl.objectProp(NS + "has_input");
		HAS_OUTPUT = dl.objectProp(NS + "has_output");

		paths = new TreeSet<>(new Path.CostBasedComparator());
		for (Step s : steps) {
			if (s instanceof SimpleStep) {
				SimpleStep simp = (SimpleStep) s;
				for (DLClassExpression output : simp.getOutput()) {
					Set<Path> ps = abductor.getAllPaths(initialInput, output);
					paths.addAll(ps);
				}
			} else if (s instanceof Branch) {
				Path newPath = new Path(initialInput, abductor);
				Set<Collection<DLClassExpression>> toAdd = new HashSet<>();
				toAdd.add(((Branch) s).getServices());
				paths.add(newPath);
			}
		}
	}

	@Override
	public Condition copy() {
		Condition out = new Condition(getAbductor());
		Set<Path> newPaths = new TreeSet<>(new Path.CostBasedComparator());
		for (Path p : paths) {
			newPaths.add(p.copy());
		}
		out.setPaths(newPaths);
		return out;
	}

	public Set<Path> getPaths() {
		return paths;
	}

	public void setPaths(Set<Path> paths) {
		this.paths = paths;
	}

	@Override
	public int compareTo(Object o) {
		if (!(o instanceof Condition)) {
			return 1;
		}
		Condition co = (Condition) o;
		Iterator<Path> pathIterator = paths.iterator();
		Iterator<Path> otherPathIterator = co.getPaths().iterator();
		while (pathIterator.hasNext()) {
			if (otherPathIterator.hasNext()) {
				Path p = pathIterator.next();
				Path op = otherPathIterator.next();
				int c = p.compareTo(op);
				if (c != 0) {
					return c;
				}
			} else {
				return 1;
			}
		}
		if (otherPathIterator.hasNext()) {
			return -1;
		} else {
			return 0;
		}
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
	public IndividualPlus exec(IndividualPlus input, Path contextPath) {
		Abductor ab = getAbductor();
		IndividualPlus out = null;
		Set<DLAxiom<?>> ax = new HashSet<>();
		try {
			ax.addAll(input.getAxioms());
			dl.addAxioms(ax);

			IndividualPlus latestOutcome;

			for (Path p : paths) {
				boolean fail = false;
				// ab.setExecutingPath(p);
				IndividualPlus outcome = p.exec(input);
				out = ab.mergeIndividuals(Arrays.asList(outcome, out));

				if (outcome.isStop()) {
					continue;
				}
				// Peek and see if it passes the next step
				long start = System.currentTimeMillis();
				Set<DLAxiom<?>> ax2 = new HashSet<>();

				Step nextStep = null;
				if (contextPath.nextStep() != null) {
					nextStep = contextPath.nextStep();
				} else {
					nextStep = ab.getExecutingPath().nextStep();
				}

				if (nextStep != null) {
					DLClassExpression<?> nextInput = ab
							.getServiceInputFiller(nextStep.getUnifiedClass());
					try {
						dl.addAxioms(outcome.getAxioms());
						fail = dl.checkEntailed(dl.individualType(
								outcome.getIndividual(), nextInput));
					} finally {
						dl.removeAxioms(outcome.getAxioms());
					}
					if (fail) {
						// out.setStop(true);
						long end = System.currentTimeMillis();
						dbg(DBG_TIMING, "Condition peek: %d millis", end
								- start);
						return out;
					}

				} else {
					// out = mergeIndividuals(Arrays
					// .asList(latestOutcome, outcome));
				}
				long end = System.currentTimeMillis();
				dbg(DBG_TIMING, "Condition peek: %d millis", end - start);
				// latestOutcome = outcome;
			}
		} finally {
			dl.removeAxioms(ax);
		}

		// if (out == null) {
		// outcome.setStop(true);
		// return outcome;
		// }
		return out;
	}

	@Override
	public Collection<DLClassExpression> getInput() {
		Set<DLClassExpression> outcomes = new HashSet<>();
		for (Path p : paths) {
			for (DLClassExpression ip : p.getLastInput()) {
				outcomes.add(ip);
			}
		}
		return outcomes;
	}

	@Override
	public Collection<DLClassExpression> getOutput() {
		Set<DLClassExpression> outcomes = new HashSet<>();
		for (Path p : paths) {
			for (DLClassExpression ip : p.getLastOutput()) {
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
			output = dl.orClass(dlClasses
					.toArray(new DLClassExpression[dlClasses.size()]));
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
