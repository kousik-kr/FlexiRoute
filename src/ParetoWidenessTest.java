import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import models.RoutingMode;

/**
 * Test script to find pairs with positive wideness percentage in Pareto optimal mode
 */
public class ParetoWidenessTest {
    public static void main(String[] args) {
        String csvFile = "pairs_under_60.csv";
        
        try {
            // Initialize the system - configure defaults first
            BidirectionalAstar.configureDefaults();
            
            // Load the graph from dataset folder
            boolean loaded = BidirectionalAstar.loadGraphFromDisk("dataset", null);
            if (!loaded) {
                System.err.println("Failed to load graph!");
                return;
            }
            
            System.out.println("Testing pairs from " + csvFile + " for positive wideness in Pareto mode...\n");
            System.out.println("Pairs with positive wideness percentage:");
            System.out.println("=========================================");
            
            BufferedReader reader = new BufferedReader(new FileReader(csvFile));
            String line;
            reader.readLine(); // Skip header
            
            List<String[]> positivePairs = new ArrayList<>();
            int totalPairs = 0;
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 3) continue;
                
                int source = Integer.parseInt(parts[0].trim());
                int destination = Integer.parseInt(parts[1].trim());
                double fastestCost = Double.parseDouble(parts[2].trim());
                
                totalPairs++;
                
                // Run the algorithm with Pareto mode (WIDENESS_AND_TURNS)
                // Parameters: source, destination, departureMinutes, intervalMinutes, budgetMinutes, routingMode
                double departure = 0;  // Start at midnight
                double interval = 10;  // 10 minute interval
                double budget = fastestCost * 1.5;  // Budget is 50% more than fastest
                
                try {
                    Result result = BidirectionalAstar.runSingleQuery(
                        source, destination, departure, interval, budget, RoutingMode.WIDENESS_AND_TURNS);
                    
                    if (result != null && result.get_score() > 0) {
                        System.out.printf("Source: %d, Destination: %d, Wideness: %.2f%%, Turns: %d\n", 
                            source, destination, result.get_score(), result.get_right_turns());
                        positivePairs.add(new String[]{String.valueOf(source), String.valueOf(destination), 
                            String.format("%.2f", result.get_score()), String.valueOf(result.get_right_turns())});
                    }
                } catch (Exception e) {
                    System.err.println("Error processing pair " + source + " -> " + destination + ": " + e.getMessage());
                }
            }
            
            reader.close();
            
            System.out.println("\n=========================================");
            System.out.println("Total pairs tested: " + totalPairs);
            System.out.println("Total pairs with positive wideness: " + positivePairs.size());
            
            // Save results to output file
            BufferedWriter writer = new BufferedWriter(new FileWriter("positive_wideness_pairs.csv"));
            writer.write("source,destination,wideness_percentage,right_turns\n");
            for (String[] pair : positivePairs) {
                writer.write(pair[0] + "," + pair[1] + "," + pair[2] + "," + pair[3] + "\n");
            }
            writer.close();
            System.out.println("Results saved to positive_wideness_pairs.csv");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
