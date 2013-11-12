package edu.yale.seqtree;

import java.util.HashMap;

public class Chromosomes {
	private static String[] chromosomes;
	private static HashMap<String, Integer> chrMap;
	public static final int NUM_CHROM = 25;

	public static void init() {

		createChromosomeMaps();
	}

	private static void createChromosomeMaps() {
		chromosomes = new String[25];
		chrMap = new HashMap<String, Integer>();
		for (int i = 0; i < 22; i++) {
			chromosomes[i] = "chr" + (i + 1);
			chrMap.put("chr" + (i + 1), i);
		}
		chromosomes[22] = "chrX";
		chrMap.put("chrX", 22);
		chromosomes[23] = "chrY";
		chrMap.put("chrY", 23);
		chromosomes[24] = "chrM";
		chrMap.put("chrM", 24);
	}

	public static String getChromosome(int index) {
		return chromosomes[index];
	}

	public static int getChromIndex(String chromName) {
		if (chrMap.containsKey(chromName)) {
			return chrMap.get(chromName);
		}
		return -1;
	}

	public static int count() {
		return NUM_CHROM;
	}
}
