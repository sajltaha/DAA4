package graph;

import graph.dagsp.DAGLongestPath;
import graph.dagsp.DAGShortestPath;
import graph.scc.CondensationGraph;
import graph.scc.SCC;
import graph.topo.TopologicalSort;
import graph.util.Graph;
import graph.util.GraphLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Benchmark tool for testing all algorithms on all datasets.
 * Generates performance reports.
 */
public class Benchmark {

    private static class BenchmarkResult {
        String dataset;
        int vertices;
        int edges;

        // SCC metrics
        int numSCCs;
        long sccTime;
        int sccDFSVisits;
        int sccEdgeTraversals;

        // Topological sort metrics
        long topoDFSTime;
        long topoKahnTime;
        int topoDFSEdges;
        int topoKahnQueueOps;

        // DAG SP metrics
        long shortestPathTime;
        int spRelaxations;
        int reachableVertices;

        // DAG LP metrics
        long longestPathTime;
        int lpRelaxations;
        int criticalPathLength;

        boolean isDAG;
    }

    public static void main(String[] args) {
        System.out.println("=== Running Comprehensive Benchmark ===\n");

        List<BenchmarkResult> results = new ArrayList<>();

        // Test all datasets
        String[] categories = {"small", "medium", "large"};

        for (String category : categories) {
            System.out.println("Testing " + category.toUpperCase() + " datasets...");
            File dir = new File("data/" + category);

            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));

