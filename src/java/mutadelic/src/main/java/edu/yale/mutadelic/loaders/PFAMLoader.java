package edu.yale.mutadelic.loaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import edu.yale.mutadelic.mongo.MongoConnection;
import static edu.yale.mutadelic.mongo.MongoConnection.*;

public class PFAMLoader {

	public static void main(String[] args) {
		try {
			new PFAMLoader().init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Map<String, SortedSet<PFAMEntry>> pfamMap;

	private void init() throws Exception {
		pfamMap = new HashMap<>();

		BufferedReader br = new BufferedReader(new FileReader(new File(
				"pfam_output")));
		br.readLine();
		String s;

		while ((s = br.readLine()) != null) {
			String[] ss = s.split("\\s+");
			String term = ss[0];
			String startPre = ss[1];
			String endPre = ss[2];
			String protein = ss[3];
			int start = Integer.parseInt(startPre);
			int end = Integer.parseInt(endPre);
			PFAMEntry e = new PFAMEntry(term, start, end);

			if (!pfamMap.containsKey(protein)) {
				pfamMap.put(protein, new TreeSet<PFAMEntry>());
			}

			pfamMap.get(protein).add(e);
		}

		br.close();

		DBCollection table = MongoConnection.instance().getPFAMTable();

		for (String prot : pfamMap.keySet()) {
			DBObject dbe = new BasicDBObject();
			dbe.put(MONGO_ID, prot);
			List<DBObject> elist = new ArrayList<DBObject>();
			for (PFAMEntry e : pfamMap.get(prot)) {
				DBObject eo = new BasicDBObject();
				eo.put(PFAM_DOMAIN_NAME, e.term);
				eo.put(PFAM_DOMAIN_START, e.start);
				eo.put(PFAM_DOMAIN_END, e.end);
				elist.add(eo);
			}
			dbe.put(PFAM_DOMAINS, elist);
			table.insert(dbe);
		}

	}

	class PFAMEntry implements Comparable<PFAMEntry> {
		String term;
		Integer start;
		Integer end;

		public PFAMEntry(String term, Integer start, Integer end) {
			this.term = term;
			this.start = start;
			this.end = end;
		}

		@Override
		public int compareTo(PFAMEntry other) {
			if (start.equals(other.start)) {
				return end.compareTo(other.end);
			}
			return start.compareTo(other.start);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((end == null) ? 0 : end.hashCode());
			result = prime * result + ((start == null) ? 0 : start.hashCode());
			result = prime * result + ((term == null) ? 0 : term.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PFAMEntry other = (PFAMEntry) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (end == null) {
				if (other.end != null)
					return false;
			} else if (!end.equals(other.end))
				return false;
			if (start == null) {
				if (other.start != null)
					return false;
			} else if (!start.equals(other.start))
				return false;
			if (term == null) {
				if (other.term != null)
					return false;
			} else if (!term.equals(other.term))
				return false;
			return true;
		}

		private PFAMLoader getOuterType() {
			return PFAMLoader.this;
		}
	}
}
