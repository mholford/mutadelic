package edu.yale.mutadelic.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import edu.yale.mutadelic.mongo.MongoConnection;
import static edu.yale.mutadelic.mongo.MongoConnection.*;

public class RCMDBMongoLoader {
	public static void main(String[] args) {
		try {
			new RCMDBMongoLoader().load();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Map<String, String> geneTranscriptMap;

	private void load() throws Exception {
		DBCollection table = MongoConnection.instance().getRCMDBTable();
		table.drop();
		geneTranscriptMap = initGeneTranscriptMap();

		BufferedReader br = new BufferedReader(new InputStreamReader(
				RCMDBMongoLoader.class.getClassLoader().getResourceAsStream(
						"rcmdb")));
		String s;
		while ((s = br.readLine()) != null) {
			String[] ss = s.split("\t");
			String disease = ss[0];
			String gene = ss[1];
			String region = ss[2];
			String hgvst = ss[3];
			String hgvsp = ss[4];
			String mutType = ss[5];
			String name = ss[6];
			String pubmed = ss[7];
			String refs = ss[8];
			String transcript = geneTranscriptMap.get(gene);
			String id = String.format("%s:%s", transcript, hgvst);

			DBObject row = new BasicDBObject();
			row.put(MONGO_ID, id);
			row.put(RCMDB_DISEASE, disease);
			row.put(RCMDB_GENE, gene);
			row.put(RCMDB_REGION, region);
			row.put(RCMDB_HGVSP, hgvsp);
			row.put(RCMDB_MUT_TYPE, mutType);
			row.put(RCMDB_MUT_NAME, name);
			row.put(RCMDB_PMID, pubmed);
			row.put(RCMDB_REFS, refs);
			try {
				table.insert(row);
			} catch (Exception e) {
				// Note exception and just don't insert the duplicate
				e.printStackTrace();
			}
		}
	}

	private Map<String, String> initGeneTranscriptMap() {
		Map<String, String> output = new HashMap<>();
		output.put("ANK1", "NM_020476.2");
		output.put("SLC4A1", "NM_000342.3");
		output.put("SPTA1", "NM_003126.2");
		output.put("SPTB", "NM_001024858.2");
		output.put("EPB42", "NM_000119.2");
		return output;
	}
}
