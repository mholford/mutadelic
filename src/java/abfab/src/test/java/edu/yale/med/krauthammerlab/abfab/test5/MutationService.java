package edu.yale.med.krauthammerlab.abfab.test5;

import static org.dyndns.norbrand.Utils.*;

import java.util.Set;

import org.dyndns.norbrand.NS;
import org.dyndns.norbrand.NS.SIO;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import edu.yale.med.krauthammerlab.abfab.old.service.AbfabServiceException;
import edu.yale.med.krauthammerlab.abfab.old.vep.Mutation;
import edu.yale.med.krauthammerlab.abfab.old.vep.VEPExecutor;

/**
 * Creates a mutation object to feed to VEP, given the standard inputs.
 * @author matt
 *
 */
public abstract class MutationService extends AnnotatedService {

   public Mutation getMutation(OWLIndividual input)
         throws AbfabServiceException {
      Set<OWLIndividual> loci = input.getObjectPropertyValues(op(NS.GELO,
            "has_locus"), ont);
      if (loci.size() != 1) {
         throw new AbfabServiceException(
               "Unexpected number of loci for mutation: " + loci.size());
      }
      OWLIndividual locus = loci.iterator().next();
      
      Set<OWLIndividual> predictions = input.getObjectPropertyValues(op(NS.SIO, SIO.is_modelled_by), ont);
      if (predictions.size() != 1) {
         throw new AbfabServiceException(
               "Unexpected number of predictios for mutation: " + predictions.size());
      }
      OWLIndividual prediction = predictions.iterator().next();
      
      Set<OWLIndividual> chrs = locus.getObjectPropertyValues(op(NS.GELO,
            "on_chromosome"), ont);
      if (chrs.size() != 1) {
         throw new AbfabServiceException(
               "Unexpected number of chromosomes for mutation locus: "
                     + chrs.size());
      }
      OWLNamedIndividual namedChr = (OWLNamedIndividual) chrs.iterator().next();
      String chr = namedChr.getIRI().getFragment();
      if (chr.startsWith("Chr")) {
         chr = chr.substring(3);
      }

      Set<OWLLiteral> startLocs = locus.getDataPropertyValues(dp(NS.GELO, "locus_start"),
            ont);
      if (startLocs.size() != 1) {
         throw new AbfabServiceException(
               "Unexpected number of coordinates for mutation locus: "
                     + startLocs.size());
      }
      int start = startLocs.iterator().next().parseInteger();
      
      Set<OWLLiteral> endLocs = locus.getDataPropertyValues(dp(NS.GELO, "locus_end"),
            ont);
      if (startLocs.size() != 1) {
         throw new AbfabServiceException(
               "Unexpected number of coordinates for mutation locus: "
                     + startLocs.size());
      }
      int end = startLocs.iterator().next().parseInteger();

      Set<OWLLiteral> refs = prediction.getDataPropertyValues(
            dp(NS.SIO, SIO.has_value), ont);
      if (refs.size() != 1) {
         throw new AbfabServiceException("Unexpected number of ref bases: "
               + refs.size());
      }
      String refBase = refs.iterator().next().getLiteral();

      Set<OWLLiteral> muts = locus.getDataPropertyValues(
            dp(NS.GELO, "sequence"), ont);
      if (refs.size() != 1) {
         throw new AbfabServiceException("Unexpected number of mut bases: "
               + refs.size());
      }
      String mutBase = muts.iterator().next().getLiteral();
      
      Set<OWLLiteral> strands = locus.getDataPropertyValues(
            dp(NS.GELO, "strand"), ont);
      if (refs.size() != 1) {
         throw new AbfabServiceException("Unexpected number of strands: "
               + refs.size());
      }
      String strand= strands.iterator().next().getLiteral();
      
      Mutation mut = new Mutation(chr, start, end, refBase, mutBase, strand, "");
      return mut;
   }
}
