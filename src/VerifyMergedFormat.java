/**
 * Verify that merged format is loading clusters and edge properties correctly
 */
public class VerifyMergedFormat {
    public static void main(String[] args) {
        try {
            System.out.println("========================================");
            System.out.println("Verifying Merged Format Data Loading");
            System.out.println("========================================\n");
            
            BidirectionalAstar.configureDefaults();
            boolean loaded = BidirectionalAstar.loadGraphFromDisk(null, null);
            
            if (!loaded) {
                System.err.println("ERROR: Failed to load graph!");
                System.exit(1);
            }
            
            System.out.println("\n✓ Graph loaded successfully!");
            System.out.println("  Total nodes: " + Graph.get_nodes().size());
            
            // Check first 10 nodes for cluster assignment
            System.out.println("\n--- Node Cluster Verification ---");
            System.out.println("Checking first 10 nodes for cluster IDs:");
            for (int i = 0; i < 10 && i < Graph.get_nodes().size(); i++) {
                Node node = Graph.get_node(i);
                if (node != null) {
                    System.out.println("  Node " + i + ": clusterId=" + node.getClusterId() + 
                                     ", lat=" + String.format("%.6f", node.get_latitude()) +
                                     ", lon=" + String.format("%.6f", node.get_longitude()));
                }
            }
            
            // Check clusters
            System.out.println("\n--- Cluster Statistics ---");
            System.out.println("Checking if clusters are loaded...");
            int nodesWithClusters = 0;
            for (int i = 0; i < Math.min(100, Graph.get_nodes().size()); i++) {
                Node node = Graph.get_node(i);
                if (node != null && node.getClusterId() != -1) {
                    nodesWithClusters++;
                }
            }
            System.out.println("  Nodes with cluster IDs (first 100): " + nodesWithClusters + "/100");
            
            // Check edges for width and distance properties
            System.out.println("\n--- Edge Properties Verification ---");
            System.out.println("Checking first 5 edges:");
            int edgeCount = 0;
            outerLoop:
            for (int i = 0; i < Graph.get_nodes().size() && edgeCount < 5; i++) {
                Node node = Graph.get_node(i);
                if (node != null && node.get_outgoing_edges() != null) {
                    for (Edge edge : node.get_outgoing_edges().values()) {
                        System.out.println("  Edge " + edge.get_source() + " -> " + edge.get_destination() + ":");
                        System.out.println("    Base width: " + String.format("%.2f", edge.getBaseWidth()));
                        System.out.println("    Rush width: " + String.format("%.2f", edge.getRushWidth()));
                        System.out.println("    Distance: " + String.format("%.6f", edge.get_distance()));
                        System.out.println("    Lowest cost: " + String.format("%.6f", edge.getLowestCost()));
                        edgeCount++;
                        if (edgeCount >= 5) break outerLoop;
                    }
                }
            }
            
            System.out.println("\n========================================");
            System.out.println("Verification Complete!");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
