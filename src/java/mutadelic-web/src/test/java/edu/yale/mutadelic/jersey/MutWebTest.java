package edu.yale.mutadelic.jersey;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.Test;
import org.mongodb.morphia.Datastore;

import com.mongodb.MongoClient;

import edu.yale.mutadelic.morphia.MorphiaService;
import edu.yale.mutadelic.morphia.MorphiaServiceTestImpl;
import edu.yale.mutadelic.morphia.dao.UserDAO;
import edu.yale.mutadelic.morphia.entities.AnnotatedVariant;
import edu.yale.mutadelic.morphia.entities.Input;
import edu.yale.mutadelic.morphia.entities.Output;
import edu.yale.mutadelic.morphia.entities.User;
import edu.yale.mutadelic.morphia.entities.ValueEntry;
import edu.yale.mutadelic.morphia.entities.Variant;
import edu.yale.mutadelic.morphia.entities.Workflow;
import edu.yale.mutadelic.morphia.entities.Workflow.Criterion;
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
	public void testGetWorkflows() {
		try {
			GenericType<List<Workflow>> workflowListType = new GenericType<List<Workflow>>() {
			};
			List<Workflow> workflows = target().path("workflows")
					.request(MediaType.APPLICATION_JSON).get(workflowListType);
			assertEquals(1, workflows.size());

			Workflow w1 = workflows.get(0);
			assertEquals("Workflow 1", w1.getName());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGetUsers() {
		try {
			GenericType<List<User>> userListType = new GenericType<List<User>>() {
			};
			List<User> users = target().path("users")
					.request(MediaType.APPLICATION_JSON).get(userListType);
			assertEquals(1, users.size());

			User u1 = users.get(0);
			assertEquals("Joe", u1.getName());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testAddWorkflow() {
		try {
			Workflow w = new Workflow();
			w.setName("Workflow 2");
			w.setOwner(1);
			Integer resp = target()
					.path("workflows")
					.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(w, MediaType.APPLICATION_JSON),
							Integer.class);
			assertEquals(new Integer(2), resp);

			Workflow w2 = target().path("workflows/2")
					.request(MediaType.APPLICATION_JSON).get(Workflow.class);
			assertEquals("Workflow 2", w2.getName());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testAddUser() {
		try {
			User u = new User();
			u.setName("Reggie");
			u.setEmail("reggie@dunkindonuts.com");
			Integer response = target()
					.path("users")
					.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(u, MediaType.APPLICATION_JSON),
							Integer.class);
			assertEquals(new Integer(2), response);
			User reggie = target().path("users/2")
					.request(MediaType.APPLICATION_JSON).get(User.class);
			assertEquals("Reggie", reggie.getName());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testAddOutput() {
		try {
			Input i = new Input();
			i.setName("Test Input");
			i.setOwner(1);
			i.setVariants(Arrays.asList(new Variant[] { new Variant("9",
					82336654, 82336654, "C", "T", "+") }));
			int iid = target()
					.path("inputs")
					.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(i, MediaType.APPLICATION_JSON),
							Integer.class);

			int oid = target().path("outputs").queryParam("input_id", iid)
					.request(MediaType.APPLICATION_JSON)
					.post(null, Integer.class);
			
			assertEquals(2, oid);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testAddInput() {
		try {
			Input i = new Input();
			i.setName("Reggie's Input");
			i.setOwner(1);
			i.setVariants(new ArrayList<Variant>());
			Integer response = target()
					.path("inputs")
					.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(i, MediaType.APPLICATION_JSON),
							Integer.class);
			assertEquals(new Integer(2), response);
			Input reggie = target().path("inputs/2")
					.request(MediaType.APPLICATION_JSON).get(Input.class);
			assertEquals("Reggie's Input", reggie.getName());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testAddInputVariant() {
		try {
			Variant newV = new Variant();
			newV.setChromosome("6");
			newV.setStart(222);
			newV.setEnd(222);
			newV.setStrand("+");
			newV.setReference("A");
			newV.setObserved("G");
			Integer response = target()
					.path("inputs/1/variants")
					.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(newV, MediaType.APPLICATION_JSON),
							Integer.class);
			assertEquals(new Integer(3), response);

			GenericType<List<Variant>> variantListType = new GenericType<List<Variant>>() {
			};
			List<Variant> vars = target().path("inputs/1/variants")
					.request(MediaType.APPLICATION_JSON).get(variantListType);
			assertEquals(3, vars.size());

			Variant v3 = target().path("inputs/1/variants/3")
					.request(MediaType.APPLICATION_JSON).get(Variant.class);
			assertEquals(new Integer(222), v3.getEnd());
			assertEquals("A", v3.getReference());
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
	public void testGetWorkflow() {
		try {
			Workflow w = target().path("workflows/1")
					.request(MediaType.APPLICATION_JSON).get(Workflow.class);
			assertEquals(w.getName(), "Workflow 1");
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGetInput() {
		try {
			Input i = target().path("inputs/1")
					.request(MediaType.APPLICATION_JSON).get(Input.class);
			assertEquals(i.getName(), "Input 1");
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGetInputVariant() {
		try {
			Variant v = target().path("inputs/1/variants/2")
					.request(MediaType.APPLICATION_JSON).get(Variant.class);
			assertEquals(new Integer(3456), v.getStart());
			assertEquals("T", v.getReference());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGetOutput() {
		try {
			Output o = target().path("outputs/1")
					.request(MediaType.APPLICATION_JSON).get(Output.class);
			assertEquals(new Integer(1), o.getOwner());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGetOutputVariant() {
		try {
			AnnotatedVariant av = target().path("outputs/1/variants/2")
					.request(MediaType.APPLICATION_JSON)
					.get(AnnotatedVariant.class);
			Variant v = av.getVariant();
			assertEquals(new Integer(3456), v.getStart());
			assertEquals("T", v.getReference());
			List<ValueEntry> values = av.getValueEntries();
			assertEquals(2, values.size());
			ValueEntry ve1 = values.get(0);
			assertEquals("false", ve1.getValue());
			ValueEntry ve2 = values.get(1);
			assertEquals("true", ve2.getValue());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGetUserWorkflows() {
		try {
			GenericType<List<Workflow>> workflowListType = new GenericType<List<Workflow>>() {
			};
			List<Workflow> lw = target().path("users/1/workflows")
					.request(MediaType.APPLICATION_JSON).get(workflowListType);
			assertEquals(1, lw.size());

			Workflow w1 = lw.get(0);
			assertEquals("Workflow 1", w1.getName());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGetUserInputs() {
		try {
			GenericType<List<Input>> inputListType = new GenericType<List<Input>>() {
			};
			List<Input> linput = target().path("users/1/inputs")
					.request(MediaType.APPLICATION_JSON).get(inputListType);
			assertEquals(1, linput.size());

			Input i1 = linput.get(0);
			assertEquals("Input 1", i1.getName());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGetUserOutputs() {
		try {
			GenericType<List<Output>> outputListType = new GenericType<List<Output>>() {
			};
			List<Output> lout = target().path("users/1/outputs")
					.request(MediaType.APPLICATION_JSON).get(outputListType);
			assertEquals(1, lout.size());

			Output o1 = lout.get(0);
			assertEquals(new Integer(1), o1.getWorkflow());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGetWorkflowOutputs() {
		try {
			GenericType<List<Output>> outputListType = new GenericType<List<Output>>() {
			};
			List<Output> lout = target().path("workflows/1/outputs")
					.request(MediaType.APPLICATION_JSON).get(outputListType);
			assertEquals(1, lout.size());

			Output o1 = lout.get(0);
			assertEquals(new Integer(1), o1.getInput());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGetInputOutputs() {
		try {
			GenericType<List<Output>> outputListType = new GenericType<List<Output>>() {
			};
			List<Output> lout = target().path("inputs/1/outputs")
					.request(MediaType.APPLICATION_JSON).get(outputListType);
			assertEquals(1, lout.size());

			Output o1 = lout.get(0);
			assertEquals(new Integer(1), o1.getWorkflow());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGetInputVariants() {
		try {
			GenericType<List<Variant>> variantListType = new GenericType<List<Variant>>() {
			};
			List<Variant> lvar = target().path("inputs/1/variants")
					.request(MediaType.APPLICATION_JSON).get(variantListType);
			assertEquals(2, lvar.size());

			Variant v1 = lvar.get(0);
			assertEquals("G", v1.getReference());
			assertEquals("A", v1.getObserved());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGetOutputVariants() {
		try {
			GenericType<List<AnnotatedVariant>> annotatedVariantListType = new GenericType<List<AnnotatedVariant>>() {
			};
			List<AnnotatedVariant> lvar = target().path("outputs/1/variants")
					.request(MediaType.APPLICATION_JSON)
					.get(annotatedVariantListType);
			assertEquals(2, lvar.size());

			AnnotatedVariant av1 = lvar.get(0);
			Variant v1 = av1.getVariant();
			List<ValueEntry> valueEntries = av1.getValueEntries();
			assertEquals("G", v1.getReference());
			assertEquals("A", v1.getObserved());
			assertEquals(2, valueEntries.size());
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
			assertEquals(200, response.getStatus());
			User u = target().path("users/1")
					.request(MediaType.APPLICATION_JSON).get(User.class);
			assertEquals(u.getName(), "Bill");
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testPostWorkflow() {
		try {
			Workflow newW = new Workflow();
			newW.setName("Bilge");
			Response response = target().path("workflows/1")
					.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(newW, MediaType.APPLICATION_JSON));
			assertEquals(200, response.getStatus());

			Workflow w = target().path("workflows/1")
					.request(MediaType.APPLICATION_JSON).get(Workflow.class);
			assertEquals(w.getName(), "Bilge");
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testPostInput() {
		try {
			Input newI = new Input();
			newI.setName("Binput");
			Response response = target().path("inputs/1")
					.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(newI, MediaType.APPLICATION_JSON));
			assertEquals(200, response.getStatus());
			Input i = target().path("inputs/1")
					.request(MediaType.APPLICATION_JSON).get(Input.class);
			assertEquals(i.getName(), "Binput");
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testPostVariant() {
		try {
			Variant newV = new Variant();
			newV.setObserved("G");
			Response response = target().path("inputs/1/variants/2")
					.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(newV, MediaType.APPLICATION_JSON));
			assertEquals(200, response.getStatus());
			Variant v = target().path("inputs/1/variants/2")
					.request(MediaType.APPLICATION_JSON).get(Variant.class);
			assertEquals(v.getObserved(), "G");
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

			GenericType<List<User>> userListType = new GenericType<List<User>>() {
			};
			List<User> users = target().path("users")
					.request(MediaType.APPLICATION_JSON).get(userListType);
			assertEquals(0, users.size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testDeleteInput() {
		try {
			Response response = target().path("inputs/1")
					.request(MediaType.APPLICATION_JSON).delete();
			assertEquals(response.getStatus(), 200);

			Input i = target().path("inputs/1")
					.request(MediaType.APPLICATION_JSON).get(Input.class);
			assertNull(i);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testDeleteOutput() {
		try {
			Response response = target().path("outputs/1")
					.request(MediaType.APPLICATION_JSON).delete();
			assertEquals(response.getStatus(), 200);

			Output o = target().path("outputs/1")
					.request(MediaType.APPLICATION_JSON).get(Output.class);
			assertNull(o);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testDeleteVariant() {
		try {
			Response response = target().path("inputs/1/variants/2")
					.request(MediaType.APPLICATION_JSON).delete();
			assertEquals(response.getStatus(), 200);

			GenericType<List<Variant>> variantListType = new GenericType<List<Variant>>() {
			};
			List<Variant> variants = target().path("inputs/1/variants")
					.request(MediaType.APPLICATION_JSON).get(variantListType);
			assertEquals(1, variants.size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testDeleteWorkflow() {
		try {
			Response response = target().path("workflows/1")
					.request(MediaType.APPLICATION_JSON).delete();
			assertEquals(response.getStatus(), 200);

			GenericType<List<Workflow>> workflowListType = new GenericType<List<Workflow>>() {
			};
			List<Workflow> workflows = target().path("workflows")
					.request(MediaType.APPLICATION_JSON).get(workflowListType);
			assertEquals(0, workflows.size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
