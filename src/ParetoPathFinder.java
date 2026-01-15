import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import models.RoutingMode;

/**
 * ParetoPathFinder - Test driver to find source-destination pairs with diverse paths
 * 
 * This class automatically:
 * 1. Loads the London dataset (no GUI)
 * 2. Finds pairs where different routing modes produce different paths
 * 3. Requires multiple Pareto optimal paths for the balanced mode
 * 4. Uses rush hour departure times
 * 5. Stores results to a text file
 * 
 * Usage: java ParetoPathFinder [numPairs] [outputFile]
 */
public class ParetoPathFinder {
    
    // Configuration
    private static final String LONDON_DATASET_DIR = System.getProperty("user.dir") + File.separator + "dataset" + File.separator + "London" + File.separator;
    private static final int LONDON_VERTEX_COUNT = 288016;
    
    // Rush hour time ranges (in minutes from midnight)
    private static final int MORNING_RUSH_START = 7 * 60;      // 7:00 AM
    private static final int MORNING_RUSH_END = 9 * 60;        // 9:00 AM
    private static final int EVENING_RUSH_START = 17 * 60;     // 5:00 PM
    private static final int EVENING_RUSH_END = 19 * 60;       // 7:00 PM
    
    // Search parameters
    private static final double MIN_BUDGET = 15.0;   // Minimum 15 minutes travel budget
    private static final double MAX_BUDGET = 60.0;   // Maximum 60 minutes travel budget
    private static final double INTERVAL_DURATION = 30.0;  // 30 minute departure window
    private static final int MAX_HOP_DISTANCE = 50;  // Max hops for destination search
    
    private static Random random = new Random(42);  // Fixed seed for reproducibility
    private static int totalPairsChecked = 0;
    private static int validPairsFound = 0;
    
    public static void main(String[] args) {
        int targetPairs = 10;  // Default number of pairs to find
        String outputFile = "pareto_path_pairs.txt";
        
        if (args.length > 0) {
            try {
                targetPairs = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid number of pairs. Using default: " + targetPairs);
            }
        }
        if (args.length > 1) {
            outputFile = args[1];
        }
        
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║          ParetoPathFinder - Diverse Path Discovery          ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Configuration:");
        System.out.println("  - Target pairs to find: " + targetPairs);
        System.out.println("  - Output file: " + outputFile);
        System.out.println("  - Dataset: London (" + LONDON_VERTEX_COUNT + " nodes)");
        System.out.println("  - Rush hours: 7:00-9:00 AM & 5:00-7:00 PM");
        System.out.println();
        
        try {
            // Initialize and load dataset
            if (!initializeDataset()) {
                System.err.println("Failed to load dataset. Exiting.");
                System.exit(1);
            }
            
            // Find valid pairs
            List<DiversePathPair> validPairs = findDiversePathPairs(targetPairs);
            
            // Write results to file
            writeResultsToFile(validPairs, outputFile);
            
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("SUMMARY:");
            System.out.println("  - Total pairs checked: " + totalPairsChecked);
            System.out.println("  - Valid pairs found: " + validPairsFound);
            System.out.println("  - Results saved to: " + outputFile);
            System.out.println("═══════════════════════════════════════════════════════════════");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cleanup
            if (BidirectionalAstar.pool != null) {
                BidirectionalAstar.pool.shutdown();
            }
        }
    }
    
    /**
     * Initialize the dataset without launching GUI
     */
    private static boolean initializeDataset() {
        System.out.println("[Init] Loading London dataset...");
        
        // Configure defaults
        BidirectionalAstar.configureDefaults();
        BidirectionalAstar.setIntervalDuration(INTERVAL_DURATION);
        
        // Load the graph
        boolean loaded = BidirectionalAstar.loadGraphFromDisk(LONDON_DATASET_DIR, LONDON_VERTEX_COUNT);
        
        if (loaded) {
            System.out.println("[Init] Dataset loaded successfully with " + Graph.get_nodes().size() + " nodes.");
        }
        
        return loaded;
    }
    
