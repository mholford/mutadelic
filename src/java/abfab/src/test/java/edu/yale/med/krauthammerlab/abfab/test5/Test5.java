package edu.yale.med.krauthammerlab.abfab.test5;

import static org.dyndns.norbrand.Utils.*;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dyndns.norbrand.NS;
import org.dyndns.norbrand.OntologyBuilder;
import org.dyndns.norbrand.NS.SIO;
import org.dyndns.norbrand.NS.SO;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualVisitor;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import edu.yale.med.krauthammerlab.abfab.old.Abductor;

public class Test5 {

   private static OntologyBuilder ob;
   private static Set<OWLAxiom> axioms;
   private static OWLIndividual result;
   private static OWLOntology ont;
   private static String savePoint;
   private static String pkg;
   private boolean init = false;

   @BeforeClass
   public static void onlyOnce() {
      ob = OntologyBuilder.instance(true);
      pkg = Test5.class.getPackage().getName();
      String saveFolder = pkg.substring(pkg.lastIndexOf(".") + 1);
      savePoint = String.format("file:/home/matt/sw/abfab-%s/abfab-save.owl",
            saveFolder);
      try {
         ob.init(new Test5Builder(), "http://krauthammerlab.med.yale.edu",
               String.format("file:/home/matt/sw/abfab-%s/abfab.owl",
                     saveFolder), String.format(
                     "file:/home/matt/sw/abfab-%s/abfab-inf.owl", saveFolder));
      } catch (Exception e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      ont = ob.getOntology();

      axioms = null;
      try {
         OWLIndividual anonLoc = i("loc1");
         OWLIndividual anonPred = i("ps1");
         axioms = new HashSet<OWLAxiom>();

         axioms.add(_indiv("mut1", thing()));
         axioms.add(_indiv("Chr1", c(NS.SO, SO.chromosome)));
         axioms.add(_indiv("VEP", c(NS.SIO, SIO.information_content_entity)));
         axioms.add(_indiv("1000Genome",c(NS.SIO, SIO.information_content_entity)));
         axioms.add(_indiv(anonPred, c("PredictedSequence")));
         axioms.add(_indiv(anonLoc, c(NS.GELO, "GenomicLocus")));
         axioms.add(_dp_value(anonLoc, dp(NS.GELO, "strand"), "+"));
         axioms.add(_dp_value(anonLoc, dp(NS.GELO, "locus_start"), 158612236));
         axioms.add(_dp_value(anonLoc, dp(NS.GELO, "locus_end"), 158612236));
         axioms.add(_dp_value(anonLoc, dp(NS.GELO, "sequence"), "G"));
         axioms
               .add(_op_value(anonLoc, op(NS.GELO, "on_chromosome"), i("Chr1")));
         axioms.add(_dp_value(anonPred, dp(NS.SIO, SIO.has_value), "A"));
         axioms.add(_op_value(i("mut1"), op(NS.GELO, "has_locus"), anonLoc));
         axioms.add(_op_value(i("mut1"), op(NS.SIO, SIO.is_modelled_by),
               anonPred));

         ob.getManager().addAxioms(ob.getOntology(), axioms);

         result = new Abductor(pkg).abduce(i("mut1"), c("FinishedMutation"), false);
         ob.save(savePoint);

      } catch (Exception e) {
         e.printStackTrace();
         fail();
      }
   }

   @Before
   public void setUp() throws Exception {

   }

   @Test
   public void testDummy() {
      /*
       * Ie ran without crapping out.
       */
      assertEquals(1, 1);
   }

   class PropertyFinder implements OWLIndividualVisitor {
      Set<OWLObject> output;
      private OWLProperty prop;
      private OWLClassExpression typeCheck;
      private OWLClassExpression enclosingTypeCheck;
      private OWLOntology ont;
      private OntologyBuilder ob;
      private OWLReasoner reasoner;

      PropertyFinder(OWLProperty prop, OWLClassExpression typeCheck,
            OWLClassExpression enclosingTypeCheck, Set<OWLObject> output,
            OntologyBuilder ob) {
         this.prop = prop;
         this.typeCheck = typeCheck;
         this.enclosingTypeCheck = enclosingTypeCheck;
         this.output = output;
         this.ob = ob;
         ont = ob.getOntology();
         reasoner = ob.getReasoner();
      }

      @Override
      public void visit(OWLNamedIndividual individual) {
         doIndividual(individual);
      }

      @Override
      public void visit(OWLAnonymousIndividual individual) {
         doIndividual(individual);
      }

      private boolean checkInstance(OWLIndividual i, OWLClassExpression ce) {
         // return reasoner.getInstances(ce, false).getFlattened().contains(i);
         return ob.checkEntailed(_indiv(i, ce));
      }

      private void doIndividual(OWLIndividual i) {
         boolean encOK = (enclosingTypeCheck == null)
               || (checkInstance(i, enclosingTypeCheck));
         Map<OWLObjectPropertyExpression, Set<OWLIndividual>> ipropMap = i
               .getObjectPropertyValues(ont);
         for (OWLObjectPropertyExpression p : ipropMap.keySet()) {
            if (encOK && p.equals(prop)) {
               for (OWLIndividual ind : ipropMap.get(p)) {
                  if (checkInstance(ind, typeCheck)) {
                     output.add(ind);
                  }
               }
            } else {
               for (OWLIndividual ind : ipropMap.get(p)) {
                  ind.accept(this);
               }
            }
         }

         Map<OWLDataPropertyExpression, Set<OWLLiteral>> dpropMap = i
               .getDataPropertyValues(ont);
         for (OWLDataPropertyExpression d : dpropMap.keySet()) {
            if (encOK && d.equals(prop)) {
               for (OWLLiteral ind : dpropMap.get(d)) {
                  output.add(ind);
               }
            }
         }
      }
   }

   private Object getActual(OWLProperty oprop, OWLClassExpression type) {
      Set<OWLObject> objs = new HashSet<OWLObject>();
      PropertyFinder pf = new PropertyFinder(oprop, type, null, objs, ob);
      result.accept(pf);
      if (objs.size() != 1) {
         _fail("Wrong number found: " + objs.size());
      }
      OWLNamedIndividual i = (OWLNamedIndividual) objs.iterator().next();
      IRI iiri = i.getIRI();
      String actual = iiri.getFragment();
      if (actual == null) {
         String istring = iiri.toString();
         actual = istring.substring(istring.lastIndexOf(':') + 1);
      }
      return actual;
   }

   private Object getActualValue(OWLObjectPropertyExpression oprop,
         OWLClassExpression type) {
      Set<OWLObject> vals = new HashSet<OWLObject>();
      PropertyFinder pf = new PropertyFinder(dp(NS.SIO, SIO.has_value), null,
            some(oprop, type), vals, ob);
      result.accept(pf);
      if (vals.size() != 1) {
         _fail("Wrong number of values found: " + vals.size());
      }
      OWLLiteral lit = (OWLLiteral) vals.iterator().next();
      if (lit.isBoolean()) {
         return lit.parseBoolean();
      } else if (lit.isDouble()) {
         return lit.parseDouble();
      } else if (lit.isFloat()) {
         return lit.parseFloat();
      } else if (lit.isInteger()) {
         return lit.parseInteger();
      } else {
         return lit.getLiteral();
      }
   }

   @Test
   public void testGene() {
      String expectedGene = "SPTA1";
      Object actual = getActual(op(NS.SIO, SIO.refers_to), c(NS.SO, SO.gene));
      assertEquals(expectedGene, actual);
   }

   @Test
   public void testProtein() {
      String expectedProtein = "NP_003117.2";
      Object actual = getActual(op(NS.SIO, SIO.refers_to), c(NS.SIO,
            SIO.protein));
      assertEquals(expectedProtein, actual);
   }

   @Test
   public void testSNP() {
      String expectedSNP = "NO_SNP";
      Object actual = getActual(op(NS.SIO, SIO.refers_to), c("DBSNPMutation"));
      assertEquals(actual, expectedSNP);
   }

   @Test
   public void testRefseq() {
      String expectedRefseq = "NM_003126.2";
      Object actual = getActual(op(NS.SIO, SIO.refers_to), c("RefseqSequence"));
      assertEquals(expectedRefseq, actual);
   }

   @Test
   public void testCCDS() {
      String expectedCCDS = "CCDS41423.1";
      Object actual = getActual(op(NS.SIO, SIO.refers_to), c("CCDSSequence"));
      assertEquals(expectedCCDS, actual);
   }

   @Test
   public void testCompletionStatus() {
      assertTrue((Boolean) getActualValue(op(NS.SIO, SIO.refers_to),
            c("CompletionStatus")));
   }

   @Test
   public void testCDNAPosition() {
      assertEquals(4901, getActualValue(op(NS.SIO, SIO.refers_to),
            c("CDNAPosition")));
   }

   @Test
   public void testCDSPosition() {
      assertEquals(4702, getActualValue(op(NS.SIO, SIO.refers_to),
            c("CDSPosition")));
   }

   @Test
   public void testExonNumber() {
      assertEquals(33, getActualValue(op(NS.SIO, SIO.refers_to),
            c("ExonNumber")));
   }

   @Test
   public void testAAChange() {
      assertEquals("C/R", getActualValue(op(NS.SIO, SIO.refers_to),
            c("AAChange")));
   }

   @Test
   public void testSiftValue() {
      assertEquals(1d,
            getActualValue(op(NS.SIO, SIO.refers_to), c("SiftValue")));
   }
   
   @Test
   public void testAlleleFrequency() {
      assertEquals(0.6229,
            getActualValue(op(NS.SIO, SIO.refers_to), c("AlleleFrequency")));
   }

   @Test
   public void testPolyphenValue() {
      assertEquals(0d, getActualValue(op(NS.SIO, SIO.refers_to),
            c("PolyphenValue")));
   }

   @AfterClass
   public static void fullTearDown() throws Exception {
      ob.getManager().removeAxioms(ob.getOntology(), axioms);
      ob.getManager().removeOntology(ont);
   }

   private void _fail(String f) {
      try {
         ob.save(savePoint);
      } catch (OWLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      fail(f);
   }

}
