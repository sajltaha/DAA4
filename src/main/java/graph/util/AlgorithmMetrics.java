package graph.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of Metrics interface for tracking algorithm performance.
 */
public class AlgorithmMetrics implements Metrics {

    private long startTime;
    private long endTime;
    private final Map<String, Integer> counters;

    public AlgorithmMetrics() {
        this.counters = new HashMap<>();
        this.startTime = 0;
        this.endTime = 0;
    }

    @Override
    public void startTimer() {
        this.startTime = System.nanoTime();
    }

    @Override
    public void stopTimer() {
        this.endTime = System.nanoTime();
    }

    @Override
    public long getExecutionTime() {
        return endTime - startTime;
    }

    @Override
    public double getExecutionTimeMs() {
        return (endTime - startTime) / 1_000_000.0;
    }

    @Override
    public void incrementCounter(String counterName) {
        counters.put(counterName, counters.getOrDefault(counterName, 0) + 1);
    }

    @Override
    public void incrementCounter(String counterName, int value) {
        counters.put(counterName, counters.getOrDefault(counterName, 0) + value);
    }

    @Override
    public int getCounter(String counterName) {
        return counters.getOrDefault(counterName, 0);
    }

    @Override
    public void reset() {
        counters.clear();
        startTime = 0;
        endTime = 0;
    }

    @Override
    public void printMetrics() {
        System.out.println(getMetricsReport());
    }

    @Override
    public String getMetricsReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Algorithm Metrics ===\n");
        sb.append(String.format("Execution Time: %.3f ms (%.0f ns)\n",
                getExecutionTimeMs(), (double) getExecutionTime()));
        sb.append("Counters:\n");
        counters.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> sb.append(String.format("  %s: %d\n",
                        entry.getKey(), entry.getValue())));
        return sb.toString();
    }
}