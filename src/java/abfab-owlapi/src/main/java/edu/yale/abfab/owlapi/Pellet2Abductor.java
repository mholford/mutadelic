package edu.yale.abfab.owlapi;

import edu.yale.dlgen.controller.DLController;
import edu.yale.dlgen.controller.Pellet2DLController;

public class Pellet2Abductor extends OWLAPIAbductor {

	public Pellet2Abductor() {
		super();
	}

	@Override
	public DLController initSpecificDLController() {
		return new Pellet2DLController();
	}

}
