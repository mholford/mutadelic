import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.yale.seqtree.SeqtreeManager;
import edu.yale.seqtree.Sequence;
import edu.yale.seqtree.persist.MemoryDataMap;

public class Test {

	private static boolean build;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0 && args[0].equals("build")) {
			build = true;
		}
		new Test().init(build);
	}

	private int scnt;
	private SeqtreeManager sm;

	private void init(boolean build) {
		long preStart = System.currentTimeMillis();
		sm = SeqtreeManager.instance();
		sm.init(new MemoryDataMap(), "/home/matt/nio", build);

		if (build) {
			String numMatch = "^(\\d+).*";
			Pattern nm = Pattern.compile(numMatch);
			Matcher matcher = nm.matcher("123M");

			String file = "/home/matt/yucot.bed.shuf";
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(new File(file)));
				String s;
				scnt = 0;
				while ((s = br.readLine()) != null) {
					if ((scnt != 0) && (scnt % 1000 == 0)) {
						System.out.println(scnt + " read");
					}
					String[] ss = s.split("\\t");
					String name = ss[0];
					String chr = ss[1];

					String pstart = ss[2];
					long start = Long.parseLong(pstart);
					String pend = ss[3];
					long end = Long.parseLong(pend);

					Sequence seq = new Sequence(start, end, chr);
					seq.setName(name);
					scnt++;
					sm.addExtent(name, seq);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					long postLoadStart = System.currentTimeMillis();
					sm.postLoad();
					int postLoadTime = (int) (System.currentTimeMillis() - postLoadStart);
					System.out.println(String.format(
							"Post load took %d millis", postLoadTime));
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		int preQuery = (int) (System.currentTimeMillis() - preStart);
		System.out.println(String.format("Time to warm up: %d mill", preQuery));
		testQuery();
		sm.close();
	}

	private void testQuery() {

		long start = 101538720;
		long end = 101548720;
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
//		Collections.sort(seqs);
		for (String s : seqs) {
			System.out.println(s);
		}
		long endTime = System.currentTimeMillis();
		int qTime = (int) (endTime - startTime);
		System.out.println(String.format("Query done in %d mill", qTime));

	}
}
