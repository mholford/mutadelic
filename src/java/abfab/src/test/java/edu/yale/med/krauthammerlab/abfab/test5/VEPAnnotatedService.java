package edu.yale.med.krauthammerlab.abfab.test5;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

import edu.yale.med.krauthammerlab.abfab.old.vep.VEPExecutor;

import static org.dyndns.norbrand.Utils.*;

public abstract class VEPAnnotatedService extends MutationService {
   
   protected VEPExecutor executor;

   public VEPAnnotatedService() {
      executor = VEPExecutor.instance(false);
   }

   public OWLIndividual annotatedDescription(OWLIndividual input,
         OWLIndividual descriptor, OWLClassExpression descriptorClass,
         Object value) {
      // TODO Auto-generated method stub
      return super.annotatedDescription(input, descriptor, descriptorClass,
            i("VEP"), value);
   }

   public OWLIndividual annotatedDescription(OWLIndividual input,
         OWLIndividual descriptor, OWLClassExpression descriptorClass) {
      // TODO Auto-generated method stub
      return super.annotatedDescription(input, descriptor, descriptorClass,
            i("VEP"));
   }
}
