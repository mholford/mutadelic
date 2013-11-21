package edu.yale.mutadelic.morphia;

import java.net.UnknownHostException;

import com.google.code.morphia.Morphia;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.mongodb.MongoClient;

public class MorphiaTestModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(String.class).annotatedWith(Names.named("mongoDB")).toInstance(
				"mut_web_test");
	}

	@Provides
	public Morphia provideMorphia() {
		Morphia m = new MorphiaConfig().getMorphia();
		return m;
	}
	
	@Provides
	public MongoClient provideMongo() {
		MongoClient mongo = null;
		try {
			mongo = new MongoClient("localhost", 27017);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return mongo;
	}
}
