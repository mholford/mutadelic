package edu.yale.mutadelic.jersey;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import edu.yale.abfab.IndividualPlus;
import edu.yale.mutadelic.morphia.entities.AnnotatedVariant;
import edu.yale.mutadelic.morphia.entities.Variant;
import edu.yale.mutadelic.morphia.entities.Workflow;
import edu.yale.mutadelic.morphia.entities.Workflow.CriteriaRestriction;
import edu.yale.mutadelic.morphia.entities.Workflow.Criterion;
import edu.yale.mutadelic.morphia.entities.Workflow.Level;
import edu.yale.mutadelic.pipeline.PipelineExecutor;
import static edu.yale.mutadelic.pipeline.service.AbstractPipelineService.*;

public class AbfabProcessor {

	private PipelineExecutor pipelineExecutor;
	private Workflow workflow;

	public AbfabProcessor(PipelineExecutor pipelineExecutor, Workflow workflow) {
		this.pipelineExecutor = pipelineExecutor;
		this.workflow = workflow;
		pipelineExecutor.setStagingDoc(new StringReader(workflow
				.getStagingDoc()));
		pipelineExecutor.setExecDoc(new StringReader(workflow.getExecDoc()));
	}

	public AnnotatedVariant annotateVariant(Variant v) {
		IndividualPlus ip = pipelineExecutor
				.execute(new edu.yale.mutadelic.pipeline.model.Variant(v
						.getChromosome(), v.getStart(), v.getEnd(), v
						.getReference(), v.getObserved(), v.getStrand()));

		return processOutput(ip, v);
	}

	private AnnotatedVariant processOutput(IndividualPlus ip, Variant v) {
		Map<String, String> values = new HashMap<>();
		Map<String, Level> levels = new HashMap<>();
		
		String flaggedPre = pipelineExecutor.getLiteralResult(ip, COMPLETION_STATUS);
		boolean flagged = flaggedPre != null && flaggedPre.equals("true");

		for (Criterion c : workflow.getCriteria()) {
			Object output;
			String cparam = c.getParam();
			if (c.isLiteral()) {
				String outputPre = pipelineExecutor
						.getLiteralResult(ip, cparam);
				if (outputPre == null) {
					continue;
				}
				try {
					output = Double.parseDouble(outputPre);
				} catch (NumberFormatException e) {
					output = String.valueOf(outputPre);
				}
			} else {
				output = String.valueOf(pipelineExecutor.getResult(ip, cparam));
				if (output == null) {
					continue;
				}
			}
			for (CriteriaRestriction cr : c.getRestrictionLevels().values()) {
				if (matchesRestriction(cr, output)) {
					levels.put(c.getLabel(), cr.getLevel());
				}
			}
			values.put(c.getLabel(), String.valueOf(output));
		}

		AnnotatedVariant av = new AnnotatedVariant();
		av.setVariant(v);
		av.setValues(values);
		av.setValueLevels(levels);
		av.setFlagged(flagged);
		return av;
	}

	private boolean matchesRestriction(CriteriaRestriction cr, Object output) {
		Comparable co = (Comparable) output;
		switch (cr.getType()) {
		case GT:
			return co.compareTo(cr.getValue()) > 0;
		case GTE:
			return co.compareTo(cr.getValue()) >= 0;
		case LT:
			return co.compareTo(cr.getValue()) < 0;
		case LTE:
			return co.compareTo(cr.getValue()) <= 0;
		case EQ:
			return co.compareTo(cr.getValue()) == 0;
		default:
			throw new RuntimeException("Shouldn't happen");
		}
	}

	public PipelineExecutor getPipelineExecutor() {
		return pipelineExecutor;
	}

	public void setPipelineExecutor(PipelineExecutor pipelineExecutor) {
		this.pipelineExecutor = pipelineExecutor;
	}

	public Workflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}

}
