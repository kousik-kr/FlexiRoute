package models;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Immutable data class representing a query result with all metrics
 */
public class QueryResult {
    private final int sourceNode;
    private final int destinationNode;
    private final double departureTime;
    private final double intervalDuration;
    private final double budget;
    private final double actualDepartureTime;
    private final double score;
    private final double travelTime;
    private final int rightTurns;
    private final int sharpTurns;
    private final List<Integer> pathNodes;
    private final List<Integer> wideEdgeIndices;
    private final long executionTimeMs;
    private final LocalDateTime timestamp;
    private final boolean success;
    private final String errorMessage;

    private QueryResult(Builder builder) {
        this.sourceNode = builder.sourceNode;
        this.destinationNode = builder.destinationNode;
        this.departureTime = builder.departureTime;
        this.intervalDuration = builder.intervalDuration;
        this.budget = builder.budget;
        this.actualDepartureTime = builder.actualDepartureTime;
        this.score = builder.score;
        this.travelTime = builder.travelTime;
        this.rightTurns = builder.rightTurns;
        this.sharpTurns = builder.sharpTurns;
        this.pathNodes = builder.pathNodes;
        this.wideEdgeIndices = builder.wideEdgeIndices;
        this.executionTimeMs = builder.executionTimeMs;
        this.timestamp = builder.timestamp;
        this.success = builder.success;
        this.errorMessage = builder.errorMessage;
    }

    // Getters
    public int getSourceNode() { return sourceNode; }
    public int getDestinationNode() { return destinationNode; }
    public double getDepartureTime() { return departureTime; }
    public double getIntervalDuration() { return intervalDuration; }
    public double getBudget() { return budget; }
    public double getActualDepartureTime() { return actualDepartureTime; }
    public double getScore() { return score; }
    public double getTravelTime() { return travelTime; }
    public int getRightTurns() { return rightTurns; }
    public int getSharpTurns() { return sharpTurns; }
    public List<Integer> getPathNodes() { return pathNodes; }
    public List<Integer> getWideEdgeIndices() { return wideEdgeIndices; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }

    public static class Builder {
        private int sourceNode;
        private int destinationNode;
        private double departureTime;
        private double intervalDuration;
        private double budget;
        private double actualDepartureTime;
        private double score;
        private double travelTime;
        private int rightTurns;
        private int sharpTurns;
        private List<Integer> pathNodes;
        private List<Integer> wideEdgeIndices;
        private long executionTimeMs;
        private LocalDateTime timestamp = LocalDateTime.now();
        private boolean success = true;
        private String errorMessage = "";

        public Builder setSourceNode(int sourceNode) {
            this.sourceNode = sourceNode;
            return this;
        }

        public Builder setDestinationNode(int destinationNode) {
            this.destinationNode = destinationNode;
            return this;
        }

        public Builder setDepartureTime(double departureTime) {
            this.departureTime = departureTime;
            return this;
        }

        public Builder setIntervalDuration(double intervalDuration) {
            this.intervalDuration = intervalDuration;
            return this;
        }

        public Builder setBudget(double budget) {
            this.budget = budget;
            return this;
        }

        public Builder setActualDepartureTime(double actualDepartureTime) {
            this.actualDepartureTime = actualDepartureTime;
            return this;
        }

        public Builder setScore(double score) {
            this.score = score;
            return this;
        }

        public Builder setTravelTime(double travelTime) {
            this.travelTime = travelTime;
            return this;
        }

        public Builder setRightTurns(int rightTurns) {
            this.rightTurns = rightTurns;
            return this;
        }

        public Builder setSharpTurns(int sharpTurns) {
            this.sharpTurns = sharpTurns;
            return this;
        }

        public Builder setPathNodes(List<Integer> pathNodes) {
            this.pathNodes = pathNodes;
            return this;
        }

        public Builder setWideEdgeIndices(List<Integer> wideEdgeIndices) {
            this.wideEdgeIndices = wideEdgeIndices;
            return this;
        }

        public Builder setExecutionTimeMs(long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
            return this;
        }

        public Builder setSuccess(boolean success) {
            this.success = success;
            return this;
        }

        public Builder setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public QueryResult build() {
            return new QueryResult(this);
        }
    }
}
