package edu.yale.med.krauthammerlab.abfab.old.vep;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public abstract class VEPExecutor {

	private Map<Mutation, VEPResult> results = new HashMap<Mutation, VEPResult>();
	private static VEPExecutor INSTANCE;

	static class Remote extends VEPExecutor {
		private String hostname = "ristretto.med.yale.edu";
		private String username = "matt";
		private String password = "53n4t0r_m3nd0z4";

		@Override
		protected String getVEPCommand() {
			return System.getProperty("user.home")
					+ "/mut/variant_effect_predictor/variant_effect_predictor.pl";
		}

		@Override
		protected String getVEPNonRefseqOutputFile() {
			return "STDOUT";
		}

		@Override
		protected Map<String, String> getVEPRefseqParameters() {
			Map<String, String> map = super.getVEPRefseqParameters();
			map.put("--host", "localhost");
			map.put("--port", "3306");
			map.put("--user", "vep");
			map.put("--password", "vep");
			return map;
		}

		@Override
		protected Map<String, String> getVEPNonRefseqParameters() {
			Map<String, String> map = super.getVEPNonRefseqParameters();
			map.put("--host", "localhost");
			map.put("--port", "3306");
			map.put("--user", "vep");
			map.put("--password", "vep");
			return map;
		}

		@Override
		protected Reader runVEPProcess(Map<String, String> p)
				throws IOException, InterruptedException {
			Connection conn = new Connection(hostname);
			conn.connect();
			boolean isAuth = conn.authenticateWithPassword(username, password);
			if (!isAuth) {
				throw new IOException("Authentication failed");
			}
			Session sess = conn.openSession();
			String command = invocation2commandString(p);
			sess.execCommand(command);
			InputStream err = new StreamGobbler(sess.getStderr());
			processError(err);
			InputStream stdout = new StreamGobbler(sess.getStdout());
			return new InputStreamReader(stdout);
		}

		private void processError(InputStream err) throws IOException {
			BufferedReader br = new BufferedReader(new InputStreamReader(err));
			String s;
			while ((s = br.readLine()) != null) {
				System.out.println(s);
			}
			br.close();
		}

		@Override
		protected Map<String, String> getBaseVEPInvocation(Mutation m) {
			Map<String, String> out = new LinkedHashMap<String, String>();
			long endPos = m.getEndPos();
			if (m.getRefSequence().length() > m.getMutSequence().length()) {
				int diff = m.getRefSequence().length() - m.getMutSequence().length();
				endPos += diff;
			}
			String echoString = String.format(
					"echo \"%s %d %d %s/%s %s\" > vep-input && ",
					m.getChromosome(), m.getStartPos(), endPos,
					m.getMutSequence(), m.getRefSequence(), m.getStrand());
			out.put(echoString, "");

			out.putAll(super.getBaseVEPInvocation(m));
			return out;
		}

		private String invocation2commandString(Map<String, String> p) {
			StringBuilder sb = new StringBuilder();
			Iterator<String> keyIter = p.keySet().iterator();
			while (keyIter.hasNext()) {
				String key = keyIter.next();
				String val = p.get(key);
				if (val.length() > 0) {
					sb.append(String.format("%s=%s", key, val));
				} else {
					sb.append(key);
				}
				if (keyIter.hasNext()) {
					sb.append(" ");
				}
			}
			return sb.toString();
		}

		@Override
		protected String getVEPRefseqOutputFile() {
			return "STDOUT";
		}

	}

	static class Local extends VEPExecutor {

		@Override
		protected String getVEPCommand() {
			return System.getProperty("user.home")
					+ "/mut/variant_effect_predictor/variant_effect_predictor.pl";
		}

		@Override
		protected String getVEPNonRefseqOutputFile() {
			return "vep-output2";
		}

		@Override
		protected String getVEPRefseqOutputFile() {
			return "vep-output";
		}

		@Override
		protected Reader runVEPProcess(Map<String, String> p)
				throws IOException, InterruptedException {
			File outputFile = new File(p.get("--output_file"));
			List<String> pbList = invocation2pblist(p);
			ProcessBuilder pb = new ProcessBuilder(pbList);
			Process proc = pb.start();
			proc.waitFor();
			return new FileReader(outputFile);
		}

		private List<String> invocation2pblist(Map<String, String> p) {
			List<String> out = new ArrayList<String>();
			for (String key : p.keySet()) {
				String val = p.get(key);
				if (val.length() > 0) {
					out.add(String.format("%s=%s", key, val));
				} else {
					out.add(key);
				}
			}
			return out;
		}

		@Override
		protected Reader runVEPNonRefseq(Mutation m) throws Exception {
			writeMutationToFile(m);
			return super.runVEPNonRefseq(m);
		}

	}

	public static VEPExecutor instance(boolean remote) {
		return instance(remote, false);
	}

	public static VEPExecutor instance(boolean remote, boolean forceNew) {
		if (forceNew) {
			INSTANCE = null;
		}
		if (INSTANCE == null) {
			INSTANCE = (remote) ? new VEPExecutor.Remote()
					: new VEPExecutor.Local();
		}
		return INSTANCE;
	}

	public VEPResult getResult(Mutation m) {
		if (!results.containsKey(m)) {
			VEPResult r;
			try {
				r = lookupMutation(m);
				results.put(m, r);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return results.get(m);
	}

	private VEPResult lookupMutation(Mutation m) throws Exception {
		VEPResult result;
		try {
			Reader nonRefseqReader = runVEPNonRefseq(m);
			result = parseNonRefseqOutput(nonRefseqReader, m);
		} finally {
			// outputFile.delete();
		}

		// Have to run again with refseq flag
		try {
			Reader refseqReader = runVEPRefseq(m);
			result = parseRefseqOutput(refseqReader, result, m);
		} finally {

		}

		return result;
	}

	protected Reader runVEPNonRefseq(Mutation m) throws Exception {
		return runVEP(m, false);
	}

	protected Reader runVEPRefseq(Mutation m) throws Exception {
		return runVEP(m, true);
	}

	private Reader runVEP(Mutation m, boolean refseq) throws Exception {
		Map<String, String> p = getVEPInvocation(m, refseq);
		return runVEPProcess(p);
	}

	protected abstract Reader runVEPProcess(Map<String, String> p)
			throws IOException, InterruptedException;

	private Map<String, String> getVEPInvocation(Mutation m, boolean refseq)
			throws IOException {
		Map<String, String> p = getBaseVEPInvocation(m);
		if (!refseq) {
			p.putAll(getVEPRefseqParameters());
		} else {
			p.putAll(getVEPNonRefseqParameters());
		}

		// writeInvocationFile(p, refseq);
		return p;
	}

	protected Map<String, String> getVEPNonRefseqParameters() {
		Map<String, String> p = new LinkedHashMap<String, String>();
		p.put("--output_file", getVEPNonRefseqOutputFile());
		p.put("--protein", "");
		p.put("--refseq", "");
		p.put("--hgvs", "");
		return p;
	}

	protected abstract String getVEPNonRefseqOutputFile();

	protected abstract String getVEPRefseqOutputFile();

	protected Map<String, String> getVEPRefseqParameters() {
		Map<String, String> p = new LinkedHashMap<String, String>();
		p.put("--output_file", getVEPRefseqOutputFile());
		p.put("--sift", "b");
		p.put("--terms", "ncbi");
		p.put("--polyphen", "b");
		p.put("--hgnc", "");
		p.put("--xref_refseq", "");
		p.put("--protein", "");
		// p.put("--check_ref", "");
		// p.put("--check_alleles", "");
		p.put("--check_existing", "");
		p.put("--numbers", "");
		p.put("--ccds", "");
		// p.put("--gmaf", "");
		p.put("--hgvs", "");
		p.put("--domains", "");
		p.put("--host", "ristretto.med.yale.edu");
		p.put("--port", "3306");
		p.put("--user", "vep");
		p.put("--password", "vep");
		return p;
	}

	protected Map<String, String> getBaseVEPInvocation(Mutation m) {
		Map<String, String> p = new LinkedHashMap<String, String>();
		p.put("perl", "");
		p.put(getVEPCommand(), "");
		p.put("--quiet", "");
		p.put("--input_file", "vep-input");
		p.put("--force_overwrite", "");
		p.put("--host", "ristretto.med.yale.edu");
		p.put("--port", "3306");
		p.put("--user", "vep");
		p.put("--password", "vep");
		return p;
	}

	protected abstract String getVEPCommand();

	private void writeInvocationFile(List<String> p, boolean refseq)
			throws IOException {
		File f = refseq ? new File("vep-invocation") : new File(
				"vep-invocation-refseq");
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(f));
			for (String i : p) {
				bw.write(i + " ");
			}
		} finally {
			bw.flush();
			bw.close();
		}
	}

	private VEPResult parseNonRefseqOutput(Reader reader, Mutation m)
			throws IOException {
		BufferedReader br = null;
		VEPResult res = new VEPResult();
		try {
			br = new BufferedReader(reader);
			String s;

			while ((s = br.readLine()) != null) {
				System.out.println(s);
				if (s.startsWith("#")) {
					continue;
				}
				System.out.println(m.getRefseq());
				if (s.contains(m.getRefseq())) {
					String[] ss = s.split("\\t", -1);
					res.setExisting(ss[12]);
					String extras = ss[13];
					res.setPolyphen(parseDouble(parseExtras(extras, "PolyPhen",
							".*\\(([\\d.]*)\\)")));
					res.setSift(parseDouble(parseExtras(extras, "SIFT",
							".*\\(([\\d.]*)\\)")));
					res.setHgnc(parseExtras(extras, "HGNC"));
					res.setCCDS(parseExtras(extras, "CCDS"));
					res.setDomain(parseExtras(extras, "DOMAINS",
							".*Pfam_domain:(PF\\d\\d\\d\\d\\d).*"));
					String preExon = parseExtras(extras, "EXON", "(\\d+)/.*");
					if (preExon != null) {
						res.setExonNumber(Integer.parseInt(preExon));
					}
					String preIntron = parseExtras(extras, "INTRON",
							"(\\d+)/.*");
					if (preIntron != null) {
						res.setIntronNumber(Integer.parseInt(preIntron));
					}
					String aachange = ss[10];
					if (!(aachange.equals("-"))) {
						res.setAAChange(aachange);
					}
				}
			}
		} finally {
			br.close();
		}
		return res;
	}

	private VEPResult parseRefseqOutput(Reader reader, VEPResult result,
			Mutation m) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(reader);
			String s;
			while ((s = br.readLine()) != null) {
				System.out.println(s);
				if (s.startsWith("#")) {
					continue;
				}
				if (s.contains(m.getRefseq())) {
					String[] ss = s.split("\\t", -1);
					String feature = ss[4];
					String extras = ss[13];
					if (feature.startsWith("NM_")) {
						String cdnaPosPre = ss[7];
						if (!cdnaPosPre.equals("-")) {
							result.setCDNAPosition(Integer.parseInt(cdnaPosPre));
						}
					}
					String cdsPos = ss[8];
					if (cdsPos != null && !cdsPos.equals("-")) {
						result.setCDSPosition(Integer.parseInt(cdsPos));
					}
					String proteinPos = ss[9];
					if (proteinPos != null && !proteinPos.equals("-")) {
						result.setProteinPosition(Integer.parseInt(proteinPos));
					}
					result.setRefseq(feature);
					result.setProtein(parseExtras(extras, "ENSP"));
					result.setHGVS(parseExtras(extras, "HGVSc", ".*:(.*)"));
				}
			}
		} finally {
			br.close();
		}
		return result;
	}

	protected File writeMutationToFile(Mutation m) throws IOException {
		File f = new File("vep-input");
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(f));
			bw.write(asVEPFormat(m));
			bw.flush();
		} finally {
			bw.close();
		}
		return f;
	}
	
	private String asVEPFormat(Mutation m) {
		long endPos = m.getEndPos();
		if (m.getRefSequence().length() > m.getMutSequence().length()) {
			int diff = m.getRefSequence().length() - m.getMutSequence().length();
			endPos += diff;
		}
		return String.format("%s %d %d %s/%s %s", m.getChromosome(), m.getStartPos(),
	            endPos, m.getRefSequence(), m.getMutSequence(), m.getStrand());
	}

	private String parseExtras(String extras, String key) {
		return parseExtras(extras, key, null);
	}

	private String parseExtras(String extras, String key, String pattern) {
		Pattern p = null;
		if (pattern != null && pattern.length() > 0) {
			p = Pattern.compile(pattern);
		}
		String[] words = extras.split(";");
		for (String word : words) {
			String[] sw = word.split("=");
			if (sw[0].equals(key)) {
				String val = sw[1];
				if (p != null) {
					// System.out.println("p: " + p + "; val: " + val);
					Matcher matcher = p.matcher(val);
					if (matcher.matches()) {
						val = matcher.group(1);
					} else {
						val = null;
					}
				}
				return val;
			}
		}
		return null;
	}

	private Integer parseInt(String s) {
		Integer output = null;
		if (s != null) {
			try {
				output = Integer.parseInt(s);
			} catch (NumberFormatException nfe) {
				;
			}
		}
		return output;
	}

	private Double parseDouble(String s) {
		Double output = null;
		if (s != null) {
			try {
				output = Double.parseDouble(s);
			} catch (NumberFormatException nfe) {
				;
			}
		}
		return output;
	}
}
