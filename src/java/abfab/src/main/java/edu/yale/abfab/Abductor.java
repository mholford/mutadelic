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
import java.util.UUID;

import edu.yale.abfab.Path;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClass;
import edu.yale.dlgen.DLClassExpression;
import edu.yale.dlgen.DLDataPropertyExpression;
import edu.yale.dlgen.DLEntity;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.DLLiteral;
import edu.yale.dlgen.DLObjectPropertyExpression;
import edu.yale.dlgen.controller.DLController;
import static edu.yale.abfab.NS.*;
import static edu.yale.abfab.Logging.*;

public abstract class Abductor {

	private DLController dl;
	private String namespace;
	private Path executingPath;
	private Path2 executingPath2;
	private DLObjectPropertyExpression<?> HAS_INPUT;
	private DLObjectPropertyExpression<?> HAS_OUTPUT;
	private Map<ServiceOutputMatchCacheKey, Collection<Collection<IndividualPlus>>> serviceOutputMatchCache;
	private Map<ServiceOutputMatchCacheKey2, Collection<Collection<DLClassExpression>>> serviceOutputMatchCache2;
	private Map<PathCacheKey, Path> pathCache;
	private Map<PathCacheKey, Path2> pathCache2;
	private Map<SCCKey, Boolean> scCache;

	public Abductor() {
		dl = initDLController();
		HAS_INPUT = dl.objectProp(NS + "has_input");
		HAS_OUTPUT = dl.objectProp(NS + "has_output");
		serviceOutputMatchCache = new HashMap<>();
		serviceOutputMatchCache2 = new HashMap<>();
		pathCache = new HashMap<>();
		pathCache2 = new HashMap<>();
		scCache = new HashMap<>();
	}

	public enum MatchStatus {
		FULL, PARTIAL, NONE
	}

	class PathCacheKey {
		Collection<DLClassExpression> types;
		Collection<DLDataPropertyExpression> dataProperties;
		Map<DLObjectPropertyExpression<?>, Collection<DLClassExpression>> objectPropertyMap;
		DLClassExpression<?> goal;

		public PathCacheKey(
				Collection<DLClassExpression> types,
				Collection<DLDataPropertyExpression> dataProperties,
				Map<DLObjectPropertyExpression<?>, Collection<DLClassExpression>> objectPropertyMap,
				DLClassExpression<?> goal) {
			super();
			this.types = types;
			this.dataProperties = dataProperties;
			this.objectPropertyMap = objectPropertyMap;
			this.goal = goal;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime
					* result
					+ ((dataProperties == null) ? 0 : dataProperties.hashCode());
			result = prime * result + ((goal == null) ? 0 : goal.hashCode());
			result = prime
					* result
					+ ((objectPropertyMap == null) ? 0 : objectPropertyMap
							.hashCode());
			result = prime * result + ((types == null) ? 0 : types.hashCode());
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
			PathCacheKey other = (PathCacheKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (dataProperties == null) {
				if (other.dataProperties != null)
					return false;
			} else if (!dataProperties.equals(other.dataProperties))
				return false;
			if (goal == null) {
				if (other.goal != null)
					return false;
			} else if (!goal.equals(other.goal))
				return false;
			if (objectPropertyMap == null) {
				if (other.objectPropertyMap != null)
					return false;
			} else if (!objectPropertyMap.equals(other.objectPropertyMap))
				return false;
			if (types == null) {
				if (other.types != null)
					return false;
			} else if (!types.equals(other.types))
				return false;
			return true;
		}

		private Abductor getOuterType() {
			return Abductor.this;
		}

		@Override
		public String toString() {
			return "PathCacheKey [types=" + types + ", dataProperties="
					+ dataProperties + ", objectPropertyMap="
					+ objectPropertyMap + ", goal=" + goal + "]";
		}
	}

	class ServiceOutputMatchCacheKey2 {
		DLClassExpression ce;
		Collection<DLClassExpression> targets;

		public ServiceOutputMatchCacheKey2(DLClassExpression ce,
				Collection<DLClassExpression> targets) {
			this.ce = ce;
			this.targets = targets;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((ce == null) ? 0 : ce.hashCode());
			result = prime * result
					+ ((targets == null) ? 0 : targets.hashCode());
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
			ServiceOutputMatchCacheKey2 other = (ServiceOutputMatchCacheKey2) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (ce == null) {
				if (other.ce != null)
					return false;
			} else if (!ce.equals(other.ce))
				return false;
			if (targets == null) {
				if (other.targets != null)
					return false;
			} else if (!targets.equals(other.targets))
				return false;
			return true;
		}

		private Abductor getOuterType() {
			return Abductor.this;
		}
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

	public Path2 getExecutingPath2() {
		return executingPath2;
	}

	public void setExecutingPath(Path executingPath) {
		this.executingPath = executingPath;
	}
	
	public void setExecutingPath2(Path2 executingPath2) {
		this.executingPath2 = executingPath2;
	}

	public void clearCaches() {
		serviceOutputMatchCache = new HashMap<>();
		pathCache = new HashMap<>();
		pathCache2 = new HashMap<>();
		scCache = new HashMap<>();
	}

	public SCCKey createSCCKey(DLClassExpression<?> serviceClass,
			SCCIndividual input, SCCIndividual output) {
		return new SCCKey(serviceClass, input, output);
	}

	public boolean checkSCCache(SCCKey key) {
		return checkSCCache(key, false);
	}

