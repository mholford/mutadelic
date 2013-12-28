package edu.yale.mutadelic.morphia.entities;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class ValueEntry {
	
	String key;
	
	String value;
	
	String level;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}
}
