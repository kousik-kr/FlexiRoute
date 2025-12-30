import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import ui.components.WorldClassSplashScreen;
import ui.panels.ResultData;
import ui.panels.WorldClassMapPanel;
import ui.panels.WorldClassQueryPanel;
import ui.panels.WorldClassResultsPanel;

/**
 * World-Class Wide Path Navigator
 * A professional, feature-rich GUI for pathfinding with wide road optimization
 * 
 * Features:
 * - Modern, clean UI design
 * - Multiple visualization modes
 * - Real-time progress tracking
 * - Export capabilities
 * - Comprehensive results dashboard
 * 
 * @version 3.0 - World Class Edition
 */
public class WorldClassGuiLauncher extends JFrame {
    
    // === CONSTANTS ===
    private static final String APP_TITLE = "🌟 Wide-Path Navigator🌟";
    private static final String VERSION = "v3.0 Rainbow Edition";
    private static final int DEFAULT_WIDTH = 1550;
    private static final int DEFAULT_HEIGHT = 980;
    
    // === 🌈 VIBRANT RAINBOW COLOR PALETTE ===
    private static final Color CORAL_PINK = new Color(255, 107, 107);      // Coral
    private static final Color ELECTRIC_BLUE = new Color(59, 130, 246);    // Electric Blue
    private static final Color VIVID_PURPLE = new Color(168, 85, 247);     // Vivid Purple
    private static final Color NEON_GREEN = new Color(16, 185, 129);       // Neon Green
    private static final Color SUNSET_ORANGE = new Color(251, 146, 60);    // Sunset Orange
    private static final Color HOT_PINK = new Color(236, 72, 153);         // Hot Pink
    private static final Color CYBER_YELLOW = new Color(250, 204, 21);     // Cyber Yellow
    private static final Color OCEAN_TEAL = new Color(20, 184, 166);       // Ocean Teal
    private static final Color ROYAL_INDIGO = new Color(99, 102, 241);     // Royal Indigo
    private static final Color LIME_GREEN = new Color(132, 204, 22);       // Lime Green
    
