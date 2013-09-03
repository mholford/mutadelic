package edu.yale.med.krauthammerlab.abfab.old;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;

public class Path {

   private List<Step> steps;
   int idx = 0;
   private OWLClassExpression desiredClass;

   public Path(OWLClassExpression desiredClass) {
      this.desiredClass = desiredClass;
      steps = new ArrayList<Step>();
   }

   public void add(OWLClass c) {
      steps.add(0, new SimpleStep(c));
      idx = 0;
   }

   public void add(SplitStep ss) {
      steps.add(0, ss);
   }

   public void add(ChoiceStep cs) {
      steps.add(0, cs);
   }

   public Path getSplit(int index, OWLClassExpression goal) {
      SplitStep ss = (SplitStep) steps.get(0);
      if (ss.getNumberOfSubPaths() > index && ss.getSubPath(index) != null) {
         return ss.getSubPath(index);
      } else {
         return ss.addSubPath(goal);
      }
   }

   public Path getChoice(int index, OWLClassExpression goal) {
      ChoiceStep cs = (ChoiceStep) steps.get(0);
      if (cs.getNumberOfSubPaths() > index && cs.getSubPath(index) != null) {
         return cs.getSubPath(index);
      } else {
         return cs.addSubPath(goal);
      }
   }

   public void copy(Path orig) {
      // Shallow copy.
      desiredClass = orig.desiredClass;
      steps = orig.steps;
   }

   public double getCost() {
      if (steps.size() > 0) {
         double total = 0d;
         for (Step s : steps) {
            total += s.getCost();
         }
         return total;
      } else {
         return Double.MAX_VALUE;
      }
   }

   public OWLClassExpression getDesiredClass() {
      return desiredClass;
   }

   public boolean contains(OWLClass c) {
      for (Step s : steps) {
         if (s.get().equals(c)) {
            return true;
         }
      }
      return false;
   }

   public void init() {
      idx = 0;
   }

   @SuppressWarnings("unchecked")
   @Override
   public String toString() {
      StringBuilder output = new StringBuilder();
      Iterator<Step> stepIt = steps.iterator();
      while (stepIt.hasNext()) {
         Step next = stepIt.next();
         Object nextStep = next.get();
         if (nextStep instanceof OWLClass) {
            output.append(nextStep);
         } else if (nextStep instanceof List) {
            output.append("[");
            Iterator<Path> iter = ((List<Path>) nextStep).iterator();
            while (iter.hasNext()) {
               output.append("(" + iter.next() + ")");

               if (iter.hasNext()) {
                  if (next instanceof SplitStep) {
                     output.append("&");
                  } else if (next instanceof ChoiceStep) {
                     output.append("|");
                  }
               }
            }
            output.append("]");
         }

         if (stepIt.hasNext()) {
            output.append("; ");
         }
      }
      return output.toString();
   }

   public Iterator<Step> iterator() {
      return steps.iterator();
   }

}
