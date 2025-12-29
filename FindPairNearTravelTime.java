import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class FindPairNearTravelTime {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Search for pair with travel time near 50 min (budget 60) ===\n");

        BidirectionalAstar.configureDefaults();
        boolean loaded = BidirectionalAstar.loadGraphFromDisk(null, null);
        if (!loaded) {
            System.err.println("Failed to load graph.");
            return;
        }

        double target = 40.0;
        double tolerance = 5.0;
        double budget = 60.0;

        Integer bestSource = null;
        Integer bestDest = null;

        // Sweep a handful of sources to find a within-budget path near the target travel time
        int[] sourcesToTry = new int[]{0, 10, 50, 100, 200, 300, 400, 500, 750, 1000, 1500, 2000, 2500, 3000, 4000, 5000};
        for (int s : sourcesToTry) {
            Integer candidate = findDestinationNearTravelTime(s, target, tolerance, budget);
            if (candidate != null) {
                bestSource = s;
                bestDest = candidate;
                break;
            }
        }

        if (bestDest == null) {
            System.out.println("No candidate found within tolerance across sampled sources.");
            double maxSeen = 0.0;
            for (int s : sourcesToTry) {
                maxSeen = Math.max(maxSeen, maxTravelWithinBudget(s, budget));
            }
            System.out.printf("Max travel time observed within budget %.1f among sampled sources: %.2f%n", budget, maxSeen);
            return;
        }

        System.out.println("Found candidate: " + bestSource + " -> " + bestDest);
        Result result = BidirectionalAstar.runSingleQuery(bestSource, bestDest, 0.0, budget, budget);
        if (result == null) {
            System.out.println("Driver returned null for the candidate pair.");
            return;
        }

        System.out.printf("Result: travel=%.2f, score=%.4f, departure=%.2f\n",
                result.get_travel_time(), result.get_score(), result.get_departureTime());
    }

    private static Integer findDestinationNearTravelTime(int source, double target, double tolerance, double budget) {
        Map<Integer, Double> best = new HashMap<>();
        PriorityQueue<double[]> pq = new PriorityQueue<>((a, b) -> Double.compare(a[0], b[0]));

        best.put(source, 0.0);
        pq.offer(new double[]{0.0, source});

        while (!pq.isEmpty()) {
            double[] state = pq.poll();
            double currentTime = state[0];
            int nodeId = (int) state[1];

            if (currentTime > best.getOrDefault(nodeId, Double.MAX_VALUE)) {
                continue;
            }

            if (Math.abs(currentTime - target) <= tolerance) {
                return nodeId;
            }

            Node node = Graph.get_node(nodeId);
            if (node == null) continue;

            for (Edge edge : node.get_outgoing_edges().values()) {
                double arrival = edge.get_arrival_time(currentTime);
                double travel = arrival; // since we start at 0, arrival equals travel time so far

                if (travel > budget) {
                    continue; // over budget for the search
                }

                int dest = edge.get_destination();
                if (travel < best.getOrDefault(dest, Double.MAX_VALUE)) {
                    best.put(dest, travel);
                    pq.offer(new double[]{travel, dest});
                }
            }
        }
        return null;
    }

    private static double maxTravelWithinBudget(int source, double budget) {
        Map<Integer, Double> best = new HashMap<>();
        PriorityQueue<double[]> pq = new PriorityQueue<>((a, b) -> Double.compare(a[0], b[0]));
        best.put(source, 0.0);
        pq.offer(new double[]{0.0, source});
        double maxSeen = 0.0;

        while (!pq.isEmpty()) {
            double[] state = pq.poll();
            double currentTime = state[0];
            int nodeId = (int) state[1];

            if (currentTime > best.getOrDefault(nodeId, Double.MAX_VALUE)) {
                continue;
            }

            maxSeen = Math.max(maxSeen, currentTime);

            Node node = Graph.get_node(nodeId);
            if (node == null) continue;

            for (Edge edge : node.get_outgoing_edges().values()) {
                double arrival = edge.get_arrival_time(currentTime);
                if (arrival > budget) {
                    continue;
                }
                int dest = edge.get_destination();
                if (arrival < best.getOrDefault(dest, Double.MAX_VALUE)) {
                    best.put(dest, arrival);
                    pq.offer(new double[]{arrival, dest});
                }
            }
        }
        return maxSeen;
    }
}
