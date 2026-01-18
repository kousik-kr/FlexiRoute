import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

/**
 * Central registry for the in-memory road network.
 * <p>
 * The solver expects a globally accessible graph so the structure is exposed
 * as a static utility. To keep the responsibilities clear, the class focuses on
 * a handful of well-documented operations: managing the adjacency list,
 * storing the time-series metadata used by the solver, and providing helper
 * routines for directional math and A* search. Extensive comments walk through
 * the intent of each step so future refactors can reason about the state
 * transitions without reverse-engineering the algorithm.
 */
public final class Graph {

    private static int n_vertexes;
    private static final Map<Integer, Node> adjacency_list = new HashMap<>();
    private static final Map<Integer, Cluster> clusters = new HashMap<>();
    private static double[] arrivalTimeSeries;

    private Graph() {
        // Static utility class; do not instantiate.
    }

    /**
     * Number of vertices expected in the network (mirrors dataset header).
     */
    public static int get_vertex_count() {
        return n_vertexes;
    }

    /**
     * Seed the expected vertex count. This does not mutate adjacency entries.
     */
    public static void set_vertex_count(int n) {
        n_vertexes = n;
    }

    /**
     * Insert or replace a node in the adjacency list.
     */
    public static void add_node(int node_id, Node node) {
        adjacency_list.put(node_id, node);
    }

    /**
     * Lightweight existence check used by validation routines.
     */
    public static boolean contains_node(int node_id) {
        return adjacency_list.containsKey(node_id);
    }

    /**
     * Retrieve a node from the adjacency list, or {@code null} when absent.
     */
    public static Node get_node(int node_id) {
        return adjacency_list.get(node_id);
    }

    /**
     * Read-only view of nodes for iteration-heavy routines.
     */
    public static Map<Integer, Node> get_nodes() {
        return adjacency_list;
    }

    /**
     * Reset per-query state on every node so repeated solver invocations start
     * from a clean slate without reallocating the graph structure.
     */
    public static void reset() {
        for (Entry<Integer, Node> entry : adjacency_list.entrySet()) {
            entry.getValue().reset();
        }
    }

    /**
     * Drop all graph state. Useful when hot-reloading a new dataset without
     * restarting the JVM.
     */
    public static void clear() {
        adjacency_list.clear();
        clusters.clear();
        n_vertexes = 0;
        arrivalTimeSeries = null;
    }

    public static void updateArrivalTimeSeries(String[] time_series) {
        arrivalTimeSeries = new double[time_series.length];
        for (int i = 0; i < time_series.length; i++) {
            arrivalTimeSeries[i] = Double.parseDouble(time_series[i]);
        }

    }

    public static double[] getArrivalTimeSeries() {
        return arrivalTimeSeries;
    }

    public static List<Double> getArrivalTimeSeries(double start_departure_time, double end_departure_time) {
        List<Double> time_series = new ArrayList<>();

        for (double time_point : arrivalTimeSeries) {

            if (time_point == start_departure_time || time_point == end_departure_time)
                continue;

            if (time_point > start_departure_time && time_point < end_departure_time) {
                time_series.add(time_point);
            } else if (time_point > end_departure_time) {
                break; // No need to continue as the list is sorted
            }
        }

        return time_series;
    }

    /**
     * Check if a turn is a right turn (any turn to the right)
     * A right turn has a negative angle (turning clockwise)
     * @param previous_node The node we came from
     * @param current_node The current node (intersection)
     * @param next_node The node we're going to
     * @return true if this is a right turn (angle < 0)
     */
    public static boolean isRightTurn(Node previous_node, Node current_node, Node next_node) {
        double b1 = bearing(previous_node, current_node);
        double b2 = bearing(current_node, next_node);

        // Signed angle difference in range [-180, 180]
        double delta = (b2 - b1 + 540) % 360 - 180;

        // Right turn: any negative angle (turning right/clockwise)
        // Small threshold to avoid counting near-straight paths as turns
        return delta < -10; // -10 degrees threshold for meaningful right turn
    }

