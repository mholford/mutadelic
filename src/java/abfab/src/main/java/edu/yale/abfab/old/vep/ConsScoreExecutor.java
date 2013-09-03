package edu.yale.med.krauthammerlab.abfab.old.vep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class ConsScoreExecutor {

   private static ConsScoreExecutor INSTANCE;
   private Map<Mutation, Double[]> rates = new HashMap<Mutation, Double[]>();
   public static final int PHYLOP = 0;
   public static final int PHAST_CONS = 1;
   private String hostname = "ristretto.med.yale.edu";
   private String username = "matt";
   private String password = "53n4t0r_m3nd0z4";

   public static ConsScoreExecutor instance() {
      return instance(false);
   }

   public static ConsScoreExecutor instance(boolean forceNew) {
      if (forceNew) {
         INSTANCE = null;
      }
      if (INSTANCE == null) {
         INSTANCE = new ConsScoreExecutor();
      }
      return INSTANCE;
   }

   public double getPhylop(Mutation m) {
      checkInTable(m);
      return rates.get(m)[PHYLOP];
   }

   public double getPhastCons(Mutation m) {
      checkInTable(m);
      return rates.get(m)[PHAST_CONS];
   }

   private void checkInTable(Mutation m) {
      if (!rates.containsKey(m)) {
         Double[] r;
         try {
            r = lookupMutation(m);
            rates.put(m, r);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   private String getCommand(Mutation m) {
      String chr = m.getChromosome();
      long pos = m.getStartPos();
      StringBuilder sb = new StringBuilder();
      sb.append("mysql -u root --batch -e \"");
      sb.append(String.format(
            "select phylop, phastCons from cons%s where pos = %d\" consScores", chr, pos));
      return sb.toString();
   }

   private void processError(InputStream err) throws IOException {
      BufferedReader br = new BufferedReader(new InputStreamReader(err));
      String s;
      while ((s = br.readLine()) != null) {
         System.out.println(s);
      }
   }

   private Double[] lookupMutation(Mutation m) throws IOException {
      Connection conn = new Connection(hostname);
      conn.connect();
      boolean isAuth = conn.authenticateWithPassword(username, password);
      if (!isAuth) {
         throw new IOException("Authentication failed!");
      }
      Session sess = conn.openSession();
      String command = getCommand(m);
      sess.execCommand(command);
      InputStream err = new StreamGobbler(sess.getStderr());
      processError(err);
      InputStream stdout = new StreamGobbler(sess.getStdout());

      BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
      String s;
      br.readLine();
      s = br.readLine();
      String[] ss = s.split("\t");
      return new Double[] { Double.parseDouble(ss[0]),
            Double.parseDouble(ss[1]) };
   }
}
