package edu.yale.mutadelic.morphia;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.google.code.morphia.Morphia;
import com.google.code.morphia.ext.guice.GuiceExtension;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.mongodb.MongoClient;

import edu.yale.mutadelic.morphia.dao.UserDAO;
import edu.yale.mutadelic.morphia.entities.User;

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
		mongoDBName = injector.getInstance(Key.get(String.class, Names.named("mongoDB")));
		new GuiceExtension(morphia, injector);
	}
	
	@Test
	public void testUserDAO() {
		UserDAO userDAO = new UserDAO(User.class, mongo, morphia, mongoDBName);
		User u = new User();
		u.setEmail("abc@bcd.com");
		u.setName("Joe");
		userDAO.save(u);
		
		User found = userDAO.findOne("name", "Joe");
		assertEquals(found.getEmail(), "abc@bcd.com");
	}

}
