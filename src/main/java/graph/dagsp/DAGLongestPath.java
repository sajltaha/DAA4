package graph.dagsp;

import graph.topo.TopologicalSort;
import graph.util.AlgorithmMetrics;
import graph.util.Graph;
import graph.util.Metrics;

import java.util.*;

/**
 * Longest path algorithm for Directed Acyclic Graphs (DAG).
 * Also known as Critical Path Method (CPM) for project scheduling.
 * Uses topological ordering for O(V + E) complexity.
 */
public class DAGLongestPath {

    private final Graph graph;
    private final Metrics metrics;

    private int source;
    private int[] dist;
    private int[] parent;
    private List<Integer> topoOrder;

    private static final int NEG_INF = Integer.MIN_VALUE / 2;

    public DAGLongestPath(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Graph must be directed");
        }
        this.graph = graph;
        this.metrics = new AlgorithmMetrics();
    }

    /**
     * Compute longest paths from source vertex.
     * @param source source vertex
     * @return true if successful, false if graph has a cycle
     */
    public boolean computeLongestPaths(int source) {
        this.source = source;
        int n = graph.getVertexCount();

        // Initialize distances and parents
        dist = new int[n];
        parent = new int[n];
        Arrays.fill(dist, NEG_INF);
        Arrays.fill(parent, -1);
        dist[source] = 0;

        metrics.startTimer();

        // Get topological order
        TopologicalSort topo = new TopologicalSort(graph);
        topoOrder = topo.sortDFS();

        if (topoOrder == null) {
            metrics.stopTimer();
            return false; // Graph has a cycle
        }

        // Process vertices in topological order
        for (int u : topoOrder) {
            if (dist[u] != NEG_INF) {
                // Relax all edges from u (maximize instead of minimize)
                for (Graph.Edge edge : graph.getEdges(u)) {
                    int v = edge.to;
                    int weight = edge.weight;

                    metrics.incrementCounter("edge_relaxations");

                    if (dist[u] + weight > dist[v]) {
                        dist[v] = dist[u] + weight;
                        parent[v] = u;
                        metrics.incrementCounter("distance_updates");
                    }
                }
            }
        }

        metrics.stopTimer();
        return true;
    }

    /**
     * Compute longest path in entire graph (not from a specific source).
     * Finds the overall critical path.
     * @return true if successful
     */
    public boolean computeCriticalPath() {
        int n = graph.getVertexCount();

        // Initialize
        dist = new int[n];
        parent = new int[n];
        Arrays.fill(dist, 0);
        Arrays.fill(parent, -1);

        metrics.startTimer();

        // Get topological order
        TopologicalSort topo = new TopologicalSort(graph);
        topoOrder = topo.sortDFS();

        if (topoOrder == null) {
            metrics.stopTimer();
            return false;
        }

        // Process all vertices
        for (int u : topoOrder) {
            for (Graph.Edge edge : graph.getEdges(u)) {
                int v = edge.to;
                int weight = edge.weight;

                metrics.incrementCounter("edge_relaxations");

                if (dist[u] + weight > dist[v]) {
                    dist[v] = dist[u] + weight;
                    parent[v] = u;
                    metrics.incrementCounter("distance_updates");
                }
            }
        }

        metrics.stopTimer();
        this.source = -1; // No specific source
        return true;
    }

    /**
     * Get the longest distance to a vertex.
     * @param vertex target vertex
     * @return longest distance
     */
    public int getDistance(int vertex) {
        if (dist == null) {
            throw new IllegalStateException("Must call compute method first");
        }
        return dist[vertex];
    }

    /**
     * Check if a vertex is reachable from source.
     * @param vertex target vertex
     * @return true if reachable
     */
    public boolean isReachable(int vertex) {
        return dist != null && dist[vertex] != NEG_INF;
    }

    /**
     * Reconstruct longest path to a vertex.
     * @param target target vertex
     * @return path from source to target
     */
    public List<Integer> getPath(int target) {
        if (dist == null) {
            return null;
        }

        List<Integer> path = new ArrayList<>();
        int current = target;

        while (current != -1) {
            path.add(current);
            current = parent[current];
        }

        Collections.reverse(path);
        return path;
    }

    /**
     * Find the critical path (longest path in the entire graph).
     * @return critical path details
     */
    public CriticalPath getCriticalPath() {
        if (dist == null) {
            throw new IllegalStateException("Must call compute method first");
        }

        // Find vertex with maximum distance
        int maxDist = 0;
        int endVertex = 0;

        for (int v = 0; v < dist.length; v++) {
            if (dist[v] > maxDist) {
                maxDist = dist[v];
                endVertex = v;
            }
        }

        List<Integer> path = getPath(endVertex);
        return new CriticalPath(path, maxDist);
    }

    /**
     * Get all distances.
     * @return array of distances
     */
    public int[] getAllDistances() {
        if (dist == null) {
            throw new IllegalStateException("Must call compute method first");
        }
        return dist.clone();
    }

    /**
     * Get performance metrics.
     * @return metrics object
     */
    public Metrics getMetrics() {
        return metrics;
    }

    /**
     * Print longest path results.
     */
    public void printResults() {
        System.out.println("\n=== Longest Paths ===");

        if (dist == null) {
            System.out.println("ERROR: Graph contains a cycle (not a DAG)");
            return;
        }

        if (source >= 0) {
            System.out.println("From source: " + source);
        } else {
            System.out.println("Critical Path Analysis (all vertices)");
        }

        System.out.println("\nDistances:");
        for (int v = 0; v < dist.length; v++) {
            if (source < 0 || dist[v] != NEG_INF) {
                System.out.printf("  Vertex %d: distance = %d\n", v, dist[v]);
            }
        }

        // Show critical path
        CriticalPath cp = getCriticalPath();
        System.out.println("\n*** Critical Path ***");
        System.out.println("Path: " + cp.path);
        System.out.println("Length: " + cp.length);

        System.out.println("\n" + metrics.getMetricsReport());
    }

    /**
     * Container for critical path information.
     */
    public static class CriticalPath {
        public final List<Integer> path;
        public final int length;

        public CriticalPath(List<Integer> path, int length) {
            this.path = path;
            this.length = length;
        }

        @Override
        public String toString() {
            return String.format("CriticalPath{path=%s, length=%d}", path, length);
        }
    }
}