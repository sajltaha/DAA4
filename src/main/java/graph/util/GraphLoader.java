package graph.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.FileReader;
import java.io.IOException;

/**
 * Utility class for loading graphs from JSON files.
 */
public class GraphLoader {

    /**
     * Load a graph from a JSON file.
     * Expected format:
     * {
     *   "directed": true,
     *   "n": 8,
     *   "edges": [{"u": 0, "v": 1, "w": 3}, ...],
     *   "source": 4,
     *   "weight_model": "edge"
     * }
     *
     * @param filePath path to JSON file
     * @return loaded Graph object
     * @throws IOException if file cannot be read
     */
    public static GraphData loadFromJson(String filePath) throws IOException {
        Gson gson = new Gson();
        JsonObject json;

        try (FileReader reader = new FileReader(filePath)) {
            json = gson.fromJson(reader, JsonObject.class);
        }

        boolean directed = json.get("directed").getAsBoolean();
        int n = json.get("n").getAsInt();
        String weightModel = json.has("weight_model") ?
                json.get("weight_model").getAsString() : "edge";

        Graph graph = new Graph(n, directed, weightModel);

        JsonArray edges = json.getAsJsonArray("edges");
        for (JsonElement edgeElement : edges) {
            JsonObject edge = edgeElement.getAsJsonObject();
            int u = edge.get("u").getAsInt();
            int v = edge.get("v").getAsInt();
            int w = edge.get("w").getAsInt();
            graph.addEdge(u, v, w);
        }

        int source = json.has("source") ? json.get("source").getAsInt() : 0;

        return new GraphData(graph, source);
    }

    /**
     * Container class for graph and source vertex.
     */
    public static class GraphData {
        public final Graph graph;
        public final int source;

        public GraphData(Graph graph, int source) {
            this.graph = graph;
            this.source = source;
        }
    }
}