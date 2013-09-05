package edu.yale.abfab.owlapi;

import edu.yale.dlgen.controller.DLController;
import edu.yale.dlgen.controller.HermitDLController;

public class HermitAbductor extends OWLAPIAbductor {
	
	public HermitAbductor(String name){
		super();
	}

	@Override
	public DLController initSpecificDLController() {
		return new HermitDLController();
	}

}
