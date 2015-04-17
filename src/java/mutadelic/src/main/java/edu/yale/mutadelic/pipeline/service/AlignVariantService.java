package edu.yale.mutadelic.pipeline.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import edu.yale.abfab.Abductor;
import edu.yale.abfab.IndividualPlus;
import edu.yale.abfab.service.AbfabServiceException;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClass;
import edu.yale.dlgen.controller.DLController;
import edu.yale.mutadelic.mongo.MongoConnection;
import edu.yale.mutadelic.mongo.MongoDataMap;
import edu.yale.mutadelic.ncbivr.NCBIVariationReporter;
import edu.yale.mutadelic.pipeline.model.Variant;
import edu.yale.seqtree.SeqtreeManager;
import edu.yale.seqtree.Sequence;
import static edu.yale.abfab.NS.*;
import static edu.yale.mutadelic.mongo.MongoConnection.*;
import static edu.yale.mutadelic.ncbivr.NCBIVariationReporter.*;
import static edu.yale.mutadelic.pipeline.service.SiftService.*;
import static org.junit.Assert.*;

public class AlignVariantService extends AbstractPipelineService {

	private final static String CCDS_NIO_PATH = System.getProperty("user.home")
			+ "/nio/ccds";

	private final static Map<String, String> flipMap;
	private final static Map<String, String> nm2Gene;

	static {
		flipMap = new HashMap<>();
		flipMap.put("A", "T");
		flipMap.put("C", "G");
		flipMap.put("G", "C");
		flipMap.put("T", "A");
		nm2Gene = new HashMap<>();
		initNM2Gene();
	}

	class ExonMatch {
		String name;
		int start;
		int end;

		ExonMatch(String name, int start, int end) {
			this.name = name;
			this.start = start;
			this.end = end;
		}

		@Override
		public String toString() {
			return String.format("%s: %d-%d", name, start, end);
		}
	}

