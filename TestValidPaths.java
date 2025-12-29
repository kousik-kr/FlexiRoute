/**
 * Test queries with valid connected paths in California dataset
 */
public class TestValidPaths {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Testing Valid Paths in California Dataset ===\n");
        
        // Configure and load graph
        BidirectionalAstar.configureDefaults();
        boolean loaded = BidirectionalAstar.loadGraphFromDisk(null, null);
        
        if (!loaded) {
            System.err.println("Failed to load graph!");
            return;
        }
        
        System.out.println("Graph loaded: " + Graph.get_nodes().size() + " nodes\n");
        
        // Test with connected pairs found by BFS
        System.out.println("=== Testing Short Distance Paths ===\n");
        
        // Very short path - adjacent nodes
        testQuery(1, "Adjacent nodes (0.17 km)", 0, 1, 0, 120.0, 60.0);
        
        // Short paths
        testQuery(2, "Short path (0.49 km)", 0, 6, 0, 120.0, 60.0);
        testQuery(3, "Short path (1.26 km)", 2, 3, 0, 120.0, 60.0);
        testQuery(4, "Medium path (1.54 km)", 1, 2, 0, 120.0, 60.0);
        
        System.out.println("\n=== Testing with Different Departure Times ===\n");
        
        // Test same path at different times
        testQuery(5, "Midnight (off-peak)", 0, 2, 0, 120.0, 60.0);
        testQuery(6, "Morning rush (8:30 AM)", 0, 2, 510, 120.0, 60.0);
        testQuery(7, "Evening rush (5:00 PM)", 0, 2, 1020, 120.0, 60.0);
        testQuery(8, "Noon (off-peak)", 0, 2, 720, 120.0, 60.0);
        
        System.out.println("\n=== Testing with Different Budgets ===\n");
        
        // Test with varying budgets
        testQuery(9, "Tight budget (10 min)", 0, 1, 0, 60.0, 10.0);
        testQuery(10, "Moderate budget (30 min)", 0, 2, 0, 120.0, 30.0);
        testQuery(11, "Large budget (120 min)", 0, 4, 0, 180.0, 120.0);

        runAdditionalPairs();
        
        System.out.println("\n=== Test Complete ===");
    }

    private static void runAdditionalPairs() {
        System.out.println("\n=== Additional Pair Checks ===\n");
        // Wider sampling across the small-ID region; fallback will still produce a path if reachable.
        testQuery(12, "Pair 5 -> 10 (budget 120)", 5, 10, 0, 120.0, 120.0);
        testQuery(13, "Pair 7 -> 12 (budget 90)", 7, 12, 0, 90.0, 90.0);
        testQuery(14, "Pair 10 -> 20 (budget 120)", 10, 20, 0, 120.0, 120.0);
        testQuery(15, "Pair 15 -> 30 (budget 120)", 15, 30, 0, 120.0, 120.0);
        testQuery(16, "Pair 100 -> 200 (budget 150)", 100, 200, 0, 150.0, 150.0);
    }
    
    private static void testQuery(int testNum, String description, 
                                  int source, int dest, 
                                  double departureMin, double intervalMin, double budgetMin) {
        System.out.println("Test " + testNum + ": " + description);
        System.out.println("  Query: " + source + " → " + dest);
        System.out.println("  Departure: " + formatTime(departureMin) + " (minute " + departureMin + ")");
        System.out.println("  Budget: " + budgetMin + " min");
        
        try {
            long startTime = System.currentTimeMillis();
            Result result = BidirectionalAstar.runSingleQuery(source, dest, departureMin, intervalMin, budgetMin);
            long duration = System.currentTimeMillis() - startTime;
            
            if (result != null) {
                System.out.println("  ✓ PATH FOUND!");
                System.out.println("    Departure time: " + formatTime(result.get_departureTime()) + 
                                 " (minute " + String.format("%.2f", result.get_departureTime()) + ")");
                System.out.println("    Score: " + String.format("%.6f", result.get_score()));
                System.out.println("    Execution time: " + duration + " ms");
            } else {
                System.out.println("  ✗ No path found");
                System.out.println("    Execution time: " + duration + " ms");
            }
        } catch (Exception e) {
            System.out.println("  ✗ Exception: " + e.getClass().getSimpleName());
            System.out.println("    Message: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static String formatTime(double minutes) {
        int hours = (int)(minutes / 60);
        int mins = (int)(minutes % 60);
        return String.format("%02d:%02d", hours, mins);
    }
}
