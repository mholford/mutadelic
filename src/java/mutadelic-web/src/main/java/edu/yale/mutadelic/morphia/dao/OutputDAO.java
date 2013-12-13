package edu.yale.mutadelic.morphia.dao;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.QueryResults;

import com.mongodb.Mongo;

import edu.yale.mutadelic.morphia.entities.Output;
import edu.yale.mutadelic.morphia.entities.Workflow;

public class OutputDAO extends MutadelicDAO<Output, ObjectId> {

	@Inject
	public OutputDAO(Class<Output> entityClass, Mongo mongo, Morphia morphia,
			String mongoDBName) {
		super(entityClass, mongo, morphia, mongoDBName);
	}

	public List<Output> findByUserId(Integer uid) {
		List<Output> output = new ArrayList<>();
		QueryResults<Output> qr = find(getDatastore().createQuery(Output.class)
				.filter("owner =", uid));
		output = qr.asList();
		return output;
	}

	public List<Output> findByWorkflowId(Integer wid) {
		List<Output> output = new ArrayList<>();
		QueryResults<Output> qr = find(getDatastore().createQuery(Output.class)
				.filter("workflow =", wid));
		output = qr.asList();
		return output;
	}
	
	public List<Output> findByInputId(Integer iid) {
		List<Output> output = new ArrayList<>();
		QueryResults<Output> qr = find(getDatastore().createQuery(Output.class)
				.filter("input =", iid));
		output = qr.asList();
		return output;
	}
}
