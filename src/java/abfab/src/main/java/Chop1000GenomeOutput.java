import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Chop1000GenomeOutput {

   /**
    * @param args
    */
   public static void main(String[] args) {
      new Chop1000GenomeOutput().exec();
   }

   private void exec() {
      File f = new File(
            "/home/matt/ALL.chr22.phase1_release_v3.20101123.snps_indels_svs.genotypes.vcf");
      File output = new File("/home/matt/1000genome_chop");
      BufferedReader br = null;
      BufferedWriter bw = null;
      String s;
      int lc = 0;
      try {
         br = new BufferedReader(new FileReader(f));
         bw = new BufferedWriter(new FileWriter(output));
         while ((s = br.readLine()) != null) {
            if (lc % 1000 == 0) {
               System.out.println(lc + " lines read");
            }
            String[] split = s.split("\\t");
            if (split.length > 10) {
               StringBuilder sb = new StringBuilder();
               for (int i = 0; i < 8; i++) {
                  sb.append(split[i] + "\t");
               }
               bw.write(sb.toString());
               bw.write("\n");
               bw.flush();
               lc++;
            }
         }

      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         try {
            bw.close();
            br.close();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }
}
