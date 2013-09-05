package edu.yale.abfab;

import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.controller.DLController;

public class SimpleStep extends Step {

	private IndividualPlus input;
	private IndividualPlus output;
	private DLController dl;
	private DLIndividual<?> service;
	private double cost = 0.0;

	public SimpleStep(DLIndividual<?> service, Abductor abductor) {
		super(abductor);
		this.service = service;
		dl = abductor.getDLController();
		String NS = abductor.getNamespace();
		input = new IndividualPlus(dl
				.getObjectPropertyValues(service,
						dl.objectProp(NS + "has_input")).iterator().next());
		output = new IndividualPlus(dl
				.getObjectPropertyValues(service,
						dl.objectProp(NS + "has_output")).iterator().next());
		cost = Double.parseDouble(dl.getLiteralValue(dl
				.getDataPropertyValues(service, dl.dataProp(NS + "has_cost"))
				.iterator().next()));
	}

	@Override
	public double getCost() {
		return cost;
	}

	@Override
	public IndividualPlus exec(IndividualPlus input) {
		return null;
	}

	@Override
	public IndividualPlus getInput() {
		return input;
	}

	@Override
	public IndividualPlus getOutput() {
		return output;
	}

	@Override
	public SimpleStep copy() {
		SimpleStep out = new SimpleStep(service, getAbductor());
		return out;
	}

	@Override
	public String toString() {
		return dl.getIRI(service);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((service == null) ? 0 : service.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleStep other = (SimpleStep) obj;
		if (service == null) {
			if (other.service != null)
				return false;
		} else if (!dl.getIRI(service).equals(dl.getIRI(other.service)))
			return false;
		return true;
	}
}
