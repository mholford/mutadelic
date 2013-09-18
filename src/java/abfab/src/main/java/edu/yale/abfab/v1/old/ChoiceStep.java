package edu.yale.med.krauthammerlab.abfab.old;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.semanticweb.owlapi.model.OWLClassExpression;

public class ChoiceStep extends Step {
   List<Path> subPaths;
   Path bestPath;

   public ChoiceStep() {
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
   
   public Path getBestPath() {
      if (bestPath != null) {
         return bestPath;
      }
      Path chosenPath = null;
      double bestScore = Double.MAX_VALUE;
      for (Path path : subPaths) {
         if (path.getCost() <= bestScore) {
            chosenPath = path;
            bestScore = path.getCost();
         }
      }
      return chosenPath;
   }
   
   public void setBestPath(Path bestPath) {
      this.bestPath = bestPath;
   }

   @Override
   public double getCost() {
      return getBestPath().getCost();
   }
}
