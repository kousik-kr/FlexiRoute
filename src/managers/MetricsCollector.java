package managers;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Collects and provides real-time metrics
 */
public class MetricsCollector {
    private final AtomicLong totalQueriesExecuted = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    private final AtomicInteger successfulQueries = new AtomicInteger(0);
    private final AtomicInteger failedQueries = new AtomicInteger(0);
    private final AtomicLong totalNodesExplored = new AtomicLong(0);

    public void recordQuery(boolean success, long executionTimeMs, int nodesExplored) {
        totalQueriesExecuted.incrementAndGet();
        totalExecutionTime.addAndGet(executionTimeMs);
        if (success) {
            successfulQueries.incrementAndGet();
        } else {
            failedQueries.incrementAndGet();
        }
        totalNodesExplored.addAndGet(nodesExplored);
    }

    public long getTotalQueries() {
        return totalQueriesExecuted.get();
    }

    public double getAverageExecutionTime() {
        long total = totalQueriesExecuted.get();
        return total > 0 ? (double) totalExecutionTime.get() / total : 0.0;
    }

    public double getSuccessRate() {
        long total = totalQueriesExecuted.get();
        return total > 0 ? (double) successfulQueries.get() / total * 100.0 : 0.0;
    }

    public long getSuccessfulQueries() {
        return successfulQueries.get();
    }

    public long getFailedQueries() {
        return failedQueries.get();
    }

    public long getTotalNodesExplored() {
        return totalNodesExplored.get();
    }

    public void reset() {
        totalQueriesExecuted.set(0);
        totalExecutionTime.set(0);
        successfulQueries.set(0);
        failedQueries.set(0);
        totalNodesExplored.set(0);
    }
}
