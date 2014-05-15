package edu.yale.abfab.mazes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.yale.abfab.mazes.MazeGenerator.Branch;
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

public class MazeTransformer2 {

	private DLController dl;

	public MazeTransformer2() {
		dl = new HermitDLController();
	}

	public Set<DLAxiom<?>> transform(Maze m) {
		Set<DLAxiom<?>> ax = new HashSet<>();
		DLClassExpression<?> service = dl.clazz(NS + "Service");
		DLObjectPropertyExpression<?> hasInput = dl
				.objectProp(NS + "has_input");
		DLObjectPropertyExpression<?> hasOutput = dl.objectProp(NS
				+ "has_output");
		DLDataPropertyExpression<?> hasCost = dl.dataProp(NS + "has_cost");
		DLDataPropertyExpression<?> hasExecutable = dl.dataProp(NS
				+ "has_executable");

		for (Node n : m.getAllNodes().values()) {
			DLClassExpression<?> typeInput = dl.clazz(n.getName() + "Input");
			DLClassExpression<?> typeOutput = dl.clazz(n.getName() + "Output");
			if (n.getParent() != null) {
				String parentName = n.getParent().getName();
				if (n instanceof Branch) {
					Branch b = (Branch) n;
					List<Maze> forks = b.getForks();
					Set<DLClassExpression<?>> branchOutputs = new HashSet<>();
					for (Maze f : forks) {
						String fd = f.dump();
						Set<DLAxiom<?>> tf = transform(f);
						ax.addAll(tf);
						DLClassExpression<?> fRootInput = dl.clazz(f.getRoot()
								.getName() + "Input");
						DLClassExpression<?> parentOutput = dl.clazz(parentName
								+ "Output");
						DLClassExpression<?> branchOutput = dl.clazz(String
								.format("%s-%dOutput", n.getName(),
										f.getCurrBranch()));
						ax.add(dl.equiv(fRootInput, parentOutput));
						branchOutputs.add(branchOutput);
					}
					DLClassExpression<?> intersectionOfBranchOutputs = dl
							.andClass(new ArrayList<DLClassExpression<?>>(
									branchOutputs)
									.toArray(new DLClassExpression<?>[branchOutputs
											.size()]));
					ax.add(dl.equiv(typeInput, intersectionOfBranchOutputs));
				} else {
					ax.add(dl.equiv(typeInput, dl.clazz(parentName + "Output")));
				}
			}

			DLClassExpression<?> typeService = dl
					.clazz(n.getName());
			ax.add(dl.subClass(typeService, service));
			ax.add(dl.equiv(
					typeService,
					dl.andClass(dl.some(hasInput, typeInput),
							dl.some(hasOutput, typeOutput),
							dl.value(hasCost, dl.asLiteral(1.0)),
							dl.value(hasExecutable, dl.asLiteral("exec")))));
		}
		return ax;
	}
}
