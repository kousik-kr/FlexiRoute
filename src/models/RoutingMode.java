package models;

/**
 * FlexiRoute Routing Modes - User-selectable optimization objectives
 * 
 * Defines the different optimization strategies available in the FlexiRoute navigation system.
 * Users can choose their preferred balance between wideness, turns, and travel time.
 */
public enum RoutingMode {
    
    /**
     * WIDENESS_ONLY: Maximize road wideness within travel time budget
     * - Primary objective: Maximize wideness score
     * - Constraint: Travel time ≤ budget
     * - Use case: Users who prioritize wider roads for comfort/safety
     */
    WIDENESS_ONLY("Maximize Wideness", "Find the widest route within time budget"),
    
    /**
     * MIN_TURNS_ONLY: Minimize right turns within travel time budget
     * - Primary objective: Minimize number of right turns
     * - Constraint: Travel time ≤ budget
     * - Use case: Users who want simpler navigation with fewer turns
     */
    MIN_TURNS_ONLY("Minimize Turns", "Find route with fewest turns within time budget"),
    
    /**
     * WIDENESS_AND_TURNS: Multi-objective - both wideness AND turns (Pareto optimal)
     * - Objectives: Maximize wideness AND minimize right turns
     * - Constraint: Travel time ≤ budget
     * - Returns: All Pareto optimal paths
     * - Use case: Users who want to see trade-offs between wideness and turns
     */
    WIDENESS_AND_TURNS("Wideness + Turns (Pareto)", "Find all Pareto optimal routes balancing wideness and turns"),
    
    /**
     * ALL_OBJECTIVES: Full optimization (original behavior)
     * - Objectives: Maximize wideness, minimize right turns, avoid sharp turns
     * - Constraint: Travel time ≤ budget
     * - Use case: Users who want the best overall route quality
     */
    ALL_OBJECTIVES("All Objectives", "Optimize wideness, turns, and avoid sharp turns");
    
    private final String displayName;
    private final String description;
    
    RoutingMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    /**
     * Check if this mode requires Pareto optimal output (multiple paths)
     */
    public boolean isParetoMode() {
        return this == WIDENESS_AND_TURNS;
    }
    
    /**
     * Check if wideness optimization is enabled
     */
    public boolean optimizeWideness() {
        return this == WIDENESS_ONLY || this == WIDENESS_AND_TURNS || this == ALL_OBJECTIVES;
    }
    
    /**
     * Check if turn minimization is enabled
     */
    public boolean optimizeTurns() {
        return this == MIN_TURNS_ONLY || this == WIDENESS_AND_TURNS || this == ALL_OBJECTIVES;
    }
    
    /**
     * Check if sharp turn avoidance is enabled
     */
    public boolean avoidSharpTurns() {
        return this == ALL_OBJECTIVES;
    }
}
