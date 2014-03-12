package edu.yale.abfab;

import java.util.ArrayList;
import java.util.Arrays;
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
import static edu.yale.abfab.NS.*;
import static edu.yale.abfab.Logging.*;

public class Branch extends Step {
	Set<Path> paths;
	DLController dl;
	Collection<DLIndividual<?>> services;

	public Branch(Abductor abductor) {
		super(abductor);
		dl = abductor.getDLController();
	}

	public Branch(Collection<DLIndividual<?>> services,
			IndividualPlus initialInput, Abductor abductor) {
		super(abductor);
		dl = abductor.getDLController();
		paths = new HashSet<>();
		for (DLIndividual<?> i : services) {
			DLClassExpression<?> unionType = dl.getIntersectingType(i);
			Path p = abductor.getBestPath(initialInput, unionType);
			paths.add(p);
		}
	}

	public Branch copy() {
		Branch out = new Branch(getAbductor());
		Set<Path> newPaths = new HashSet<>();
		for (Path p : paths) {
			newPaths.add(p.copy());
		}
		out.setPaths(newPaths);
		return out;
	}

	@Override
	public int compareTo(Object o) {
		Branch bo = (Branch) o;
		Iterator<Path> pathIterator = paths.iterator();
		Iterator<Path> otherPathIterator = bo.getPaths().iterator();
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
		double sum = 0.0;
		for (Path p : paths) {
			sum += p.getCost();
		}
		return sum;
	}

	@Override
	public IndividualPlus exec(IndividualPlus input, Path contextPath) {
		Abductor ab = getAbductor();
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

			Set<Path> unexecutedPaths = new HashSet<>(paths);
			IndividualPlus currentEx = null;
			for (Path p : costSortedPaths) {
				unexecutedPaths.remove(p);
				IndividualPlus latestOutcome = p.exec(input.copy(input));

				// If Path failed, quit here
				if (latestOutcome.isStop()) {
					// Merge with out?  ie the latest path to execute?
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

				Step nextStep = null;
				if (contextPath.nextStep() != null) {
					nextStep = contextPath.nextStep();
				} else {
					nextStep = ab.getExecutingPath().nextStep();
				}
				// Step nextStep = ab.getExecutingPath().nextStep();

				// Get classes of outputs of unexecutedPaths
				Set<DLClassExpression<?>> otherPathOutputClasses = new HashSet<>();
				for (Path up : unexecutedPaths) {
					for (DLClassExpression<?> tc : up.getLastStepDLClasses()) {
						for (DLIndividual<?> tci : dl.getInstances(tc)) {
							for (DLIndividual<?> tcOut : dl
									.getObjectPropertyValues(tci,
											dl.objectProp(NS + "has_output"))) {
								for (DLClassExpression<?> tcOutC : dl
										.getTypes(tcOut)) {
									otherPathOutputClasses.add(tcOutC);
								}
							}
						}
					}
				}

				for (DLClassExpression<?> nc : nextStep.getDLClasses()) {
					for (DLIndividual<?> nci : dl.getInstances(nc)) {
						for (DLIndividual<?> ncOut : dl
								.getObjectPropertyValues(nci,
										dl.objectProp(NS + "has_output"))) {

							/* PRE SCC */
							// DLIndividual<?> testI = dl.individual(NS +
							// "testI");
							// ax2.add(dl.newObjectFact(testI,
							// dl.objectProp(NS + "has_output"), ncOut));
							// IndividualPlus mergedOutcomes =
							// mergeIndividuals(outcomes);
							// ax2.addAll(mergedOutcomes.getAxioms());
							// ax2.add(dl.newObjectFact(testI,
							// dl.objectProp(NS + "has_input"),
							// mergedOutcomes.getIndividual()));
							// for (DLClassExpression<?> otherPathOutputClass :
							// otherPathOutputClasses) {
							// ax2.add(dl.individualType(
							// mergedOutcomes.getIndividual(),
							// otherPathOutputClass));
							// }
							// ax2.add(dl.individualType(testI,
							// dl.notClass(nc)));
							// dl.addAxioms(ax2);
							//
							// boolean fail = false;
							// ab.debug();
							// if (dl.checkConsistency()) {
							// fail = true;
							// }
							// dl.removeAxioms(ax2);

							/* POST SCC */
							IndividualPlus mergedOutcomes = ab.mergeIndividuals(outcomes);
							IndividualPlus sInput = mergedOutcomes;
							for (DLClassExpression<?> otherPathOutputClass : otherPathOutputClasses) {
								sInput.getAxioms().add(
										dl.individualType(
												mergedOutcomes.getIndividual(),
												otherPathOutputClass));
							}
							IndividualPlus sOutput = new IndividualPlus(ncOut);
							DLClassExpression<?> service = nc;
							SCCIndividual sccInput = ab
									.createSCCIndividual(sInput);
							SCCIndividual sccOutput = ab
									.createSCCIndividual(sOutput);
							SCCKey sccKey = ab.createSCCKey(service, sccInput,
									sccOutput);

							boolean fail = !ab.checkSCCache(sccKey);

							if (fail) {
								currentEx.setStop(true);
								long end = System.currentTimeMillis();
								dbg(DBG_TIMING, "Branch peek: %d millis", end
										- start);
								return currentEx;
							}
						}
					}
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

	public Set<Path> getPaths() {
		return paths;
	}

	public void setPaths(Set<Path> paths) {
		this.paths = paths;
	}

	public Collection<DLIndividual<?>> getServices() {
		return services;
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
	public Collection<IndividualPlus> getOutput() {
		Set<IndividualPlus> outcomes = new HashSet<>();
		for (Path p : paths) {
			for (IndividualPlus output : p.getLastOutput()) {
				outcomes.add(output);
			}
		}
		return Arrays
				.asList(new IndividualPlus[] { getAbductor().mergeIndividuals(outcomes) });
	}

	@Override
	public Collection<DLClassExpression> getDLClasses() {
		Set<DLClassExpression> output = new HashSet<>();
		for (Path p : paths) {
			output.addAll(p.getTopStepDLClasses());
		}
		return output;
	}

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
		List<Path> sortedPaths = new ArrayList<>(paths);
		Collections.sort(sortedPaths);
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		Iterator<Path> piter = sortedPaths.iterator();
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