    /**
     * Find source-destination pairs with diverse paths across routing modes
     */
    private static List<DiversePathPair> findDiversePathPairs(int targetCount) throws InterruptedException, ExecutionException {
        List<DiversePathPair> validPairs = new ArrayList<>();
        Map<Integer, Node> allNodes = Graph.get_nodes();
        List<Integer> nodeIds = new ArrayList<>(allNodes.keySet());
        
        System.out.println();
        System.out.println("[Search] Starting search for diverse path pairs...");
        System.out.println("[Search] Using BFS to find reachable destinations...");
        System.out.println("[Search] This may take several minutes...");
        System.out.println();
        
        int maxAttempts = targetCount * 200;  // Limit total attempts
        int attempts = 0;
        
        while (validPairs.size() < targetCount && attempts < maxAttempts) {
            attempts++;
            totalPairsChecked++;
            
            // Random source
            int sourceIdx = random.nextInt(nodeIds.size());
            int source = nodeIds.get(sourceIdx);
            
            // Find a reachable destination using BFS
            int dest = findReachableDestination(source, allNodes);
            if (dest == -1 || dest == source) continue;
            
            // Random rush hour departure time
            double departureTime = getRandomRushHourTime();
            
            // Random budget within range
            double budget = MIN_BUDGET + random.nextDouble() * (MAX_BUDGET - MIN_BUDGET);
            
            if (attempts % 20 == 0) {
                System.out.printf("[Search] Progress: %d attempts, %d valid pairs found%n", attempts, validPairs.size());
            }
            
            try {
                DiversePathPair pair = evaluatePair(source, dest, departureTime, budget);
                if (pair != null) {
                    validPairs.add(pair);
                    validPairsFound++;
                    System.out.printf("[FOUND] Pair #%d: %d -> %d (dep=%.0f min, budget=%.1f min)%n",
                        validPairsFound, source, dest, departureTime, budget);
                    System.out.printf("        Wideness path: %d nodes, score=%.1f%%, turns=%d%n",
                        pair.widenessPath.getPathLength(), pair.widenessPath.get_score(), pair.widenessPath.get_right_turns());
                    System.out.printf("        MinTurns path: %d nodes, score=%.1f%%, turns=%d%n",
                        pair.minTurnsPath.getPathLength(), pair.minTurnsPath.get_score(), pair.minTurnsPath.get_right_turns());
                    System.out.printf("        Pareto paths: %d different optimal paths%n", pair.paretoPathCount);
                }
            } catch (Exception e) {
                // Skip this pair if there's an error
                continue;
            }
        }
        
        return validPairs;
    }
    
    /**
     * Find a reachable destination from source using BFS with random exploration
     */
    private static int findReachableDestination(int source, Map<Integer, Node> allNodes) {
        Set<Integer> visited = new HashSet<>();
        List<Integer> frontier = new ArrayList<>();
        frontier.add(source);
        visited.add(source);
        
        int lastReachable = -1;
        int hops = 0;
        
        // BFS to find nodes at various distances
        while (!frontier.isEmpty() && hops < MAX_HOP_DISTANCE) {
            List<Integer> nextFrontier = new ArrayList<>();
            
            for (int current : frontier) {
                Node node = allNodes.get(current);
                if (node == null) continue;
                
                Map<Integer, Edge> outgoing = node.get_outgoing_edges();
                if (outgoing == null) continue;
                
                for (Integer neighbor : outgoing.keySet()) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        nextFrontier.add(neighbor);
                        
                        // After 15+ hops, start considering nodes as potential destinations
                        if (hops >= 15 && random.nextDouble() < 0.1) {
                            lastReachable = neighbor;
                        }
                    }
                }
            }
            
            frontier = nextFrontier;
            hops++;
            
