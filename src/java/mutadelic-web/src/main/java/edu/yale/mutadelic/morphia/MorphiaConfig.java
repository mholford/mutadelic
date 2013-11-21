package edu.yale.mutadelic.morphia;

import javax.inject.Singleton;

import com.google.code.morphia.Morphia;

@Singleton
public class MorphiaConfig {

	public Morphia getMorphia() {
		Morphia morphia = new Morphia();
		morphia.mapPackage("edu.yale.mutadelic.morphia.entities");
		return morphia;
	}
}
