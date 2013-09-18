package edu.yale.med.krauthammerlab.abfab.old.vep;

public class VEPResult {
   String existing;
   String protein;
   String refseq;
   String ccds;
   String AAChange;
   Double polyphen;
   Double sift;
   String hgnc;
   String domain;
   Integer cdnaPosition;
   Integer cdsPosition;
   Integer proteinPosition;
   Integer exonNumber;
   Integer intronNumber;
   String hgvs;

   public String getExisting() {
      return existing;
   }

   public void setExisting(String existing) {
      if (existing != null && !existing.equals("-")) {
         StringBuilder postex = new StringBuilder();
         String[] es = existing.split(",");
         for (String e : es) {
            if (e.startsWith("rs")) {
               postex.append(e);
               postex.append(", ");
            }
         }

         this.existing = (postex.length() >= 2) ? postex.substring(0, postex
               .length() - 2) : "";
      }
   }

   public Double getPolyphen() {
      return (polyphen == null) ? -1d : polyphen;
   }

   public void setPolyphen(Double polyphen) {
      if (polyphen != null) {
         this.polyphen = polyphen;
      }
   }

   public Integer getCDNAPosition() {
      return (cdnaPosition == null) ? -1 : cdnaPosition;
   }

   public void setCDNAPosition(Integer cdnaPosition) {
      this.cdnaPosition = cdnaPosition;
   }

   public Integer getCDSPosition() {
      return (cdsPosition == null) ? -1 : cdsPosition;
   }

   public void setCDSPosition(Integer cdsPosition) {
      this.cdsPosition = cdsPosition;
   }

   public Integer getProteinPosition() {
      return (proteinPosition == null) ? -1 : proteinPosition;
   }

   public void setProteinPosition(Integer proteinPosition) {
      this.proteinPosition = proteinPosition;
   }

   public Integer getIntronNumber() {
      return (intronNumber) == null ? -1 : intronNumber;
   }

   public void setIntronNumber(Integer intronNumber) {
      this.intronNumber = intronNumber;
   }

   public Integer getExonNumber() {
      return (exonNumber) == null ? -1 : exonNumber;
   }

   public void setExonNumber(Integer exonNumber) {
      this.exonNumber = exonNumber;
   }

   public Double getSift() {
      return (sift == null) ? -1d : sift;
   }

   public void setSift(Double sift) {
      if (sift != null) {
         this.sift = sift;
      }
   }

   public String getHgnc() {
      return hgnc;
   }

   public void setHgnc(String hgnc) {
      if (hgnc != null && !hgnc.equals("-")) {
         this.hgnc = hgnc;
      }
   }

   public String getDomain() {
      return domain;
   }

   public void setDomain(String domain) {
      if (domain != null && !domain.equals(("-"))) {
         this.domain = domain;
      }
   }

   public String getAAChange() {
      return (AAChange == null) ? "NA" : AAChange;
   }

   public void setAAChange(String AAChange) {
      this.AAChange = AAChange;
   }

   public String getProtein() {
      return protein;
   }

   public String getHGVS() {
	return hgvs;
}

public void setHGVS(String hgvs) {
	this.hgvs = hgvs;
}

public void setProtein(String protein) {
      if (protein != null && protein.startsWith("NP_")) {
         this.protein = protein;
      }
   }

   public String getRefseq() {
      return refseq;
   }

   public void setRefseq(String refseq) {
      if (refseq != null && refseq.startsWith("NM_")) {
         this.refseq = refseq;
      }
   }

   public String getCCDS() {
      return ccds;
   }

   public void setCCDS(String ccds) {
      if (ccds != null && ccds.startsWith("CCDS")) {
         this.ccds = ccds;
      }
   }

}
