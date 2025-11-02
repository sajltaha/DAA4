package graph;

import graph.util.GraphGenerator;
import graph.util.GraphGenerator.GraphConfig;

import java.io.IOException;

/**
 * Main class for generating all required datasets.
 * Generates 9 datasets: 3 small, 3 medium, 3 large.
 */
public class DatasetGenerator {

    public static void main(String[] args) {
        GraphGenerator generator = new GraphGenerator(42);

        System.out.println("=== Generating Datasets ===\n");

        try {
            // === SMALL DATASETS (6-10 vertices) ===
            System.out.println("--- Small Datasets ---");

            // Small 1: Simple DAG
            GraphConfig small1 = generator.generateDAG(8, 0.3, 1, 10);
            generator.saveToFile(small1, "data/small/small1_dag.json");

            // Small 2: Graph with one cycle
            GraphConfig small2 = generator.generateGraph(7, 0.25, 1, 8, true);
            generator.saveToFile(small2, "data/small/small2_cycle.json");

            // Small 3: Multiple small SCCs
            GraphConfig small3 = generator.generateMultipleSCCs(3, 2, 3, 0.4, 1, 5);
            generator.saveToFile(small3, "data/small/small3_multi_scc.json");

            // === MEDIUM DATASETS (10-20 vertices) ===
            System.out.println("\n--- Medium Datasets ---");

            // Medium 1: Sparse DAG
            GraphConfig medium1 = generator.generateDAG(15, 0.2, 1, 15);
            generator.saveToFile(medium1, "data/medium/medium1_sparse_dag.json");

            // Medium 2: Dense graph with cycles
            GraphConfig medium2 = generator.generateGraph(12, 0.4, 2, 12, true);
            generator.saveToFile(medium2, "data/medium/medium2_dense_cycles.json");

            // Medium 3: Multiple SCCs with inter-connections
            GraphConfig medium3 = generator.generateMultipleSCCs(4, 3, 5, 0.3, 1, 10);
            generator.saveToFile(medium3, "data/medium/medium3_scc_connected.json");

            // === LARGE DATASETS (20-50 vertices) ===
            System.out.println("\n--- Large Datasets ---");

            // Large 1: Large sparse DAG
            GraphConfig large1 = generator.generateDAG(35, 0.15, 1, 20);
            generator.saveToFile(large1, "data/large/large1_sparse_dag.json");

            // Large 2: Large dense graph with many cycles
            GraphConfig large2 = generator.generateGraph(30, 0.25, 1, 15, true);
            generator.saveToFile(large2, "data/large/large2_dense_cycles.json");

            // Large 3: Complex SCC structure
            GraphConfig large3 = generator.generateMultipleSCCs(8, 3, 7, 0.2, 1, 12);
            generator.saveToFile(large3, "data/large/large3_complex_scc.json");

            System.out.println("\n=== Dataset Generation Complete ===");
            System.out.println("Total datasets created: 9");
            System.out.println("\nDataset Summary:");
            System.out.println("Small (6-10 vertices): 3 datasets");
            System.out.println("  - small1_dag.json: Simple DAG");
            System.out.println("  - small2_cycle.json: Graph with cycle");
            System.out.println("  - small3_multi_scc.json: Multiple SCCs");
            System.out.println("\nMedium (10-20 vertices): 3 datasets");
            System.out.println("  - medium1_sparse_dag.json: Sparse DAG");
            System.out.println("  - medium2_dense_cycles.json: Dense with cycles");
            System.out.println("  - medium3_scc_connected.json: Connected SCCs");
            System.out.println("\nLarge (20-50 vertices): 3 datasets");
            System.out.println("  - large1_sparse_dag.json: Large sparse DAG");
            System.out.println("  - large2_dense_cycles.json: Dense with cycles");
            System.out.println("  - large3_complex_scc.json: Complex SCC structure");

        } catch (IOException e) {
            System.err.println("Error generating datasets: " + e.getMessage());
            e.printStackTrace();
        }
    }
}