    private static final Color BG_COLOR = new Color(248, 250, 252);        // Off White
    private static final Color SIDEBAR_BG = new Color(255, 255, 255);      // White
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);       // Dark Slate
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);  // Cool Gray
    
    // === UI COMPONENTS ===
    private WorldClassQueryPanel queryPanel;
    private WorldClassMapPanel mapPanel;
    private WorldClassResultsPanel resultsPanel;
    private JTabbedPane rightTabs;
    private JLabel statusLabel;
    private JProgressBar globalProgress;
    
    // === STATE ===
    private Result lastResult;
    private boolean isDarkMode = false;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public WorldClassGuiLauncher() {
        super(APP_TITLE + " — " + VERSION);
        initializeUI();
        loadDataset();
    }
    
    private void initializeUI() {
        // Frame setup
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setMinimumSize(new Dimension(1100, 750));
        setLocationRelativeTo(null);
        
        // Set system look and feel with enhanced defaults
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // Enhanced font defaults for better readability
            UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("Button.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("ComboBox.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("Menu.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("MenuItem.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("TabbedPane.font", new Font("Segoe UI", Font.BOLD, 14));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Main container
        JPanel mainContainer = new JPanel(new BorderLayout(0, 0));
        mainContainer.setBackground(BG_COLOR);
        
        // Create menu bar
        setJMenuBar(createMenuBar());
        
        // Create main content
        JSplitPane mainSplit = createMainSplit();
        mainContainer.add(mainSplit, BorderLayout.CENTER);
        
        // Create status bar
        JPanel statusBar = createStatusBar();
        mainContainer.add(statusBar, BorderLayout.SOUTH);
        
        setContentPane(mainContainer);
        
        // Key bindings
        setupKeyBindings();
    }
    
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
                // Rainbow gradient menu bar
                java.awt.GradientPaint gp = new java.awt.GradientPaint(
                    0, 0, new Color(248, 250, 255),
                    getWidth(), 0, new Color(250, 245, 255)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        menuBar.setOpaque(false);
        menuBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, VIVID_PURPLE),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        menuBar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        
        // File Menu - Blue themed
        JMenu fileMenu = new JMenu("📁 File");
        fileMenu.setFont(new Font("Segoe UI", Font.BOLD, 15));
        fileMenu.setForeground(ELECTRIC_BLUE);
        fileMenu.setMnemonic(KeyEvent.VK_F);
        
        JMenuItem loadDataset = new JMenuItem("Load Dataset...", KeyEvent.VK_L);
        loadDataset.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        loadDataset.addActionListener(e -> loadCustomDataset());
        
        JMenuItem exportImage = new JMenuItem("Export Map Image...", KeyEvent.VK_E);
        exportImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        
        JMenuItem exportResults = new JMenuItem("Export Results...", KeyEvent.VK_R);
        
        JMenuItem exit = new JMenuItem("Exit", KeyEvent.VK_X);
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        exit.addActionListener(e -> exitApplication());
        
        fileMenu.add(loadDataset);
        fileMenu.addSeparator();
        fileMenu.add(exportImage);
        fileMenu.add(exportResults);
        fileMenu.addSeparator();
        fileMenu.add(exit);
        
        // View Menu - Purple themed
        JMenu viewMenu = new JMenu("🎨 View");
        viewMenu.setFont(new Font("Segoe UI", Font.BOLD, 15));
        viewMenu.setForeground(VIVID_PURPLE);
        viewMenu.setMnemonic(KeyEvent.VK_V);
        
        JCheckBoxMenuItem darkMode = new JCheckBoxMenuItem("Dark Mode");
        darkMode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
        darkMode.addActionListener(e -> toggleDarkMode());
        
        JMenuItem zoomIn = new JMenuItem("Zoom In");
        zoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK));
        
        JMenuItem zoomOut = new JMenuItem("Zoom Out");
        zoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
        
        JMenuItem resetView = new JMenuItem("Reset View");
        resetView.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK));
        
        viewMenu.add(darkMode);
        viewMenu.addSeparator();
        viewMenu.add(zoomIn);
        viewMenu.add(zoomOut);
        viewMenu.add(resetView);
        
        // Query Menu - Green themed
        JMenu queryMenu = new JMenu("🔍 Query");
        queryMenu.setFont(new Font("Segoe UI", Font.BOLD, 15));
        queryMenu.setForeground(NEON_GREEN);
        queryMenu.setMnemonic(KeyEvent.VK_Q);
        
        JMenuItem runQuery = new JMenuItem("Run Query", KeyEvent.VK_R);
        runQuery.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK));
        runQuery.addActionListener(e -> executeQuery());
        
        JMenuItem randomQuery = new JMenuItem("Random Query", KeyEvent.VK_A);
        randomQuery.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        randomQuery.addActionListener(e -> executeRandomQuery());
        
        JMenuItem clearResults = new JMenuItem("Clear Results", KeyEvent.VK_C);
        
        queryMenu.add(runQuery);
        queryMenu.add(randomQuery);
        queryMenu.addSeparator();
        queryMenu.add(clearResults);
        
        // Help Menu - Orange themed
        JMenu helpMenu = new JMenu("❓ Help");
        helpMenu.setFont(new Font("Segoe UI", Font.BOLD, 15));
        helpMenu.setForeground(SUNSET_ORANGE);
        helpMenu.setMnemonic(KeyEvent.VK_H);
        
        JMenuItem userGuide = new JMenuItem("User Guide", KeyEvent.VK_U);
        userGuide.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        userGuide.addActionListener(e -> showUserGuide());
        
        JMenuItem about = new JMenuItem("About", KeyEvent.VK_A);
        about.addActionListener(e -> showAboutDialog());
        
        helpMenu.add(userGuide);
        helpMenu.addSeparator();
        helpMenu.add(about);
        
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(queryMenu);
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    private JSplitPane createMainSplit() {
        // Left side: Query Panel
        queryPanel = new WorldClassQueryPanel();
        queryPanel.setPreferredSize(new Dimension(500, 0));
        queryPanel.setMinimumSize(new Dimension(500, 0));
        
        // Set callbacks
        queryPanel.setOnRunQuery(this::executeQuery);
        queryPanel.setOnRandomQuery(this::executeRandomQuery);
        queryPanel.setOnPreviewChange(this::updateQueryPreview);
        
        // Right side: Tabbed pane with map and results
        rightTabs = new JTabbedPane(JTabbedPane.TOP);
        rightTabs.setFont(new Font("Segoe UI", Font.BOLD, 15));
        rightTabs.setBackground(BG_COLOR);
        
        // Map Panel
        mapPanel = new WorldClassMapPanel();
        rightTabs.addTab("🗺️ Map View", mapPanel);
        
        // Results Panel
        resultsPanel = new WorldClassResultsPanel();
        rightTabs.addTab("📊 Results", resultsPanel);
        
        // Metrics Panel (placeholder)
        JPanel metricsPanel = createMetricsPanel();
        rightTabs.addTab("📈 Metrics", metricsPanel);
        
        // Main split pane
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, queryPanel, rightTabs);
        split.setDividerLocation(500);
        split.setDividerSize(4);
        split.setContinuousLayout(true);
        split.setBorder(null);
        
        return split;
    }
    
    private JPanel createMetricsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        JLabel placeholder = new JLabel("📈 Detailed metrics and statistics will appear here after running queries.", SwingConstants.CENTER);
        placeholder.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        placeholder.setForeground(TEXT_SECONDARY);
        
        panel.add(placeholder, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
                // Gradient status bar
                java.awt.GradientPaint gp = new java.awt.GradientPaint(
                    0, 0, new Color(248, 250, 255),
                    getWidth(), 0, new Color(255, 248, 250)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        statusBar.setOpaque(false);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, VIVID_PURPLE),
            BorderFactory.createEmptyBorder(14, 22, 14, 22)
        ));
        
        statusLabel = new JLabel("🚀 Ready — Load a dataset to begin your adventure!");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        statusLabel.setForeground(NEON_GREEN);
        
        globalProgress = new JProgressBar() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                // Background
                g2d.setColor(new Color(226, 232, 240));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Progress gradient
                int w = (int) ((getValue() / 100.0) * getWidth());
                if (w > 0) {
                    java.awt.GradientPaint gp = new java.awt.GradientPaint(0, 0, HOT_PINK, getWidth(), 0, VIVID_PURPLE);
                    g2d.setPaint(gp);
                    g2d.fillRoundRect(0, 0, w, getHeight(), 10, 10);
                }
                // Text
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                java.awt.FontMetrics fm = g2d.getFontMetrics();
                String text = getString();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(text, x, y);
                g2d.dispose();
            }
        };
        globalProgress.setVisible(false);
        globalProgress.setPreferredSize(new Dimension(280, 22));
        globalProgress.setStringPainted(true);
        globalProgress.setFont(new Font("Segoe UI", Font.BOLD, 13));
        globalProgress.setBorder(null);
        
        JLabel versionLabel = new JLabel("🌟 " + VERSION + " 🌟");
        versionLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        versionLabel.setForeground(HOT_PINK);
        
        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(globalProgress, BorderLayout.CENTER);
        statusBar.add(versionLabel, BorderLayout.EAST);
        
        return statusBar;
    }
    
    private void setupKeyBindings() {
        // Ctrl+Enter to run query
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "runQuery");
        getRootPane().getActionMap().put("runQuery", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executeQuery();
            }
        });
        
        // F5 to refresh/run
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "refresh");
        getRootPane().getActionMap().put("refresh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executeQuery();
            }
        });
    }
    
    private void loadDataset() {
        setStatus("Loading dataset...");
        
        executor.submit(() -> {
            try {
                // Configure and load using BidirectionalAstar
                BidirectionalAstar.configureDefaults();
                boolean loaded = BidirectionalAstar.loadGraphFromDisk(null, null);
                
                if (!loaded) {
                    SwingUtilities.invokeLater(() -> {
                        setStatus("Failed to load dataset files");
                    });
                    return;
                }
                
                SwingUtilities.invokeLater(() -> {
                    int nodeCount = Graph.get_nodes().size();
                    setStatus(String.format("Dataset loaded: %,d nodes", nodeCount));
                    queryPanel.setMaxNodeId(nodeCount);
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    setStatus("Failed to load dataset: " + e.getMessage());
                });
            }
        });
    }
    
    private void loadCustomDataset() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Dataset Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File datasetDir = chooser.getSelectedFile();
            
            executor.submit(() -> {
                try {
                    SwingUtilities.invokeLater(() -> setStatus("Loading custom dataset..."));
                    
                    BidirectionalAstar.configureDefaults();
                    boolean loaded = BidirectionalAstar.loadGraphFromDisk(datasetDir.getAbsolutePath(), null);
                    
                    if (!loaded) {
                        SwingUtilities.invokeLater(() -> {
                            setStatus("Failed to load dataset from directory");
                            JOptionPane.showMessageDialog(this, 
                                "Failed to load dataset from selected directory",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        });
                        return;
                    }
                    
                    SwingUtilities.invokeLater(() -> {
                        int nodeCount = Graph.get_nodes().size();
                        setStatus(String.format("Custom dataset loaded: %,d nodes", nodeCount));
                        queryPanel.setMaxNodeId(nodeCount);
                    });
                    
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        setStatus("Failed to load dataset: " + e.getMessage());
                        JOptionPane.showMessageDialog(this, 
                            "Failed to load dataset:\n" + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
            });
        }
    }
    
    private void executeQuery() {
        int source = queryPanel.getSource();
        int dest = queryPanel.getDestination();
        int departure = queryPanel.getDeparture();
        int interval = queryPanel.getInterval();
        int budget = queryPanel.getBudget();
        int heuristic = queryPanel.getHeuristicMode();
        
        setStatus("Running query: " + source + " → " + dest + " (budget: " + budget + ")");
        queryPanel.setRunning(true);
        resultsPanel.showLoading();
        mapPanel.clearQueryPreview();
        mapPanel.setSearchProgress(0, "Initializing search...");
        
        executor.submit(() -> {
            try {
                long startTime = System.currentTimeMillis();
                
                // Update progress
                SwingUtilities.invokeLater(() -> mapPanel.setSearchProgress(10, "Setting up bidirectional search..."));
                
                // Configure algorithm based on heuristic mode
                if (heuristic == 1) {
                    BidirectionalLabeling.setAggressiveMode();
                } else {
                    BidirectionalLabeling.setBalancedMode();
                }
                
                SwingUtilities.invokeLater(() -> mapPanel.setSearchProgress(30, "Expanding labels..."));
                
                // Use the existing BidirectionalAstar.runSingleQuery
                BidirectionalAstar.setIntervalDuration(interval);
                Result result = BidirectionalAstar.runSingleQuery(source, dest, departure, interval, budget);
                
                SwingUtilities.invokeLater(() -> mapPanel.setSearchProgress(80, "Reconstructing path..."));
                
                long elapsed = System.currentTimeMillis() - startTime;
                
                // Create result with enhanced info if null
                if (result == null) {
                    result = new Result(departure, 0, 0, 0, 0, new ArrayList<>(), new ArrayList<>());
                }
                result.setSource(source);
                result.setDestination(dest);
                result.setBudget(budget);
                result.setExecutionTime(elapsed);
                
                // Get path coordinates
                if (result.isPathFound()) {
                    collectPathCoordinates(result);
                }
                
                lastResult = result;
                final Result finalResult = result;
                
                SwingUtilities.invokeLater(() -> {
                    mapPanel.setSearchProgress(100, "Complete!");
                    mapPanel.clearSearchFrontier();
                    displayResults(finalResult);
                    queryPanel.setRunning(false);
                    setStatus(finalResult.isPathFound() 
                        ? String.format("✅ Path found: %d nodes in %.2f ms", finalResult.getPathLength(), finalResult.getExecutionTime())
                        : "❌ No path found within budget");
                    
                    // Write path to file
                    writePathToFile(finalResult);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    resultsPanel.showError(e.getMessage());
                    queryPanel.setRunning(false);
                    mapPanel.clearSearchFrontier();
                    setStatus("❌ Query failed: " + e.getMessage());
                });
            }
        });
    }
    
    private void executeRandomQuery() {
        Random rand = new Random();
        int maxNode = Graph.get_nodes().size();
        
        queryPanel.setSource(rand.nextInt(maxNode) + 1);
        queryPanel.setDestination(rand.nextInt(maxNode) + 1);
    }
    
    private void collectPathCoordinates(Result result) {
        List<Integer> pathNodes = result.getPathNodes();
        if (pathNodes == null || pathNodes.isEmpty()) return;
        
        List<double[]> coordinates = new ArrayList<>();
        Map<Integer, Node> allNodes = Graph.get_nodes();
        
        for (Integer nodeId : pathNodes) {
            Node node = allNodes.get(nodeId);
            if (node != null) {
                coordinates.add(new double[]{node.get_latitude(), node.get_longitude()});
            }
        }
        
        result.setPathCoordinates(coordinates);
    }
    
    private void displayResults(Result result) {
        // Convert Result to ResultData for UI panels
        ResultData resultData = ResultData.create()
            .source(result.getSource())
            .destination(result.getDestination())
            .budget(result.getBudget())
            .executionTime(result.getExecutionTime())
            .pathFound(result.isPathFound())
            .totalCost(result.getTotalCost())
            .pathLength(result.getPathLength())
            .wideEdgeCount(result.getWideEdgeCount())
            .pathNodes(result.getPathNodes())
            .pathCoordinates(result.getPathCoordinates())
            .wideEdgeIndices(result.getWideEdgeIndices());
        
        // Update results panel
        resultsPanel.displayResult(resultData);
        
        // Update map
        if (result.isPathFound()) {
            mapPanel.setPath(result.getPathNodes(), result.getWideEdgeIndices(), result.getPathCoordinates());
            rightTabs.setSelectedIndex(0); // Switch to map view
        }
    }
    
    private void updateQueryPreview(int source, int dest) {
        Map<Integer, Node> allNodes = Graph.get_nodes();
        Node srcNode = allNodes.get(source);
        Node dstNode = allNodes.get(dest);
        
        if (srcNode != null && dstNode != null) {
            double[] srcCoord = new double[]{srcNode.get_latitude(), srcNode.get_longitude()};
            double[] dstCoord = new double[]{dstNode.get_latitude(), dstNode.get_longitude()};
            mapPanel.setQueryPreview(source, dest, srcCoord, dstCoord);
        }
    }
    
    private void writePathToFile(Result result) {
        if (result == null || !result.isPathFound()) return;
        
        try {
            Files.createDirectories(Paths.get("output"));
            
            StringBuilder sb = new StringBuilder();
            sb.append("Wide-Path Query Result\n");
            sb.append("======================\n\n");
            sb.append("Source: ").append(result.getSource()).append("\n");
            sb.append("Destination: ").append(result.getDestination()).append("\n");
            sb.append("Path Length: ").append(result.getPathLength()).append(" nodes\n");
            sb.append("Total Cost: ").append(String.format("%.2f", result.getTotalCost())).append("\n");
            sb.append("Wide Edges: ").append(result.getWideEdgeCount()).append("\n");
            sb.append("Execution Time: ").append(String.format("%.2f", result.getExecutionTime())).append(" ms\n\n");
            
            sb.append("Path Node IDs:\n");
            List<Integer> path = result.getPathNodes();
            for (int i = 0; i < path.size(); i++) {
                sb.append(path.get(i));
                if (i < path.size() - 1) sb.append(" -> ");
                if ((i + 1) % 10 == 0) sb.append("\n");
            }
            sb.append("\n\nPath Coordinates (lat, lon):\n");
            
            if (result.getPathCoordinates() != null) {
                for (int i = 0; i < result.getPathCoordinates().size(); i++) {
                    double[] coord = result.getPathCoordinates().get(i);
                    sb.append(String.format("[%d] %.6f, %.6f\n", i, coord[0], coord[1]));
                }
            }
            
            Files.writeString(Paths.get("output/last_path.txt"), sb.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setStatus(String message) {
        statusLabel.setText(message);
    }
    
    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        // TODO: Implement dark mode theme switching
        JOptionPane.showMessageDialog(this, "Dark mode coming soon!", "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showUserGuide() {
        String guide = """
            <html>
            <body style="font-family: Segoe UI; padding: 10px; width: 400px;">
            <h2>🗺️ Wide-Path Navigator User Guide</h2>
            
            <h3>Quick Start</h3>
            <ol>
                <li>Enter source and destination node IDs</li>
                <li>Adjust travel budget using the slider</li>
                <li>Click "Find Wide Path" or press Ctrl+Enter</li>
            </ol>
            
            <h3>Parameters</h3>
            <ul>
                <li><b>Source/Destination:</b> Node IDs from the loaded dataset</li>
                <li><b>Departure Time:</b> Start time (for time-dependent routing)</li>
                <li><b>Time Interval:</b> Time window for edge costs</li>
                <li><b>Budget:</b> Maximum travel cost allowed</li>
            </ul>
            
            <h3>Keyboard Shortcuts</h3>
            <ul>
                <li><b>Ctrl+Enter:</b> Run query</li>
                <li><b>Ctrl+R:</b> Random query</li>
                <li><b>Ctrl+O:</b> Load dataset</li>
                <li><b>F5:</b> Refresh/Run</li>
            </ul>
            
            <h3>Visualization</h3>
            <p>Use the toolbar in the Map View to:</p>
            <ul>
                <li>Change visualization mode</li>
                <li>Zoom in/out</li>
                <li>Toggle labels and grid</li>
                <li>Enable path animation</li>
                <li>Export map as PNG</li>
            </ul>
            </body>
            </html>
            """;
        
        JOptionPane.showMessageDialog(this, guide, "User Guide", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showAboutDialog() {
        String about = """
            <html>
            <body style="font-family: Segoe UI; text-align: center; padding: 20px;">
            <h1>🗺️ Wide-Path Navigator</h1>
            <h3>Version 3.0 — World Class Edition</h3>
            <p>Advanced pathfinding with wide road optimization</p>
            <br>
            <p>Using Bi-TDCPO algorithm for optimal<br>
            constrained path queries on road networks.</p>
            <br>
            <p><small>© 2024 Wide-Path Project</small></p>
            </body>
            </html>
            """;
        
        JOptionPane.showMessageDialog(this, about, "About", JOptionPane.PLAIN_MESSAGE);
    }
    
    private void exitApplication() {
        executor.shutdown();
        System.exit(0);
    }
    
    // === MAIN ENTRY POINT ===
    
    public static void main(String[] args) {
        // Show splash screen
        SwingUtilities.invokeLater(() -> {
            WorldClassSplashScreen splash = new WorldClassSplashScreen();
            splash.showSplash();
            
            // Simulate loading
            javax.swing.Timer loadTimer = new javax.swing.Timer(100, null);
            final int[] progress = {0};
            String[] messages = {
                "Loading application...",
                "Initializing UI components...",
                "Preparing visualization engine...",
                "Loading map renderer...",
                "Setting up algorithms...",
                "Configuring themes...",
                "Almost ready...",
                "Starting application..."
            };
            
            loadTimer.addActionListener(e -> {
                progress[0] += 5;
                int msgIndex = Math.min(progress[0] / 15, messages.length - 1);
                splash.setProgress(progress[0], messages[msgIndex]);
                
                if (progress[0] >= 100) {
                    loadTimer.stop();
                    
                    // Create and show main window
                    WorldClassGuiLauncher app = new WorldClassGuiLauncher();
                    app.setVisible(true);
                    
                    splash.closeSplash();
                }
            });
            
            loadTimer.start();
        });
    }
}
