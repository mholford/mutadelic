package edu.yale.abfab.v2.owlapi;

import edu.yale.abfab.v2.Abductor;
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
