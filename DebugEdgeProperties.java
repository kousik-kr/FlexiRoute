import java.util.*;

/**
 * Debug edge time properties to understand NullPointerException
 */
public class DebugEdgeProperties {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Debugging Edge Time Properties ===\n");
        
        // Load graph
        BidirectionalAstar.configureDefaults();
        boolean loaded = BidirectionalAstar.loadGraphFromDisk(null, null);
        
        if (!loaded) {
            System.err.println("Failed to load graph!");
            return;
        }
        
        System.out.println("Graph loaded: " + Graph.get_nodes().size() + " nodes\n");
        
        // Check arrival time series
        System.out.println("=== Arrival Time Series ===");
        String[] arrivalSeries = Graph.getArrivalTimeSeries();
        if (arrivalSeries != null) {
            System.out.print("Time points: ");
            for (String time : arrivalSeries) {
                System.out.print(time + " ");
            }
            System.out.println("\n");
        }
        
        // Examine first few edges
        System.out.println("=== Examining Edge 0->1 ===");
        Node node0 = Graph.get_node(0);
        if (node0 != null) {
            Map<Integer, Edge> outgoing = node0.get_outgoing_edges();
            if (outgoing.containsKey(1)) {
                Edge edge = outgoing.get(1);
                System.out.println("Edge 0->1 found");
                System.out.println("  Base width: " + edge.getBaseWidth());
                System.out.println("  Rush width: " + edge.getRushWidth());
                System.out.println("  Distance: " + edge.getDistance());
                
                // Check time properties
                System.out.println("\n  Time properties stored:");
                Map<Integer, Properties> timeProps = edge.get_time_properties();
                if (timeProps != null) {
                    List<Integer> times = new ArrayList<>(timeProps.keySet());
                    Collections.sort(times);
                    for (int time : times) {
                        Properties prop = timeProps.get(time);
                        System.out.println("    Time " + time + ": cost = " + prop.get_travel_cost());
                    }
                } else {
                    System.out.println("    ERROR: Time properties map is null!");
                }
                
                // Test arrivals at specific times
                System.out.println("\n  Testing get_arrival_time():");
                double[] testTimes = {0, 450, 510, 720, 1020};
                for (double testTime : testTimes) {
                    try {
                        double arrival = edge.get_arrival_time(testTime);
                        System.out.println("    Depart at " + testTime + " -> arrive at " + arrival);
                    } catch (Exception e) {
                        System.out.println("    Depart at " + testTime + " -> ERROR: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("ERROR: Edge 0->1 not found!");
            }
        }
        
        // Check incoming edges to node 1
        System.out.println("\n=== Examining Incoming Edges to Node 1 ===");
        Node node1 = Graph.get_node(1);
        if (node1 != null) {
            Map<Integer, Edge> incoming = node1.get_incoming_edges();
            System.out.println("Node 1 has " + incoming.size() + " incoming edges");
            for (Map.Entry<Integer, Edge> entry : incoming.entrySet()) {
                Edge edge = entry.getValue();
                System.out.println("  Edge " + edge.get_source() + "->" + edge.get_destination());
                System.out.println("    Time properties: " + edge.get_time_properties().size() + " entries");
            }
        }
    }
}
