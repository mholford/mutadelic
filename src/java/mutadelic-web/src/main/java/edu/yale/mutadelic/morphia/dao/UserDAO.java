package edu.yale.mutadelic.morphia.dao;

import javax.inject.Inject;
import javax.inject.Named;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;

import com.mongodb.Mongo;
import edu.yale.mutadelic.morphia.entities.User;

public class UserDAO extends MutadelicDAO<User, ObjectId>{

	@Inject
	public UserDAO(Class<User> entityClass, Mongo mongo, Morphia morphia,
			@Named("mongoDB") String mongoDBName) {
		super(entityClass, mongo, morphia, mongoDBName);
	}
}
