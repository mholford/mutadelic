package edu.yale.med.krauthammerlab.abfab.old.vep;

public class Mutation {
   private String chromosome;
   private long startPos;
   private long endPos;
   private String refSequence;
   private String mutSequence;
   private String strand;
   private String refseq;

   public Mutation(String chromosome, long startPos, long endPos,
         String refSequence, String mutSequence, String strand, String refseq) {
      super();
      this.chromosome = chromosome;
      this.startPos = startPos;
      this.endPos = endPos;
      this.refSequence = refSequence;
      this.mutSequence = mutSequence;
      this.strand = strand;
      this.refseq = refseq;
   }

   public String getChromosome() {
      return chromosome;
   }

   public long getStartPos() {
      return startPos;
   }

   public long getEndPos() {
      return endPos;
   }

   public String getRefSequence() {
      return refSequence;
   }

   public String getMutSequence() {
      return mutSequence;
   }

   public String getStrand() {
      return strand;
   }
   
   public String getRefseq() {
      return refseq;
   }

   public void setChromosome(String chromosome) {
      this.chromosome = chromosome;
   }

   public void setStartPos(long startPos) {
      this.startPos = startPos;
   }

   public void setEndPos(long endPos) {
      this.endPos = endPos;
   }

   public void setRefSequence(String refSequence) {
      this.refSequence = refSequence;
   }

   public void setMutSequence(String mutSequence) {
      this.mutSequence = mutSequence;
   }

   public void setStrand(String strand) {
      this.strand = strand;
   }
   
   public void setRefseq(String refseq) {
      this.refseq = refseq;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
            + ((chromosome == null) ? 0 : chromosome.hashCode());
      result = prime * result + (int) (endPos ^ (endPos >>> 32));
      result = prime * result
            + ((mutSequence == null) ? 0 : mutSequence.hashCode());
      result = prime * result
            + ((refSequence == null) ? 0 : refSequence.hashCode());
      result = prime * result + (int) (startPos ^ (startPos >>> 32));
      result = prime * result + ((strand == null) ? 0 : strand.hashCode());
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
      Mutation other = (Mutation) obj;
      if (chromosome == null) {
         if (other.chromosome != null)
            return false;
      } else if (!chromosome.equals(other.chromosome))
         return false;
      if (endPos != other.endPos)
         return false;
      if (mutSequence == null) {
         if (other.mutSequence != null)
            return false;
      } else if (!mutSequence.equals(other.mutSequence))
         return false;
      if (refSequence == null) {
         if (other.refSequence != null)
            return false;
      } else if (!refSequence.equals(other.refSequence))
         return false;
      if (startPos != other.startPos)
         return false;
      if (strand == null) {
         if (other.strand != null)
            return false;
      } else if (!strand.equals(other.strand))
         return false;
      return true;
   }

   @Override
   public String toString() {
      return String.format("%s %d %d %s/%s %s", chromosome, startPos,
            endPos, refSequence, mutSequence, strand);
   }

}
