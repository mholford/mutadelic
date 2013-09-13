package edu.yale.abfab.v2;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.yale.abfab.v2.Path;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClassExpression;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.controller.DLController;

import static edu.yale.abfab.v2.NS.*;

public abstract class Abductor {

	private DLController dl;
	private Map<DLClassExpression<?>, Path> goalPathCache;
	private String namespace;

	public Abductor() {
		dl = initDLContoller();
		goalPathCache = new HashMap<>();
	}

	public abstract DLController initDLContoller();

	public DLController getDLController() {
		return dl;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public Path getBestPath(IndividualPlus input, DLClassExpression<?> goalClass) {
		if (goalPathCache.containsKey(goalClass)) {
			return goalPathCache.get(goalClass);
		}

		Set<DLAxiom<?>> ax = new HashSet<>();
		ax.add(dl.individualType(dl.individual("test"), goalClass));
		IndividualPlus i = new IndividualPlus(dl.individual("Test"), ax);

		Path p = getBestPath(input, i);

		goalPathCache.put(goalClass, p);

		return p;
	}

	public Path getBestPath(IndividualPlus origInput, IndividualPlus goalI) {
		Set<Path> completedPaths = new HashSet<>();

		Set<Path> paths = extendPath(null, goalI);

		for (;;) {
			Set<Path> nextPaths = new HashSet<>();
			for (Path p : paths) {
				if (matchesInput(origInput, p)) {
					completedPaths.add(p);
				} else {
					nextPaths.addAll(extendPath(p, p.getLastInput()));
				}
			}
			if (nextPaths.size() == 0) {
				break;
			}
			paths = nextPaths;
		}

		return chooseBestPath(completedPaths);
	}
	
	public boolean matchesInput(IndividualPlus i, Path p) {
		
	}

	public Collection<Collection<IndividualPlus>> findServiceOutputMatch(
			IndividualPlus i) {
		Set<Collection<IndividualPlus>> output = new HashSet<>();

		for (DLClassExpression<?> serviceClass : dl
				.getSubclasses(dl.clazz(NS + "Service"))) {
			IndividualPlus serviceI = new IndividualPlus(dl.individual(NS
					+ "testService"));
			serviceI.getAxioms().add(
					dl.individualType(dl.individual(NS + "testService"),
							dl.clazz(NS + "Service")));
			for (DLIndividual<?> serviceClassI : dl.getInstances(serviceClass)) {
				Collection<DLIndividual> serviceClassInputs = dl.getObjectPropertyValues(serviceClassI,
						dl.objectProp(NS + "has_input"));
				for (DLIndividual<?> serviceClassInput : serviceClassInputs) {
					Set<DLAxiom<?>> ax = new HashSet<>();
					ax.add(dl.newObjectFact(serviceI.getIndividual(),
							dl.objectProp(NS + "has_input"), serviceClassInput));
					ax.add(dl.newObjectFact(serviceI.getIndividual(),
							dl.objectProp(NS + "has_output"), i.getIndividual()));
					dl.addAxioms(ax);
					if (dl.checkEntailed(dl.individualType(serviceI.getIndividual(), serviceClass))) {
						output.add(Arrays.asList(new IndividualPlus[] { serviceI }));
					}
					dl.removeAxioms(ax);
				}
			}
		}

		return output;
	}

	public Set<Path> extendPath(Path p, IndividualPlus goalI) {
		Set<IndividualPlus> gis = new HashSet<>();
		gis.add(goalI);
		return extendPath(p, gis);
	}

	public Set<Path> extendPath(Path p, Collection<IndividualPlus> goalI) {
		Set<Path> output = new HashSet<>();
		for (IndividualPlus gi : goalI) {
			Collection<Collection<IndividualPlus>> services = findServiceOutputMatch(gi);
			for (Collection<IndividualPlus> service : services) {
				Path np = p.copy();
				np.add(service);
			}
		}
		return output;
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

}
