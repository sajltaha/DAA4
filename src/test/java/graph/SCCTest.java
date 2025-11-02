package graph;

import graph.scc.CondensationGraph;
import graph.scc.SCC;
import graph.util.Graph;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Strongly Connected Components algorithm.
 */
class SCCTest {

    @Test
    void testSimpleCycle() {
        // Graph: 0->1->2->0 (one SCC)
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);

        SCC scc = new SCC(graph);
        List<List<Integer>> sccs = scc.findSCCs();

        assertEquals(1, sccs.size(), "Should have 1 SCC");
        assertEquals(3, sccs.get(0).size(), "SCC should contain 3 vertices");
    }

    @Test
    void testDAG() {
        // Graph: 0->1->2 (no cycles, 3 SCCs)
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);

        SCC scc = new SCC(graph);
        List<List<Integer>> sccs = scc.findSCCs();

        assertEquals(3, sccs.size(), "Should have 3 SCCs (each vertex is its own SCC)");
        for (List<Integer> component : sccs) {
            assertEquals(1, component.size(), "Each SCC should have 1 vertex");
        }
    }

    @Test
    void testMultipleSCCs() {
        // Graph: 0->1->2->1, 3->4->3
        // Two SCCs: {1,2} and {3,4}, plus {0}
        Graph graph = new Graph(5, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 1, 1);
        graph.addEdge(3, 4, 1);
        graph.addEdge(4, 3, 1);

        SCC scc = new SCC(graph);
        List<List<Integer>> sccs = scc.findSCCs();

        assertEquals(3, sccs.size(), "Should have 3 SCCs");

        // Check component IDs are assigned
        int[] componentIds = scc.getComponentIds();
        assertNotEquals(componentIds[1], componentIds[0], "Vertices 0 and 1 in different components");
        assertEquals(componentIds[1], componentIds[2], "Vertices 1 and 2 in same component");
        assertEquals(componentIds[3], componentIds[4], "Vertices 3 and 4 in same component");
    }

    @Test
    void testSingleVertex() {
        Graph graph = new Graph(1, true, "edge");

        SCC scc = new SCC(graph);
        List<List<Integer>> sccs = scc.findSCCs();

        assertEquals(1, sccs.size());
        assertEquals(1, sccs.get(0).size());
        assertEquals(0, sccs.get(0).get(0));
    }

    @Test
    void testDisconnectedGraph() {
        // Two separate components: 0->1, 2->3
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(2, 3, 1);

        SCC scc = new SCC(graph);
        List<List<Integer>> sccs = scc.findSCCs();

        assertEquals(4, sccs.size(), "Each vertex should be its own SCC");
    }

    @Test
    void testCondensationIsDAG() {
        // Graph with cycles
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 1, 1);
        graph.addEdge(2, 3, 1);

        SCC scc = new SCC(graph);
        List<List<Integer>> sccs = scc.findSCCs();

        CondensationGraph condensation = new CondensationGraph(graph, sccs, scc.getComponentIds());
        Graph dag = condensation.build();

        assertTrue(condensation.isDAG(), "Condensation graph must be a DAG");
    }

    @Test
    void testMetricsTracking() {
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);

        SCC scc = new SCC(graph);
        scc.findSCCs();

        assertTrue(scc.getMetrics().getCounter("dfs_visits") > 0, "Should track DFS visits");
        assertTrue(scc.getMetrics().getCounter("edge_traversals") > 0, "Should track edge traversals");
        assertTrue(scc.getMetrics().getExecutionTime() > 0, "Should track execution time");
    }
}