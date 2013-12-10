package edu.yale.mutadelic.morphia;

import org.jvnet.hk2.annotations.Contract;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;

import edu.yale.mutadelic.morphia.dao.InputDAO;
import edu.yale.mutadelic.morphia.dao.OutputDAO;
import edu.yale.mutadelic.morphia.dao.UserDAO;
import edu.yale.mutadelic.morphia.dao.VariantDAO;
import edu.yale.mutadelic.morphia.dao.WorkflowDAO;

@Contract
public interface MorphiaService {
	String getMongoDB();
	Morphia getMorphia();
	MongoClient getMongoClient();
	UserDAO getUserDAO();
	WorkflowDAO getWorkflowDAO();
	InputDAO getInputDAO();
	OutputDAO getOutputDAO();
	VariantDAO getVariantDAO();
}
