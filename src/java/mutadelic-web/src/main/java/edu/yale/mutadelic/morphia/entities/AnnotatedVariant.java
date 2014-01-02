package edu.yale.mutadelic.morphia.entities;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Reference;

import edu.yale.mutadelic.morphia.entities.Workflow.Level;

@Embedded
public class AnnotatedVariant {
	
	@Reference
	Variant variant;
	
	boolean flagged;
	
	String rdf;

	@Embedded
	List<ValueEntry> valueEntries;

	public Variant getVariant() {
		return variant;
	}

	public void setVariant(Variant variant) {
		this.variant = variant;
	}

	public boolean isFlagged() {
		return flagged;
	}

	public void setFlagged(boolean flagged) {
		this.flagged = flagged;
	}

	public String getRdf() {
		return rdf;
	}

	public void setRdf(String rdf) {
		this.rdf = rdf;
	}

	public List<ValueEntry> getValueEntries() {
		return valueEntries;
	}

	public void setValueEntries(List<ValueEntry> valueEntries) {
		this.valueEntries = valueEntries;
	}


}