    /**
     * Check if a right turn should be counted with the new logic:
     * - Out degree of current_node must be > 1 (multiple outgoing edges)
     * - The next_node must NOT be the leftmost edge among all outgoing edges
     * 
     * @param previous_node The node we came from
     * @param current_node The current node (intersection)
     * @param next_node_id The ID of the node we're going to
     * @return true if this is a counted right turn
     */
    public static boolean isCountedRightTurn(Node previous_node, Node current_node, int next_node_id) {
        Node next_node = get_node(next_node_id);
        if (next_node == null) {
            return false;
        }
        
        // First check if it's a right turn geometrically
        if (!isRightTurn(previous_node, current_node, next_node)) {
            return false;
        }
        
        // Check if out degree > 1
        Map<Integer, Edge> outgoingEdges = current_node.get_outgoing_edges();
        if (outgoingEdges.size() <= 1) {
            return false; // No choice, not a "right turn" in the sense we're counting
        }
        
        // Find the leftmost edge (smallest bearing among outgoing edges)
        double incomingBearing = bearing(previous_node, current_node);
        double leftmostBearing = Double.POSITIVE_INFINITY;
        int leftmostNodeId = -1;
        
        for (Integer nextNodeId : outgoingEdges.keySet()) {
            Node nextN = get_node(nextNodeId);
            if (nextN != null) {
                double outgoingBearing = bearing(current_node, nextN);
                // Calculate relative angle from incoming direction
                double relativeAngle = (outgoingBearing - incomingBearing + 360) % 360;
                // Normalize to [-180, 180] range for proper left comparison
                if (relativeAngle > 180) {
                    relativeAngle -= 360;
                }
                // Leftmost means most negative angle (furthest left)
                if (relativeAngle < leftmostBearing) {
                    leftmostBearing = relativeAngle;
                    leftmostNodeId = nextNodeId;
                }
            }
        }
        
        // Return true if next_node is NOT the leftmost edge
        return next_node_id != leftmostNodeId;
    }

    /**
     * @deprecated Use isRightTurn instead. Kept for backward compatibility.
     */
    @Deprecated
    public static boolean isSharpRightTurn(Node previous_node, Node current_node, Node next_node) {
        return isRightTurn(previous_node, current_node, next_node);
    }

    public static double bearing(Node previous_node, Node current_node) {
        double lat1Rad = Math.toRadians(previous_node.get_latitude());
        double lon1Rad = Math.toRadians(previous_node.get_longitude());
        double lat2Rad = Math.toRadians(current_node.get_latitude());
        double lon2Rad = Math.toRadians(current_node.get_longitude());

        double dLon = lon2Rad - lon1Rad;

        double x = Math.sin(dLon) * Math.cos(lat2Rad);
        double y = Math.cos(lat1Rad) * Math.sin(lat2Rad) -
                   Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(dLon);

        double brng = Math.toDegrees(Math.atan2(x, y));
        return (brng + 360) % 360;
    }

