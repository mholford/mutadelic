package edu.yale.mutadelic.jersey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import edu.yale.mutadelic.morphia.entities.User;
import edu.yale.mutadelic.morphia.entities.Workflow;
import edu.yale.mutadelic.morphia.entities.Workflow.Criterion;
import edu.yale.mutadelic.morphia.entities.Workflow.Level;
import edu.yale.mutadelic.morphia.entities.Workflow.RestrictionType;
import static edu.yale.mutadelic.pipeline.service.AbstractPipelineService.*;

public class Defaults {

	public static User getDefaultUser() {
		User u = new User();
		u.setEmail("matt.holford@gmail.com");
		u.setName("Mutadelic User");
		u.setId(2);

		return u;
	}

	public static Workflow getDefaultWorkflow() throws Exception {
		Workflow w = new Workflow();

		String stagingDoc = streamToString("pipeline-stage.owl");
		w.setStagingDoc(stagingDoc);

		String execDoc = streamToString("pipeline.owl");
		w.setExecDoc(execDoc);

		List<Criterion> criteria = getDefaultCriteria();
		w.setCriteria(criteria);

		w.setName("Default Workflow");
		w.setOwner(2);
		w.setId(2);

		return w;
	}

	private static List<Criterion> getDefaultCriteria() {
		List<Criterion> criteria = new ArrayList<>();

		criteria.add(new Criterion(DATABASE_PRESENCE, "In Red Cell DB", true,
				RestrictionType.EQ, "true", Level.UP));
		criteria.add(new Criterion(ALLELE_FREQUENCY, "Allele Frequency", true,
				RestrictionType.LTE, 0.01, Level.UP));
		criteria.add(new Criterion(SIFT_SCORE, "Sift Score", true,
				RestrictionType.LTE, 0.05, Level.UP));
		criteria.add(new Criterion(VARIATION_LOCATION, "Locus", true,
				RestrictionType.EQ, "SpliceSite"));
		criteria.add(new Criterion(PHYLOP_SCORE, "PhyloP Score", true,
				RestrictionType.GTE, 1.0, Level.UP));
		criteria.add(new Criterion(DOMAIN_COLOCATION, "In Critical Domain",
				true, RestrictionType.EQ, "true", Level.UP));

		return criteria;
	}

	private static String streamToString(String resourceName)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				Defaults.class.getClassLoader().getResourceAsStream(
						resourceName)));
		String s;
		while ((s = br.readLine()) != null) {
			sb.append(s);
		}
		return sb.toString();
	}
}
