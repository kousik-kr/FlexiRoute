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
    private static double[] widthTimeSeries;

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
        widthTimeSeries = null;
    }

    public static void updateArrivalTimeSeries(String[] time_series) {
        arrivalTimeSeries = new double[time_series.length];
        for (int i = 0; i < time_series.length; i++) {
            arrivalTimeSeries[i] = Double.parseDouble(time_series[i]);
        }

    }

    public static void updateWidthTimeSeries(String[] time_series) {
        widthTimeSeries = new double[time_series.length];
        for (int i = 0; i < time_series.length; i++) {
            widthTimeSeries[i] = Double.parseDouble(time_series[i]);
        }

    }

    public static double[] getArrivalTimeSeries() {
        return arrivalTimeSeries;
    }

    public static double[] getWidthTimeSeries() {
        return widthTimeSeries;
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

    public static List<Double> getWidthTimeSeries(double start_departure_time, double end_departure_time) {
        List<Double> time_series = new ArrayList<>();

        for (double time_point : widthTimeSeries) {

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

    public static boolean isSharpRightTurn(Node previous_node, Node current_node, Node next_node) {
        double b1 = bearing(previous_node, current_node);
        double b2 = bearing(current_node, next_node);

        // Signed angle difference in range [-180, 180]
        double delta = (b2 - b1 + 540) % 360 - 180;

        // Sharp right turn: angle < -60 degrees
        return delta < -BidirectionalAstar.SHARP_THRESHOLD;
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
        System.out.println("[ForwardA*] source=" + source + " dest=" + destination + " budget=" + budget);

        Map<Integer, Double> gTime = new HashMap<>();
        Map<Integer, Double> gWideDistance = new HashMap<>();
        Map<Integer, Double> gDistance = new HashMap<>();
        Map<Integer, Integer> gRightTurn = new HashMap<>();
        int prevoious_node = source;

        PriorityQueue<Integer> pQueue = new PriorityQueue<>(get_vertex_count(), new Comparator<Integer>() {
            @Override
            public int compare(Integer i, Integer j) {

                if (gTime.get(i) > gTime.get(j)) {
                    return 1;
                } else if (gTime.get(i) < gTime.get(j)) {
                    return -1;
                }
                return 0;
            }
        });


        pQueue.add(source);
        gTime.put(source, 0.0);
        gWideDistance.put(source, 0.0);
        gDistance.put(source, 0.0);
        gRightTurn.put(source, 0);
        get_node(source).setForwardReachebility();

        while (!pQueue.isEmpty()) {

            int current_vertex = pQueue.poll();
            if (pQueue.size() % 5000 == 0) {
                System.out.println("[ForwardA*] queue size=" + pQueue.size());
            }

            Node node = get_node(current_vertex);
            double current_cost = gTime.get(current_vertex);
            double current_wide_distance = gWideDistance.get(current_vertex);
            double current_distance = gDistance.get(current_vertex);
            int current_right_turn = gRightTurn.get(current_vertex);

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
                if (current_vertex != source && isSharpRightTurn(get_node(prevoious_node), node, get_node(j))) {
                    g_right_turn++;
                }

                if (g_time <= budget) {
                    if (!gTime.containsKey(j)) {
                        get_node(j).setForwardReachebility();
                        gTime.put(j, g_time);
                        gWideDistance.put(j, g_wide_distance);
                        gDistance.put(j, g_distance);
                        gRightTurn.put(j, g_right_turn);
                        if (j != destination)
                            pQueue.add(j);
                    }

                    else if (gTime.get(j) > g_time) {
                        gTime.replace(j, g_time);
                        gWideDistance.replace(j, g_wide_distance);
                        gDistance.replace(j, g_distance);
                        gRightTurn.replace(j, g_right_turn);

                    }
                }
            }

            //pQueue.poll();
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
        System.out.println("[BackwardA*] source=" + source + " dest=" + destination + " budget=" + budget);

        Map<Integer, Double> hTime = new HashMap<>();
        Map<Integer, Double> gTime = new HashMap<>();
        Map<Integer, Double> gWideDistance = new HashMap<>();
        Map<Integer, Double> gDistance = new HashMap<>();
        Map<Integer, Integer> gRightTurn = new HashMap<>();
        int next_node = destination;

        PriorityQueue<Integer> pQueue = new PriorityQueue<>(get_vertex_count(), new Comparator<Integer>() {
            @Override
            public int compare(Integer i, Integer j) {

                if (hTime.get(i) > hTime.get(j)) {
                    return 1;
                } else if (hTime.get(i) < hTime.get(j)) {
                    return -1;
                }
                return 0;
            }
        });

        if (get_node(destination).isForwardReacheble()) {
            pQueue.add(destination);
            gTime.put(destination, 0.0);
            hTime.put(destination, get_node(destination).get_forward_hTime());
            gWideDistance.put(source, 0.0);
            gDistance.put(source, 0.0);
            gRightTurn.put(source, 0);
            get_node(destination).setBackwardReachebility();
        }

        while (!pQueue.isEmpty()) {

            int current_vertex = pQueue.poll();
            if (pQueue.size() % 5000 == 0) {
                System.out.println("[BackwardA*] queue size=" + pQueue.size());
            }
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
                if (current_vertex != destination && isSharpRightTurn(get_node(j), node, get_node(next_node))) {
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
