package graph;

import graph.dagsp.DAGLongestPath;
import graph.dagsp.DAGShortestPath;
import graph.util.Graph;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DAG Shortest and Longest Path algorithms.
 */
class DAGPathTest {

    @Test
    void testShortestPathSimple() {
        // Graph: 0->1(5), 0->2(3), 2->1(1)
        // Shortest 0->1 should be 0->2->1 = 4
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 5);
        graph.addEdge(0, 2, 3);
        graph.addEdge(2, 1, 1);

        DAGShortestPath sp = new DAGShortestPath(graph);
        assertTrue(sp.computeShortestPaths(0));

        assertEquals(0, sp.getDistance(0), "Distance to self should be 0");
        assertEquals(4, sp.getDistance(1), "Shortest path 0->1 should be 4");
        assertEquals(3, sp.getDistance(2), "Shortest path 0->2 should be 3");
    }

    @Test
    void testShortestPathReconstruction() {
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 5);
        graph.addEdge(0, 2, 3);
        graph.addEdge(2, 1, 1);

        DAGShortestPath sp = new DAGShortestPath(graph);
        sp.computeShortestPaths(0);

        List<Integer> path = sp.getPath(1);
        assertNotNull(path);
        assertEquals(3, path.size(), "Path should have 3 vertices");
        assertEquals(0, path.get(0), "Path should start at 0");
        assertEquals(2, path.get(1), "Path should go through 2");
        assertEquals(1, path.get(2), "Path should end at 1");
    }

    @Test
    void testShortestPathUnreachable() {
        // Disconnected: 0->1, 2 isolated
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 5);

        DAGShortestPath sp = new DAGShortestPath(graph);
        sp.computeShortestPaths(0);

        assertTrue(sp.isReachable(1), "Vertex 1 should be reachable");
        assertFalse(sp.isReachable(2), "Vertex 2 should be unreachable");
        assertNull(sp.getPath(2), "No path to unreachable vertex");
    }

    @Test
    void testLongestPathSimple() {
        // Graph: 0->1(5), 0->2(3), 2->1(1)
        // Longest 0->1 should be 0->1 = 5
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 5);
        graph.addEdge(0, 2, 3);
        graph.addEdge(2, 1, 1);

        DAGLongestPath lp = new DAGLongestPath(graph);
        assertTrue(lp.computeLongestPaths(0));

        assertEquals(0, lp.getDistance(0), "Distance to self should be 0");
        assertEquals(5, lp.getDistance(1), "Longest path 0->1 should be 5");
        assertEquals(3, lp.getDistance(2), "Longest path 0->2 should be 3");
    }

    @Test
    void testCriticalPath() {
        // Linear: 0->1(3)->2(4)->3(2)
        // Critical path should be 0->1->2->3 = 9
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 3);
        graph.addEdge(1, 2, 4);
        graph.addEdge(2, 3, 2);

        DAGLongestPath lp = new DAGLongestPath(graph);
        assertTrue(lp.computeCriticalPath());

        DAGLongestPath.CriticalPath cp = lp.getCriticalPath();
        assertEquals(9, cp.length, "Critical path length should be 9");
        assertEquals(4, cp.path.size(), "Critical path should have 4 vertices");
    }

    @Test
    void testCriticalPathDiamond() {
        // Diamond: 0->1(2), 0->2(5), 1->3(3), 2->3(1)
        // Critical path: 0->2->3 = 6
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 2);
        graph.addEdge(0, 2, 5);
        graph.addEdge(1, 3, 3);
        graph.addEdge(2, 3, 1);

        DAGLongestPath lp = new DAGLongestPath(graph);
        assertTrue(lp.computeCriticalPath());

        DAGLongestPath.CriticalPath cp = lp.getCriticalPath();
        assertEquals(6, cp.length, "Critical path length should be 6");
    }

    @Test
    void testSingleVertex() {
        Graph graph = new Graph(1, true, "edge");

        DAGShortestPath sp = new DAGShortestPath(graph);
        assertTrue(sp.computeShortestPaths(0));
        assertEquals(0, sp.getDistance(0));

        DAGLongestPath lp = new DAGLongestPath(graph);
        assertTrue(lp.computeLongestPaths(0));
        assertEquals(0, lp.getDistance(0));
    }

    @Test
    void testCycleRejection() {
        // Graph with cycle: 0->1->2->0
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);

        DAGShortestPath sp = new DAGShortestPath(graph);
        assertFalse(sp.computeShortestPaths(0), "Should reject graph with cycle");

        DAGLongestPath lp = new DAGLongestPath(graph);
        assertFalse(lp.computeLongestPaths(0), "Should reject graph with cycle");
    }

    @Test
    void testMetricsTracking() {
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 5);
        graph.addEdge(1, 2, 3);

        DAGShortestPath sp = new DAGShortestPath(graph);
        sp.computeShortestPaths(0);

        assertTrue(sp.getMetrics().getCounter("edge_relaxations") > 0);
        assertTrue(sp.getMetrics().getExecutionTime() > 0);
    }

    @Test
    void testZeroWeightEdges() {
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 0);
        graph.addEdge(1, 2, 0);

        DAGShortestPath sp = new DAGShortestPath(graph);
        sp.computeShortestPaths(0);

        assertEquals(0, sp.getDistance(1));
        assertEquals(0, sp.getDistance(2));
    }
}