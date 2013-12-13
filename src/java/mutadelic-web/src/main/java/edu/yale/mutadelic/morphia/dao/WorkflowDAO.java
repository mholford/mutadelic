package edu.yale.mutadelic.morphia.dao;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.QueryResults;

import com.mongodb.Mongo;

import edu.yale.mutadelic.morphia.entities.Workflow;

public class WorkflowDAO extends MutadelicDAO<Workflow, ObjectId> {

	@Inject
	public WorkflowDAO(Class<Workflow> entityClass, Mongo mongo,
			Morphia morphia, String mongoDBName) {
		super(entityClass, mongo, morphia, mongoDBName);
	}

	public List<Workflow> findByUserId(Integer uid) {
		List<Workflow> output = new ArrayList<>();
		QueryResults<Workflow> qr = find(getDatastore().createQuery(
				Workflow.class).filter("owner =", uid));
		output = qr.asList();
		return output;
	}

	public Workflow findByName(String name) {
		return findOne("name", name);
	}
}