    /**
     * Forward search pass that estimates time, distance, and right turns from
     * the source. The implementation intentionally avoids clever heuristics in
     * favor of readability: the queues are annotated, and every state mutation
     * is accompanied by descriptive variable names and comments.
     */
    public static void forwardAstar(int source, int destination, double budget) {
        Map<Integer, Double> gTime = new HashMap<>();
        Map<Integer, Double> gWideDistance = new HashMap<>();
        Map<Integer, Double> gDistance = new HashMap<>();
        Map<Integer, Integer> gRightTurn = new HashMap<>();
        int prevoious_node = source;

        PriorityQueue<Integer> pQueue = new PriorityQueue<>(get_vertex_count(), new Comparator<Integer>() {
            @Override
            public int compare(Integer i, Integer j) {
                try {
                    Double iTime = gTime.get(i);
                    Double jTime = gTime.get(j);
                    if(iTime == null || jTime == null) {
                        return 0;
                    }
                    if (iTime > jTime) {
                        return 1;
                    } else if (iTime < jTime) {
                        return -1;
                    }
                    return 0;
                } catch(Exception e) {
                    return 0;
                }
            }
        });


        gTime.put(source, 0.0);
        gWideDistance.put(source, 0.0);
        gDistance.put(source, 0.0);
        gRightTurn.put(source, 0);
        get_node(source).setForwardReachebility();
        pQueue.add(source);

        while (!pQueue.isEmpty()) {

            int current_vertex = pQueue.poll();
            // System.out.println("[ForwardA*] Polled " + current_vertex + ", queue size now=" + pQueue.size());
            if (pQueue.size() % 5000 == 0) {
                // System.out.println("[ForwardA*] queue size=" + pQueue.size());
            }

            Node node = get_node(current_vertex);

            // Defensive: guards against missing bookkeeping entries that would NPE on auto-unboxing.
            Double currentCostObj = gTime.get(current_vertex);
            Double currentWideObj = gWideDistance.get(current_vertex);
            Double currentDistObj = gDistance.get(current_vertex);
            Integer currentRightTurnObj = gRightTurn.get(current_vertex);

            if (currentCostObj == null || currentWideObj == null || currentDistObj == null || currentRightTurnObj == null) {
                System.err.println("[BackwardA*] Missing state for node " + current_vertex + " (gTime=" + currentCostObj + ", gWide=" + currentWideObj + ", gDist=" + currentDistObj + ", gRT=" + currentRightTurnObj + ") – skipping");
                continue;
            }

            double current_cost = currentCostObj;
            double current_wide_distance = currentWideObj;
            double current_distance = currentDistObj;
            int current_right_turn = currentRightTurnObj;

            Map<Integer, Edge> temp_outgoing_edge = node.get_outgoing_edges();

            for (Entry<Integer, Edge> entry : temp_outgoing_edge.entrySet()) {

                Edge edge = entry.getValue();
                int j = edge.get_destination();
                double cost_j = edge.getLowestCost();
                double g_time = current_cost + cost_j;
                
                double distance = edge.get_distance();
                double g_wide_distance = current_wide_distance;

                if (!edge.is_clearway() && (edge.get_width(0) >= BidirectionalAstar.WIDENESS_THRESHOLD)) {
                    g_wide_distance += distance;
                }

                double g_distance = current_distance + distance;
                int g_right_turn = current_right_turn;
                if (current_vertex != source && isCountedRightTurn(get_node(prevoious_node), node, j)) {
                    g_right_turn++;
                }

                if (g_time <= budget) {
                    if (!gTime.containsKey(j)) {
                        get_node(j).setForwardReachebility();
                        gTime.put(j, g_time);
                        gWideDistance.put(j, g_wide_distance);
                        gDistance.put(j, g_distance);
                        gRightTurn.put(j, g_right_turn);
                        if (j != destination) {
                            pQueue.add(j);
                        }
                    }

                    else if (gTime.get(j) > g_time) {
                        gTime.replace(j, g_time);
                        gWideDistance.replace(j, g_wide_distance);
                        gDistance.replace(j, g_distance);
                        gRightTurn.replace(j, g_right_turn);
                    }
                }
            }
        }

        for (Entry<Integer, Double> entry : gTime.entrySet()) {
            int node_id = entry.getKey();
            Node node = get_node(node_id);
            node.setForwardHTime(entry.getValue());
            node.setForwardHWideDistance(gWideDistance.get(node_id));
            node.setForwardHDistance(gDistance.get(node_id));
            node.setForwardHRightTurn(gRightTurn.get(node_id));
        }
    }

