package edu.yale.mutadelic.jersey;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class Application extends ResourceConfig {

	public Application() {
		super(JacksonFeature.class, InputResource.class, OutputResource.class,
				UserResource.class, WorkflowResource.class);
	}
}
