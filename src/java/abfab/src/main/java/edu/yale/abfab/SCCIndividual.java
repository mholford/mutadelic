package edu.yale.abfab;

import static edu.yale.abfab.NS.NS;

import java.util.Collection;
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

public class SCCIndividual {
	Collection<DLClassExpression> types;
	Map<DLDataPropertyExpression<?>, Collection<DLLiteral>> dataMap;
	Map<DLObjectPropertyExpression<?>, Collection<SCCIndividual>> objectMap;
	DLController dl;

	public SCCIndividual(DLController dl,
			Collection<DLClassExpression> types,
			Map<DLDataPropertyExpression<?>, Collection<DLLiteral>> dataMap,
			Map<DLObjectPropertyExpression<?>, Collection<SCCIndividual>> objectMap) {
		super();
		this.dl = dl;
		this.types = types;
		this.dataMap = dataMap;
		this.objectMap = objectMap;
	}

	public Set<DLAxiom<?>> getAxioms(DLIndividual<?> indiv) {
		Set<DLAxiom<?>> ax = new HashSet<>();
		for (DLClassExpression type : types) {
			ax.add(dl.individualType(indiv, type));
		}
		for (DLDataPropertyExpression<?> dataProp : dataMap.keySet()) {
			for (DLLiteral<?> val : dataMap.get(dataProp)) {
				ax.add(dl.newDataFact(indiv, dataProp, val));
			}
		}
		for (DLObjectPropertyExpression<?> obProp : objectMap.keySet()) {
			for (SCCIndividual opi : objectMap.get(obProp)) {
				DLIndividual<?> opii = dl.individual(NS
						+ UUID.randomUUID().toString());
				ax.addAll(opi.getAxioms(opii));
				ax.add(dl.newObjectFact(indiv, obProp, opii));
			}
		}
		return ax;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dataMap == null) ? 0 : dataMap.hashCode());
		result = prime * result
				+ ((objectMap == null) ? 0 : objectMap.hashCode());
		result = prime * result + ((types == null) ? 0 : types.hashCode());
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
		SCCIndividual other = (SCCIndividual) obj;
		if (dataMap == null) {
			if (other.dataMap != null)
				return false;
		} else if (!dataMap.equals(other.dataMap))
			return false;
		if (objectMap == null) {
			if (other.objectMap != null)
				return false;
		} else if (!objectMap.equals(other.objectMap))
			return false;
		if (types == null) {
			if (other.types != null)
				return false;
		} else if (!types.equals(other.types))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "SCCIndividual [types=" + types + ", dataMap=" + dataMap
				+ ", objectMap=" + objectMap + "]";
	}
}