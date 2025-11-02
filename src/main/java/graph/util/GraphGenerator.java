package graph.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Generator for creating test graph datasets with various properties.
 */
public class GraphGenerator {

    private final Random random;

    public GraphGenerator() {
        this.random = new Random(42); // Fixed seed for reproducibility
    }

    public GraphGenerator(long seed) {
        this.random = new Random(seed);
    }

    /**
     * Generate a random directed graph.
     * @param n number of vertices
     * @param density edge density (0.0 to 1.0)
     * @param minWeight minimum edge weight
     * @param maxWeight maximum edge weight
     * @param ensureCycle if true, ensure graph has at least one cycle
     * @return generated graph configuration
     */
    public GraphConfig generateGraph(int n, double density, int minWeight, int maxWeight, boolean ensureCycle) {
        GraphConfig config = new GraphConfig();
        config.directed = true;
        config.n = n;
        config.edges = new ArrayList<>();
        config.weight_model = "edge";
        config.source = random.nextInt(n);

        Set<String> edgeSet = new HashSet<>();

        if (ensureCycle) {
            // Create at least one cycle
            int cycleSize = 2 + random.nextInt(Math.min(4, n - 1));
            List<Integer> cycleVertices = new ArrayList<>();

            for (int i = 0; i < cycleSize; i++) {
                cycleVertices.add(random.nextInt(n));
            }

            // Create cycle
            for (int i = 0; i < cycleSize; i++) {
                int u = cycleVertices.get(i);
                int v = cycleVertices.get((i + 1) % cycleSize);
                int w = minWeight + random.nextInt(maxWeight - minWeight + 1);

                String edgeKey = u + "->" + v;
                if (!edgeSet.contains(edgeKey)) {
                    config.edges.add(new EdgeConfig(u, v, w));
                    edgeSet.add(edgeKey);
                }
            }
        }

        // Add random edges based on density
        int maxEdges = n * (n - 1); // For directed graph
        int targetEdges = (int) (maxEdges * density);

        while (config.edges.size() < targetEdges) {
            int u = random.nextInt(n);
            int v = random.nextInt(n);

            if (u != v) { // No self-loops
                String edgeKey = u + "->" + v;
                if (!edgeSet.contains(edgeKey)) {
                    int w = minWeight + random.nextInt(maxWeight - minWeight + 1);
                    config.edges.add(new EdgeConfig(u, v, w));
                    edgeSet.add(edgeKey);
                }
            }
        }

        return config;
    }

    /**
     * Generate a DAG (no cycles).
     * @param n number of vertices
     * @param density edge density
     * @param minWeight minimum weight
     * @param maxWeight maximum weight
     * @return generated DAG configuration
     */
    public GraphConfig generateDAG(int n, double density, int minWeight, int maxWeight) {
        GraphConfig config = new GraphConfig();
        config.directed = true;
        config.n = n;
        config.edges = new ArrayList<>();
        config.weight_model = "edge";
        config.source = 0; // Start from first vertex

        // Create edges only from lower to higher numbered vertices (ensures DAG)
        List<EdgeConfig> possibleEdges = new ArrayList<>();
        for (int u = 0; u < n; u++) {
            for (int v = u + 1; v < n; v++) {
                int w = minWeight + random.nextInt(maxWeight - minWeight + 1);
                possibleEdges.add(new EdgeConfig(u, v, w));
            }
        }

        // Shuffle and select based on density
        Collections.shuffle(possibleEdges, random);
        int targetEdges = (int) (possibleEdges.size() * density);

        for (int i = 0; i < Math.min(targetEdges, possibleEdges.size()); i++) {
            config.edges.add(possibleEdges.get(i));
        }

        return config;
    }

    /**
     * Generate a graph with multiple SCCs.
     * @param numSCCs number of SCCs to create
     * @param minSCCSize minimum SCC size
     * @param maxSCCSize maximum SCC size
     * @param interSCCDensity density of edges between SCCs
     * @param minWeight minimum weight
     * @param maxWeight maximum weight
     * @return generated graph configuration
     */
    public GraphConfig generateMultipleSCCs(int numSCCs, int minSCCSize, int maxSCCSize,
                                            double interSCCDensity, int minWeight, int maxWeight) {
        GraphConfig config = new GraphConfig();
        config.directed = true;
        config.edges = new ArrayList<>();
        config.weight_model = "edge";

        List<List<Integer>> sccs = new ArrayList<>();
        int currentVertex = 0;

        // Create each SCC
        for (int i = 0; i < numSCCs; i++) {
            int sccSize = minSCCSize + random.nextInt(maxSCCSize - minSCCSize + 1);
            List<Integer> scc = new ArrayList<>();

            for (int j = 0; j < sccSize; j++) {
                scc.add(currentVertex++);
            }
            sccs.add(scc);

            // Create cycle within SCC
            for (int j = 0; j < sccSize; j++) {
                int u = scc.get(j);
                int v = scc.get((j + 1) % sccSize);
                int w = minWeight + random.nextInt(maxWeight - minWeight + 1);
                config.edges.add(new EdgeConfig(u, v, w));
            }

            // Add some random edges within SCC
            for (int j = 0; j < sccSize; j++) {
                if (random.nextDouble() < 0.3) {
                    int u = scc.get(j);
                    int v = scc.get(random.nextInt(sccSize));
                    if (u != v) {
                        int w = minWeight + random.nextInt(maxWeight - minWeight + 1);
                        config.edges.add(new EdgeConfig(u, v, w));
                    }
                }
            }
        }

        config.n = currentVertex;
        config.source = sccs.get(0).get(0);

        // Add edges between SCCs
        for (int i = 0; i < numSCCs; i++) {
            for (int j = 0; j < numSCCs; j++) {
                if (i != j && random.nextDouble() < interSCCDensity) {
                    int u = sccs.get(i).get(random.nextInt(sccs.get(i).size()));
                    int v = sccs.get(j).get(random.nextInt(sccs.get(j).size()));
                    int w = minWeight + random.nextInt(maxWeight - minWeight + 1);
                    config.edges.add(new EdgeConfig(u, v, w));
                }
            }
        }

        return config;
    }

    /**
     * Save graph configuration to JSON file.
     * @param config graph configuration
     * @param filename output filename
     * @throws IOException if file cannot be written
     */
    public void saveToFile(GraphConfig config, String filename) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(config, writer);
        }
        System.out.println("Generated: " + filename +
                " (n=" + config.n + ", edges=" + config.edges.size() + ")");
    }

    /**
     * Configuration class for JSON serialization.
     */
    public static class GraphConfig {
        public boolean directed;
        public int n;
        public List<EdgeConfig> edges;
        public int source;
        public String weight_model;
    }

    /**
     * Edge configuration for JSON serialization.
     */
    public static class EdgeConfig {
        public int u;
        public int v;
        public int w;

        public EdgeConfig(int u, int v, int w) {
            this.u = u;
            this.v = v;
            this.w = w;
        }
    }
}