	public boolean checkSCCache(SCCKey key, boolean ignoreCache) {
		if (!scCache.containsKey(key) || ignoreCache) {
			Set<DLAxiom<?>> ax = new HashSet<>();
			DLIndividual<?> testI = dl.individual(NS + "testI");
			DLIndividual<?> inputI = dl.individual(NS
					+ UUID.randomUUID().toString());
			DLIndividual<?> outputI = dl.individual(NS
					+ UUID.randomUUID().toString());
			ax.addAll(key.input.getAxioms(inputI));
			ax.addAll(key.output.getAxioms(outputI));
			ax.add(dl.newObjectFact(testI, HAS_INPUT, inputI));
			ax.add(dl.newObjectFact(testI, HAS_OUTPUT, outputI));
			ax.add(dl.individualType(testI, dl.notClass(key.serviceClass)));
			Set<DLAxiom<?>> newAx = new HashSet<>();
			try {

				for (DLAxiom<?> a : ax) {
					if (!dl.getAxioms().contains(ax)) {
						newAx.add(a);
					}
				}

				dl.addAxioms(newAx);
				debug();
				boolean consistent = dl.checkConsistency();
				scCache.put(key, !consistent);
			} finally {
				dl.removeAxioms(newAx);
			}
		} else {
			System.out.println("SCCACHE HIT!!");
		}
		return scCache.get(key);
	}

	public SCCIndividual createSCCIndividual(IndividualPlus ip) {
		return createSCCIndividual(ip.getIndividual(), ip.getAxioms());
	}

	public SCCIndividual createSCCIndividual(DLIndividual<?> indiv,
			Set<DLAxiom<?>> axioms) {
		Set<DLAxiom<?>> newAx = new HashSet<>();
		for (DLAxiom<?> ax : axioms) {
			if (!dl.getAxioms().contains(ax)) {
				newAx.add(ax);
			}
		}
		try {
			dl.addAxioms(newAx);
			Collection<DLClassExpression> types = dl.getTypes(indiv);

			Map<DLDataPropertyExpression<?>, Collection<DLLiteral>> dataMap = new HashMap<>();
			Collection<DLDataPropertyExpression> dataProperties = dl
					.getDataProperties(indiv);
			Set<DLDataPropertyExpression<?>> ignores = new HashSet<>();
			Map<DLDataPropertyExpression<?>, DLLiteral<?>> proxies = new HashMap<>();
			if (dataProperties.contains(dl.dataProp(NS + "cache_value_ignore"))) {
				Collection<DLLiteral> ignoreLiterals = dl
						.getDataPropertyValues(indiv,
								dl.dataProp(NS + "cache_value_ignore"));
				for (DLLiteral ignoreLit : ignoreLiterals) {
					String ignore = dl.getLiteralValue(ignoreLit);
					ignores.add(dl.dataProp(ignore));
				}
			}
			if (dataProperties.contains(dl.dataProp(NS + "cache_value_proxy"))) {
				Collection<DLLiteral> proxyLiterals = dl.getDataPropertyValues(
						indiv, dl.dataProp(NS + "cache_value_proxy"));
				for (DLLiteral proxyLiteral : proxyLiterals) {
					String proxy = dl.getLiteralValue(proxyLiteral);
					/* Assume format IRI=Value */
					String[] ps = proxy.split("=");
					String proxiedProp = ps[0];
					String proxyValue = ps[1];

					DLLiteral<?> asLiteral = dl.asLiteral(proxyValue);
					try {
						double d = Double.parseDouble(proxyValue);
						asLiteral = dl.asLiteral(d);
					} catch (Exception e) {
					}

					try {
						int i = Integer.parseInt(proxyValue);
						asLiteral = dl.asLiteral(i);
					} catch (Exception e) {

					}
					proxies.put(dl.dataProp(proxiedProp), asLiteral);
				}
			}
			for (DLDataPropertyExpression<?> dataProp : dataProperties) {
				if (dataProp.equals(dl.dataProp(NS + "cache_value_ignore"))
						|| dataProp.equals(dl
								.dataProp(NS + "cache_value_proxy"))) {
					continue;
				} else if (ignores.contains(dataProp)) {
					Collection<DLLiteral> literals = new HashSet<>();
					literals.add(dl.asLiteral("ignore"));
					dataMap.put(dataProp, literals);
				} else if (proxies.containsKey(dataProp)) {
					Collection<DLLiteral> literals = new HashSet<>();
					literals.add(proxies.get(dataProp));
					dataMap.put(dataProp, literals);
				} else {
					dataMap.put(dataProp,
							dl.getDataPropertyValues(indiv, dataProp));
				}
			}

			Map<DLObjectPropertyExpression<?>, Collection<SCCIndividual>> objectMap = new HashMap<>();
			Collection<DLObjectPropertyExpression> objectProperties = dl
					.getObjectProperties(indiv);
			for (DLObjectPropertyExpression op : objectProperties) {
				Set<SCCIndividual> sccIndivs = new HashSet<>();
				Collection<DLIndividual> opVals = dl.getObjectPropertyValues(
						indiv, op);
				for (DLIndividual<?> opValI : opVals) {
					Collection<DLClassExpression> opValITypes = dl
							.getTypes(opValI);
					SCCIndividual scci = createSCCIndividual(opValI, newAx);
					sccIndivs.add(scci);
				}
				objectMap.put(op, sccIndivs);
			}
			return new SCCIndividual(dl, types, dataMap, objectMap);
		} finally {
			dl.removeAxioms(newAx);
		}
	}

