package edu.yale.abfab;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
	private String pkg;
	private Map<DLClassExpression<?>, Path> goalPathCache;
	private Map<DLIndividual<?>, Path> servicePathCache;
	private Set<DLAxiom<?>> dummyAxioms;
	private List<Set<DLAxiom<?>>> mergedIndivAxioms;
	private int mergedCount;
	private int dummyCount;
	private String namespace;
	private String NS;

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

	public Collection<Path> extendPath(Path p) {
		Set<Path> out = new HashSet<>();
		IndividualPlus i = p.getLastInput();
		for (DLIndividual<?> s : dl.getInstances(dl.clazz(NS + "Service"))) {
			Collection<DLIndividual> ios = dl.getObjectPropertyValues(s,
					dl.objectProp(NS + "has_output"));
			for (DLIndividual<?> io : ios) {
				if (matchesOutput(i, new IndividualPlus(io))) {
					Path np = p.copy();
					np.add(s);
					out.add(np);
				}
			}
		}
		return out;
	}

	public boolean matchesInput(IndividualPlus ind, IndividualPlus input) {
		ind.getAxioms().add(
				dl.individualType(ind.getIndividual(),
						dl.clazz(NS + "ServiceInput")));
		return matchesToClass(ind, input, NS + "ServiceInput");
	}

	public boolean matchesOutput(IndividualPlus ind, IndividualPlus output) {
		ind.getAxioms().add(
				dl.individualType(ind.getIndividual(),
						dl.clazz(NS + "ServiceOutput")));
		return matchesToClass(ind, output, NS + "ServiceOutput");
	}

	private boolean matchesToClass(IndividualPlus ind, IndividualPlus ind2,
			String className) {
		Set<DLAxiom<?>> ax = new HashSet<>();
		try {
			ax.addAll(ind.getAxioms());
			ax.addAll(ind2.getAxioms());
			dl.addAxioms(ax);
			Set<DLClassExpression<?>> outputs = new HashSet<>();
			for (DLClassExpression<?> c : dl.getTypes(ind2.getIndividual())) {
				if (!dl.getIRI(c).equals(className)) {
					outputs.add(c);
				}
			}
			for (DLClassExpression<?> c : outputs) {
				DLAxiom<?> testAx = dl.individualType(ind.getIndividual(),
						dl.notClass(c));
				dl.addAxiom(testAx);
				if (!dl.checkConsistency()) {
					dl.removeAxiom(testAx);
					return true;
				}
				dl.removeAxiom(testAx);
			}
		} finally {
			dl.removeAxioms(ax);
		}
		return false;
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

//				if (dl.getIRI(dci).equals(NS + "FINO")) {
//					try {
//						dl.setOutputFile(new File(
//								"/home/matt/sw/abfab-integration-output.owl"));
//						dl.saveOntology();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
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
