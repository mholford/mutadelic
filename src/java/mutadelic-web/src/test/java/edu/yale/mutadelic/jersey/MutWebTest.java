package edu.yale.mutadelic.jersey;

import static org.junit.Assert.*;

import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongodb.morphia.Datastore;

import com.mongodb.MongoClient;

import edu.yale.mutadelic.morphia.MorphiaService;
import edu.yale.mutadelic.morphia.MorphiaServiceTestImpl;
import edu.yale.mutadelic.morphia.dao.UserDAO;
import edu.yale.mutadelic.morphia.entities.User;
import fixy.Fixy;
import fixy.MorphiaFixyBuilder;

public class MutWebTest extends JerseyTest {

	class MorphiaBinder implements Binder {

		@Override
		public void bind(DynamicConfiguration config) {
			config.bind(BuilderHelper.link(MorphiaServiceTestImpl.class)
					.to(MorphiaService.class).build());
		}

	}

	@Override
	protected ResourceConfig configure() {
		enable(TestProperties.LOG_TRAFFIC);
		enable(TestProperties.DUMP_ENTITY);

		ResourceConfig rc = new ResourceConfig();
		rc.packages("edu.yale.mutadelic.jersey");
		rc.registerInstances(new MorphiaBinder(), new JacksonFeature());

		return rc;
	}

	@Override
	protected void configureClient(ClientConfig config) {
		config.register(new JacksonFeature());
	}

	@Before
	public void setup() {
		ServiceLocator serviceLocator = ServiceLocatorFactory.getInstance()
				.create("sl");
		ServiceLocatorUtilities.addClasses(serviceLocator,
				MorphiaServiceTestImpl.class);
		MorphiaService morphiaService = serviceLocator
				.getService(MorphiaService.class);
		MongoClient mongo = morphiaService.getMongoClient();
		String mongoDBName = morphiaService.getMongoDB();
		mongo.dropDatabase(mongoDBName);
		UserDAO userDAO = morphiaService.getUserDAO();
		Datastore ds = userDAO.getDatastore();
		Fixy fixtures = new MorphiaFixyBuilder(ds).build();
		fixtures.load("fixy/Story1.yml");
	}

	@Test
	public void testGetUsers() {
		try {
			List<User> users = target().path("users")
					.request(MediaType.APPLICATION_JSON).get(List.class);
			assertEquals(1, users.size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testAddUsers() {
		try {
			User u = new User();
			u.setName("Reggie");
			u.setEmail("reggie@dunkindonuts.com");
			Response response = target().path("users")
					.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(u, MediaType.APPLICATION_JSON));
			assertEquals(201, response.getStatus());
			User reggie = target().path("users/2")
					.request(MediaType.APPLICATION_JSON).get(User.class);
			assertEquals("Reggie", reggie.getName());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGetUser() {
		try {
			User u = target().path("users/1")
					.request(MediaType.APPLICATION_JSON).get(User.class);
			assertEquals(u.getName(), "Joe");
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testPostUser() {
		try {
			User newU = new User();
			newU.setName("Bill");
			Response response = target().path("users/1")
					.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(newU, MediaType.APPLICATION_JSON));
			System.out.println("Response: " + response);
			User u = target().path("users/1")
					.request(MediaType.APPLICATION_JSON).get(User.class);
			assertEquals(u.getName(), "Bill");
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testDeleteUser() {
		try {
			Response response = target().path("users/1")
					.request(MediaType.APPLICATION_JSON).delete();
			assertEquals(response.getStatus(), 200);
			
			List<User> users = target().path("users")
					.request(MediaType.APPLICATION_JSON).get(List.class);
			assertEquals(0, users.size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
