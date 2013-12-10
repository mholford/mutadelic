package edu.yale.mutadelic.morphia.entities;

import org.mongodb.morphia.annotations.Entity;

@Entity(value = "users")
public class User extends MutadelicEntity {

	public User() {
		super();
	}

	private String name;

	private String email;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
