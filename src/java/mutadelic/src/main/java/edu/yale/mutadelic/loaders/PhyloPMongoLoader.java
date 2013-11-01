package edu.yale.mutadelic.loaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Iterator;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import edu.yale.mutadelic.mongo.MongoConnection;
import static edu.yale.mutadelic.mongo.MongoConnection.*;

public class PhyloPMongoLoader {

	public final static int RANGE_SIZE = 1000;
	public final static File INPUT = new File(System.getProperty("user.home")
			+ "/mutadelic/src/java/abfab/phyloP-output");

	public static void main(String[] args) {
		try {
			new PhyloPMongoLoader().loadPhyloP();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeOldRange(String key, Double[] range, DBCollection table) {
		StringBuilder sb = new StringBuilder();
		Iterator<Double> riter = Arrays.asList(range).iterator();
		while (riter.hasNext()) {
			String next = String.valueOf(riter.next());
			next = (next != null && !(next.equals("null"))) ? next : "";
			
			sb.append(next);
			if (riter.hasNext()) {
				sb.append(",");
			}
		}
		DBObject row = new BasicDBObject();
		row.put(MONGO_ID, key);
		row.put(PHYLOP_VALUES, sb.toString());
		try {
			table.insert(row);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadPhyloP() throws Exception {
		DBCollection table = MongoConnection.instance().getPhylopTable();
		table.drop();

		int curLow = 0;
		int curHigh = 0;
		String curChr = null;
		String curKey = null;
		Double[] curRange = null;

		BufferedReader br = new BufferedReader(new FileReader(INPUT));
		String s;
		long cnt = 0;
		while ((s = br.readLine()) != null) {
			if (++cnt % 1000000 == 0) {
				System.out.println(String.format("%d lines read", cnt));
			}
			String[] ss = s.split("\t");
			String chr = ss[0];
			int pos = Integer.parseInt(ss[1]);
			Double val = Double.parseDouble(ss[2]);

			boolean newRange = false;
			if (!chr.equals(curChr)) {
				curChr = chr;
				newRange = true;
			}
			if (pos > curHigh || (pos - curLow) < 0) {
				curLow = (((pos -1) / RANGE_SIZE) * RANGE_SIZE) + 1;
				curHigh = curLow + RANGE_SIZE - 1;
				newRange = true;
			}
			if (newRange) {
				if (curRange != null) {
					writeOldRange(curKey, curRange, table);
				}
				curRange = new Double[RANGE_SIZE];
				curKey = String.format("%s_%d", curChr, curLow);
			}
			int rindex = pos - curLow;
			try {
			curRange[rindex] = val;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		br.close();
	}
}
