package edu.yale.med.krauthammerlab.abfab.test5;

import static org.dyndns.norbrand.Utils.*;

import org.dyndns.norbrand.NS;
import org.dyndns.norbrand.NS.SO;
import org.semanticweb.owlapi.model.OWLIndividual;

import edu.yale.med.krauthammerlab.abfab.old.service.AbfabServiceException;
import edu.yale.med.krauthammerlab.abfab.old.vep.Mutation;
import edu.yale.med.krauthammerlab.abfab.old.vep.VEPResult;

public class GeneAnnotatedMutationService extends VEPAnnotatedService {

   /**
    * Attaches the HGNC for the input obtained by calling VEP
    */
   @Override
   public OWLIndividual exec(OWLIndividual input) throws AbfabServiceException {
      Mutation mut = getMutation(input);
      VEPResult result = executor.getResult(mut);
      String hgnc = result.getHgnc();
      OWLIndividual geneValue = (hgnc == null) ? i("NO_GENE") : i(
            NS.BIO2RDF_GENEID, String.valueOf(hgnc));

      return annotatedDescription(input, geneValue, c(NS.SO, SO.gene));
   }
}
