package edu.yale.abfab;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClassExpression;
import edu.yale.dlgen.DLDataPropertyExpression;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.DLLiteral;
import edu.yale.dlgen.DLObjectPropertyExpression;
import edu.yale.dlgen.controller.DLController;

public abstract class Step implements Comparable {

	private Abductor abductor;

	public Step(Abductor abductor) {
		this.abductor = abductor;
	}

	public abstract double getCost();

	public abstract IndividualPlus exec(IndividualPlus input, Path contextPath);

	public abstract Collection<IndividualPlus> getInput();

	public abstract Collection<IndividualPlus> getOutput();

	public abstract Step copy();

	public abstract Collection<DLClassExpression> getDLClasses();

	public abstract DLClassExpression<?> getUnifiedClass();

	public Abductor getAbductor() {
		return abductor;
	}

	
}
