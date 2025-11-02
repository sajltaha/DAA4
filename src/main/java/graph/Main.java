package graph;

import graph.scc.CondensationGraph;
import graph.scc.SCC;
import graph.util.Graph;
import graph.util.GraphLoader;

import java.io.IOException;
import java.util.List;

/**
 * Main entry point for the Smart City Graph Algorithms application.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Smart City Graph Algorithms ===\n");

        // Default dataset
        String datasetPath = "data/tasks.json";

        // Allow command line argument for custom dataset
        if (args.length > 0) {
            datasetPath = args[0];
        }

        try {
            System.out.println("Loading graph from: " + datasetPath);
            GraphLoader.GraphData data = GraphLoader.loadFromJson(datasetPath);
            Graph graph = data.graph;
            int source = data.source;

            System.out.println("\n" + graph);
            System.out.println("Source vertex: " + source);

            System.out.println("\n=== Graph Analysis ===");
            System.out.println("Vertices: " + graph.getVertexCount());
            System.out.println("Edges: " + graph.getEdgeCount());
            System.out.println("Directed: " + graph.isDirected());
            System.out.println("Weight Model: " + graph.getWeightModel());

            // === SCC Analysis ===
            System.out.println("\n" + "=".repeat(50));
            System.out.println("STEP 1: Finding Strongly Connected Components");
            System.out.println("=".repeat(50));

            SCC sccFinder = new SCC(graph);
            List<List<Integer>> sccs = sccFinder.findSCCs();
            sccFinder.printResults();

            // Build condensation graph
            System.out.println("\n" + "=".repeat(50));
            System.out.println("STEP 2: Building Condensation Graph");
            System.out.println("=".repeat(50));

            CondensationGraph condensation = new CondensationGraph(
                    graph, sccs, sccFinder.getComponentIds());
            Graph dag = condensation.build();
            condensation.printCondensation();

            System.out.println("\nIs condensation a valid DAG? " + condensation.isDAG());

            // === Topological Sort ===
            System.out.println("\n" + "=".repeat(50));
            System.out.println("STEP 3: Topological Sorting of Components");
            System.out.println("=".repeat(50));

            graph.topo.TopologicalSort topoSort = new graph.topo.TopologicalSort(dag);

            // Try DFS-based topological sort
            System.out.println("\n--- Using DFS-based algorithm ---");
            List<Integer> orderDFS = topoSort.sortDFS();
            topoSort.printResults();

            if (orderDFS != null) {
                System.out.println("Valid topological order? " + topoSort.isValidOrder());

                // Derive task ordering
                graph.topo.TaskOrdering taskOrdering = new graph.topo.TaskOrdering(orderDFS, sccs);
                taskOrdering.deriveTaskOrder();
                taskOrdering.printTaskOrder();
            }

            // Try Kahn's algorithm for comparison
            System.out.println("\n--- Using Kahn's algorithm (for comparison) ---");
            graph.topo.TopologicalSort topoSortKahn = new graph.topo.TopologicalSort(dag);
            List<Integer> orderKahn = topoSortKahn.sortKahn();
            topoSortKahn.printResults();

            if (orderKahn != null) {
                System.out.println("Valid topological order? " + topoSortKahn.isValidOrder());
            }

            // === Shortest Paths in DAG ===
            System.out.println("\n" + "=".repeat(50));
            System.out.println("STEP 4: Shortest Paths in DAG");
            System.out.println("=".repeat(50));

            graph.dagsp.DAGShortestPath shortestPath = new graph.dagsp.DAGShortestPath(dag);

            // Map source from original graph to condensation graph
            int condensationSource = condensation.getComponentForVertex(source);
            System.out.println("\nOriginal source vertex: " + source);
            System.out.println("Condensation source component: " + condensationSource);

            if (shortestPath.computeShortestPaths(condensationSource)) {
                shortestPath.printResults();

                System.out.println("\nSummary: " + shortestPath.getSummary());
            }

            // === Longest Paths (Critical Path) ===
            System.out.println("\n" + "=".repeat(50));
            System.out.println("STEP 5: Longest Paths (Critical Path Analysis)");
            System.out.println("=".repeat(50));

            graph.dagsp.DAGLongestPath longestPath = new graph.dagsp.DAGLongestPath(dag);

            if (longestPath.computeCriticalPath()) {
                longestPath.printResults();
            }

            // Also compute from specific source
            System.out.println("\n--- Longest paths from source " + condensationSource + " ---");
            graph.dagsp.DAGLongestPath longestFromSource = new graph.dagsp.DAGLongestPath(dag);
            if (longestFromSource.computeLongestPaths(condensationSource)) {
                longestFromSource.printResults();
            }

        } catch (IOException e) {
            System.err.println("Error loading graph: " + e.getMessage());
            System.exit(1);
        }
    }
}