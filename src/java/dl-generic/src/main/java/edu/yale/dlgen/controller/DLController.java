package edu.yale.dlgen.controller;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Set;

import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClass;
import edu.yale.dlgen.DLClassExpression;
import edu.yale.dlgen.DLDataPropertyExpression;
import edu.yale.dlgen.DLEntity;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.DLLiteral;
import edu.yale.dlgen.DLObjectPropertyExpression;
import edu.yale.dlgen.DLVisitor;

/**
 * @author matt
 *
 */
/**
 * @author matt
 * 
 */
public interface DLController {

	/**
	 * Add an axiom to the KB
	 * 
	 * @param axiom
	 *            - The DL Axiom to be added
	 */
	void addAxiom(DLAxiom<?> axiom);

	/**
	 * Add a {@link Set} of DL Axioms to the KB
	 * 
	 * @param axioms
	 *            - A {@link Set} of axioms to be added
	 */
	void addAxioms(Set<DLAxiom<?>> axioms);

	void removeAxiom(DLAxiom<?> axiom);

	/**
	 * Get the full {@link Collection} of DL Axioms from the KB
	 * 
	 * @return All axioms from the KB
	 */
	Collection<DLAxiom> getAxioms();

	/**
	 * Answer whether the knowledge base has the axiom
	 * 
	 * @param ax
	 *            The Axiom to check for
	 * @return Whether the axiom exists in the kb
	 */
	boolean containsAxiom(DLAxiom<?> ax);

	/**
	 * Removes the {@link Set} of axioms from the KB
	 * 
	 * @param axioms
	 *            Axioms to be removed from the KB
	 */
	void removeAxioms(Set<DLAxiom<?>> axioms);

	/**
	 * Write the ontology to a {@link File} specified in the #setOutputFile
	 * method (mostly for debugging purposes)
	 * 
	 * @throws IOException
	 *             if the writing of the file generates an exception
	 */
	void saveOntology() throws IOException;

	/**
	 * Set output file to be used when dumping the ontology to disk.
	 * 
	 * @param outputFile
	 *            - The {@link File} to be written to
	 */
	void setOutputFile(File outputFile);

	/**
	 * The the resource identifier (IRI) of an object in the KB
	 * 
	 * @param entity
	 *            Any DL object
	 * @return The IRI
	 */
	String getIRI(DLEntity<?> entity);

	/**
	 * Answer whether an axiom is entailed by the reasoner for the current KB
	 * 
	 * @param axiom
	 *            The axiom to be tested
	 * @return Whether the axiom is entailed by the reasoner
	 */
	boolean checkEntailed(DLAxiom<?> axiom);

	/**
	 * Ask the reasoner whether the KB is consistent as it currently stands.
	 * 
	 * @return Whether the KB is consistent
	 */
	boolean checkConsistency();

	/**
	 * Get all the Data Properties which have been assigned (filled) for this
	 * individual
	 * 
	 * @param individual
	 *            - The individual whose filled Data Properties we are
	 *            retrieving
	 * @return {@link Collection} of Data Properties filled for this Individual
	 */
	Collection<DLDataPropertyExpression> getDataProperties(
			DLIndividual<?> individual);

	/**
	 * Return the value of the data property for a given individual.
	 * 
	 * @param individual
	 *            The individual for whom the data property filler is to be
	 *            returned
	 * @param prop
	 *            The data property to find the value (filler) for
	 * @return The Literal value of the data property for the specified
	 *         individual
	 */
	Collection<DLLiteral> getDataPropertyValues(DLIndividual<?> individual,
			DLDataPropertyExpression<?> prop);

	/**
	 * Get all the object properties which been assigned (filled) for this
	 * individual
	 * 
	 * @param individual
	 *            The individual whose filled object properties we are
	 *            retrieving
	 * @return The object properties filled for this individual
	 */
	Collection<DLObjectPropertyExpression> getObjectProperties(
			DLIndividual<?> individual);

	/**
	 * Return the individual(s) which fill the object property for the given
	 * individual
	 * 
	 * @param individual
	 *            The individual for whom the object property filler is to be
	 *            returned
	 * @param prop
	 *            The object property to find the value (filler) for
	 * @return The individual(s) filling the object property for the specified
	 *         individual
	 */
	Collection<DLIndividual> getObjectPropertyValues(
			DLIndividual<?> individual, DLObjectPropertyExpression<?> prop);

