package edu.yale.med.krauthammerlab.abfab.old.service;

import org.dyndns.norbrand.OntologyBuilder;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

public abstract class AbstractService implements Service {

   protected OntologyBuilder ob;
   protected OWLDataFactory df;
   protected OWLOntology ont;

   public AbstractService() {
      ob = OntologyBuilder.instance();
      df = ob.getDataFactory();
      ont = ob.getOntology();
   }

   @Override
   public abstract OWLIndividual exec(OWLIndividual input)
         throws AbfabServiceException;

   @Override
   public double cost() {
      return 1.0d;
   }

}
