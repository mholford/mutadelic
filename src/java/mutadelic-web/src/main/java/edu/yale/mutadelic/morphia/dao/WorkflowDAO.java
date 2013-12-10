package edu.yale.mutadelic.morphia.dao;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;

import com.mongodb.Mongo;

import edu.yale.mutadelic.morphia.entities.Workflow;

public class WorkflowDAO extends MutadelicDAO<Workflow, ObjectId> {

	@Inject
	public WorkflowDAO(Class<Workflow> entityClass, Mongo mongo,
			Morphia morphia, String mongoDBName) {
		super(entityClass, mongo, morphia, mongoDBName);
	}
	
	public List<Workflow> findByUserId(String userId) {
		List<Workflow> output = new ArrayList<>();
		
		return output;
	}

}
