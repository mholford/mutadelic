package edu.yale.mutadelic.pipeline;

import static org.junit.Assert.*;
import static edu.yale.abfab.NS.*;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.yale.abfab.IndividualPlus;
import edu.yale.mutadelic.pipeline.model.Variant;
import edu.yale.mutadelic.pipeline.service.AlignVariantService;
import edu.yale.mutadelic.pipeline.service.DefaultValues;
import edu.yale.mutadelic.pipeline.service.PhylopService;
import edu.yale.mutadelic.pipeline.service.SiftService;

import static edu.yale.mutadelic.pipeline.service.AbstractPipelineService.*;

public class PipelineExecutorTest {

	private static PipelineExecutor pex;

	@BeforeClass
	public static void beforeClass() {
		pex = new PipelineExecutor();
	}
	
	@Test
	public void testAAChangedService() {
		System.out.println("TEST AA CHANGED SERVICE");
		try {
			DefaultValues.ALLELE_FREQUENCY = 0.001;
			Variant v;
			IndividualPlus output;
			String varType;
			
			v = new Variant("22", 50563994, 50563994, "C", "T", "+");
			output = pex.execute(v);
			varType = pex.getLiteralResult(output, VARIATION_OUTCOME);
			assertEquals(varType, SYNONYMOUS);

			v= new Variant("22", 50563994, 50563994, "C", "G", "+");
			output = pex.execute(v);
			varType = pex.getLiteralResult(output, VARIATION_OUTCOME);
			assertEquals(varType, NON_SYNONYMOUS);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testCriticalDomainServiceService() {
		System.out.println("TEST CRITICAL DOMAIN SERVICE");
		try {
			DefaultValues.ALLELE_FREQUENCY = 0.001;
			Variant v;
			IndividualPlus output;
			String varVal;
			
			v = new Variant("1", 158653179, 158653179, "C", "T", "+");
			output = pex.execute(v);
			varVal = pex.getLiteralResult(output, DOMAIN_COLOCATION);
			assertEquals(varVal, "true");
			varVal = pex.getLiteralResult(output, PROTEIN_DOMAIN);
			assertEquals(varVal, "PF00435");

			v= new Variant("1", 158655079, 158655079, "C", "G", "+");
			output = pex.execute(v);
			varVal= pex.getLiteralResult(output, DOMAIN_COLOCATION);
			assertEquals(varVal, "false");
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testIndelOrPointService() {
		System.out.println("TEST INDEL OR POINT SERVICE");
		try {
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
		System.out.println("TEST ALLELE FREQUENCY SERVICE");
		try {
			// PipelineExecutor pex = new PipelineExecutor();
			Variant v1 = new Variant("19", 80840, 80840, "CCT", "C", "+");
			IndividualPlus output = pex.execute(v1);
			String preMAF = pex
					.getLiteralResult(output, NS + "AlleleFrequency");
			Double freq = Double.parseDouble(preMAF);
			assertEquals(new Double(0.2029), freq);

			Variant v2 = new Variant("19", 80841, 80841, "CCT", "C", "+");
			// Not a known variant
			output = pex.execute(v2);
			preMAF = pex.getLiteralResult(output, NS + "AlleleFrequency");
			freq = Double.parseDouble(preMAF);
			assertEquals(new Double(0d), freq);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testAlignVariantService() {
		System.out.println("TEST ALIGN VARIANT SERVICE");
		try {
			// PipelineExecutor pex = new PipelineExecutor();
			Variant v1 = new Variant("22", 50563990, 50563990, "C", "G", "+");
			IndividualPlus output = pex.execute(v1);
			String alignment = pex
					.getLiteralResult(output, NS + "HGVSNotation");
			assertEquals("NM_018995.2:c.1739C>G", alignment);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testRCMDBService() {
		System.out.println("TEST RCMDB SERVICE");
		try {
			// PipelineExecutor pex = new PipelineExecutor();
			Variant v1 = new Variant("1", 159682233, 159682233, "C", "A", "+");
			IndividualPlus output = pex.execute(v1);
			String alignment = pex.getLiteralResult(output, NS
					+ "DatabasePresence");
			assertEquals(false, Boolean.parseBoolean(alignment));

			// Variant v2 = new Variant("1", 158655079, 158655079, "C", "T",
			// "+");
			// output = pex.execute(v2);
			// alignment = pex.getLiteralResult(output, NS +
			// "DatabasePresence");
			// assertEquals(true, Boolean.parseBoolean(alignment));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testSiftService() {
		System.out.println("TEST SIFT SERVICE");
		try {
			// PipelineExecutor pex = new PipelineExecutor();
			Variant v1 = new Variant("1", 229577655, 229577655, "A", "G", "-");
			IndividualPlus output = pex.execute(v1);
			String preSift = pex.getLiteralResult(output, NS + "SiftScore");
			Double sift = Double.parseDouble(preSift);
			assertEquals(new Double(0.04), sift);

			Variant v2 = new Variant("1", 229553999, 229553999, "C", "A", "+");
			output = pex.execute(v2);
			preSift = pex.getLiteralResult(output, NS + "SiftScore");
			assertEquals(null, preSift);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testCCDSAlign() {
		try {
			AlignVariantService avs = new AlignVariantService();
			Variant v;
			String result;
			
			v = new Variant("Chr1", 158655079, 158655079, "C", "T", "+");
			result = avs.getAlignmentFromCCDS(v);
			assertEquals(result, "NM_003126.2:c.83C>T");

			v = new Variant("Chr22", 50563990, 50563990, "C", "T", "+");
			result = avs.getAlignmentFromCCDS(v);
			assertEquals("NM_018995.2:c.1739C>T", result);

			v = new Variant("Chr22", 50564234, 50564234, "C", "T", "+");
			result = new AlignVariantService().getAlignmentFromCCDS(v);
			assertEquals(result, "NM_018995.2:c.1747+236C>T");

			v = new Variant("Chr22", 50564345, 50564345, "C", "T", "+");
			result = new AlignVariantService().getAlignmentFromCCDS(v);
			assertEquals(result, "NM_018995.2:c.1748-286C>T");
			
			v = new Variant("Chr22", 50510000, 50510000, "C", "T", "-");
			result = new AlignVariantService().getAlignmentFromCCDS(v);
			assertEquals(result, "NM_139202.2:c.715-988C>T");
			
			v = new Variant("Chr22", 50512620, 50512620, "C", "T", "-");
			result = new AlignVariantService().getAlignmentFromCCDS(v);
			assertEquals(result, "NM_139202.2:c.714+25C>T");
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testPhylopScore() {
		try {
			String test = "||";
			String[] ts = test.split("\\|", -1);

			Variant v1 = new Variant("1", 229554000, 229554000, "C", "A", "+");
			Double phylop = new PhylopService().getPhylopScore(v1);
			assertEquals(new Double(0.075), phylop);

			Variant v2 = new Variant("1", 229553999, 229553999, "C", "A", "+");
			phylop = new PhylopService().getPhylopScore(v2);
			assertEquals(null, phylop);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testSiftScore() {
		try {
			Variant v1 = new Variant("1", 229577655, 229577655, "A", "G", "-");
			Double sift = new SiftService().getSiftScore(v1);
			assertEquals(new Double(0.04), sift);

			Variant v2 = new Variant("1", 229553999, 229553999, "C", "A", "+");
			sift = new SiftService().getSiftScore(v2);
			assertEquals(null, sift);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
