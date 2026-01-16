package models;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    
    // New enhanced fields
    private final double totalDistance;           // Total physical distance of the path
    private final double optimalDepartureTime;    // Optimal departure time suggested by algorithm
    private final double wideRoadPercentage;      // Percentage of wide road coverage
    private final int wideEdgeCount;              // Count of wide edges
    private final String logFilePath;             // Path to the log file for this query
    private final RoutingMode routingMode;        // Routing mode used for this query
    private final List<List<Integer>> paretoPaths;   // All Pareto optimal paths
    private final List<double[]> paretoMetrics;      // Metrics [wideRoad%, turns, distance] for each Pareto path

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
        
        // New fields
        this.totalDistance = builder.totalDistance;
        this.optimalDepartureTime = builder.optimalDepartureTime;
        this.wideRoadPercentage = builder.wideRoadPercentage;
        this.wideEdgeCount = builder.wideEdgeCount;
        this.logFilePath = builder.logFilePath;
        this.routingMode = builder.routingMode;
        this.paretoPaths = builder.paretoPaths;
        this.paretoMetrics = builder.paretoMetrics;
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
    
    // New enhanced getters
    public double getTotalDistance() { return totalDistance; }
    public double getOptimalDepartureTime() { return optimalDepartureTime; }
    public double getWideRoadPercentage() { return wideRoadPercentage; }
    public int getWideEdgeCount() { return wideEdgeCount; }
    public String getLogFilePath() { return logFilePath; }
    public RoutingMode getRoutingMode() { return routingMode; }
    public List<List<Integer>> getParetoPaths() { return paretoPaths; }
    public List<double[]> getParetoMetrics() { return paretoMetrics; }
    public boolean hasParetoPaths() { return paretoPaths != null && !paretoPaths.isEmpty(); }
    public int getParetoPathCount() { return paretoPaths != null ? paretoPaths.size() : 0; }
    
    /**
     * Format optimal departure time as HH:MM string
     */
    public String getFormattedOptimalDepartureTime() {
        if (optimalDepartureTime <= 0) return "--";
        int hours = (int) (optimalDepartureTime / 60);
        int mins = (int) (optimalDepartureTime % 60);
        return String.format("%02d:%02d", hours, mins);
    }

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
        
        // New enhanced fields
        private double totalDistance;
        private double optimalDepartureTime;
        private double wideRoadPercentage;
        private int wideEdgeCount;
        private String logFilePath;
        private RoutingMode routingMode;
        private List<List<Integer>> paretoPaths = new ArrayList<>();
        private List<double[]> paretoMetrics = new ArrayList<>();

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
        
        // New enhanced setters
        public Builder setTotalDistance(double totalDistance) {
            this.totalDistance = totalDistance;
            return this;
        }
        
        public Builder setOptimalDepartureTime(double optimalDepartureTime) {
            this.optimalDepartureTime = optimalDepartureTime;
            return this;
        }
        
        public Builder setWideRoadPercentage(double wideRoadPercentage) {
            this.wideRoadPercentage = wideRoadPercentage;
            return this;
        }
        
        public Builder setWideEdgeCount(int wideEdgeCount) {
            this.wideEdgeCount = wideEdgeCount;
            return this;
        }
        
        public Builder setLogFilePath(String logFilePath) {
            this.logFilePath = logFilePath;
            return this;
        }
        
        public Builder setRoutingMode(RoutingMode routingMode) {
            this.routingMode = routingMode;
            return this;
        }
        
        public Builder setParetoPaths(List<List<Integer>> paretoPaths) {
            this.paretoPaths = paretoPaths != null ? paretoPaths : new ArrayList<>();
            return this;
        }
        
        public Builder setParetoMetrics(List<double[]> paretoMetrics) {
            this.paretoMetrics = paretoMetrics != null ? paretoMetrics : new ArrayList<>();
            return this;
        }
        
        public Builder addParetoPath(List<Integer> path, double[] metrics) {
            if (path != null) {
                this.paretoPaths.add(path);
                if (metrics != null) {
                    this.paretoMetrics.add(metrics);
                }
            }
            return this;
        }

        public QueryResult build() {
            return new QueryResult(this);
        }
    }
}
