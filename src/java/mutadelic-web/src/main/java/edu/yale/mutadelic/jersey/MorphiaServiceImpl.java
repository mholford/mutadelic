package edu.yale.mutadelic.jersey;

import java.net.UnknownHostException;

import javax.inject.Singleton;

import org.jvnet.hk2.annotations.Service;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;

import edu.yale.mutadelic.morphia.MorphiaConfig;
import edu.yale.mutadelic.morphia.MorphiaService;
import edu.yale.mutadelic.morphia.dao.InputDAO;
import edu.yale.mutadelic.morphia.dao.OutputDAO;
import edu.yale.mutadelic.morphia.dao.UserDAO;
import edu.yale.mutadelic.morphia.dao.VariantDAO;
import edu.yale.mutadelic.morphia.dao.WorkflowDAO;
import edu.yale.mutadelic.morphia.entities.Input;
import edu.yale.mutadelic.morphia.entities.Output;
import edu.yale.mutadelic.morphia.entities.User;
import edu.yale.mutadelic.morphia.entities.Variant;
import edu.yale.mutadelic.morphia.entities.Workflow;
import edu.yale.mutadelic.pipeline.PipelineExecutor;

@Singleton
@Service
public class MorphiaServiceImpl implements MorphiaService {

	@Override
	public String getMongoDB() {
		return "mut_web_test";
	}

	@Override
	public Morphia getMorphia() {
		Morphia m = new MorphiaConfig().getMorphia();
		return m;
	}

	@Override
	public MongoClient getMongoClient() {
		MongoClient mongo = null;
		try {
			mongo = new MongoClient("localhost", 27017);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return mongo;
	}

	@Override
	public UserDAO getUserDAO() {
		return new UserDAO(User.class, getMongoClient(), getMorphia(),
				getMongoDB());
	}

	@Override
	public WorkflowDAO getWorkflowDAO() {
		return new WorkflowDAO(Workflow.class, getMongoClient(), getMorphia(),
				getMongoDB());
	}

	@Override
	public InputDAO getInputDAO() {
		return new InputDAO(Input.class, getMongoClient(), getMorphia(),
				getMongoDB());
	}

	@Override
	public OutputDAO getOutputDAO() {
		return new OutputDAO(Output.class, getMongoClient(), getMorphia(),
				getMongoDB());
	}

	@Override
	public VariantDAO getVariantDAO() {
		return new VariantDAO(Variant.class, getMongoClient(), getMorphia(),
				getMongoDB());
	}

	@Override
	public PipelineExecutor getPipelineExecutor() {
		return new PipelineExecutor();
	}
}
