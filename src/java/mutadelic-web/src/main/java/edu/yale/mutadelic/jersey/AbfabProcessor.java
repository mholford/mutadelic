package edu.yale.mutadelic.jersey;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.yale.abfab.Abductor;
import edu.yale.abfab.IndividualPlus;
import edu.yale.abfab.owlapi.HermitAbductor;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.controller.DLController;
import edu.yale.mutadelic.morphia.entities.AnnotatedVariant;
import edu.yale.mutadelic.morphia.entities.ValueEntry;
import edu.yale.mutadelic.morphia.entities.Variant;
import edu.yale.mutadelic.morphia.entities.Workflow;
import edu.yale.mutadelic.morphia.entities.Workflow.CriteriaRestriction;
import edu.yale.mutadelic.morphia.entities.Workflow.Criterion;
import edu.yale.mutadelic.morphia.entities.Workflow.Level;
import edu.yale.mutadelic.pipeline.PipelineExecutor;
import static edu.yale.mutadelic.pipeline.service.AbstractPipelineService.*;
import static edu.yale.abfab.NS.*;

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
				} else {
					levels.put(c.getLabel(), Level.DOWN);
				}
			}
			values.put(c.getLabel(), String.valueOf(output));
		}
		
		List<ValueEntry> valueEntries = new ArrayList<>();
		for (String vk:values.keySet()) {
			String vv=values.get(vk);
			String level = levels.get(vk).toString();
			ValueEntry ve = new ValueEntry();
			ve.setKey(vk);
			ve.setValue(vv);
			ve.setLevel(level);
			valueEntries.add(ve);
		}

		AnnotatedVariant av = new AnnotatedVariant();
		av.setVariant(v);
		av.setValueEntries(valueEntries);
		av.setFlagged(flagged);
		av.setRdf(toRDF(ip));
		return av;
	}
	
	private String toRDF(IndividualPlus ip) {
		Abductor ab = new HermitAbductor("test");
		ab.setNamespace(NS);
		DLController dl = ab.getDLController();
		dl.newOntology();
		dl.addAxioms(ip.getAxioms());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			dl.saveOntology(baos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toString();
	}

	private boolean matchesRestriction(CriteriaRestriction cr, Object output) {
		Comparable co = (Comparable) output;
		switch (cr.getType()) {
		case GT:
			Double d = Double.parseDouble(cr.getValue());
			return co.compareTo(d) > 0;
		case GTE:
			d = Double.parseDouble(cr.getValue());
			return co.compareTo(d) >= 0;
		case LT:
			d = Double.parseDouble(cr.getValue());
			return co.compareTo(d) < 0;
		case LTE:
			d = Double.parseDouble(cr.getValue());
			return co.compareTo(d) <= 0;
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
