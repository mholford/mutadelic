import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GetAllPhastCons {
   public static final String FILE_SUFFIX = ".phastCons46way.wigFix.gz";
   public static final String OUTPUT_FILE = "phastCons-output";
   public static final String ADDRESS = "hgdownload.cse.ucsc.edu/goldenpath/hg19/phastCons46way/vertebrate";

   public static void main(String[] args) {
      new GetAllPhastCons().exec();
   }

   private HashSet<String> allChrs;
   private File outputFile;
   private BufferedWriter ofWriter;

   private void init() throws IOException {
      allChrs = new HashSet<String>();
      allChrs.addAll(getRangeAsStringList(1, 22));
      allChrs.add("X");
      allChrs.add("Y");

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
            File zf = dlFile("chr" + chrName);
            f = unzip(zf);
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
      File outFile = new File("phastcons-" + fname + "-input.gz");
      List<String> inv = new ArrayList<String>();
      inv.add("curl");
      inv.add("-s");
      inv.add("-o");
      inv.add("phastcons-" + fname + "-input.gz");
      inv.add(String.format("%s/%s%s", ADDRESS, fname, FILE_SUFFIX));
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
         if (++cnt % 100000 == 0) {
            System.out.println(String.format("%d lines read", cnt));
         }
         if (s.startsWith("fixedStep")) {
            String[] words = s.split("\\W+");
            num = Integer.parseInt(words[4]);
         } else {
            double val = Double.parseDouble(s);
            ofWriter.write(String.format("%s\t%d\t%10.3f\n", chr, num, val));
            ofWriter.flush();
            ++num;
         }
      }
      br.close();
   }

   private void cleanUp(File f) {
       f.delete();
   }
}
