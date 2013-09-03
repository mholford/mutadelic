package edu.yale.abfab.owlapi;

import edu.yale.abfab.Abductor;
import edu.yale.dlgen.controller.DLController;

public abstract class OWLAPIAbductor extends Abductor {

	public OWLAPIAbductor() {
		super();
	}

	@Override
	public DLController initDLController() {
		DLController dl = initSpecificDLController();

		return dl;
	}

	public abstract DLController initSpecificDLController();
}
