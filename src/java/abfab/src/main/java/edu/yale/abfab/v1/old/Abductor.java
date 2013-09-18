package edu.yale.med.krauthammerlab.abfab.old;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dyndns.norbrand.OntologyBuilder;
import org.mindswap.pellet.PelletOptions;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

//import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;

//import com.clarkparsia.owlapi.explanation.SatisfiabilityConverter;
//import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

import edu.yale.med.krauthammerlab.abfab.old.service.Service;
import static org.dyndns.norbrand.Utils.*;

public class Abductor {

	private static OntologyBuilder ob;
	private OWLReasoner reasoner;
	private static OWLDataFactory df;
	private OWLOntologyManager mgr;
	private OWLOntology ont;
	private int mergedCount = 0;
	private List<Set<OWLAxiom>> mergedIndivAxioms = new ArrayList<Set<OWLAxiom>>();
	private int dummyCount = 0;
	private Set<OWLAxiom> dummyAxioms = new HashSet<OWLAxiom>();
	private OWLReasonerFactory rf;
	// private SatisfiabilityConverter conv;
	private String pkg;
	private String savePoint;
	private Map<OWLClassExpression, Path> pathCache;

	public Abductor(String pkg) {
		this.pkg = pkg;
		ob = OntologyBuilder.instance();
		// rf = new FaCTPlusPlusReasonerFactory();
		rf = new PelletReasonerFactory();
		reasoner = rf.createNonBufferingReasoner(ob.getOntology());
		ob.setReasonerFactory(rf);
		ob.setReasoner(reasoner);
		df = ob.getDataFactory();
		mgr = ob.getManager();
		ont = ob.getOntology();
		// Save in the folder based on the last word in the package name
		String saveFolder = pkg.substring(pkg.lastIndexOf(".") + 1);
		savePoint = String.format("file:" + System.getProperty("user.home")
				+ "/sw/abfab-%s/abfab-save.owl", saveFolder);
	}

	public OWLIndividual abduce(OWLIndividual input,
			OWLClassExpression desiredClass, boolean usePathCache)
			throws Exception {
		return abduce(input, desiredClass, usePathCache,
				new Path(desiredClass), true, -99);
	}

	/*
	 * Use pathCache - note will NOT check each individual against path. Assumes
	 * that multiple the individual is like the previous, i.e doesn't have any
	 * of the constraints already filled.
	 */
	public OWLIndividual abduce(OWLIndividual input,
			OWLClassExpression desiredClass, boolean usePathCache, Path path,
			boolean execute, int index) throws Exception {
		long startime = startMethod("Start abduce: " + desiredClass);
		Path bestPath = null;
		if (usePathCache && pathCache != null
				&& pathCache.containsKey(desiredClass)) {
			System.out.println("Got From Cache");
			bestPath = pathCache.get(desiredClass);
		} else {
			if (!usePathCache) {
				System.out.println("Not using cache");
			}
			System.out.println("Not from cache");
			Set<Path> paths = new HashSet<Path>();
			Set<Path> completePaths = new HashSet<Path>();

			findTerminalClasses(desiredClass, paths);

			for (;;) {
				Set<Path> nextPaths = new HashSet<Path>();
				for (Path p : paths) {
					if (checkPathAgainstInput(input, p)) {
						// path.copy(p);
						// break;
						completePaths.add(p);
					} else {
						Set<Path> nps = extendPath(p);
						for (Path np : nps) {
							nextPaths.add(np);
						}
					}
				}
				if (nextPaths.size() <= 0) {
					break;
				}
				paths = nextPaths;

			}

			bestPath = getBestPath(completePaths);
		}
		if (usePathCache && pathCache != null) {
			pathCache.put(desiredClass, bestPath);
		}

		if (bestPath == null) {
			return null;
		} else {
			path.copy(bestPath);
		}
		methodTime("Stop abduce: " + desiredClass, startime);
		if (execute) {
			return executePath(input, path);
		} else {
			return dummyIndiv(desiredClass, index);
		}
	}

	private long startMethod(String meth) {
		System.out.println("Start " + meth);
		return System.currentTimeMillis();
	}

