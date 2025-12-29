import managers.*;
import models.QueryResult;
import ui.components.*;
import ui.panels.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * World-Class Wide-Path GUI Application - Main Entry Point
 * 
 * Architecture:
 * - Modular design with separate packages for UI, managers, and models
 * - Thread-safe concurrent operations
 * - Modern Material Design inspired interface
 * - Comprehensive metrics and history tracking
 * 
 * @author Wide-Path Team
 * @version 2.0
 */
public class GuiLauncher {
    // Core Application Components
    private final JFrame frame;
    private final ExecutorService executorService;
    private final QueryHistoryManager historyManager;
    private final MetricsCollector metricsCollector;
    private final ThemeManager themeManager;
    
    // UI Panels
    private QueryInputPanel inputPanel;
    private JTextPane outputPane;
    private AdvancedMapPanel mapPanel;
    private MetricsDashboard metricsDashboard;
    private QueryHistoryPanel historyPanel;
    private StatusBar statusBar;
    private JProgressBar progressBar;
    
    // State Management
    private volatile boolean isQueryRunning = false;
    private List<Integer> currentPath = Collections.emptyList();
    private List<Integer> currentWideEdges = Collections.emptyList();

    public static void main(String[] args) {
        // Set system look and feel properties for better rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Headless environment detected. GUI cannot be displayed.");
            return;
        }
        
        // Set modern Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                GuiLauncher launcher = new GuiLauncher();
                launcher.start();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Failed to launch application: " + e.getMessage(),
                    "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private JTabbedPane tabbedPane;

