package edu.yale.mutadelic.pipeline.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.management.RuntimeErrorException;

import edu.yale.abfab.IndividualPlus;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.DLLiteral;
import edu.yale.dlgen.controller.DLController;
import static edu.yale.abfab.NS.*;

public class Variant {
	String chromosome;
	int startPos;
	int endPos;
	String reference;
	String observed;
	String strand;

	public Variant(String chromosome, int startPos, int endPos,
			String reference, String observed, String strand) {
		this.chromosome = chromosome;
		this.startPos = startPos;
		this.endPos = endPos;
		this.reference = reference;
		this.observed = observed;
		this.strand = strand;
	}

	public static Variant fromOWL(DLController dl, IndividualPlus input) {
		dl.addAxioms(input.getAxioms());
		Collection<DLIndividual> loci = dl.getObjectPropertyValues(
				input.getIndividual(), dl.objectProp(GELO + "has_locus"));
		if (loci.size() != 1) {
			throw new RuntimeException(String.format(
					"Unexpected number of loci (%d) for variant (%s)",
					loci.size(), input.getIndividual()));
		}
		DLIndividual<?> locus = loci.iterator().next();

		Collection<DLIndividual> chromInds = dl.getObjectPropertyValues(locus,
				dl.objectProp(GELO + "on_chromosome"));
		if (chromInds.size() != 1) {
			throw new RuntimeException(String.format(
					"Unexpected number of chromosomes (%d) for locus (%s)",
					chromInds.size(), locus));
		}
		DLIndividual<?> chromInd = chromInds.iterator().next();
		String chromIndString = dl.getIRI(chromInd);
		String chromosome = chromIndString.substring(
				chromIndString.lastIndexOf('#') + 1, chromIndString.length());

		Collection<DLLiteral> locusStarts = dl.getDataPropertyValues(locus,
				dl.dataProp(GELO + "locus_start"));
		if (locusStarts.size() != 1) {
			throw new RuntimeException(String.format(
					"Unexpected number of locus starts (%d) for locus (%s)",
					locusStarts.size(), locus));
		}
		DLLiteral<?> locusStart = locusStarts.iterator().next();
		int start = Integer.parseInt(dl.getLiteralValue(locusStart));

		Collection<DLLiteral> locusEnds = dl.getDataPropertyValues(locus,
				dl.dataProp(GELO + "locus_end"));
		if (locusEnds.size() != 1) {
			throw new RuntimeException(String.format(
					"Unexpected number of locus ends (%d) for locus (%s)",
					locusEnds.size(), locus));
		}
		DLLiteral<?> locusEnd = locusEnds.iterator().next();
		int end = Integer.parseInt(dl.getLiteralValue(locusEnd));

		Collection<DLLiteral> locusStrands = dl.getDataPropertyValues(locus,
				dl.dataProp(GELO + "strand"));
		if (locusStrands.size() != 1) {
			throw new RuntimeException(String.format(
					"Unexpected number of locus strands (%d) for locus (%s)",
					locusStrands.size(), locus));
		}
		DLLiteral<?> locusStrand = locusStrands.iterator().next();
		String strand = dl.getLiteralValue(locusStrand);

		Collection<DLLiteral> locusSeqs = dl.getDataPropertyValues(locus,
				dl.dataProp(GELO + "sequence"));
		if (locusSeqs.size() != 1) {
			throw new RuntimeException(String.format(
					"Unexpected number of locus seqs (%d) for locus (%s)",
					locusSeqs.size(), locus));
		}
		DLLiteral<?> locusSeq = locusSeqs.iterator().next();
		String seq = dl.getLiteralValue(locusSeq);

		Collection<DLIndividual> models = dl.getObjectPropertyValues(
				input.getIndividual(), dl.objectProp(SIO + "is_modelled_by"));
		if (models.size() != 1) {
			throw new RuntimeException(String.format(
					"Unexpected number of models (%d) for variant (%s)",
					models.size(), input.getIndividual()));
		}
		DLIndividual<?> model = models.iterator().next();

		Collection<DLLiteral> modelVals = dl.getDataPropertyValues(model,
				dl.dataProp(SIO + "has_value"));
		if (modelVals.size() != 1) {
			throw new RuntimeException(String.format(
					"Unexpected number of model values (%d) for model(%s)",
					modelVals.size(), model));
		}
		DLLiteral<?> modelVal = modelVals.iterator().next();
		String reference = dl.getLiteralValue(modelVal);

		return new Variant(chromosome, start, end, reference, seq, strand);
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
		ax.add(dl.newDataFact(locus, dl.dataProp(NS + "cache_value_ignore"),
				dl.asLiteral(GELO + "locus_start")));
		ax.add(dl.newDataFact(locus, dl.dataProp(NS + "cache_value_ignore"),
				dl.asLiteral(GELO + "locus_end")));
		ax.add(dl.newDataFact(locus, dl.dataProp(NS + "cache_value_ignore"),
				dl.asLiteral(GELO + "strand")));
		ax.add(dl.newDataFact(locus, dl.dataProp(NS + "cache_value_ignore"),
				dl.asLiteral(GELO + "sequence")));

		String modelID = "model-" + UUID.randomUUID().toString();
		DLIndividual<?> model = dl.individual(NS + modelID);
		ax.add(dl.newDataFact(model, dl.dataProp(SIO + "has_value"),
				dl.asLiteral(reference)));
		ax.add(dl.newDataFact(model, dl.dataProp(NS + "cache_value_ignore"),
				dl.asLiteral(SIO + "has_value")));

		ax.add(dl.newObjectFact(var, dl.objectProp(GELO + "has_locus"), locus));
		ax.add(dl.newObjectFact(var, dl.objectProp(SIO + "is_modelled_by"),
				model));
		ax.add(dl.individualType(var, dl.clazz(NS + "Variant")));

		return new IndividualPlus(var, ax);
	}

	public String getChromosome() {
		return chromosome;
	}

	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}

	public int getStartPos() {
		return startPos;
	}

	public void setStartPos(int startPos) {
		this.startPos = startPos;
	}

	public int getEndPos() {
		return endPos;
	}

	public void setEndPos(int endPos) {
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
