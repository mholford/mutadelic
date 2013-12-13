package edu.yale.mutadelic.morphia.dao;

import javax.inject.Inject;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;

import com.mongodb.Mongo;

import edu.yale.mutadelic.morphia.entities.Variant;

public class VariantDAO extends MutadelicDAO<Variant, ObjectId> {

	@Inject
	public VariantDAO(Class<Variant> entityClass, Mongo mongo, Morphia morphia,
			String mongoDBName) {
		super(entityClass, mongo, morphia, mongoDBName);
	}

}