	/**
	 * Return the asserted Class Expressions to which the Individual belongs
	 * 
	 * @param individual
	 *            The individual to get Types for
	 * @return The (asserted) types for an individual
	 */
	Collection<DLClassExpression> getTypes(DLIndividual<?> individual);

	/**
	 * Return the individuals which have a specified property filled by a
	 * specified value
	 * 
	 * @param type
	 *            The type of individual
	 * @param prop
	 *            The Object Property
	 * @param value
	 *            The individual value filling the object property
	 * @return The individuals with object property filled by the specified
	 *         individual
	 */
	Collection<DLIndividual> getHavingPropertyValue(DLClassExpression<?> type,
			DLObjectPropertyExpression<?> prop, DLIndividual<?> value);

	/**
	 * Return the asserted Individuals of the given type.
	 * 
	 * @param clz
	 *            The class to get instances of
	 * @return The individuals which are asserted to have the specified type.
	 */
	Collection<DLIndividual> getInstances(DLClassExpression<?> clz);

	/**
	 * Return the individuals asserted to be different from a given individual.
	 * 
	 * @param individual
	 *            The individual for whom we are finding different individuals
	 * @return The individuals asserted to be different
	 */
	Collection<DLIndividual> getDifferentIndividuals(DLIndividual<?> individual);

	/**
	 * Return the individuals asserted to be the same as a given individual.
	 * 
	 * @param individual
	 *            The individual for whom we are finding same individuals
	 * @return The individuals asserted to be the same
	 */
	Collection<DLIndividual> getSameIndividuals(DLIndividual<?> individual);

	/**
	 * Return the lexical (String) value of DL Literal. Later, it can be parsed
	 * as a double or int or whatever.
	 * 
	 * @param literal
	 *            The literal to get the value of
	 * @return The lexical value of the literal
	 */
	String getLiteralValue(DLLiteral<?> literal);
	
	DLLiteral<?> asLiteral(boolean val);

	/**
	 * Returns an individual with specified IRI cast as a {@link DLIndividual}
	 * 
	 * @param name
	 *            The IRI for the individual
	 * @return An individual with specified IRI
	 */
	DLIndividual<?> individual(String name);

	/**
	 * Returns a DL Class with the specified IRI
	 * 
	 * @param name
	 *            The IRI for the class
	 * @return A DL class with the specified IRI
	 */
	DLClass<?> clazz(String name);

	/**
	 * Return the complement (NOT) of the given class expression.
	 * 
	 * @param clz
	 *            The class we are taking the Complement of
	 * @return The Complement class
	 */
	DLClassExpression<?> notClass(DLClassExpression<?> clz);

	/**
	 * Return the conjunction (AND) of the given class expression.
	 * 
	 * @param clz
	 *            The classes to AND together
	 * @return The Conjunction class
	 */
	DLClassExpression<?> andClass(DLClassExpression<?>... clz);

	/**
	 * Returns a DL data property with the specified IRI
	 * 
	 * @param name
	 *            The IRI for the data property
	 * @return A DL Data property with the specified IRI
	 */
	DLDataPropertyExpression<?> dataProp(String name);

	/**
	 * Returns a DL object property with the specified IRI
	 * 
	 * @param name
	 *            The IRI for the object property
	 * @return A DL Object Property with the specified IRI
	 */
	DLObjectPropertyExpression<?> objectProp(String name);

	/**
	 * Returns an axiom that creates a new individual and assigns it to a type.
	 * 
	 * @param name
	 *            The IRI of the new Individual
	 * @param clz
	 *            The DL Class to which the Individual is typed
	 * @return The axiom
	 */
	DLAxiom<?> newIndividual(String name, DLClassExpression<?> clz);

	/**
	 * Returns an axiom that assign an individual to a type
	 * 
	 * @param individual
	 *            The individual to be typed
	 * @param clz
	 *            The DL class to assign the individual
	 * @return The axiom
	 */
	DLAxiom<?> individualType(DLIndividual<?> individual,
			DLClassExpression<?> clz);