	private PathCacheKey createPathCacheKey(IndividualPlus ip,
			DLClassExpression<?> goal) {
		Set<DLAxiom<?>> newAx = new HashSet<>();
		for (DLAxiom<?> ax : ip.getAxioms()) {
			if (!dl.getAxioms().contains(ax)) {
				newAx.add(ax);
			}
		}
		try {
			dl.addAxioms(newAx);
			Collection<DLClassExpression> types = dl.getTypes(ip
					.getIndividual());
			Collection<DLDataPropertyExpression> dataProperties = dl
					.getDataProperties(ip.getIndividual());
			Collection<DLObjectPropertyExpression> ops = dl
					.getObjectProperties(ip.getIndividual());
			Map<DLObjectPropertyExpression<?>, Collection<DLClassExpression>> opMap = new HashMap<>();
			for (DLObjectPropertyExpression op : ops) {
				Collection<DLIndividual> opVals = dl.getObjectPropertyValues(
						ip.getIndividual(), op);
				for (DLIndividual<?> opValI : opVals) {
					Collection<DLClassExpression> opValITypes = dl
							.getTypes(opValI);
					opMap.put(op, opValITypes);
				}
			}
			return new PathCacheKey(types, dataProperties, opMap, goal);
		} finally {
			dl.removeAxioms(newAx);
		}
	}

	public IndividualPlus exec(IndividualPlus input, Path goalPath) {
		System.out.println("EXEC");
		executingPath = goalPath;
		return goalPath.exec(input);
	}
	
	public IndividualPlus exec2(IndividualPlus input, Path2 goalPath) {
		System.out.println("EXEC");
		executingPath2 = goalPath;
		return goalPath.exec(input);
	}

	public DLClassExpression<?> getServiceOutputFiller(
			DLClassExpression<?> serviceClass) {
		return getServiceObjectFiller(serviceClass,
				dl.objectProp(NS + "has_output"));
	}

	public DLClassExpression<?> getServiceInputFiller(
			DLClassExpression<?> serviceClass) {
		return getServiceObjectFiller(serviceClass,
				dl.objectProp(NS + "has_input"));
	}

	public double getServiceCost(DLClassExpression<?> serviceClass) {
		String costPre = getServiceDataFiller(serviceClass,
				dl.dataProp(NS + "has_cost"));
		return costPre == null ? Double.MAX_VALUE : Double.parseDouble(costPre);
	}

	public String getServiceExecutable(DLClassExpression<?> serviceClass) {
		return getServiceDataFiller(serviceClass,
				dl.dataProp(NS + "has_executable"));
	}

	// TODO replace with visitor pattern
	public String getServiceDataFiller(DLClassExpression<?> sc,
			DLDataPropertyExpression<?> dp) {
		DLClassExpression eq = dl.getEquivalentClasses(sc).iterator().next();
		for (DLEntity<?> term : dl.getTerms(eq)) {
			DLDataPropertyExpression<?> dataValueProperty = dl
					.getDataValueProperty((DLClassExpression<?>) term);
			if (dataValueProperty != null && dataValueProperty.equals(dp)) {
				return dl.getLiteralValue(dl
						.getDataValueFiller((DLClassExpression<?>) term));
			}
		}
		return null;
	}

	// TODO replace with visitor pattern
	@SuppressWarnings("rawtypes")
	private DLClassExpression<?> getServiceObjectFiller(
			DLClassExpression<?> serviceClass, DLObjectPropertyExpression<?> op) {
		Set<DLClassExpression> fillers = new HashSet<>();
		Set<DLClassExpression> scs = new HashSet<>();
		boolean union = false;

		if (dl.isIntersectionClass(serviceClass)) {
			for (DLEntity<?> term : dl.getTerms(serviceClass)) {
				scs.add((DLClassExpression) term);
			}
		} else if (dl.isUnionClass(serviceClass)) {
			union = true;
			for (DLEntity<?> term : dl.getTerms(serviceClass)) {
				scs.add((DLClassExpression) term);
			}
		} else {
			scs.add(serviceClass);
		}

		for (DLClassExpression sc : scs) {
			DLClassExpression eq = dl.getEquivalentClasses(sc).iterator()
					.next();
			for (DLEntity<?> term : dl.getTerms(eq)) {
				DLObjectPropertyExpression<?> objectSomeProperty = dl
						.getObjectSomeProperty((DLClassExpression<?>) term);
				if (objectSomeProperty != null && objectSomeProperty.equals(op)) {
					fillers.add(dl
							.getObjectSomeFiller((DLClassExpression<?>) term));
				}
			}
		}
		if (fillers.size() == 1) {
			return fillers.iterator().next();
		} else {
			if (union) {
				return dl.orClass(fillers.toArray(new DLClassExpression[fillers
						.size()]));
			} else {
				return dl.andClass(fillers
						.toArray(new DLClassExpression[fillers.size()]));
			}
		}
	}

