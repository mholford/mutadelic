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
			Variant v1 = new Variant("1", 123, 123, "G", "G", "+");
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

}
