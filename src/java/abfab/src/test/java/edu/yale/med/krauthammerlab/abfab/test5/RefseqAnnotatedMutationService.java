package edu.yale.med.krauthammerlab.abfab.test5;

import static org.dyndns.norbrand.Utils.*;

import org.dyndns.norbrand.NS;
import org.dyndns.norbrand.NS.SIO;
import org.semanticweb.owlapi.model.OWLIndividual;

import edu.yale.med.krauthammerlab.abfab.old.service.AbfabServiceException;
import edu.yale.med.krauthammerlab.abfab.old.vep.Mutation;
import edu.yale.med.krauthammerlab.abfab.old.vep.VEPResult;

public class RefseqAnnotatedMutationService extends VEPAnnotatedService {

   /**
    * Attaches the HGNC for the input obtained by calling VEP
    */
   @Override
   public OWLIndividual exec(OWLIndividual input) throws AbfabServiceException {
      Mutation mut = getMutation(input);

      VEPResult result = executor.getResult(mut);

      String refseq = result.getRefseq();
      OWLIndividual refseqValue = (refseq == null) ? i("NO_REFSEQ") : i(
            NS.BIO2RDF_REFSEQ, String.valueOf(refseq));

      return annotatedDescription(input, refseqValue, c("RefseqSequence"));
   }
}
