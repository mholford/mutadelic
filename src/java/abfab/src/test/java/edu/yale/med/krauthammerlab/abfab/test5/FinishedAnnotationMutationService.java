package edu.yale.med.krauthammerlab.abfab.test5;

import org.semanticweb.owlapi.model.OWLIndividual;
import static org.dyndns.norbrand.Utils.*;

public class FinishedAnnotationMutationService extends VEPAnnotatedService {

   /**
    * Marks the input individual as finished.
    */
   @Override
   public OWLIndividual exec(OWLIndividual input) {
      return annotatedDescription(input, i("cs" + ob.next()),
            c("CompletionStatus"), true);
   }
}
