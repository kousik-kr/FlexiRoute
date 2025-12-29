import java.util.concurrent.ExecutionException;

/**
 * Test query execution with California dataset (21,048 nodes)
 */
public class TestGuiQuery {
    public static void main(String[] args) throws Exception {
        System.out.println("=== California Dataset Query Test ===\n");
        
        // Configure and load graph
        BidirectionalAstar.configureDefaults();
        boolean loaded = BidirectionalAstar.loadGraphFromDisk(null, null);
        
        if (!loaded) {
            System.err.println("Failed to load graph!");
            return;
        }
        
        int nodeCount = Graph.get_nodes().size();
        System.out.println("Graph loaded: " + nodeCount + " nodes\n");
        
        // Test queries with different scenarios
        runTestQuery(1, "Short distance", 100, 200, 0, 60.0, 45.0);
        runTestQuery(2, "Medium distance", 500, 1500, 480, 90.0, 60.0); // 8:00 AM rush hour
        runTestQuery(3, "Long distance", 1000, 5000, 600, 120.0, 90.0); // 10:00 AM
        runTestQuery(4, "Adjacent nodes", 0, 1, 0, 30.0, 15.0);
        runTestQuery(5, "Large budget", 2000, 8000, 300, 180.0, 120.0); // 5:00 AM
        
        System.out.println("\n=== Test Complete ===");
    }
    
    private static void runTestQuery(int testNum, String description, 
                                     int source, int dest, 
                                     double departureMin, double intervalMin, double budgetMin) {
        System.out.println("Test " + testNum + ": " + description);
        System.out.println("  Query: " + source + " → " + dest);
        System.out.println("  Departure: " + formatTime(departureMin) + " (minute " + departureMin + ")");
        System.out.println("  Budget: " + budgetMin + " min, Interval: " + intervalMin + " min");
        
        try {
            long startTime = System.currentTimeMillis();
            Result result = BidirectionalAstar.runSingleQuery(source, dest, departureMin, intervalMin, budgetMin);
            long duration = System.currentTimeMillis() - startTime;
            
            if (result != null) {
                System.out.println("  ✓ Path found!");
                System.out.println("    Departure time: " + formatTime(result.get_departureTime()) + 
                                 " (minute " + result.get_departureTime() + ")");
                System.out.println("    Score: " + String.format("%.6f", result.get_score()));
                System.out.println("    Execution time: " + duration + " ms");
            } else {
                System.out.println("  ✗ No path found (within budget/time constraints)");
                System.out.println("    Execution time: " + duration + " ms");
            }
        } catch (Exception e) {
            System.out.println("  ✗ Exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    private static String formatTime(double minutes) {
        int hours = (int)(minutes / 60);
        int mins = (int)(minutes % 60);
        return String.format("%02d:%02d", hours, mins);
    }
}
