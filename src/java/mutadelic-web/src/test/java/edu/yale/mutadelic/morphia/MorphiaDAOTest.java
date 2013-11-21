package edu.yale.mutadelic.morphia;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.ext.guice.GuiceExtension;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.mongodb.MongoClient;

import edu.yale.mutadelic.morphia.dao.InputDAO;
import edu.yale.mutadelic.morphia.dao.OutputDAO;
import edu.yale.mutadelic.morphia.dao.UserDAO;
import edu.yale.mutadelic.morphia.dao.WorkflowDAO;
import edu.yale.mutadelic.morphia.entities.AnnotatedVariant;
import edu.yale.mutadelic.morphia.entities.Input;
import edu.yale.mutadelic.morphia.entities.Output;
import edu.yale.mutadelic.morphia.entities.User;
import edu.yale.mutadelic.morphia.entities.Variant;
import edu.yale.mutadelic.morphia.entities.Workflow;
import fixy.Fixy;
import fixy.MorphiaFixyBuilder;

public class MorphiaDAOTest {

	private Injector injector;
	private Morphia morphia;
	private MongoClient mongo;
	private String mongoDBName;

	@Before
	public void setUp() {
		injector = Guice.createInjector(new MorphiaTestModule());
		morphia = injector.getInstance(Morphia.class);
		mongo = injector.getInstance(MongoClient.class);
		mongoDBName = injector.getInstance(Key.get(String.class,
				Names.named("mongoDB")));
		/* Learn Guice about Morphia */
		new GuiceExtension(morphia, injector);
		/* Clear out db at start of each test */
		mongo.dropDatabase(mongoDBName);
	}

	@Test
	public void testUserDAO() {
		try {
			UserDAO userDAO = new UserDAO(User.class, mongo, morphia,
					mongoDBName);
			User u = new User();
			u.setEmail("abc@bcd.com");
			u.setName("Joe");
			userDAO.save(u);

			User found = userDAO.findOne("name", "Joe");
			assertEquals(found.getEmail(), "abc@bcd.com");
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testFixy() {
		try {
			UserDAO userDAO = new UserDAO(User.class, mongo, morphia,
					mongoDBName);
			WorkflowDAO workflowDAO = new WorkflowDAO(Workflow.class, mongo,
					morphia, mongoDBName);
			InputDAO inputDAO = new InputDAO(Input.class, mongo, morphia,
					mongoDBName);
			OutputDAO outputDAO = new OutputDAO(Output.class, mongo, morphia,
					mongoDBName);

			Datastore ds = userDAO.getDatastore();
			Fixy fixtures = new MorphiaFixyBuilder(ds).build();
			fixtures.load("fixy/Story1.yml");

			User found = userDAO.findOne("name", "Joe");
			assertEquals(found.getEmail(), "joe@gmail.com");

			Workflow w2 = workflowDAO.findOne("name", "Workflow 1");
			User uw2 = w2.getOwner();
			assertEquals("Joe", uw2.getName());

			Input i1 = inputDAO.findOne("name", "Input 1");
			List<Variant> vars = i1.getVariants();
			assertEquals(2, vars.size());
			Collections.sort(vars, new Comparator<Variant>() {

				@Override
				public int compare(Variant o1, Variant o2) {
					if (o1.getChromosome().equals(o2.getChromosome())) {
						return ((Integer) o1.getStart()).compareTo(o2
								.getStart());
					}
					return o1.getChromosome().compareTo(o2.getChromosome());
				}
			});
			Variant v1 = vars.get(0);
			assertEquals("A", v1.getObserved());

			Output o1 = outputDAO.findOne("input", i1);
			List<AnnotatedVariant> results = o1.getResults();
			assertEquals(2, results.size());
			Collections.sort(results, new Comparator<AnnotatedVariant>() {

				@Override
				public int compare(AnnotatedVariant o1, AnnotatedVariant o2) {
					Variant v1 = o1.getVariant();
					Variant v2 = o2.getVariant();
					if (v1.getChromosome().equals(v2.getChromosome())) {
						return ((Integer) v1.getStart()).compareTo(v2
								.getStart());
					}
					return v1.getChromosome().compareTo(v2.getChromosome());
				}
			});
			AnnotatedVariant res1 = results.get(0);
			assertEquals("true", res1.getValues().get("rare"));
		} catch (Exception e) {
			e.printStackTrace();
			fail();

		}
	}
}
