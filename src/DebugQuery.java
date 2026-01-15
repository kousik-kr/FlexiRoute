import java.util.*;
import models.RoutingMode;

/**
 * Debug class to test a specific source-destination query
 */
public class DebugQuery {
    
    public static void main(String[] args) {
        int source = 110966;
        int dest = 111706;
        double departureTime = 480.0; // 8:00 AM rush hour
        double interval = 60.0; // 1 hour interval
        
        System.out.println("=== Debug Query ===");
        System.out.println("Source: " + source);
        System.out.println("Destination: " + dest);
        System.out.println("Departure Time: " + departureTime + " minutes (8:00 AM)");
        System.out.println();
        
        // Load dataset
        System.out.println("Loading London dataset...");
        BidirectionalAstar.configureDefaults();
        BidirectionalAstar.loadGraphFromDisk(null, null);
        System.out.println("Graph loaded with " + Graph.get_nodes().size() + " nodes");
        System.out.println();
        
        // Check if nodes exist
        Map<Integer, Node> allNodes = Graph.get_nodes();
        Node srcNode = allNodes.get(source);
        Node dstNode = allNodes.get(dest);
        
        if (srcNode == null) {
            System.out.println("ERROR: Source node " + source + " not found in graph!");
            return;
        }
        if (dstNode == null) {
            System.out.println("ERROR: Destination node " + dest + " not found in graph!");
            return;
        }
        
        System.out.println("Source node found: lat=" + srcNode.get_latitude() + ", lon=" + srcNode.get_longitude());
        System.out.println("Destination node found: lat=" + dstNode.get_latitude() + ", lon=" + dstNode.get_longitude());
        System.out.println("Source feasible: " + srcNode.isFeasible());
        System.out.println("Destination feasible: " + dstNode.isFeasible());
        
        // Check outgoing edges
        Map<Integer, Edge> srcEdges = srcNode.get_outgoing_edges();
        Map<Integer, Edge> dstEdges = dstNode.get_outgoing_edges();
        System.out.println("Source has " + (srcEdges != null ? srcEdges.size() : 0) + " outgoing edges");
        System.out.println("Destination has " + (dstEdges != null ? dstEdges.size() : 0) + " outgoing edges");
        
        // Print some outgoing edges from source
        if (srcEdges != null && !srcEdges.isEmpty()) {
            System.out.println("Source outgoing edges (first 5):");
            int count = 0;
            for (Map.Entry<Integer, Edge> entry : srcEdges.entrySet()) {
                if (count++ >= 5) break;
                Edge e = entry.getValue();
                System.out.println("  -> " + entry.getKey() + " (width=" + e.getBaseWidth() + ")");
            }
        }
        System.out.println();
        
        // Calculate distance
        double latDiff = Math.abs(srcNode.get_latitude() - dstNode.get_latitude());
        double lonDiff = Math.abs(srcNode.get_longitude() - dstNode.get_longitude());
        double approxDistKm = Math.sqrt(latDiff*latDiff + lonDiff*lonDiff) * 111; // rough km
        System.out.println("Approximate straight-line distance: " + String.format("%.2f", approxDistKm) + " km");
        System.out.println();
        
        // Try different budget values
        double[] budgets = {30.0, 60.0, 120.0};
        
        for (double budget : budgets) {
            System.out.println("=== Trying with budget: " + budget + " minutes ===");
            
            try {
                // Run query with WIDENESS_ONLY mode
                Result result = BidirectionalAstar.runSingleQuery(source, dest, departureTime, interval, budget, RoutingMode.WIDENESS_ONLY);
                
                if (result != null && result.isPathFound()) {
                    System.out.println("PATH FOUND!");
                    System.out.println("  Path length: " + result.getPathLength() + " nodes");
                    System.out.println("  Total cost: " + String.format("%.2f", result.getTotalCost()));
                    System.out.println("  Wide edges: " + result.getWideEdgeCount());
                    System.out.println("  Execution time: " + String.format("%.2f", result.getExecutionTime()) + " ms");
                    
                    List<Integer> path = result.getPathNodes();
                    if (path != null && !path.isEmpty()) {
                        System.out.println("  Path: " + path.get(0) + " -> ... -> " + path.get(path.size()-1));
                        System.out.println("  Full path: " + path);
                    }
                    break; // Found a path, stop trying larger budgets
                } else {
                    System.out.println("No path found with this budget");
                    if (result != null) {
                        System.out.println("  Result details: pathFound=" + result.isPathFound());
                    }
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println();
        }
    }
}
