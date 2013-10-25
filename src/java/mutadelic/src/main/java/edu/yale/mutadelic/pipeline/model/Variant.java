package edu.yale.mutadelic.pipeline.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import edu.yale.abfab.IndividualPlus;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.controller.DLController;
import static edu.yale.abfab.NS.*;

public class Variant {
	String chromosome;
	long startPos;
	long endPos;
	String reference;
	String observed;
	String strand;

	public Variant(String chromosome, long startPos, long endPos,
			String reference, String observed, String strand) {
		this.chromosome = chromosome;
		this.startPos = startPos;
		this.endPos = endPos;
		this.reference = reference;
		this.observed = observed;
		this.strand = strand;
	}

	public static Variant fromOWL(IndividualPlus input) {
		return null;
	}

	public IndividualPlus toOWL(DLController dl) {
		String varID = "var-" + UUID.randomUUID().toString();
		DLIndividual<?> var = dl.individual(NS + varID);
		Set<DLAxiom<?>> ax = new HashSet<>();

		String locusID = "locus-" + UUID.randomUUID().toString();
		DLIndividual<?> locus = dl.individual(NS + locusID);
		DLIndividual<?> chrom = dl.individual(String.format("%sChr%s", NS,
				chromosome));
		ax.add(dl.individualType(chrom, dl.clazz(SO + "Chromosome")));
		ax.add(dl.newObjectFact(locus, dl.objectProp(GELO + "on_chromosome"),
				chrom));
		ax.add(dl.newDataFact(locus, dl.dataProp(GELO + "locus_start"),
				dl.asLiteral(startPos)));
		ax.add(dl.newDataFact(locus, dl.dataProp(GELO + "locus_end"),
				dl.asLiteral(endPos)));
		ax.add(dl.newDataFact(locus, dl.dataProp(GELO + "strand"),
				dl.asLiteral(strand)));
		ax.add(dl.newDataFact(locus, dl.dataProp(GELO + "sequence"),
				dl.asLiteral(observed)));

		String modelID = "model-" + UUID.randomUUID().toString();
		DLIndividual<?> model = dl.individual(NS + modelID);
		ax.add(dl.newDataFact(model, dl.dataProp(SIO + "has_value"),
				dl.asLiteral(reference)));

		ax.add(dl.newObjectFact(var, dl.objectProp(GELO + "has_locus"), locus));
		ax.add(dl.newObjectFact(var, dl.objectProp(SIO + "is_modelled_by"),
				model));

		return new IndividualPlus(var, ax);
	}

	public String getChromosome() {
		return chromosome;
	}

	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}

	public long getStartPos() {
		return startPos;
	}

	public void setStartPos(long startPos) {
		this.startPos = startPos;
	}

	public long getEndPos() {
		return endPos;
	}

	public void setEndPos(long endPos) {
		this.endPos = endPos;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getObserved() {
		return observed;
	}

	public void setObserved(String observed) {
		this.observed = observed;
	}

	public String getStrand() {
		return strand;
	}

	public void setStrand(String strand) {
		this.strand = strand;
	}

}
