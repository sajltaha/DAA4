package graph;

import graph.topo.TopologicalSort;
import graph.util.Graph;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Topological Sort algorithms.
 */
class TopologicalSortTest {

    @Test
    void testSimpleDAG_DFS() {
        // Graph: 0->1->2
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);

        TopologicalSort topo = new TopologicalSort(graph);
        List<Integer> order = topo.sortDFS();

        assertNotNull(order, "Should return valid order for DAG");
        assertEquals(3, order.size());
        assertTrue(topo.isValidOrder(), "Order should be valid");

        // 0 should come before 1, 1 before 2
        assertTrue(order.indexOf(0) < order.indexOf(1));
        assertTrue(order.indexOf(1) < order.indexOf(2));
    }

    @Test
    void testSimpleDAG_Kahn() {
        // Graph: 0->1->2
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);

        TopologicalSort topo = new TopologicalSort(graph);
        List<Integer> order = topo.sortKahn();

        assertNotNull(order, "Should return valid order for DAG");
        assertEquals(3, order.size());
        assertTrue(topo.isValidOrder(), "Order should be valid");
    }

    @Test
    void testCycleDetection_DFS() {
        // Graph with cycle: 0->1->2->0
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);

        TopologicalSort topo = new TopologicalSort(graph);
        List<Integer> order = topo.sortDFS();

        assertNull(order, "Should return null for graph with cycle");
    }

    @Test
    void testCycleDetection_Kahn() {
        // Graph with cycle: 0->1->2->0
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);

        TopologicalSort topo = new TopologicalSort(graph);
        List<Integer> order = topo.sortKahn();

        assertNull(order, "Should return null for graph with cycle");
    }

    @Test
    void testComplexDAG() {
        // Diamond shape: 0->1, 0->2, 1->3, 2->3
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 2, 1);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 1);

        TopologicalSort topo = new TopologicalSort(graph);
        List<Integer> order = topo.sortDFS();

        assertNotNull(order);
        assertEquals(4, order.size());
        assertTrue(topo.isValidOrder());

        // 0 must come first, 3 must come last
        assertEquals(0, order.get(0));
        assertEquals(3, order.get(3));
    }

    @Test
    void testSingleVertex() {
        Graph graph = new Graph(1, true, "edge");

        TopologicalSort topo = new TopologicalSort(graph);
        List<Integer> order = topo.sortDFS();

        assertNotNull(order);
        assertEquals(1, order.size());
        assertEquals(0, order.get(0));
    }

    @Test
    void testDisconnectedDAG() {
        // Two components: 0->1, 2->3
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(2, 3, 1);

        TopologicalSort topo = new TopologicalSort(graph);
        List<Integer> order = topo.sortDFS();

        assertNotNull(order);
        assertEquals(4, order.size());
        assertTrue(topo.isValidOrder());
    }

    @Test
    void testMetricsTracking() {
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);

        TopologicalSort topo = new TopologicalSort(graph);
        topo.sortKahn();

        assertTrue(topo.getMetrics().getCounter("queue_adds") > 0);
        assertTrue(topo.getMetrics().getCounter("queue_removes") > 0);
        assertTrue(topo.getMetrics().getExecutionTime() > 0);
    }

    @Test
    void testBothAlgorithmsGiveSameResult() {
        Graph graph = new Graph(5, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 2, 1);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 4, 1);

        TopologicalSort topo1 = new TopologicalSort(graph);
        List<Integer> orderDFS = topo1.sortDFS();

        TopologicalSort topo2 = new TopologicalSort(graph);
        List<Integer> orderKahn = topo2.sortKahn();

        assertNotNull(orderDFS);
        assertNotNull(orderKahn);

        // Both should be valid topological orders (not necessarily identical)
        assertTrue(topo1.isValidOrder());
        assertTrue(topo2.isValidOrder());
    }
}