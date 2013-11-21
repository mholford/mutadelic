package edu.yale.mutadelic.morphia.entities;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Property;
import com.google.code.morphia.annotations.Reference;

@Entity(value="outputs")
public class Output {

	@Id
	private ObjectId id;
	
	@Reference
	private User owner;
	
	@Reference(lazy = true)
	private Workflow workflow;
	
	@Reference(lazy = true)
	private Input input;
	
	@Embedded
	private List<AnnotatedVariant> results;

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Workflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}

	public Input getInput() {
		return input;
	}

	public void setInput(Input input) {
		this.input = input;
	}

	public List<AnnotatedVariant> getResults() {
		return results;
	}

	public void setResults(List<AnnotatedVariant> results) {
		this.results = results;
	}
}


