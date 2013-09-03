package edu.yale.med.krauthammerlab.abfab.test5;

import static org.dyndns.norbrand.Utils.*;

import org.dyndns.norbrand.NS;
import org.semanticweb.owlapi.model.OWLIndividual;

import edu.yale.med.krauthammerlab.abfab.old.service.AbfabServiceException;
import edu.yale.med.krauthammerlab.abfab.old.vep.Mutation;
import edu.yale.med.krauthammerlab.abfab.old.vep.VEPResult;

/**
 * Attaches the SNP id if known obtained by calling VEP
 * 
 * @author matt
 * 
 */
public class SNPAnnotatedMutationService extends VEPAnnotatedService {

   @Override
   public OWLIndividual exec(OWLIndividual input) throws AbfabServiceException {
      Mutation mut = getMutation(input);
      VEPResult result = executor.getResult(mut);

      String snp = result.getExisting();
      OWLIndividual snpValue = (snp == null || snp.equals("")) ? i("NO_SNP")
            : i(NS.LSRN_DBSNP, String.valueOf(snp));

      return annotatedDescription(input, snpValue, c("DBSNPMutation"));
   }

}