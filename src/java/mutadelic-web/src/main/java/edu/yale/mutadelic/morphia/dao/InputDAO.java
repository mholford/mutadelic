package edu.yale.mutadelic.morphia.dao;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.QueryResults;

import com.mongodb.Mongo;

import edu.yale.mutadelic.morphia.entities.Input;

public class InputDAO extends MutadelicDAO<Input, ObjectId> {

	@Inject
	public InputDAO(Class<Input> entityClass, Mongo mongo, Morphia morphia,
			String mongoDBName) {
		super(entityClass, mongo, morphia, mongoDBName);
	}
	
	public List<Input> findByUserId(Integer uid) {
		List<Input> output = new ArrayList<>();
		QueryResults<Input> qr = find(getDatastore().createQuery(
				Input.class).filter("owner =", uid));
		output = qr.asList();
		return output;
	}

}
