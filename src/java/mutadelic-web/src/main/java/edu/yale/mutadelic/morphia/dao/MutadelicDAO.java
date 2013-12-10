package edu.yale.mutadelic.morphia.dao;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import com.mongodb.Mongo;

import edu.yale.mutadelic.morphia.entities.EntityId;
import edu.yale.mutadelic.morphia.entities.MutadelicEntity;


public abstract class MutadelicDAO<T, K> extends BasicDAO<T, K> {

	public MutadelicDAO(Class<T> entityClass, Mongo mongo, Morphia morphia,
			String mongoDBName) {
		super(entityClass, mongo, morphia, mongoDBName);
	}
	
	@Override
	public Key<T> save(T t) {
		MutadelicEntity me = (MutadelicEntity) t;
	    if(me.getId() == null) {
	        Integer id = generateId(t);
	        me.setId(id);
	    }
	    return super.save(t);
	}
	
	protected Integer generateId(T entity) {
		Datastore ds = getDatastore(); 
	    // lookup the collection name for the entity
	    String collName = ds.getCollection(getClass()).getName();
	    // find any existing counters for the type
	    Query<EntityId> q = ds.find(EntityId.class, "_id", collName);
	    // create an update operation which increments the counter
	    UpdateOperations<EntityId> update = ds.createUpdateOperations(EntityId.class).inc("counter");
	    // execute on server, if not found null is return,
	    // else the counter is incremented atomically
	    EntityId counter = ds.findAndModify(q, update);
	    if (counter == null) {
	        // so just create one
	        counter = new EntityId(collName);
	        ds.save(counter);
	    }
	    // return new id
	    return counter.getCounter();
	}
	
	public T findById(Class<T> clz, String idString) {
		Integer id = Integer.parseInt(idString);
		Datastore ds = getDatastore();
		return findOne(ds.createQuery(clz).filter("id =", id));
	}
}
