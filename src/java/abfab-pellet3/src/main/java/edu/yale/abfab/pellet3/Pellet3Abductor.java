package edu.yale.abfab.pellet3;

import edu.yale.abfab.old.Abductor;
import edu.yale.dlgen.controller.DLController;
import edu.yale.dlgen.owl.pellet3.Pellet3DLController;

public class Pellet3Abductor extends Abductor {

	public Pellet3Abductor(String name) {
		super();
	}

	@Override
	public DLController initDLController() {
		DLController dl = new Pellet3DLController();

		return dl;
	}

}
