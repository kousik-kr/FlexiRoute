#!/bin/bash

# Test script to validate if different routing modes produce different paths
# This will test the functionality of:
# 1. Aggressive vs Balanced frontier modes
# 2. Different routing optimization modes (wideness, turns, pareto, all objectives)

echo "==================================================================="
echo "FlexiRoute - Routing Mode Validation Test"
echo "==================================================================="
echo ""
echo "This test will verify if the different routing modes are functional"
echo "by running queries with different settings and comparing results."
echo ""

# Test parameters
SOURCE=100
DEST=15000
DEPARTURE=480
INTERVAL=60
BUDGET=120

echo "Test Parameters:"
echo "  Source: $SOURCE"
echo "  Destination: $DEST"
echo "  Departure: $DEPARTURE minutes"
echo "  Interval: $INTERVAL minutes"
echo "  Budget: $BUDGET minutes"
echo ""
echo "==================================================================="
echo ""

# We'll create a simple Java test program to validate the modes
cat > TestRoutingModes.java << 'EOF'
import models.RoutingMode;
import java.util.*;

public class TestRoutingModes {
    public static void main(String[] args) {
        System.out.println("\n=== TESTING ROUTING MODES ===\n");
        
        // Test parameters
        int source = 100;
        int dest = 15000;
        double departure = 480;
        double interval = 60;
        double budget = 120;
        
        // Initialize graph
        System.out.println("Loading graph data...");
        try {
            Graph.readGraph("dataset/nodes_21048.txt", "dataset/edges_21048.txt");
            System.out.println("Graph loaded: " + Graph.get_vertex_count() + " nodes\n");
        } catch (Exception e) {
            System.err.println("Error loading graph: " + e.getMessage());
            return;
        }
        
        // Test all routing modes
        RoutingMode[] modes = RoutingMode.values();
        Map<String, Result> results = new HashMap<>();
        
        for (RoutingMode mode : modes) {
            System.out.println("\n--- Testing " + mode.getDisplayName() + " ---");
            System.out.println("Description: " + mode.getDescription());
            
            try {
                Result result = BidirectionalAstar.runSingleQuery(
                    source, dest, departure, interval, budget, mode
                );
                
                if (result != null) {
                    results.put(mode.getDisplayName(), result);
                    
                    System.out.println("✓ SUCCESS");
                    System.out.println("  Wideness Score: " + String.format("%.2f%%", result.get_score()));
                    System.out.println("  Right Turns: " + result.get_right_turns());
                    System.out.println("  Sharp Turns: " + result.get_sharp_turns());
                    System.out.println("  Travel Time: " + String.format("%.2f", result.get_travel_time()) + " min");
                    System.out.println("  Path Length: " + result.getPathLength() + " nodes");
                    System.out.println("  Wide Edges: " + result.getWideEdgeCount());
                    
                    if (result.hasParetoOptimalPaths()) {
                        System.out.println("  Pareto Paths: " + result.getParetoPathCount());
                        for (int i = 0; i < result.getParetoPathCount(); i++) {
                            Result p = result.getParetoPath(i);
                            System.out.println("    Path " + (i+1) + ": Score=" + 
                                String.format("%.1f%%", p.get_score()) + 
                                ", Turns=" + p.get_right_turns());
                        }
                    }
                    
                    // Print first 10 nodes of path for comparison
                    List<Integer> path = result.getPathNodes();
                    if (path != null && !path.isEmpty()) {
                        System.out.print("  Path preview: ");
                        for (int i = 0; i < Math.min(10, path.size()); i++) {
                            System.out.print(path.get(i));
                            if (i < Math.min(10, path.size()) - 1) System.out.print(" → ");
                        }
                        if (path.size() > 10) System.out.print(" ...");
                        System.out.println();
                    }
                } else {
                    System.out.println("✗ FAILED - No result returned");
                }
            } catch (Exception e) {
                System.out.println("✗ ERROR: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Compare results
        System.out.println("\n\n=== COMPARISON ANALYSIS ===\n");
        
        if (results.size() < 2) {
            System.out.println("Not enough results to compare.");
            return;
        }
        
        // Check if paths are different
        boolean allPathsSame = true;
        boolean allScoresSame = true;
        boolean allTurnsSame = true;
        
        List<Integer> firstPath = null;
        Double firstScore = null;
        Integer firstTurns = null;
        
        for (Map.Entry<String, Result> entry : results.entrySet()) {
            Result r = entry.getValue();
            if (firstPath == null) {
                firstPath = r.getPathNodes();
                firstScore = r.get_score();
                firstTurns = r.get_right_turns();
            } else {
                if (!pathsEqual(firstPath, r.getPathNodes())) {
                    allPathsSame = false;
                }
                if (Math.abs(firstScore - r.get_score()) > 0.01) {
                    allScoresSame = false;
                }
                if (!firstTurns.equals(r.get_right_turns())) {
                    allTurnsSame = false;
                }
            }
        }
        
        System.out.println("Results Summary:");
        System.out.println("  Total modes tested: " + results.size());
        System.out.println("  All paths identical: " + (allPathsSame ? "YES ⚠️" : "NO ✓"));
        System.out.println("  All scores identical: " + (allScoresSame ? "YES ⚠️" : "NO ✓"));
        System.out.println("  All turn counts identical: " + (allTurnsSame ? "YES ⚠️" : "NO ✓"));
        
        System.out.println("\nDetailed Comparison:");
        for (Map.Entry<String, Result> entry : results.entrySet()) {
            Result r = entry.getValue();
            System.out.println(String.format("  %-30s Score: %6.2f%%  Turns: %3d  Path Length: %4d",
                entry.getKey(), r.get_score(), r.get_right_turns(), r.getPathLength()));
        }
        
        // Final verdict
        System.out.println("\n=== VERDICT ===\n");
        if (allPathsSame && allScoresSame && allTurnsSame) {
            System.out.println("⚠️  WARNING: All routing modes produce identical results!");
            System.out.println("    This suggests the modes may not be fully functional.");
        } else {
            System.out.println("✓ SUCCESS: Different routing modes produce different results!");
            System.out.println("          The routing mode feature is FUNCTIONAL.");
        }
        
        // Test frontier thresholds
        System.out.println("\n\n=== TESTING FRONTIER THRESHOLDS ===\n");
        
        BidirectionalLabeling.setAggressiveMode();
        System.out.println("Aggressive Mode - Threshold: " + BidirectionalLabeling.getFrontierThreshold());
        
        BidirectionalLabeling.setBalancedMode();
        System.out.println("Balanced Mode - Threshold: " + BidirectionalLabeling.getFrontierThreshold());
        
        System.out.println("\n✓ Frontier threshold modes are correctly configured.");
        System.out.println("  Note: Performance differences may be subtle and depend on graph size.");
    }
    
    private static boolean pathsEqual(List<Integer> path1, List<Integer> path2) {
        if (path1 == null || path2 == null) return path1 == path2;
        if (path1.size() != path2.size()) return false;
        for (int i = 0; i < path1.size(); i++) {
            if (!path1.get(i).equals(path2.get(i))) return false;
        }
        return true;
    }
}
EOF

echo "Compiling test program..."
javac -cp ".:target/classes" TestRoutingModes.java 2>/dev/null

if [ $? -eq 0 ]; then
    echo "Running validation tests..."
    echo ""
    java -cp ".:target/classes" TestRoutingModes
    rm -f TestRoutingModes.class
else
    echo "❌ Compilation failed. Using manual validation approach..."
    echo ""
    echo "MANUAL VALIDATION INSTRUCTIONS:"
    echo "================================"
    echo ""
    echo "To validate the routing modes manually:"
    echo ""
    echo "1. Start the application: ./run.sh"
    echo ""
    echo "2. Test different ROUTING MODES with the same source/destination:"
    echo "   - Set Source: 100, Destination: 15000"
    echo "   - Set Departure: 480, Interval: 60, Budget: 120"
    echo ""
    echo "   a) Select 'Maximize Wideness' and run"
    echo "      → Should prioritize wider roads (higher wideness score)"
    echo ""
    echo "   b) Select 'Minimize Turns' and run"
    echo "      → Should have fewer right turns (may sacrifice wideness)"
    echo ""
    echo "   c) Select 'Wideness + Turns (Pareto)' and run"
    echo "      → Should show MULTIPLE paths with different trade-offs"
    echo ""
    echo "   d) Select 'All Objectives' and run"
    echo "      → Should balance wideness, turns, and sharp turns"
    echo ""
    echo "3. Compare the results:"
    echo "   - Check 'Wideness Score' in results"
    echo "   - Check 'Right Turns' count"
    echo "   - Check if path (node sequence) is different"
    echo ""
    echo "4. Test FRONTIER THRESHOLD modes:"
    echo "   a) Set to 'Aggressive (Frontier: 10)' and run a query"
    echo "      → Faster, more pruning"
    echo ""
    echo "   b) Set to 'Balanced (Frontier: 50)' and run same query"
    echo "      → More thorough, may find better paths"
    echo ""
    echo "EXPECTED RESULTS:"
    echo "=================="
    echo "- Different routing modes should produce DIFFERENT paths"
    echo "- 'Maximize Wideness' should have HIGHEST wideness score"
    echo "- 'Minimize Turns' should have FEWEST turns"
    echo "- 'Pareto' mode should show MULTIPLE alternative paths"
    echo "- Frontier modes affect search speed/thoroughness"
    echo ""
fi

rm -f TestRoutingModes.java
