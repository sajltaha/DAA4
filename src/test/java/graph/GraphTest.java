package graph;

import graph.util.Graph;
import graph.util.GraphLoader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Graph and GraphLoader utilities.
 */
class GraphTest {

    @Test
    void testGraphCreation() {
        Graph graph = new Graph(5, true, "edge");

        assertEquals(5, graph.getVertexCount());
        assertTrue(graph.isDirected());
        assertEquals("edge", graph.getWeightModel());
        assertEquals(0, graph.getEdgeCount());
    }

    @Test
    void testAddEdgeDirected() {
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 5);
        graph.addEdge(1, 2, 3);

        assertEquals(2, graph.getEdgeCount());

        List<Graph.Edge> edges0 = graph.getEdges(0);
        assertEquals(1, edges0.size());
        assertEquals(1, edges0.get(0).to);
        assertEquals(5, edges0.get(0).weight);
    }

    @Test
    void testAddEdgeUndirected() {
        Graph graph = new Graph(3, false, "edge");
        graph.addEdge(0, 1, 5);

        assertEquals(1, graph.getEdgeCount(), "Undirected edge counts as 1");

        // Both directions should exist
        assertEquals(1, graph.getEdges(0).size());
        assertEquals(1, graph.getEdges(1).size());
    }

    @Test
    void testReverseGraph() {
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 5);
        graph.addEdge(1, 2, 3);

        Graph reversed = graph.reverse();

        assertEquals(graph.getVertexCount(), reversed.getVertexCount());
        assertEquals(graph.getEdgeCount(), reversed.getEdgeCount());

        // 0->1 becomes 1->0
        assertEquals(1, reversed.getEdges(1).size());
        assertEquals(0, reversed.getEdges(1).get(0).to);
    }

    @Test
    void testLoadFromJson() throws IOException {
        // Test loading the default tasks.json file
        GraphLoader.GraphData data = GraphLoader.loadFromJson("data/tasks.json");

        assertNotNull(data);
        assertNotNull(data.graph);
        assertEquals(8, data.graph.getVertexCount());
        assertEquals(4, data.source);
        assertTrue(data.graph.isDirected());
    }

    @Test
    void testEmptyGraph() {
        Graph graph = new Graph(5, true, "edge");

        for (int i = 0; i < 5; i++) {
            assertTrue(graph.getEdges(i).isEmpty());
        }
    }

    @Test
    void testMultipleEdgesToSameVertex() {
        Graph graph = new Graph(2, true, "edge");
        graph.addEdge(0, 1, 5);
        graph.addEdge(0, 1, 3);

        // Should allow multiple edges
        assertEquals(2, graph.getEdges(0).size());
        assertEquals(2, graph.getEdgeCount());
    }

    @Test
    void testSelfLoop() {
        Graph graph = new Graph(2, true, "edge");
        graph.addEdge(0, 0, 1);

        assertEquals(1, graph.getEdgeCount());
        assertEquals(1, graph.getEdges(0).size());
        assertEquals(0, graph.getEdges(0).get(0).to);
    }
}