package edu.yale.abfab.mazes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MazeGenerator2 {

	public static class Maze implements Comparable<Maze> {
		private Node root;
		private Map<String, Node> allNodes;
		private double branchProb;
		private int branchCount;
		private int currBranch;

		public Maze(Node root) {
			this(root, 0d);
		}

		public Maze(Node root, double branchProb) {
			this(root, branchProb, -1);
		}

		public Maze(Node root, double branchProb, int currBranch) {
			this.root = root;
			this.branchProb = branchProb;
			this.currBranch = currBranch;
			allNodes = new HashMap<>();
			addNode(root);
			branchCount = 0;
		}

		public void addNode(Node node) {
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

		public double getBranchProb() {
			return branchProb;
		}

		public void setBranchProb(double branchProb) {
			this.branchProb = branchProb;
		}

		public int getBranchCount() {
			return branchCount;
		}

		public void setBranchCount(int branchCount) {
			this.branchCount = branchCount;
		}

		public int getCurrBranch() {
			return currBranch;
		}

		public void setCurrBranch(int currBranch) {
			this.currBranch = currBranch;
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

		@Override
		public int compareTo(Maze o) {
			return root.toString().compareTo(o.root.toString());
		}

	}

	public static class Node {
		private String name;
		private Set<Node> branches;
		private Node parent;

		public Node(String name) {
			this(name, null);
		}

		public Node(String name, Node parent) {
			this.name = name;
			this.parent = parent;
			branches = new HashSet<>();
		}

		public String getName() {
			return name + "Service";
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
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	}

	public static class Branch extends Node {
		private List<Maze> forks;
		
		public Branch(String name, Node parent, List<Maze> forks) {
			super(name, parent);
			this.forks = forks;
		}

		public Branch(String name, Node parent, double branchProb,
				List<String> nodePool, Maze rootMaze) {
			super(name, parent);
			forks = new ArrayList<>();

			// 2 or 3 forks
			double p = new Random().nextDouble();
			int numForks = (p <= 0.5) ? 2 : 3;
			//int numForks = 2;
			for (int i = 0; i < numForks; i++) {
				int bn = rootMaze.getBranchCount();
				List<String> newNodePool = new ArrayList<>();
				for (String np : nodePool) {
					newNodePool.add(String.format("%s-%d", np, bn));
				}
				forks.add(createDAG(newNodePool, branchProb, branchProb, bn));
				rootMaze.setBranchCount(bn + 1);
			}
		}

		public List<Maze> getForks() {
			return forks;
		}

		public void setForks(List<Maze> forks) {
			this.forks = forks;
		}

		public String dump() {
			Iterator<Maze> forkIter = forks.iterator();
			StringBuilder fsb = new StringBuilder();
			while (forkIter.hasNext()) {
				fsb.append(forkIter.next().dump());
				if (forkIter.hasNext()) {
					fsb.append(", ");
				}
			}
			String head = "";
			if (fsb.length() > 0) {
				head = String.format("[%s: {%s}]", fsb, getName());
			}

			Iterator<Node> branchIter = getBranches().iterator();
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
			return String.format("%s%s", head, following);
		}
	}

	class Solver {
		List<Node> path;
		Node goal;
		Maze maze;
		List<Node> solution;

		public Solver(Maze maze, Node goal) {
			this.maze = maze;
			this.goal = goal;
			solution = solve();
		}

		public String solution() {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			Iterator<Node> solIter = solution.iterator();
			while (solIter.hasNext()) {
				Node n = solIter.next();
				if (n instanceof Branch) {
					Branch b = (Branch) n;
					sb.append("(");
					Collections.sort(b.getForks());
					Iterator<Maze> forkIter = b.getForks().iterator();
					while (forkIter.hasNext()) {
						Solver solver = new Solver(forkIter.next(), b);
						sb.append(solver.solution());
						if (forkIter.hasNext()) {
							sb.append(" & ");
						}
					}
					sb.append(")");

					sb.append(String.format(" -> %s", b.getName()));

				} else {
					sb.append(n);
				}
				if (solIter.hasNext()) {
					sb.append(" -> ");
				}
			}
			sb.append("]");

			return sb.toString();
		}

		private List<Node> solve() {
			if (maze.getCurrBranch() >= 0) {
				goal = new Node(String.format("%s-%d", goal,
						maze.getCurrBranch()));
			}
			Node p = maze.getNode(goal);
			if (p == null) {
				boolean debug = true;
			}
			path = new ArrayList<>();
			while (p != maze.root) {
				path.add(0, p);
				p = p.parent;
			}
			path.add(0, maze.root);
			return path;
		}
	}

	List<String> nodePool;

	public List<String> getNodePool() {
		return nodePool;
	}

	public void setNodePool(List<String> nodePool) {
		this.nodePool = nodePool;
	}

	public Solver getDAGSolver(Maze maze, Node goal) {
		return new Solver(maze, goal);
	}

	public String solveRandomDAG(Maze m, String goal) {
		return getDAGSolver(m, new Node(goal)).solution();
	}

	public static Maze createDAG(List<String> nodePool, double initialBranchProb,
			double subsequentBranchProb, int currBranch) {
		//this.nodePool = nodePool;
		List<String> origNodePool = new ArrayList<>();
		for (String np : nodePool) {
			origNodePool.add(np);
		}
		Random random = new Random();
		Map<Integer, List<Node>> nodesLevels = new HashMap<>();
		int level = 1;
		Node root = null;
		Maze m = null;

		while (nodePool.size() > 0) {
			nodesLevels.put(level, new ArrayList<Node>());
			if (level == 1) {
				int r = random.nextInt(nodePool.size() - 1) + 1;
				root = new Node(String.valueOf(nodePool.get(r - 1)), null);
				m = new Maze(root, subsequentBranchProb, currBranch);
				nodePool.remove(r - 1);
				nodesLevels.get(level).add(root);
			} else {
				int prevLevel = level - 1;
				for (Node n : nodesLevels.get(prevLevel)) {
					if (nodePool.size() > 0) {
						int numBranchSize = Math.min(nodePool.size(), 3);
						int numBranches;
						if (numBranchSize == 1) {
							numBranches = 1;
						} else {
							numBranches = random.nextInt(numBranchSize - 1) + 1;
						}
						for (int x = 0; x < numBranches; x++) {
							int r;
							if (nodePool.size() == 1) {
								r = 1;
							} else {
								r = random.nextInt(nodePool.size() - 1) + 1;
							}
							Object rx = nodePool.get(r - 1);

							Node nx;
							double rp = random.nextDouble();
							if (rp < initialBranchProb) {
								nx = new Branch(String.valueOf(rx), n,
										subsequentBranchProb, origNodePool, m);
							} else {
								nx = new Node(String.valueOf(rx), n);
							}
							m.addNode(nx);
							nodePool.remove(rx);
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
		for (int c = 0; c < 100; c++) {
			int numNodes = 10;
			List<String> nodeList = new ArrayList<>();
			for (int i = 1; i <= numNodes; i++) {
				nodeList.add(String.valueOf(i));
			}
			MazeGenerator2 mg = new MazeGenerator2();
			Maze m = mg.createDAG(nodeList, 0.2, 0.0, -1);
			System.out.println();
			System.out.println("PATH");
			System.out.println(m.dump());

			Random random = new Random();
			int randomGoal = random.nextInt(numNodes) + 1;
			System.out.println(String.format("Solve for %d", randomGoal));
			String solution = mg.solveRandomDAG(m, String.valueOf(randomGoal));
			System.out.print(solution);
		}
	}
}
