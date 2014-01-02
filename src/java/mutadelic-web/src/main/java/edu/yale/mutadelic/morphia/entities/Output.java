package edu.yale.mutadelic.morphia.entities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

import edu.yale.mutadelic.morphia.dao.WorkflowDAO;

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
	
	
}
