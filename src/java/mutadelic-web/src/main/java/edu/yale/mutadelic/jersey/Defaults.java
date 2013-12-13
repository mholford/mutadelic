package edu.yale.mutadelic.jersey;

import edu.yale.mutadelic.morphia.entities.User;
import edu.yale.mutadelic.morphia.entities.Workflow;

public class Defaults {

	public static User getDefaultUser() {
		User u = new User();
		u.setEmail("matt.holford@gmail.com");
		u.setName("Mutadelic User");
		u.setId(2);
		
		return u;
	}
	
	public static Workflow getDefaultWorkflow() {
		return new Workflow();
	}
}
