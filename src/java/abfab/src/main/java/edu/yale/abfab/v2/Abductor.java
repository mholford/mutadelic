package edu.yale.abfab.v2;

import java.io.File;
import java.io.IOException;
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
		dl = initDLController();
		goalPathCache = new HashMap<>();
	}

	public abstract DLController initDLController();

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
		ax.add(dl.individualType(dl.individual(NS + "Test"), goalClass));
		IndividualPlus i = new IndividualPlus(dl.individual(NS + "Test"), ax);

		Path p = getBestPath(input, i);

		goalPathCache.put(goalClass, p);

		return p;
	}

	public Path getBestPath(IndividualPlus origInput, IndividualPlus goalI) {
		Set<Path> completedPaths = new HashSet<>();

		Set<Path> paths = extendPath(null, origInput, goalI);

		for (;;) {
			Set<Path> nextPaths = new HashSet<>();
			for (Path p : paths) {
				if (matchesInput(origInput, p)) {
					completedPaths.add(p);
				} else {
					nextPaths
							.addAll(extendPath(p, origInput, p.getLastInput()));
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
		try {
			dl.addAxioms(i.getAxioms());
			// Get Class representing top step of the Path
			Collection<DLClassExpression> pathTopClasses = p
					.getTopStepDLClasses();
			// Cast a new individual as a member of that class
			IndividualPlus testServiceI = new IndividualPlus(dl.individual(NS
					+ "testService"));
			// Set the output of the new individual to existing output for
			// instance
			// of the class
			for (DLClassExpression<?> pathTopClass : pathTopClasses) {
				for (DLIndividual<?> pathTopClassI : dl
						.getInstances(pathTopClass)) {
					Collection<DLIndividual> pathTopClassOutputs = dl
							.getObjectPropertyValues(pathTopClassI,
									dl.objectProp(NS + "has_output"));
					for (DLIndividual<?> pathTopClassOutput : pathTopClassOutputs) {
						Set<DLAxiom<?>> ax = new HashSet<>();
						ax.add(dl.newObjectFact(testServiceI.getIndividual(),
								dl.objectProp(NS + "has_output"),
								pathTopClassOutput));
						// Set the input of the new individual to the individual
						// we are checking
						ax.add(dl.newObjectFact(testServiceI.getIndividual(),
								dl.objectProp(NS + "has_input"),
								i.getIndividual()));
						ax.add(dl.individualType(testServiceI.getIndividual(),
								dl.notClass(pathTopClass)));
						dl.addAxioms(ax);
						debug();
						// Check entailed return if entailed
						if (!dl.checkConsistency()) {
							return true;
						}
						dl.removeAxioms(ax);
					}
				}
			}
		} finally {
			dl.removeAxioms(i.getAxioms());
		}

		return false;
	}

	public Collection<Collection<IndividualPlus>> findServiceOutputMatch(
			IndividualPlus i) {
		Set<Collection<IndividualPlus>> output = new HashSet<>();

		try {
			dl.addAxioms(i.getAxioms());

			for (DLClassExpression<?> serviceClass : dl.getSubclasses(dl
					.clazz(NS + "Service"))) {
				IndividualPlus serviceI = new IndividualPlus(dl.individual(NS
						+ "testService"));
				serviceI.getAxioms().add(
						dl.individualType(dl.individual(NS + "testService"),
								dl.clazz(NS + "Service")));
				for (DLIndividual<?> serviceClassI : dl
						.getInstances(serviceClass)) {
					Collection<DLIndividual> serviceClassInputs = dl
							.getObjectPropertyValues(serviceClassI,
									dl.objectProp(NS + "has_input"));
					for (DLIndividual<?> serviceClassInput : serviceClassInputs) {
						Set<DLAxiom<?>> ax = new HashSet<>();
						ax.add(dl.newObjectFact(serviceI.getIndividual(),
								dl.objectProp(NS + "has_input"),
								serviceClassInput));
						ax.add(dl.newObjectFact(serviceI.getIndividual(),
								dl.objectProp(NS + "has_output"),
								i.getIndividual()));
						ax.add(dl.individualType(serviceI.getIndividual(),
								dl.notClass(serviceClass)));
						dl.addAxioms(ax);
						debug();
						boolean add = false;
						if (!dl.checkConsistency()) {
							add = true;
						}
						dl.removeAxioms(ax);

						if (add) {
							ax = new HashSet<>();
							for (DLClassExpression<?> serviceClassInputClass : dl
									.getTypes(serviceClassInput)) {
								for (DLClassExpression<?> iClass : dl
										.getTypes(i.getIndividual())) {
									ax.add(dl.equiv(dl.clazz(NS + "TestC"), dl
											.andClass(dl.some(
													dl.objectProp(NS
															+ "has_input"),
													serviceClassInputClass), dl
													.some(dl.objectProp(NS
															+ "has_output"),
															iClass))));
								}
							}
							ax.add(dl.individualType(
									dl.individual(NS + "testI"), serviceClass));
							ax.add(dl.individualType(
									dl.individual(NS + "testI"),
									dl.notClass(dl.clazz(NS + "TestC"))));
							dl.addAxioms(ax);

							add = !dl.checkConsistency();
							debug();
							dl.removeAxioms(ax);
						}

						if (add) {
							output.add(Arrays
									.asList(new IndividualPlus[] { new IndividualPlus(
											serviceClassI) }));
						}
					}
				}
			}
		} finally {
			dl.removeAxioms(i.getAxioms());
		}
		return output;
	}

	public Set<Path> extendPath(Path p, IndividualPlus origInput,
			IndividualPlus goalI) {
		Set<IndividualPlus> gis = new HashSet<>();
		gis.add(goalI);
		return extendPath(p, origInput, gis);
	}

	public Set<Path> extendPath(Path p, IndividualPlus origInput,
			Collection<IndividualPlus> goalI) {
		Set<Path> output = new HashSet<>();
		for (IndividualPlus gi : goalI) {
			Collection<Collection<IndividualPlus>> services = findServiceOutputMatch(gi);
			for (Collection<IndividualPlus> service : services) {
				Path np = p != null ? p.copy() : new Path(origInput, this);
				np.add(service);
				output.add(np);
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

	private void debug() {
		try {
			dl.setOutputFile(new File(
					"/home/matt/sw/abfab-integration-output.owl"));
			dl.saveOntology();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
