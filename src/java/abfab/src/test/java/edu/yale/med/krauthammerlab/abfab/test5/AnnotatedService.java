package edu.yale.med.krauthammerlab.abfab.test5;

import org.dyndns.norbrand.NS;
import org.dyndns.norbrand.NS.SIO;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

import edu.yale.med.krauthammerlab.abfab.old.service.AbstractService;
import static org.dyndns.norbrand.Utils.*;

public abstract class AnnotatedService extends AbstractService {

   public OWLIndividual annotatedDescription(OWLIndividual input,
         OWLIndividual descriptor, OWLClassExpression descriptorClass,
         OWLIndividual citation) {
      return annotatedDescription(input, descriptor, descriptorClass, citation,
            null);
   }

   public OWLIndividual annotatedDescription(OWLIndividual input,
         OWLIndividual descriptor, OWLClassExpression descriptorClass,
         OWLIndividual citation, Object value) {
      OWLIndividual anonA = i("desc" + ob.next());
      indiv(anonA, c(NS.SIO, SIO.description));
      indiv(descriptor, descriptorClass);
      op_value(anonA, op(NS.SIO, SIO.cites), citation);
      if (value != null) {
         dp_value(anonA, dp(NS.SIO, SIO.has_value), value);
      }
      op_value(anonA, op(NS.SIO, SIO.refers_to), descriptor);
      op_value(input, op(NS.SIO, SIO.is_described_by), anonA);
      return input;
   }
}
