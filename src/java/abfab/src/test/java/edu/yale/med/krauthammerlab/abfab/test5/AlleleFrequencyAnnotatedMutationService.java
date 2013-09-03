package edu.yale.med.krauthammerlab.abfab.test5;

import static org.dyndns.norbrand.Utils.c;
import static org.dyndns.norbrand.Utils.i;

import org.semanticweb.owlapi.model.OWLIndividual;

import edu.yale.med.krauthammerlab.abfab.old.service.AbfabServiceException;
import edu.yale.med.krauthammerlab.abfab.old.vep.Mutation;
import edu.yale.med.krauthammerlab.abfab.old.vep.VEPResult;

public class AlleleFrequencyAnnotatedMutationService extends OneKAnnotatedService {

   @Override
   public OWLIndividual exec(OWLIndividual input) throws AbfabServiceException {
      Mutation mut = getMutation(input);
      double result = executor.getRate(mut);
      return annotatedDescription(input, i("cs" + ob.next()), c("AlleleFrequency"),
            result);
   }

}