            // Keep track of nodes at good distances (20-40 hops)
            if (hops >= 20 && hops <= 40 && !frontier.isEmpty()) {
                lastReachable = frontier.get(random.nextInt(frontier.size()));
            }
        }
        
        return lastReachable;
    }
    
    /**
     * Evaluate a source-destination pair for diverse paths
     * Returns null if the pair doesn't meet criteria
     */
    private static DiversePathPair evaluatePair(int source, int dest, double departureTime, double budget) 
            throws InterruptedException, ExecutionException {
        
        // Run query with Wideness mode
        Result widenessResult = BidirectionalAstar.runSingleQuery(
            source, dest, departureTime, INTERVAL_DURATION, budget, RoutingMode.WIDENESS_ONLY);
        
        if (widenessResult == null || !widenessResult.isPathFound()) {
            return null;
        }
        
        // Run query with MinTurns mode
        Result minTurnsResult = BidirectionalAstar.runSingleQuery(
            source, dest, departureTime, INTERVAL_DURATION, budget, RoutingMode.MIN_TURNS_ONLY);
        
        if (minTurnsResult == null || !minTurnsResult.isPathFound()) {
            return null;
        }
        
        // Check if paths are different
        if (!arePathsDifferent(widenessResult, minTurnsResult)) {
            return null;
        }
        
        // Run query with Pareto mode
        Result paretoResult = BidirectionalAstar.runSingleQuery(
            source, dest, departureTime, INTERVAL_DURATION, budget, RoutingMode.WIDENESS_AND_TURNS);
        
        if (paretoResult == null || !paretoResult.isPathFound()) {
            return null;
        }
        
        // Check for multiple Pareto optimal paths
        int paretoCount = paretoResult.hasParetoOptimalPaths() ? paretoResult.getParetoPathCount() : 1;
        if (paretoCount < 2) {
            return null;  // Need at least 2 Pareto optimal paths
        }
        
        // Valid pair found!
        return new DiversePathPair(source, dest, departureTime, budget, 
            widenessResult, minTurnsResult, paretoResult, paretoCount);
    }
    
    /**
     * Check if two paths are different by comparing node sequences
     */
    private static boolean arePathsDifferent(Result path1, Result path2) {
        List<Integer> nodes1 = path1.getPathNodes();
        List<Integer> nodes2 = path2.getPathNodes();
        
        if (nodes1 == null || nodes2 == null) return false;
        if (nodes1.size() != nodes2.size()) return true;
        
        // Check if node sequences are identical
        for (int i = 0; i < nodes1.size(); i++) {
            if (!nodes1.get(i).equals(nodes2.get(i))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get a random rush hour departure time
     */
    private static double getRandomRushHourTime() {
        // 50% morning rush, 50% evening rush
        if (random.nextBoolean()) {
            return MORNING_RUSH_START + random.nextInt(MORNING_RUSH_END - MORNING_RUSH_START);
        } else {
            return EVENING_RUSH_START + random.nextInt(EVENING_RUSH_END - EVENING_RUSH_START);
        }
    }
    
    /**
     * Write results to output file
     */
    private static void writeResultsToFile(List<DiversePathPair> pairs, String filename) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Header
            writer.write("═══════════════════════════════════════════════════════════════════════════════\n");
            writer.write("                    ParetoPathFinder Results\n");
            writer.write("                    Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n");
            writer.write("                    Dataset: London (" + LONDON_VERTEX_COUNT + " nodes)\n");
            writer.write("═══════════════════════════════════════════════════════════════════════════════\n\n");
            
            writer.write("Total pairs found: " + pairs.size() + "\n");
            writer.write("Total pairs checked: " + totalPairsChecked + "\n\n");
            
            // Write each pair
            for (int i = 0; i < pairs.size(); i++) {
                DiversePathPair pair = pairs.get(i);
                writer.write("───────────────────────────────────────────────────────────────────────────────\n");
                writer.write("PAIR #" + (i + 1) + "\n");
                writer.write("───────────────────────────────────────────────────────────────────────────────\n");
                writer.write(String.format("Source: %d%n", pair.source));
                writer.write(String.format("Destination: %d%n", pair.dest));
                writer.write(String.format("Departure Time: %.0f minutes (%.0f:%02.0f)%n", 
                    pair.departureTime, Math.floor(pair.departureTime / 60), pair.departureTime % 60));
                writer.write(String.format("Budget: %.1f minutes%n%n", pair.budget));
                
                // Wideness path
                writer.write("=== MAXIMIZE WIDENESS PATH ===\n");
                writePathDetails(writer, pair.widenessPath);
                
                // MinTurns path
                writer.write("\n=== MINIMIZE TURNS PATH ===\n");
                writePathDetails(writer, pair.minTurnsPath);
                
                // Pareto paths
                writer.write("\n=== PARETO OPTIMAL PATHS (" + pair.paretoPathCount + " paths) ===\n");
                if (pair.paretoResult.hasParetoOptimalPaths()) {
                    for (int j = 0; j < pair.paretoResult.getParetoPathCount(); j++) {
                        Result paretoPath = pair.paretoResult.getParetoPath(j);
                        if (paretoPath != null) {
                            writer.write(String.format("\n--- Pareto Path #%d ---%n", j + 1));
                            writePathDetails(writer, paretoPath);
                        }
                    }
                } else {
                    writePathDetails(writer, pair.paretoResult);
                }
                
                writer.write("\n");
            }
            
            writer.write("═══════════════════════════════════════════════════════════════════════════════\n");
            writer.write("                              END OF RESULTS\n");
            writer.write("═══════════════════════════════════════════════════════════════════════════════\n");
        }
        
        System.out.println("[Output] Results written to: " + filename);
    }
    
    /**
     * Write path details to the output file
     */
    private static void writePathDetails(BufferedWriter writer, Result result) throws IOException {
        writer.write(String.format("  Wideness Score: %.2f%%%n", result.get_score()));
        writer.write(String.format("  Right Turns: %d%n", result.get_right_turns()));
        writer.write(String.format("  Sharp Turns: %d%n", result.get_sharp_turns()));
        writer.write(String.format("  Travel Time: %.2f minutes%n", result.get_travel_time()));
        writer.write(String.format("  Suggested Departure: %.2f minutes%n", result.get_departureTime()));
        writer.write(String.format("  Path Length: %d nodes%n", result.getPathLength()));
        writer.write(String.format("  Wide Edges: %d%n", result.getWideEdgeCount()));
        
        // Write path node sequence
        List<Integer> pathNodes = result.getPathNodes();
        if (pathNodes != null && !pathNodes.isEmpty()) {
            writer.write("  Path Nodes: ");
            int maxNodesToShow = Math.min(20, pathNodes.size());
            for (int i = 0; i < maxNodesToShow; i++) {
                writer.write(pathNodes.get(i).toString());
                if (i < maxNodesToShow - 1) {
                    writer.write(" -> ");
                }
            }
            if (pathNodes.size() > maxNodesToShow) {
                writer.write(" ... [" + (pathNodes.size() - maxNodesToShow) + " more nodes]");
            }
            writer.write("\n");
        }
    }
    
    /**
     * Data class to hold a diverse path pair and its results
     */
    private static class DiversePathPair {
        final int source;
        final int dest;
        final double departureTime;
        final double budget;
        final Result widenessPath;
        final Result minTurnsPath;
        final Result paretoResult;
        final int paretoPathCount;
        
        DiversePathPair(int source, int dest, double departureTime, double budget,
                        Result widenessPath, Result minTurnsPath, Result paretoResult, int paretoPathCount) {
            this.source = source;
            this.dest = dest;
            this.departureTime = departureTime;
            this.budget = budget;
            this.widenessPath = widenessPath;
            this.minTurnsPath = minTurnsPath;
            this.paretoResult = paretoResult;
            this.paretoPathCount = paretoPathCount;
        }
    }
}
