package managers;

import models.QueryResult;
import models.RoutingMode;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 📁 FlexiRoute Log Manager
 * 
 * Handles persistent storage of query results as beautifully formatted log files.
 * Each query generates a unique log file with comprehensive details including:
 * - Query parameters (source, destination, departure, budget)
 * - Routing mode and algorithm settings
 * - Path statistics (distance, nodes, wide roads, turns)
 * - Pareto optimal paths (if applicable)
 * - Execution metrics
 * 
 * Logs are stored in the 'logs/' directory with timestamps for easy retrieval.
 */
public class LogManager {
    
    private static final String LOG_DIRECTORY = "logs";
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");
    private static final DateTimeFormatter DISPLAY_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Singleton instance
    private static LogManager instance;
    
    private final Path logDir;
    
    // Session tracking - same source/dest queries append to same file
    private String currentSessionLogPath;
    private int currentSessionSource = -1;
    private int currentSessionDest = -1;
    private double currentSessionDeparture = -1;
    private double currentSessionBudget = -1;
    private int appendCount = 0;
    
    private LogManager() {
        logDir = Paths.get(LOG_DIRECTORY);
        ensureLogDirectory();
    }
    
    public static synchronized LogManager getInstance() {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }
    
    private void ensureLogDirectory() {
        try {
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not create log directory: " + e.getMessage());
        }
    }
    
    /**
     * Save a query result to a formatted log file.
     * If the same source/dest/departure/budget query is run with a different mode,
     * the result is appended to the existing log file.
     * @return The path to the created/updated log file, or null if failed
     */
    public String saveQueryLog(QueryResult result) {
        if (result == null) return null;
        
        // Check if this is part of the same query session (same source, dest, departure, budget)
        boolean isSameSession = isSameQuerySession(result);
        
        if (isSameSession && currentSessionLogPath != null) {
            // Append to existing log file
            return appendToLog(result);
        } else {
            // Start a new session with a new log file
            return createNewLog(result);
        }
    }
    
    /**
     * Check if this query is part of the same session (same source, dest, departure, budget)
     */
    private boolean isSameQuerySession(QueryResult result) {
        return currentSessionSource == result.getSourceNode() &&
               currentSessionDest == result.getDestinationNode() &&
               Math.abs(currentSessionDeparture - result.getDepartureTime()) < 0.01 &&
               Math.abs(currentSessionBudget - result.getBudget()) < 0.01;
    }
    
