package edu.yale.abfab.owlapi;

import edu.yale.dlgen.controller.DLController;
import edu.yale.dlgen.controller.FactPPDLController;

public class FactPPAbductor extends OWLAPIAbductor {

	public FactPPAbductor(String name) {
		super();
	}

	@Override
	public DLController initSpecificDLController() {
		return new FactPPDLController();
	}

}
