package edu.yale.abfab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClassExpression;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.controller.DLController;
import static edu.yale.abfab.NS.*;
import static edu.yale.abfab.Logging.*;

public class Branch2 extends Step2 {
	Set<Path2> paths;
	DLController dl;
	Collection<DLClassExpression> services;

	public Branch2(Abductor abductor) {
		super(abductor);
		dl = abductor.getDLController();
	}

	public Branch2(Collection<DLClassExpression> services,
			IndividualPlus initialInput, Abductor abductor) {
		super(abductor);
		dl = abductor.getDLController();
		paths = new TreeSet<>(new Path2.CostBasedComparator());
		for (DLClassExpression i : services) {
			// DLClassExpression<?> unionType = dl.getIntersectingType(i);
			Path2 p = abductor.getBestPath2(initialInput,
					abductor.getServiceOutputFiller(i));
			paths.add(p);
		}
	}

	public Branch2 copy() {
		Branch2 out = new Branch2(getAbductor());
		Set<Path2> newPaths = new TreeSet<>(new Path2.CostBasedComparator());
		for (Path2 p : paths) {
			newPaths.add(p.copy());
		}
		out.setPaths(newPaths);
		return out;
	}

	@Override
	public int compareTo(Object o) {
		Branch2 bo = (Branch2) o;
		Iterator<Path2> pathIterator = paths.iterator();
		Iterator<Path2> otherPathIterator = bo.getPaths().iterator();
		while (pathIterator.hasNext()) {
			if (otherPathIterator.hasNext()) {
				Path2 p = pathIterator.next();
				Path2 op = otherPathIterator.next();
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
		double sum = 0.0;
		for (Path2 p : paths) {
			sum += p.getCost();
		}
		return sum;
	}

	@Override
	public IndividualPlus exec(IndividualPlus input, Path2 contextPath) {
		Abductor ab = getAbductor();
		Set<IndividualPlus> outcomes = new HashSet<>();
		IndividualPlus out = null;
		Set<DLAxiom<?>> ax = new HashSet<>();
		try {
			ax.addAll(input.getAxioms());
			dl.addAxioms(ax);

			Set<Path2> unexecutedPaths = new HashSet<>(paths);
			IndividualPlus currentEx = null;
			for (Path2 p : paths) {
				unexecutedPaths.remove(p);
				IndividualPlus latestOutcome = p.exec(input.copy(input));

				// If Path failed, quit here
				if (latestOutcome.isStop()) {
					// Merge with out? ie the latest path to execute?
					outcomes.add(latestOutcome);
					IndividualPlus early = ab.mergeIndividuals(outcomes);
					early.setStop(true);
					return early;
				}

				outcomes.add(latestOutcome);
				currentEx = ab.mergeIndividuals(outcomes);

				// peek and check if passes next step
				long start = System.currentTimeMillis();
				Set<DLAxiom<?>> ax2 = new HashSet<>();

				Step2 nextStep = null;
				if (contextPath.nextStep() != null) {
					nextStep = contextPath.nextStep();
				} else {
					nextStep = ab.getExecutingPath2().nextStep();
				}
				// Step nextStep = ab.getExecutingPath().nextStep();

				// Get classes of outputs of unexecutedPaths
				Set<DLClassExpression<?>> otherPathOutputClasses = new HashSet<>();
				for (Path2 up : unexecutedPaths) {
					for (DLClassExpression<?> tc : up.getLastStepDLClasses()) {
						DLClassExpression<?> tcOutput = ab
								.getServiceOutputFiller(tc);
						otherPathOutputClasses.add(tcOutput);
					}
				}

				for (DLClassExpression<?> otherPathOutputClass : otherPathOutputClasses) {
					currentEx.getAxioms().add(
							dl.individualType(currentEx.getIndividual(),
									otherPathOutputClass));
				}

				ax2.addAll(currentEx.getAxioms());

				dl.addAxioms(ax2);
				DLClassExpression<?> nextInput = ab
						.getServiceInputFiller(nextStep.getUnifiedClass());
//				ab.debug();
//				boolean fail = dl.checkEntailed(dl.individualType(
//							currentEx.getIndividual(), nextInput));
//				
				dl.addAxiom(dl.individualType(currentEx.getIndividual(), dl.notClass(nextInput)));
				ab.debug();
				boolean fail = !dl.checkConsistency();
				dl.removeAxiom(dl.individualType(currentEx.getIndividual(), dl.notClass(nextInput)));
				
				// boolean fail = dl.checkEntailed(dl.individualType(
				// currentEx.getIndividual(), nextInput));

				dl.removeAxioms(ax2);

				if (!fail) {
					currentEx.setStop(true);
					long end = System.currentTimeMillis();
					dbg(DBG_TIMING, "Branch peek: %d millis", end - start);
					return currentEx;
				}

				long end = System.currentTimeMillis();
				dbg(DBG_TIMING, "Branch peek: %d millis", end - start);
			}

			out = currentEx;
		} finally {
			dl.removeAxioms(ax);
		}
		return out;
	}

	public Set<Path2> getPaths() {
		return paths;
	}

	public void setPaths(Set<Path2> paths) {
		this.paths = paths;
	}

	public Collection<DLClassExpression> getServices() {
		return services;
	}

	@Override
	public Collection<DLClassExpression> getInput() {
		Set<DLClassExpression> outcomes = new HashSet<>();
		for (Path2 p : paths) {
			for (DLClassExpression ip : p.getLastInput()) {
				outcomes.add(ip);
			}
		}
		// return mergeIndividuals(outcomes);
		return outcomes;
	}

	@Override
	public Collection<DLClassExpression> getOutput() {
		Set<DLClassExpression> outcomes = new HashSet<>();
		for (Path2 p : paths) {
			for (DLClassExpression output : p.getLastOutput()) {
				outcomes.add(output);
			}
		}
		return outcomes;
	}

	@Override
	public Collection<DLClassExpression> getDLClasses() {
		Set<DLClassExpression> output = new HashSet<>();
		for (Path2 p : paths) {
			output.addAll(p.getTopStepDLClasses());
		}
		return output;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public DLClassExpression getUnifiedClass() {
		DLClassExpression<?> output;
		Collection<DLClassExpression> dlClasses = getDLClasses();
		if (dlClasses.size() == 1) {
			output = dlClasses.iterator().next();
		} else {
			output = dl.andClass(dlClasses
					.toArray(new DLClassExpression[dlClasses.size()]));
		}
		return output;
	}

	@Override
	public String toString() {
		List<Path2> sortedPaths = new ArrayList<>(paths);
		Collections.sort(sortedPaths);
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		Iterator<Path2> piter = sortedPaths.iterator();
		while (piter.hasNext()) {
			Path2 p = piter.next();
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
		Branch2 other = (Branch2) obj;
		if (paths == null) {
			if (other.paths != null)
				return false;
		} else if (!paths.equals(other.paths))
			return false;
		return true;
	}
}
