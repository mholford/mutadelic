package edu.yale.mutadelic.morphia.entities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "outputs")
public class Output extends MutadelicEntity {

	@Property("user_id")
	private Integer owner;

	@Property("workflow_id")
	private Integer workflow;

	@Property("input_id")
	private Integer input;

	@Embedded
	private List<AnnotatedVariant> results;

	public Integer getOwner() {
		return owner;
	}

	public void setOwner(Integer owner) {
		this.owner = owner;
	}

	public Integer getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Integer workflow) {
		this.workflow = workflow;
	}

	public Integer getInput() {
		return input;
	}

	public void setInput(Integer input) {
		this.input = input;
	}

	public List<AnnotatedVariant> getResults() {
		return results;
	}

	public void setResults(List<AnnotatedVariant> results) {
		this.results = results;
	}

	public File asExcelFile() throws Exception {
		String outfileName = String.format("mutadelic-output-%d.xls", getId());
		File output = new File(outfileName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(output));

		// Write header
		bw.write(String
				.format("Flagged\tChromosome\tStrand\tStart\tEnd\tReference\tObserved\t"
						+ "Property Name\tProperty Value\tSignificant\n"));
		bw.flush();

		for (AnnotatedVariant av : results) {
			for (ValueEntry ve : av.valueEntries) {
				bw.write(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n", av
						.isFlagged() ? "Y" : "N", av.getVariant()
						.getChromosome(), av.getVariant().getStrand(), av
						.getVariant().getStart(), av.getVariant().getEnd(), av
						.getVariant().getReference(), av.getVariant()
						.getObserved(), ve.getKey(), ve.getValue(), ve
						.getLevel().equals("UP") ? "Y" : "N"));
				bw.flush();
			}
		}
		
		bw.close();
		
		return output;
	}
}
