package graph.scc;

import graph.util.Graph;

import java.util.*;

/**
 * Builds a condensation graph (DAG) from strongly connected components.
 * Each SCC becomes a single vertex in the condensation graph.
 */
public class CondensationGraph {

    private final Graph originalGraph;
    private final List<List<Integer>> sccs;
    private final int[] componentId;

    private Graph condensation;

    public CondensationGraph(Graph originalGraph, List<List<Integer>> sccs, int[] componentId) {
        this.originalGraph = originalGraph;
        this.sccs = sccs;
        this.componentId = componentId;
    }

    /**
     * Build the condensation graph.
     * Each SCC becomes a single vertex, edges connect different SCCs.
     * @return condensation graph (DAG)
     */
    public Graph build() {
        int numComponents = sccs.size();
        condensation = new Graph(numComponents, true, originalGraph.getWeightModel());

        // Track edges between components to avoid duplicates
        Set<String> addedEdges = new HashSet<>();

        // For each vertex in original graph
        for (int u = 0; u < originalGraph.getVertexCount(); u++) {
            int compU = componentId[u];

            // Check all outgoing edges
            for (Graph.Edge edge : originalGraph.getEdges(u)) {
                int v = edge.to;
                int compV = componentId[v];

                // If edge goes to different component, add to condensation
                if (compU != compV) {
                    String edgeKey = compU + "->" + compV;
                    if (!addedEdges.contains(edgeKey)) {
                        condensation.addEdge(compU, compV, edge.weight);
                        addedEdges.add(edgeKey);
                    }
                }
            }
        }

        return condensation;
    }

    /**
     * Get the condensation graph.
     * @return condensation DAG
     */
    public Graph getCondensation() {
        if (condensation == null) {
            throw new IllegalStateException("Must call build() first");
        }
        return condensation;
    }

    /**
     * Get the SCC that a vertex belongs to.
     * @param vertex original vertex
     * @return SCC index
     */
    public int getComponentForVertex(int vertex) {
        return componentId[vertex];
    }

    /**
     * Get all vertices in a component.
     * @param componentIndex SCC index
     * @return list of vertices in that SCC
     */
    public List<Integer> getVerticesInComponent(int componentIndex) {
        return sccs.get(componentIndex);
    }

    /**
     * Print condensation graph information.
     */
    public void printCondensation() {
        System.out.println("\n=== Condensation Graph (DAG) ===");
        System.out.println("Components: " + condensation.getVertexCount());
        System.out.println("Inter-component edges: " + condensation.getEdgeCount());
        System.out.println("\nComponent details:");

        for (int i = 0; i < sccs.size(); i++) {
            System.out.printf("Component %d: vertices %s\n", i, sccs.get(i));
            List<Graph.Edge> edges = condensation.getEdges(i);
            if (!edges.isEmpty()) {
                System.out.printf("  Edges to: %s\n", edges);
            }
        }
    }

    /**
     * Check if the condensation is indeed a DAG (no cycles).
     * @return true if it's a valid DAG
     */
    public boolean isDAG() {
        // A condensation graph should always be a DAG by definition
        // But we can verify by checking for back edges
        int n = condensation.getVertexCount();
        int[] color = new int[n]; // 0=white, 1=gray, 2=black

        for (int v = 0; v < n; v++) {
            if (color[v] == 0) {
                if (hasCycleDFS(v, color)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasCycleDFS(int u, int[] color) {
        color[u] = 1; // Gray - being processed

        for (Graph.Edge edge : condensation.getEdges(u)) {
            int v = edge.to;
            if (color[v] == 1) {
                return true; // Back edge found - cycle!
            }
            if (color[v] == 0 && hasCycleDFS(v, color)) {
                return true;
            }
        }

        color[u] = 2; // Black - finished
        return false;
    }
}