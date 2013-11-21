package edu.yale.mutadelic.morphia.dao;

import javax.inject.Inject;
import javax.inject.Named;

import org.bson.types.ObjectId;

import com.google.code.morphia.Morphia;
import com.google.code.morphia.dao.BasicDAO;
import com.mongodb.Mongo;

import edu.yale.mutadelic.morphia.entities.User;
import edu.yale.mutadelic.pipeline.model.Variant;

public class VariantDAO extends BasicDAO<Variant, ObjectId> {

	@Inject
	public VariantDAO(Class<Variant> entityClass, Mongo mongo, Morphia morphia,
			@Named("mongoDB") String mongoDBName) {
		super(entityClass, mongo, morphia, mongoDBName);
	}

}
