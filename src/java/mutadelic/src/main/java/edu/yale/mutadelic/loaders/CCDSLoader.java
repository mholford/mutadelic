package edu.yale.mutadelic.loaders;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import edu.yale.mutadelic.mongo.MongoConnection;
import edu.yale.mutadelic.mongo.MongoDataMap;
import edu.yale.seqtree.SeqtreeManager;
import edu.yale.seqtree.Sequence;
import static edu.yale.mutadelic.mongo.MongoConnection.*;

public class CCDSLoader {

	private static final String FILE = "/home/matt/Downloads/CCDS.current.txt";
	private static final String REF_FILE = "/home/matt/Downloads/CCDS2Sequence.current.txt";
	private static final String NIO_PATH = "/home/matt/nio/ccds";

	public static void main(String[] args) {
		CCDSLoader l = new CCDSLoader();
		try {
			if (args[0].equals("parse-ccds")) {
				l.init();
			} else if (args[0].equals("build-seqtree")) {
				l.buildSeqTree();
			} else if (args[0].equals("build-seq-pos-table")) {
				l.buildSeqPosTable();
			} else if (args[0].equals("build-seq-ref-table")) {
				l.buildSeqRefTable();
			} else if (args[0].equals("test-query")) {
				l.testQuery();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private SeqtreeManager sm;

	private void buildSeqPosTable() {
		DBCollection table = MongoConnection.instance().getCCDSPositionTable();
		table.drop();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File("ccds-output")));
			String s;
			while ((s = br.readLine()) != null) {
				String[] ss = s.split("\\t");
				String chr = "chr" + ss[0];
				String name = ss[1];
				String strand = ss[2];
				String from = ss[3];
				String to = ss[4];

				String key = String.format("%s_%s(%s)", chr, name, strand);
				DBObject i = new BasicDBObject();
				i.put(MONGO_ID, key);
				i.put(CCDS_POS_START, from);
				i.put(CCDS_POS_END, to);
				table.insert(i);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void buildSeqRefTable() throws Exception {
		BufferedReader br = null;
		DBCollection table = MongoConnection.instance().getCCDSRefTable();
		table.drop();
		try {
			br = new BufferedReader(new FileReader(new File(REF_FILE)));
			// Skip header
			br.readLine();
			String s;
			String prevCCDS = null;
			List<String> otherIds = null;

			while ((s = br.readLine()) != null) {
				String[] ss = s.split("\t");
				String ccds = ss[0];
				String id = ss[4];

				if (!ccds.equals(prevCCDS)) {
					if (prevCCDS != null) {
						String nm = null;
						String ens = null;
						String havana = null;
						for (String oid : otherIds) {
							if (oid.startsWith("NM")) {
								nm = oid;
							} else if (oid.startsWith("ENST")) {
								ens = oid;
							} else if (oid.startsWith("OTT")) {
								havana = oid;
							}
						}
						DBObject ins = new BasicDBObject();
						ins.put(MONGO_ID, prevCCDS);
						if (nm != null) {
							ins.put(CCDS_REF_REFSEQ, nm);
						}
						if (ens != null) {
							ins.put(CCDS_REF_ENSEMBL, ens);
						}
						if (havana != null) {
							ins.put(CCDS_REF_HAVANA, havana);
						}
						table.insert(ins);
					}
					prevCCDS = ccds;
					otherIds = new ArrayList<>();
				}

				otherIds.add(id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			br.close();
		}
	}

	private void buildSeqTree() {
		long preStart = System.currentTimeMillis();
		sm = SeqtreeManager.instance();
		sm.init(new MongoDataMap(), NIO_PATH, true);

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File("ccds-output")));
			String s;
			while ((s = br.readLine()) != null) {
				String[] ss = s.split("\\t");
				String chr = "chr" + ss[0];
				String name = ss[1];
				String strand = ss[2];
				String fromPre = ss[3];
				String toPre = ss[4];

				Long start = Long.parseLong(fromPre);
				Long end = Long.parseLong(toPre);
				Sequence seq = new Sequence(start, end, chr);
				String fullName = String.format("%s_%s(%s)", chr, name, strand);
				seq.setName(fullName);
				sm.addExtent(name, seq);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				long postLoadStart = System.currentTimeMillis();
				sm.postLoad();
				int postLoadTime = (int) (System.currentTimeMillis() - postLoadStart);
				System.out.println(String.format("Post load took %d millis",
						postLoadTime));
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		int preQuery = (int) (System.currentTimeMillis() - preStart);
		System.out.println(String.format("Time to warm up: %d mill", preQuery));
		testQuery();
		sm.close();
	}

	private void testQuery() {
		if (sm == null) {
			sm = SeqtreeManager.instance();
			sm.init(new MongoDataMap(), NIO_PATH, false);
		}
		long start = 800000;
		long end = 900000;
		System.out
				.println(String.format("Test sequence (%d - %d)", start, end));
		long startTime = System.currentTimeMillis();
		Sequence testSeq = new Sequence(start, end, "chr1");
		testSeq.setName("TestSequence");
		Iterator<Sequence> touches = sm.touches(testSeq);
		List<String> seqs = new ArrayList<String>();
		while (touches.hasNext()) {
			Sequence n = touches.next();
			seqs.add(n.getName());
		}
		// Collections.sort(seqs);
		for (String s : seqs) {
			System.out.println(s);
		}
		long endTime = System.currentTimeMillis();
		int qTime = (int) (endTime - startTime);
		System.out.println(String.format("Query done in %d mill", qTime));

	}

	private void init() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(new File(FILE)));
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
				"ccds-output")));
		String s;
		br.readLine();
		while ((s = br.readLine()) != null) {
			String[] ss = s.split("\t");
			String chr = ss[0];
			String ccds = ss[4];
			String status = ss[5];
			String strand = ss[6];
			String fromPre = ss[7];
			String toPre = ss[8];
			String locs = ss[9];
			String match = ss[10];

			if (status.equals("Withdrawn") || (match.equals("Partial"))) {
				continue;
			}
			String outFormat = "%s\t%s\t%s\t%d\t%d\n";
			bw.write(String.format(outFormat, chr, ccds, strand,
					Integer.parseInt(fromPre) + 1, Integer.parseInt(toPre) + 1));
			bw.flush();

			// Trim off []
			String subLoc = locs.substring(1, locs.length() - 1);
			String[] sls = subLoc.split(", ");
			// Reverse the order if '-' strand
			if (strand.equals("-")) {
				for (int i = sls.length - 1, j = 0; i >= 0; i--, j++) {
					String sq = sls[i];
					String[] sqs = sq.split("-");
					bw.write(String.format(outFormat, chr,
							String.format("%s_%d", ccds, j), strand,
							Integer.parseInt(sqs[0]) + 1,
							Integer.parseInt(sqs[1]) + 1));
					bw.flush();
				}
			} else {
				for (int i = 0; i < sls.length; i++) {
					String sq = sls[i];
					String[] sqs = sq.split("-");
					bw.write(String.format(outFormat, chr,
							String.format("%s_%d", ccds, i), strand,
							Integer.parseInt(sqs[0]) + 1,
							Integer.parseInt(sqs[1]) + 1));
					bw.flush();
				}
			}
		}
		br.close();
	}
}
