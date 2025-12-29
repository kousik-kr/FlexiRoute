import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Find a destination with the maximum fastest-path travel time from random sources
 * and then run the solver on that pair.
 */
public class FindRandomLongestPath {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Random-sourced longest fastest-path search ===\n");

        BidirectionalAstar.configureDefaults();
        boolean loaded = BidirectionalAstar.loadGraphFromDisk(null, null);
        if (!loaded) {
            System.err.println("Failed to load graph.");
            return;
        }

        int vertexCount = Graph.get_vertex_count();
        int samples = 50; // number of random sources to try
        long seed = 12345L;
        Random rnd = new Random(seed);

        double bestTravel = -1.0;
        int bestSrc = -1;
        int bestDst = -1;

        for (int i = 0; i < samples; i++) {
            int src = rnd.nextInt(vertexCount);
            double[] longest = longestFromSource(src);
            double travel = longest[0];
            int dst = (int) longest[1];
            System.out.printf("Sample %02d: src=%d longest=%.4f to dst=%d%n", i + 1, src, travel, dst);
            if (travel > bestTravel) {
                bestTravel = travel;
                bestSrc = src;
                bestDst = dst;
            }
        }

        if (bestTravel < 0) {
            System.out.println("No reachable pairs found.");
            return;
        }

        double budget = bestTravel + 10.0; // small margin above fastest path
        System.out.printf("\nBest pair: %d -> %d  fastest=%.4f  using budget=%.2f%n", bestSrc, bestDst, bestTravel, budget);

        Result result = BidirectionalAstar.runSingleQuery(bestSrc, bestDst, 0.0, budget, budget);
        if (result != null) {
            System.out.printf("Driver result: travel=%.4f score=%.4f departure=%.2f%n",
                    result.get_travel_time(), result.get_score(), result.get_departureTime());
        } else {
            System.out.println("Driver returned null for the identified pair.");
        }
    }

    // Dijkstra on time-dependent edge costs to get fastest arrivals from a source.
    private static double[] longestFromSource(int source) {
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
