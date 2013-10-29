package edu.yale.mutadelic.pipeline;

import static org.junit.Assert.*;
import static edu.yale.abfab.NS.*;

import org.junit.Test;

import edu.yale.abfab.IndividualPlus;
import edu.yale.mutadelic.pipeline.model.Variant;
import edu.yale.mutadelic.pipeline.service.DefaultValues;

public class PipelineExecutorTest {

	@Test
	public void testIndelOrPointService() {
		try {
			PipelineExecutor pex = new PipelineExecutor();
			Variant v1 = new Variant("1", 123, 123, "G", "A", "+");
			DefaultValues.ALLELE_FREQUENCY = 0.001;
			IndividualPlus output = pex.execute(v1);
			String varType = pex.getLiteralResult(output, NS + "VariationType");
			assertEquals(varType, "Point");

			Variant v2 = new Variant("1", 234, 234, "G", "GA", "-");
			output = pex.execute(v2);
			varType = pex.getLiteralResult(output, NS + "VariationType");
			assertEquals(varType, "Indel");
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testAlleleFrequencyService() {
		try {
			PipelineExecutor pex = new PipelineExecutor();
			Variant v1 = new Variant("19", 80840, 80840, "CCT", "C", "+");
			IndividualPlus output = pex.execute(v1);
			String preMAF = pex.getLiteralResult(output, NS + "AlleleFrequency");
			Double freq = Double.parseDouble(preMAF);
			assertEquals(new Double(0.2029), freq);
			
			Variant v2 = new Variant("19", 80841, 80841, "CCT", "C", "+");
			// Not a known variant
			output = pex.execute(v2);
			preMAF= pex.getLiteralResult(output, NS + "AlleleFrequency");
			freq = Double.parseDouble(preMAF);
			assertEquals(new Double(0d), freq);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testAlignVariantService() {
		try {
			PipelineExecutor pex = new PipelineExecutor();
			Variant v1 = new Variant("1", 159682233, 159682233, "C", "A", "+");
			IndividualPlus output = pex.execute(v1);
			String alignment = pex
					.getLiteralResult(output, NS + "HGVSNotation");
			assertEquals("NM_000567.2:c.*1082G>T", alignment);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
