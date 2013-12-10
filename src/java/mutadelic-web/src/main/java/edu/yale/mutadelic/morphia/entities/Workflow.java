package edu.yale.mutadelic.morphia.entities;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "workflows")
public class Workflow extends MutadelicEntity {

	@Reference
	private User owner;

	@Property("exec_doc")
	private String execDoc;

	@Property("staging_doc")
	private String stagingDoc;

	@Property("orig_doc")
	private String origDoc;

	private String name;

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public String getExecDoc() {
		return execDoc;
	}

	public void setExecDoc(String execDoc) {
		this.execDoc = execDoc;
	}

	public String getStagingDoc() {
		return stagingDoc;
	}

	public void setStagingDoc(String stagingDoc) {
		this.stagingDoc = stagingDoc;
	}

	public String getOrigDoc() {
		return origDoc;
	}

	public void setOrigDoc(String origDoc) {
		this.origDoc = origDoc;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
