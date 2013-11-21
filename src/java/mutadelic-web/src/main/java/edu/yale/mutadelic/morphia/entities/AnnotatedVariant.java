package edu.yale.mutadelic.morphia.entities;

import java.util.Map;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Reference;

@Embedded
public class AnnotatedVariant {
	
	@Reference(lazy = true)
	Variant variant;
	
	Map<String, String> values;

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
}