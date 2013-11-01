package edu.yale.mutadelic.loaders;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import edu.yale.mutadelic.mongo.MongoConnection;
import static edu.yale.mutadelic.mongo.MongoConnection.*;


public class OneKMongoLoader {

	public static void main(String[] args) {
		String file = args[0];
		new OneKMongoLoader().load1Kdata(file);
	}
	
	private void load1Kdata(String file) {
		try {
			MongoClient mongo = new MongoClient("localhost", 27017);
			DB db = mongo.getDB(MONGO_DB);
			DBCollection table = db.getCollection(ONE_K_GENOME_TABLE);

			BufferedReader br = new BufferedReader(new FileReader(
					new File(file)));

			String s;
			int i = 0;
			while ((s = br.readLine()) != null) {
				if (++i % 100000 == 0) {
					System.out.println(String.format("%d lines read", i));
				}
				String[] ss = s.split("\t");
				BasicDBObject row = new BasicDBObject();
				String chr = ss[0];
				String pos = ss[1];
				String ref = ss[2];
				String obs = ss[3];
				String refSub = ref.length() >= 5 ? ref.substring(0, 5) : ref;
				String obsSub = obs.length() >= 5 ? obs.substring(0, 5) : obs;
				String key = String.format("%s_%s_%s_%s", chr, pos, refSub,
						obsSub);
				row.put(MONGO_ID, key);
				row.put(ONE_K_POSITION, pos);
				row.put(ONE_K_REFERENCE, ref);
				row.put(ONE_K_CHROMOSOME, chr);
				row.put(ONE_K_OBSERVED, obs);
				row.put(ONE_K_MAF, ss[4]);
				row.put(ONE_K_AFR_MAF, ss[5]);
				row.put(ONE_K_AMR_MAF, ss[6]);
				row.put(ONE_K_ASN_MAF, ss[7]);
				row.put(ONE_K_EUR_MAF, ss[8]);
				try {
					table.insert(row);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
