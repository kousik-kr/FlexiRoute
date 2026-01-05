import java.util.ArrayList;
import java.util.List;

import models.RoutingMode;

/**
 * Enhanced Result class for FlexiRoute Navigator
 * Stores query results with comprehensive path information
 * Supports both single optimal path and multiple Pareto optimal paths
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
	
	// Enhanced fields for UI
	private int source;
	private int destination;
	private int budget;
	private double executionTime;
	private boolean pathFound;
	private double totalCost;
	private List<double[]> pathCoordinates;
	
	// Pareto optimal paths support
	private List<Result> paretoOptimalPaths;
	private RoutingMode routingMode;
	private int paretoPathIndex = -1; // -1 means this is the main result, >=0 means this is a pareto path
	
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
		this.paretoOptimalPaths = new ArrayList<>();
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
	
	// === NEW API ===
	
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
	
	// === PARETO OPTIMAL PATHS API ===
	
	/**
	 * Add a Pareto optimal path to this result
	 */
	public void addParetoPath(Result path) {
		if (paretoOptimalPaths == null) {
			paretoOptimalPaths = new ArrayList<>();
		}
		path.setParetoPathIndex(paretoOptimalPaths.size());
		paretoOptimalPaths.add(path);
	}
	
	/**
	 * Get all Pareto optimal paths
	 */
	public List<Result> getParetoOptimalPaths() {
		return paretoOptimalPaths != null ? paretoOptimalPaths : new ArrayList<>();
	}
	
	/**
	 * Check if this result contains multiple Pareto optimal paths
	 */
	public boolean hasParetoOptimalPaths() {
		return paretoOptimalPaths != null && !paretoOptimalPaths.isEmpty();
	}
	
	/**
	 * Get the number of Pareto optimal paths
	 */
	public int getParetoPathCount() {
		return paretoOptimalPaths != null ? paretoOptimalPaths.size() : 0;
	}
	
	/**
	 * Get a specific Pareto optimal path by index
	 */
	public Result getParetoPath(int index) {
		if (paretoOptimalPaths != null && index >= 0 && index < paretoOptimalPaths.size()) {
			return paretoOptimalPaths.get(index);
		}
		return null;
	}
	
	public void setParetoPathIndex(int index) {
		this.paretoPathIndex = index;
	}
	
	public int getParetoPathIndex() {
		return paretoPathIndex;
	}
	
	public boolean isParetoPath() {
		return paretoPathIndex >= 0;
	}
	
	public void setRoutingMode(RoutingMode mode) {
		this.routingMode = mode;
	}
	
	public RoutingMode getRoutingMode() {
		return routingMode;
	}
	
	/**
	 * Check if this result dominates another in Pareto sense
	 * A dominates B if A is better or equal in all objectives and strictly better in at least one
	 */
	public boolean dominates(Result other) {
		boolean betterInWideness = this.score >= other.score;
		boolean betterInTurns = this.right_turns <= other.right_turns;
		boolean strictlyBetterInWideness = this.score > other.score;
		boolean strictlyBetterInTurns = this.right_turns < other.right_turns;
		
		return betterInWideness && betterInTurns && (strictlyBetterInWideness || strictlyBetterInTurns);
	}
	
	/**
	 * Get a summary string for Pareto path display
	 */
	public String getParetoSummary() {
		return String.format("Path %d: Score=%.1f%%, Turns=%d, Time=%.1fmin", 
			paretoPathIndex + 1, score, right_turns, travel_time);
	}
	
	@Override
	public String toString() {
		if (hasParetoOptimalPaths()) {
			return String.format("Result{source=%d, dest=%d, paretoCount=%d, mode=%s}",
				source, destination, getParetoPathCount(), routingMode);
		}
		return String.format("Result{source=%d, dest=%d, nodes=%d, score=%.1f%%, turns=%d, cost=%.2f}",
			source, destination, getPathLength(), score, right_turns, totalCost);
	}
}
