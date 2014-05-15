package edu.yale.abfab;

import java.util.Arrays;
import java.util.Collection;

import edu.yale.abfab.service.Service;
import edu.yale.dlgen.DLClassExpression;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.controller.DLController;

public class SimpleStep2 extends Step2 {

	private DLClassExpression input;
	private DLClassExpression output;
	private DLController dl;
	private DLClassExpression<?> service;
	private String executable;
	private double cost = 0.0;
	
	public SimpleStep2(DLClassExpression<?> service, Abductor abductor) {
		super(abductor);
		this.service = service;
		dl = abductor.getDLController();
		String NS = abductor.getNamespace();
		input = abductor.getServiceInputFiller(service);
		output = abductor.getServiceOutputFiller(service);
		cost = abductor.getServiceCost(service);
		executable = abductor.getServiceExecutable(service);
	}

	@Override
	public double getCost() {
		return cost;
	}

	@Override
	public IndividualPlus exec(IndividualPlus input, Path2 contextPath) {
		IndividualPlus output = null;
		try {
			Service service = (Service) Class.forName(executable).newInstance();
			input.setStop(false);
			output = service.serviceExec(input, getAbductor());
//			output.setStop(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}

	@Override
	public int compareTo(Object o) {
		SimpleStep2 sso = (SimpleStep2) o;
		return dl.getIRI(service).compareTo(dl.getIRI(sso.getService()));
	}

	@Override
	public Collection<DLClassExpression> getInput() {
		return Arrays.asList(new DLClassExpression[] { input });
	}

	@Override
	public Collection<DLClassExpression> getOutput() {
		return Arrays.asList(new DLClassExpression[] { output });
	}

	@Override
	public SimpleStep2 copy() {
		SimpleStep2 out = new SimpleStep2(service, getAbductor());
		return out;
	}

	@Override
	public Collection<DLClassExpression> getDLClasses() {
		return Arrays.asList(new DLClassExpression[] { service });
	}

	@Override
	public DLClassExpression<?> getUnifiedClass() {
		return service;
	}

	public DLClassExpression<?> getService() {
		return service;
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
		SimpleStep2 other = (SimpleStep2) obj;
		if (service == null) {
			if (other.service != null)
				return false;
		} else if (!dl.getIRI(service).equals(dl.getIRI(other.service)))
			return false;
		return true;
	}
}
