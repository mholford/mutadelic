package edu.yale.mutadelic.morphia.dao;

import javax.inject.Inject;
import javax.inject.Named;

import org.bson.types.ObjectId;

import com.google.code.morphia.Morphia;
import com.google.code.morphia.dao.BasicDAO;
import com.mongodb.Mongo;

import edu.yale.mutadelic.morphia.entities.User;

public class UserDAO extends BasicDAO<User, ObjectId>{

	@Inject
	public UserDAO(Class<User> entityClass, Mongo mongo, Morphia morphia,
			@Named("mongoDB") String mongoDBName) {
		super(entityClass, mongo, morphia, mongoDBName);
	}

}
