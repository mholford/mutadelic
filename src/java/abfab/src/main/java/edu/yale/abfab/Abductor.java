package edu.yale.abfab;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.yale.abfab.service.Service;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClass;
import edu.yale.dlgen.DLClassExpression;
import edu.yale.dlgen.DLDataPropertyExpression;
import edu.yale.dlgen.DLEntity;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.DLLiteral;
import edu.yale.dlgen.DLObjectIntersection;
import edu.yale.dlgen.DLObjectPropertyExpression;
import edu.yale.dlgen.DLObjectUnion;
import edu.yale.dlgen.DLVisitor;
import edu.yale.dlgen.controller.DLController;

public abstract class Abductor {

	private DLController dl;
	private Map<DLClassExpression<?>, Path> goalPathCache;
	private Map<DLIndividual<?>, Path> servicePathCache;
	private String namespace;
	private String NS;
	private Path executingPath;

	public Abductor() {
		dl = initDLController();
		goalPathCache = new HashMap<>();
		servicePathCache = new HashMap<>();
	}

	public abstract DLController initDLController();

	public DLController getDLController() {
		return dl;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
		NS = namespace;
	}

	public String getNamespace() {
		return namespace;
	}
	
	public Path getExecutingPath() {
		return executingPath;
	}

	public void setExecutingPath(Path executingPath) {
		this.executingPath = executingPath;
	}

	public IndividualPlus exec(IndividualPlus input, Path goalPath) {
		executingPath = goalPath;
		return goalPath.exec(input);
	}

	public Path getBestPath(IndividualPlus input, DLClassExpression<?> goalClass) {

		if (goalPathCache.containsKey(goalClass)) {
			return goalPathCache.get(goalClass);
		}

		Collection<DLIndividual> terminals = getTerminals(goalClass);
		Path bestPath = getBestPathToServices(input, terminals);

		goalPathCache.put(goalClass, bestPath);

		return bestPath;
	}

	public Path getBestPathToServices(IndividualPlus input,
			Collection<DLIndividual> terminals) {
		Set<Path> paths = new HashSet<>();
		Set<Path> completedPaths = new HashSet<>();

		for (DLIndividual<?> t : terminals) {
			Path p;
			if (servicePathCache.containsKey(t)) {
				p = servicePathCache.get(t);
			} else {
				p = new Path(input, this);
				p.add(t);
			}
			paths.add(p);
		}

		for (;;) {
			Set<Path> nextPaths = new HashSet<>();
			for (Path p : paths) {
				if (matchesInput(input, p.getLastInput())) {
					completedPaths.add(p);
				} else {
					nextPaths.addAll(extendPath(p));
				}
			}
			if (nextPaths.size() == 0) {
				break;
			}
			paths = nextPaths;
		}

		Path bestPath = chooseBestPath(completedPaths);
		return bestPath;
	}

	@SuppressWarnings("rawtypes")
	public Collection<Path> extendPath(Path p) {
		Set<Path> out = new HashSet<>();
		Collection<IndividualPlus> ind = p.getLastInput();
		for (DLIndividual<?> s : dl.getInstances(dl.clazz(NS + "Service"))) {
			Collection<DLIndividual> ios = dl.getObjectPropertyValues(s,
					dl.objectProp(NS + "has_output"));
			for (DLIndividual<?> io : ios) {
				if (matchesOutput(ind, new IndividualPlus(io))) {
					Path np = p.copy();
					np.add(s);
					out.add(np);
				}
			}
		}
		if (out.size() == 0) {
			// try by two's
			List<DLIndividual> instances = new ArrayList<>(dl.getInstances(dl
					.clazz(NS + "Service")));
			List<List<DLIndividual<?>>> pairs = new ArrayList<>();
			for (int i = 0; i < instances.size(); i++) {
				for (int j = i + 1; j < instances.size(); j++) {
					pairs.add(Arrays.asList(new DLIndividual<?>[] {
							instances.get(i), instances.get(j) }));
				}
			}
			for (List<DLIndividual<?>> pair : pairs) {
				List<IndividualPlus> ips = new ArrayList<>();
				for (DLIndividual<?> dli : pair) {
					ips.add(new IndividualPlus(dli));
				}
				if (matchesOutput(ind, ips)) {
					Path np = p.copy();
					np.add(pair);
					out.add(np);
				}
			}

		}
		return out;
	}

	public boolean matchesOutput(IndividualPlus ind,
			Collection<IndividualPlus> output) {
		return matchesOutput(Arrays.asList(new IndividualPlus[] { ind }),
				output);
	}

	public boolean matchesOutput(Collection<IndividualPlus> ind,
			IndividualPlus output) {
		return matchesOutput(ind,
				Arrays.asList(new IndividualPlus[] { output }));
	}

