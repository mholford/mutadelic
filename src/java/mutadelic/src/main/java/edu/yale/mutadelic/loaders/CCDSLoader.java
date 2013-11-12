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

import edu.yale.mutadelic.mongo.MongoDataMap;
import edu.yale.seqtree.SeqtreeManager;
import edu.yale.seqtree.Sequence;

public class CCDSLoader {

	private static final String FILE = "/home/matt/Downloads/CCDS.current.txt";

	public static void main(String[] args) {
		CCDSLoader l = new CCDSLoader();
		try {
			if (args[0].equals("parse-ccds")) {
				l.init();
			} else if (args[0].equals("build-seqtree")) {
				l.buildSeqTree();
			} else if (args[0].equals("test-query")) {
				l.testQuery();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private SeqtreeManager sm;

	private void buildSeqTree() {
		long preStart = System.currentTimeMillis();
		sm = SeqtreeManager.instance();
		sm.init(new MongoDataMap(), true);

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
			sm.init(new MongoDataMap(), false);
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