	private static void initNM2Gene() {
		// File downloaded from NCBI's ftp site (gene2refseq) and then processed
		// via bash (grep "^9606" | cut 4,16 | grep "^NM_" | uniq >
		// nm2GeneSymbol)
		BufferedReader br = new BufferedReader(new InputStreamReader(
				AlignVariantService.class.getClassLoader().getResourceAsStream(
						"nm2GeneSymbol")));
		String s;
		try {
			while ((s = br.readLine()) != null) {
				String[] ss = s.split("\t");
				String key = ss[0].substring(0, ss[0].indexOf("."));
				nm2Gene.put(key, ss[1]);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String flip(String orig) {
		StringBuilder sb = new StringBuilder();
		for (char c : orig.toCharArray()) {
			String k = String.valueOf(c);
			String v = flipMap.get(k);
			sb.append(v);
		}
		return sb.reverse().toString();
	}
	
//	@Test
//	public void testFlip() {
//		assertEquals("C", flip("G"));
//		assertEquals("CGT", flip("ACG"));
//	}

	@Override
	public IndividualPlus exec(IndividualPlus input, Abductor abductor)
			throws AbfabServiceException {
		DLController dl = abductor.getDLController();
		DLClass<?> hgvs = dl.clazz(HGVS_NOTATION);
		if (valueFilled(dl, input.getIndividual(), hgvs)) {
			return input;
		}
		Variant v = Variant.fromOWL(dl, input);
		String result = "";
		try {
			// result = getAlignmentForVariantFromNCBIVR(v);
			// result = getAlignmentForVariantFromSift(v);
			result = getAlignmentFromCCDS(v);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Set<DLAxiom<?>> annotation = annotatedResult(dl, input.getIndividual(),
				hgvs, dl.individual(MUTADELIC), result, true);
		input.getAxioms().addAll(annotation);

		// For 3/2/2015: add the Gene symbol
		Set<DLAxiom<?>> geneAnnotation = annotatedResult(dl,
				input.getIndividual(), dl.clazz(GENE),
				dl.individual(MUTADELIC), getGeneName(result), true);
		input.getAxioms().addAll(geneAnnotation);

		return input;
	}

	public String getGeneName(String alignment) {
		if (alignment != null && alignment.startsWith("NM")) {
			int periodIdx = alignment.indexOf(".");
			String key = alignment;
			if (periodIdx > 0) {
				key = alignment.substring(0, periodIdx);
			}
			if (nm2Gene.containsKey(key)) {
				return nm2Gene.get(key);
			}
		}
		return "NA";
	}

	public String getAlignmentFromCCDS(Variant v) {
		String alignment = null;
		DBCollection table = MongoConnection.instance().getCCDSPositionTable();

		SeqtreeManager sm = SeqtreeManager.instance();
		sm.init(new MongoDataMap(), CCDS_NIO_PATH, false);
		Sequence testSeq = new Sequence(v.getStartPos(), v.getEndPos(), v
				.getChromosome().toLowerCase());
		testSeq.setName("TestSequence");
		Iterator<Sequence> touches = sm.touches(testSeq);
		List<String> seqs = new ArrayList<String>();
		while (touches.hasNext()) {
			Sequence n = touches.next();
			seqs.add(n.getName());
		}

		Pattern exonMatchPattern = Pattern
				.compile("(.*_CCDS.*)_(\\d+)\\((.)\\)");
		Pattern transcriptMatchPattern = Pattern
				.compile("(.*_CCDS.*)\\((.)\\)");

		Set<String> exonMatches = null;
		Set<String> transcriptMatches = null;

		for (String seq : seqs) {
			Matcher exonMatcher = exonMatchPattern.matcher(seq);
			if (exonMatcher.matches()) {
				if (exonMatches == null) {
					exonMatches = new HashSet<>();
				}
				// Hack to force red cell's NM
				if (!seq.startsWith("chr8_CCDS47849.1")
						&& !seq.startsWith("chr8_6121.1")) {
					exonMatches.add(seq);
				}
			} else {
				if (transcriptMatches == null)
					transcriptMatches = new HashSet<>();
				if (!seq.startsWith("chr8_CCDS47849.1")
						&& !seq.startsWith("chr8_6121.1")) {
					transcriptMatches.add(seq);
				}
			}
		}

		if (exonMatches != null) {
			List<String> sortedExonMatches = new ArrayList<>(exonMatches);
			Collections.sort(sortedExonMatches);

			// Just the first for now?
			String exonMatch = sortedExonMatches.iterator().next();
			Matcher m = exonMatchPattern.matcher(exonMatch);
			m.matches();
			String exonNoPre = m.group(2);
			String strand = m.group(3);
			String transcript = m.group(1);

			int exonNo = Integer.parseInt(exonNoPre);
			int transcriptPos = 0;

			// These will be 'flipped' if on '-' strand
			String vref = v.getReference();
			String vobs = v.getObserved();

			for (int i = 0; i <= exonNo; i++) {
				String iexon = String.format("%s_%d", transcript, i);

				// Lookup exon coordinates in mongo
				String key = String.format("%s(%s)", iexon, strand);
				DBObject q = new BasicDBObject();
				q.put(MONGO_ID, key);
				DBObject r = table.findOne(q);
				int start = Integer.parseInt((String) r.get(CCDS_POS_START));
				int end = Integer.parseInt((String) r.get(CCDS_POS_END));

				if (i < exonNo) {
					transcriptPos += 1 + (end - start);
				} else {
					if (strand.equals("+")) {
						transcriptPos += 1 + (v.getStartPos() - start);
					} else {
						transcriptPos += 1 + (end - v.getStartPos());
						// Flip the nucleotides
						vref = flip(vref);
						vobs = flip(vobs);
					}
				}
			}

			// Look NM in mongo
			DBCollection refTable = MongoConnection.instance()
					.getCCDSRefTable();
			DBObject q = new BasicDBObject();
			// First strip off leading 'chr_'
			q.put(MONGO_ID, transcript.substring(transcript.indexOf('_') + 1));
			DBObject r = refTable.findOne(q);

			String refseqTranscript = (String) r.get(CCDS_REF_REFSEQ);

			alignment = String.format("%s:c.%d%s>%s", refseqTranscript,
					transcriptPos, vref, vobs);
		}

		if (alignment == null) {
			if (transcriptMatches != null) {
				List<String> sortedTranscriptMatches = new ArrayList<>(
						transcriptMatches);
				Collections.sort(sortedTranscriptMatches);

				String trMatch = sortedTranscriptMatches.get(0);
				Matcher m = transcriptMatchPattern.matcher(trMatch);
				m.matches();
				String transcript = m.group(1);
				String strand = m.group(2);

				// Will need to 'flip' the nucleotides if on '-' strand
				String vref = v.getReference();
				String vobs = v.getObserved();

				// Get all exons within this transcript by querying Mongo
				Pattern key = Pattern.compile(String
						.format("%s_.*", transcript));
				DBObject q = new BasicDBObject();
				q.put(MONGO_ID, key);
				Map<String, ExonMatch> exonsInRange = new HashMap<>();
				DBCursor dbc = table.find(q);
				while (dbc.hasNext()) {
					DBObject next = dbc.next();
					String name = (String) next.get(MONGO_ID);
					String startPre = (String) next.get(CCDS_POS_START);
					int start = Integer.parseInt(startPre);
					String endPre = (String) next.get(CCDS_POS_END);
					int end = Integer.parseInt(endPre);
					exonsInRange.put(name, new ExonMatch(name, start, end));
				}

				// Find the exon closest to the variant
				int erSize = exonsInRange.size();
				ExonMatch match1 = null;
				ExonMatch match2 = null;
				ExonMatch closest = null;
				for (int i = 0; i < erSize; i++) {
					String erKey = String.format("%s_%d(%s)", transcript, i,
							strand);
					ExonMatch exon = exonsInRange.get(erKey);
					if (strand.equals("+")) {
						if (v.getEndPos() < exon.start) {
							match2 = exon;
							break;
						}
						if (v.getStartPos() > exon.end) {
							match1 = exon;
						}
					} else {
						// Flip nucleotides b./c on '-' strand
						vref = flip(vref);
						vobs = flip(vobs);
						if (v.getStartPos() > exon.end) {
							match2 = exon;
							break;
						}
						if (v.getEndPos() < exon.start) {
							match1 = exon;
						}
					}
				}

				assert match1 != null && match2 != null;
				if (strand.equals("+")) {
					int m1dist = Math.abs(v.getStartPos() - match1.end);
					int m2dist = Math.abs(v.getEndPos() - match2.start);
					closest = (m1dist < m2dist) ? match1 : match2;
				} else {
					int m1dist = Math.abs(v.getEndPos() - match1.start);
					int m2dist = Math.abs(v.getStartPos() - match2.end);
					closest = (m1dist < m2dist) ? match1 : match2;
				}

				// Get the CDS coordinate
				int exonNo = Integer.parseInt(closest.name.substring(
						closest.name.lastIndexOf("_") + 1,
						closest.name.indexOf("(")));
				int transcriptPos = 0;
				String offset = null;
				for (int i = 0; i <= exonNo; i++) {
					String iexon = String.format("%s_%d", transcript, i);

					// Lookup exon coordinates in mongo
					String k = String.format("%s(%s)", iexon, strand);
					ExonMatch em = exonsInRange.get(k);

					if (i < exonNo) {
						transcriptPos += 1 + (em.end - em.start);
					} else {
						if (strand.equals("+")) {
							if (v.getStartPos() > em.end) {
								transcriptPos += 1 + (em.end - em.start);
								int offsetPre = v.getStartPos() - em.end;
								offset = "+" + offsetPre;
							} else {
								transcriptPos += 1;
								int offsetPre = em.start - v.getEndPos();
								offset = "-" + offsetPre;
							}
						} else {
							
							if (v.getStartPos() > em.end) {
								transcriptPos += 1;
								int offsetPre = v.getStartPos() - em.end;
								offset = "-" + offsetPre;
							} else {
								transcriptPos += 1 + (em.end - em.start);
								int offsetPre = em.start - v.getEndPos();
								offset = "+" + offsetPre;
							}
						}
					}
				}
				
				
				// Lookup the NM in mongo
				DBCollection refTable = MongoConnection.instance()
						.getCCDSRefTable();
				q = new BasicDBObject();
				// First strip off leading 'chr_'
				q.put(MONGO_ID,
						transcript.substring(transcript.indexOf('_') + 1));
				DBObject r = refTable.findOne(q);

				String refseqTranscript = (String) r.get(CCDS_REF_REFSEQ);

				alignment = String.format("%s:c.%d%s%s>%s", refseqTranscript,
						transcriptPos, offset, vref, vobs);

			} else {
				alignment = "INTERGENIC";
			}
		}

		return alignment;
	}

	private String getAlignmentForVariantFromSift(Variant v) {
		String alignment = "";
		DBCollection table = MongoConnection.instance().getSiftTable();
		String key = siftKey(v);

		DBObject q = new BasicDBObject();
		q.put("_id", key);
		DBObject r = table.findOne(q);

		if (r != null) {
			String vals = (String) r.get(SIFT_VALUES);
			String[] ss = vals.split(";", -1);
			int idx = siftIndex(v);
			String siftData = ss[idx];

			String[] sds = siftData.split(",", -1);
			String transcript = sds[4];
			String tPos = sds[5];
			alignment = String.format("%s:c.%s%s>%s", transcript, tPos,
					v.getReference(), v.getObserved());
		}

		return alignment;
	}

	private String getAlignmentForVariantFromNCBIVR(Variant v) throws Exception {
		String alignment = "";
		NCBIVariationReporter ncbivr = new NCBIVariationReporter();
		String q = var2NCBIVR(v);
		String r = ncbivr.analyze(HUMAN, DEFAULT_ASSEMBLY, q);
		BufferedReader br = new BufferedReader(new StringReader(r));
		String s;
		br.readLine();
		while ((s = br.readLine()) != null) {
			if (s.length() > 0 && !(s.startsWith("#"))) {
				String[] ss = s.split("\t");
				if (ss.length > 12) {
					alignment = ss[12];
					System.out.println("Alignment: " + alignment);
				}
			}

		}
		return alignment;
	}

	private String var2NCBIVR(Variant v) {
		String output = String.format("%s:g.%d%s>%s",
				CHR_NUC.get(v.getChromosome()), v.getStartPos(),
				v.getReference(), v.getObserved());
		return output;
	}

	@Override
	public double cost() {
		return 1.0;
	}
}
