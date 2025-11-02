package graph.dagsp;

import graph.topo.TopologicalSort;
import graph.util.AlgorithmMetrics;
import graph.util.Graph;
import graph.util.Metrics;

import java.util.*;

/**
 * Shortest path algorithm for Directed Acyclic Graphs (DAG).
 * Uses topological ordering for O(V + E) complexity.
 */
public class DAGShortestPath {

    private final Graph graph;
    private final Metrics metrics;

    private int source;
    private int[] dist;
    private int[] parent;
    private List<Integer> topoOrder;

    private static final int INF = Integer.MAX_VALUE / 2;

    public DAGShortestPath(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Graph must be directed");
        }
        this.graph = graph;
        this.metrics = new AlgorithmMetrics();
    }

    /**
     * Compute shortest paths from source vertex.
     * @param source source vertex
     * @return true if successful, false if graph has a cycle
     */
    public boolean computeShortestPaths(int source) {
        this.source = source;
        int n = graph.getVertexCount();

        // Initialize distances and parents
        dist = new int[n];
        parent = new int[n];
        Arrays.fill(dist, INF);
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
            if (dist[u] != INF) {
                // Relax all edges from u
                for (Graph.Edge edge : graph.getEdges(u)) {
                    int v = edge.to;
                    int weight = edge.weight;

                    metrics.incrementCounter("edge_relaxations");

                    if (dist[u] + weight < dist[v]) {
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
     * Get the shortest distance to a vertex.
     * @param vertex target vertex
     * @return shortest distance, or INF if unreachable
     */
    public int getDistance(int vertex) {
        if (dist == null) {
            throw new IllegalStateException("Must call computeShortestPaths() first");
        }
        return dist[vertex];
    }

    /**
     * Check if a vertex is reachable from source.
     * @param vertex target vertex
     * @return true if reachable
     */
    public boolean isReachable(int vertex) {
        return dist != null && dist[vertex] != INF;
    }

    /**
     * Reconstruct shortest path to a vertex.
     * @param target target vertex
     * @return path from source to target, or null if unreachable
     */
    public List<Integer> getPath(int target) {
        if (dist == null || dist[target] == INF) {
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
     * Get all distances from source.
     * @return array of distances
     */
    public int[] getAllDistances() {
        if (dist == null) {
            throw new IllegalStateException("Must call computeShortestPaths() first");
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
     * Print shortest path results.
     */
    public void printResults() {
        System.out.println("\n=== Shortest Paths from Source " + source + " ===");

        if (dist == null) {
            System.out.println("ERROR: Graph contains a cycle (not a DAG)");
            return;
        }

        System.out.println("\nDistances:");
        for (int v = 0; v < dist.length; v++) {
            if (dist[v] == INF) {
                System.out.printf("  Vertex %d: unreachable\n", v);
            } else {
                System.out.printf("  Vertex %d: distance = %d\n", v, dist[v]);
            }
        }

        System.out.println("\nPaths:");
        for (int v = 0; v < dist.length; v++) {
            if (dist[v] != INF && v != source) {
                List<Integer> path = getPath(v);
                System.out.printf("  %d -> %d: %s (length %d)\n",
                        source, v, path, dist[v]);
            }
        }

        System.out.println("\n" + metrics.getMetricsReport());
    }

    /**
     * Get summary statistics of shortest paths.
     * @return summary map
     */
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();

        int reachable = 0;
        int minDist = INF;
        int maxDist = 0;
        int totalDist = 0;

        for (int v = 0; v < dist.length; v++) {
            if (dist[v] != INF && v != source) {
                reachable++;
                minDist = Math.min(minDist, dist[v]);
                maxDist = Math.max(maxDist, dist[v]);
                totalDist += dist[v];
            }
        }

        summary.put("source", source);
        summary.put("reachable_vertices", reachable);
        summary.put("min_distance", minDist == INF ? 0 : minDist);
        summary.put("max_distance", maxDist);
        summary.put("avg_distance", reachable > 0 ? (double) totalDist / reachable : 0);

        return summary;
    }
}