package graph.scc;

import graph.util.AlgorithmMetrics;
import graph.util.Graph;
import graph.util.Metrics;

import java.util.*;

/**
 * Tarjan's algorithm for finding Strongly Connected Components (SCC).
 * Time complexity: O(V + E)
 */
public class SCC {

    private final Graph graph;
    private final Metrics metrics;

    // Tarjan's algorithm state
    private int[] disc;      // Discovery time
    private int[] low;       // Lowest reachable vertex
    private boolean[] onStack;
    private Stack<Integer> stack;
    private int time;

    // Results
    private List<List<Integer>> sccs;
    private int[] componentId; // Which component each vertex belongs to

    public SCC(Graph graph) {
        this.graph = graph;
        this.metrics = new AlgorithmMetrics();
    }

    /**
     * Find all strongly connected components using Tarjan's algorithm.
     * @return list of SCCs, each SCC is a list of vertices
     */
    public List<List<Integer>> findSCCs() {
        int n = graph.getVertexCount();

        // Initialize
        disc = new int[n];
        low = new int[n];
        onStack = new boolean[n];
        stack = new Stack<>();
        componentId = new int[n];
        sccs = new ArrayList<>();
        time = 0;

        Arrays.fill(disc, -1);
        Arrays.fill(componentId, -1);

        metrics.startTimer();

        // Run DFS from each unvisited vertex
        for (int v = 0; v < n; v++) {
            if (disc[v] == -1) {
                tarjanDFS(v);
            }
        }

        metrics.stopTimer();

        return sccs;
    }

    /**
     * Tarjan's DFS recursive procedure.
     * @param u current vertex
     */
    private void tarjanDFS(int u) {
        // Initialize discovery time and low value
        disc[u] = low[u] = time++;
        stack.push(u);
        onStack[u] = true;

        metrics.incrementCounter("dfs_visits");

        // Visit all neighbors
        for (Graph.Edge edge : graph.getEdges(u)) {
            int v = edge.to;
            metrics.incrementCounter("edge_traversals");

            if (disc[v] == -1) {
                // Tree edge - not yet visited
                tarjanDFS(v);
                low[u] = Math.min(low[u], low[v]);
            } else if (onStack[v]) {
                // Back edge to vertex in current SCC
                low[u] = Math.min(low[u], disc[v]);
            }
        }

        // If u is a root node, pop the stack and create SCC
        if (low[u] == disc[u]) {
            List<Integer> scc = new ArrayList<>();
            int v;
            do {
                v = stack.pop();
                onStack[v] = false;
                componentId[v] = sccs.size();
                scc.add(v);
                metrics.incrementCounter("stack_pops");
            } while (v != u);

            // Sort vertices in SCC for consistent output
            Collections.sort(scc);
            sccs.add(scc);
        }
    }

    /**
     * Get the component ID for each vertex.
     * @return array where componentId[v] = component number of vertex v
     */
    public int[] getComponentIds() {
        if (componentId == null) {
            throw new IllegalStateException("Must call findSCCs() first");
        }
        return componentId;
    }

    /**
     * Get the number of strongly connected components.
     * @return number of SCCs
     */
    public int getComponentCount() {
        if (sccs == null) {
            throw new IllegalStateException("Must call findSCCs() first");
        }
        return sccs.size();
    }

    /**
     * Get performance metrics.
     * @return metrics object
     */
    public Metrics getMetrics() {
        return metrics;
    }

    /**
     * Print SCC results.
     */
    public void printResults() {
        System.out.println("\n=== Strongly Connected Components ===");
        System.out.println("Number of SCCs: " + sccs.size());

        for (int i = 0; i < sccs.size(); i++) {
            List<Integer> scc = sccs.get(i);
            System.out.printf("SCC %d (size %d): %s\n", i, scc.size(), scc);
        }

        System.out.println("\n" + metrics.getMetricsReport());
    }

    /**
     * Get a summary of SCC sizes.
     * @return map of size -> count
     */
    public Map<Integer, Integer> getSizeSummary() {
        Map<Integer, Integer> summary = new HashMap<>();
        for (List<Integer> scc : sccs) {
            int size = scc.size();
            summary.put(size, summary.getOrDefault(size, 0) + 1);
        }
        return summary;
    }
}