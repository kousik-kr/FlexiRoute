import java.util.*;

/**
 * Find connected node pairs in the California dataset for valid query testing
 */
public class FindConnectedNodes {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Finding Connected Nodes in California Dataset ===\n");
        
        // Load graph
        BidirectionalAstar.configureDefaults();
        boolean loaded = BidirectionalAstar.loadGraphFromDisk(null, null);
        
        if (!loaded) {
            System.err.println("Failed to load graph!");
            return;
        }
        
        int nodeCount = Graph.get_nodes().size();
        System.out.println("Graph loaded: " + nodeCount + " nodes\n");
        
        // Analyze graph connectivity
        System.out.println("=== Graph Connectivity Analysis ===");
        analyzeConnectivity();
        
        System.out.println("\n=== Finding Sample Valid Paths ===");
        findSamplePaths(10);
    }
    
    private static void analyzeConnectivity() {
        Map<Integer, Node> nodes = Graph.get_nodes();
        int totalNodes = nodes.size();
        int nodesWithOutgoing = 0;
        int nodesWithIncoming = 0;
        int isolatedNodes = 0;
        
        for (Map.Entry<Integer, Node> entry : nodes.entrySet()) {
            Node node = entry.getValue();
            boolean hasOutgoing = !node.get_outgoing_edges().isEmpty();
            boolean hasIncoming = !node.get_incoming_edges().isEmpty();
            
            if (hasOutgoing) nodesWithOutgoing++;
            if (hasIncoming) nodesWithIncoming++;
            if (!hasOutgoing && !hasIncoming) isolatedNodes++;
        }
        
        System.out.println("Total nodes: " + totalNodes);
        System.out.println("Nodes with outgoing edges: " + nodesWithOutgoing);
        System.out.println("Nodes with incoming edges: " + nodesWithIncoming);
        System.out.println("Isolated nodes (no edges): " + isolatedNodes);
    }
    
    private static void findSamplePaths(int maxSamples) {
        Map<Integer, Node> nodes = Graph.get_nodes();
        List<int[]> validPairs = new ArrayList<>();
        int checked = 0;
        
        // Find nodes with outgoing edges
        List<Integer> nodesWithEdges = new ArrayList<>();
        for (Map.Entry<Integer, Node> entry : nodes.entrySet()) {
            if (!entry.getValue().get_outgoing_edges().isEmpty()) {
                nodesWithEdges.add(entry.getKey());
            }
        }
        
        System.out.println("Found " + nodesWithEdges.size() + " nodes with outgoing edges");
        System.out.println("\nSearching for connected pairs...\n");
        
        // Try to find paths using BFS
        for (int i = 0; i < Math.min(50, nodesWithEdges.size()) && validPairs.size() < maxSamples; i++) {
            int source = nodesWithEdges.get(i);
            Set<Integer> reachable = bfsReachable(source, 5); // depth limit
            
            if (!reachable.isEmpty()) {
                for (int dest : reachable) {
                    if (dest != source) {
                        validPairs.add(new int[]{source, dest});
                        if (validPairs.size() >= maxSamples) break;
                    }
                }
            }
            checked++;
        }
        
        System.out.println("Checked " + checked + " source nodes");
        System.out.println("Found " + validPairs.size() + " valid pairs\n");
        
        // Display results
        if (validPairs.isEmpty()) {
            System.out.println("No connected pairs found! Graph may be disconnected.");
        } else {
            System.out.println("=== Sample Valid Query Pairs ===\n");
            for (int i = 0; i < validPairs.size(); i++) {
                int[] pair = validPairs.get(i);
                Node src = Graph.get_node(pair[0]);
                Node dst = Graph.get_node(pair[1]);
                
                // Calculate approximate distance
                double distance = calculateDistance(src, dst);
                
                System.out.println("Query " + (i+1) + ":");
                System.out.println("  Source: " + pair[0] + " (lat=" + 
                                 String.format("%.6f", src.get_latitude()) + ", lon=" + 
                                 String.format("%.6f", src.get_longitude()) + ")");
                System.out.println("  Destination: " + pair[1] + " (lat=" + 
                                 String.format("%.6f", dst.get_latitude()) + ", lon=" + 
                                 String.format("%.6f", dst.get_longitude()) + ")");
                System.out.println("  Approx distance: " + String.format("%.2f", distance) + " km");
                System.out.println("  Test command: java -cp build TestGuiQuery " + pair[0] + " " + pair[1]);
                System.out.println();
            }
        }
    }
    
    private static Set<Integer> bfsReachable(int source, int maxDepth) {
        Set<Integer> reachable = new HashSet<>();
        Queue<int[]> queue = new LinkedList<>(); // [nodeId, depth]
        Set<Integer> visited = new HashSet<>();
        
        queue.add(new int[]{source, 0});
        visited.add(source);
        
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int nodeId = current[0];
            int depth = current[1];
            
            if (depth >= maxDepth) continue;
            
            Node node = Graph.get_node(nodeId);
            Map<Integer, Edge> outgoing = node.get_outgoing_edges();
            
            for (Map.Entry<Integer, Edge> entry : outgoing.entrySet()) {
                int neighbor = entry.getValue().get_destination();
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    reachable.add(neighbor);
                    queue.add(new int[]{neighbor, depth + 1});
                }
            }
        }
        
        return reachable;
    }
    
    private static double calculateDistance(Node n1, Node n2) {
        // Haversine formula for great circle distance
        double lat1 = Math.toRadians(n1.get_latitude());
        double lon1 = Math.toRadians(n1.get_longitude());
        double lat2 = Math.toRadians(n2.get_latitude());
        double lon2 = Math.toRadians(n2.get_longitude());
        
        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;
        
        double a = Math.sin(dlat/2) * Math.sin(dlat/2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dlon/2) * Math.sin(dlon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        
        return 6371 * c; // Earth radius in km
    }
}
