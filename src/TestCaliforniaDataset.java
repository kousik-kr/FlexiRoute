/**
 * Test program to verify the California dataset works with FlexRoute algorithm
 */
public class TestCaliforniaDataset {
    public static void main(String[] args) {
        try {
            System.out.println("========================================");
            System.out.println("Testing California Dataset (21,048 nodes)");
            System.out.println("========================================\n");
            
            // Configure and load graph
            System.out.println("Configuring defaults...");
            BidirectionalAstar.configureDefaults();
            
            System.out.println("\nLoading graph from dataset/...");
            boolean loaded = BidirectionalAstar.loadGraphFromDisk(null, null);
            
            if (!loaded) {
                System.err.println("ERROR: Failed to load graph!");
                System.exit(1);
            }
            
            System.out.println("\n✓ Graph loaded successfully!");
            System.out.println("  Nodes: " + Graph.get_nodes().size());
            System.out.println("  Vertex count: " + Graph.get_vertex_count());
            
            // Test queries with different departure times
            System.out.println("\n========================================");
            System.out.println("Running Test Queries");
            System.out.println("========================================\n");
            
            // Test 1: Non-rush hour (midnight) - adjacent nodes
            System.out.println("Test 1: Non-rush hour query (midnight)");
            System.out.println("  Source: 0, Destination: 1");
            System.out.println("  Departure: 0 minutes (00:00)");
            System.out.println("  Budget: 120 minutes");
            BidirectionalAstar.setIntervalDuration(360);
            Result result1 = BidirectionalAstar.runSingleQuery(0, 1, 0, 360, 120);
            printResult(result1, "Non-rush");
            
            // Test 2: Morning rush hour - larger budget
            System.out.println("\nTest 2: Morning rush hour query");
            System.out.println("  Source: 0, Destination: 1");
            System.out.println("  Departure: 480 minutes (08:00 - peak rush)");
            System.out.println("  Budget: 120 minutes");
            Result result2 = BidirectionalAstar.runSingleQuery(0, 1, 480, 360, 120);
            printResult(result2, "Morning rush");
            
            // Test 3: Evening rush hour - larger range
            System.out.println("\nTest 3: Evening rush hour query");
            System.out.println("  Source: 10, Destination: 50");
            System.out.println("  Departure: 1020 minutes (17:00 - peak rush)");
            System.out.println("  Budget: 180 minutes");
            Result result3 = BidirectionalAstar.runSingleQuery(10, 50, 1020, 360, 180);
            printResult(result3, "Evening rush");
            
            System.out.println("\n========================================");
            System.out.println("All tests completed successfully!");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void printResult(Result result, String testName) {
        if (result == null) {
            System.out.println("  ✗ " + testName + ": No path found");
            return;
        }
        
        System.out.println("  ✓ " + testName + " result:");
        System.out.println("    Path found: " + (result.get_pathNodes() != null && !result.get_pathNodes().isEmpty()));
        if (result.get_pathNodes() != null) {
            System.out.println("    Path length: " + result.get_pathNodes().size() + " nodes");
        }
        System.out.println("    Travel time: " + String.format("%.2f", result.get_travel_time()) + " minutes");
        System.out.println("    Score: " + String.format("%.2f", result.get_score()));
    }
}
