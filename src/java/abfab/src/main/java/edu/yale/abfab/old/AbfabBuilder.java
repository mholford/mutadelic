package edu.yale.med.krauthammerlab.abfab.old;

import org.dyndns.norbrand.Builder;
import org.semanticweb.owlapi.model.OWLException;

import static org.dyndns.norbrand.Utils.*;

public class AbfabBuilder extends Builder {

    @Override
    public void buildClasses() throws OWLException {
        ob_prop("hasInputClass");
        ob_prop("hasOutputClass");

        clazz("Service");
    }
}
