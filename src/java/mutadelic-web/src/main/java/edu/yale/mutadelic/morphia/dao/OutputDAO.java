package edu.yale.mutadelic.morphia.dao;

import javax.inject.Inject;
import javax.inject.Named;

import org.bson.types.ObjectId;

import com.google.code.morphia.Morphia;
import com.google.code.morphia.dao.BasicDAO;
import com.mongodb.Mongo;

import edu.yale.mutadelic.morphia.entities.Output;
import edu.yale.mutadelic.morphia.entities.User;

public class OutputDAO extends BasicDAO<Output, ObjectId>{

	@Inject
	public OutputDAO(Class<Output> entityClass, Mongo mongo, Morphia morphia,
			@Named("mongoDB") String mongoDBName) {
		super(entityClass, mongo, morphia, mongoDBName);
	}

}
