package edu.yale.med.krauthammerlab.abfab.old.vep;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class OneKExecutor {

	private static OneKExecutor INSTANCE;
	private Map<Mutation, RateGroup> rates = new HashMap<Mutation, RateGroup>();
	private Connection conn;

	class RateGroup {
		double rate, afrRate, amrRate, asnRate, eurRate;

		RateGroup(double rate, double afrRate, double amrRate, double asnRate,
				double eurRate) {
			this.rate = rate;
			this.afrRate = afrRate;
			this.amrRate = amrRate;
			this.asnRate = asnRate;
			this.eurRate = eurRate;
		}
	}

	public static OneKExecutor instance() {
		return instance(false);
	}

	public static OneKExecutor instance(boolean forceNew) {
		if (forceNew) {
			INSTANCE = null;
		}
		if (INSTANCE == null) {
			INSTANCE = new OneKExecutor();
		}
		return INSTANCE;
	}

	private Connection getConnection() {
		System.out.println("Try to connect 1k");
		if (conn == null) {
			try {
				Class.forName("com.mysql.jdbc.Driver");
				conn = DriverManager.getConnection(
						"jdbc:mysql://ristretto.med.yale.edu:3306/blood",
						"vep", "vep");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return conn;
	}

	public double getRate(Mutation m) {
		return getRates(m).rate;
	}

	public double getAfrRate(Mutation m) {
		return getRates(m).afrRate;
	}

	public double getAmrRate(Mutation m) {
		return getRates(m).amrRate;
	}

	public double getAsnRate(Mutation m) {
		return getRates(m).asnRate;
	}

	public double getEurRate(Mutation m) {
		return getRates(m).eurRate;
	}

	private RateGroup getRates(Mutation m) {
		if (!rates.containsKey(m)) {
			RateGroup r;
			try {
				r = lookupMutation(m);
				rates.put(m, r);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return rates.get(m);
	}

	RateGroup lookupMutation(Mutation m) {
		Connection con = getConnection();
		RateGroup output = null;
		double rate = 0.0d;
		double afrRate = 0.0d;
		double amrRate = 0.0d;
		double asnRate = 0.0d;
		double eurRate = 0.0d;
		try {
			PreparedStatement ps = con
					.prepareStatement("select rate, afr_rate, amr_rate, "
							+ "asn_rate, eur_rate" + " from 1kdata "
							+ "where chr = ? and pos = ? and ref = ? "
							+ "and actual = ?");
			ps.setString(1, m.getChromosome());
			ps.setLong(2, m.getStartPos());
			ps.setString(3, m.getRefSequence());
			ps.setString(4, m.getMutSequence());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				rate = rs.getDouble(1);
				afrRate = rs.getDouble(2);
				amrRate = rs.getDouble(3);
				asnRate = rs.getDouble(4);
				eurRate = rs.getDouble(5);
			}

			output = new RateGroup(rate, afrRate, amrRate, asnRate, eurRate);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return output;
	}
}
