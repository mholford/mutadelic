package edu.yale.abfab;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
	private boolean debugFine = true;
	private Map<ServiceOutputMatchCacheKey, Collection<Collection<IndividualPlus>>> serviceOutputMatchCache;

	public Abductor() {
		dl = initDLController();
		goalPathCache = new HashMap<>();
		HAS_INPUT = dl.objectProp(NS + "has_input");
		HAS_OUTPUT = dl.objectProp(NS + "has_output");
		serviceOutputMatchCache = new HashMap<>();
	}

	public enum MatchStatus {
		FULL, PARTIAL, NONE
	}

	class ServiceOutputMatchCacheKey {
		IndividualPlus indiv;
		Collection<DLClassExpression> targetClasses;

		public ServiceOutputMatchCacheKey(IndividualPlus indiv,
				Collection<DLClassExpression> targetClasses) {
			this.indiv = indiv;
			this.targetClasses = targetClasses;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((indiv == null) ? 0 : indiv.hashCode());
			result = prime * result
					+ ((targetClasses == null) ? 0 : targetClasses.hashCode());
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
			ServiceOutputMatchCacheKey other = (ServiceOutputMatchCacheKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (indiv == null) {
				if (other.indiv != null)
					return false;
			} else if (!indiv.equals(other.indiv))
				return false;
			if (targetClasses == null) {
				if (other.targetClasses != null)
					return false;
			} else if (!targetClasses.equals(other.targetClasses))
				return false;
			return true;
		}

		private Abductor getOuterType() {
			return Abductor.this;
		}
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

	private void dbg(String s, Object... args) {
		if (debugFine) {
			System.out.println(String.format(s, args));
		}
	}

	public Set<Path> getAllPaths(IndividualPlus origInput,
			DLClassExpression<?> goalClass) {
		dbg("Get All Paths: %s, %s", origInput, goalClass);
		Set<Path> completedPaths = new HashSet<>();

		// Set<Path> paths = extendPath(null, origInput, goalI);
		Set<Path> paths = initializePaths(origInput, goalClass);
		dbg("Initial Paths: %s", paths);

		for (;;) {
			Set<Path> nextPaths = new HashSet<>();
			for (Path p : paths) {
				if (matchesInput(origInput, p)) {
					Path merged = mergeBranches(p);
					completedPaths.add(merged);
					dbg("Path %d(%s) complete", p.hashCode(), merged.toString());
				} else {
					Set<Path> eps = extendPath(p, origInput, p.getLastInput());
					for (Path ep : eps) {
						dbg("Extend Path %d to %d(%s)", p.hashCode(),
								ep.hashCode(), ep.toString());
					}
					nextPaths.addAll(eps);
				}
			}
			if (nextPaths.size() == 0) {
				break;
			}
			paths = nextPaths;
		}

		return completedPaths;
	}

	public Path mergeBranches(Path input) {
		Path output = new Path(input.getInitialInput(), input.getAbductor());
		List<Step> reverseSteps = new ArrayList<>();
		for (Step s : input.getSteps()) {
			reverseSteps.add(s.copy());
		}
		Collections.reverse(reverseSteps);
		Iterator<Step> mainIter = reverseSteps.iterator();
		while (mainIter.hasNext()) {
			Step s = mainIter.next();
			if (!(s instanceof Branch)) {
				output.getSteps().add(0, s);
			} else {
				Branch b = (Branch) s;
				List<Iterator<Step>> piters = new ArrayList<>();
				List<Path> newPaths = new ArrayList<>();
				for (Path p : b.getPaths()) {
					piters.add(p.getSteps().iterator());
					newPaths.add(p.copy());
				}
				boolean done = false;
				DLIndividual<?> serviceToAdd = null;
				int addPos = 0;
				while (!done) {
					for (Iterator<Step> piter : piters) {
						if (piter.hasNext()) {
							Step step = piter.next();
							if (step instanceof SimpleStep) {
								DLIndividual<?> service = ((SimpleStep) step)
										.getService();
								if (serviceToAdd == null) {
									serviceToAdd = service;
								}
								if (!serviceToAdd.equals(service)) {
									done = true;
									break;
								} else {
									serviceToAdd = service;
								}

							}
						} else {
							done = true;
							break;
						}
					}
					if (!done && serviceToAdd != null) {
						output.getSteps().add(addPos,
								new SimpleStep(serviceToAdd, this));
						for (Path np : newPaths) {
							np.getSteps().remove(0);
						}
						addPos++;
						serviceToAdd = null;
					}
				}
				Branch newBranch = new Branch(this);
				newBranch.setPaths(new HashSet<>(newPaths));
				output.getSteps().add(addPos, newBranch);
			}
		}
		return output;
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
				// debug();
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
						// debug();
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
			// debug();
			if (!dl.checkConsistency()) {
				return true;
			}
			return false;
		} finally {
			dl.removeAxioms(ax);
		}
	}

	public Collection<Collection<IndividualPlus>> findServiceOutputMatch(
			IndividualPlus i, Collection<DLClassExpression> targetServiceClasses) {
		ServiceOutputMatchCacheKey k = new ServiceOutputMatchCacheKey(i, targetServiceClasses);
		if (serviceOutputMatchCache.containsKey(k)) {
			return serviceOutputMatchCache.get(k);
		}
		Set<Collection<IndividualPlus>> output = new HashSet<>();
		Set<DLClassExpression> servicePartials = new HashSet<>();

		try {
			dl.addAxioms(i.getAxioms());

			for (DLClassExpression serviceClass : dl.getSubclasses(dl.clazz(NS
					+ "Service"))) {
				// dbg("Try service: %s", serviceClass);
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
						MatchStatus match = serviceClassMatches(i,
								serviceClass, targetServiceClasses, serviceI,
								serviceClassInput,
								dl.objectProp(NS + "has_input"),
								dl.objectProp(NS + "has_output"));

						switch (match) {
						case FULL:
							output.add(Arrays
									.asList(new IndividualPlus[] { new IndividualPlus(
											serviceClassI) }));
							break;
						case PARTIAL:
							servicePartials.add(serviceClass);
							break;
						default:
							break;
						}
					}
				}
			}

			if (output.size() == 0) {
				// debug();
				Collection<DLAxiom> axioms = dl.getAxioms();
				// List<DLClassExpression> serviceClasses = new ArrayList<>(
				// dl.getSubclasses(dl.clazz(NS + "Service")));
				boolean matchFound = false;
				for (int n = 2; n <= servicePartials.size(); n++) {
					if (matchFound) {
						break;
					}
					Set<Set<DLClassExpression>> serviceClassTuples = Utils
							.getNTuplePermutations(servicePartials, n);

					for (Set<DLClassExpression> serviceClassTuple : serviceClassTuples) {
						// dbg("Try services: %s", serviceClassTuple);
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
								// debug();
								MatchStatus match = serviceClassMatches(i,
										serviceClassTuple,
										targetServiceClasses, serviceI,
										serviceClassInputPair,
										dl.objectProp(NS + "has_input"),
										dl.objectProp(NS + "has_output"));

								switch (match) {
								case FULL:
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
									matchFound = true;
									break;

								default:
									break;
								}
							}
						}

					}
				}
			}
		} finally {
			dl.removeAxioms(i.getAxioms());
		}
		serviceOutputMatchCache.put(k, output);
		return output;
	}

	private MatchStatus serviceClassMatches(IndividualPlus testI,
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

	private MatchStatus serviceClassMatches(IndividualPlus testI,
			Collection<DLClassExpression> serviceClasses,
			Collection<DLClassExpression> nextServiceClasses,
			IndividualPlus serviceClassI,
			Collection<DLIndividual> serviceClassIFillers,
			DLObjectPropertyExpression<?> propToKeep,
			DLObjectPropertyExpression<?> propToReplace) {
		MatchStatus output = MatchStatus.NONE;
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
		// debug();
		boolean add = false;
		if (!dl.checkConsistency()) {
			add = true;
			output = MatchStatus.PARTIAL;
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
						// debug();
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
		if (add) {
			output = MatchStatus.FULL;
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
			dl.setOutputFile(new File(System.getProperty("user.home")
					+ "/sw/abfab-integration-output.owl"));
			dl.saveOntology();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
