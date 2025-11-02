package graph;

import graph.util.Graph;
import graph.util.GraphLoader;

import java.io.IOException;

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

            // TODO: Add SCC analysis
            // TODO: Add topological sort
            // TODO: Add shortest/longest paths

        } catch (IOException e) {
            System.err.println("Error loading graph: " + e.getMessage());
            System.exit(1);
        }
    }
}