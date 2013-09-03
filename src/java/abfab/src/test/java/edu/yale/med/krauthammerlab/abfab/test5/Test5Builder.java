package edu.yale.med.krauthammerlab.abfab.test5;

import static org.dyndns.norbrand.Utils.*;

import org.dyndns.norbrand.NS;
import org.dyndns.norbrand.NS.RDFS;
import org.dyndns.norbrand.NS.SIO;
import org.dyndns.norbrand.NS.SO;
import org.semanticweb.owlapi.model.OWLException;

import edu.yale.med.krauthammerlab.abfab.old.AbfabBuilder;

public class Test5Builder extends AbfabBuilder {

   @Override
   public void buildClasses() throws OWLException {
      super.buildClasses();
      
      clazz("RefseqSequence");
      subclass(
            c(NS.SIO, SIO.nucleic_acid_sequence),
            c("RefseqSequence"));
      
      clazz("CCDSSequence");
      subclass(
            c(NS.SIO, SIO.nucleic_acid_sequence),
            c("RefseqSequence"));
      
      /* 
       * Annotation equivalent_to 
       * SIO:description and 
       * (SIO:cites some SIO:information_content_entity)
       */
      clazz("Annotation");
      equiv(
              c("Annotation"), 
              and(
                      c(NS.SIO, SIO.description), 
                      some(
                              op(NS.SIO, SIO.cites), 
                              c(NS.SIO, SIO.information_content_entity))));

      /* 
       * CompletionStatus subclass_of SIO:information_content_entity 
       */
      clazz("CompletionStatus");
      subclass(
              c(NS.SIO, SIO.information_content_entity), 
              c("CompletionStatus"));

      /* 
       * DBSNPMutation subclass_of SO:sequence_variant  
       */
      clazz("DBSNPMutation");
      subclass(
              c(NS.SO, SO.sequence_variant), 
              c("DBSNPMutation"));
      
      clazz("AAChange");
      subclass(
              c(NS.SO, SO.sequence_variant), 
              c("AAChange"));

      /*
       *  PredictedSequence subclass_of SIO:model
       */
      clazz("PredictedSequence");
      subclass(
              c(NS.SIO, SIO.model), 
              c("PredictedSequence"));

      /*
       *  SiftValue subclass_of SIO:bioinformatics_data
       */
      clazz("SiftValue");
      subclass(
              c(NS.SIO, SIO.bioinformatic_data), 
              c("SiftValue"));
      
      clazz("PolyphenValue");
      subclass(
            c(NS.SIO, SIO.bioinformatic_data),
            c("PolyphenValue"));
      
      clazz("AlleleFrequency");
      subclass(
            c(NS.SIO, SIO.probability),
            c("AlleleFrequency"));
      
      clazz("CDNAPosition");
      subclass(
            c(NS.SIO, SIO.positional_value),
            c("CDNAPosition"));
      
      clazz("CDSPosition");
      subclass(
            c(NS.SIO, SIO.positional_value),
            c("CDSPosition"));
      
      clazz("ExonNumber");
      subclass(
            c(NS.SIO, SIO.ordinal_number),
            c("ExonNumber"));

      /*
       * Mutation subclass_of gelo:GenomicElement
       * Mutation subclass_of so:sequence_variant
       * Mutation equivalent_to 
       * (gelo:has_locus some 
       *   (gelo:on_chromosome some so:chromosome) and
       *   (gelo:locus_start min 1) and
       *   (gelo:locus_end min 1) and
       *   (gelo:strand min 1) and
       *   (gelo:sequence min 1)) and
       * (sio:is_modelled_by some
       *   PredictedSequence and
       *   (sio:has_value min 1))
       */
      clazz("Mutation");
      subclass(
              c(NS.GELO, "GenomicElement"), 
              c("Mutation"));
      subclass(
              c(NS.SO, SO.sequence_variant), 
              c("Mutation"));
      equiv(
              c("Mutation"), 
              and(
                      some(
                              op(NS.GELO, "has_locus"), 
                              and(
                                      some(
                                              op(NS.GELO, "on_chromosome"), 
                                              c(NS.SO, SO.chromosome)), 
                                      at_least(
                                              1, 
                                              dp(NS.GELO, "locus_start")), 
                                      at_least(
                                              1, 
                                              dp(NS.GELO, "locus_end")), 
                                      at_least(
                                              1, 
                                              dp(NS.GELO, "strand")), 
                                      at_least(
                                              1, 
                                              dp(NS.GELO, "sequence")))), 
                      some(
                              op(NS.SIO, SIO.is_modelled_by),
                              at_least(1, dp(NS.SIO, SIO.has_value)))));

      /*
       * GeneAnnotatedMutation equivalent_to
       * Mutation and 
       * (sio:is_described_by some
       *   (Annotation and
       *     (sio:refers_to some so:gene)))
       */
      clazz("GeneAnnotatedMutation");
      equiv(
              c("GeneAnnotatedMutation"), 
              and(
                      c("Mutation"), 
                      some(
                              op(NS.SIO, SIO.is_described_by), 
                              and(
                                      c("Annotation"), 
                                      some(
                                              op(NS.SIO, SIO.refers_to), 
                                              c(NS.SO, SO.gene))))));
      
      /*
       * ProteinAnnotated Mutation equivalent_to
       * Mutation and 
       * (sio:is_described_by some
       *    (Annotation and
       *        (sio:refers_to some sio:protein)))
       */
      clazz("ProteinAnnotatedMutation");
      equiv(
            c("ProteinAnnotatedMutation"),
            and(
                  c("Mutation"),
                  some(
                        op(NS.SIO, SIO.is_described_by),
                        and(
                              c("Annotation"),
                              some(
                                    op(NS.SIO, SIO.refers_to),
                                    c(NS.SIO, SIO.protein))))));
      
      clazz("RefseqAnnotatedMutation");
      equiv(
            c("RefseqAnnotatedMutation"),
            and(
                  c("Mutation"),
                  some(
                        op(NS.SIO, SIO.is_described_by),
                        and(
                              c("Annotation"),
                              some(
                                    op(NS.SIO, SIO.refers_to),
                                    c("RefseqSequence"))))));
      
      clazz("CCDSAnnotatedMutation");
      equiv(
            c("CCDSAnnotatedMutation"),
            and(
                  c("Mutation"),
                  some(
                        op(NS.SIO, SIO.is_described_by),
                        and(
                              c("Annotation"),
                              some(
                                    op(NS.SIO, SIO.refers_to),
                                    c("CCDSSequence"))))));

      /*
       * SNPAnnotatedMutation equivalent_to
       * Mutation and 
       * (sio:is_described_by some
       *   (Annotation and
       *     (sio:refers_to some DBSNPMutation)))
       */
      clazz("SNPAnnotatedMutation");
      equiv(
              c("SNPAnnotatedMutation"), 
              and(
                      c("Mutation"), 
                      some(
                              op(NS.SIO, SIO.is_described_by), 
                              and(
                                      c("Annotation"), 
                                      some(
                                              op(NS.SIO, SIO.refers_to), 
                                              c("DBSNPMutation"))))));

      /*
       * SiftValueAnnotatedMutation equivalent_to
       * Mutation and
       * (sio:is_described_by some
       *   (Annotation and
       *     (sio:refers_to some SiftValue) and
       *     (sio:has_value min 1)))
       */
      clazz("SiftValueAnnotatedMutation");
      equiv(
              c("SiftValueAnnotatedMutation"), 
              and(
                      c("Mutation"), 
                      some(
                              op(NS.SIO, SIO.is_described_by), 
                              and(
                                      c("Annotation"), 
                                      some(
                                              op(NS.SIO,SIO.refers_to), 
                                              c("SiftValue")), 
                                      at_least(
                                              1, 
                                              dp(NS.SIO, SIO.has_value))))));
      
      clazz("PolyphenValueAnnotatedMutation");
      equiv(
              c("PolyphenValueAnnotatedMutation"), 
              and(
                      c("Mutation"), 
                      some(
                              op(NS.SIO, SIO.is_described_by), 
                              and(
                                      c("Annotation"), 
                                      some(
                                              op(NS.SIO,SIO.refers_to), 
                                              c("PolyphenValue")), 
                                      at_least(
                                              1, 
                                              dp(NS.SIO, SIO.has_value))))));
      
      clazz("AlleleFrequencyAnnotatedMutation");
      equiv(
              c("AlleleFrequencyAnnotatedMutation"), 
              and(
                      c("Mutation"), 
                      some(
                              op(NS.SIO, SIO.is_described_by), 
                              and(
                                      c("Annotation"), 
                                      some(
                                              op(NS.SIO,SIO.refers_to), 
                                              c("AlleleFrequency")), 
                                      at_least(
                                              1, 
                                              dp(NS.SIO, SIO.has_value))))));
      
      clazz("CDNAPositionAnnotatedMutation");
      equiv(
              c("CDNAPositionAnnotatedMutation"), 
              and(
                      c("Mutation"), 
                      some(
                              op(NS.SIO, SIO.is_described_by), 
                              and(
                                      c("Annotation"), 
                                      some(
                                              op(NS.SIO,SIO.refers_to), 
                                              c("CDNAPosition")), 
                                      at_least(
                                              1, 
                                              dp(NS.SIO, SIO.has_value))))));
      
      clazz("CDSPositionAnnotatedMutation");
      equiv(
              c("CDSPositionAnnotatedMutation"), 
              and(
                      c("Mutation"), 
                      some(
                              op(NS.SIO, SIO.is_described_by), 
                              and(
                                      c("Annotation"), 
                                      some(
                                              op(NS.SIO,SIO.refers_to), 
                                              c("CDSPosition")), 
                                      at_least(
                                              1, 
                                              dp(NS.SIO, SIO.has_value))))));
      
      clazz("ExonNumberAnnotatedMutation");
      equiv(
              c("ExonNumberAnnotatedMutation"), 
              and(
                      c("Mutation"), 
                      some(
                              op(NS.SIO, SIO.is_described_by), 
                              and(
                                      c("Annotation"), 
                                      some(
                                              op(NS.SIO,SIO.refers_to), 
                                              c("ExonNumber")), 
                                      at_least(
                                              1, 
                                              dp(NS.SIO, SIO.has_value))))));
      
      clazz("AAChangeAnnotatedMutation");
      equiv(
              c("AAChangeAnnotatedMutation"), 
              and(
                      c("Mutation"), 
                      some(
                              op(NS.SIO, SIO.is_described_by), 
                              and(
                                      c("Annotation"), 
                                      some(
                                              op(NS.SIO,SIO.refers_to), 
                                              c("AAChange")), 
                                      at_least(
                                              1, 
                                              dp(NS.SIO, SIO.has_value))))));

      /*
       * FullyAnnotatedMutation equivalent_to
       * GeneAnnotatedMutation and 
       * SNPAnnotatedMutation and
       * SiftValueAnnotatedMutation
       */
      clazz("FullyAnnotatedMutation");
      equiv(
            c("FullyAnnotatedMutation"), 
            and(
                  c("GeneAnnotatedMutation"),
                  c("SNPAnnotatedMutation"),
                  c("ProteinAnnotatedMutation"),
                  c("RefseqAnnotatedMutation"),
                  c("CCDSAnnotatedMutation"),
                  c("CDNAPositionAnnotatedMutation"),
                  c("CDSPositionAnnotatedMutation"),
                  c("ExonNumberAnnotatedMutation"),
                  c("AAChangeAnnotatedMutation"),
                  c("SiftValueAnnotatedMutation"),
                  c("PolyphenValueAnnotatedMutation"),
                  c("AlleleFrequencyAnnotatedMutation")));

      /*
       * FinishedMutation equivalent_to
       * Mutation and
       * (sio:is_described_by some
       *   (Annotation and
       *     (sio:refers_to some CompletionStatus) and
       *     (sio:has_value min 1)))
       */
      clazz("FinishedMutation");
      equiv(
            c("FinishedMutation"), 
            and(
                  c("Mutation"), 
                  some(
                        op(NS.SIO, SIO.is_described_by), 
                        and(
                              c("Annotation"), 
                              some(
                                    op(NS.SIO,SIO.refers_to), 
                                    c("CompletionStatus")), 
                              at_least(
                                    1, 
                                    dp(NS.SIO, SIO.has_value))))));

      /*
       * FinishedAnnotationMutationService equivalent_to
       * Service and
       * (hasInputClass some FullyAnnotatedMutation) and
       * (hasOutputClass some FinishedMutation)
       */
      clazz("FinishedAnnotationMutationService");
      equiv(
            c("FinishedAnnotationMutationService"), 
            and(
                  c("Service"), 
                  some(
                        op("hasInputClass"), 
                        c("FullyAnnotatedMutation")), 
                  some(
                        op("hasOutputClass"), 
                        c("FinishedMutation"))));

      /*
       * GeneAnnotatedMutationService equivalent_to
       * Service and
       * (hasInputClass some Mutation) and
       * (hasOutputClass some GeneAnnotatedMutation)
       */
      clazz("GeneAnnotatedMutationService");
      equiv(
            c("GeneAnnotatedMutationService"), 
            and(
                  c("Service"), 
                  some(
                        op("hasInputClass"), 
                        c("Mutation")), 
                  some(
                        op("hasOutputClass"),
                        c("GeneAnnotatedMutation"))));
      
      clazz("ProteinAnnotatedMutationService");
      equiv(
            c("ProteinAnnotatedMutationService"),
            and(
                  c("Service"),
                  some(
                        op("hasInputClass"),
                        c("Mutation")),
                  some(
                        op("hasOutputClass"),
                        c("ProteinAnnotatedMutation"))));
      
      clazz("RefseqAnnotatedMutationService");
      equiv(
            c("RefseqAnnotatedMutationService"),
            and(
                  c("Service"),
                  some(
                        op("hasInputClass"),
                        c("Mutation")),
                  some(
                        op("hasOutputClass"),
                        c("RefseqAnnotatedMutation"))));
      
      clazz("CCDSAnnotatedMutationService");
      equiv(
            c("CCDSAnnotatedMutationService"),
            and(
                  c("Service"),
                  some(
                        op("hasInputClass"),
                        c("Mutation")),
                  some(
                        op("hasOutputClass"),
                        c("CCDSAnnotatedMutation"))));
      
      clazz("CDNAPositionAnnotatedMutationService");
      equiv(
            c("CDNAPositionAnnotatedMutationService"),
            and(
                  c("Service"),
                  some(
                        op("hasInputClass"),
                        c("Mutation")),
                  some(
                        op("hasOutputClass"),
                        c("CDNAPositionAnnotatedMutation"))));

      clazz("CDSPositionAnnotatedMutationService");
      equiv(
            c("CDSPositionAnnotatedMutationService"),
            and(
                  c("Service"),
                  some(
                        op("hasInputClass"),
                        c("Mutation")),
                  some(
                        op("hasOutputClass"),
                        c("CDSPositionAnnotatedMutation"))));
      
      clazz("ExonNumberAnnotatedMutationService");
      equiv(
            c("ExonNumberAnnotatedMutationService"),
            and(
                  c("Service"),
                  some(
                        op("hasInputClass"),
                        c("Mutation")),
                  some(
                        op("hasOutputClass"),
                        c("ExonNumberAnnotatedMutation"))));
      
      clazz("AAChangeAnnotatedMutationService");
      equiv(
            c("AAChangeAnnotatedMutationService"),
            and(
                  c("Service"),
                  some(
                        op("hasInputClass"),
                        c("Mutation")),
                  some(
                        op("hasOutputClass"),
                        c("AAChangeAnnotatedMutation"))));
      
      /*
       * SNPAnnotationMutationService equivalent_to
       * Service and
       * (hasInputClass some Mutation) and
       * (hasOutputClass some SNPAnnotatedMutation)
       */
      clazz("SNPAnnotatedMutationService");
      equiv(
            c("SNPAnnotatedMutationService"), 
            and(
                  c("Service"), 
                  some(
                        op("hasInputClass"), 
                        c("Mutation")), 
                  some(
                        op("hasOutputClass"),
                        c("SNPAnnotatedMutation"))));

      /*
       * SiftValueAnnotatedMutationService equivalent_to
       * Service and
       * (hasInputClass some Mutation) and
       * (hasOutputClass some SiftValueAnnotatedMutation)
       */
      clazz("SiftValueAnnotatedMutationService");
      equiv(
            c("SiftValueAnnotatedMutationService"), 
            and(
                  c("Service"), 
                  some(
                        op("hasInputClass"), 
                        c("Mutation")), 
                   some(
                         op("hasOutputClass"),
                         c("SiftValueAnnotatedMutation"))));
      
      clazz("AlleleFrequencyAnnotatedMutationService");
      equiv(
            c("AlleleFrequencyAnnotatedMutationService"), 
            and(
                  c("Service"), 
                  some(
                        op("hasInputClass"), 
                        c("Mutation")), 
                   some(
                         op("hasOutputClass"),
                         c("AlleleFrequencyAnnotatedMutation"))));
      
      clazz("PolyphenValueAnnotatedMutationService");
      equiv(
            c("PolyphenValueAnnotatedMutationService"), 
            and(
                  c("Service"), 
                  some(
                        op("hasInputClass"), 
                        c("Mutation")), 
                   some(
                         op("hasOutputClass"),
                         c("PolyphenValueAnnotatedMutation"))));
      
      annot(c(NS.SIO, SIO.probability), RDFS.label, "probability");
      annot(c(NS.SO, SO.sequence_variant), RDFS.label, "sequence variant");
      annot(c(NS.SO, SO.gene), RDFS.label, "gene");
      annot(c(NS.SO, SO.chromosome), RDFS.label, "chromosome");
      annot(c(NS.SIO, SIO.protein), RDFS.label, "protein");
      annot(c(NS.SIO, SIO.positional_value), RDFS.label, "positional value");
      annot(c(NS.SIO, SIO.ordinal_number), RDFS.label, "ordinal number");
      annot(c(NS.SIO, SIO.model), RDFS.label, "description");
      annot(c(NS.SIO, SIO.description), RDFS.label, "description");
      annot(c(NS.SIO, SIO.bioinformatic_data), RDFS.label, "bioinformatic data");
      annot(c(NS.SIO, SIO.nucleic_acid_sequence), RDFS.label, "nucleic acid sequence");
      annot(c(NS.SIO, SIO.information_content_entity), RDFS.label, "information content entity");
      annot(dp(NS.SIO, SIO.has_value), RDFS.label, "has value");
      annot(op(NS.SIO, SIO.is_modelled_by), RDFS.label, "is modelled by");
      annot(op(NS.SIO, SIO.refers_to), RDFS.label, "refers to");
      annot(op(NS.SIO, SIO.is_described_by), RDFS.label, "is described by");
      annot(op(NS.SIO, SIO.cites), RDFS.label, "cites");
   }

}
