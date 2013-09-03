import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetAllSift {
	final String regex = "(.*):g\\.(\\d*)([ACTG])>([ACTG])";
	
	public void init() {
		Pattern p = Pattern.compile(regex);
		BufferedWriter bw = null;
		Connection connection = null;
		try {
			bw = new BufferedWriter(new FileWriter(new File("all-sifts.out")));
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager
					.getConnection("jdbc:mysql://ristretto.med.yale.edu/homo_sapiens_variation_67_37?user=vep&password=vep");
			Statement statement = connection.createStatement();
			ResultSet rs = statement
					.executeQuery("select hgvs_genomic, sift_score from transcript_variation "
							+ "where sift_score is not NULL");
			while (rs.next()) {
				String hgvs = rs.getString(1);
				String score = rs.getString(2);
				String chr = null, pos = null, ref = null, mut = null;
				
				Matcher m = p.matcher(hgvs);
				if (m.matches()) {
					chr = m.group(1);
					pos = m.group(2);
					ref = m.group(3);
					mut = m.group(4);
				} else {
					System.out.println(String.format("%s did not match", hgvs));
				}
				bw.write(String.format("%s\t%s\t%s\t%s\t%s\n", chr, pos, ref, mut, score));
				bw.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}

	public static void main(String[] args) {
		new GetAllSift().init();
	}

}
