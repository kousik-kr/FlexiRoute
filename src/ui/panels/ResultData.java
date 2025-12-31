package ui.panels;

import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer object for passing result information to UI panels.
 * This avoids direct dependency on the Result class in the default package.
 */
public class ResultData {
    private int source;
    private int destination;
    private double budget;
    private double executionTime;
    private boolean pathFound;
    private double totalCost;
    private int pathLength;
    private int wideEdgeCount;
    private int rightTurns;
    private int departureTime;
    private double suggestedDepartureTime;  // Departure time suggested by the algorithm
    private List<Integer> pathNodes;
    private List<double[]> pathCoordinates;
    private List<Integer> wideEdgeIndices;
    
    // Pareto optimal paths support
    private List<ResultData> paretoOptimalPaths;
    private String routingModeName;
    private double wideScore;  // Wideness score for this path
    
    public ResultData() {
        this.pathNodes = new ArrayList<>();
        this.pathCoordinates = new ArrayList<>();
        this.wideEdgeIndices = new ArrayList<>();
        this.paretoOptimalPaths = new ArrayList<>();
    }
    
    // Builder pattern for easy construction
    public static ResultData create() {
        return new ResultData();
    }
    
    public ResultData source(int source) { this.source = source; return this; }
    public ResultData destination(int destination) { this.destination = destination; return this; }
    public ResultData budget(double budget) { this.budget = budget; return this; }
    public ResultData executionTime(double time) { this.executionTime = time; return this; }
    public ResultData pathFound(boolean found) { this.pathFound = found; return this; }
    public ResultData totalCost(double cost) { this.totalCost = cost; return this; }
    public ResultData pathLength(int length) { this.pathLength = length; return this; }
    public ResultData wideEdgeCount(int count) { this.wideEdgeCount = count; return this; }
    public ResultData rightTurns(int turns) { this.rightTurns = turns; return this; }
    public ResultData departureTime(int time) { this.departureTime = time; return this; }
    public ResultData suggestedDepartureTime(double time) { this.suggestedDepartureTime = time; return this; }
    public ResultData routingModeName(String name) { this.routingModeName = name; return this; }
    public ResultData wideScore(double score) { this.wideScore = score; return this; }
    public ResultData pathNodes(List<Integer> nodes) { 
        this.pathNodes = nodes != null ? new ArrayList<>(nodes) : new ArrayList<>(); 
        return this; 
    }
    public ResultData pathCoordinates(List<double[]> coords) { 
        this.pathCoordinates = coords != null ? new ArrayList<>(coords) : new ArrayList<>(); 
        return this; 
    }
    public ResultData wideEdgeIndices(List<Integer> indices) { 
        this.wideEdgeIndices = indices != null ? new ArrayList<>(indices) : new ArrayList<>(); 
        return this; 
    }
    public ResultData paretoOptimalPaths(List<ResultData> paths) {
        this.paretoOptimalPaths = paths != null ? new ArrayList<>(paths) : new ArrayList<>();
        return this;
    }
    public void addParetoPath(ResultData path) {
        this.paretoOptimalPaths.add(path);
    }
    
    // Getters
    public int getSource() { return source; }
    public int getDestination() { return destination; }
    public double getBudget() { return budget; }
    public double getExecutionTime() { return executionTime; }
    public boolean isPathFound() { return pathFound; }
    public double getTotalCost() { return totalCost; }
    public int getPathLength() { return pathLength; }
    public int getWideEdgeCount() { return wideEdgeCount; }
    public int getRightTurns() { return rightTurns; }
    public int getDepartureTime() { return departureTime; }
    public double getSuggestedDepartureTime() { return suggestedDepartureTime; }
    public List<Integer> getPathNodes() { return pathNodes; }
    public List<double[]> getPathCoordinates() { return pathCoordinates; }
    public List<Integer> getWideEdgeIndices() { return wideEdgeIndices; }
    public List<ResultData> getParetoOptimalPaths() { return paretoOptimalPaths; }
    public boolean hasParetoOptimalPaths() { return !paretoOptimalPaths.isEmpty(); }
    public int getParetoPathCount() { return paretoOptimalPaths.size(); }
    public String getRoutingModeName() { return routingModeName; }
    public double getWideScore() { return wideScore; }
}