	private Path getBestPath(Set<Path> paths) {
		// long starttime = startMethod("getBestPath");
		double bestScore = Double.MAX_VALUE;
		Path bestPath = null;
		for (Path p : paths) {
			if (p.getCost() <= bestScore) {
				bestPath = p;
				bestScore = p.getCost();
			}
		}
		// methodTime("getBestPath", starttime);
		return bestPath;
	}

	public OWLIndividual dummyIndiv(OWLClassExpression desiredClass, int index) {
		// long starttime = startMethod("dummyIndiv");
		String name = "dummy" + index;
		OWLAxiom ax = _indiv(name, desiredClass);
		mgr.addAxiom(ont, ax);
		reasoner.flush();
		dummyAxioms.add(ax);
		// methodTime("dummyIndiv", starttime);
		return i(name);
	}

	private void methodTime(String meth, long start) {
		System.out.println(String.format("Stop %s: %d", meth,
				(System.currentTimeMillis() - start)));
	}

	private void failure(String message) throws Exception {
		 ob.save(savePoint);
		throw new Exception(message);
	}

	public Map<OWLClassExpression, Path> getPathCache() {
		return pathCache;
	}

	public void setPathCache(Map<OWLClassExpression, Path> pathCache) {
		this.pathCache = pathCache;
	}

	@SuppressWarnings("unchecked")
	public OWLIndividual executePath(OWLIndividual input, Path goalPath)
			throws Exception {
		System.out.println("Executing: " + goalPath);
		OWLIndividual currInput = input;
		Iterator<Step> gpIter = goalPath.iterator();
		while (gpIter.hasNext()) {
			Step n = gpIter.next();
			if (n instanceof SimpleStep) {
				OWLClass c = (OWLClass) n.get();

				/*
				 * Validate input
				 */
				OWLClassExpression oce = getFirstRestriction(c, "hasInputClass");
				if (!ob.checkEntailed(_indiv(currInput, oce))) {
					failure("Failed input class validation");
				}

				String preClassName = c.getIRI().toString();
				String className = pkg + "."
						+ preClassName.substring(preClassName.indexOf('#') + 1);
				System.out.println("Call " + className);
				Service s = (Service) Class.forName(className).newInstance();
				OWLIndividual tempOutput = s.exec(currInput);

				/*
				 * Validate output
				 */
				oce = getFirstRestriction(c, "hasOutputClass");
				if (!ob.checkEntailed(_indiv(tempOutput, oce))) {
					failure("Failed output class validation: " + oce);
				}

				currInput = tempOutput;
			} else if (n instanceof SplitStep) {
				List<Path> paths = (List<Path>) n.get();
				Set<OWLIndividual> outputs = new HashSet<OWLIndividual>();
				for (Path p : paths) {
					OWLIndividual i = executePath(currInput, p);
					outputs.add(i);
				}
				currInput = mergeIndividuals(outputs, mergedCount++);
			} else if (n instanceof ChoiceStep) {
				Path bestPath = ((ChoiceStep) n).getBestPath();
				currInput = executePath(currInput, bestPath);
			}
		}
		return currInput;
	}

