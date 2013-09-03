package edu.yale.med.krauthammerlab.abfab.test5;

import static org.dyndns.norbrand.Utils.c;
import static org.dyndns.norbrand.Utils.i;

import org.semanticweb.owlapi.model.OWLIndividual;

import edu.yale.med.krauthammerlab.abfab.old.service.AbfabServiceException;
import edu.yale.med.krauthammerlab.abfab.old.vep.Mutation;
import edu.yale.med.krauthammerlab.abfab.old.vep.VEPResult;

public class PolyphenValueAnnotatedMutationService extends VEPAnnotatedService {

   @Override
   public OWLIndividual exec(OWLIndividual input) throws AbfabServiceException {
      Mutation mut = getMutation(input);
      VEPResult result = executor.getResult(mut);
      Double polyphen = result.getPolyphen();
      return annotatedDescription(input, i("cs" + ob.next()), c("PolyphenValue"),
            polyphen);
   }

}
