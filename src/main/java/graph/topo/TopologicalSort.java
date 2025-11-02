package graph.topo;

import graph.util.AlgorithmMetrics;
import graph.util.Graph;
import graph.util.Metrics;

import java.util.*;

/**
 * Topological sorting algorithms for DAGs.
 * Provides both DFS-based and Kahn's algorithm implementations.
 */
public class TopologicalSort {

    private final Graph graph;
    private final Metrics metrics;
    private List<Integer> topoOrder;

    public TopologicalSort(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Graph must be directed for topological sort");
        }
        this.graph = graph;
        this.metrics = new AlgorithmMetrics();
    }

    /**
     * Perform topological sort using DFS-based algorithm.
     * Time complexity: O(V + E)
     * @return topological order, or null if graph has a cycle
     */
    public List<Integer> sortDFS() {
        int n = graph.getVertexCount();
        boolean[] visited = new boolean[n];
        Stack<Integer> stack = new Stack<>();

        metrics.startTimer();

        // Visit all vertices
        for (int v = 0; v < n; v++) {
            if (!visited[v]) {
                if (!dfsTopo(v, visited, stack, new boolean[n])) {
                    metrics.stopTimer();
                    return null; // Cycle detected
                }
            }
        }

        // Build result from stack
        topoOrder = new ArrayList<>();
        while (!stack.isEmpty()) {
            topoOrder.add(stack.pop());
            metrics.incrementCounter("stack_pops");
        }

        metrics.stopTimer();
        return topoOrder;
    }

    /**
     * DFS helper for topological sort with cycle detection.
     */
    private boolean dfsTopo(int u, boolean[] visited, Stack<Integer> stack, boolean[] recStack) {
        visited[u] = true;
        recStack[u] = true;
        metrics.incrementCounter("dfs_visits");

        // Visit all neighbors
        for (Graph.Edge edge : graph.getEdges(u)) {
            int v = edge.to;
            metrics.incrementCounter("edge_traversals");

            if (recStack[v]) {
                return false; // Back edge - cycle detected
            }

            if (!visited[v]) {
                if (!dfsTopo(v, visited, stack, recStack)) {
                    return false;
                }
            }
        }

        recStack[u] = false;
        stack.push(u);
        metrics.incrementCounter("stack_pushes");
        return true;
    }

    /**
     * Perform topological sort using Kahn's algorithm (BFS-based).
     * Time complexity: O(V + E)
     * @return topological order, or null if graph has a cycle
     */
    public List<Integer> sortKahn() {
        int n = graph.getVertexCount();
        int[] inDegree = new int[n];

        // Calculate in-degrees
        for (int u = 0; u < n; u++) {
            for (Graph.Edge edge : graph.getEdges(u)) {
                inDegree[edge.to]++;
            }
        }

        metrics.startTimer();

        // Queue for vertices with in-degree 0
        Queue<Integer> queue = new LinkedList<>();
        for (int v = 0; v < n; v++) {
            if (inDegree[v] == 0) {
                queue.offer(v);
                metrics.incrementCounter("queue_adds");
            }
        }

        topoOrder = new ArrayList<>();

        while (!queue.isEmpty()) {
            int u = queue.poll();
            topoOrder.add(u);
            metrics.incrementCounter("queue_removes");

            // Reduce in-degree for neighbors
            for (Graph.Edge edge : graph.getEdges(u)) {
                int v = edge.to;
                inDegree[v]--;
                metrics.incrementCounter("degree_updates");

                if (inDegree[v] == 0) {
                    queue.offer(v);
                    metrics.incrementCounter("queue_adds");
                }
            }
        }

        metrics.stopTimer();

        // Check if all vertices were processed (no cycle)
        if (topoOrder.size() != n) {
            return null; // Graph has a cycle
        }

        return topoOrder;
    }

    /**
     * Get the topological order from the last sort operation.
     * @return topological order
     */
    public List<Integer> getOrder() {
        if (topoOrder == null) {
            throw new IllegalStateException("Must call sort method first");
        }
        return topoOrder;
    }

    /**
     * Get performance metrics.
     * @return metrics object
     */
    public Metrics getMetrics() {
        return metrics;
    }

    /**
     * Print topological sort results.
     */
    public void printResults() {
        System.out.println("\n=== Topological Sort ===");
        if (topoOrder == null) {
            System.out.println("ERROR: Graph contains a cycle (not a DAG)");
        } else {
            System.out.println("Topological order: " + topoOrder);
            System.out.println("Number of vertices: " + topoOrder.size());
        }
        System.out.println("\n" + metrics.getMetricsReport());
    }

    /**
     * Verify if the computed order is valid.
     * @return true if valid topological order
     */
    public boolean isValidOrder() {
        if (topoOrder == null || topoOrder.size() != graph.getVertexCount()) {
            return false;
        }

        // Map each vertex to its position in the order
        Map<Integer, Integer> position = new HashMap<>();
        for (int i = 0; i < topoOrder.size(); i++) {
            position.put(topoOrder.get(i), i);
        }

        // Check that for every edge u->v, u comes before v
        for (int u = 0; u < graph.getVertexCount(); u++) {
            for (Graph.Edge edge : graph.getEdges(u)) {
                int v = edge.to;
                if (position.get(u) >= position.get(v)) {
                    return false; // Invalid order
                }
            }
        }

        return true;
    }
}