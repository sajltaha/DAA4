package graph.util;

/**
 * Interface for tracking algorithm performance metrics.
 * Includes operation counters and execution time.
 */
public interface Metrics {

    /**
     * Start timing the algorithm execution.
     */
    void startTimer();

    /**
     * Stop timing and record the execution time.
     */
    void stopTimer();

    /**
     * Get the execution time in nanoseconds.
     * @return execution time in nanoseconds
     */
    long getExecutionTime();

    /**
     * Get the execution time in milliseconds.
     * @return execution time in milliseconds
     */
    double getExecutionTimeMs();

    /**
     * Increment a specific counter by name.
     * @param counterName name of the counter
     */
    void incrementCounter(String counterName);

    /**
     * Increment a specific counter by a given value.
     * @param counterName name of the counter
     * @param value value to add
     */
    void incrementCounter(String counterName, int value);

    /**
     * Get the value of a specific counter.
     * @param counterName name of the counter
     * @return counter value
     */
    int getCounter(String counterName);

    /**
     * Reset all metrics.
     */
    void reset();

    /**
     * Print all metrics to console.
     */
    void printMetrics();

    /**
     * Get a formatted string of all metrics.
     * @return formatted metrics string
     */
    String getMetricsReport();
}
