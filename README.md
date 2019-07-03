mutadelic
=========

**Update (2019)** - This project is the proof of concept from my Computational Biology dissertation (http://hdl.handle.net/10079/bibid/12365826).  As this was over five years ago, the program will almost certainly not work out of the box.  This is due largely to the fact that external resources required by the program have been moved, altered or lost.  If you are interested in the the program and how it might be brought back to life, please reach out to me.

## Description

Mutation Analysis using Description Logic inferencing capabilities.   Trippy.

There are two phases: (1) the staging phase and (2) the execution phase.  The staging phase builds an execution path base on an initial input (individual) and a desired output (class).  The path is built by inferences on a knowledge base of available Services whose input requirements and output capabilites are specified in an ontology.  The execution phase performs the path created in the staging phase.  Individual Services are called which add knowledge to the input (individual) until it satisfies the requirements of the desired output (class).
