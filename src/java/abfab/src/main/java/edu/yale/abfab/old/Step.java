package edu.yale.med.krauthammerlab.abfab.old;

public abstract class Step {

   private double cost;

   public abstract Object get();

   public double getCost() {
      return cost;
   }

   public void setCost(double cost) {
      this.cost = cost;
   }
}
