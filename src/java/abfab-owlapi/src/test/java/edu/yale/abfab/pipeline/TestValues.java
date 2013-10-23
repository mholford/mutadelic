package edu.yale.abfab.pipeline;

public class TestValues {
	public static String AA_CHANGE = "NonSynonymous";
	public static String ALIGNMENT = "123A>C";
	public static double ALLELE_FREQUENCY = 0.4d;
	public static boolean CRITICAL_DOMAIN_MISSING = false;
	public static boolean CRITICAL_DOMAIN = false;
	public static boolean FINISHED = true;
	public static String INDEL_OR_POINT = "Point";
	public static String MARK_RARE_UNUSUAL = "RareAndUnusual";
	public static String MARK_UNUSUAL = "Unusual";
	public static double PHYLOP = 0.5d;
	public static boolean RCMDB_KNOWN = false;
	public static double SIFT = 0.4d;
	public static String TRANSCRIPT_LOCALE = "CDS";
	
	public static void revert() {
		AA_CHANGE = "NonSynonymous";
		ALIGNMENT = "123A>C";
		ALLELE_FREQUENCY = 0.4d;
		CRITICAL_DOMAIN_MISSING = false;
		CRITICAL_DOMAIN = false;
		FINISHED = true;
		INDEL_OR_POINT = "Point";
		MARK_RARE_UNUSUAL = "RareAndUnusual";
		MARK_UNUSUAL = "Unusual";
		PHYLOP = 0.5d;
		RCMDB_KNOWN = false;
		SIFT = 0.4d;
		TRANSCRIPT_LOCALE = "CDS";
	}
}