    public GuiLauncher() {
        this.frame = new JFrame("Wide-Path Pro - Advanced Pathfinding Analysis");
        this.executorService = Executors.newFixedThreadPool(4);
        this.historyManager = new QueryHistoryManager();
        this.metricsCollector = new MetricsCollector();
        this.themeManager = new ThemeManager();
        
        // Configure frame
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });
        
        setupKeyboardShortcuts();
    }

    private void start() {
        // Check if dataset exists, if not offer to download from Google Drive
        if (!GoogleDriveConfigHelper.checkDatasetExists()) {
            int choice = JOptionPane.showConfirmDialog(frame,
                    "Dataset files not found.\n" +
                    "Would you like to download them from Google Drive?",
                    "Dataset Not Found",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            
            if (choice == JOptionPane.YES_OPTION) {
                boolean downloaded = GoogleDriveDatasetLoader.downloadDataset(frame);
                if (!downloaded) {
                    System.out.println("[GUI] Dataset download cancelled or failed.");
                    return;
                }
            } else {
                System.out.println("[GUI] User chose not to download dataset.");
                return;
            }
        }
        
        // Load graph
        BidirectionalAstar.configureDefaults();
        boolean loaded = BidirectionalAstar.loadGraphFromDisk(null, null);
        if (!loaded) {
            JOptionPane.showMessageDialog(null,
                    "Failed to load graph files. Ensure the configured graph directory points to your dataset.",
                    "Graph Load Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int nodeCount = Graph.get_nodes().size();
        System.out.println("[GUI] Graph loaded. Nodes=" + nodeCount);
        
        initializeUI(nodeCount);
        frame.setVisible(true);
    }

    private void setupKeyboardShortcuts() {
        KeyStroke runShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK);
        
        Action runAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isQueryRunning && inputPanel != null) {
                    // Trigger run button programmatically
                    System.out.println("[GUI] Run query triggered via Ctrl+Enter");
                }
            }
        };
        
        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(runShortcut, "runQuery");
        frame.getRootPane().getActionMap().put("runQuery", runAction);
    }

    private void applyThemeToAllComponents() {
        // Apply theme to main frame and all child components
        applyThemeToContainer(frame.getContentPane());
        
        // Apply to menu bar
        if (frame.getJMenuBar() != null) {
            applyThemeToContainer(frame.getJMenuBar());
        }
        
        // Apply to panels
        if (inputPanel != null) {
            applyThemeToContainer(inputPanel);
        }
        if (mapPanel != null) {
            applyThemeToContainer(mapPanel);
        }
        if (metricsDashboard != null) {
            applyThemeToContainer(metricsDashboard);
        }
        if (historyPanel != null) {
            applyThemeToContainer(historyPanel);
        }
        if (statusBar != null) {
            applyThemeToContainer(statusBar);
        }
        if (tabbedPane != null) {
            applyThemeToContainer(tabbedPane);
            tabbedPane.setBackground(themeManager.getColor("panel"));
            tabbedPane.setForeground(themeManager.getColor("foreground"));
        }
        
        // Force repaint
        frame.repaint();
        frame.revalidate();
    }
    
    private void applyThemeToContainer(Container container) {
        if (container == null) return;
        
        // Apply theme colors based on component type
        boolean isDark = themeManager.isDarkTheme();
        
        container.setBackground(themeManager.getColor("background"));
        container.setForeground(themeManager.getColor("foreground"));
        
        // Recursively apply to all child components
        for (Component comp : container.getComponents()) {
            if (comp instanceof JPanel) {
                comp.setBackground(themeManager.getColor("panel"));
                comp.setForeground(themeManager.getColor("foreground"));
            } else if (comp instanceof JButton) {
                comp.setBackground(themeManager.getColor("primary"));
                comp.setForeground(Color.WHITE);
            } else if (comp instanceof JTextField || comp instanceof JTextArea) {
                comp.setBackground(themeManager.getColor("backgroundElevated"));
                comp.setForeground(themeManager.getColor("foreground"));
                comp.setFont(comp.getFont());
            } else if (comp instanceof JLabel) {
                comp.setForeground(themeManager.getColor("foreground"));
            } else if (comp instanceof JScrollPane) {
                comp.setBackground(themeManager.getColor("panel"));
                JScrollPane scrollPane = (JScrollPane) comp;
                if (scrollPane.getViewport() != null) {
                    scrollPane.getViewport().setBackground(themeManager.getColor("backgroundElevated"));
                }
            } else if (comp instanceof JTabbedPane) {
                comp.setBackground(themeManager.getColor("panel"));
                comp.setForeground(themeManager.getColor("foreground"));
            } else if (comp instanceof JMenuBar || comp instanceof JMenu || comp instanceof JMenuItem) {
                comp.setBackground(themeManager.getColor("panel"));
                comp.setForeground(themeManager.getColor("foreground"));
            }
            
            // Recursively apply to nested containers
            if (comp instanceof Container) {
                applyThemeToContainer((Container) comp);
            }
        }
    }
    
    private void shutdown() {
        // Use the same enhanced exit dialog as the button
        exitApplication();
    }

    private void initializeUI(int maxNodeId) {
        frame.setLayout(new BorderLayout(0, 0));
        frame.setPreferredSize(new Dimension(1600, 900));
        
        // Create menu bar
        createMenuBar();
        frame.setJMenuBar(createMenuBar());
        
        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create input panel (left side) with reset and exit callbacks
        inputPanel = new QueryInputPanel(maxNodeId, this::executeQuery, this::visualizeQuery, this::resetQuery, this::exitApplication);
        
        // Create tabbed pane for output and visualization
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        // Results tab
        JPanel resultsPanel = createResultsPanel();
        tabbedPane.addTab("📊 Results", null, resultsPanel, "Query results and statistics");
        
        // Visualization tab
        mapPanel = new AdvancedMapPanel();
        tabbedPane.addTab("🗺️ Visualization", null, mapPanel, "Path visualization and graph explorer");
        
        // Metrics tab
        metricsDashboard = new MetricsDashboard(metricsCollector);
        tabbedPane.addTab("📈 Metrics", null, metricsDashboard, "Performance metrics and analytics");
        
        // History tab
        historyPanel = new QueryHistoryPanel(historyManager);
        tabbedPane.addTab("🕐 History", null, historyPanel, "Query history and comparison");
        
        // Create split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, tabbedPane);
        splitPane.setResizeWeight(0.25);
        splitPane.setDividerSize(8);
        splitPane.setDividerLocation(400);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        // Create status bar
        statusBar = new StatusBar();
        mainPanel.add(statusBar, BorderLayout.SOUTH);
        
        frame.add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        
        statusBar.setMessage("Ready. Graph loaded with " + maxNodeId + " nodes.", StatusBar.MessageType.INFO);
        
        // Show welcome message
        displayWelcomeMessage();
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exportItem = new JMenuItem("Export Results...");
        exportItem.addActionListener(e -> {
            if (historyPanel != null) {
                // Switch to history tab and trigger export
                tabbedPane.setSelectedComponent(historyPanel);
                // Use a timer to ensure tab switch completes before showing export dialog
                javax.swing.Timer timer = new javax.swing.Timer(100, evt -> {
                    historyPanel.exportHistory();
                    ((javax.swing.Timer)evt.getSource()).stop();
                });
                timer.setRepeats(false);
                timer.start();
            } else {
                JOptionPane.showMessageDialog(frame,
                    "No query history available to export.",
                    "Export",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> shutdown());
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // View Menu
        JMenu viewMenu = new JMenu("View");
        JCheckBoxMenuItem darkModeItem = new JCheckBoxMenuItem("Dark Mode");
        darkModeItem.setSelected(themeManager.getCurrentTheme() == ThemeManager.Theme.DARK);
        darkModeItem.addActionListener(e -> {
            themeManager.toggleTheme();
            applyThemeToAllComponents();
            darkModeItem.setSelected(themeManager.getCurrentTheme() == ThemeManager.Theme.DARK);
        });
        viewMenu.add(darkModeItem);
        
        // Add theme submenu for other themes
        JMenu themeMenu = new JMenu("Themes");
        for (ThemeManager.Theme theme : ThemeManager.Theme.values()) {
            JMenuItem themeItem = new JMenuItem(theme.getDisplayName());
            themeItem.setToolTipText(theme.getDescription());
            themeItem.addActionListener(e -> {
                themeManager.setTheme(theme);
                applyThemeToAllComponents();
                darkModeItem.setSelected(themeManager.isDarkTheme());
            });
            themeMenu.add(themeItem);
        }
        viewMenu.add(themeMenu);
        
        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        
        return menuBar;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Output text pane with styled document
        outputPane = new JTextPane();
        outputPane.setEditable(false);
        outputPane.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputPane);
        
        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        
        panel.add(progressBar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void executeQuery(QueryInputPanel.QueryParameters params) {
        if (isQueryRunning) {
            statusBar.setMessage("A query is already running!", StatusBar.MessageType.WARNING);
            return;
        }
        
        // Clear any query preview
        mapPanel.clearQueryPreview();
        
        // Set heuristic mode based on user selection
        if ("Aggressive".equals(params.heuristicMode)) {
            BidirectionalLabeling.setAggressiveMode();
            System.out.println("[GUI] Heuristic Mode: Aggressive (Frontier Threshold = 10)");
        } else {
            BidirectionalLabeling.setBalancedMode();
            System.out.println("[GUI] Heuristic Mode: Balanced (Frontier Threshold = 50)");
        }
        
        isQueryRunning = true;
        inputPanel.setRunEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        statusBar.setMessage("Executing query...", StatusBar.MessageType.INFO);
        
        // Display input parameters
        displayInputParameters(params);
        
        SwingWorker<QueryResult, Void> worker = new SwingWorker<>() {
            @Override
            protected QueryResult doInBackground() {
                long startTime = System.currentTimeMillis();
                
                try {
                    BidirectionalAstar.setIntervalDuration(params.interval);
                    System.out.println("[GUI] Running query " + params.source + " -> " + params.destination);
                    
                    Result result = BidirectionalAstar.runSingleQuery(
                        params.source, params.destination, params.departure, params.interval, params.budget);
                    
                    long executionTime = System.currentTimeMillis() - startTime;
                    
                    if (result != null) {
                        return new QueryResult.Builder()
                            .setSourceNode(params.source)
                            .setDestinationNode(params.destination)
                            .setDepartureTime(params.departure)
                            .setIntervalDuration(params.interval)
                            .setBudget(params.budget)
                            .setActualDepartureTime(result.get_departureTime())
                            .setScore(result.get_score())
                            .setTravelTime(result.get_travel_time())
                            .setRightTurns(result.get_right_turns())
                            .setSharpTurns(result.get_sharp_turns())
                            .setPathNodes(result.get_pathNodes())
                            .setWideEdgeIndices(result.get_wideEdgeIndices())
                            .setExecutionTimeMs(executionTime)
                            .setSuccess(true)
                            .build();
                    } else {
                        return new QueryResult.Builder()
                            .setSourceNode(params.source)
                            .setDestinationNode(params.destination)
                            .setDepartureTime(params.departure)
                            .setIntervalDuration(params.interval)
                            .setBudget(params.budget)
                            .setExecutionTimeMs(executionTime)
                            .setSuccess(false)
                            .setErrorMessage("Query returned null result")
                            .build();
                    }
                } catch (Exception e) {
                    long executionTime = System.currentTimeMillis() - startTime;
                    return new QueryResult.Builder()
                        .setSourceNode(params.source)
                        .setDestinationNode(params.destination)
                        .setDepartureTime(params.departure)
                        .setIntervalDuration(params.interval)
                        .setBudget(params.budget)
                        .setExecutionTimeMs(executionTime)
                        .setSuccess(false)
                        .setErrorMessage(e.getMessage())
                        .build();
                }
            }

            @Override
            protected void done() {
                try {
                    QueryResult result = get();
                    
                    // Update history and metrics
                    historyManager.addQuery(result);
                    metricsCollector.recordQuery(result.isSuccess(), result.getExecutionTimeMs(), 
                        result.getPathNodes() != null ? result.getPathNodes().size() : 0);
                    
                    // Display results
                    displayResults(result);
                    
                    // Update map
                    if (result.isSuccess() && result.getPathNodes() != null) {
                        currentPath = result.getPathNodes();
                        currentWideEdges = result.getWideEdgeIndices();
                        mapPanel.setPath(currentPath, currentWideEdges);
                    }
                    
                    // Update history panel
                    historyPanel.refreshTable();
                    
                    if (result.isSuccess()) {
                        statusBar.setMessage("Query completed successfully in " + result.getExecutionTimeMs() + " ms", 
                            StatusBar.MessageType.SUCCESS);
                        showSuccessAnimation();
                    } else {
                        statusBar.setMessage("Query failed: " + result.getErrorMessage(), StatusBar.MessageType.ERROR);
                    }
                    
                    // Show action buttons after query completion
                    SwingUtilities.invokeLater(() -> inputPanel.showActionButtons());
                } catch (Exception e) {
                    statusBar.setMessage("Error processing results: " + e.getMessage(), StatusBar.MessageType.ERROR);
                } finally {
                    isQueryRunning = false;
                    inputPanel.setRunEnabled(true);
                    progressBar.setVisible(false);
                    progressBar.setIndeterminate(false);
                }
            }
        };
        
        worker.execute();
    }

    private void displayInputParameters(QueryInputPanel.QueryParameters params) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════\n");
        sb.append("        QUERY INPUT PARAMETERS\n");
        sb.append("═══════════════════════════════════════\n\n");
        sb.append(String.format("🎯 Source Node:        %d\n", params.source));
        sb.append(String.format("🏁 Destination Node:   %d\n", params.destination));
        sb.append(String.format("🕐 Departure Time:     %d min (%.2f hrs)\n", params.departure, params.departure/60.0));
        sb.append(String.format("⏱  Interval Duration:  %d min\n", params.interval));
        sb.append(String.format("💰 Budget:             %d min\n", params.budget));
        sb.append(String.format("🧠 Heuristic Mode:     %s (%d frontiers)\n\n", 
            params.heuristicMode, 
            "Aggressive".equals(params.heuristicMode) ? 10 : 50));
        sb.append("Processing query, please wait...\n");
        sb.append("═══════════════════════════════════════\n\n");
        
        outputPane.setText(sb.toString());
    }
    
    private void visualizeQuery(QueryInputPanel.QueryPreview preview) {
        mapPanel.setQueryPreview(preview.source, preview.destination);
        tabbedPane.setSelectedIndex(1); // Switch to visualization tab
    }

    private void displayResults(QueryResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append(outputPane.getText());
        
        if (result.isSuccess()) {
            sb.append("\n═══════════════════════════════════════\n");
            sb.append("          QUERY RESULTS\n");
            sb.append("═══════════════════════════════════════\n\n");
            sb.append(String.format("✓ Route Found!\n\n"));
            sb.append(String.format("📍 Route:              %d → %d\n", result.getSourceNode(), result.getDestinationNode()));
            sb.append(String.format("⏰ Departure:          %.2f min\n", result.getActualDepartureTime()));
            sb.append(String.format("🎯 Score:              %.2f\n", result.getScore()));
            sb.append(String.format("⏱  Travel Time:        %.2f min (%.2f hrs)\n", 
                result.getTravelTime(), result.getTravelTime()/60.0));
            sb.append(String.format("↪  Right Turns:        %d\n", result.getRightTurns()));
            sb.append(String.format("⤴  Sharp Turns:        %d\n", result.getSharpTurns()));
            
            if (result.getPathNodes() != null && !result.getPathNodes().isEmpty()) {
                sb.append(String.format("\n📊 Path Statistics:\n"));
                sb.append(String.format("   Nodes in path:     %d\n", result.getPathNodes().size()));
                sb.append(String.format("   Wide edges:        %d\n", 
                    result.getWideEdgeIndices() != null ? result.getWideEdgeIndices().size() : 0));

                // Emit full path for external plotting: node IDs and lat,lon pairs
                sb.append("\n🛣️ Path (node IDs):\n");
                sb.append(formatPathIds(result.getPathNodes()));

                sb.append("\n🗺️ Path (lat, lon):\n");
                sb.append(formatPathCoords(result.getPathNodes()));

                String pathFile = writePathToFile(result.getPathNodes());
                if (pathFile != null) {
                    sb.append(String.format("\n💾 Full path saved to: %s\n", pathFile));
                }
            }
            
            sb.append(String.format("\n⚡ Execution Time:     %d ms\n", result.getExecutionTimeMs()));
            sb.append("\n═══════════════════════════════════════\n");
        } else {
            sb.append("\n═══════════════════════════════════════\n");
            sb.append("          QUERY FAILED\n");
            sb.append("═══════════════════════════════════════\n\n");
            sb.append(String.format("✗ Error: %s\n", result.getErrorMessage()));
            sb.append(String.format("⚡ Execution Time: %d ms\n", result.getExecutionTimeMs()));
            sb.append("\n═══════════════════════════════════════\n");
        }
        
        outputPane.setText(sb.toString());
        outputPane.setCaretPosition(outputPane.getDocument().getLength());
    }

    private void showAboutDialog() {
        String message = "Wide-Path Pro v2.0\n\n" +
                        "Advanced Pathfinding Analysis System\n\n" +
                        "Features:\n" +
                        "• Modern Material Design UI\n" +
                        "• Real-time performance metrics\n" +
                        "• Query history and analytics\n" +
                        "• Advanced path visualization\n" +
                        "• Multiple rendering modes\n" +
                        "• Query reset and session management\n\n" +
                        "© 2025 Wide-Path Team";
        
        JOptionPane.showMessageDialog(frame, message, "About Wide-Path Pro", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetQuery() {
        // Animate reset with visual feedback
        statusBar.setMessage("Resetting query session...", StatusBar.MessageType.INFO);
        
        SwingWorker<Void, Void> resetWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    Thread.sleep(300); // Brief pause for visual feedback
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            }

            @Override
            protected void done() {
                // Clear output pane with fade effect
                outputPane.setText("");
                
                // Clear map visualization
                currentPath = Collections.emptyList();
                currentWideEdges = Collections.emptyList();
                mapPanel.setPath(currentPath, currentWideEdges);
                
                // Switch to input tab
                tabbedPane.setSelectedIndex(0);
                
                // Reset input panel
                inputPanel.resetToInitialState();
                
                // Update status
                statusBar.setMessage("Ready for new query. All fields reset.", StatusBar.MessageType.SUCCESS);
                
                // Show welcome message
                displayWelcomeMessage();
            }
        };
        
        resetWorker.execute();
    }

    private void displayWelcomeMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════\n");
        sb.append("    WIDE-PATH PRO - READY\n");
        sb.append("═══════════════════════════════════════\n\n");
        sb.append("🎯 Enter query parameters and click 'Run Query'\n");
        sb.append("📊 View results in the tabs above\n");
        sb.append("🗺️  Visualize paths with multiple rendering modes\n");
        sb.append("📈 Track performance metrics\n");
        sb.append("🕐 Review query history\n\n");
        sb.append("Tip: Use Ctrl+Enter to run queries quickly\n");
        sb.append("═══════════════════════════════════════\n");
        
        outputPane.setText(sb.toString());
    }

    private String formatPathIds(List<Integer> path) {
        if (path == null || path.isEmpty()) return "   (no path)\n";
        StringBuilder sb = new StringBuilder();
        sb.append("   ");
        int perLine = 20;
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i));
            if (i < path.size() - 1) sb.append(" -> ");
            if ((i + 1) % perLine == 0 && i < path.size() - 1) {
                sb.append("\n   ");
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    private String formatPathCoords(List<Integer> path) {
        if (path == null || path.isEmpty()) return "   (no coordinates)\n";
        StringBuilder sb = new StringBuilder();
        sb.append("   ");
        int perLine = 6;
        for (int i = 0; i < path.size(); i++) {
            int nodeId = path.get(i);
            Node node = Graph.get_node(nodeId);
            if (node != null) {
                sb.append(String.format("%.6f,%.6f", node.get_latitude(), node.get_longitude()));
            } else {
                sb.append(nodeId).append("(missing)");
            }
            if (i < path.size() - 1) sb.append(" -> ");
            if ((i + 1) % perLine == 0 && i < path.size() - 1) {
                sb.append("\n   ");
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    private String writePathToFile(List<Integer> path) {
        if (path == null || path.isEmpty()) return null;
        try {
            java.nio.file.Path dir = java.nio.file.Paths.get("output");
            java.nio.file.Files.createDirectories(dir);
            java.nio.file.Path file = dir.resolve("last_path.txt");

            try (java.io.BufferedWriter w = java.nio.file.Files.newBufferedWriter(file)) {
                w.write("Path node IDs:\n");
                w.write(formatPathIds(path));
                w.write("\nPath lat,lon:\n");
                w.write(formatPathCoords(path));
            }
            return file.toAbsolutePath().toString();
        } catch (Exception e) {
            System.out.println("[GUI] Failed to write path file: " + e.getMessage());
            return null;
        }
    }

    private void exitApplication() {
        // Create custom confirmation dialog with styling
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel icon = new JLabel("🚪");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel message = new JLabel("<html><div style='text-align: center;'>" +
            "<b>Exit Wide-Path Pro?</b><br><br>" +
            "Are you sure you want to exit?<br>" +
            "All unsaved data will be lost." +
            "</div></html>");
        message.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        message.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(icon, BorderLayout.NORTH);
        panel.add(message, BorderLayout.CENTER);
        
        String[] options = {"Exit", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
            frame,
            panel,
            "Confirm Exit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[1]
        );
        
        if (choice == 0) { // Exit chosen
            performGracefulShutdown();
        }
    }

    private void performGracefulShutdown() {
        statusBar.setMessage("Shutting down Wide-Path Pro...", StatusBar.MessageType.INFO);
        
        SwingWorker<Void, Void> shutdownWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    // Cleanup resources
                    statusBar.dispose();
                    metricsDashboard.dispose();
                    executorService.shutdown();
                    
                    if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executorService.shutdownNow();
                    Thread.currentThread().interrupt();
                }
                return null;
            }

            @Override
            protected void done() {
                // Fade out animation (with platform compatibility check)
                try {
                    if (frame.isUndecorated() || !GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)) {
                        // Platform doesn't support opacity, just dispose immediately
                        frame.dispose();
                        System.exit(0);
                        return;
                    }
                    
                    javax.swing.Timer fadeTimer = new javax.swing.Timer(30, null);
                    final float[] opacity = {1.0f};
                    fadeTimer.addActionListener(e -> {
                        opacity[0] -= 0.1f;
                        if (opacity[0] <= 0.0f) {
                            ((javax.swing.Timer) e.getSource()).stop();
                            frame.dispose();
                            System.exit(0);
                        } else {
                            try {
                                frame.setOpacity(opacity[0]);
                            } catch (Exception ex) {
                                // Opacity not supported, just dispose
                                ((javax.swing.Timer) e.getSource()).stop();
                                frame.dispose();
                                System.exit(0);
                            }
                        }
                    });
                    fadeTimer.start();
                } catch (Exception ex) {
                    // Fallback: just dispose and exit
                    frame.dispose();
                    System.exit(0);
                }
            }
        };
        
        shutdownWorker.execute();
    }

    private void showSuccessAnimation() {
        // Visual success feedback with color flash on status bar
        javax.swing.Timer flashTimer = new javax.swing.Timer(100, null);
        final int[] flashCount = {0};
        flashTimer.addActionListener(e -> {
            if (flashCount[0]++ >= 6) {
                ((javax.swing.Timer) e.getSource()).stop();
            } else {
                // Toggle between success color and normal
                statusBar.repaint();
            }
        });
        flashTimer.start();
    }
}
