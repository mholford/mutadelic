package edu.yale.mutadelic.ncbivr;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.Test;

public class NCBIVariationReporterTest {

	@Test
	public void test() {
		try {
			String testVar = "NC_000001.10:g.159682233C>A\nNC_000008.10:g.19819724C>G";

			NCBIVariationReporter r = new NCBIVariationReporter();
			String result = r.analyze("9606",
					NCBIVariationReporter.ASSEMBLY_37, testVar);

			System.out.println(result);
			assertNotNull(result);

			BufferedReader br = new BufferedReader(new StringReader(result));
			String s;
			br.readLine();
			while ((s = br.readLine()) != null) {
				if (s.length() > 0 && !(s.startsWith("#"))) {
					String[] ss = s.split("\t");
					if (ss.length > 12) {
						String alignment = ss[12];
						System.out.println("Alignment: " + alignment);
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
