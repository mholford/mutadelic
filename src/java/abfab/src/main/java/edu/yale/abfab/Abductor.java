package edu.yale.abfab;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sun.security.jca.ServiceId;

import edu.yale.abfab.Path;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClassExpression;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.DLObjectPropertyExpression;
import edu.yale.dlgen.controller.DLController;

import static edu.yale.abfab.NS.*;

public abstract class Abductor {

	private DLController dl;
	private Map<DLClassExpression<?>, Path> goalPathCache;
	private String namespace;
	private Path executingPath;
	private DLObjectPropertyExpression<?> HAS_INPUT;
	private DLObjectPropertyExpression<?> HAS_OUTPUT;

	public Abductor() {
		dl = initDLController();
		goalPathCache = new HashMap<>();
		HAS_INPUT = dl.objectProp(NS + "has_input");
		HAS_OUTPUT = dl.objectProp(NS + "has_output");
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

	// public Path getBestPath(IndividualPlus input, DLClassExpression<?>
	// goalClass) {
	// if (goalPathCache.containsKey(goalClass)) {
	// return goalPathCache.get(goalClass);
	// }
	//
	// Set<DLAxiom<?>> ax = new HashSet<>();
	// ax.add(dl.individualType(dl.individual(NS + "Test"), goalClass));
	// IndividualPlus i = new IndividualPlus(dl.individual(NS + "Test"), ax);
	//
	// Path p = getBestPath(input, i);
	//
	// goalPathCache.put(goalClass, p);
	//
	// return p;
	// }

	public Set<Path> getAllPaths(IndividualPlus origInput,
			DLClassExpression<?> goalClass) {
		Set<Path> completedPaths = new HashSet<>();

		// Set<Path> paths = extendPath(null, origInput, goalI);
		Set<Path> paths = initializePaths(origInput, goalClass);

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
		return completedPaths;
	}

	public Path getBestPath(IndividualPlus origInput,
			DLClassExpression<?> goalClass) {
		Set<Path> completedPaths = getAllPaths(origInput, goalClass);

		return chooseBestPath(completedPaths);
	}

	public Set<Path> initializePaths(IndividualPlus input,
			DLClassExpression<?> goalClass) {
		Set<Path> paths = new HashSet<>();

		Collection<DLIndividual<?>> terminals = findTerminals(goalClass);
		for (DLIndividual<?> terminal : terminals) {
			Path p = new Path(input, this);
			p.add(terminal);
			paths.add(p);
		}

		return paths;
	}

	public Collection<DLIndividual<?>> findTerminals(
			DLClassExpression<?> goalClass) {
		Set<DLIndividual<?>> terminals = new HashSet<>();

		for (DLClassExpression<?> serviceClass : dl.getSubclasses(dl.clazz(NS
				+ "Service"))) {
			for (DLIndividual<?> serviceClassI : dl.getInstances(serviceClass)) {
				boolean add = false;
				Set<DLAxiom<?>> ax = new HashSet<>();

				DLIndividual<?> testService = dl.individual(NS + "testService");
				ax.add(dl.individualType(testService, dl.thing()));

				for (DLIndividual<?> sciInput : dl.getObjectPropertyValues(
						serviceClassI, HAS_INPUT)) {
					ax.add(dl.newObjectFact(testService, HAS_INPUT, sciInput));
				}

				DLIndividual<?> testOutput = dl.individual(NS + "testOutput");

				ax.add(dl.individualType(testOutput, goalClass));
				ax.add(dl.newObjectFact(testService, HAS_OUTPUT, testOutput));

				ax.add(dl.individualType(testService, dl.notClass(serviceClass)));
				dl.addAxioms(ax);
				debug();
				if (!dl.checkConsistency()) {
					add = true;
				}
				dl.removeAxioms(ax);

				if (add) {

					Collection<DLIndividual> sciOutputs = dl
							.getObjectPropertyValues(serviceClassI, HAS_OUTPUT);
					for (DLIndividual<?> sciOutput : sciOutputs) {
						ax = new HashSet<>();
						DLClassExpression<?> scioType = dl
								.getIntersectingType(sciOutput);
						DLIndividual<?> testI = dl.individual(NS + "testI");
						ax.add(dl.individualType(testI, scioType));
						ax.add(dl.individualType(testI, dl.notClass(goalClass)));
						dl.addAxioms(ax);
						debug();
						add = !dl.checkConsistency();
						dl.removeAxioms(ax);
					}
				}

				if (add) {
					terminals.add(serviceClassI);
				}
			}
		}

		return terminals;
	}

	public boolean matchesInput(IndividualPlus input, Path p) {
		Set<DLAxiom<?>> ax = new HashSet<>();

		try {
			ax.addAll(input.getAxioms());

			DLClassExpression topClass = p.getTopStepUnifiedClass();
			DLIndividual<?> testService = dl.individual(NS + "testService");
			ax.add(dl.individualType(testService, dl.thing()));

			for (IndividualPlus tsOutput : p.getLastOutput()) {
				ax.addAll(tsOutput.getAxioms());
				ax.add(dl.newObjectFact(testService, HAS_OUTPUT,
						tsOutput.getIndividual()));
			}
			ax.add(dl.newObjectFact(testService, HAS_INPUT,
					input.getIndividual()));
			ax.add(dl.individualType(testService, dl.notClass(topClass)));
			dl.addAxioms(ax);
			debug();
			if (!dl.checkConsistency()) {
				return true;
			}
			return false;
		} finally {
			dl.removeAxioms(ax);
		}
	}

	// public boolean matchesInput(IndividualPlus i, Path p) {
	// boolean matches = false;
	// try {
	// dl.addAxioms(i.getAxioms());
	// // Get Class representing top step of the Path
	// Collection<DLClassExpression> pathTopClasses = p
	// .getTopStepDLClasses();
	//
	// Collection<DLClassExpression> pathNextClasses = p
	// .getNextStepDLClasses();
	// // Cast a new individual as a member of that class
	//
	// // Set the output of the new individual to existing output for
	// // instance
	// // of the class
	// for (DLClassExpression<?> pathTopClass : pathTopClasses) {
	// matches = matchInputToPathClass(i, pathTopClass,
	// pathNextClasses);
	// }
	// } finally {
	// dl.removeAxioms(i.getAxioms());
	// }
	//
	// return matches;
	// }

	// public boolean matchInputToPathClass(IndividualPlus i,
	// DLClassExpression<?> pathTopClass,
	// Collection<DLClassExpression> targetServiceClasses) {
	// boolean matches = false;
	// IndividualPlus testServiceI = new IndividualPlus(dl.individual(NS
	// + "testService"));
	// for (DLIndividual<?> pathTopClassI : dl.getInstances(pathTopClass)) {
	// Collection<DLIndividual> pathTopClassOutputs = dl
	// .getObjectPropertyValues(pathTopClassI,
	// dl.objectProp(NS + "has_output"));
	// for (DLIndividual<?> pathTopClassOutput : pathTopClassOutputs) {
	// matches = serviceClassMatches(i, pathTopClass,
	// targetServiceClasses, testServiceI, pathTopClassOutput,
	// dl.objectProp(NS + "has_output"),
	// dl.objectProp(NS + "has_input"));
	// }
	// }
	// return matches;
	// }

	public Collection<Collection<IndividualPlus>> findServiceOutputMatch(
			IndividualPlus i, Collection<DLClassExpression> targetServiceClasses) {
		Set<Collection<IndividualPlus>> output = new HashSet<>();

		try {
			dl.addAxioms(i.getAxioms());

			for (DLClassExpression<?> serviceClass : dl.getSubclasses(dl
					.clazz(NS + "Service"))) {
				IndividualPlus serviceI = new IndividualPlus(dl.individual(NS
						+ "testService"));
				// serviceI.getAxioms().add(
				// dl.individualType(dl.individual(NS + "testService"),
				// dl.clazz(NS + "Service")));
				for (DLIndividual<?> serviceClassI : dl
						.getInstances(serviceClass)) {
					Collection<DLIndividual> serviceClassInputs = dl
							.getObjectPropertyValues(serviceClassI,
									dl.objectProp(NS + "has_input"));
					for (DLIndividual<?> serviceClassInput : serviceClassInputs) {
						boolean add = serviceClassMatches(i, serviceClass,
								targetServiceClasses, serviceI,
								serviceClassInput,
								dl.objectProp(NS + "has_input"),
								dl.objectProp(NS + "has_output"));

						if (add) {
							output.add(Arrays
									.asList(new IndividualPlus[] { new IndividualPlus(
											serviceClassI) }));
						}
					}
				}
			}

			if (output.size() == 0) {
				debug();
				Collection<DLAxiom> axioms = dl.getAxioms();
				List<DLClassExpression> serviceClasses = new ArrayList<>(
						dl.getSubclasses(dl.clazz(NS + "Service")));
				for (int n = 2; n <= serviceClasses.size(); n++) {
					Set<Set<DLClassExpression>> serviceClassTuples = Utils
							.getNTuplePermutations(serviceClasses, n);

					for (Set<DLClassExpression> serviceClassTuple : serviceClassTuples) {
						IndividualPlus serviceI = new IndividualPlus(
								dl.individual(NS + "testService"));

						// Get distinct n-tuples of service class instances
						// where n
						// is size of "pair"
						Map<Integer, Collection<DLIndividual>> serviceClassMap = new HashMap<>();
						int cnt = 0;
						for (DLClassExpression<?> serviceClass : serviceClassTuple) {
							Collection<DLIndividual> serviceClassIList = dl
									.getInstances(serviceClass);
							serviceClassMap.put(cnt, serviceClassIList);
							cnt++;
						}
						cnt = 0;
						List<List<IndividualPlus>> prevLists = new ArrayList<>();
						for (Integer c : serviceClassMap.keySet()) {
							List<List<IndividualPlus>> newLists = new ArrayList<>();
							Collection<DLIndividual> serviceClassIList = serviceClassMap
									.get(c);
							if (prevLists.size() == 0) {
								for (DLIndividual serviceClassI : serviceClassIList) {
									List<IndividualPlus> newIList = new ArrayList<>();
									newIList.add(new IndividualPlus(
											serviceClassI));
									newLists.add(newIList);
								}
							} else {
								for (List<IndividualPlus> prevList : prevLists) {
									for (DLIndividual serviceClassI : serviceClassIList) {
										List<IndividualPlus> newIList = new ArrayList<>();
										newIList.addAll(prevList);
										newIList.add(new IndividualPlus(
												serviceClassI));
										newLists.add(newIList);
									}
								}
							}
							prevLists = newLists;
						}
						List<List<IndividualPlus>> serviceClassIPairs = prevLists;

						for (List<IndividualPlus> serviceClassIPair : serviceClassIPairs) {

							// Get distinct n-tuples of serviceClassInputs where
							// n
							// is size of "pair"
							Map<Integer, Collection<DLIndividual>> serviceClassInputMap = new HashMap<>();
							cnt = 0;
							for (IndividualPlus serviceClassI : serviceClassIPair) {
								Collection<DLIndividual> serviceClassInputList = dl
										.getObjectPropertyValues(
												serviceClassI.getIndividual(),
												dl.objectProp(NS + "has_input"));
								serviceClassInputMap.put(cnt,
										serviceClassInputList);
								cnt++;
							}
							cnt = 0;
							List<List<DLIndividual>> prevLists2 = new ArrayList<>();
							for (Integer c : serviceClassInputMap.keySet()) {
								List<List<DLIndividual>> newLists = new ArrayList<>();
								Collection<DLIndividual> serviceClassInputList = serviceClassInputMap
										.get(c);
								if (prevLists2.size() == 0) {
									for (DLIndividual serviceClassInput : serviceClassInputList) {
										List<DLIndividual> newInputList = new ArrayList<>();
										newInputList.add(serviceClassInput);
										newLists.add(newInputList);
									}
								} else {
									for (List<DLIndividual> prevList : prevLists2) {
										for (DLIndividual serviceClassInput : serviceClassInputList) {
											List<DLIndividual> newInputList = new ArrayList<>();
											newInputList.addAll(prevList);
											newInputList.add(serviceClassInput);
											newLists.add(newInputList);
										}
									}
								}
								prevLists2 = newLists;
							}

							List<List<DLIndividual>> serviceClassInputPairs = prevLists2;

							for (List<DLIndividual> serviceClassInputPair : serviceClassInputPairs) {
								debug();
								boolean add = serviceClassMatches(i,
										serviceClassTuple, targetServiceClasses,
										serviceI, serviceClassInputPair,
										dl.objectProp(NS + "has_input"),
										dl.objectProp(NS + "has_output"));
								if (add) {
									List<IndividualPlus> outputsToAdd = new ArrayList<>();
									for (IndividualPlus s : serviceClassIPair) {
										for (DLIndividual<?> soutput : dl
												.getObjectPropertyValues(
														s.getIndividual(),
														dl.objectProp(NS
																+ "has_output"))) {
											outputsToAdd
													.add(new IndividualPlus(
															soutput));
										}
									}
									// output.add(serviceClassIPair);
									output.add(outputsToAdd);
								}
							}
						}

					}
				}
			}
		} finally {
			dl.removeAxioms(i.getAxioms());
		}
		return output;
	}

	private boolean serviceClassMatches(IndividualPlus testI,
			DLClassExpression serviceClass,
			Collection<DLClassExpression> nextServiceClass,
			IndividualPlus serviceClassI, DLIndividual<?> serviceClassIFiller,
			DLObjectPropertyExpression<?> propToKeep,
			DLObjectPropertyExpression<?> propToReplace) {
		return serviceClassMatches(testI,
				Arrays.asList(new DLClassExpression[] { serviceClass }),
				nextServiceClass, serviceClassI,
				Arrays.asList(new DLIndividual[] { serviceClassIFiller }),
				propToKeep, propToReplace);
	}

	private boolean serviceClassMatches(IndividualPlus testI,
			Collection<DLClassExpression> serviceClasses,
			Collection<DLClassExpression> nextServiceClasses,
			IndividualPlus serviceClassI,
			Collection<DLIndividual> serviceClassIFillers,
			DLObjectPropertyExpression<?> propToKeep,
			DLObjectPropertyExpression<?> propToReplace) {
		Set<DLAxiom<?>> ax = new HashSet<>();
		for (DLIndividual<?> serviceClassIFiller : serviceClassIFillers) {
			ax.add(dl.newObjectFact(serviceClassI.getIndividual(), propToKeep,
					serviceClassIFiller));
		}
		ax.add(dl.newObjectFact(serviceClassI.getIndividual(), propToReplace,
				testI.getIndividual()));

		DLClassExpression<?> serv;
		if (serviceClasses.size() > 1) {
			serv = dl.andClass(serviceClasses
					.toArray(new DLClassExpression<?>[serviceClasses.size()]));
		} else {
			serv = serviceClasses.iterator().next();
		}
		ax.add(dl.individualType(serviceClassI.getIndividual(),
				dl.notClass(serv)));
		dl.addAxioms(ax);
		debug();
		boolean add = false;
		if (!dl.checkConsistency()) {
			add = true;
		}
		dl.removeAxioms(ax);

		if (add && nextServiceClasses != null) {
			Set<DLIndividual<?>> keepers = new HashSet<>();
			for (DLClassExpression<?> serviceClass : serviceClasses) {
				for (DLIndividual<?> sci : dl.getInstances(serviceClass)) {
					for (DLIndividual<?> k : dl.getObjectPropertyValues(sci,
							propToReplace)) {
						keepers.add(k);
					}
				}
			}
			Set<DLAxiom<?>> ax2 = new HashSet<>();
			for (DLClassExpression<?> nextServiceClass : nextServiceClasses) {
				for (DLIndividual<?> nextServiceClassI : dl
						.getInstances(nextServiceClass)) {
					for (DLIndividual<?> replacer : dl.getObjectPropertyValues(
							nextServiceClassI, propToReplace)) {
						if (keepers.size() > 1) {
							Set<DLClassExpression> keeperClasses = new HashSet<>();
							for (DLIndividual<?> keeper : keepers) {
								keeperClasses.addAll(dl.getTypes(keeper));
							}
							DLClassExpression keeperClass = dl
									.andClass(keeperClasses
											.toArray(new DLClassExpression[keeperClasses
													.size()]));
							DLIndividual<?> keeperI = dl.individual(NS
									+ "keeperI");
							ax2.add(dl.individualType(keeperI, keeperClass));
							ax2.add(dl.newObjectFact(
									serviceClassI.getIndividual(), propToKeep,
									keeperI));

						} else {
							ax2.add(dl.newObjectFact(
									serviceClassI.getIndividual(), propToKeep,
									keepers.iterator().next()));
						}

						ax2.add(dl.newObjectFact(serviceClassI.getIndividual(),
								propToReplace, replacer));
						if (nextServiceClasses.size() > 1) {
							serv = dl
									.andClass(nextServiceClasses
											.toArray(new DLClassExpression<?>[nextServiceClasses
													.size()]));
						} else {
							serv = nextServiceClasses.iterator().next();
						}
						ax2.add(dl.individualType(
								serviceClassI.getIndividual(),
								dl.notClass(serv)));
						dl.addAxioms(ax2);
						debug();
						if (dl.checkConsistency()) {
							add = false;
						}
						dl.removeAxioms(ax2);
						if (!add) {
							break;
						}

					}
				}
			}
		}
		// if (add && serviceClasses.size() == 1) {
		// ax = new HashSet<>();
		// for (DLIndividual<?> serviceClassIFiller : serviceClassIFillers) {
		// for (DLClassExpression<?> serviceClassInputClass : dl
		// .getTypes(serviceClassIFiller)) {
		// for (DLClassExpression<?> iClass : dl.getTypes(testI
		// .getIndividual())) {
		// ax.add(dl.newClazz(dl.clazz(NS + "TestC")));
		// ax.add(dl.equiv(dl.clazz(NS + "TestC"), dl.andClass(
		// dl.some(propToKeep, serviceClassInputClass),
		// dl.some(propToReplace, iClass))));
		// }
		// }
		// }
		// for (DLClassExpression<?> serviceClass : serviceClasses) {
		// ax.add(dl.individualType(dl.individual(NS + "testI"),
		// serviceClass));
		// }
		// ax.add(dl.individualType(dl.individual(NS + "testI"),
		// dl.notClass(dl.clazz(NS + "TestC"))));
		// dl.addAxioms(ax);
		//
		// add = !dl.checkConsistency();
		// debug();
		// dl.removeAxioms(ax);
		// }
		return add;
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
			Collection<DLClassExpression> topStepDLClasses = p != null ? p
					.getTopStepDLClasses() : null;
			Collection<Collection<IndividualPlus>> services = findServiceOutputMatch(
					gi, topStepDLClasses);

			Path np = p != null ? p.copy() : new Path(origInput, this);
			np.add(services);
			output.add(np);
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

	public void debug() {
		try {
			dl.setOutputFile(new File(
					"/home/matt/sw/abfab-integration-output.owl"));
			dl.saveOntology();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
