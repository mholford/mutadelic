package edu.yale.mutadelic.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import edu.yale.seqtree.persist.DataMap;
import static edu.yale.mutadelic.mongo.MongoConnection.*;

public class MongoDataMap implements DataMap<String> {

	private DBCollection table;

	@Override
	public void init(boolean build) {
		table = MongoConnection.instance().getCCDSTable();
		if (build)
			table.drop();
	}

	@Override
	public boolean check(String node) {
		DBObject check = new BasicDBObject();
		check.put(MONGO_ID, node);
		DBObject r = table.findOne(check);
		return r != null;
	}

	@Override
	public void putNameToID(String ID, String name) {
		DBObject ins = new BasicDBObject();
		ins.put(MONGO_ID, ID);
		ins.put(CCDS_NAME, name);
		table.insert(ins);
	}

	@Override
	public String getNameFromID(String ID) {
		DBObject check = new BasicDBObject();
		check.put(MONGO_ID, ID);
		DBObject r = table.findOne(check);
		return (String) r.get(CCDS_NAME);
	}

	@Override
	public long size() {
		return 0;
	}

	@Override
	public void postLoad() {

	}

	@Override
	public void close() {
		
	}

}
