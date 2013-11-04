package edu.yale.abfab;

public class Logging {
	public static final int DBG_TIMING = 1 << 0;
	public static final int DBG_PATH_CREATION = 1 << 1;
	public static final int DBG_SERVICE_MATCH = 1 << 2;

	public static final int dbgLevel = DBG_PATH_CREATION | DBG_TIMING;
	
	public static void dbg(int level, String s, Object... args) {
		if ((dbgLevel & level) == level) {
			System.out.println(String.format(s, args));
		}
	}
}
