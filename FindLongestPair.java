import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Scan a subset of sources and report the pair with the longest fastest-path travel time.
 */
public class FindLongestPair {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Finding longest fastest-path pair (time-dependent) ===\n");

        BidirectionalAstar.configureDefaults();
        boolean loaded = BidirectionalAstar.loadGraphFromDisk(null, null);
        if (!loaded) {
            System.err.println("Failed to load graph.");
            return;
        }

        int maxSources = 2000; // scan first N node ids (adjustable)
        double budget = 120.0; // upper cap to ignore paths far beyond any realistic query budget

        double bestTravel = -1.0;
        int bestSrc = -1;
        int bestDst = -1;

        int vertexCount = Graph.get_vertex_count();
        int sourcesToScan = Math.min(maxSources, vertexCount);

        for (int src = 0; src < sourcesToScan; src++) {
            double[] longestFromSrc = longestFromSource(src, budget);
            double travel = longestFromSrc[0];
            int dst = (int) longestFromSrc[1];

            if (travel > bestTravel) {
                bestTravel = travel;
                bestSrc = src;
                bestDst = dst;
            }
        }

        if (bestTravel < 0) {
            System.out.println("No reachable pairs found within budget.");
            return;
        }

        System.out.printf("Longest pair: %d -> %d  travel=%.4f (budget cap=%.1f)\n",
                bestSrc, bestDst, bestTravel, budget);

        Result result = BidirectionalAstar.runSingleQuery(bestSrc, bestDst, 0.0, budget, budget);
        if (result != null) {
            System.out.printf("Driver verification: travel=%.4f score=%.4f\n",
                    result.get_travel_time(), result.get_score());
        } else {
            System.out.println("Driver could not produce a path for the identified pair.");
        }
    }

    // Return [longestTravel, destinationId] from single-source Dijkstra with budget cap
    private static double[] longestFromSource(int source, double budget) {
        Map<Integer, Double> best = new HashMap<>();
        PriorityQueue<double[]> pq = new PriorityQueue<>((a, b) -> Double.compare(a[0], b[0]));
        best.put(source, 0.0);
        pq.offer(new double[]{0.0, source});
        double maxTravel = -1.0;
        int maxNode = -1;

        while (!pq.isEmpty()) {
            double[] state = pq.poll();
            double currentTime = state[0];
            int nodeId = (int) state[1];

            if (currentTime > best.getOrDefault(nodeId, Double.MAX_VALUE)) {
                continue;
            }

            if (currentTime > maxTravel) {
                maxTravel = currentTime;
                maxNode = nodeId;
            }

            Node node = Graph.get_node(nodeId);
            if (node == null) continue;

            for (Edge edge : node.get_outgoing_edges().values()) {
                double arrival = edge.get_arrival_time(currentTime);
                if (arrival > budget) {
                    continue; // respect budget cap for search
                }
                int dest = edge.get_destination();
                if (arrival < best.getOrDefault(dest, Double.MAX_VALUE)) {
                    best.put(dest, arrival);
                    pq.offer(new double[]{arrival, dest});
                }
            }
        }

        return new double[]{maxTravel, maxNode};
    }
}