	public boolean matchesOutput(IndividualPlus ind, IndividualPlus output) {
		return matchesOutput(Arrays.asList(new IndividualPlus[] { ind }),
				Arrays.asList(new IndividualPlus[] { output }));
	}

	public boolean matchesInput(IndividualPlus ind,
			Collection<IndividualPlus> input) {
		return matchesInput(Arrays.asList(new IndividualPlus[] { ind }), input);
	}

	public boolean matchesInput(IndividualPlus ind, IndividualPlus input) {
		return matchesInput(Arrays.asList(new IndividualPlus[] { ind }),
				Arrays.asList(new IndividualPlus[] { input }));
	}

	public boolean matchesInput(Collection<IndividualPlus> ind,
			Collection<IndividualPlus> input) {
		Map<IndividualPlus, DLAxiom<?>> adds = new HashMap<>();
		for (IndividualPlus ip1 : ind) {
			DLAxiom<?> ax = dl.individualType(ip1.getIndividual(),
					dl.clazz(NS + "ServiceInput"));
			adds.put(ip1, ax);
			ip1.getAxioms().add(ax);
		}
		boolean m = matches(
				ind,
				input,
				Arrays.asList(new String[] { NS + "ServiceOutput",
						NS + "ServiceInput" }));
		for (IndividualPlus ip : adds.keySet()) {
			ip.getAxioms().remove(adds.get(ip));
		}
		return m;
	}
	
	public boolean partMatchesInput(Collection<IndividualPlus> ind,
			Collection<IndividualPlus> input) {
		Map<IndividualPlus, DLAxiom<?>> adds = new HashMap<>();
		for (IndividualPlus ip1 : ind) {
			DLAxiom<?> ax = dl.individualType(ip1.getIndividual(),
					dl.clazz(NS + "ServiceInput"));
			adds.put(ip1, ax);
			ip1.getAxioms().add(ax);
		}
		boolean m = matchesLR(
				ind,
				input,
				Arrays.asList(new String[] { NS + "ServiceOutput",
						NS + "ServiceInput" }));
		for (IndividualPlus ip : adds.keySet()) {
			ip.getAxioms().remove(adds.get(ip));
		}
		return m;
	}

	// If it's a list of ind (ie conjunction), try assigning ind to type
	// conjunction of the type of has_output
	public boolean matchesOutput(Collection<IndividualPlus> ind,
			Collection<IndividualPlus> output) {
		Map<IndividualPlus, Set<DLAxiom<?>>> adds = new HashMap<>();
		List<DLClassExpression<?>> serviceOutputs = new ArrayList<>();
		for (IndividualPlus ip : output) {
			Collection<DLIndividual> vals = dl.getObjectPropertyValues(
					ip.getIndividual(), dl.objectProp(NS + "has_output"));
			for (DLIndividual<?> i : vals) {
				for (DLClassExpression<?> dce : dl.getTypes(i)) {
					serviceOutputs.add(dce);
				}
			}
		}
		for (IndividualPlus ip1 : output) {
			adds.put(ip1, new HashSet<DLAxiom<?>>());
			DLAxiom<?> ax1 = dl.individualType(ip1.getIndividual(),
					dl.clazz(NS + "ServiceOutput"));
			ip1.getAxioms().add(ax1);
			adds.get(ip1).add(ax1);
			for (DLClassExpression<?> so : serviceOutputs) {
				DLAxiom<?> axx = dl.individualType(ip1.getIndividual(), so);
				ip1.getAxioms().add(axx);
				adds.get(ip1).add(axx);
			}
		}
		boolean m = matches(
				ind,
				output,
				Arrays.asList(new String[] { NS + "ServiceOutput",
						NS + "ServiceInput" }));

		for (IndividualPlus ip : adds.keySet()) {
			ip.getAxioms().removeAll(adds.get(ip));
		}
		return m;
	}

	private boolean matches(Collection<IndividualPlus> i1,
			Collection<IndividualPlus> i2, List<String> classFilters) {
		// Both (I1 and !I2) and (!I1 and I2) clash
		return matchesLR(i1, i2, classFilters)
				&& matchesLR(i2, i1, classFilters);
	}

