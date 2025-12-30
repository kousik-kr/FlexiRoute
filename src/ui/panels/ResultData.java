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
    private int departureTime;
    private List<Integer> pathNodes;
    private List<double[]> pathCoordinates;
    private List<Integer> wideEdgeIndices;
    
    public ResultData() {
        this.pathNodes = new ArrayList<>();
        this.pathCoordinates = new ArrayList<>();
        this.wideEdgeIndices = new ArrayList<>();
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
    public ResultData departureTime(int time) { this.departureTime = time; return this; }
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
    
    // Getters
    public int getSource() { return source; }
    public int getDestination() { return destination; }
    public double getBudget() { return budget; }
    public double getExecutionTime() { return executionTime; }
    public boolean isPathFound() { return pathFound; }
    public double getTotalCost() { return totalCost; }
    public int getPathLength() { return pathLength; }
    public int getWideEdgeCount() { return wideEdgeCount; }
    public int getDepartureTime() { return departureTime; }
    public List<Integer> getPathNodes() { return pathNodes; }
    public List<double[]> getPathCoordinates() { return pathCoordinates; }
    public List<Integer> getWideEdgeIndices() { return wideEdgeIndices; }
}
