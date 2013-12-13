package edu.yale.mutadelic.morphia.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "workflows")
public class Workflow extends MutadelicEntity {
	
	public static enum RestrictionType {
		GT, GTE, LT, LTE, EQ
	}
	
	public static enum Level {
		UP, DOWN
	}
	
	public static class CriteriaRestriction {
		RestrictionType type;
		Comparable value;
		
		public CriteriaRestriction() {}
		
		public CriteriaRestriction(RestrictionType type, Comparable value) {
			this.type = type;
			this.value = value;
		}

		public RestrictionType getType() {
			return type;
		}

		public void setType(RestrictionType type) {
			this.type = type;
		}
		
		public Comparable getValue() {
			return value;
		}
		
		public void setValue(Comparable value) {
			this.value = value;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CriteriaRestriction other = (CriteriaRestriction) obj;
			if (type != other.type)
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
	}
	
	public static class Criterion {
		String param;
		String label;
		Map<CriteriaRestriction, Level> restrictionLevels;
		boolean literal;
		
		public Criterion() {}
		
		public Criterion(String param, String label, boolean literal, Object... restrictions) {
			Map<CriteriaRestriction, Level> restrictionLevels = new HashMap<>();
			if (restrictions.length % 3 != 0) {
				throw new RuntimeException("Improperly defined restrictions for Criterion");
			}
			for (int i = 0; i < restrictions.length; i++) {
				Object rtype = restrictions[i];
				if (!(rtype instanceof RestrictionType)) {
					throw new RuntimeException(String.format("Parameter %d must be a RestrictionType", i));
				}
				RestrictionType rt = (RestrictionType) rtype;
				
				Object comp = restrictions[++i];
				if (!(comp instanceof Comparable)) {
					throw new RuntimeException(String.format("Parameter %d must be a Comparable", i));
				}
				Comparable c = (Comparable) comp;
				
				CriteriaRestriction cr = new CriteriaRestriction(rt, c);
				
				Object level = restrictions[++i];
				if (!(level instanceof Level)) {
					throw new RuntimeException(String.format("Paramenter %d must be a Level", i));
				}
				Level l = (Level) level;
				
				restrictionLevels.put(cr, l);
			}
			this.param = param;
			this.label = label;
			this.literal = literal;
			this.restrictionLevels = restrictionLevels;
		}
		
		public Criterion(String param, String label, boolean literal, Map<CriteriaRestriction, Level> restrictionLevels) {
			this.param = param;
			this.label = label;
			this.literal = literal;
			this.restrictionLevels = restrictionLevels;
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
		public Map<CriteriaRestriction, Level> getRestrictionLevels() {
			return restrictionLevels;
		}
		public void setRestrictionLevels(
				Map<CriteriaRestriction, Level> restrictionLevels) {
			this.restrictionLevels = restrictionLevels;
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