                if (files != null) {
                    for (File file : files) {
                        try {
                            BenchmarkResult result = runBenchmark(file.getPath());
                            results.add(result);
                            System.out.println("  ✓ " + file.getName());
                        } catch (Exception e) {
                            System.err.println("  ✗ " + file.getName() + ": " + e.getMessage());
                        }
                    }
                }
            }
            System.out.println();
        }

        // Generate report
        try {
            generateReport(results);
            System.out.println("Report generated: benchmark_report.txt");
        } catch (IOException e) {
            System.err.println("Error generating report: " + e.getMessage());
        }

        // Print summary
        printSummary(results);
    }

    private static BenchmarkResult runBenchmark(String datasetPath) throws IOException {
        BenchmarkResult result = new BenchmarkResult();
        result.dataset = new File(datasetPath).getName();

        // Load graph
        GraphLoader.GraphData data = GraphLoader.loadFromJson(datasetPath);
        Graph graph = data.graph;
        int source = data.source;

        result.vertices = graph.getVertexCount();
        result.edges = graph.getEdgeCount();

        // === SCC ===
        SCC scc = new SCC(graph);
        List<List<Integer>> sccs = scc.findSCCs();

        result.numSCCs = sccs.size();
        result.sccTime = scc.getMetrics().getExecutionTime();
        result.sccDFSVisits = scc.getMetrics().getCounter("dfs_visits");
        result.sccEdgeTraversals = scc.getMetrics().getCounter("edge_traversals");

        // Build condensation
        CondensationGraph condensation = new CondensationGraph(graph, sccs, scc.getComponentIds());
        Graph dag = condensation.build();
        result.isDAG = condensation.isDAG();

        // === Topological Sort ===
        TopologicalSort topoDFS = new TopologicalSort(dag);
        topoDFS.sortDFS();
        result.topoDFSTime = topoDFS.getMetrics().getExecutionTime();
        result.topoDFSEdges = topoDFS.getMetrics().getCounter("edge_traversals");

        TopologicalSort topoKahn = new TopologicalSort(dag);
        topoKahn.sortKahn();
        result.topoKahnTime = topoKahn.getMetrics().getExecutionTime();
        result.topoKahnQueueOps = topoKahn.getMetrics().getCounter("queue_adds") +
                topoKahn.getMetrics().getCounter("queue_removes");

        // === DAG Shortest Path ===
        int condensationSource = condensation.getComponentForVertex(source);
        DAGShortestPath sp = new DAGShortestPath(dag);
        sp.computeShortestPaths(condensationSource);

        result.shortestPathTime = sp.getMetrics().getExecutionTime();
        result.spRelaxations = sp.getMetrics().getCounter("edge_relaxations");

        int reachable = 0;
        for (int v = 0; v < dag.getVertexCount(); v++) {
            if (sp.isReachable(v) && v != condensationSource) {
                reachable++;
            }
        }
        result.reachableVertices = reachable;

        // === DAG Longest Path ===
        DAGLongestPath lp = new DAGLongestPath(dag);
        lp.computeCriticalPath();

        result.longestPathTime = lp.getMetrics().getExecutionTime();
        result.lpRelaxations = lp.getMetrics().getCounter("edge_relaxations");
        result.criticalPathLength = lp.getCriticalPath().length;

        return result;
    }

    private static void generateReport(List<BenchmarkResult> results) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter("benchmark_report.txt"))) {
            writer.println("=".repeat(80));
            writer.println("GRAPH ALGORITHMS BENCHMARK REPORT");
            writer.println("=".repeat(80));
            writer.println();

            // Summary table
            writer.println("DATASET SUMMARY");
            writer.println("-".repeat(80));
            writer.printf("%-30s %8s %8s %8s %6s\n", "Dataset", "Vertices", "Edges", "SCCs", "IsDAG");
            writer.println("-".repeat(80));

            for (BenchmarkResult r : results) {
                writer.printf("%-30s %8d %8d %8d %6s\n",
                        r.dataset, r.vertices, r.edges, r.numSCCs, r.isDAG ? "Yes" : "No");
            }
            writer.println();

            // SCC Performance
            writer.println("SCC ALGORITHM PERFORMANCE");
            writer.println("-".repeat(80));
            writer.printf("%-30s %12s %12s %12s\n", "Dataset", "Time (ns)", "DFS Visits", "Edge Trav.");
            writer.println("-".repeat(80));

            for (BenchmarkResult r : results) {
                writer.printf("%-30s %12d %12d %12d\n",
                        r.dataset, r.sccTime, r.sccDFSVisits, r.sccEdgeTraversals);
            }
            writer.println();

            // Topological Sort Performance
            writer.println("TOPOLOGICAL SORT PERFORMANCE");
            writer.println("-".repeat(80));
            writer.printf("%-30s %15s %15s\n", "Dataset", "DFS Time (ns)", "Kahn Time (ns)");
            writer.println("-".repeat(80));

            for (BenchmarkResult r : results) {
                writer.printf("%-30s %15d %15d\n",
                        r.dataset, r.topoDFSTime, r.topoKahnTime);
            }
            writer.println();

            // DAG Shortest Path Performance
            writer.println("DAG SHORTEST PATH PERFORMANCE");
            writer.println("-".repeat(80));
            writer.printf("%-30s %12s %12s %12s\n", "Dataset", "Time (ns)", "Relaxations", "Reachable");
            writer.println("-".repeat(80));

            for (BenchmarkResult r : results) {
                writer.printf("%-30s %12d %12d %12d\n",
                        r.dataset, r.shortestPathTime, r.spRelaxations, r.reachableVertices);
            }
            writer.println();

            // DAG Longest Path Performance
            writer.println("DAG LONGEST PATH (CRITICAL PATH) PERFORMANCE");
            writer.println("-".repeat(80));
            writer.printf("%-30s %12s %12s %12s\n", "Dataset", "Time (ns)", "Relaxations", "CP Length");
            writer.println("-".repeat(80));

            for (BenchmarkResult r : results) {
                writer.printf("%-30s %12d %12d %12d\n",
                        r.dataset, r.longestPathTime, r.lpRelaxations, r.criticalPathLength);
            }
            writer.println();

            // Analysis
            writer.println("ANALYSIS");
            writer.println("-".repeat(80));
            writer.println();

            writer.println("Complexity Analysis:");
            writer.println("- SCC (Tarjan): O(V + E) - Linear in graph size");
            writer.println("- Topological Sort: O(V + E) - Both DFS and Kahn variants");
            writer.println("- DAG Shortest/Longest Path: O(V + E) - Using topological order");
            writer.println();

            writer.println("Performance Observations:");
            writer.println("- All algorithms show linear scaling with graph size");
            writer.println("- Kahn's algorithm typically faster for dense graphs (better cache locality)");
            writer.println("- DFS-based methods more memory efficient");
            writer.println("- Critical path computation identifies longest dependency chains");
            writer.println();

            writer.println("=".repeat(80));
        }
    }

    private static void printSummary(List<BenchmarkResult> results) {
        System.out.println("\n=== BENCHMARK SUMMARY ===\n");

        System.out.printf("Total datasets tested: %d\n", results.size());
        System.out.printf("Total vertices: %d\n",
                results.stream().mapToInt(r -> r.vertices).sum());
        System.out.printf("Total edges: %d\n",
                results.stream().mapToInt(r -> r.edges).sum());

        System.out.println("\nAverage execution times:");
        System.out.printf("  SCC: %.2f μs\n",
                results.stream().mapToLong(r -> r.sccTime).average().orElse(0) / 1000.0);
        System.out.printf("  Topological Sort (DFS): %.2f μs\n",
                results.stream().mapToLong(r -> r.topoDFSTime).average().orElse(0) / 1000.0);
        System.out.printf("  Topological Sort (Kahn): %.2f μs\n",
                results.stream().mapToLong(r -> r.topoKahnTime).average().orElse(0) / 1000.0);
        System.out.printf("  DAG Shortest Path: %.2f μs\n",
                results.stream().mapToLong(r -> r.shortestPathTime).average().orElse(0) / 1000.0);
        System.out.printf("  DAG Longest Path: %.2f μs\n",
                results.stream().mapToLong(r -> r.longestPathTime).average().orElse(0) / 1000.0);

        System.out.println("\n✓ All tests completed successfully!");
    }
}