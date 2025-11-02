# Smart City Graph Algorithms

Assignment 4: Design and Analysis of Algorithms

## Overview

This project implements graph algorithms for smart city task scheduling:
1. **Strongly Connected Components (SCC)** - Tarjan's algorithm
2. **Topological Sorting** - for DAG ordering
3. **Shortest/Longest Paths in DAG** - for critical path analysis

## Building

```bash
mvn clean compile
```

## Running Tests

```bash
mvn test
```

## Running the Application

```bash
mvn exec:java -Dexec.mainClass="graph.Main"
```

## Dataset Format

Input graphs are in JSON format:
```json
{
  "directed": true,
  "n": 8,
  "edges": [
    {"u": 0, "v": 1, "w": 3}
  ],
  "source": 4,
  "weight_model": "edge"
}
```

- `directed`: whether graph is directed
- `n`: number of vertices (0 to n-1)
- `edges`: list of edges with source (u), destination (v), weight (w)
- `source`: source vertex for shortest path algorithms
- `weight_model`: "edge" or "node" (determines weight interpretation)

## Algorithms

### 1. Strongly Connected Components (SCC)
- Algorithm: Tarjan's algorithm
- Time Complexity: O(V + E)
- Outputs: List of SCCs and condensation graph (DAG)

### 2. Topological Sort
- Algorithm: DFS-based topological ordering
- Time Complexity: O(V + E)
- Works on: Condensation DAG from SCC

### 3. DAG Shortest/Longest Paths
- Shortest Path: Single-source distances using topological order
- Longest Path: Critical path (for project scheduling)
- Time Complexity: O(V + E)
- Reconstructs optimal path

## Performance Metrics

Each algorithm tracks:
- **Execution time** (nanoseconds)
- **Operation counters** (DFS visits, edge relaxations, etc.)