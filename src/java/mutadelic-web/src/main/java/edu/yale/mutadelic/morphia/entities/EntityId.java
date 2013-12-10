package edu.yale.mutadelic.morphia.entities;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity
public class EntityId {

	@Id
	private String className;

	// this is the actual ID counter, will
	// be incremented atomically
	private Integer counter = 1;

	public EntityId() {
	}

	public EntityId(String className) {
		this.className = className;
	}

	public Integer getCounter() {
		return counter;
	}

	public void setCounter(Integer counter) {
		this.counter = counter;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

}
