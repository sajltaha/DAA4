package graph.util;

import java.util.*;

/**
 * Represents a weighted directed graph using adjacency list representation.
 */
public class Graph {

    private final int n; // number of vertices
    private final boolean directed;
    private final List<List<Edge>> adjList;
    private final String weightModel; // "edge" or "node"

    /**
     * Edge representation with destination and weight.
     */
    public static class Edge {
        public final int to;
        public final int weight;

        public Edge(int to, int weight) {
            this.to = to;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return String.format("->%d (w=%d)", to, weight);
        }
    }

    /**
     * Constructor for creating a graph.
     * @param n number of vertices
     * @param directed whether the graph is directed
     * @param weightModel "edge" or "node"
     */
    public Graph(int n, boolean directed, String weightModel) {
        this.n = n;
        this.directed = directed;
        this.weightModel = weightModel;
        this.adjList = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            adjList.add(new ArrayList<>());
        }
    }

    /**
     * Add an edge to the graph.
     * @param u source vertex
     * @param v destination vertex
     * @param w weight
     */
    public void addEdge(int u, int v, int w) {
        adjList.get(u).add(new Edge(v, w));
        if (!directed) {
            adjList.get(v).add(new Edge(u, w));
        }
    }

    /**
     * Get all edges from a vertex.
     * @param u vertex
     * @return list of outgoing edges
     */
    public List<Edge> getEdges(int u) {
        return adjList.get(u);
    }

    /**
     * Get number of vertices.
     * @return number of vertices
     */
    public int getVertexCount() {
        return n;
    }

    /**
     * Check if graph is directed.
     * @return true if directed
     */
    public boolean isDirected() {
        return directed;
    }

    /**
     * Get weight model.
     * @return "edge" or "node"
     */
    public String getWeightModel() {
        return weightModel;
    }

    /**
     * Get total number of edges in the graph.
     * @return edge count
     */
    public int getEdgeCount() {
        int count = 0;
        for (List<Edge> edges : adjList) {
            count += edges.size();
        }
        return directed ? count : count / 2;
    }

    /**
     * Create a reverse graph (transpose).
     * @return reversed graph
     */
    public Graph reverse() {
        Graph reversed = new Graph(n, directed, weightModel);
        for (int u = 0; u < n; u++) {
            for (Edge e : adjList.get(u)) {
                reversed.addEdge(e.to, u, e.weight);
            }
        }
        return reversed;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Graph: n=%d, edges=%d, directed=%s, weightModel=%s\n",
                n, getEdgeCount(), directed, weightModel));
        for (int u = 0; u < n; u++) {
            sb.append(String.format("  %d: %s\n", u, adjList.get(u)));
        }
        return sb.toString();
    }
}