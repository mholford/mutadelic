import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GetAll1Ks {
	public static final String FILE_SUFFIX = ".phase1_release_v3.20101123.snps_indels_svs.genotypes.vcf.gz";
	public static final String OUTPUT_FILE = "1k-output";
	public static final String ADDRESS = "ftp://ftp.1000genomes.ebi.ac.uk/vol1/ftp/release/20110521";

	public static void main(String[] args) {
		new GetAll1Ks().exec();
	}

	private HashSet<String> allChrs;
	private File outputFile;
	private BufferedWriter ofWriter;

	private void init() throws IOException {
		allChrs = new HashSet<String>();
		allChrs.addAll(getRangeAsStringList(1, 22));
		allChrs.add("X");
		allChrs.add("wgs");

		outputFile = new File(OUTPUT_FILE);
		ofWriter = new BufferedWriter(new FileWriter(outputFile));
	}

	private void exec() {
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (String chrName : allChrs) {
			File f = null;
			try {
				String preFile = chrName.equals("wgs") ? "1k-wgs" : "1k-chr"
						+ chrName;
				File zf = dlFile(preFile);
				f = unzip(zf);
				// f = new File("1k-chr22-input");
				parseFile(chrName, f);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				cleanUp(f);
			}
		}
	}

	private File dlFile(String fname) throws IOException, InterruptedException {
		System.out.println("Download input for " + fname);
		File outFile = new File(fname + "-input.gz");
		List<String> inv = new ArrayList<String>();
		inv.add("wget");
		inv.add("-q");
		inv.add("-O");
		inv.add(fname + "-input.gz");
		inv.add(String.format("%s/%s%s", ADDRESS, "ALL." + fname.substring(3),
				FILE_SUFFIX));
		ProcessBuilder pb = new ProcessBuilder(inv);
		Process p = pb.start();
		int x = p.waitFor();
		return outFile;
	}

	private File unzip(File f) throws IOException, InterruptedException {
		System.out.println("Unzipping file: " + f.getName());
		List<String> inv = new ArrayList<String>();
		inv.add("gunzip");
		inv.add(f.getName());
		ProcessBuilder pb = new ProcessBuilder(inv);
		Process p = pb.start();
		int x = p.waitFor();
		f.delete();
		return new File(f.getName().substring(0, f.getName().length() - 3));
	}

	private List<String> getRangeAsStringList(int start, int end) {
		List<String> out = new ArrayList<String>();
		for (int i = start; i <= end; i++) {
			out.add(String.valueOf(i));
		}
		return out;
	}

	private void parseFile(String chr, File f) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(f));
		String s;
		int num = 0;
		int cnt = 0;
		while ((s = br.readLine()) != null) {
//			if (++cnt % 100000 == 0) {
//				System.out.println(String.format("%d lines read", cnt));
//			}
			if (!s.startsWith("#")) {
				String[] split = s.split("\\t");
				if (split.length > 10) {
					String chrom = split[0];
					int pos = Integer.parseInt(split[1]);
					String ref = split[3];
					String att = split[4];
					String info = split[7];
					String[] infosplit = info.split(";");
					double ldaf = -1d;
					double asnaf = -1d;
					double amraf = -1d;
					double afraf = -1d;
					double euraf = -1d;
					for (String i : infosplit) {
						if (i.startsWith("LDAF=")) {
							ldaf = Double.parseDouble(i.substring(5));
						}
						if (i.startsWith("ASN_AF=")) {
							asnaf = Double.parseDouble(i.substring(7));
						}
						if (i.startsWith("AMR_AF=")) {
							amraf = Double.parseDouble(i.substring(7));
						}
						if (i.startsWith("AFR_AF=")) {
							afraf = Double.parseDouble(i.substring(7));
						}
						if (i.startsWith("EUR_AF=")) {
							euraf = Double.parseDouble(i.substring(7));
						}
					}
					if (!(asnaf > -1d)) {
						asnaf = 0d;
					}
					if (!(amraf > -1d)) {
						amraf = 0d;
					}
					if (!(afraf > -1d)) {
						afraf = 0d;
					}
					if (!(euraf > -1d)) {
						euraf = 0d;
					}

					if (ldaf > -1d)
						ofWriter.write(String
								.format("%s\t%d\t%s\t%s\t%f\t%f\t%f\t%f\t%f\n",
										chrom, pos, ref, att, ldaf, afraf,
										amraf, asnaf, euraf));
					ofWriter.flush();
					++num;
				}
			}
		}
		br.close();
	}

	private void cleanUp(File f) {
		f.delete();
	}
}
