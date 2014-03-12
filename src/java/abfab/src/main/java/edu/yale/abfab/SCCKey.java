package edu.yale.abfab;

import edu.yale.dlgen.DLClassExpression;

public class SCCKey {
	DLClassExpression<?> serviceClass;
	SCCIndividual input;
	SCCIndividual output;

	public SCCKey(DLClassExpression<?> serviceClass, SCCIndividual input,
			SCCIndividual output) {
		super();
		this.serviceClass = serviceClass;
		this.input = input;
		this.output = output;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((input == null) ? 0 : input.hashCode());
		result = prime * result
				+ ((output == null) ? 0 : output.hashCode());
		result = prime * result
				+ ((serviceClass == null) ? 0 : serviceClass.hashCode());
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
		SCCKey other = (SCCKey) obj;
		if (input == null) {
			if (other.input != null)
				return false;
		} else if (!input.equals(other.input))
			return false;
		if (output == null) {
			if (other.output != null)
				return false;
		} else if (!output.equals(other.output))
			return false;
		if (serviceClass == null) {
			if (other.serviceClass != null)
				return false;
		} else if (!serviceClass.equals(other.serviceClass))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SCCKey [serviceClass=" + serviceClass + ", input=" + input
				+ ", output=" + output + "]";
	}
}