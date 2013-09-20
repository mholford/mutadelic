mutadelic
=========

Mutation Analysis using Description Logic inferencing capabilities.   Trippy.

There are two phases: (1) the staging phase and (2) the execution phase.  The staging phase builds an execution path base on an initial input (individual) and a desired output (class).  The path is built by inferences on a knowledge base of available Services whose input requirements and output capabilites are specified in an ontology.  The execution phase performs the path created in the staging phase.  Individual Services are called which add knowledge to the input (individual) until it satisfies the requirements of the desired output (class).
