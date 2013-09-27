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

	public class Maze {
		private Node root;
		private Map<String, Node> allNodes;

		public Maze(Node root) {
			this.root = root;
			allNodes = new HashMap<>();
			addNode(root);
		}
		
		public void addNode(Node node){
			allNodes.put(node.name, node);
		}
		
		public Node getNode(Node node) {
			return allNodes.get(node.name);
		}

		public Node getRoot() {
			return root;
		}

		public void setRoot(Node root) {
			this.root = root;
		}

		public Map<String, Node> getAllNodes() {
			return allNodes;
		}

		public void setAllNodes(Map<String, Node> allNodes) {
			this.allNodes = allNodes;
		}

		@Override
		public String toString() {
			return root.toString();
		}
		
		public String dump() {
			return root.dump();
		}

	}

	public class Node {
		private String name;
		private Set<Node> branches;
		private Node parent;
		
		public Node(String name){
			this(name, null);
		}

		public Node(String name, Node parent) {
			this.name = name;
			this.parent = parent;
			branches = new HashSet<>();
		}
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Set<Node> getBranches() {
			return branches;
		}

		public void setBranches(Set<Node> branches) {
			this.branches = branches;
		}

		public Node getParent() {
			return parent;
		}

		public void setParent(Node parent) {
			this.parent = parent;
		}

		@Override
		public String toString() {
			return name;
		}

		public String dump() {
			Iterator<Node> branchIter = branches.iterator();
			StringBuilder sb = new StringBuilder();
			while (branchIter.hasNext()) {
				sb.append(branchIter.next().dump());
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Node other = (Node) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		private MazeGenerator getOuterType() {
			return MazeGenerator.this;
		}
	}

	class Solver {
		List<Node> checkedNodes;
		List<Node> path;
		Node goal;
		public List<Node> solve(Maze m, Node goal) {
			Node p = m.getNode(goal);
			path = new ArrayList<>();
			while (p != m.root) {
				path.add(0, p);
				p = p.parent;
			}
			path.add(0, m.root);
			return path;
		}
	}
	
	public Solver getDAGSolver() {
		return new Solver();
	}
	
	public List<Node> solveRandomDAG(Maze m, String goal) {
		return getDAGSolver().solve(m, new Node(goal));
	}

	public Maze createDAG(int poolSize) {
		Random random = new Random();
		Map<Integer, List<Node>> nodesLevels = new HashMap<>();
		int level = 1;
		Node root = null;
		Maze m = null;
		List<Integer> pool = new ArrayList<Integer>();
		for (int i = 1; i <= poolSize; i++) {
			pool.add(i);
		}
		while (pool.size() > 0) {
			nodesLevels.put(level, new ArrayList<Node>());
			if (level == 1) {
				int r = random.nextInt(pool.size() - 1) + 1;
				root = new Node(String.valueOf(pool.get(r - 1)), null);
				m = new Maze(root);
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
							Node nx = new Node(String.valueOf(rx), n);
							m.addNode(nx);
							pool.remove(r - 1);
							nodesLevels.get(level).add(nx);
							n.branches.add(nx);
						}
					}
				}
			}

			level++;
		}

		return m;
	}

	public static void main(String[] args) {
		int numNodes = 100;
		MazeGenerator mg = new MazeGenerator();
		Maze m = mg.createDAG(numNodes);
		System.out.println(m.dump());
		
		Random random = new Random();
		int randomGoal = random.nextInt(numNodes) + 1;
		System.out.println(String.format("Solve for %d", randomGoal));
		List<Node> solution = mg.solveRandomDAG(m, String.valueOf(randomGoal));
		System.out.print(solution);
		
	}
}
