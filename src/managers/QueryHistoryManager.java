package managers;

import models.QueryResult;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages query history with persistence and analytics
 */
public class QueryHistoryManager {
    private final List<QueryResult> history = new ArrayList<>();
    private static final int MAX_HISTORY_SIZE = 100;

    public void addQuery(QueryResult result) {
        history.add(0, result); // Add to beginning
        if (history.size() > MAX_HISTORY_SIZE) {
            history.remove(history.size() - 1);
        }
    }

    public List<QueryResult> getHistory() {
        return new ArrayList<>(history);
    }

    public List<QueryResult> getSuccessfulQueries() {
        return history.stream()
            .filter(QueryResult::isSuccess)
            .collect(Collectors.toList());
    }

    public List<QueryResult> getFailedQueries() {
        return history.stream()
            .filter(q -> !q.isSuccess())
            .collect(Collectors.toList());
    }

    public double getAverageExecutionTime() {
        return history.stream()
            .filter(QueryResult::isSuccess)
            .mapToLong(QueryResult::getExecutionTimeMs)
            .average()
            .orElse(0.0);
    }

    public double getSuccessRate() {
        if (history.isEmpty()) return 0.0;
        long successCount = history.stream().filter(QueryResult::isSuccess).count();
        return (double) successCount / history.size() * 100.0;
    }

    public void clearHistory() {
        history.clear();
    }

    public int getHistorySize() {
        return history.size();
    }
}
