package edu.yale.abfab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.yale.abfab.Abductor.SCCIndividual;
import edu.yale.abfab.Abductor.SCCKey;
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

		paths = new HashSet<>();
		for (Step s : steps) {
			if (s instanceof SimpleStep) {
				SimpleStep simp = (SimpleStep) s;
				for (IndividualPlus output : simp.getOutput()) {
					DLClassExpression<?> unionType = dl
							.getIntersectingType(output.getIndividual());
					Set<Path> ps = abductor
							.getAllPaths(initialInput, unionType);
					paths.addAll(ps);
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
		// Path pathToUse = null;
		// Abductor ab = getAbductor();
		// for (Path p : paths) {
		// if (ab.matchesInput(input, p)) {
		// pathToUse = p;
		// break;
		// }
		// }
		// if (pathToUse == null) {
		// return null;
		// } else {
		// return pathToUse.exec(input);
		// }

		Abductor ab = getAbductor();
		IndividualPlus out = null;
		Set<DLAxiom<?>> ax = new HashSet<>();
		try {
			ax.addAll(input.getAxioms());
			dl.addAxioms(ax);

			// Try the cheapest path first
			List<Path> costSortedPaths = new ArrayList<>(paths);
			Collections.sort(costSortedPaths, new Comparator<Path>() {

				@Override
				public int compare(Path o1, Path o2) {
					Double d1 = o1.getCost();
					Double d2 = o2.getCost();
					return d1.compareTo(d2);
				}
			});

			IndividualPlus latestOutcome;

			for (Path p : costSortedPaths) {
				boolean fail = false;
				// ab.setExecutingPath(p);
				IndividualPlus outcome = p.exec(input);
				out = mergeIndividuals(Arrays.asList(outcome, out));

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
					for (DLClassExpression<?> nc : nextStep.getDLClasses()) {
						for (DLIndividual<?> nci : dl.getInstances(nc)) {
							for (DLIndividual<?> ncOut : dl
									.getObjectPropertyValues(nci, HAS_OUTPUT)) {
//								DLIndividual<?> testI = dl.individual(NS
//										+ "testI");
//								ax2.add(dl.newObjectFact(testI, HAS_OUTPUT,
//										ncOut));
//								ax2.addAll(outcome.getAxioms());
//								ax2.add(dl.newObjectFact(testI, HAS_INPUT,
//										outcome.getIndividual()));
//								ax2.add(dl.individualType(testI,
//										dl.notClass(nc)));
//								dl.addAxioms(ax2);
//								//ab.debug();
//								if (dl.checkConsistency()) {
//									fail = true;
//								}
//								dl.removeAxioms(ax2);
								IndividualPlus sInput = outcome;
								IndividualPlus sOutput = new IndividualPlus(ncOut);
								DLClassExpression<?> service = nc;
								SCCIndividual sccInput = ab.createSCCIndividual(sInput);
								SCCIndividual sccOutput = ab.createSCCIndividual(sOutput);
								SCCKey sccKey = ab.createSCCKey(service, sccInput, sccOutput);
								
								fail = ab.checkSCCache(sccKey);
								
								if (!fail) {
									// out.setStop(true);
									long end = System.currentTimeMillis();
									dbg(DBG_TIMING,
											"Condition peek: %d millis", end
													- start);
									return out;
								}
							}
						}
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
