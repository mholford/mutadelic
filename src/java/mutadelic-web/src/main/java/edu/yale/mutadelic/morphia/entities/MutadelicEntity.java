package edu.yale.mutadelic.morphia.entities;

import org.mongodb.morphia.annotations.Id;

public class MutadelicEntity {

	@Id
	private Integer id;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
}
