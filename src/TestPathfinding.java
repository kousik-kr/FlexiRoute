import java.util.concurrent.ExecutionException;

/**
 * Test runner to execute a hardcoded query and compare with Python output.
 */
public class TestPathfinding {
    public static void main(String[] args) throws Exception {
        // Hardcoded test input (same as Python)
        int source = 72594;
        int destination = 72541;
        double departureTime = 450;
        double interval = 360;
        double budget = 5.0;  // Budget constraint
        
        System.out.println("Java Wide-Path Test");
        System.out.println("============================================================");
        System.out.println("Input:");
        System.out.println("  Source: " + source);
        System.out.println("  Destination: " + destination);
        System.out.println("  Departure time: " + departureTime);
        System.out.println("  Interval: " + interval);
        System.out.println("  Budget: " + budget);
        System.out.println("============================================================");
        
        // Configure and load graph
        BidirectionalAstar.setConfiguredGraphDataDir("C:\\Users\\kousi\\eclipse-workspace\\Wide-Path\\");
        BidirectionalAstar.configureDefaults();
        System.out.println("Loading graph...");
        boolean loaded = BidirectionalAstar.loadGraphFromDisk(null, null);
        
        if (!loaded) {
            System.err.println("Failed to load graph!");
            return;
        }
        
        int nodeCount = Graph.get_vertex_count();
        System.out.println("Graph loaded: " + nodeCount + " nodes");
        
        // Initialize runtime for memory tracking
        BidirectionalAstar.runtime = Runtime.getRuntime();
        BidirectionalAstar.setIntervalDuration(interval);
        
        // Create and execute query
        System.out.println("\nComputing path from " + source + " to " + destination + "...");
        
        try {
            Result result = BidirectionalAstar.runSingleQuery(source, destination, departureTime, interval, budget);
            
            // Output results
            System.out.println("\n============================================================");
            System.out.println("RESULTS:");
            System.out.println("============================================================");
            
            if (result == null) {
                System.out.println("No path found.");
            } else {
                System.out.println("Departure time: " + result.get_departureTime());
                System.out.println("Score: " + result.get_score());
                System.out.println("Travel time: " + result.get_travel_time());
                System.out.println("Right turns: " + result.get_right_turns());
                
                if (result.get_pathNodes() != null && !result.get_pathNodes().isEmpty()) {
                    System.out.println("Number of nodes in path: " + result.get_pathNodes().size());
                    System.out.println("\nPath nodes:");
                    java.util.List<Integer> path = result.get_pathNodes();
                    for (int i = 0; i < path.size(); i += 10) {
                        int end = Math.min(i + 10, path.size());
                        for (int j = i; j < end; j++) {
                            System.out.print(path.get(j) + " ");
                        }
                        System.out.println();
                    }
                }
            }
            System.out.println("============================================================");
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error during query execution: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
