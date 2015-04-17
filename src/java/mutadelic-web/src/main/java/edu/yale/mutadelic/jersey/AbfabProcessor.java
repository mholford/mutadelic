package edu.yale.mutadelic.jersey;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import edu.yale.abfab.Abductor;
import edu.yale.abfab.IndividualPlus;
import edu.yale.abfab.owlapi.HermitAbductor;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.controller.DLController;
import edu.yale.mutadelic.morphia.entities.AnnotatedVariant;
import edu.yale.mutadelic.morphia.entities.ValueEntry;
import edu.yale.mutadelic.morphia.entities.Variant;
import edu.yale.mutadelic.morphia.entities.Workflow;
import edu.yale.mutadelic.morphia.entities.Workflow.CriteriaRestriction;
import edu.yale.mutadelic.morphia.entities.Workflow.Criterion;
import edu.yale.mutadelic.morphia.entities.Workflow.Level;
import edu.yale.mutadelic.pipeline.PipelineExecutor;
import edu.yale.mutadelic.pipeline.PipelineExecutor.PipelineResult;
import static edu.yale.mutadelic.pipeline.service.AbstractPipelineService.*;
import static edu.yale.abfab.NS.*;

public class AbfabProcessor {

	private PipelineExecutor pipelineExecutor;
	private Workflow workflow;

	public AbfabProcessor(PipelineExecutor pipelineExecutor, Workflow workflow) {
		this.pipelineExecutor = pipelineExecutor;
		this.workflow = workflow;
		BufferedReader stagingReader = new BufferedReader(new StringReader(workflow
				.getStagingDoc()));
		try {
			stagingReader.mark(100000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pipelineExecutor.setStagingDoc(stagingReader);
		BufferedReader execReader = new BufferedReader(new StringReader(workflow.getExecDoc()));
		try {
			execReader.mark(100000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pipelineExecutor.setExecDoc(execReader);
	}

	public AnnotatedVariant annotateVariant(Variant v) {
		IndividualPlus ip = pipelineExecutor
				.execute(new edu.yale.mutadelic.pipeline.model.Variant(v
						.getChromosome(), v.getStart(), v.getEnd(), v
						.getReference(), v.getObserved(), v.getStrand()));

		return processOutput(ip, v);
	}

	private Set<DLAxiom<?>> variantAxioms(Variant v, IndividualPlus ip) {
		Set<DLAxiom<?>> ax = new HashSet<>();
		Abductor ab = new HermitAbductor("VariantAxioms");
		DLController dl = ab.getDLController();

		DLIndividual<?> var = ip.getIndividual();
		String locusID = "locus-" + UUID.randomUUID().toString();
		DLIndividual<?> locus = dl.individual(NS + locusID);
		DLIndividual<?> chrom = dl.individual(String.format("%sChr%s", NS,
				v.getChromosome()));
		ax.add(dl.individualType(chrom, dl.clazz(SO + "Chromosome")));
		ax.add(dl.newObjectFact(locus, dl.objectProp(GELO + "on_chromosome"),
				chrom));
		ax.add(dl.newDataFact(locus, dl.dataProp(GELO + "locus_start"),
				dl.asLiteral(v.getStart())));
		ax.add(dl.newDataFact(locus, dl.dataProp(GELO + "locus_end"),
				dl.asLiteral(v.getEnd())));
		ax.add(dl.newDataFact(locus, dl.dataProp(GELO + "strand"),
				dl.asLiteral(v.getStrand())));
		ax.add(dl.newDataFact(locus, dl.dataProp(GELO + "sequence"),
				dl.asLiteral(v.getObserved())));

		String modelID = "model-" + UUID.randomUUID().toString();
		DLIndividual<?> model = dl.individual(NS + modelID);
		ax.add(dl.newDataFact(model, dl.dataProp(SIO + "has_value"),
				dl.asLiteral(v.getReference())));

		ax.add(dl.newObjectFact(var, dl.objectProp(GELO + "has_locus"), locus));
		ax.add(dl.newObjectFact(var, dl.objectProp(SIO + "is_modelled_by"),
				model));
		ax.add(dl.individualType(var, dl.clazz(NS + "Variant")));

		return ax;
	}

	private AnnotatedVariant processOutput(IndividualPlus ip, Variant v) {
		Map<String, String> values = new HashMap<>();
		Map<String, Level> levels = new HashMap<>();

		String flaggedPre = pipelineExecutor.getLiteralResult(ip,
				COMPLETION_STATUS).getResult();
		boolean flagged = flaggedPre != null && flaggedPre.equals("true");

		Set<DLAxiom<?>> preRDF = new HashSet<>();
		preRDF.addAll(variantAxioms(v, ip));

		for (Criterion c : workflow.getCriteria()) {
			Object output;
			String cparam = c.getParam();
			if (c.isLiteral()) {
				PipelineResult pr = pipelineExecutor.getLiteralResult(ip,
						cparam);
				if (pr == null) {
					continue;
				}
				String outputPre = pr.getResult();
				if (outputPre == null) {
					continue;
				}
				preRDF.addAll(pr.getAxioms());

				try {
					output = Double.parseDouble(outputPre);
				} catch (NumberFormatException e) {
					output = String.valueOf(outputPre);
				}
			} else {
				PipelineResult pr = pipelineExecutor.getResult(ip, cparam);
				if (pr == null) {
					continue;
				}
				output = pr.getResult();
				if (output == null) {
					continue;
				}
				preRDF.addAll(pr.getAxioms());

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
		for (String vk : values.keySet()) {
			String vv = values.get(vk);
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
		// av.setRdf(toRDF(ip));
		av.setRdf(toRDF(preRDF));
		return av;
	}

	private String toRDF(Set<DLAxiom<?>> preRDF) {
		Abductor ab = new HermitAbductor("preRDF");
		ab.setNamespace(NS);
		DLController dl = ab.getDLController();
		dl.newOntology();
		dl.addAxioms(preRDF);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			dl.saveOntology(baos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toString();
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
