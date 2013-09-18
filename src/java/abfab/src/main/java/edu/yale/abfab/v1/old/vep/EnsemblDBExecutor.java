package edu.yale.med.krauthammerlab.abfab.old.vep;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnsemblDBExecutor {

	private static EnsemblDBExecutor INSTANCE;
	Map<String, List<String>> nms;
	private Connection conn;

	public static EnsemblDBExecutor instance() {
		return instance(false);
	}
	
	private EnsemblDBExecutor() {
		nms = new HashMap<String, List<String>>();
	}

	public static EnsemblDBExecutor instance(boolean forceNew) {
		if (forceNew) {
			INSTANCE = null;
		}
		if (INSTANCE == null) {
			INSTANCE = new EnsemblDBExecutor();
		}
		return INSTANCE;
	}

	private Connection getConnection() {
		if (conn == null) {
			try {
				Class.forName("com.mysql.jdbc.Driver");
				conn = DriverManager
						.getConnection(
								"jdbc:mysql://ristretto.med.yale.edu:3306/homo_sapiens_otherfeatures_67_37",
								"vep", "vep");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return conn;
	}

	public List<String> getNMs(String chr, String pos) {
		String key = String.format("%s-%s", chr, pos);
		if (!nms.containsKey(key)) {
			try {
				List<String> refs = lookupNMs(key);
				nms.put(key, refs);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return nms.get(key);
	}

	List<String> lookupNMs(String key) {
		String[] sp = key.split("-");
		String chr = sp[0];
		long pos = Long.parseLong(sp[1]);
		Connection con = getConnection();
		List<String> output = new ArrayList<String>();
		try {
			PreparedStatement ps = con.prepareStatement("select t.stable_id "
					+ "from transcript t, seq_region sr "
					+ "where sr.coord_system_id = 2" + "  and sr.name = ?"
					+ "  and t.seq_region_id = sr.seq_region_id"
					+ "  and t.seq_region_start <= ?"
					+ "  and t.seq_region_end >= ?"
					+ "  and t.stable_id like 'NM_%'");
			ps.setString(1, chr);
			ps.setLong(2, pos);
			ps.setLong(3, pos);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				output.add(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return output;
	}
}
