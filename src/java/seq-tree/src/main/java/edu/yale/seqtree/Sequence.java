package edu.yale.seqtree;

public class Sequence {
	private long start;
	private long end;
	private String chromosome;
	private String name;

	public Sequence() {

	}

	public Sequence(long start, long end, String chromosome) {
		this.start = start;
		this.end = end;
		this.chromosome = chromosome;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public String getChromosome() {
		return chromosome;
	}

	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		long result = 1;
		result = prime * result + start + end;
		return (int)result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sequence other = (Sequence) obj;
		if (start != other.start && end != other.end && !(chromosome.equals(other.chromosome)))
			return false;
		return true;
	}
}
