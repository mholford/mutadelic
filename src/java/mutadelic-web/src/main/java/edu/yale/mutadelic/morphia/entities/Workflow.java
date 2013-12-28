package edu.yale.mutadelic.morphia.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "workflows")
public class Workflow extends MutadelicEntity {

	public static enum RestrictionType {
		GT, LT, EQ, GTE, LTE
	}

	public static enum Level {
		UP, DOWN
	}

	@Embedded
	public static class CriteriaRestriction {
		RestrictionType type;
		String value;
		Level level;

		public CriteriaRestriction() {
		}

		public CriteriaRestriction(RestrictionType type, String value,
				Level level) {
			this.type = type;
			this.value = value;
			this.level = level;
		}

		public RestrictionType getType() {
			return type;
		}

		public void setType(RestrictionType type) {
			this.type = type;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public Level getLevel() {
			return level;
		}

		public void setLevel(Level level) {
			this.level = level;
		}
	}

	@Embedded
	public static class Criterion {
		String param;
		String label;
		
		@Embedded
		Map<String, CriteriaRestriction> criteriaRestrictions;
		
		boolean literal;
		
		public Criterion() {
			
		}

		public Criterion(String param, String label, boolean literal,
				Map<String, CriteriaRestriction> criteriaRestrictions) {
			this.param = param;
			this.label = label;
			this.criteriaRestrictions = criteriaRestrictions;
			this.literal = literal;
		}
		
		public Criterion(String param, String label, boolean literal, Object... restr) {
			this.param = param;
			this.label = label;
			this.literal = literal;
			Map<String, CriteriaRestriction> cr = new HashMap<>(); 
			for (int i = 0; i < restr.length; i++) {
				String rname = (String) restr[i];
				RestrictionType rtype = (RestrictionType) restr[++i];
				String comp = (String) restr[++i];
				Level lvl = (Level) restr[++i];
				cr.put(rname, new CriteriaRestriction(rtype, comp, lvl));
			}
			this.criteriaRestrictions = cr;
		}

		public String getParam() {
			return param;
		}

		public void setParam(String param) {
			this.param = param;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public Map<String, CriteriaRestriction> getRestrictionLevels() {
			return criteriaRestrictions;
		}

		public void setRestrictionLevels(
				Map<String, CriteriaRestriction> criteriaRestrictions) {
			this.criteriaRestrictions = criteriaRestrictions;
		}

		public boolean isLiteral() {
			return literal;
		}

		public void setLiteral(boolean literal) {
			this.literal = literal;
		}
	}

	@Property("user_id")
	private Integer owner;

	@Property("exec_doc")
	private String execDoc;

	@Property("staging_doc")
	private String stagingDoc;

	@Property("orig_doc")
	private String origDoc;

	private String name;

	@Embedded
	private List<Criterion> criteria;

	public List<Criterion> getCriteria() {
		return criteria;
	}

	public void setCriteria(List<Criterion> criteria) {
		this.criteria = criteria;
	}

	public Integer getOwner() {
		return owner;
	}

	public void setOwner(Integer owner) {
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
