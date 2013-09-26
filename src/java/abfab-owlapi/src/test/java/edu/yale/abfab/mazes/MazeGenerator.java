package edu.yale.abfab.mazes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MazeGenerator {

	class Maze {
		Node root;

		Maze(Node root) {
			this.root = root;
		}

		@Override
		public String toString() {
			return root.toString();
		}

	}

	class Node {
		String name;
		Set<Node> branches;

		Node(String name) {
			this.name = name;
			branches = new HashSet<>();
		}

		@Override
		public String toString() {
			Iterator<Node> branchIter = branches.iterator();
			StringBuilder sb = new StringBuilder();
			while (branchIter.hasNext()) {
				sb.append(branchIter.next().toString());
				if (branchIter.hasNext()) {
					sb.append(", ");
				}
			}
			String following = "";
			if (sb.length() > 0) {
				following = String.format(" -> (%s)", sb.toString());
			}
			return String.format("%s%s", name, following);
		}
	}

	public Maze createDAG(int poolSize) {
		Random random = new Random();
		Map<Integer, List<Node>> nodesLevels = new HashMap<>();
		int level = 1;
		Node root = null;
		List<Integer> pool = new ArrayList<Integer>();
		for (int i = 1; i <= poolSize; i++) {
			pool.add(i);
		}
		while (pool.size() > 0) {
			nodesLevels.put(level, new ArrayList<Node>());
			if (level == 1) {
				int r = random.nextInt(pool.size() - 1) + 1;
				root = new Node(String.valueOf(pool.get(r - 1)));
				pool.remove(r - 1);
				nodesLevels.get(level).add(root);
			} else {
				int prevLevel = level - 1;
				for (Node n : nodesLevels.get(prevLevel)) {
					if (pool.size() > 0) {
						int numBranchSize = Math.min(pool.size(), 3);
						int numBranches;
						if (numBranchSize == 1) {
							numBranches = 1;
						} else {
							numBranches = random.nextInt(numBranchSize - 1) + 1;
						}
						for (int x = 0; x < numBranches; x++) {
							int r;
							if (pool.size() == 1) {
								r = 1;
							} else {
								r = random.nextInt(pool.size() - 1) + 1;
							}
							int rx = pool.get(r - 1);
							Node nx = new Node(String.valueOf(rx));
							pool.remove(r - 1);
							nodesLevels.get(level).add(nx);
							n.branches.add(nx);
						}
					}
				}
			}

			level++;
		}

		return new Maze(root);
	}

	public static void main(String[] args) {
		MazeGenerator mg = new MazeGenerator();
		Maze m = mg.createDAG(10);
		System.out.println(m);
	}
}