    /**
     * Create a new log file for a new query session
     */
    private String createNewLog(QueryResult result) {
        // Update session tracking
        currentSessionSource = result.getSourceNode();
        currentSessionDest = result.getDestinationNode();
        currentSessionDeparture = result.getDepartureTime();
        currentSessionBudget = result.getBudget();
        appendCount = 0;
        
        String filename = generateFilename(result);
        Path logFile = logDir.resolve(filename);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile.toFile()))) {
            writeLogContent(writer, result);
            currentSessionLogPath = logFile.toString();
            return currentSessionLogPath;
        } catch (IOException e) {
            System.err.println("Error saving log file: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Append a new routing mode result to the existing session log file
     */
    private String appendToLog(QueryResult result) {
        if (currentSessionLogPath == null) return createNewLog(result);
        
        appendCount++;
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(currentSessionLogPath, true))) {
            writeAppendedContent(writer, result, appendCount);
            return currentSessionLogPath;
        } catch (IOException e) {
            System.err.println("Error appending to log file: " + e.getMessage());
            // Fall back to creating new file
            return createNewLog(result);
        }
    }
    
    /**
     * Reset the session tracking (call when starting a completely new query)
     */
    public void resetSession() {
        currentSessionLogPath = null;
        currentSessionSource = -1;
        currentSessionDest = -1;
        currentSessionDeparture = -1;
        currentSessionBudget = -1;
        appendCount = 0;
    }
    
    /**
     * Get the current session log path
     */
    public String getCurrentSessionLogPath() {
        return currentSessionLogPath;
    }
    
    private String generateFilename(QueryResult result) {
        String timestamp = result.getTimestamp().format(FILE_TIMESTAMP);
        String mode = result.getRoutingMode() != null ? 
            result.getRoutingMode().name().toLowerCase() : "standard";
        String status = result.isSuccess() ? "success" : "failed";
        return String.format("query_%s_%s_%s.log", timestamp, mode, status);
    }
    
    private void writeLogContent(PrintWriter writer, QueryResult result) {
        String timestamp = result.getTimestamp().format(DISPLAY_TIMESTAMP);
        
        // Header with decorative border
        writer.println("╔══════════════════════════════════════════════════════════════════════════════╗");
        writer.println("║                                                                              ║");
        writer.println("║     🗺️  F L E X I R O U T E   Q U E R Y   L O G                              ║");
        writer.println("║                                                                              ║");
        writer.println("╠══════════════════════════════════════════════════════════════════════════════╣");
        writer.printf( "║  📅 Timestamp:    %-58s ║%n", timestamp);
        writer.printf( "║  🎯 Status:       %-58s ║%n", result.isSuccess() ? "✅ SUCCESS" : "❌ FAILED");
        writer.println("╚══════════════════════════════════════════════════════════════════════════════╝");
        writer.println();
        
        // Query Parameters Section
        writer.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        writer.println("│  📋 QUERY PARAMETERS                                                         │");
        writer.println("├──────────────────────────────────────────────────────────────────────────────┤");
        writer.printf( "│  Source Node:        %-56d │%n", result.getSourceNode());
        writer.printf( "│  Destination Node:   %-56d │%n", result.getDestinationNode());
        writer.printf( "│  Departure Time:     %-56s │%n", formatTime(result.getDepartureTime()));
        writer.printf( "│  Interval Duration:  %-56s │%n", String.format("%.2f minutes", result.getIntervalDuration()));
        writer.printf( "│  Budget:             %-56s │%n", String.format("%.2f units", result.getBudget()));
        writer.println("└──────────────────────────────────────────────────────────────────────────────┘");
        writer.println();
        
        // Routing Mode Section
        writer.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        writer.println("│  🎛️ ROUTING MODE                                                             │");
        writer.println("├──────────────────────────────────────────────────────────────────────────────┤");
        RoutingMode mode = result.getRoutingMode();
        if (mode != null) {
            writer.printf("│  Mode:               %-56s │%n", mode.getDisplayName());
            writer.printf("│  Description:        %-56s │%n", getRoutingModeDescription(mode));
            writer.printf("│  Key:                %-56s │%n", mode.name());
        } else {
            writer.printf("│  Mode:               %-56s │%n", "Standard (Default)");
        }
        writer.println("└──────────────────────────────────────────────────────────────────────────────┘");
        writer.println();
        
        if (result.isSuccess()) {
            writeSuccessDetails(writer, result);
        } else {
            writeFailureDetails(writer, result);
        }
        
        // Execution Metrics
        writer.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        writer.println("│  ⏱️ EXECUTION METRICS                                                        │");
        writer.println("├──────────────────────────────────────────────────────────────────────────────┤");
        writer.printf( "│  Execution Time:     %-56s │%n", String.format("%d ms", result.getExecutionTimeMs()));
        writer.printf( "│  Algorithm:          %-56s │%n", "Bidirectional Labeling with A* Heuristic");
        writer.println("└──────────────────────────────────────────────────────────────────────────────┘");
        writer.println();
        
        // Footer
        writer.println("═══════════════════════════════════════════════════════════════════════════════");
        writer.println("                         FlexiRoute Navigator - Log End                        ");
        writer.println("═══════════════════════════════════════════════════════════════════════════════");
    }
    
    /**
     * Write appended content for a mode change in the same query session
     */
    private void writeAppendedContent(PrintWriter writer, QueryResult result, int modeChangeNumber) {
        String timestamp = result.getTimestamp().format(DISPLAY_TIMESTAMP);
        
        writer.println();
        writer.println();
        
        // Mode change separator
        writer.println("╔══════════════════════════════════════════════════════════════════════════════╗");
        writer.println("║                                                                              ║");
        writer.printf( "║     🔄  M O D E   C H A N G E   # %-2d                                         ║%n", modeChangeNumber);
        writer.println("║                                                                              ║");
        writer.println("╠══════════════════════════════════════════════════════════════════════════════╣");
        writer.printf( "║  📅 Timestamp:    %-58s ║%n", timestamp);
        writer.printf( "║  🎯 Status:       %-58s ║%n", result.isSuccess() ? "✅ SUCCESS" : "❌ FAILED");
        writer.println("╚══════════════════════════════════════════════════════════════════════════════╝");
        writer.println();
        
        // New Routing Mode Section
        writer.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        writer.println("│  🎛️ NEW ROUTING MODE                                                         │");
        writer.println("├──────────────────────────────────────────────────────────────────────────────┤");
        RoutingMode mode = result.getRoutingMode();
        if (mode != null) {
            writer.printf("│  Mode:               %-56s │%n", mode.getDisplayName());
            writer.printf("│  Description:        %-56s │%n", getRoutingModeDescription(mode));
            writer.printf("│  Key:                %-56s │%n", mode.name());
        } else {
            writer.printf("│  Mode:               %-56s │%n", "Standard (Default)");
        }
        writer.println("└──────────────────────────────────────────────────────────────────────────────┘");
        writer.println();
        
        if (result.isSuccess()) {
            writeSuccessDetails(writer, result);
        } else {
            writeFailureDetails(writer, result);
        }
        
        // Execution Metrics
        writer.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        writer.println("│  ⏱️ EXECUTION METRICS                                                        │");
        writer.println("├──────────────────────────────────────────────────────────────────────────────┤");
        writer.printf( "│  Execution Time:     %-56s │%n", String.format("%d ms", result.getExecutionTimeMs()));
        writer.printf( "│  Algorithm:          %-56s │%n", "Bidirectional Labeling with A* Heuristic");
        writer.println("└──────────────────────────────────────────────────────────────────────────────┘");
        writer.println();
        
        // Mode change footer
        writer.println("───────────────────────────────────────────────────────────────────────────────");
        writer.printf( "                      End of Mode Change #%d Results                           %n", modeChangeNumber);
        writer.println("───────────────────────────────────────────────────────────────────────────────");
    }
    
    private void writeSuccessDetails(PrintWriter writer, QueryResult result) {
        // Path Statistics
        writer.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        writer.println("│  📊 PATH STATISTICS                                                          │");
        writer.println("├──────────────────────────────────────────────────────────────────────────────┤");
        writer.printf( "│  Total Distance:     %-56s │%n", String.format("%.2f units", result.getTotalDistance()));
        writer.printf( "│  Travel Time:        %-56s │%n", String.format("%.2f minutes", result.getTravelTime()));
        writer.printf( "│  Path Length:        %-56s │%n", String.format("%d nodes", result.getPathNodes().size()));
        writer.printf( "│  Wide Road %%:        %-56s │%n", String.format("%.2f%%", result.getWideRoadPercentage()));
        writer.printf( "│  Wide Edges:         %-56s │%n", String.format("%d edges", result.getWideEdgeCount()));
        writer.printf( "│  Right Turns:        %-56s │%n", String.format("%d turns", result.getRightTurns()));
        writer.println("└──────────────────────────────────────────────────────────────────────────────┘");
        writer.println();
        
        // Optimal Departure Time
        if (result.getOptimalDepartureTime() > 0) {
            writer.println("┌──────────────────────────────────────────────────────────────────────────────┐");
            writer.println("│  ⭐ OPTIMAL DEPARTURE TIME                                                   │");
            writer.println("├──────────────────────────────────────────────────────────────────────────────┤");
            writer.printf( "│  Suggested:          %-56s │%n", formatTime(result.getOptimalDepartureTime()));
            writer.printf( "│  Raw Value:          %-56s │%n", String.format("%.2f minutes from midnight", result.getOptimalDepartureTime()));
            writer.println("└──────────────────────────────────────────────────────────────────────────────┘");
            writer.println();
        }
        
        // Path Nodes (abbreviated if long)
        writer.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        writer.println("│  🛤️ PATH NODES                                                               │");
        writer.println("├──────────────────────────────────────────────────────────────────────────────┤");
        List<Integer> pathNodes = result.getPathNodes();
        if (pathNodes != null && !pathNodes.isEmpty()) {
            if (pathNodes.size() <= 10) {
                writer.printf("│  Full Path:          %-56s │%n", formatPathNodes(pathNodes));
            } else {
                writer.printf("│  Path Start:         %-56s │%n", formatPathStart(pathNodes));
                writer.printf("│  ...                 %-56s │%n", String.format("(%d intermediate nodes)", pathNodes.size() - 4));
                writer.printf("│  Path End:           %-56s │%n", formatPathEnd(pathNodes));
            }
        }
        writer.println("└──────────────────────────────────────────────────────────────────────────────┘");
        writer.println();
        
        // Pareto Paths (if available)
        List<List<Integer>> paretoPaths = result.getParetoPaths();
        List<double[]> paretoMetrics = result.getParetoMetrics();
        if (paretoPaths != null && !paretoPaths.isEmpty()) {
            writer.println("╔══════════════════════════════════════════════════════════════════════════════╗");
            writer.println("║  🎯 PARETO OPTIMAL PATHS                                                     ║");
            writer.printf( "║  Found %d non-dominated solutions representing trade-offs between           ║%n", paretoPaths.size());
            writer.println("║  maximizing wide road usage and minimizing right turns.                      ║");
            writer.println("╠══════════════════════════════════════════════════════════════════════════════╣");
            
            for (int i = 0; i < paretoPaths.size(); i++) {
                List<Integer> path = paretoPaths.get(i);
                double[] metrics = paretoMetrics != null && i < paretoMetrics.size() ? paretoMetrics.get(i) : null;
                
                writer.printf("║  ┌─ PATH #%d ─────────────────────────────────────────────────────────────┐  ║%n", i + 1);
                if (metrics != null) {
                    writer.printf("║  │  Wide Road %%:  %.2f%%                                                   │  ║%n", metrics[0]);
                    writer.printf("║  │  Right Turns:  %.0f                                                      │  ║%n", metrics[1]);
                    if (metrics.length > 2) {
                        writer.printf("║  │  Distance:     %.2f units                                              │  ║%n", metrics[2]);
                    }
                }
                writer.printf("║  │  Path Length:  %d nodes                                                 │  ║%n", path.size());
                writer.printf("║  │  Route:        %s  │  ║%n", formatPathSummary(path));
                writer.println("║  └──────────────────────────────────────────────────────────────────────────┘  ║");
            }
            writer.println("╚══════════════════════════════════════════════════════════════════════════════╝");
            writer.println();
        }
    }
    
    private void writeFailureDetails(PrintWriter writer, QueryResult result) {
        writer.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        writer.println("│  ❌ FAILURE DETAILS                                                          │");
        writer.println("├──────────────────────────────────────────────────────────────────────────────┤");
        String errorMsg = result.getErrorMessage() != null ? result.getErrorMessage() : "Unknown error";
        // Word wrap error message if too long
        List<String> wrappedLines = wrapText(errorMsg, 54);
        for (int i = 0; i < wrappedLines.size(); i++) {
            if (i == 0) {
                writer.printf("│  Error Message:      %-56s │%n", wrappedLines.get(i));
            } else {
                writer.printf("│                      %-56s │%n", wrappedLines.get(i));
            }
        }
        writer.println("└──────────────────────────────────────────────────────────────────────────────┘");
        writer.println();
    }
    
    private String formatTime(double minutes) {
        int hours = (int) (minutes / 60);
        int mins = (int) (minutes % 60);
        return String.format("%02d:%02d (%d hours %d minutes)", hours, mins, hours, mins);
    }
    
    private String formatPathNodes(List<Integer> nodes) {
        if (nodes == null || nodes.isEmpty()) return "None";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodes.size(); i++) {
            if (i > 0) sb.append(" → ");
            sb.append(nodes.get(i));
        }
        return sb.length() > 50 ? sb.substring(0, 47) + "..." : sb.toString();
    }
    
    private String formatPathStart(List<Integer> nodes) {
        if (nodes == null || nodes.size() < 2) return "None";
        return nodes.get(0) + " → " + nodes.get(1) + " → ...";
    }
    
    private String formatPathEnd(List<Integer> nodes) {
        if (nodes == null || nodes.size() < 2) return "None";
        int n = nodes.size();
        return "... → " + nodes.get(n - 2) + " → " + nodes.get(n - 1);
    }
    
    private String formatPathSummary(List<Integer> nodes) {
        if (nodes == null || nodes.isEmpty()) return "Empty";
        if (nodes.size() <= 3) return formatPathNodes(nodes);
        return nodes.get(0) + " → ... → " + nodes.get(nodes.size() - 1);
    }
    
    private String getRoutingModeDescription(RoutingMode mode) {
        return switch (mode) {
            case WIDENESS_AND_TURNS -> "Trade-off: Wide roads vs. Right turns (Pareto)";
            case MIN_TURNS_ONLY -> "Minimize right turns only";
            case WIDENESS_ONLY -> "Maximize wide road percentage only";
            default -> "Standard routing";
        };
    }
    
    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }
        
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder();
            }
            if (currentLine.length() > 0) {
                currentLine.append(" ");
            }
            currentLine.append(word);
        }
        
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines;
    }
    
    /**
     * Read a log file content
     */
    public String readLogFile(String logFilePath) {
        try {
            return Files.readString(Paths.get(logFilePath));
        } catch (IOException e) {
            return "Error reading log file: " + e.getMessage();
        }
    }
    
    /**
     * Get all log files sorted by date (newest first)
     */
    public List<Path> getAllLogFiles() {
        try {
            return Files.list(logDir)
                .filter(p -> p.toString().endsWith(".log"))
                .sorted(Comparator.reverseOrder())
                .toList();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * Delete a log file
     */
    public boolean deleteLogFile(String logFilePath) {
        try {
            return Files.deleteIfExists(Paths.get(logFilePath));
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Clean up old logs (keep only last N)
     */
    public void cleanupOldLogs(int keepCount) {
        try {
            List<Path> allLogs = Files.list(logDir)
                .filter(p -> p.toString().endsWith(".log"))
                .sorted(Comparator.reverseOrder())
                .toList();
            
            for (int i = keepCount; i < allLogs.size(); i++) {
                Files.deleteIfExists(allLogs.get(i));
            }
        } catch (IOException e) {
            System.err.println("Error cleaning up logs: " + e.getMessage());
        }
    }
    
    /**
     * Get the log directory path
     */
    public String getLogDirectory() {
        return logDir.toAbsolutePath().toString();
    }
}