	private boolean matchesLR(Collection<IndividualPlus> i1,
			Collection<IndividualPlus> i2, List<String> classFilters) {
		// !I1 and I2 clash
		Set<DLAxiom<?>> ax = new HashSet<>();
		boolean matches = false;
		try {
			for (IndividualPlus ip1 : i1) {
				ax.addAll(ip1.getAxioms());
			}
			for (IndividualPlus ip2 : i2) {
				ax.addAll(ip2.getAxioms());
			}
			dl.addAxioms(ax);
			Set<DLClassExpression<?>> i1Outputs = new HashSet<>();
			Set<DLClassExpression<?>> i2Outputs = new HashSet<>();
			for (IndividualPlus ip1 : i1) {
				for (DLClassExpression<?> c : dl.getTypes(ip1.getIndividual())) {
					if (!classFilters.contains(dl.getIRI(c))) {
						i1Outputs.add(c);
					}
				}
			}
			for (IndividualPlus ip2 : i2) {
				for (DLClassExpression<?> c : dl.getTypes(ip2.getIndividual())) {
					if (!classFilters.contains(dl.getIRI(c))) {
						i2Outputs.add(c);
					}
				}
			}
			Set<DLAxiom<?>> adds = new HashSet<>();
			Set<DLAxiom<?>> drops = new HashSet<>();

			for (DLClassExpression<?> i2o : i2Outputs) {
				for (DLClassExpression<?> i1o : i1Outputs) {
					Collection<DLAxiom> axioms = dl.getAxioms();
					Set<DLAxiom<?>> ax1s = new HashSet<>();
					for (IndividualPlus ip1 : i1) {
						DLAxiom<?> ax1 = dl.individualType(ip1.getIndividual(),
								i2o);
						ax1s.add(ax1);
						if (!dl.containsAxiom(ax1)) {
							adds.add(ax1);
						}
					}
					Set<DLAxiom<?>> ax2s = new HashSet<>();
					for (IndividualPlus ip1 : i1) {
						DLAxiom<?> ax2 = dl.individualType(ip1.getIndividual(),
								dl.notClass(i1o));
						ax2s.add(ax2);
						if (!dl.containsAxiom(ax2)) {
							adds.add(ax2);
						}
					}
					for (IndividualPlus ip1 : i1) {
						DLAxiom<?> dropAx = dl.individualType(
								ip1.getIndividual(), i1o);
						if (!adds.contains(dropAx)) {
							// Make sure the "drop" is not the same as an
							// attempted
							// "add"
							if (!ax1s.contains(dropAx)
									&& !ax2s.contains(dropAx)) {
								drops.add(dropAx);
							}
						}
					}
				}
			}
			dl.removeAxioms(drops);
			dl.addAxioms(adds);

			debug();

			if (!dl.checkConsistency()) {
				dl.removeAxioms(adds);
				dl.addAxioms(drops);
				return true;
			}
			dl.removeAxioms(adds);
			dl.addAxioms(drops);

		} finally {
			dl.removeAxioms(ax);
		}
		return matches;
	}

	private void debug() {
		try {
			dl.setOutputFile(new File(
					"/home/matt/sw/abfab-integration-output.owl"));
			dl.saveOntology();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Path chooseBestPath(Set<Path> paths) {
		double bestScore = Double.MAX_VALUE;
		Path bestPath = null;
		for (Path p : paths) {
			if (p.getCost() <= bestScore) {
				bestPath = p;
				bestScore = p.getCost();
			}
		}

		return bestPath;
	}

	public Map<DLClassExpression<?>, Path> getPathCache() {
		return goalPathCache;
	}

	public void setPathCache(Map<DLClassExpression<?>, Path> pathCache) {
		this.goalPathCache = pathCache;
	}

	public Collection<DLIndividual> getTerminals(
			DLClassExpression<?> desiredClass) {
		Set<DLIndividual> out = new HashSet<>();
		Collection<DLIndividual> serviceInputs = dl.getInstances(dl.clazz(NS
				+ "ServiceOutput"));
		Set<DLIndividual<?>> acceptableOutputs = new HashSet<>();
		for (DLIndividual<?> dci : serviceInputs) {
			Set<DLAxiom<?>> ax = new HashSet<>();
			try {

				Set<DLClassExpression<?>> ces = new HashSet<>();
				for (DLClassExpression<?> ce : dl.getTypes(dci)) {
					if (!dl.getIRI(ce).equals(NS + "ServiceOutput")) {
						ces.add(ce);
					}
				}

				ax.add(dl.newIndividual(NS + "testOutput",
						dl.clazz(NS + "ServiceOutput")));

				for (DLClassExpression<?> ce : ces) {
					ax.add(dl.individualType(dl.individual(NS + "testOutput"),
							ce));
				}

				ax.add(dl.individualType(dl.individual(NS + "testOutput"),
						dl.notClass(desiredClass)));

				dl.addAxioms(ax);

				if (!dl.checkConsistency()) {
					acceptableOutputs.add(dci);
				}

			} finally {
				dl.removeAxioms(ax);
			}
		}

		for (DLIndividual<?> i : acceptableOutputs) {
			Collection<DLIndividual> services = dl.getHavingPropertyValue(
					dl.clazz(NS + "Service"), dl.objectProp(NS + "has_output"),
					i);
			out.addAll(services);
		}

		return out;
	}
}
