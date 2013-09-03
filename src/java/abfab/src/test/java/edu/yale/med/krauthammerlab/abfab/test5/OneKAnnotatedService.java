package edu.yale.med.krauthammerlab.abfab.test5;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

import edu.yale.med.krauthammerlab.abfab.old.vep.OneKExecutor;
import edu.yale.med.krauthammerlab.abfab.old.vep.VEPExecutor;

import static org.dyndns.norbrand.Utils.*;

public abstract class OneKAnnotatedService extends MutationService {
   
   protected OneKExecutor executor;

   public OneKAnnotatedService() {
      executor = OneKExecutor.instance();
   }

   public OWLIndividual annotatedDescription(OWLIndividual input,
         OWLIndividual descriptor, OWLClassExpression descriptorClass,
         Object value) {
      // TODO Auto-generated method stub
      return super.annotatedDescription(input, descriptor, descriptorClass,
            i("1000Genome"), value);
   }

   public OWLIndividual annotatedDescription(OWLIndividual input,
         OWLIndividual descriptor, OWLClassExpression descriptorClass) {
      // TODO Auto-generated method stub
      return super.annotatedDescription(input, descriptor, descriptorClass,
            i("1000Genome"));
   }
}
