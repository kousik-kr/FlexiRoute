import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import managers.MetricsCollector;
import managers.QueryHistoryManager;
import managers.ThemeManager;
import map.MapViewMode;
import map.OSMMapComponent;
import models.QueryResult;
import models.RoutingMode;
import ui.components.SplashScreen;
import ui.panels.MapPanel;
import ui.panels.MetricsDashboard;
import ui.panels.PreferenceSlidersPanel;
import ui.panels.QueryHistoryPanel;
import ui.panels.QueryPanel;
import ui.panels.ResultData;
import ui.panels.ResultsPanel;

/**
 * FlexiRoute Navigator - Professional GUI Application
 * A feature-rich GUI for pathfinding with wide road optimization
 * 
 * Features:
 * - Modern, clean UI design
 * - Multiple visualization modes
 * - Real-time progress tracking
 * - Query history and analytics
 * - Performance metrics dashboard
 * - Theme support
 * - Export capabilities
 * - Comprehensive results dashboard
 * 
 * @version 3.0
 */
public class GuiLauncher extends JFrame {
    
    // === CONSTANTS ===
    private static final String APP_TITLE = "🌟 FlexiRoute Navigator 🌟";
    //private static final String VERSION = "";
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
    private QueryPanel queryPanel;
    private MapPanel mapPanel;
    private OSMMapComponent osmMapComponent;
    private PreferenceSlidersPanel preferenceSlidersPanel;
    private ResultsPanel resultsPanel;
    private JTabbedPane rightTabs;
    private JLabel statusLabel;
    private JProgressBar globalProgress;
    private QueryHistoryPanel historyPanel;
    private MetricsDashboard metricsDashboard;
    private MapViewMode currentMapMode = MapViewMode.OSM_TILES;
    private JPanel mapContainer;
    
    // === MANAGERS ===
    private final QueryHistoryManager historyManager = new QueryHistoryManager();
    private final MetricsCollector metricsCollector = new MetricsCollector();
    private final ThemeManager themeManager = new ThemeManager();
    
    // === STATE ===
    private Result lastResult;
    private boolean isDarkMode = false;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    
    // === QUERY EXECUTION STATE ===
    private volatile Future<?> currentQueryFuture = null;
    private volatile AtomicBoolean queryCancelled = new AtomicBoolean(false);
    private static final int QUERY_TIMEOUT_SECONDS = 10; // Timeout for query execution
    