	private OWLIndividual mergeIndividuals(Set<OWLIndividual> indivs, int index) {
		Set<OWLAxiom> ax = new HashSet<OWLAxiom>();
		String name = "merge" + index;
		OWLIndividual output = null;
		if (indivs.size() > 1) {
			Map<OWLDataPropertyExpression, Set<OWLLiteral>> dpvs = new HashMap<OWLDataPropertyExpression, Set<OWLLiteral>>();
			Map<OWLObjectPropertyExpression, Set<OWLIndividual>> opvs = new HashMap<OWLObjectPropertyExpression, Set<OWLIndividual>>();
			Set<OWLClassExpression> types = new HashSet<OWLClassExpression>();
			Set<OWLIndividual> diffIndivs = new HashSet<OWLIndividual>();
			Set<OWLIndividual> sameIndivs = new HashSet<OWLIndividual>();

			for (OWLIndividual i : indivs) {
				for (OWLDataPropertyExpression odpe : i.getDataPropertyValues(
						ont).keySet()) {
					dpvs.put(odpe, i.getDataPropertyValues(ont).get(odpe));
				}
				for (OWLObjectPropertyExpression oope : i
						.getObjectPropertyValues(ont).keySet()) {
					opvs.put(oope, i.getObjectPropertyValues(ont).get(oope));
				}
				for (OWLClassExpression oce : i.getTypes(ont)) {
					types.add(oce);
				}
				for (OWLIndividual oi : i.getDifferentIndividuals(ont)) {
					diffIndivs.add(oi);
				}
				for (OWLIndividual oi : i.getSameIndividuals(ont)) {
					sameIndivs.add(oi);
				}
				diffIndivs.add(i);
				sameIndivs.add(i);
			}

			ax.add(_indiv(name, thing()));
			for (OWLClassExpression t : types) {
				ax.add(_indiv(name, t));
			}
			for (OWLDataPropertyExpression odpe : dpvs.keySet()) {
				for (OWLLiteral owls : dpvs.get(odpe)) {
					ax.add(_dp_value(i(name), odpe, owls));
				}
			}
			for (OWLObjectPropertyExpression oope : opvs.keySet()) {
				for (OWLIndividual oi : opvs.get(oope)) {
					ax.add(_op_value(i(name), oope, oi));
				}
			}
		} else if (indivs.size() == 1) {
			output = indivs.iterator().next();
		}
		if (ax.size() > 0) {
			mgr.addAxioms(ont, ax);
			reasoner.flush();
		}
		// try {
		// // ob.save(savePoint);
		// } catch (OWLException e) {
		// e.printStackTrace();
		// }

		if (reasoner.isConsistent()) {
			if (ax.size() > 0) {
				output = i(name);
			}
			mergedIndivAxioms.add(index, ax);
		}
		return output;
	}

	private void findTerminalClasses(OWLClassExpression desiredClass,
			Set<Path> paths) throws Exception {
		// long starttime = startMethod("findTerminalClasses");
		NodeSet<OWLClass> servicesNodes = reasoner.getSubClasses(c("Service"),
				true);
		Set<OWLClass> services = servicesNodes.getFlattened();
		for (OWLClass s : services) {
			Set<OWLAxiom> ax = new HashSet<OWLAxiom>();
			try {
				ax.add(_indiv("test", desiredClass));
				// TODO more than one equiv class
				Set<OWLClassExpression> equivs = s.getEquivalentClasses(ont);
				if (equivs.size() != 1) {
					throw new Exception(
							String.format(
									"Unexpected number of equivalent classes for %s: %d",
									s, equivs.size()));
				}
				OWLClassExpression equiv = equivs.iterator().next();

				OWLClassExpression restrict = getFirstRestriction(equiv,
						"hasOutputClass");

				mgr.addAxioms(ont, ax);
				reasoner.flush();
				// ob.save(savePoint);

				if (ob.checkEntailed(_indiv("test", restrict))) {
					Path p = new Path(desiredClass);
					p.add(s);
					paths.add(p);

				}
			} finally {
				mgr.removeAxioms(ont, ax);
				reasoner.flush();
			}
		}
		// System.out.println("findTerminalClasses: "
		// + (System.currentTimeMillis() - starttime));
	}

	private OWLClassExpression getFirstRestriction(OWLClassExpression ce,
			String prop) throws Exception {
		// long start = startMethod("getFirstRestriction");
		RestrictionCollector orcc = new RestrictionCollector(prop, ont);
		ce.accept(orcc);
		Set<OWLClassExpression> restricts = orcc.getObjects();
		// TODO more than one restriction
		if (restricts.size() != 1) {
			throw new Exception(String.format(
					"Unexpected number of restriction classes for %s: %d", ce,
					restricts.size()));
		}
		OWLClassExpression restrict = (OWLClassExpression) restricts.iterator()
				.next();
		// methodTime("getFirstRestriction", start);
		return restrict;
	}

