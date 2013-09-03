package edu.yale.med.krauthammerlab.abfab.old;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.semanticweb.owlapi.model.OWLClassExpression;

public class SplitStep extends Step {
	List<Path> subPaths;

	public SplitStep() {
		subPaths = new ArrayList<Path>();
	}

	@Override
	public List<Path> get() {
		return subPaths;
	}

	public Iterator<Path> iterator() {
		return subPaths.iterator();
	}
	
	public Path getSubPath(int idx) {
		if (subPaths.size() < idx) {
			return null;
		}
		return subPaths.get(idx);
	}
	
	public int getNumberOfSubPaths() {
		return subPaths.size();
	}
	
	public Path addSubPath(OWLClassExpression oce) {
		Path newPath = new Path(oce);
		subPaths.add(newPath);
		return newPath;
	}

   @Override
   public double getCost() {
      double total = 0d;
      for (Path p : subPaths) {
         total += p.getCost();
      }
      return total;
   }
}
