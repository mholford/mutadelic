package edu.yale.mutadelic.morphia.entities;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Reference;

import edu.yale.mutadelic.morphia.entities.Workflow.Level;

@Embedded
public class AnnotatedVariant {
	
	@Reference
	Variant variant;
	
	Map<String, String> values;
	
	Map<String, Level> valueLevels;

	public Variant getVariant() {
		return variant;
	}

	public void setVariant(Variant variant) {
		this.variant = variant;
	}

	public Map<String, String> getValues() {
		return values;
	}

	public void setValues(Map<String, String> values) {
		this.values = values;
	}

	public Map<String, Level> getValueLevels() {
		return valueLevels;
	}

	public void setValueLevels(Map<String, Level> valueLevels) {
		this.valueLevels = valueLevels;
	}
}