package edu.yale.mutadelic.ncbivr;

import static org.junit.Assert.*;

import org.junit.Test;

public class NCBIVariationReporterTest {

	@Test
	public void test() {
		try {
			String testVar = "NC_000001.10:g.159682233C>A\nNC_000008.10:g.19819724C>G";
			
			NCBIVariationReporter r = new NCBIVariationReporter();
			String result = r.analyze("9606", NCBIVariationReporter.ASSEMBLY_37, testVar);
			
			System.out.println(result);
			assertNotNull(result);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