    public static void backwardAstar(int source, int destination, double budget) {
        Map<Integer, Double> gTime = new HashMap<>();
        Map<Integer, Double> hTime = new HashMap<>();
        Map<Integer, Double> gWideDistance = new HashMap<>();
        Map<Integer, Double> gDistance = new HashMap<>();
        Map<Integer, Integer> gRightTurn = new HashMap<>();
        int next_node = destination;

        PriorityQueue<Integer> pQueue = new PriorityQueue<>(get_vertex_count(), new Comparator<Integer>() {
            @Override
            public int compare(Integer i, Integer j) {
                try {
                    Double iTime = hTime.get(i);
                    Double jTime = hTime.get(j);
                    if(iTime == null || jTime == null) {
                        return 0;
                    }
                    if (iTime > jTime) {
                        return 1;
                    } else if (iTime < jTime) {
                        return -1;
                    }
                    return 0;
                } catch(Exception e) {
                    return 0;
                }
            }
        });

        if (get_node(destination).isForwardReacheble()) {
            gTime.put(destination, 0.0);
            hTime.put(destination, get_node(destination).get_forward_hTime());
            gWideDistance.put(destination, 0.0);
            gDistance.put(destination, 0.0);
            gRightTurn.put(destination, 0);
            get_node(destination).setBackwardReachebility();
            pQueue.add(destination);
        }

        while (!pQueue.isEmpty()) {

            int current_vertex = pQueue.poll();
            Node node = get_node(current_vertex);
            double current_cost = gTime.get(current_vertex);
            double current_wide_distance = gWideDistance.get(current_vertex);
            double current_distance = gDistance.get(current_vertex);
            int current_right_turn = gRightTurn.get(current_vertex);

            Map<Integer, Edge> temp_incoming_edge = node.get_incoming_edges();

            for (Entry<Integer, Edge> entry : temp_incoming_edge.entrySet()) {

                Edge edge = entry.getValue();
                int j = edge.get_source();
                if (!get_node(j).isForwardReacheble())
                    continue;

                double cost_j = edge.getLowestCost();
                double g_time = current_cost + cost_j;
                double f_time = get_node(j).get_forward_hTime();
                double distance = edge.get_distance();
                double g_wide_distance = current_wide_distance;

                if (!edge.is_clearway() && (edge.get_width(0) >= BidirectionalAstar.WIDENESS_THRESHOLD)) {
                    g_wide_distance += distance;
                }

                double g_distance = current_distance + distance;
                int g_right_turn = current_right_turn;
                if (current_vertex != destination && isCountedRightTurn(get_node(j), node, next_node)) {
                    g_right_turn++;
                }

                if (g_time + f_time <= budget) {
                    if (!hTime.containsKey(j)) {
                        get_node(j).setBackwardReachebility();
                        gTime.put(j, g_time);
                        hTime.put(j, g_time + f_time);
                        gWideDistance.put(j, g_wide_distance);
                        gDistance.put(j, g_distance);
                        gRightTurn.put(j, g_right_turn);
                        if (j != source) pQueue.add(j);
                    }

                    else if (gTime.get(j) > g_time) {
                        gTime.replace(j, g_time);
                        hTime.replace(j, g_time + f_time);
                        gWideDistance.replace(j, g_wide_distance);
                        gDistance.replace(j, g_distance);
                        gRightTurn.replace(j, g_right_turn);
                    }
                }
            }

        }

        for (Entry<Integer, Double> entry : gTime.entrySet()) {
            int node_id = entry.getKey();
            Node node = get_node(node_id);
            node.setBackwardHTime(entry.getValue());
            node.setBackwardHWideDistance(gWideDistance.get(node_id));
            node.setBackwardHDistance(gDistance.get(node_id));
            node.setBackwardHRightTurn(gRightTurn.get(node_id));
        }
    }

    // Add a cluster to the graph
    public static void addCluster(Cluster cluster) {
        clusters.put(cluster.getClusterId(), cluster);
    }

    // Get a cluster by its ID
    public static Cluster getCluster(int clusterId) {
        return clusters.get(clusterId);
    }

    // Get all clusters
    public static Map<Integer, Cluster> getAllClusters() {
        return clusters;
    }

    // Create a cluster and add nodes to it
    public static void createCluster(int clusterId, List<Node> nodes) {
        Cluster cluster = new Cluster(clusterId);
        for (Node node : nodes) {
            cluster.addNode(node);
        }
        addCluster(cluster);
    }
}