	private boolean checkPathAgainstInput(OWLIndividual input, Path p)
			throws Exception {
		// long start = startMethod("Start checkPathAgainstInput: " +
		// p.getDesiredClass());
		boolean output = true;
		OWLObject testInput = input;
		Set<OWLAxiom> ax = new HashSet<OWLAxiom>();
		Iterator<Step> pathIt = p.iterator();
		List<Integer> mergeds = new ArrayList<Integer>();
		List<Integer> dummies = new ArrayList<Integer>();
		while (pathIt.hasNext() && output) {
			Step s = pathIt.next();
			if (s instanceof SimpleStep) {
				try {
					OWLClass c = (OWLClass) s.get();
					OWLClassExpression restrict = getFirstRestriction(c,
							"hasInputClass");

					OWLIndividual tester = null;
					if (testInput instanceof OWLIndividual) {
						tester = (OWLIndividual) testInput;
					} else {
						ax.add(_indiv("tester", (OWLClassExpression) testInput));
						tester = i("tester");
					}
					if (ob.checkEntailed(_indiv(tester, restrict))) {
						output = true;
						// Now make the output restriction the input of the next
						// element
						restrict = getFirstRestriction(c, "hasOutputClass");
						testInput = restrict;
					} else {
						output = false;
					}

				} finally {
					mgr.removeAxioms(ont, ax);
					reasoner.flush();
				}
			} else if (s instanceof SplitStep) {
				Set<OWLIndividual> splitIndivs = new HashSet<OWLIndividual>();
				for (Path sp : ((SplitStep) s).get()) {
					int dummyNo = dummyCount++;
					OWLIndividual i = abduce(input, sp.getDesiredClass(), true,
							sp, false, dummyNo);
					dummies.add(dummyNo);
					splitIndivs.add(i);
				}
				int mergeNo = mergedCount++;
				testInput = mergeIndividuals(splitIndivs, mergeNo);
				mergeds.add(mergeNo);
			} else if (s instanceof ChoiceStep) {
				Iterator<Path> iter = ((ChoiceStep) s).get().iterator();
				while (iter.hasNext()) {
					Path sp = iter.next();
					int dummyNo = dummyCount++;
					try {
						OWLIndividual i = abduce(input, sp.getDesiredClass(),
								false, sp, false, dummyNo);
						dummies.add(dummyNo);
						if (i == null) {
							continue;
						} else {
							// TODO I think this is wrong; Need to check for
							// best path
							// at the end
							((ChoiceStep) s).setBestPath(sp);
							testInput = i;
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}

				}
			}
		}
		for (int i : mergeds) {
			Set<OWLAxiom> max = mergedIndivAxioms.get(i);
			mgr.removeAxioms(ont, max);
			reasoner.flush();
		}
		for (int i : dummies) {
			mgr.removeAxioms(ont, dummyAxioms);
			reasoner.flush();
		}
		// methodTime("checkPathAgainstInput: " + p.getDesiredClass(), start);
		return output;
	}

	class RestrictionCollector extends OWLClassExpressionVisitorAdapter {
		private String property;
		private Set<OWLClassExpression> restrictions;
		private OWLOntology ont;

		RestrictionCollector(String property, OWLOntology ont) {
			super();
			this.property = property;
			this.ont = ont;
			restrictions = new HashSet<OWLClassExpression>();
		}

		public Set<OWLClassExpression> getObjects() {
			return restrictions;
		}

		@Override
		public void visit(OWLObjectIntersectionOf desc) {
			for (OWLClassExpression oce : desc.getOperands()) {
				oce.accept(this);
			}
		}

		@Override
		public void visit(OWLObjectSomeValuesFrom desc) {
			if (desc.getProperty().equals(op(property))) {
				restrictions.add((OWLClassExpression) desc.getFiller());
			}
		}

		@Override
		public void visit(OWLClass desc) {
			for (OWLClassExpression oce : desc.getSubClasses(ont)) {
				oce.accept(this);
			}
			for (OWLClassExpression oce : desc.getEquivalentClasses(ont)) {
				oce.accept(this);
			}
		}
	}

	private Set<OWLClass> findServiceOutputsForInput(
			OWLClassExpression inputClass, Path p) {
		// long start = startMethod("findServiceOutputsForInput");
		Set<OWLClass> outputs = new HashSet<OWLClass>();
		NodeSet<OWLClass> servicesNodes = reasoner.getSubClasses(c("Service"),
				true);
		Set<OWLClass> services = servicesNodes.getFlattened();
		for (OWLClass s : services) {
			if (s.equals(inputClass) || p.contains(s)) {
				continue;
			}
			Set<OWLAxiom> ax = new HashSet<OWLAxiom>();
			try {
				OWLClassExpression restrict = getFirstRestriction(s,
						"hasOutputClass");
				ax.add(_indiv("tester", inputClass));
				if (ob.checkEntailed(_indiv("tester", restrict))) {
					outputs.add(s);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				mgr.removeAxioms(ont, ax);
				reasoner.flush();
			}
		}
		// methodTime("findServiceOutputsForInput", start);
		return outputs;
	}

	private Set<Path> extendPath(Path p) throws Exception {
		// long start = startMethod("extendPath: " + p);
		System.out.println("Start extendPath: " + p);
		Set<Path> extendedPaths = new HashSet<Path>();
		System.out.println("Extend path");

		Iterator<Step> pIter = p.iterator();

		// Find the input of the top Service in the path.
		Step first = pIter.next();
		OWLClass c = (OWLClass) first.get();
		OWLClassExpression inputClass = getFirstRestriction(c, "hasInputClass");
		extendInputClass(inputClass, extendedPaths, p);

		for (Path ep : extendedPaths) {
			System.out.println(ep);
		}
		// methodTime("extendPath: " + p, start);
		return extendedPaths;
	}

	class ExtendInputVisitor extends OWLClassExpressionVisitorAdapter {
		private Path origPath;
		private Set<Path> paths;
		private OWLClassExpression inputClass;

		ExtendInputVisitor(OWLClassExpression inputClass, Set<Path> paths,
				Path origPath) {
			this.inputClass = inputClass;
			this.paths = paths;
			this.origPath = origPath;
		}

		@Override
		public void visit(OWLClass desc) {
			OWLClass oc = notSameOrNothing(desc, inputClass);
			if (oc != null) {
				extendInputClass(oc, paths, origPath);
			}
		}

		@Override
		public void visit(OWLObjectIntersectionOf desc) {
			SplitStep ss = new SplitStep();
			origPath.add(ss);
			int idx = 0;
			for (OWLClassExpression oce : desc.getOperands()) {
				origPath.getSplit(idx++, oce);
			}
			paths.add(origPath);
		}

		@Override
		public void visit(OWLObjectUnionOf desc) {
			ChoiceStep cs = new ChoiceStep();
			origPath.add(cs);
			int idx = 0;
			for (OWLClassExpression oce : desc.getOperands()) {
				origPath.getChoice(idx++, oce);
			}
			paths.add(origPath);
		}
	}

	private boolean checkDirectOutputs(OWLClassExpression testClass,
			Set<Path> paths, Path origPath) {
		// long start = startMethod("checkDirectOutputs");
		boolean status = false;
		Set<OWLClass> outputs = findServiceOutputsForInput(testClass, origPath);
		if (outputs.size() > 0) {
			status = true;
			OWLClass toAdd = outputs.iterator().next();
			origPath.add(toAdd);
			paths.add(origPath);
		}
		// methodTime("checkDirectOutputs", start);
		return status;
	}

	private void extendInputClass(OWLClassExpression inputClass,
			Set<Path> paths, Path origPath) {
		// long start = startMethod("extendInputClass");
		// Try to match the input of this service with the output of another
		boolean hasDirect = checkDirectOutputs(inputClass, paths, origPath);
		if (!(hasDirect)) {
			// Look at the subclasses and equivalent classes
			ExtendInputVisitor eiv = new ExtendInputVisitor(inputClass, paths,
					origPath);

			// TODO What happens if it's not an explicit OWL Class?
			if (inputClass instanceof OWLClass) {
				Set<OWLClassExpression> subclasses = ((OWLClass) inputClass)
						.getSubClasses(ont);
				for (OWLClassExpression oce : subclasses) {
					// Check this subclass as the possible output of a Service
					if (!(checkDirectOutputs(oce, paths, origPath))) {
						// O.W. break it down further
						oce.accept(eiv);
					}
				}
				Set<OWLClassExpression> equivalents = ((OWLClass) inputClass)
						.getEquivalentClasses(ont);
				for (OWLClassExpression oce : equivalents) {
					// Check this equivalent class as the possible output of a
					// Service
					if (!(checkDirectOutputs(oce, paths, origPath))) {
						// O.W. break it down further
						oce.accept(eiv);
					}
				}
			} else if (inputClass instanceof OWLObjectIntersectionOf) {
				inputClass.accept(eiv);
			} else if (inputClass instanceof OWLObjectUnionOf) {
				inputClass.accept(eiv);
			}
		}
		// methodTime("extendInputClass", start);
	}
}
