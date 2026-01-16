package managers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import models.QueryResult;

/**
 * 📋 FlexiRoute Query History Manager
 * 
 * Manages query history with:
 * - Automatic log file creation for each query
 * - In-memory history with size limits
 * - Analytics (success rate, average execution time)
 */
public class QueryHistoryManager {
    private final List<QueryResult> history = new ArrayList<>();
    private static final int MAX_HISTORY_SIZE = 100;
    private final LogManager logManager;

    public QueryHistoryManager() {
        this.logManager = LogManager.getInstance();
    }

    /**
     * Add a query result to history and save to log file
     * @return The path to the log file created for this query
     */
    public String addQuery(QueryResult result) {
        // First, save the query to a log file
        String logFilePath = logManager.saveQueryLog(result);
        
        // Create a new QueryResult with the log file path if we got one
        QueryResult resultWithLog = result;
        if (logFilePath != null && result.getLogFilePath() == null) {
            // We need to create a modified version with the log path
            // Since QueryResult is immutable, we rebuild it
            resultWithLog = new QueryResult.Builder()
                .setSourceNode(result.getSourceNode())
                .setDestinationNode(result.getDestinationNode())
                .setDepartureTime(result.getDepartureTime())
                .setIntervalDuration(result.getIntervalDuration())
                .setBudget(result.getBudget())
                .setActualDepartureTime(result.getActualDepartureTime())
                .setScore(result.getScore())
                .setTravelTime(result.getTravelTime())
                .setRightTurns(result.getRightTurns())
                .setSharpTurns(result.getSharpTurns())
                .setPathNodes(result.getPathNodes())
                .setWideEdgeIndices(result.getWideEdgeIndices())
                .setExecutionTimeMs(result.getExecutionTimeMs())
                .setSuccess(result.isSuccess())
                .setErrorMessage(result.getErrorMessage())
                .setTotalDistance(result.getTotalDistance())
                .setOptimalDepartureTime(result.getOptimalDepartureTime())
                .setWideRoadPercentage(result.getWideRoadPercentage())
                .setWideEdgeCount(result.getWideEdgeCount())
                .setLogFilePath(logFilePath)
                .setRoutingMode(result.getRoutingMode())
                .setParetoPaths(result.getParetoPaths())
                .setParetoMetrics(result.getParetoMetrics())
                .build();
        }
        
        history.add(0, resultWithLog); // Add to beginning
        if (history.size() > MAX_HISTORY_SIZE) {
            history.remove(history.size() - 1);
        }
        
        return logFilePath;
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
    
    /**
     * Get the LogManager for direct log operations
     */
    public LogManager getLogManager() {
        return logManager;
    }
}
