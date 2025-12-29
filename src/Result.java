import java.util.List;
import java.util.ArrayList;

/**
 * Enhanced Result class for World-Class Wide-Path Navigator
 * Stores query results with comprehensive path information
 */
public class Result {
	// Core fields
	private double departure_time;
	private double score;
	private int right_turns;
	private int sharp_turns;
	private double travel_time;
	private List<Integer> pathNodes;
	private List<Integer> wideEdgeIndices;
	
	// Enhanced fields for world-class UI
	private int source;
	private int destination;
	private int budget;
	private double executionTime;
	private boolean pathFound;
	private double totalCost;
	private List<double[]> pathCoordinates;
	
	public Result(double dep_time, double scr, int turns, int sharpTurns, double travelTime,
			List<Integer> pathNodes, List<Integer> wideEdgeIndices) {
		this.departure_time = dep_time;
		this.score = scr;
		this.right_turns = turns;
		this.sharp_turns = sharpTurns;
		this.travel_time = travelTime;
		this.pathNodes = pathNodes != null ? pathNodes : new ArrayList<>();
		this.wideEdgeIndices = wideEdgeIndices != null ? wideEdgeIndices : new ArrayList<>();
		this.pathFound = pathNodes != null && !pathNodes.isEmpty();
		this.totalCost = travelTime;
	}

	// Original getters
	public double get_departureTime() {
		return this.departure_time;
	}

	public double get_score() {
		return this.score;
	}
	
	public int get_right_turns() {
		return this.right_turns;
	}

	public int get_sharp_turns() {
		return this.sharp_turns;
	}

	public double get_travel_time() {
		return this.travel_time;
	}

	public List<Integer> get_pathNodes() {
		return pathNodes;
	}

	public List<Integer> get_wideEdgeIndices() {
		return wideEdgeIndices;
	}

	public void updateScore(double final_score) {
		this.score = final_score;
	}
	
	// === NEW WORLD-CLASS API ===
	
	// Getters for enhanced UI
	public List<Integer> getPathNodes() {
		return pathNodes;
	}
	
	public List<Integer> getWideEdgeIndices() {
		return wideEdgeIndices;
	}
	
	public int getSource() {
		return source;
	}
	
	public int getDestination() {
		return destination;
	}
	
	public int getBudget() {
		return budget;
	}
	
	public double getExecutionTime() {
		return executionTime;
	}
	
	public boolean isPathFound() {
		return pathFound;
	}
	
	public double getTotalCost() {
		return totalCost;
	}
	
	public int getPathLength() {
		return pathNodes != null ? pathNodes.size() : 0;
	}
	
	public int getWideEdgeCount() {
		return wideEdgeIndices != null ? wideEdgeIndices.size() : 0;
	}
	
	public List<double[]> getPathCoordinates() {
		return pathCoordinates;
	}
	
	// Setters for enhanced UI
	public void setSource(int source) {
		this.source = source;
	}
	
	public void setDestination(int destination) {
		this.destination = destination;
	}
	
	public void setBudget(int budget) {
		this.budget = budget;
	}
	
	public void setExecutionTime(double executionTime) {
		this.executionTime = executionTime;
	}
	
	public void setPathFound(boolean pathFound) {
		this.pathFound = pathFound;
	}
	
	public void setTotalCost(double totalCost) {
		this.totalCost = totalCost;
	}
	
	public void setPathCoordinates(List<double[]> coordinates) {
		this.pathCoordinates = coordinates;
	}
	
	@Override
	public String toString() {
		return String.format("Result{source=%d, dest=%d, nodes=%d, cost=%.2f, wideEdges=%d}",
			source, destination, getPathLength(), totalCost, getWideEdgeCount());
	}
}