	/**
	 * Returns an axiom asserting a fact about an individual via a data property
	 * 
	 * @param individual
	 *            The individual about whom a fact is being stated
	 * @param prop
	 *            The data property
	 * @param value
	 *            The literal value of the data property
	 * @return The axiom
	 */
	DLAxiom<?> newDataFact(DLIndividual<?> individual,
			DLDataPropertyExpression<?> prop, DLLiteral<?> value);

	/**
	 * Returns an axiom asserting a fact about an individual via an object
	 * property
	 * 
	 * @param individual
	 *            The individual about whom a fact is being stated
	 * @param prop
	 *            The object property
	 * @param value
	 *            The individual filling the object property
	 * @return
	 */
	DLAxiom<?> newObjectFact(DLIndividual<?> individual,
			DLObjectPropertyExpression<?> prop, DLIndividual<?> value);

	/**
	 * Return all subclasses including those inferred by the reasoner. This will
	 * not include the bottom concept (owl:Nothing).
	 * 
	 * @param clz
	 *            The DL class to get subclasses of
	 * @return All subclasses
	 */
	Collection<DLClassExpression> getSubclasses(DLClass<?> clz);

	/**
	 * Answer whether a given class is a subclass of another. Returns false if
	 * the two classes are the safme.
	 * 
	 * @param sub
	 *            The potential sub-class
	 * @param sup
	 *            The potential super-class
	 * @return Whether sub is indeed a subclass of sup
	 */
	boolean isSubclass(DLClassExpression<?> sub, DLClassExpression<?> sup);

	/**
	 * Return all equivalent classes including those inferred by the reasoner.
	 * 
	 * @param clz
	 *            The DL class to get equivalent classes from
	 * @return All equivalent classes
	 */
	Collection<DLClassExpression> getEquivalentClasses(DLClassExpression<?> clz);

	/**
	 * Add the specified Visitor to the {@link DLController}'s collection of
	 * visitors.
	 * 
	 * @param name
	 *            The name (key) for the visitor
	 * @param visitor
	 *            The visitor itself
	 */
	void registerVisitor(String name, DLVisitor<?> visitor);

	/**
	 * Return the visitor specified by name (key).
	 * 
	 * @param name
	 *            The name (key) of the visitor
	 * @return The visitor
	 */
	DLVisitor<?> getVisitor(String name);

	/**
	 * Using the visitor pattern, add the visitor to the specified
	 * {@link DLEntity}
	 * 
	 * @param visitor
	 *            The visitor
	 * @param entity
	 *            What to add the visitor to.
	 */
	void addVisitor(DLVisitor<?> visitor, DLEntity<?> entity);

	/**
	 * @param clz
	 *            The compound DL Class (ie Intersection and Union)
	 * @return
	 */
	Collection<DLEntity> getTerms(DLClassExpression<?> clz);

	/**
	 * Returns the class that acts as the filler in a restriction. E.g. what the
	 * X is in "some X".
	 * 
	 * @param clz
	 *            The class to get the filler for
	 * @return The DL class of the filler
	 */
	DLClassExpression<?> getQualification(DLClassExpression<?> clz);

	/**
	 * Returns whatever a wrapper for whatever represents DL top class (eg.
	 * Owl:thing).
	 * 
	 * @return The DL "top" Class
	 */
	DLClassExpression<?> thing();

	/**
	 * Read an ontology (or other collection of DL axioms) from an artifact
	 * (e.g. File, String, InputStream) represented by the {@link Reader}.
	 * 
	 * @param reader
	 *            Holder of the ontology artifact
	 * @return True if loaded without exception; false otherwise
	 */
	boolean load(Reader reader);

	/**
	 * Read an ontology (or other collection of DL axioms) from an artifact
	 * (e.g. File, String, InputStream) represented by the {@link Reader} and
	 * specified as the format.
	 * 
	 * @param reader
	 *            Holder of the ontology artificat
	 * @param type
	 *            Format of the ontology (e.g. OWL/XML, Manchester, etc.)
	 * @return True if loaded without exception; false otherwise
	 */
	boolean load(Reader reader, String type);
}
