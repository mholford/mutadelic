package edu.yale.abfab.mazes;

import java.util.HashSet;
import java.util.Set;

import edu.yale.abfab.mazes.MazeGenerator.Maze;
import edu.yale.abfab.mazes.MazeGenerator.Node;
import edu.yale.dlgen.DLAxiom;
import edu.yale.dlgen.DLClassExpression;
import edu.yale.dlgen.DLDataPropertyExpression;
import edu.yale.dlgen.DLIndividual;
import edu.yale.dlgen.DLObjectPropertyExpression;
import edu.yale.dlgen.controller.DLController;
import edu.yale.dlgen.controller.HermitDLController;
import static edu.yale.abfab.NS.*;

public class MazeTransformer {

	private DLController dl;

	public MazeTransformer() {
		dl = new HermitDLController();
	}

	public Set<DLAxiom<?>> transform(Maze m) {
		Set<DLAxiom<?>> ax = new HashSet<>();
		DLClassExpression<?> service = dl.clazz(NS + "Service");
		DLObjectPropertyExpression<?> hasInput = dl.objectProp(NS
				+ "has_input");
		DLObjectPropertyExpression<?> hasOutput = dl.objectProp(NS
				+ "has_output");
		DLDataPropertyExpression<?> hasCost = dl.dataProp(NS + "has_cost");
		DLDataPropertyExpression<?> hasExecutable = dl.dataProp(NS
				+ "has_executable");
		ax.add(dl.equiv(
				service,
				dl.andClass(dl.some(hasInput, dl.thing()),
						dl.some(hasOutput, dl.thing()))));

		for (Node n : m.getAllNodes().values()) {
			DLClassExpression<?> typeInput = dl.clazz(String.format(
					"%sType%sInput", NS, n.getName()));
			DLClassExpression<?> typeOutput = dl.clazz(String.format(
					"%sType%sOutput", NS, n.getName()));
			if (n.getParent() != null) {
				String parentName = n.getParent().getName();
				ax.add(dl.equiv(typeInput, dl.clazz(String.format(
						"%sType%sOutput", NS, parentName))));
			}

			DLClassExpression<?> typeService = dl.clazz(String.format(
					"%sType%sService", NS, n.getName()));
			ax.add(dl.subClass(typeService, service));
			ax.add(dl.equiv(
					typeService,
					dl.andClass(dl.some(hasInput, typeInput),
							dl.some(hasOutput, typeOutput))));

			DLIndividual<?> tii = dl.individual(String.format("%sT%sI", NS,
					n.getName()));
			DLIndividual<?> toi = dl.individual(String.format("%sT%sO", NS,
					n.getName()));
			DLIndividual<?> tsi = dl.individual(String.format("%sT%sS", NS,
					n.getName()));
			ax.add(dl.individualType(tii, typeInput));
			ax.add(dl.individualType(toi, typeOutput));
			ax.add(dl.individualType(tsi, typeService));
			ax.add(dl.newObjectFact(tsi, hasInput, tii));
			ax.add(dl.newObjectFact(tsi, hasOutput, toi));
			ax.add(dl.newDataFact(tsi, hasCost, dl.asLiteral(1.0)));
			ax.add(dl.newDataFact(tsi, hasExecutable, dl.asLiteral("exec")));
		}
		return ax;
	}
}