	public Set<Path2> getAllPaths2(IndividualPlus origInput,
			DLClassExpression<?> goalClass) {
		dbg(DBG_PATH_CREATION, "Get All Paths: %s, %s",
				origInput.getIndividual(), goalClass);
		Set<Path2> completedPaths = new HashSet<>();

		// Set<Path> paths = extendPath(null, origInput, goalI);
		Set<Path2> paths = initializePaths2(origInput, goalClass);
		dbg(DBG_PATH_CREATION, "Initial Paths: %s", paths);

		for (;;) {
			Set<Path2> nextPaths = new HashSet<>();
			for (Path2 p : paths) {
				if (matchesInput2(origInput, p)) {
					Path2 merged = mergeBranches2(p);
					completedPaths.add(merged);
					dbg(DBG_PATH_CREATION, "Path %d(%s) complete",
							p.hashCode(), merged.toString());
				} else {
					Set<Path2> eps = extendPath2(p, origInput, p.getLastInput());
					for (Path2 ep : eps) {
						dbg(DBG_PATH_CREATION, "Extend Path %d to %d(%s)",
								p.hashCode(), ep.hashCode(), ep.toString());
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

	public Set<Path> getAllPaths(IndividualPlus origInput,
			DLClassExpression<?> goalClass) {
		dbg(DBG_PATH_CREATION, "Get All Paths: %s, %s", origInput, goalClass);
		Set<Path> completedPaths = new HashSet<>();

		// Set<Path> paths = extendPath(null, origInput, goalI);
		Set<Path> paths = initializePaths(origInput, goalClass);
		dbg(DBG_PATH_CREATION, "Initial Paths: %s", paths);

		for (;;) {
			Set<Path> nextPaths = new HashSet<>();
			for (Path p : paths) {
				if (matchesInput(origInput, p)) {
					Path merged = mergeBranches(p);
					completedPaths.add(merged);
					dbg(DBG_PATH_CREATION, "Path %d(%s) complete",
							p.hashCode(), merged.toString());
				} else {
					Set<Path> eps = extendPath(p, origInput, p.getLastInput());
					for (Path ep : eps) {
						dbg(DBG_PATH_CREATION, "Extend Path %d to %d(%s)",
								p.hashCode(), ep.hashCode(), ep.toString());
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
			if (s instanceof Branch) {
				Branch b = (Branch) s;
				List<Iterator<Step>> piters = new ArrayList<>();
				List<Path> newPaths = new ArrayList<>();
				for (Path p : b.getPaths()) {
					piters.add(p.getSteps().iterator());
					newPaths.add(p.copy());
				}
				boolean done = false;
				Step stepToAdd = null;
				int addPos = 0;
				while (!done) {
					for (Iterator<Step> piter : piters) {
						if (piter.hasNext()) {
							Step step = piter.next();

							if (stepToAdd == null) {
								stepToAdd = step;
							}
							if (!stepToAdd.equals(step)) {
								done = true;
								break;
							} else {
								stepToAdd = step;

							}
						} else {
							done = true;
							break;
						}
					}
					if (!done && stepToAdd != null) {
						output.getSteps().add(addPos, stepToAdd);
						for (Path np : newPaths) {
							np.getSteps().remove(0);
						}
						addPos++;
						stepToAdd = null;
					}
				}
				Branch newBranch = new Branch(this);
				newBranch.setPaths(new HashSet<>(newPaths));
				output.getSteps().add(addPos, newBranch);
			} /*
			 * else if (s instanceof Condition) { Condition b = (Condition) s;
			 * List<Iterator<Step>> piters = new ArrayList<>(); List<Path>
			 * newPaths = new ArrayList<>(); for (Path p : b.getPaths()) {
			 * piters.add(p.getSteps().iterator()); newPaths.add(p.copy()); }
			 * boolean done = false; Step stepToAdd = null; int addPos = 0;
			 * while (!done) { for (Iterator<Step> piter : piters) { if
			 * (piter.hasNext()) { Step step = piter.next();
			 * 
			 * if (stepToAdd == null) { stepToAdd = step; } if
			 * (!stepToAdd.equals(step)) { done = true; break; } else {
			 * stepToAdd = step;
			 * 
			 * } } else { done = true; break; } } if (!done && stepToAdd !=
			 * null) { output.getSteps().add(addPos, stepToAdd); for (Path np :
			 * newPaths) { np.getSteps().remove(0); } addPos++; stepToAdd =
			 * null; } } Condition newBranch = new Condition(this);
			 * newBranch.setPaths(new HashSet<>(newPaths));
			 * output.getSteps().add(addPos, newBranch); }
			 */else {
				output.getSteps().add(0, s);
			}
			if (!(output.equals(input))) {
				output = mergeBranches(output);
			}
		}
		return output;
	}

	public Path2 mergeBranches2(Path2 input) {
		Path2 output = new Path2(input.getInitialInput(), input.getAbductor());
		List<Step2> reverseSteps = new ArrayList<>();
		for (Step2 s : input.getSteps()) {
			reverseSteps.add(s.copy());
		}
		Collections.reverse(reverseSteps);
		Iterator<Step2> mainIter = reverseSteps.iterator();
		while (mainIter.hasNext()) {
			Step2 s = mainIter.next();
			if (s instanceof Branch2) {
				Branch2 b = (Branch2) s;
				List<Iterator<Step2>> piters = new ArrayList<>();
				List<Path2> newPaths = new ArrayList<>();
				for (Path2 p : b.getPaths()) {
					piters.add(p.getSteps().iterator());
					newPaths.add(p.copy());
				}
				boolean done = false;
				Step2 stepToAdd = null;
				int addPos = 0;
				while (!done) {
					for (Iterator<Step2> piter : piters) {
						if (piter.hasNext()) {
							Step2 step = piter.next();

							if (stepToAdd == null) {
								stepToAdd = step;
							}
							if (!stepToAdd.equals(step)) {
								done = true;
								break;
							} else {
								stepToAdd = step;

							}
						} else {
							done = true;
							break;
						}
					}
					if (!done && stepToAdd != null) {
						output.getSteps().add(addPos, stepToAdd);
						for (Path2 np : newPaths) {
							np.getSteps().remove(0);
						}
						addPos++;
						stepToAdd = null;
					}
				}
				Branch2 newBranch = new Branch2(this);
				newBranch.setPaths(new HashSet<>(newPaths));
				output.getSteps().add(addPos, newBranch);
			} else {
				output.getSteps().add(0, s);
			}
			if (!(output.equals(input))) {
				output = mergeBranches2(output);
			}
		}
		return output;
	}

	public Path getBestPath(IndividualPlus origInput,
			DLClassExpression<?> goalClass) {
		PathCacheKey k = createPathCacheKey(origInput, goalClass);
		if (pathCache.containsKey(k)) {
			dbg(DBG_PATH_CREATION, "PATH CACHE HIT");
			return pathCache.get(k);
		}
		Set<Path> completedPaths = getAllPaths(origInput, goalClass);

		Path bestPath = chooseBestPath(completedPaths);
		pathCache.put(k, bestPath);
		return bestPath;
	}

	public Path2 getBestPath2(IndividualPlus origInput,
			DLClassExpression<?> goalClass) {
		PathCacheKey k = createPathCacheKey(origInput, goalClass);
		if (pathCache.containsKey(k)) {
			dbg(DBG_PATH_CREATION, "PATH CACHE HIT");
			return pathCache2.get(k);
		}
		Set<Path2> completedPaths = getAllPaths2(origInput, goalClass);

		Path2 bestPath = chooseBestPath2(completedPaths);
		pathCache2.put(k, bestPath);
		return bestPath;
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

	public Set<Path2> initializePaths2(IndividualPlus input,
			DLClassExpression<?> goalClass) {
		Set<Path2> paths = new HashSet<>();

		Collection<DLClassExpression<?>> terminals = findTerminals2(goalClass);
		for (DLClassExpression<?> terminal : terminals) {
			Path2 p = new Path2(input, this);
			p.add(terminal);
			paths.add(p);
		}

		return paths;
	}

	public Collection<DLClassExpression<?>> findTerminals2(
			DLClassExpression<?> goalClass) {
		long start = System.currentTimeMillis();
		Set<DLClassExpression<?>> terminals = new HashSet<>();

		for (DLClassExpression<?> serviceClass : dl.getSubclasses(dl.clazz(NS
				+ "Service"))) {
			// filler of has_output for serviceClass must be subsumed by
			// goalClass
			DLClassExpression<?> serviceOutputFiller = getServiceOutputFiller(serviceClass);
			boolean isSubsumed = dl.checkEntailed(dl.subClass(
					serviceOutputFiller, goalClass));
			if (isSubsumed) {
				terminals.add(serviceClass);
			}
		}

		long end = System.currentTimeMillis();
		dbg(DBG_TIMING, "findTerminals: %d millis", end - start);
		return terminals;
	}

	public Collection<DLIndividual<?>> findTerminals(
			DLClassExpression<?> goalClass) {
		long start = System.currentTimeMillis();
		Set<DLIndividual<?>> terminals = new HashSet<>();

		for (DLClassExpression<?> serviceClass : dl.getSubclasses(dl.clazz(NS
				+ "Service"))) {
			for (DLIndividual<?> serviceClassI : dl.getInstances(serviceClass)) {
				boolean add = false;
				Set<DLAxiom<?>> ax = new HashSet<>();

				DLIndividual<?> testService = dl.individual(NS + "testService");
				ax.add(dl.individualType(testService, dl.thing()));

				Set<IndividualPlus> indivsToMerge = new HashSet<>();
				for (DLIndividual<?> sciInput : dl.getObjectPropertyValues(
						serviceClassI, HAS_INPUT)) {
					indivsToMerge.add(new IndividualPlus(sciInput));
				}

				IndividualPlus mergedIndividual = mergeIndividuals(indivsToMerge);
				ax.add(dl.newObjectFact(testService, HAS_INPUT,
						mergedIndividual.getIndividual()));

				// for (DLIndividual<?> sciInput : dl.getObjectPropertyValues(
				// serviceClassI, HAS_INPUT)) {
				// ax.add(dl.newObjectFact(testService, HAS_INPUT, sciInput));
				// }

				/* PRE SCCACHE */
				// DLIndividual<?> testOutput = dl.individual(NS +
				// "testOutput");
				//
				// ax.add(dl.individualType(testOutput, goalClass));
				// ax.add(dl.newObjectFact(testService, HAS_OUTPUT,
				// testOutput));
				//
				// ax.add(dl.individualType(testService,
				// dl.notClass(serviceClass)));
				// dl.addAxioms(ax);
				// // debug();
				// if (!dl.checkConsistency()) {
				// add = true;
				// }
				// dl.removeAxioms(ax);

				/* POST SCC */
				IndividualPlus testOutput = new IndividualPlus(dl.individual(NS
						+ "testOutput"));
				testOutput.getAxioms()
						.add(dl.individualType(testOutput.getIndividual(),
								goalClass));
				SCCIndividual sccInput = createSCCIndividual(mergedIndividual);
				SCCIndividual sccOutput = createSCCIndividual(testOutput);
				SCCKey key = createSCCKey(serviceClass, sccInput, sccOutput);
				add = checkSCCache(key);

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
		long end = System.currentTimeMillis();
		dbg(DBG_TIMING, "findTerminals: %d millis", end - start);
		return terminals;
	}

	public boolean matchesInput(IndividualPlus input, Path p) {
		boolean output;
		long start = System.currentTimeMillis();
		Set<DLAxiom<?>> ax = new HashSet<>();

		try {
			ax.addAll(input.getAxioms());

			DLClassExpression topClass = p.getTopStepUnifiedClass();
			DLIndividual<?> testService = dl.individual(NS + "testService");
			ax.add(dl.individualType(testService, dl.thing()));

			// for (IndividualPlus tsOutput : p.getLastOutput()) {
			// ax.addAll(tsOutput.getAxioms());
			// ax.add(dl.newObjectFact(testService, HAS_OUTPUT,
			// tsOutput.getIndividual()));
			// }

			IndividualPlus mergedOutput = mergeIndividuals(p.getLastOutput());

			/* PRE SCC */
			// ax.addAll(mergedOutput.getAxioms());
			// ax.add(dl.newObjectFact(testService, HAS_OUTPUT,
			// mergedOutput.getIndividual()));
			//
			// ax.add(dl.newObjectFact(testService, HAS_INPUT,
			// input.getIndividual()));
			// ax.add(dl.individualType(testService, dl.notClass(topClass)));
			// dl.addAxioms(ax);
			// // debug();
			// output = !dl.checkConsistency();

			/* POST SCC */
			SCCIndividual scInput = createSCCIndividual(input);
			SCCIndividual scOutput = createSCCIndividual(mergedOutput);
			SCCKey key = createSCCKey(topClass, scInput, scOutput);
			output = checkSCCache(key);
		} finally {
			dl.removeAxioms(ax);
		}
		long end = System.currentTimeMillis();
		dbg(DBG_TIMING, "matchesInput: %d millis", end - start);
		return output;
	}

	@SuppressWarnings("rawtypes")
	public boolean matchesInput2(IndividualPlus input, Path2 p) {
		boolean output = false;
		long start = System.currentTimeMillis();
		Set<DLAxiom<?>> ax = new HashSet<>();
		try {
			ax.addAll(input.getAxioms());
			dl.addAxioms(ax);

			DLClassExpression topClass = p.getTopStepUnifiedClass();
			DLClassExpression topClassInput = getServiceInputFiller(topClass);

			debug();

			output = dl.checkEntailed(dl.individualType(input.getIndividual(),
					topClassInput));
		} finally {
			dl.removeAxioms(ax);
		}
		long end = System.currentTimeMillis();
		dbg(DBG_TIMING, "matchesInput2: %d millis", end - start);
		return output;
	}

	public IndividualPlus mergeIndividuals(Collection<IndividualPlus> inds) {
		Collection<IndividualPlus> nonNullInds = new HashSet<>();
		for (IndividualPlus ip : inds) {
			if (ip != null) {
				nonNullInds.add(ip);
			}
		}
		DLController dl = getDLController();
		String NS = getNamespace();
		Set<DLAxiom<?>> oldAx = new HashSet<>();
		Set<DLAxiom<?>> newAx = new HashSet<>();
		String name = "merge" + UUID.randomUUID().toString();
		IndividualPlus output = null;
		if (nonNullInds.size() > 1) {

			try {
				for (IndividualPlus ind : nonNullInds) {
					if (ind.getAxioms() != null) {
						oldAx.addAll(ind.getAxioms());
						newAx.addAll(ind.getAxioms());
						dl.addAxioms(ind.getAxioms());
					}
				}
				Map<DLDataPropertyExpression<?>, Collection<DLLiteral>> dpvs = new HashMap<>();
				Map<DLObjectPropertyExpression<?>, Collection<DLIndividual>> opvs = new HashMap<>();
				Set<DLClassExpression<?>> types = new HashSet<>();
				Set<DLIndividual<?>> diffIndivs = new HashSet<>();
				Set<DLIndividual<?>> sameIndivs = new HashSet<>();

				for (IndividualPlus ip : nonNullInds) {
					DLIndividual<?> i = ip.getIndividual();
					for (DLDataPropertyExpression<?> odpe : dl
							.getDataProperties(i)) {
						if (!dpvs.containsKey(odpe)) {
							dpvs.put(odpe, new HashSet<DLLiteral>());
						}
						dpvs.get(odpe)
								.addAll(dl.getDataPropertyValues(i, odpe));
					}
					for (DLObjectPropertyExpression<?> oope : dl
							.getObjectProperties(i)) {
						if (!opvs.containsKey(oope)) {
							opvs.put(oope, new HashSet<DLIndividual>());
						}
						opvs.get(oope).addAll(
								dl.getObjectPropertyValues(i, oope));
					}
					for (DLClassExpression<?> oce : dl.getTypes(i)) {
						types.add(oce);
					}
					for (DLIndividual<?> oi : dl.getDifferentIndividuals(i)) {
						diffIndivs.add(oi);
					}
					for (DLIndividual<?> oi : dl.getSameIndividuals(i)) {
						sameIndivs.add(oi);
					}
					diffIndivs.add(i);
					sameIndivs.add(i);
				}

				newAx.add(dl.newIndividual(NS + name, dl.thing()));
				for (DLClassExpression<?> t : types) {
					newAx.add(dl.individualType(dl.individual(NS + name), t));
				}
				for (DLDataPropertyExpression<?> odpe : dpvs.keySet()) {
					for (DLLiteral<?> owls : dpvs.get(odpe)) {
						newAx.add(dl.newDataFact(dl.individual(NS + name),
								odpe, owls));
					}
				}
				for (DLObjectPropertyExpression<?> oope : opvs.keySet()) {
					for (DLIndividual<?> oi : opvs.get(oope)) {
						newAx.add(dl.newObjectFact(dl.individual(NS + name),
								oope, oi));
					}
				}

				output = new IndividualPlus(dl.individual(NS + name), newAx);
			} finally {
				dl.removeAxioms(oldAx);
			}
		} else if (nonNullInds.size() == 1) {
			output = nonNullInds.iterator().next();
		}

		return output;
	}

	public Collection<Collection<DLClassExpression>> findServiceOutputMatch2(
			DLClassExpression ce,
			Collection<DLClassExpression> targetServiceClasses) {
		ServiceOutputMatchCacheKey2 k = new ServiceOutputMatchCacheKey2(ce,
				targetServiceClasses);
		if (serviceOutputMatchCache2.containsKey(k)) {
			System.out.println("CACHE HIT");
			return serviceOutputMatchCache2.get(k);
		}
		Set<Collection<DLClassExpression>> output = new HashSet<>();

		Collection<DLClassExpression> allServiceClasses = dl.getSubclasses(dl
				.clazz(NS + "Service"));
		for (DLClassExpression serviceClass : allServiceClasses) {
			dbg(DBG_SERVICE_MATCH, "Try service: %s", serviceClass);
			for (DLClassExpression target : targetServiceClasses) {
				// Service class output must be subsumed by target class input
				DLClassExpression<?> serviceOutputFiller = getServiceOutputFiller(serviceClass);
				DLClassExpression<?> targetInputFiller = getServiceInputFiller(target);
				boolean isSubsumed = dl.checkEntailed(dl.subClass(
						serviceOutputFiller, targetInputFiller));
				if (isSubsumed) {
					output.add(Arrays
							.asList(new DLClassExpression[] { serviceClass }));
				}
			}
		}

		if (output.size() == 0) {
			boolean matchFound = false;
			for (int n = 2; n <= allServiceClasses.size(); n++) {
				if (matchFound) {
					break;
				}
				Set<Set<DLClassExpression>> serviceClassTuples = Utils
						.getNTuplePermutations(allServiceClasses, n);

				for (Set<DLClassExpression> serviceClassTuple : serviceClassTuples) {
					dbg(DBG_SERVICE_MATCH, "Try services: %s",
							serviceClassTuple);
					for (DLClassExpression target : targetServiceClasses) {
						// AND together output of each service; this class must
						// be
						// subsumed by target class input
						Set<DLClassExpression> outputs = new HashSet<>();
						for (DLClassExpression sctc : serviceClassTuple) {
							outputs.add(getServiceOutputFiller(sctc));
						}
						DLClassExpression<?> serviceOutputFiller = dl
								.andClass(outputs
										.toArray(new DLClassExpression[outputs
												.size()]));
						DLClassExpression<?> targetInputFiller = getServiceInputFiller(target);
						boolean isSubsumed = dl.checkEntailed(dl.subClass(
								serviceOutputFiller, targetInputFiller));
						if (isSubsumed) {
							output.add(serviceClassTuple);
							matchFound = true;
						}
					}
				}
			}
		}
		serviceOutputMatchCache2.put(k, output);

		return output;
	}

	public Collection<Collection<IndividualPlus>> findServiceOutputMatch(
			IndividualPlus i, Collection<DLClassExpression> targetServiceClasses) {
		ServiceOutputMatchCacheKey k = new ServiceOutputMatchCacheKey(i,
				targetServiceClasses);
		if (serviceOutputMatchCache.containsKey(k)) {
			System.out.println("CACHE HIT");
			return serviceOutputMatchCache.get(k);
		}
		Set<Collection<IndividualPlus>> output = new HashSet<>();
		Set<DLClassExpression> servicePartials = new HashSet<>();

		try {
			dl.addAxioms(i.getAxioms());

			for (DLClassExpression serviceClass : dl.getSubclasses(dl.clazz(NS
					+ "Service"))) {
				dbg(DBG_SERVICE_MATCH, "Try service: %s", serviceClass);
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
							dbg(DBG_SERVICE_MATCH, "Full Match");
							output.add(Arrays
									.asList(new IndividualPlus[] { new IndividualPlus(
											serviceClassI) }));
							break;
						case PARTIAL:
							dbg(DBG_SERVICE_MATCH, "Partial Match");
							servicePartials.add(serviceClass);
							break;
						default:
							dbg(DBG_SERVICE_MATCH, "No Match");
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
						dbg(DBG_SERVICE_MATCH, "Try services: %s",
								serviceClassTuple);
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
									dbg(DBG_SERVICE_MATCH, "Full Match");
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
									dbg(DBG_SERVICE_MATCH, "No Match");
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
		long start = System.currentTimeMillis();
		MatchStatus output = MatchStatus.NONE;
		Set<DLAxiom<?>> ax = new HashSet<>();

		// for (DLIndividual<?> serviceClassIFiller : serviceClassIFillers) {
		// ax.add(dl.newObjectFact(serviceClassI.getIndividual(), propToKeep,
		// serviceClassIFiller));
		// }

		Set<IndividualPlus> indsToMerge = new HashSet<>();
		for (DLIndividual<?> serviceClassIFiller : serviceClassIFillers) {
			indsToMerge.add(new IndividualPlus(serviceClassIFiller));
		}
		IndividualPlus mergedInd = mergeIndividuals(indsToMerge);

		/* PRE SCC */
		// ax.add(dl.newObjectFact(serviceClassI.getIndividual(), propToKeep,
		// mergedInd.getIndividual()));
		//
		// ax.add(dl.newObjectFact(serviceClassI.getIndividual(), propToReplace,
		// testI.getIndividual()));
		//
		// DLClassExpression<?> serv;
		// if (serviceClasses.size() > 1) {
		// serv = dl.andClass(serviceClasses
		// .toArray(new DLClassExpression<?>[serviceClasses.size()]));
		// } else {
		// serv = serviceClasses.iterator().next();
		// }
		// ax.add(dl.individualType(serviceClassI.getIndividual(),
		// dl.notClass(serv)));
		// dl.addAxioms(ax);
		// // debug();
		// boolean add = false;
		// if (!dl.checkConsistency()) {
		// add = true;
		// output = MatchStatus.PARTIAL;
		// }
		// dl.removeAxioms(ax);

		/* POST SCC */
		DLClassExpression<?> serv;
		if (serviceClasses.size() > 1) {
			serv = dl.andClass(serviceClasses
					.toArray(new DLClassExpression<?>[serviceClasses.size()]));
		} else {
			serv = serviceClasses.iterator().next();
		}
		IndividualPlus inputIP;
		IndividualPlus outputIP;
		if (propToKeep.equals(HAS_INPUT)) {
			inputIP = mergedInd;
			outputIP = testI;
		} else {
			inputIP = testI;
			outputIP = mergedInd;
		}
		SCCIndividual scInput = createSCCIndividual(inputIP);
		SCCIndividual scOutput = createSCCIndividual(outputIP);
		SCCKey key = createSCCKey(serv, scInput, scOutput);
		boolean add = checkSCCache(key);
		if (add) {
			output = MatchStatus.PARTIAL;
		}

		if (/* add && */nextServiceClasses != null) {
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
						if (nextServiceClasses.size() > 1) {
							serv = dl
									.andClass(nextServiceClasses
											.toArray(new DLClassExpression<?>[nextServiceClasses
													.size()]));
						} else {
							serv = nextServiceClasses.iterator().next();
						}

						/* PRE SCC */
						// if (keepers.size() > 1) {
						// Set<DLClassExpression> keeperClasses = new
						// HashSet<>();
						// for (DLIndividual<?> keeper : keepers) {
						// keeperClasses.addAll(dl.getTypes(keeper));
						// }
						// DLClassExpression keeperClass = dl
						// .andClass(keeperClasses
						// .toArray(new DLClassExpression[keeperClasses
						// .size()]));
						// DLIndividual<?> keeperI = dl.individual(NS
						// + "keeperI");
						// ax2.add(dl.individualType(keeperI, keeperClass));
						// ax2.add(dl.newObjectFact(
						// serviceClassI.getIndividual(), propToKeep,
						// keeperI));
						//
						// } else {
						// ax2.add(dl.newObjectFact(
						// serviceClassI.getIndividual(), propToKeep,
						// keepers.iterator().next()));
						// }
						//
						// ax2.add(dl.newObjectFact(serviceClassI.getIndividual(),
						// propToReplace, replacer));
						//
						// ax2.add(dl.individualType(
						// serviceClassI.getIndividual(),
						// dl.notClass(serv)));
						// dl.addAxioms(ax2);
						// // debug();
						// if (dl.checkConsistency()) {
						// add = false;
						// } else {
						// add = true;
						// }
						// dl.removeAxioms(ax2);

						/* POST SCC */
						IndividualPlus keeperInd;
						IndividualPlus input2;
						IndividualPlus output2;
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
							keeperInd = new IndividualPlus(keeperI);
							keeperInd.getAxioms().add(
									dl.individualType(keeperI, keeperClass));
							ax2.add(dl.newObjectFact(
									serviceClassI.getIndividual(), propToKeep,
									keeperI));

						} else {
							keeperInd = new IndividualPlus(keepers.iterator()
									.next());
							ax2.add(dl.newObjectFact(
									serviceClassI.getIndividual(), propToKeep,
									keepers.iterator().next()));
						}
						if (propToKeep.equals(HAS_INPUT)) {
							input2 = keeperInd;
							output2 = new IndividualPlus(replacer);
						} else {
							input2 = new IndividualPlus(replacer);
							output2 = keeperInd;
						}
						SCCIndividual scInput2 = createSCCIndividual(input2);
						SCCIndividual scOutput2 = createSCCIndividual(output2);
						key = createSCCKey(serv, scInput2, scOutput2);
						add = checkSCCache(key);

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
		long end = System.currentTimeMillis();
		dbg(DBG_TIMING, "Service Class matches: %d millis", end - start);
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

	@SuppressWarnings("rawtypes")
	public Set<Path2> extendPath2(Path2 p, IndividualPlus origInput,
			Collection<DLClassExpression> goal) {
		Set<Path2> output = new HashSet<>();
		for (DLClassExpression g : goal) {
			Collection<DLClassExpression> topStepDLClasses = p != null ? p
					.getTopStepDLClasses() : null;
			Collection<Collection<DLClassExpression>> services = findServiceOutputMatch2(
					g, topStepDLClasses);

			Path2 np = p != null ? p.copy() : new Path2(origInput, this);
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

	public Path2 chooseBestPath2(Set<Path2> paths) {
		double bestScore = Double.MAX_VALUE;
		Path2 bestPath = null;
		for (Path2 p : paths) {
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