    public GuiLauncher() {
        super(APP_TITLE);
        initializeUI();
        // Automatically load London dataset on startup
        loadLondonDataset();
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
            UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 18));
            UIManager.put("Button.font", new Font("Segoe UI", Font.PLAIN, 18));
            UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 18));
            UIManager.put("ComboBox.font", new Font("Segoe UI", Font.PLAIN, 18));
            UIManager.put("Menu.font", new Font("Segoe UI", Font.PLAIN, 18));
            UIManager.put("MenuItem.font", new Font("Segoe UI", Font.PLAIN, 18));
            UIManager.put("TabbedPane.font", new Font("Segoe UI", Font.BOLD, 18));
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
        menuBar.setFont(new Font("Segoe UI", Font.BOLD, 19));
        
        // File Menu - Blue themed
        JMenu fileMenu = new JMenu("📁 File");
        fileMenu.setFont(new Font("Segoe UI", Font.BOLD, 19));
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
        viewMenu.setFont(new Font("Segoe UI", Font.BOLD, 19));
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
        queryMenu.setFont(new Font("Segoe UI", Font.BOLD, 19));
        queryMenu.setForeground(NEON_GREEN);
        queryMenu.setMnemonic(KeyEvent.VK_Q);
        
        JMenuItem runQuery = new JMenuItem("Run Query", KeyEvent.VK_R);
        runQuery.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK));
        runQuery.addActionListener(e -> executeQuery());
        
        JMenuItem clearResults = new JMenuItem("Clear Results", KeyEvent.VK_C);
        clearResults.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        clearResults.addActionListener(e -> clearMapView());  // Cancels query and clears map
        
        queryMenu.add(runQuery);
        queryMenu.addSeparator();
        queryMenu.add(clearResults);
        
        // Help Menu - Orange themed
        JMenu helpMenu = new JMenu("❓ Help");
        helpMenu.setFont(new Font("Segoe UI", Font.BOLD, 19));
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
        // Left side: Query Panel with Preference Sliders
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBackground(SIDEBAR_BG);
        
        queryPanel = new QueryPanel();
        
        // Create preference sliders panel
        preferenceSlidersPanel = new PreferenceSlidersPanel();
        preferenceSlidersPanel.setOnPreferenceChange(values -> {
            updateStatus("⚙️ Preferences updated: Wideness=" + (int)(values.widenessWeight * 100) + 
                        "%, Time=" + (int)(values.timeWeight * 100) + "%, Distance=" + (int)(values.distanceWeight * 100) + "%");
        });
        
        // Set callback for instant recomputation when routing mode changes
        preferenceSlidersPanel.setOnRoutingModeChange(this::onRoutingModeChanged);
        
        // Collapsible preference panel
        JPanel preferenceWrapper = createCollapsiblePanel("🎚️ Routing Preferences", preferenceSlidersPanel);
        
        leftPanel.add(queryPanel, BorderLayout.CENTER);
        leftPanel.add(preferenceWrapper, BorderLayout.SOUTH);
        
        leftPanel.setPreferredSize(new Dimension(500, 0));
        leftPanel.setMinimumSize(new Dimension(500, 0));
        
        // Set callbacks
        queryPanel.setOnRunQuery(this::executeQuery);
        queryPanel.setOnPreviewChange(this::updateQueryPreview);
        queryPanel.setOnClear(this::clearMapView);  // Clear button cancels query
        
        // Right side: Tabbed pane with map and results
        rightTabs = new JTabbedPane(JTabbedPane.TOP);
        rightTabs.setFont(new Font("Segoe UI", Font.BOLD, 19));
        rightTabs.setBackground(BG_COLOR);
        
        // Map Container with switchable views
        mapContainer = new JPanel(new CardLayout());
        mapContainer.setBackground(BG_COLOR);
        
        // Coordinate-based Map Panel (existing)
        mapPanel = new MapPanel();
        mapContainer.add(mapPanel, MapViewMode.COORDINATE_BASED.name());
        
        // OSM Tile-based Map Component (new)
        osmMapComponent = new OSMMapComponent();
        mapContainer.add(osmMapComponent, MapViewMode.OSM_TILES.name());
        
        // Set up nearest node finder for map click selection
        osmMapComponent.setNearestNodeFinder((lat, lon) -> {
            Map<Integer, Node> nodes = Graph.get_nodes();
            if (nodes == null || nodes.isEmpty()) {
                return null;
            }
            
            int nearestNodeId = -1;
            double minDistSq = Double.MAX_VALUE;
            double nearestLat = 0, nearestLon = 0;
            
            // Search through all nodes
            for (Map.Entry<Integer, Node> entry : nodes.entrySet()) {
                Node node = entry.getValue();
                double nodeLat = node.get_latitude();
                double nodeLon = node.get_longitude();
                
                // Simple Euclidean distance for quick calculation
                double dLat = nodeLat - lat;
                double dLon = nodeLon - lon;
                double distSq = dLat * dLat + dLon * dLon;
                
                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    nearestNodeId = entry.getKey();
                    nearestLat = nodeLat;
                    nearestLon = nodeLon;
                }
            }
            
            if (nearestNodeId == -1) return null;
            return new double[] { nearestNodeId, nearestLat, nearestLon };
        });
        
        // Set up node selection listener to update QueryPanel
        osmMapComponent.setNodeSelectionListener((nodeId, lat, lon, isSource) -> {
            if (isSource) {
                queryPanel.setSource(nodeId);
                updateStatus("📍 Source node selected: " + nodeId + " (lat: " + 
                            String.format("%.4f", lat) + ", lon: " + String.format("%.4f", lon) + ")");
            } else {
                queryPanel.setDestination(nodeId);
                updateStatus("🎯 Destination node selected: " + nodeId + " (lat: " + 
                            String.format("%.4f", lat) + ", lon: " + String.format("%.4f", lon) + ")");
            }
        });
        
        // Map view with toolbar
        JPanel mapViewPanel = new JPanel(new BorderLayout(0, 0));
        mapViewPanel.add(createMapToolbar(), BorderLayout.NORTH);
        mapViewPanel.add(mapContainer, BorderLayout.CENTER);
        
        rightTabs.addTab("🗺️ Map View", mapViewPanel);
        
        // Results Panel
        resultsPanel = new ResultsPanel();
        resultsPanel.setOnClear(this::clearMapView);  // Clear button in results cancels query
        rightTabs.addTab("📊 Results", resultsPanel);
        
        // Metrics Dashboard
        metricsDashboard = new MetricsDashboard(metricsCollector);
        rightTabs.addTab("📈 Metrics", metricsDashboard);
        
        // History Panel
        historyPanel = new QueryHistoryPanel(historyManager);
        rightTabs.addTab("🕐 History", historyPanel);
        
        // Main split pane
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightTabs);
        split.setDividerLocation(500);
        split.setDividerSize(4);
        split.setContinuousLayout(true);
        split.setBorder(null);
        
        return split;
    }
    
    private JPanel createCollapsiblePanel(String title, JComponent content) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 0));
        wrapper.setBackground(SIDEBAR_BG);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        
        // Header with toggle
        JPanel header = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, new Color(248, 250, 255), getWidth(), 0, new Color(240, 245, 255));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(VIVID_PURPLE);
        
        JLabel toggleIcon = new JLabel("▼");
        toggleIcon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        toggleIcon.setForeground(TEXT_SECONDARY);
        
        header.add(titleLabel, BorderLayout.WEST);
        header.add(toggleIcon, BorderLayout.EAST);
        
        // Toggle functionality
        final boolean[] collapsed = {false};
        header.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                collapsed[0] = !collapsed[0];
                content.setVisible(!collapsed[0]);
                toggleIcon.setText(collapsed[0] ? "▶" : "▼");
                wrapper.revalidate();
            }
        });
        
        wrapper.add(header, BorderLayout.NORTH);
        wrapper.add(content, BorderLayout.CENTER);
        
        return wrapper;
    }
    
    private JToolBar createMapToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        toolbar.setBackground(new Color(248, 250, 255));
        
        // Map mode toggle
        JLabel modeLabel = new JLabel("🗺️ Map Mode: ");
        modeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        modeLabel.setForeground(TEXT_PRIMARY);
        
        JComboBox<MapViewMode> modeCombo = new JComboBox<>(MapViewMode.values());
        modeCombo.setSelectedItem(currentMapMode);
        modeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        modeCombo.setPreferredSize(new Dimension(180, 30));
        modeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof MapViewMode) {
                    setText(((MapViewMode) value).getDisplayName());
                }
                return this;
            }
        });
        modeCombo.addActionListener(e -> {
            MapViewMode selected = (MapViewMode) modeCombo.getSelectedItem();
            switchMapMode(selected);
        });
        
        toolbar.add(modeLabel);
        toolbar.add(modeCombo);
        toolbar.addSeparator(new Dimension(20, 0));
        
        // Zoom controls
        JButton zoomInBtn = createToolbarButton("🔍+ Zoom In", "Zoom In", e -> zoomMap(1));
        JButton zoomOutBtn = createToolbarButton("🔍- Zoom Out", "Zoom Out", e -> zoomMap(-1));
        JButton fitBtn = createToolbarButton("📐", "Fit to Path", e -> fitMapToPath());
        JButton resetBtn = createToolbarButton("🔄", "Reset View", e -> resetMapView());
        JButton clearBtn = createToolbarButton("🗑️ Clear", "Clear Map", e -> clearMapView());
        
        toolbar.add(zoomInBtn);
        toolbar.add(Box.createHorizontalStrut(5));
        toolbar.add(zoomOutBtn);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(fitBtn);
        toolbar.add(Box.createHorizontalStrut(5));
        toolbar.add(resetBtn);
        toolbar.add(Box.createHorizontalStrut(5));
        toolbar.add(clearBtn);
        
        toolbar.add(Box.createHorizontalGlue());
        
        // Tile server selection (only for OSM mode)
        JLabel serverLabel = new JLabel("🌐 Tiles: ");
        serverLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        serverLabel.setForeground(TEXT_PRIMARY);
        
        JComboBox<map.TileProvider.TileServer> serverCombo = new JComboBox<>(map.TileProvider.TileServer.values());
        serverCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        serverCombo.setPreferredSize(new Dimension(150, 30));
        serverCombo.addActionListener(e -> {
            if (osmMapComponent != null) {
                osmMapComponent.setTileServer((map.TileProvider.TileServer) serverCombo.getSelectedItem());
            }
        });
        
        toolbar.add(serverLabel);
        toolbar.add(serverCombo);
        
        return toolbar;
    }
    
    private JButton createToolbarButton(String text, String tooltip, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setToolTipText(tooltip);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 32));
        btn.addActionListener(action);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setContentAreaFilled(true);
                btn.setBackground(new Color(240, 245, 255));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setContentAreaFilled(false);
            }
        });
        return btn;
    }
    
    private void switchMapMode(MapViewMode mode) {
        currentMapMode = mode;
        CardLayout cl = (CardLayout) mapContainer.getLayout();
        cl.show(mapContainer, mode.name());
        updateStatus("🗺️ Switched to " + mode.getDisplayName() + " mode");
        
        // Sync the current path display
        if (lastResult != null) {
            displayPathOnCurrentMap(lastResult);
        }
    }
    
    private void zoomMap(int direction) {
        if (currentMapMode == MapViewMode.OSM_TILES) {
            osmMapComponent.zoom(direction);
        } else {
            // Coordinate-based zoom
            mapPanel.repaint();
        }
    }
    
    private void fitMapToPath() {
        if (currentMapMode == MapViewMode.OSM_TILES) {
            osmMapComponent.fitToPath();
        }
    }
    
    private void resetMapView() {
        if (currentMapMode == MapViewMode.OSM_TILES) {
            osmMapComponent.resetView();
        } else {
            mapPanel.clearMap();
            mapPanel.repaint();
        }
    }
    
    private void clearMapView() {
        // Cancel any ongoing query first
        cancelCurrentQuery();
        
        if (currentMapMode == MapViewMode.OSM_TILES) {
            osmMapComponent.clearMap();
            updateStatus("🗑️ Map cleared & query cancelled");
        } else {
            mapPanel.clearMap();
            mapPanel.repaint();
            updateStatus("🗑️ Map cleared & query cancelled");
        }
        
        // Reset query panel state
        queryPanel.setRunning(false);
        // Clear results without triggering callback (to avoid recursion)
        resultsPanel.clearResultsInternal();
    }
    
    /**
     * Cancel any currently running query
     */
    private void cancelCurrentQuery() {
        queryCancelled.set(true);
        if (currentQueryFuture != null && !currentQueryFuture.isDone()) {
            currentQueryFuture.cancel(true);
            currentQueryFuture = null;
            setStatus("🛑 Query cancelled");
        }
    }
    
    private void displayPathOnCurrentMap(Result result) {
        if (result == null || !result.isPathFound()) return;
        
        List<double[]> pathCoords = result.getPathCoordinates();
        
        // Check if we have multiple Pareto paths (hybrid mode)
        if (result.hasParetoOptimalPaths() && result.getParetoPathCount() > 1) {
            
            // Build ParetoPathInfo list for rendering
            List<map.RouteOverlayRenderer.ParetoPathInfo> paretoPathInfos = new ArrayList<>();
            
            for (int i = 0; i < result.getParetoPathCount(); i++) {
                Result paretoPath = result.getParetoPath(i);
                List<double[]> coords = paretoPath != null ? paretoPath.getPathCoordinates() : null;
                
                if (paretoPath != null && coords != null && !coords.isEmpty()) {
                    paretoPathInfos.add(new map.RouteOverlayRenderer.ParetoPathInfo(
                        coords,
                        paretoPath.getWideEdgeIndices(),
                        paretoPath.get_score(),
                        paretoPath.get_right_turns(),
                        i
                    ));
                }
            }
            
            if (!paretoPathInfos.isEmpty()) {
                if (currentMapMode == MapViewMode.OSM_TILES) {
                    osmMapComponent.setParetoPaths(paretoPathInfos);
                } else {
                    mapPanel.setParetoPaths(paretoPathInfos);
                }
                return;
            }
        }
        
        // Single path display (non-Pareto mode)
        if (currentMapMode == MapViewMode.OSM_TILES && pathCoords != null && !pathCoords.isEmpty()) {
            // Convert path coordinates for OSM display
            osmMapComponent.setPath(result.getPathNodes(), result.getWideEdgeIndices(), pathCoords);
            osmMapComponent.fitToPath();
        } else {
            // Use existing coordinate-based display
            if (pathCoords != null) {
                mapPanel.setPathWithContextAndSubgraph(
                    result.getPathNodes(),
                    result.getWideEdgeIndices(),
                    pathCoords,
                    null, null, null, null, null
                );
            }
        }
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
        
        statusLabel = new JLabel("🚀 Loading London dataset...");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 19));
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
        globalProgress.setFont(new Font("Segoe UI", Font.BOLD, 17));
        globalProgress.setBorder(null);
        
        //JLabel versionLabel = new JLabel("🌟 " + VERSION + " 🌟");
        // versionLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        // versionLabel.setForeground(HOT_PINK);
        
        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(globalProgress, BorderLayout.CENTER);
        //statusBar.add(versionLabel, BorderLayout.EAST);
        
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
                    
                    // Center OSM map on the dataset's location
                    centerMapOnDataset();
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    setStatus("Failed to load dataset: " + e.getMessage());
                });
            }
        });
    }
    
    /**
     * Center the OSM map component on the loaded dataset's coordinates
     */
    private void centerMapOnDataset() {
        try {
            Map<Integer, Node> nodes = Graph.get_nodes();
            if (nodes == null || nodes.isEmpty()) return;
            
            // Calculate bounds of the dataset
            double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
            double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;
            
            for (Node node : nodes.values()) {
                double lat = node.get_latitude();
                double lon = node.get_longitude();
                minLat = Math.min(minLat, lat);
                maxLat = Math.max(maxLat, lat);
                minLon = Math.min(minLon, lon);
                maxLon = Math.max(maxLon, lon);
            }
            
            // Center and zoom the OSM map
            double centerLat = (minLat + maxLat) / 2;
            double centerLon = (minLon + maxLon) / 2;
            
            // Always use zoom level 12 for both default and custom datasets
            int zoom = 12;
            osmMapComponent.centerOn(centerLat, centerLon, zoom);
            updateStatus(String.format("📍 Map centered on dataset: %.4f, %.4f (zoom %d)", centerLat, centerLon, zoom));
            
        } catch (Exception e) {
            System.err.println("Could not center map on dataset: " + e.getMessage());
        }
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
                        
                        // Center OSM map on the custom dataset's location
                        centerMapOnDataset();
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
    
    /**
     * Automatically load the London dataset on startup
     */
    private void loadLondonDataset() {
        executor.submit(() -> {
            try {
                SwingUtilities.invokeLater(() -> setStatus("Loading London dataset..."));
                
                // Configure defaults and load London dataset
                String currentDir = System.getProperty("user.dir");
                String londonDatasetPath = currentDir + "/dataset/London";
                
                // Check if London dataset exists
                File londonDir = new File(londonDatasetPath);
                if (!londonDir.exists() || !londonDir.isDirectory()) {
                    SwingUtilities.invokeLater(() -> {
                        setStatus("❌ London dataset not found");
                        JOptionPane.showMessageDialog(this,
                            "London dataset not found in dataset/London/\n\n" +
                            "Please download the dataset from Google Drive:\n" +
                            "https://drive.google.com/drive/folders/1l3NG641rHeshkYW7aDxpb7RhUy0kRuiP\n\n" +
                            "Extract to: " + londonDatasetPath + "\n\n" +
                            "You can also load a custom dataset via File > Load Dataset...",
                            "Dataset Not Found",
                            JOptionPane.WARNING_MESSAGE);
                    });
                    return;
                }
                
                BidirectionalAstar.configureDefaults();
                boolean loaded = BidirectionalAstar.loadGraphFromDisk(londonDatasetPath, null);
                
                if (!loaded) {
                    SwingUtilities.invokeLater(() -> {
                        setStatus("Failed to load London dataset");
                        JOptionPane.showMessageDialog(this,
                            "Failed to load London dataset files.\n\n" +
                            "Please ensure nodes and edges files are present in:\n" +
                            londonDatasetPath + "\n\n" +
                            "You can also load a custom dataset via File > Load Dataset...",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    });
                    return;
                }
                
                SwingUtilities.invokeLater(() -> {
                    int nodeCount = Graph.get_nodes().size();
                    setStatus(String.format("✅ London dataset loaded: %,d nodes", nodeCount));
                    queryPanel.setMaxNodeId(nodeCount);
                    
                    // Center OSM map on the London dataset's location
                    centerMapOnDataset();
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    setStatus("Failed to load dataset: " + e.getMessage());
                    JOptionPane.showMessageDialog(this,
                        "Failed to load London dataset:\n" + e.getMessage() + "\n\n" +
                        "You can load a custom dataset via File > Load Dataset...",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }
    
    /**
     * Removed - no longer prompting user on startup
     * Dataset is automatically loaded from dataset/London/
     */
    private void promptUserToLoadDataset() {
        SwingUtilities.invokeLater(() -> {
            int choice = JOptionPane.showOptionDialog(
                this,
                "Please select a dataset to load:\n\n" +
                "• Default Dataset - Load from dataset/London/ (288K nodes)\n" +
                "• Custom Dataset - Choose a different directory",
                "Load Dataset",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Default Dataset", "Custom Dataset"},
                "Default Dataset"
            );
            
            if (choice == 0) {
                // Load default dataset
                loadDataset();
            } else if (choice == 1) {
                // Load custom dataset
                loadCustomDataset();
            } else {
                // User closed dialog - show message
                setStatus("No dataset loaded - use File > Load Dataset to begin");
            }
        });
    }
    
    /**
     * Handle routing mode change - instant recomputation from cached labels if possible
     */
    private void onRoutingModeChanged(models.RoutingMode newMode) {
        // Get current query parameters
        int source = queryPanel.getSource();
        int dest = queryPanel.getDestination();
        int departure = queryPanel.getDeparture();
        int budget = queryPanel.getBudget();
        int interval = queryPanel.getInterval();
        
        // Check if we can reuse cached labels
        if (BidirectionalAstar.canReuseCachedLabels(source, dest, budget, departure)) {
            setStatus("⚡ Instant recompute: " + newMode.getDisplayName() + "...");
            
            // Run recomputation in background thread to avoid blocking UI
            executor.submit(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    
                    Result result = BidirectionalAstar.recomputeFromCache(newMode);
                    
                    long elapsed = System.currentTimeMillis() - startTime;
                    
                    if (result != null && result.isPathFound()) {
                        result.setExecutionTime(elapsed / 1000.0);
                        
                        // Collect coordinates for the result and Pareto paths
                        collectPathCoordinates(result);
                        if (result.hasParetoOptimalPaths()) {
                            for (Result paretoPath : result.getParetoOptimalPaths()) {
                                collectPathCoordinates(paretoPath);
                            }
                        }
                        
                        lastResult = result;
                        final Result finalResult = result;
                        final long execTime = elapsed;
                        
                        SwingUtilities.invokeLater(() -> {
                            displayResults(finalResult);
                            displayPathOnCurrentMap(finalResult);
                            
                            // Save to history/log (will append to same log file for same query session)
                            try {
                                int pathLen = finalResult.getPathNodes() != null ? finalResult.getPathNodes().size() : 0;
                                int wideCount = finalResult.getWideEdgeIndices() != null ? finalResult.getWideEdgeIndices().size() : 0;
                                double wideRoadPct = pathLen > 1 ? (100.0 * wideCount / (pathLen - 1)) : 0.0;
                                
                                QueryResult queryResult = new QueryResult.Builder()
                                    .setSourceNode(source)
                                    .setDestinationNode(dest)
                                    .setDepartureTime(departure)
                                    .setIntervalDuration(interval)
                                    .setBudget(budget)
                                    .setActualDepartureTime(finalResult.get_departureTime())
                                    .setScore(finalResult.get_score())
                                    .setTravelTime(finalResult.getTotalCost())
                                    .setRightTurns(finalResult.get_right_turns())
                                    .setSharpTurns(finalResult.get_sharp_turns())
                                    .setPathNodes(finalResult.getPathNodes())
                                    .setWideEdgeIndices(finalResult.getWideEdgeIndices())
                                    .setExecutionTimeMs(execTime)
                                    .setSuccess(finalResult.isPathFound())
                                    .setTotalDistance(finalResult.getPathDistance())
                                    .setOptimalDepartureTime(finalResult.get_departureTime())
                                    .setWideRoadPercentage(wideRoadPct)
                                    .setWideEdgeCount(wideCount)
                                    .setRoutingMode(finalResult.getRoutingMode())
                                    .build();
                                
                                historyManager.addQuery(queryResult);
                                historyPanel.refreshTable();
                            } catch (Exception ex) {
                                System.err.println("Error recording mode change to history: " + ex.getMessage());
                            }
                            
                            setStatus("⚡ Instant recompute: " + finalResult.getParetoPathCount() + 
                                " path(s) found in " + elapsed + "ms (mode: " + newMode.getDisplayName() + ")");
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            setStatus("⚠️ No path found with mode: " + newMode.getDisplayName());
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        setStatus("❌ Recompute error: " + e.getMessage());
                    });
                }
            });
        } else {
            // Cache is not valid - user needs to run a new query
            setStatus("ℹ️ Mode: " + newMode.getDisplayName() + " (run query to see results)");
        }
    }
    
    private void executeQuery() {
        // Cancel any previous running query first
        cancelCurrentQuery();
        queryCancelled.set(false);
        
        int source = queryPanel.getSource();
        int dest = queryPanel.getDestination();
        int departure = queryPanel.getDeparture();
        int interval = queryPanel.getInterval();
        int budget = queryPanel.getBudget();
        int heuristic = queryPanel.getHeuristicMode();
        // Get routing mode from PreferenceSlidersPanel (where the combo box actually is)
        RoutingMode routingMode = preferenceSlidersPanel.getRoutingMode();
        
        setStatus("Running query: " + source + " → " + dest + " (mode: " + routingMode.getDisplayName() + ", timeout: " + QUERY_TIMEOUT_SECONDS + "s)");
        queryPanel.setRunning(true);
        resultsPanel.showLoading();
        mapPanel.clearQueryPreview();
        mapPanel.setSearchProgress(0, "Initializing search...");
        
        // Store reference to the current query future for cancellation
        currentQueryFuture = executor.submit(() -> {
            try {
                long startTime = System.currentTimeMillis();
                
                // Check if cancelled before starting
                if (queryCancelled.get()) {
                    return;
                }
                
                // Update progress
                SwingUtilities.invokeLater(() -> mapPanel.setSearchProgress(10, "Setting up bidirectional search..."));
                
                SwingUtilities.invokeLater(() -> mapPanel.setSearchProgress(30, "Expanding labels..."));
                
                // Run the query with timeout using a separate thread
                BidirectionalAstar.setIntervalDuration(interval);
                
                // Create a callable for the actual query
                java.util.concurrent.Callable<Result> queryTask = () -> 
                    BidirectionalAstar.runSingleQuery(source, dest, departure, interval, budget, routingMode);
                
                java.util.concurrent.ExecutorService queryExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();
                java.util.concurrent.Future<Result> queryFuture = queryExecutor.submit(queryTask);
                
                Result result = null;
                try {
                    result = queryFuture.get(QUERY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                } catch (TimeoutException te) {
                    queryFuture.cancel(true);
                    
                    // Return empty result on timeout
                    result = new Result(departure, 0, 0, 0, 0, 0, new ArrayList<>(), new ArrayList<>());
                    result.setSource(source);
                    result.setDestination(dest);
                    result.setBudget(budget);
                    result.setExecutionTime(System.currentTimeMillis() - startTime);
                    result.setRoutingMode(routingMode);
                    
                    final Result timeoutResult = result;
                    SwingUtilities.invokeLater(() -> {
                        mapPanel.setSearchProgress(100, "Timed out!");
                        mapPanel.clearSearchFrontier();
                        queryPanel.setRunning(false);
                        setStatus("⏱️ Query timed out after " + QUERY_TIMEOUT_SECONDS + "s - No path found within time limit");
                        resultsPanel.showError("Query timed out after " + QUERY_TIMEOUT_SECONDS + " seconds. Try a shorter distance or simpler query.");
                    });
                    queryExecutor.shutdownNow();
                    return;
                } finally {
                    queryExecutor.shutdownNow();
                }
                
                // Check if cancelled after query completed
                if (queryCancelled.get()) {
                    return;
                }
                
                SwingUtilities.invokeLater(() -> mapPanel.setSearchProgress(80, "Reconstructing path..."));
                
                long elapsed = System.currentTimeMillis() - startTime;
                
                // Create result with enhanced info if null
                if (result == null) {
                    result = new Result(departure, 0, 0, 0, 0, 0, new ArrayList<>(), new ArrayList<>());
                }
                result.setSource(source);
                result.setDestination(dest);
                result.setBudget(budget);
                result.setExecutionTime(elapsed);
                result.setRoutingMode(routingMode);
                
                // Get path coordinates
                if (result.isPathFound()) {
                    collectPathCoordinates(result);
                    // Also collect coordinates for Pareto paths if available
                    if (result.hasParetoOptimalPaths()) {
                        for (Result paretoPath : result.getParetoOptimalPaths()) {
                            collectPathCoordinates(paretoPath);
                        }
                    }
                }
                
                lastResult = result;
                final Result finalResult = result;
                final int srcNode = source;
                final int dstNode = dest;
                final int departTime = departure;
                final int intervalDur = interval;
                final int budgetVal = budget;
                
                SwingUtilities.invokeLater(() -> {
                    try {
                        mapPanel.setSearchProgress(100, "Complete!");
                        mapPanel.clearSearchFrontier();
                        displayResults(finalResult);
                        queryPanel.setRunning(false);
                        
                        // Record to history and metrics
                        // Calculate wide road percentage
                        int pathLen = finalResult.getPathNodes() != null ? finalResult.getPathNodes().size() : 0;
                        int wideCount = finalResult.getWideEdgeIndices() != null ? finalResult.getWideEdgeIndices().size() : 0;
                        double wideRoadPct = pathLen > 1 ? (100.0 * wideCount / (pathLen - 1)) : 0.0;
                        
                        QueryResult queryResult = new QueryResult.Builder()
                            .setSourceNode(srcNode)
                            .setDestinationNode(dstNode)
                            .setDepartureTime(departTime)
                            .setIntervalDuration(intervalDur)
                            .setBudget(budgetVal)
                            .setActualDepartureTime(finalResult.get_departureTime())
                            .setScore(finalResult.get_score())
                            .setTravelTime(finalResult.getTotalCost())
                            .setRightTurns(finalResult.get_right_turns())
                            .setSharpTurns(finalResult.get_sharp_turns())
                            .setPathNodes(finalResult.getPathNodes())
                            .setWideEdgeIndices(finalResult.getWideEdgeIndices())
                            .setExecutionTimeMs((long)finalResult.getExecutionTime())
                            .setSuccess(finalResult.isPathFound())
                            // New fields
                            .setTotalDistance(finalResult.getPathDistance())
                            .setOptimalDepartureTime(finalResult.get_departureTime())
                            .setWideRoadPercentage(wideRoadPct)
                            .setWideEdgeCount(wideCount)
                            .setRoutingMode(finalResult.getRoutingMode())
                            .build();
                        
                        historyManager.addQuery(queryResult);
                        metricsCollector.recordQuery(finalResult.isPathFound(), (long)finalResult.getExecutionTime(), 
                            finalResult.getPathNodes() != null ? finalResult.getPathNodes().size() : 0);
                        
                        // Refresh history panel
                        historyPanel.refreshTable();
                    } catch (Exception ex) {
                        System.err.println("Error recording query to history: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                    
                    // Show appropriate status based on results
                    if (finalResult.hasParetoOptimalPaths()) {
                        setStatus(String.format("✅ Found %d Pareto optimal paths in %.2f ms", 
                            finalResult.getParetoPathCount(), finalResult.getExecutionTime()));
                    } else {
                        setStatus(finalResult.isPathFound() 
                            ? String.format("✅ Path found: %d nodes in %.2f ms", finalResult.getPathLength(), finalResult.getExecutionTime())
                            : "❌ No path found within budget");
                    }
                    
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
            .departureTime(queryPanel.getDeparture())
            .suggestedDepartureTime(result.get_departureTime())  // Algorithm's suggested departure time
            .executionTime(result.getExecutionTime())
            .pathFound(result.isPathFound())
            .totalCost(result.getTotalCost())
            .totalDistance(result.getPathDistance())  // Total physical distance
            .pathLength(result.getPathLength())
            .wideEdgeCount(result.getWideEdgeCount())
            .rightTurns(result.get_right_turns())
            .wideRoadPercentage(result.get_score())
            .routingModeName(result.getRoutingMode() != null ? result.getRoutingMode().getDisplayName() : "Wideness Only")
            .pathNodes(result.getPathNodes())
            .pathCoordinates(result.getPathCoordinates())
            .wideEdgeIndices(result.getWideEdgeIndices());
        
        // Convert Pareto optimal paths if available
        if (result.hasParetoOptimalPaths()) {
            for (Result paretoPath : result.getParetoOptimalPaths()) {
                ResultData paretoData = ResultData.create()
                    .source(paretoPath.getSource())
                    .destination(paretoPath.getDestination())
                    .budget(paretoPath.getBudget())
                    .departureTime(queryPanel.getDeparture())
                    .suggestedDepartureTime(paretoPath.get_departureTime())
                    .executionTime(paretoPath.getExecutionTime())
                    .pathFound(paretoPath.isPathFound())
                    .totalCost(paretoPath.getTotalCost())
                    .totalDistance(paretoPath.getPathDistance())  // Total physical distance
                    .pathLength(paretoPath.getPathLength())
                    .wideEdgeCount(paretoPath.getWideEdgeCount())
                    .rightTurns(paretoPath.get_right_turns())
                    .wideRoadPercentage(paretoPath.get_score())
                    .pathNodes(paretoPath.getPathNodes())
                    .pathCoordinates(paretoPath.getPathCoordinates())
                    .wideEdgeIndices(paretoPath.getWideEdgeIndices());
                resultData.addParetoPath(paretoData);
            }
        }
        
        // Update results panel
        resultsPanel.displayResult(resultData);
        
        // Store last result for map mode switching
        lastResult = result;
        
        // Update map with path and graph context
        if (result.isPathFound()) {
            // Collect subgraph around the path for map-like visualization
            Object[] subgraph = collectSubgraphForPath(result.getPathCoordinates());
            @SuppressWarnings("unchecked")
            List<double[]> subgraphNodes = (List<double[]>) subgraph[0];
            @SuppressWarnings("unchecked")
            List<int[]> subgraphEdges = (List<int[]>) subgraph[1];
            
            // Set path with subgraph on coordinate-based map
            mapPanel.setPathWithContextAndSubgraph(
                result.getPathNodes(), 
                result.getWideEdgeIndices(), 
                result.getPathCoordinates(),
                null,  // context removed
                null,  // context removed
                null,  // context removed
                subgraphNodes,
                subgraphEdges
            );
            
            // Also display on OSM map component
            displayPathOnCurrentMap(result);
            
            rightTabs.setSelectedIndex(0); // Switch to map view
        }
    }
    
    /**
     * Collect subgraph nodes and edges within a bounding box around the path
     * Returns: [subgraph node coordinates, subgraph edges]
     */
    private Object[] collectSubgraphForPath(List<double[]> pathCoordinates) {
        List<double[]> subgraphNodes = new ArrayList<>();
        List<int[]> subgraphEdges = new ArrayList<>();
        
        if (pathCoordinates == null || pathCoordinates.isEmpty()) {
            return new Object[]{subgraphNodes, subgraphEdges};
        }
        
        // Calculate bounding box of the path
        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;
        
        for (double[] coord : pathCoordinates) {
            minLat = Math.min(minLat, coord[0]);
            maxLat = Math.max(maxLat, coord[0]);
            minLon = Math.min(minLon, coord[1]);
            maxLon = Math.max(maxLon, coord[1]);
        }
        
        // Add 80% padding to show more context around the path
        double latPad = (maxLat - minLat) * 0.8 + 0.01;
        double lonPad = (maxLon - minLon) * 0.8 + 0.01;
        minLat -= latPad; maxLat += latPad;
        minLon -= lonPad; maxLon += lonPad;
        
        Map<Integer, Node> allNodes = Graph.get_nodes();
        Map<Integer, Integer> nodeIdToIndex = new HashMap<>();
        
        // Collect nodes within bounding box
        for (Map.Entry<Integer, Node> entry : allNodes.entrySet()) {
            Node node = entry.getValue();
            double lat = node.get_latitude();
            double lon = node.get_longitude();
            
            if (lat >= minLat && lat <= maxLat && lon >= minLon && lon <= maxLon) {
                int idx = subgraphNodes.size();
                nodeIdToIndex.put(entry.getKey(), idx);
                subgraphNodes.add(new double[]{lat, lon});
            }
        }
        
        // Collect edges between nodes in the subgraph
        for (Map.Entry<Integer, Node> entry : allNodes.entrySet()) {
            Integer fromId = entry.getKey();
            if (!nodeIdToIndex.containsKey(fromId)) continue;
            
            Node node = entry.getValue();
            Map<Integer, Edge> outgoing = node.get_outgoing_edges();
            if (outgoing != null) {
                for (Integer toId : outgoing.keySet()) {
                    if (nodeIdToIndex.containsKey(toId)) {
                        subgraphEdges.add(new int[]{nodeIdToIndex.get(fromId), nodeIdToIndex.get(toId)});
                    }
                }
            }
        }
        
        return new Object[]{subgraphNodes, subgraphEdges};
    }
    
    private void updateQueryPreview(int source, int dest) {
        Map<Integer, Node> allNodes = Graph.get_nodes();
        Node srcNode = allNodes.get(source);
        Node dstNode = allNodes.get(dest);
        
        if (srcNode != null && dstNode != null) {
            double[] srcCoord = new double[]{srcNode.get_latitude(), srcNode.get_longitude()};
            double[] dstCoord = new double[]{dstNode.get_latitude(), dstNode.get_longitude()};
            
            // Calculate bounding box with padding
            double minLat = Math.min(srcNode.get_latitude(), dstNode.get_latitude());
            double maxLat = Math.max(srcNode.get_latitude(), dstNode.get_latitude());
            double minLon = Math.min(srcNode.get_longitude(), dstNode.get_longitude());
            double maxLon = Math.max(srcNode.get_longitude(), dstNode.get_longitude());
            
            // Add 150% padding to the bounding box to show a larger subgraph (more map-like feel)
            double latPad = (maxLat - minLat) * 1.5 + 0.02;
            double lonPad = (maxLon - minLon) * 1.5 + 0.02;
            minLat -= latPad; maxLat += latPad;
            minLon -= lonPad; maxLon += lonPad;
            
            // Collect subgraph nodes and edges within bounding box
            List<double[]> subgraphNodes = new ArrayList<>();
            List<int[]> subgraphEdges = new ArrayList<>();
            Map<Integer, Integer> nodeIdToIndex = new HashMap<>();
            
            // First pass: collect nodes within bounding box
            for (Map.Entry<Integer, Node> entry : allNodes.entrySet()) {
                Node node = entry.getValue();
                double lat = node.get_latitude();
                double lon = node.get_longitude();
                
                if (lat >= minLat && lat <= maxLat && lon >= minLon && lon <= maxLon) {
                    int idx = subgraphNodes.size();
                    nodeIdToIndex.put(entry.getKey(), idx);
                    subgraphNodes.add(new double[]{lat, lon});
                }
            }
            
            // Second pass: collect edges between nodes in the subgraph
            for (Map.Entry<Integer, Node> entry : allNodes.entrySet()) {
                Integer fromId = entry.getKey();
                if (!nodeIdToIndex.containsKey(fromId)) continue;
                
                Node node = entry.getValue();
                Map<Integer, Edge> outgoing = node.get_outgoing_edges();
                if (outgoing != null) {
                    for (Integer toId : outgoing.keySet()) {
                        if (nodeIdToIndex.containsKey(toId)) {
                            subgraphEdges.add(new int[]{nodeIdToIndex.get(fromId), nodeIdToIndex.get(toId)});
                        }
                    }
                }
            }
            
            // Set preview with subgraph on both map panels
            mapPanel.setQueryPreviewWithSubgraph(source, dest, srcCoord, dstCoord, subgraphNodes, subgraphEdges);
            
            // Also update OSM map component when in OSM mode
            if (currentMapMode == MapViewMode.OSM_TILES) {
                osmMapComponent.setQueryPreviewWithSubgraph(source, dest, srcCoord, dstCoord, subgraphNodes, subgraphEdges);
                // Update selected node markers on map
                osmMapComponent.setSelectedSource(source, srcCoord[0], srcCoord[1]);
                osmMapComponent.setSelectedDestination(dest, dstCoord[0], dstCoord[1]);
            }
        }
    }

    private void writePathToFile(Result result) {
        if (result == null || !result.isPathFound()) return;
        
        try {
            Files.createDirectories(Paths.get("output"));
            
            StringBuilder sb = new StringBuilder();
            sb.append("FlexiRoute Query Result\n");
            sb.append("=======================\n\n");
            sb.append("Source: ").append(result.getSource()).append("\n");
            sb.append("Destination: ").append(result.getDestination()).append("\n");
            sb.append("Path Length: ").append(result.getPathLength()).append(" nodes\n");
            sb.append("Total Cost: ").append(String.format("%.2f", result.getTotalCost())).append("\n");
            sb.append("Wide Edges: ").append(result.getWideEdgeCount()).append("\n");
            sb.append("Execution Time: ").append(String.format("%.2f", result.getExecutionTime())).append(" ms\n");
            
            // Add suggested departure time
            double suggestedDep = result.get_departureTime();
            if (suggestedDep > 0) {
                int hours = (int)(suggestedDep / 60);
                int mins = (int)(suggestedDep % 60);
                sb.append(String.format("\n*** OPTIMAL DEPARTURE TIME: %02d:%02d (%.1f minutes) ***\n", 
                    hours, mins, suggestedDep));
            }
            sb.append("\n");
            
            sb.append("Path Node IDs with Coordinates:\n");
            sb.append("================================\n");
            sb.append("[Step] NodeID -> (Latitude, Longitude)\n");
            List<Integer> path = result.getPathNodes();
            List<double[]> coords = result.getPathCoordinates();
            for (int i = 0; i < path.size(); i++) {
                int nodeId = path.get(i);
                String coordStr = "";
                if (coords != null && i < coords.size()) {
                    double[] coord = coords.get(i);
                    coordStr = String.format(" -> (%.6f, %.6f)", coord[0], coord[1]);
                }
                String marker = "";
                if (i == 0) marker = " [START]";
                else if (i == path.size() - 1) marker = " [END]";
                
                sb.append(String.format("[%3d] %d%s%s\n", i, nodeId, coordStr, marker));
            }
            
            sb.append("\n\nPath Coordinates Only (lat, lon):\n");
            sb.append("==================================\n");
            
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
    
    private void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
            statusLabel.setForeground(NEON_GREEN);
        });
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
            <h2>🗺️ FlexiRoute Navigator User Guide</h2>
            
            <h3>Quick Start</h3>
            <ol>
                <li>Enter source and destination node IDs</li>
                <li>Adjust travel budget using the slider</li>
                <li>Click "Find Route" or press Ctrl+Enter</li>
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
            <h1>🗺️ FlexiRoute Navigator</h1>
            <p>Advanced pathfinding with wide road optimization</p>
            <br>
            <p>Using Bi-TDCPO algorithm for optimal<br>
            constrained path queries on road networks.</p>
            <br>
            <p><small>© 2024 FlexiRoute Project</small></p>
            </body>
            </html>
            """;
        
        JOptionPane.showMessageDialog(this, about, "About", JOptionPane.PLAIN_MESSAGE);
    }
    
    private void exitApplication() {
        // Create custom confirmation dialog with styling
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel icon = new JLabel("🚪");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel message = new JLabel("<html><div style='text-align: center;'>" +
            "<b>Exit FlexiRoute Navigator?</b><br><br>" +
            "Are you sure you want to exit?<br>" +
            "All unsaved data will be lost." +
            "</div></html>");
        message.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        message.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(icon, BorderLayout.NORTH);
        panel.add(message, BorderLayout.CENTER);
        
        String[] options = {"Exit", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
            this,
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
        setStatus("👋 Shutting down FlexiRoute Navigator...");
        
        // Cleanup resources
        try {
            if (metricsDashboard != null) {
                metricsDashboard.dispose();
            }
            executor.shutdown();
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        dispose();
        System.exit(0);
    }
    
    // === MAIN ENTRY POINT ===
    
    public static void main(String[] args) {
        // Show splash screen
        SwingUtilities.invokeLater(() -> {
            SplashScreen splash = new SplashScreen();
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
                    GuiLauncher app = new GuiLauncher();
                    app.setVisible(true);
                    
                    splash.closeSplash();
                }
            });
            
            loadTimer.start();
        });
    }
}
