package edu.yale.mutadelic.loaders;

import static edu.yale.mutadelic.mongo.MongoConnection.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import edu.yale.mutadelic.mongo.MongoConnection;

public class SiftMongoLoader {

	public final static int RANGE_SIZE = 1000;
	public static final String DATA_FOLDER = System.getProperty("user.home")
			+ "/Downloads";
	private Map<String, List<String>> chrTableMap;
	DBCollection siftTable;

	public static void main(String[] args) {
		try {
			new SiftMongoLoader().load();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void load() throws Exception {
		siftTable = MongoConnection.instance().getSiftTable();
		siftTable.drop();
		List<String> allChrs = rangeList(1, 22);
		allChrs.add("X");
		allChrs.add("Y");

		for (String chr : allChrs) {
			File f = new File(String.format("%s/Human_CHR%s.sqlite.gz",
					DATA_FOLDER, chr));
			File g = gunzip(f);
			// File g = new File(String.format("%s/%s", DATA_FOLDER,
			// "Human_CHR1.sqlite"));
			for (String table : tablesForChr(chr)) {
				File s = runSqlite(g.getPath(), table);
				process(s);
				s.delete();
			}
			g.delete();
		}
	}

	private void process(File f) throws Exception {
		int curLow = 0;
		int curHigh = 0;
		String curChr = null;
		String curKey = null;
		String[] curRange = null;
		String curPos = null;
		String curStrand = null;
		String curRef = null;
		String curTranscript = null;
		String curTransPos = null;
		String curAARef = null;
		String curProtein = null;
		String curAAPos = null;
		List<String> obsList = null;
		List<String> obsAAList = null;
		List<String> scoreList = null;

		BufferedReader br = new BufferedReader(new FileReader(f));
		String s;
		while ((s = br.readLine()) != null) {
			String[] ss = s.split("\\|");
			String chr = ss[0];
			String prePos = ss[1];
			String strand = ss[2];
			String transcript = ss[3];
			String protein = ss[4];
			String ref = ss[5];
			String obs = ss[6];
			String transPos = ss[7];
			String aaRef = ss[8];
			String aaObs = ss[9];
			String aaPos = ss[10];
			String score = "";
			if (ss.length > 11) {
				score = ss[11];
			}

			int pos = Integer.parseInt(prePos);
			if (curPos != null && !(curPos.equals(prePos))) {
				String obsListString = pipeList(obsList);
				String aaObsListString = pipeList(obsAAList);
				String scoreListString = pipeList(scoreList);
				String val = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
						curStrand, curPos, curRef, obsListString,
						curTranscript, curTransPos, curAARef, aaObsListString,
						curProtein, curAAPos, scoreListString);
				int rindex = Integer.parseInt(curPos) - curLow;
				try {
					curRange[rindex] = val;
				} catch (Exception e) {
					e.printStackTrace();
				}
				obsList = new ArrayList<>();
				obsAAList = new ArrayList<>();
				scoreList = new ArrayList<>();
			} else if (curPos == null) {
				obsList = new ArrayList<>();
				obsAAList = new ArrayList<>();
				scoreList = new ArrayList<>();
			}
			boolean newRange = false;
			if (!chr.equals(curChr)) {
				curChr = chr;
				newRange = true;
			}
			if (pos > curHigh || (pos - curLow) < 0) {
				curLow = (((pos - 1) / RANGE_SIZE) * RANGE_SIZE) + 1;
				curHigh = curLow + RANGE_SIZE - 1;
				newRange = true;
			}
			if (newRange) {
				if (curRange != null) {
					writeOldRange(curKey, curRange, siftTable);
				}
				curRange = new String[RANGE_SIZE];
				curKey = String.format("%s_%d", curChr.substring(3), curLow);
			}
			
			curPos = prePos;
			curStrand = strand;
			curRef = ref;
			curTranscript = transcript;
			curTransPos = transPos;
			curAARef = aaRef;
			curProtein = protein;
			curAAPos = aaPos;

			obsList.add(obs);
			obsAAList.add(aaObs);
			scoreList.add(score);
		}
		br.close();
	}

	private void writeOldRange(String key, String[] range, DBCollection table) {
		StringBuilder sb = new StringBuilder();
		Iterator<String> riter = Arrays.asList(range).iterator();
		while (riter.hasNext()) {
			String next = String.valueOf(riter.next());
			next = (next != null && !(next.equals("null"))) ? next : "";

			sb.append(next);
			if (riter.hasNext()) {
				sb.append(";");
			}
		}
		DBObject row = new BasicDBObject();
		row.put(MONGO_ID, key);
		row.put(SIFT_VALUES, sb.toString());
		try {
			table.insert(row);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String pipeList(List<String> l) {
		Iterator<String> liter = l.iterator();
		StringBuilder sb = new StringBuilder();
		while (liter.hasNext()) {
			sb.append(liter.next());
			if (liter.hasNext()) {
				sb.append("|");
			}
		}
		return sb.toString();
	}

	private File runSqlite(String sqliteFile, String table) throws Exception {
		System.out.println("Run table: " + table);
		List<String> inv = new ArrayList<>();
		inv.add("sqlite3");
		inv.add(sqliteFile);
		inv.add(String
				.format("select chr, coord2, orn, enst, ensp, "
						+ "nt1, nt2, ntpos2, aa1, aa2, aapos2, score "
						+ "from %s where snp != '' and snp != 'Reference' order by coord2",
						table));
		// inv.add(">");
		// inv.add(table + ".out");
		ProcessBuilder pb = new ProcessBuilder(inv);
		// pb.redirectErrorStream(true);
		File outputFile = new File(String.format("%s/%s.out", DATA_FOLDER,
				table));
		pb.redirectOutput(outputFile);
		Process p = pb.start();
		copyStream(p.getErrorStream(), System.out);

		int x = p.waitFor();
		return outputFile;
	}

	private List<String> tablesForChr(String chr) throws Exception {
		if (chrTableMap == null) {
			chrTableMap = initChrTableMap();
		}
		return chrTableMap.get(chr);
	}

	private Map<String, List<String>> initChrTableMap() throws Exception {
		Map<String, List<String>> out = new HashMap<>();
		BufferedReader br = new BufferedReader(new FileReader(new File(
				DATA_FOLDER + "/bins.list")));
		String s;

		while ((s = br.readLine()) != null) {
			String[] ss = s.split("\t");
			String chr = ss[1];
			String start = ss[2];
			String end = ss[3];
			String table = String.format("chr%s_%s_%s", chr, start, end);
			if (!out.containsKey(chr)) {
				out.put(chr, new ArrayList<String>());
			}
			out.get(chr).add(table);
		}
		return out;
	}

	private static void copyStream(InputStream in, OutputStream out)
			throws Exception {
		while (true) {
			int c = in.read();
			if (c == -1) {
				break;
			}
			out.write((char) c);
		}
	}

	private File gunzip(File f) throws Exception {
		System.out.println("Unzipping file: " + f.getPath());
		List<String> inv = new ArrayList<String>();
		inv.add("gunzip");
		inv.add(f.getPath());
		ProcessBuilder pb = new ProcessBuilder(inv);
		pb.redirectErrorStream(true);
		Process p = pb.start();
		copyStream(p.getInputStream(), System.out);
		int x = p.waitFor();
		f.delete();
		return new File(DATA_FOLDER + "/"
				+ f.getName().substring(0, f.getName().length() - 3));
	}

	private List<String> rangeList(int start, int end) {
		List<String> out = new ArrayList<>();
		for (int i = start; i <= end; i++) {
			out.add(String.valueOf(i));
		}
		return out;
	}
}
