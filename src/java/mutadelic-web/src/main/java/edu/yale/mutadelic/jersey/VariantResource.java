package edu.yale.mutadelic.jersey;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import edu.yale.mutadelic.morphia.entities.Variant;

@Path("variants")
public class VariantResource {

	private static final List<String> chroms = Arrays.asList(new String[] {
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
			"13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X",
			"Y" });

	private static final List<String> strands = Arrays.asList(new String[] {
			"+" });

	private static final List<Character> seqChars = Arrays
			.asList(new Character[] { 'A', 'C', 'T', 'G' });

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public List<Variant> addVariantFile(@FormDataParam("file") InputStream is,
			@FormDataParam("file") FormDataContentDisposition info) {
		List<Variant> output = new ArrayList<>();

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String s;

			while ((s = br.readLine()) != null) {
				if (s.length() == 0 || s.startsWith("#")) {
					continue;
				}
				else {
					String[] ss = s.split("\\s+");
					Variant v = new Variant();

					// Must be in 1-22, X, Y
					String chrom = ss[0].trim();
					if (!chroms.contains(chrom)) {
						throw invalid();
					}
					v.setChromosome(chrom);

					// Must be + or -
					// A/o 3/2/2015 only + allowed
					String strand = ss[1].trim();
					if (!strands.contains(strand)) {
						throw invalid();
					}
					v.setStrand(strand);

					// Must be an integer
					String startPre = ss[2].trim();
					try {
						Integer start = Integer.parseInt(startPre);
						v.setStart(start);
					} catch (Exception e) {
						e.printStackTrace();
						throw invalid();
					}

					// Must be an integer
					String endPre = ss[3].trim();
					try {
						Integer end = Integer.parseInt(endPre);
						v.setEnd(end);
					} catch (Exception e) {
						e.printStackTrace();
						throw invalid();
					}

					// Must be ACTG
					String ref = ss[4].trim();
					for (Character c : ref.toCharArray()) {
						if (!seqChars.contains(c)) {
							throw invalid();
						}
					}
					v.setReference(ref);

					// Must be ACTG
					String obs = ss[5].trim();
					for (Character c : ref.toCharArray()) {
						if (!seqChars.contains(c)) {
							throw invalid();
						}
					}
					v.setObserved(obs);

					output.add(v);
				} 
			}

			br.close();
		} catch (Exception e) {
			throw invalid();
		}
		return output;
	}

	private RuntimeException invalid() {
		return new RuntimeException("Invalid file format");
	}
}
