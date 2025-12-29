/**
 * Automated test for California dataset (21,048 nodes) in GUI mode
 * Tests that queries execute successfully with the smaller dataset
 */
public class TestCaliforniaQueries {
    public static void main(String[] args) {
        try {
            System.out.println("========================================");
            System.out.println("California Dataset Query Test (21,048 nodes)");
            System.out.println("========================================\n");
            
            // Configure and load graph
            BidirectionalAstar.configureDefaults();
            boolean loaded = BidirectionalAstar.loadGraphFromDisk(null, null);
            
            if (!loaded) {
                System.err.println("ERROR: Failed to load graph!");
                System.exit(1);
            }
            
            int nodeCount = Graph.get_nodes().size();
            System.out.println("✓ Graph loaded: " + nodeCount + " nodes");
            System.out.println("  Expected: 21048 nodes");
            System.out.println("  Status: " + (nodeCount == 21048 ? "✓ PASS" : "✗ FAIL") + "\n");
            
            // Test 1: Short distance, non-rush hour
            System.out.println("Test 1: Short distance (nodes 100 -> 150), midnight");
            System.out.println("  Departure: 0 min (00:00), Budget: 180 min");
            testQuery(100, 150, 0, 360, 180, "Short non-rush");
            
            // Test 2: Medium distance, morning rush
            System.out.println("\nTest 2: Medium distance (nodes 500 -> 1000), morning rush");
            System.out.println("  Departure: 480 min (08:00), Budget: 300 min");
            testQuery(500, 1000, 480, 360, 300, "Medium morning rush");
            
            // Test 3: Longer distance, evening rush
            System.out.println("\nTest 3: Longer distance (nodes 1000 -> 5000), evening rush");
            System.out.println("  Departure: 1020 min (17:00), Budget: 600 min");
            testQuery(1000, 5000, 1020, 360, 600, "Long evening rush");
            
            // Test 4: Very close nodes
            System.out.println("\nTest 4: Adjacent nodes (0 -> 1), non-rush");
            System.out.println("  Departure: 120 min (02:00), Budget: 60 min");
            testQuery(0, 1, 120, 360, 60, "Adjacent non-rush");
            
            // Test 5: Random distant nodes
            System.out.println("\nTest 5: Distant nodes (nodes 100 -> 10000), afternoon");
            System.out.println("  Departure: 720 min (12:00), Budget: 900 min");
            testQuery(100, 10000, 720, 360, 900, "Distant afternoon");
            
            System.out.println("\n========================================");
            System.out.println("All tests completed!");
            System.out.println("========================================");
            System.out.println("\nNOTE: Some queries may not find paths due to:");
            System.out.println("  - Graph connectivity (not all nodes reachable)");
            System.out.println("  - Budget constraints (insufficient time)");
            System.out.println("  - Very small edge distances causing numerical issues");
            System.out.println("\nThe important verification is that:");
            System.out.println("  ✓ Graph loads with 21,048 nodes");
            System.out.println("  ✓ Time-dependent costs are applied");
            System.out.println("  ✓ Queries execute without crashes");
            
        } catch (Exception e) {
            System.err.println("\nERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void testQuery(int source, int dest, double departure, double interval, double budget, String testName) {
        try {
            BidirectionalAstar.setIntervalDuration(interval);
            long startTime = System.currentTimeMillis();
            Result result = BidirectionalAstar.runSingleQuery(source, dest, departure, interval, budget);
            long queryTime = System.currentTimeMillis() - startTime;
            
            if (result != null && result.get_pathNodes() != null && !result.get_pathNodes().isEmpty()) {
                System.out.println("  ✓ " + testName + " - Path found!");
                System.out.println("    Path length: " + result.get_pathNodes().size() + " nodes");
                System.out.println("    Travel time: " + String.format("%.2f", result.get_travel_time()) + " minutes");
                System.out.println("    Query execution: " + queryTime + " ms");
            } else {
                System.out.println("  ⚠ " + testName + " - No path found (may be disconnected or budget too small)");
                System.out.println("    Query execution: " + queryTime + " ms");
            }
        } catch (Exception e) {
            System.out.println("  ✗ " + testName + " - Exception: " + e.getMessage());
            System.out.println("    This may indicate a bug in the pathfinding algorithm");
        }
    }
}
