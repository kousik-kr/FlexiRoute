package map;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * OSM Map Component - Tile-based map visualization with route overlay.
 * 
 * Architecture:
 * +--------------------------+
 * | OSMMapComponent          |
 * |  - Tile rendering        |
 * |  - Pan/zoom interaction  |
 * |  - Route overlay         |
 * |  - Info overlays         |
 * +--------------------------+
 *           |
 *           v
 * +--------------------------+     +--------------------------+
 * | TileProvider             |     | RouteOverlayRenderer     |
 * |  - Tile loading          |     |  - Path drawing          |
 * |  - Caching               |     |  - Markers               |
 * +--------------------------+     +--------------------------+
 *           |
 *           v
 * +--------------------------+
 * | CoordinateConverter      |
 * |  - Lat/Lon ↔ Pixel       |
 * +--------------------------+
 */
public class OSMMapComponent extends JPanel implements TileProvider.TileLoadListener {
    
    // === COLOR PALETTE ===
    private static final Color BG_COLOR = new Color(242, 243, 245);
    private static final Color OVERLAY_BG = new Color(255, 255, 255, 230);
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color ACCENT_COLOR = new Color(59, 130, 246);
    private static final Color VIVID_PURPLE = new Color(168, 85, 247);
    
    // === NODE SELECTION MODE ===
    public enum NodeSelectionMode {
        NONE,           // Normal pan/zoom mode
        SELECT_SOURCE,  // Click to select source node
        SELECT_DEST     // Click to select destination node
    }
    
    // Node selection callback interface
    public interface NodeSelectionListener {
        void onNodeSelected(int nodeId, double lat, double lon, boolean isSource);
    }
    
    // Interface for finding nearest node (to be implemented by caller that has access to Graph)
    public interface NearestNodeFinder {
        /**
         * Find the nearest node to given lat/lon coordinates
         * @return array of [nodeId, nodeLat, nodeLon] or null if not found
         */
        double[] findNearestNode(double lat, double lon);
    }
    
    // === COMPONENTS ===
    private final TileProvider tileProvider;
    private final CoordinateConverter converter;
    private final RouteOverlayRenderer routeRenderer;
    
    // === STATE ===
    private List<double[]> pathCoordinates = new ArrayList<>();
    private List<Integer> wideEdges = new ArrayList<>();
    private List<double[]> subgraphNodes = new ArrayList<>();
    private List<int[]> subgraphEdges = new ArrayList<>();
    
    // Pareto paths state for hybrid mode
    private List<RouteOverlayRenderer.ParetoPathInfo> paretoPaths = new ArrayList<>();
    private boolean showParetoPaths = false;
    
    // Route info popup state
    private RouteOverlayRenderer.ParetoPathInfo selectedPath = null;
    private Point popupLocation = null;
    private boolean showRouteInfoPopup = false;
    
    // Preview state
    private double[] previewSourceCoord;
    private double[] previewDestCoord;
    
    // Node selection state
    private NodeSelectionMode selectionMode = NodeSelectionMode.NONE;
    private NodeSelectionListener nodeSelectionListener;
    private NearestNodeFinder nearestNodeFinder;
    private Integer selectedSourceNodeId = null;
    private Integer selectedDestNodeId = null;
    private double[] selectedSourceCoord = null;
    private double[] selectedDestCoord = null;
    private Point hoverPoint = null;  // Current mouse position for hover effect
    private int nearestNodeIdUnderMouse = -1;  // Node ID nearest to mouse in selection mode
    private double[] nearestNodeCoordUnderMouse = null;
    
    // Selection mode UI elements
    private JToggleButton selectSourceBtn;
    private JToggleButton selectDestBtn;
    
    // Interaction state
    private Point dragStart;
    private boolean isDragging = false;
    
    // View settings
    private boolean showGrid = false;
    private boolean showLabels = true;
    private boolean showSubgraph = false;
    private boolean animatePath = false;
    
    // Animation
    private Timer animationTimer;
    private float animationPhase = 0f;
    
    // Progress overlay
    private boolean showProgress = false;
    private int progressValue = 0;
    private String progressMessage = "";
    
    // Render mode (for backwards compatibility)
    public enum RenderMode {
        OSM_STANDARD("🗺️ OSM Standard", TileProvider.TileServer.OSM_STANDARD),
        CARTO_LIGHT("☁️ Carto Light", TileProvider.TileServer.CARTO_LIGHT),
        CARTO_DARK("🌙 Carto Dark", TileProvider.TileServer.CARTO_DARK),
        HUMANITARIAN("🏥 Humanitarian", TileProvider.TileServer.OSM_HUMANITARIAN),
        TERRAIN("⛰️ Terrain", TileProvider.TileServer.STAMEN_TERRAIN);
        
        private final String displayName;
        private final TileProvider.TileServer server;
        
        RenderMode(String displayName, TileProvider.TileServer server) {
            this.displayName = displayName;
            this.server = server;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public TileProvider.TileServer getServer() {
            return server;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    private RenderMode currentMode = RenderMode.OSM_STANDARD;
    
    public OSMMapComponent() {
        this.tileProvider = new TileProvider();
        this.converter = new CoordinateConverter();
        this.routeRenderer = new RouteOverlayRenderer(converter);
        
        tileProvider.setTileLoadListener(this);
        
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(900, 650));
        setLayout(new BorderLayout());
        
        setupInteraction();
        setupAnimation();
        add(createToolbar(), BorderLayout.NORTH);
        add(createLegend(), BorderLayout.EAST);
        
        // Update converter when resized
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                converter.setViewSize(getWidth(), getHeight());
                repaint();
            }
        });
    }
    
    private void setupInteraction() {
        // Mouse wheel zoom
        addMouseWheelListener((MouseWheelEvent e) -> {
            int oldZoom = converter.getZoomLevel();
            int newZoom = oldZoom - e.getWheelRotation();
            newZoom = Math.max(1, Math.min(19, newZoom));
            
            if (newZoom != oldZoom) {
                // Zoom toward mouse position
                Point mousePos = e.getPoint();
                double[] latLon = converter.pixelToLatLon(mousePos.x, mousePos.y);
                
                converter.setZoomLevel(newZoom);
                
                // Adjust center to keep mouse position stable
                Point2D.Double newMousePos = converter.latLonToPixel(latLon[0], latLon[1]);
                converter.addPanOffset(mousePos.x - newMousePos.x, mousePos.y - newMousePos.y);
                
                repaint();
            }
        });
        
        // Pan with mouse drag and route click detection
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    dragStart = e.getPoint();
                    isDragging = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging = false;
                dragStart = null;
                setCursor(Cursor.getDefaultCursor());
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && !isDragging) {
                    // Check if we're in node selection mode
                    if (selectionMode != NodeSelectionMode.NONE) {
                        handleNodeSelection(e.getPoint());
                    } else {
                        handleRouteClick(e.getPoint());
                    }
                }
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging && dragStart != null) {
                    double dx = e.getX() - dragStart.x;
                    double dy = e.getY() - dragStart.y;
                    converter.addPanOffset(dx, dy);
                    dragStart = e.getPoint();
                    repaint();
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                // Track hover point for selection mode feedback
                if (selectionMode != NodeSelectionMode.NONE) {
                    hoverPoint = e.getPoint();
                    updateNearestNodeUnderMouse(e.getPoint());
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                    repaint();
                } else {
                    hoverPoint = null;
                    nearestNodeIdUnderMouse = -1;
                    nearestNodeCoordUnderMouse = null;
                }
            }
        });
    }
    
    private void setupAnimation() {
        animationTimer = new Timer(50, e -> {
            animationPhase += 0.05f;
            if (animationPhase > 2 * Math.PI) {
                animationPhase = 0;
            }
            routeRenderer.setAnimationPhase(animationPhase);
            if (animatePath && !pathCoordinates.isEmpty()) {
                repaint();
            }
        });
        animationTimer.start();
    }
    
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(255, 255, 255),
                    0, getHeight(), new Color(248, 250, 255)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, VIVID_PURPLE),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        
        // Map style selector
        JComboBox<RenderMode> modeCombo = new JComboBox<>(RenderMode.values());
        modeCombo.setSelectedItem(currentMode);
        modeCombo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        modeCombo.setMaximumSize(new Dimension(160, 32));
        modeCombo.addActionListener(e -> {
            currentMode = (RenderMode) modeCombo.getSelectedItem();
            tileProvider.setTileServer(currentMode.getServer());
            tileProvider.clearCache();
            repaint();
        });
        toolbar.add(new JLabel("🗺️ "));
        toolbar.add(modeCombo);
        toolbar.add(Box.createHorizontalStrut(10));
        
        // Path style selector
        JComboBox<RouteOverlayRenderer.PathStyle> pathStyleCombo = 
            new JComboBox<>(RouteOverlayRenderer.PathStyle.values());
        pathStyleCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pathStyleCombo.setMaximumSize(new Dimension(120, 32));
        pathStyleCombo.addActionListener(e -> {
            routeRenderer.setPathStyle((RouteOverlayRenderer.PathStyle) pathStyleCombo.getSelectedItem());
            repaint();
        });
        toolbar.add(new JLabel("Path: "));
        toolbar.add(pathStyleCombo);
        toolbar.add(Box.createHorizontalStrut(10));
        
        // Zoom controls
        JButton zoomIn = createToolbarButton("➕ Zoom In", "Zoom In");
        zoomIn.addActionListener(e -> {
            converter.setZoomLevel(Math.min(19, converter.getZoomLevel() + 1));
            repaint();
        });
        toolbar.add(zoomIn);
        
        JButton zoomOut = createToolbarButton("➖ Zoom Out", "Zoom Out");
        zoomOut.addActionListener(e -> {
            converter.setZoomLevel(Math.max(1, converter.getZoomLevel() - 1));
            repaint();
        });
        toolbar.add(zoomOut);
        
        JButton resetView = createToolbarButton("🔄", "Reset View");
        resetView.addActionListener(e -> resetView());
        toolbar.add(resetView);
        
        JButton fitPath = createToolbarButton("📐", "Fit to Path");
        fitPath.addActionListener(e -> fitToPath());
        toolbar.add(fitPath);
        
        JButton clearMap = createToolbarButton("🗑️ Clear", "Clear Map");
        clearMap.addActionListener(e -> clearMap());
        toolbar.add(clearMap);
        
        toolbar.add(Box.createHorizontalStrut(15));
        
        // === NODE SELECTION BUTTONS ===
        JLabel selectLabel = new JLabel("📍 Select: ");
        selectLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        selectLabel.setForeground(new Color(16, 185, 129));
        toolbar.add(selectLabel);
        
        selectSourceBtn = new JToggleButton("🟢 Source") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) {
                    g2d.setColor(new Color(16, 185, 129, 50));
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2d.setColor(new Color(16, 185, 129));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 8, 8);
                }
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        selectSourceBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        selectSourceBtn.setToolTipText("Click on map to select source node");
        selectSourceBtn.setFocusPainted(false);
        selectSourceBtn.setBorderPainted(false);
        selectSourceBtn.setContentAreaFilled(false);
        selectSourceBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        selectSourceBtn.setPreferredSize(new Dimension(90, 28));
        selectSourceBtn.setMaximumSize(new Dimension(90, 28));
        selectSourceBtn.addActionListener(e -> {
            if (selectSourceBtn.isSelected()) {
                selectionMode = NodeSelectionMode.SELECT_SOURCE;
                selectDestBtn.setSelected(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            } else {
                selectionMode = NodeSelectionMode.NONE;
                setCursor(Cursor.getDefaultCursor());
                hoverPoint = null;
                repaint();
            }
        });
        toolbar.add(selectSourceBtn);
        
        toolbar.add(Box.createHorizontalStrut(5));
        
        selectDestBtn = new JToggleButton("🔴 Dest") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) {
                    g2d.setColor(new Color(255, 107, 107, 50));
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2d.setColor(new Color(255, 107, 107));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 8, 8);
                }
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        selectDestBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        selectDestBtn.setToolTipText("Click on map to select destination node");
        selectDestBtn.setFocusPainted(false);
        selectDestBtn.setBorderPainted(false);
        selectDestBtn.setContentAreaFilled(false);
        selectDestBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        selectDestBtn.setPreferredSize(new Dimension(80, 28));
        selectDestBtn.setMaximumSize(new Dimension(80, 28));
        selectDestBtn.addActionListener(e -> {
            if (selectDestBtn.isSelected()) {
                selectionMode = NodeSelectionMode.SELECT_DEST;
                selectSourceBtn.setSelected(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            } else {
                selectionMode = NodeSelectionMode.NONE;
                setCursor(Cursor.getDefaultCursor());
                hoverPoint = null;
                repaint();
            }
        });
        toolbar.add(selectDestBtn);
        
        toolbar.add(Box.createHorizontalStrut(10));
        
        // Options
        JCheckBox showGridCb = new JCheckBox("Grid", showGrid);
        showGridCb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showGridCb.setOpaque(false);
        showGridCb.addActionListener(e -> {
            showGrid = showGridCb.isSelected();
            repaint();
        });
        toolbar.add(showGridCb);
        
        JCheckBox animateCb = new JCheckBox("Animate", animatePath);
        animateCb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        animateCb.setOpaque(false);
        animateCb.addActionListener(e -> {
            animatePath = animateCb.isSelected();
            if (animatePath) {
                routeRenderer.setPathStyle(RouteOverlayRenderer.PathStyle.ANIMATED);
            }
            repaint();
        });
        toolbar.add(animateCb);
        
        JCheckBox arrowsCb = new JCheckBox("Arrows", true);
        arrowsCb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        arrowsCb.setOpaque(false);
        arrowsCb.addActionListener(e -> {
            routeRenderer.setShowDirectionArrows(arrowsCb.isSelected());
            repaint();
        });
        toolbar.add(arrowsCb);
        
        toolbar.add(Box.createHorizontalGlue());
        
        // Export button
        JButton exportBtn = createToolbarButton("💾 Export", "Export Map");
        exportBtn.addActionListener(e -> exportImage());
        toolbar.add(exportBtn);
        
        return toolbar;
    }
    
    private JButton createToolbarButton(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setToolTipText(tooltip);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Size based on text length (icons vs text+icons)
        int width = text.length() > 3 ? 100 : 36;
        btn.setPreferredSize(new Dimension(width, 28));
        btn.setMaximumSize(new Dimension(width, 28));
        return btn;
    }
    
    private JPanel createLegend() {
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
        legend.setBackground(new Color(255, 255, 255, 240));
        legend.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 2, 0, 0, VIVID_PURPLE),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        legend.setPreferredSize(new Dimension(150, 0));
        
        JLabel title = new JLabel("🎯 Legend");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(VIVID_PURPLE);
        legend.add(title);
        legend.add(Box.createVerticalStrut(12));
        
        legend.add(createLegendItem("🟢 Source", RouteOverlayRenderer.SOURCE_COLOR));
        legend.add(createLegendItem("🔴 Destination", RouteOverlayRenderer.DEST_COLOR));
        legend.add(createLegendItem("━ Route", RouteOverlayRenderer.PATH_PRIMARY));
        legend.add(createLegendItem("━ Wide Road", RouteOverlayRenderer.PATH_WIDE));
        legend.add(Box.createVerticalGlue());
        
        return legend;
    }
    
    private JLabel createLegendItem(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(color.darker());
        label.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        return label;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        try {
            // Update converter size
            converter.setViewSize(getWidth(), getHeight());
            
            // Render map tiles (always render tiles, don't cover with empty state)
            renderTiles(g2d);
            
            // Grid overlay
            if (showGrid) {
                renderGrid(g2d);
            }
            
            // Subgraph overlay
            if (showSubgraph && !subgraphNodes.isEmpty()) {
                renderSubgraph(g2d);
            }
            
            // Route or preview
            if (showParetoPaths && !paretoPaths.isEmpty()) {
                // Render multiple Pareto paths with color coding
                routeRenderer.renderParetoPaths(g2d, paretoPaths);
            } else if (!pathCoordinates.isEmpty()) {
                routeRenderer.renderRoute(g2d, pathCoordinates, wideEdges);
            } else if (previewSourceCoord != null && previewDestCoord != null) {
                routeRenderer.renderPreviewLine(g2d, previewSourceCoord, previewDestCoord);
            }
            // Don't render empty state - let the tiles show through
            
            // Render selected source/destination markers
            renderSelectedNodes(g2d);
            
            // Render hover effect in selection mode
            if (selectionMode != NodeSelectionMode.NONE) {
                renderSelectionModeOverlay(g2d);
            }
            
            // Info overlay
            if (showLabels) {
                renderInfoOverlay(g2d);
            }
            
            // Scale bar
            routeRenderer.renderScaleBar(g2d, 20, getHeight() - 60);
            
            // Progress overlay
            if (showProgress) {
                renderProgressOverlay(g2d);
            }
            
            // Route info popup
            if (showRouteInfoPopup && selectedPath != null && popupLocation != null) {
                renderRouteInfoPopup(g2d);
            }
            
        } finally {
            g2d.dispose();
        }
    }
    
    /**
     * Handle click on the map to detect if a route was clicked
     */
    private void handleRouteClick(Point clickPoint) {
        if (!showParetoPaths || paretoPaths.isEmpty()) {
            // Hide popup if clicking elsewhere when no Pareto paths
            showRouteInfoPopup = false;
            selectedPath = null;
            repaint();
            return;
        }
        
        // Check if click is near any Pareto path
        RouteOverlayRenderer.ParetoPathInfo clickedPath = findClickedPath(clickPoint);
        
        if (clickedPath != null) {
            selectedPath = clickedPath;
            popupLocation = clickPoint;
            showRouteInfoPopup = true;
        } else {
            // Hide popup if clicking elsewhere
            showRouteInfoPopup = false;
            selectedPath = null;
        }
        repaint();
    }
    
    /**
     * Handle node selection when in selection mode
     */
    private void handleNodeSelection(Point clickPoint) {
        if (nearestNodeFinder == null) {
            JOptionPane.showMessageDialog(this, 
                "Node selection not available. Please load a dataset first.", 
                "No Dataset", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Convert click point to lat/lon
        double[] latLon = converter.pixelToLatLon(clickPoint.x, clickPoint.y);
        double clickLat = latLon[0];
        double clickLon = latLon[1];
        
        // Find nearest node using the external finder
        double[] nearestResult = nearestNodeFinder.findNearestNode(clickLat, clickLon);
        
        if (nearestResult == null) {
            JOptionPane.showMessageDialog(this, 
                "No nodes found! Please load a dataset first.", 
                "No Nodes", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int nearestNodeId = (int) nearestResult[0];
        double nodeLat = nearestResult[1];
        double nodeLon = nearestResult[2];
        
        // Store selection based on mode
        if (selectionMode == NodeSelectionMode.SELECT_SOURCE) {
            selectedSourceNodeId = nearestNodeId;
            selectedSourceCoord = new double[] { nodeLat, nodeLon };
            
            // Notify listener
            if (nodeSelectionListener != null) {
                nodeSelectionListener.onNodeSelected(nearestNodeId, nodeLat, nodeLon, true);
            }
            
            // Auto-switch to destination selection for convenience
            selectionMode = NodeSelectionMode.SELECT_DEST;
            selectSourceBtn.setSelected(false);
            selectDestBtn.setSelected(true);
            
        } else if (selectionMode == NodeSelectionMode.SELECT_DEST) {
            selectedDestNodeId = nearestNodeId;
            selectedDestCoord = new double[] { nodeLat, nodeLon };
            
            // Notify listener
            if (nodeSelectionListener != null) {
                nodeSelectionListener.onNodeSelected(nearestNodeId, nodeLat, nodeLon, false);
            }
            
            // Exit selection mode
            selectionMode = NodeSelectionMode.NONE;
            selectSourceBtn.setSelected(false);
            selectDestBtn.setSelected(false);
            setCursor(Cursor.getDefaultCursor());
            hoverPoint = null;
        }
        
        repaint();
    }
    
    /**
     * Update nearest node tracking for hover effect
     */
    private void updateNearestNodeUnderMouse(Point mousePoint) {
        if (nearestNodeFinder == null) {
            nearestNodeIdUnderMouse = -1;
            nearestNodeCoordUnderMouse = null;
            return;
        }
        
        double[] latLon = converter.pixelToLatLon(mousePoint.x, mousePoint.y);
        double[] nearestResult = nearestNodeFinder.findNearestNode(latLon[0], latLon[1]);
        
        if (nearestResult != null) {
            nearestNodeIdUnderMouse = (int) nearestResult[0];
            nearestNodeCoordUnderMouse = new double[] { nearestResult[1], nearestResult[2] };
        } else {
            nearestNodeIdUnderMouse = -1;
            nearestNodeCoordUnderMouse = null;
        }
    }
    
    /**
     * Find which Pareto path was clicked (if any)
     * Returns the path closest to the click point within a threshold distance
     */
    private RouteOverlayRenderer.ParetoPathInfo findClickedPath(Point clickPoint) {
        final double CLICK_THRESHOLD = 15.0; // pixels
        
        RouteOverlayRenderer.ParetoPathInfo closestPath = null;
        double minDistance = Double.MAX_VALUE;
        
        for (RouteOverlayRenderer.ParetoPathInfo path : paretoPaths) {
            if (path.coordinates == null || path.coordinates.size() < 2) continue;
            
            // Check distance to each segment of the path
            for (int i = 0; i < path.coordinates.size() - 1; i++) {
                double[] c1 = path.coordinates.get(i);
                double[] c2 = path.coordinates.get(i + 1);
                
                Point2D.Double p1 = converter.latLonToPixel(c1[0], c1[1]);
                Point2D.Double p2 = converter.latLonToPixel(c2[0], c2[1]);
                
                double dist = pointToSegmentDistance(clickPoint.x, clickPoint.y, p1.x, p1.y, p2.x, p2.y);
                
                if (dist < minDistance && dist < CLICK_THRESHOLD) {
                    minDistance = dist;
                    closestPath = path;
                }
            }
        }
        
        return closestPath;
    }
    
    /**
     * Calculate the distance from a point to a line segment
     */
    private double pointToSegmentDistance(double px, double py, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double lengthSq = dx * dx + dy * dy;
        
        if (lengthSq == 0) {
            // Segment is a point
            return Math.sqrt((px - x1) * (px - x1) + (py - y1) * (py - y1));
        }
        
        // Project point onto line segment
        double t = Math.max(0, Math.min(1, ((px - x1) * dx + (py - y1) * dy) / lengthSq));
        double projX = x1 + t * dx;
        double projY = y1 + t * dy;
        
        return Math.sqrt((px - projX) * (px - projX) + (py - projY) * (py - projY));
    }
    
    /**
     * Render the route info popup
     */
    private void renderRouteInfoPopup(Graphics2D g2d) {
        if (selectedPath == null || popupLocation == null) return;
        
        // Determine path type/color
        int maxWidenessIdx = 0;
        int minTurnsIdx = 0;
        double maxWideness = -1;
        int minTurns = Integer.MAX_VALUE;
        
        for (int i = 0; i < paretoPaths.size(); i++) {
            RouteOverlayRenderer.ParetoPathInfo path = paretoPaths.get(i);
            if (path.wideRoadPercentage > maxWideness) {
                maxWideness = path.wideRoadPercentage;
                maxWidenessIdx = i;
            }
            if (path.rightTurns < minTurns) {
                minTurns = path.rightTurns;
                minTurnsIdx = i;
            }
        }
        
        // Determine the color and label for this path
        Color pathColor;
        String pathLabel;
        int pathIdx = paretoPaths.indexOf(selectedPath);
        
        if (pathIdx == maxWidenessIdx) {
            pathColor = RouteOverlayRenderer.PARETO_MAX_WIDENESS;
            pathLabel = "🟢 Max Wideness Path";
        } else if (pathIdx == minTurnsIdx) {
            pathColor = RouteOverlayRenderer.PARETO_MIN_TURNS;
            pathLabel = "🟠 Min Turns Path";
        } else {
            pathColor = RouteOverlayRenderer.PARETO_OTHER;
            pathLabel = "🟣 Pareto Path #" + (pathIdx + 1);
        }
        
        // Popup dimensions
        int popupWidth = 200;
        int popupHeight = 95;
        int padding = 12;
        
        // Position popup - adjust if near edges
        int popupX = popupLocation.x + 15;
        int popupY = popupLocation.y - popupHeight / 2;
        
        // Keep popup within bounds
        if (popupX + popupWidth > getWidth() - 10) {
            popupX = popupLocation.x - popupWidth - 15;
        }
        if (popupY < 60) {
            popupY = 60;
        }
        if (popupY + popupHeight > getHeight() - 10) {
            popupY = getHeight() - popupHeight - 10;
        }
        
        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.fill(new RoundRectangle2D.Double(popupX + 3, popupY + 3, popupWidth, popupHeight, 12, 12));
        
        // Draw popup background
        g2d.setColor(new Color(255, 255, 255, 245));
        g2d.fill(new RoundRectangle2D.Double(popupX, popupY, popupWidth, popupHeight, 12, 12));
        
        // Draw colored border on left
        g2d.setColor(pathColor);
        g2d.fill(new RoundRectangle2D.Double(popupX, popupY, 5, popupHeight, 4, 4));
        
        // Draw border
        g2d.setColor(new Color(pathColor.getRed(), pathColor.getGreen(), pathColor.getBlue(), 150));
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(new RoundRectangle2D.Double(popupX, popupY, popupWidth, popupHeight, 12, 12));
        
        // Draw title
        g2d.setColor(pathColor.darker());
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2d.drawString(pathLabel, popupX + padding + 4, popupY + 22);
        
        // Draw separator line
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawLine(popupX + padding, popupY + 32, popupX + popupWidth - padding, popupY + 32);
        
        // Draw wide road percentage
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2d.setColor(TEXT_PRIMARY);
        g2d.drawString("Wide Road:", popupX + padding + 4, popupY + 52);
        
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2d.setColor(RouteOverlayRenderer.PARETO_MAX_WIDENESS);
        String wideRoadText = String.format("%.2f%%", selectedPath.wideRoadPercentage);
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(wideRoadText, popupX + popupWidth - padding - fm.stringWidth(wideRoadText), popupY + 52);
        
        // Draw right turns
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2d.setColor(TEXT_PRIMARY);
        g2d.drawString("Right Turns:", popupX + padding + 4, popupY + 75);
        
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2d.setColor(RouteOverlayRenderer.PARETO_MIN_TURNS);
        String turnsText = String.valueOf(selectedPath.rightTurns);
        fm = g2d.getFontMetrics();
        g2d.drawString(turnsText, popupX + popupWidth - padding - fm.stringWidth(turnsText), popupY + 75);
        
        // Draw small close hint
        g2d.setFont(new Font("Segoe UI", Font.ITALIC, 9));
        g2d.setColor(TEXT_SECONDARY);
        g2d.drawString("Click elsewhere to close", popupX + padding + 4, popupY + popupHeight - 6);
    }
    
    /**
     * Render selected source and destination node markers
     */
    private void renderSelectedNodes(Graphics2D g2d) {
        // Define colors for markers
        Color sourceColor = new Color(16, 185, 129);  // Green
        Color destColor = new Color(255, 107, 107);   // Coral/Red
        
        // Render selected source marker
        if (selectedSourceCoord != null) {
            Point2D.Double pos = converter.latLonToPixel(selectedSourceCoord[0], selectedSourceCoord[1]);
            renderNodeMarker(g2d, (int) pos.x, (int) pos.y, sourceColor, "S", selectedSourceNodeId);
        }
        
        // Render selected destination marker
        if (selectedDestCoord != null) {
            Point2D.Double pos = converter.latLonToPixel(selectedDestCoord[0], selectedDestCoord[1]);
            renderNodeMarker(g2d, (int) pos.x, (int) pos.y, destColor, "D", selectedDestNodeId);
        }
    }
    
    /**
     * Render a single node marker with pin style
     */
    private void renderNodeMarker(Graphics2D g2d, int x, int y, Color color, String label, Integer nodeId) {
        int markerSize = 32;
        int pinHeight = 12;
        
        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fill(new Ellipse2D.Double(x - markerSize/2 + 3, y - markerSize - pinHeight + 3, markerSize, markerSize));
        
        // Draw pin point (triangle)
        int[] pinXPoints = { x - 6, x + 6, x };
        int[] pinYPoints = { y - markerSize/2 - pinHeight + 6, y - markerSize/2 - pinHeight + 6, y };
        g2d.setColor(color.darker());
        g2d.fillPolygon(pinXPoints, pinYPoints, 3);
        
        // Draw main circle
        g2d.setColor(color);
        g2d.fill(new Ellipse2D.Double(x - markerSize/2, y - markerSize - pinHeight, markerSize, markerSize));
        
        // Draw border
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.draw(new Ellipse2D.Double(x - markerSize/2, y - markerSize - pinHeight, markerSize, markerSize));
        
        // Draw inner highlight
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.fill(new Ellipse2D.Double(x - markerSize/2 + 4, y - markerSize - pinHeight + 4, markerSize/2 - 4, markerSize/2 - 4));
        
        // Draw label
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        int labelX = x - fm.stringWidth(label) / 2;
        int labelY = y - markerSize/2 - pinHeight + fm.getAscent()/2 + 4;
        g2d.drawString(label, labelX, labelY);
        
        // Draw node ID tooltip below marker
        if (nodeId != null) {
            String idText = "Node: " + nodeId;
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
            fm = g2d.getFontMetrics();
            int tooltipWidth = fm.stringWidth(idText) + 10;
            int tooltipHeight = 18;
            int tooltipX = x - tooltipWidth/2;
            int tooltipY = y + 5;
            
            // Tooltip background
            g2d.setColor(new Color(50, 50, 50, 220));
            g2d.fillRoundRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 6, 6);
            
            // Tooltip text
            g2d.setColor(Color.WHITE);
            g2d.drawString(idText, tooltipX + 5, tooltipY + 13);
        }
    }
    
    /**
     * Render selection mode overlay showing hover effect and instructions
     */
    private void renderSelectionModeOverlay(Graphics2D g2d) {
        Color modeColor = selectionMode == NodeSelectionMode.SELECT_SOURCE ? 
            new Color(16, 185, 129) : new Color(255, 107, 107);
        String modeText = selectionMode == NodeSelectionMode.SELECT_SOURCE ? 
            "📍 Click to select SOURCE node" : "🎯 Click to select DESTINATION node";
        
        // Draw instruction banner at top
        int bannerHeight = 36;
        g2d.setColor(new Color(modeColor.getRed(), modeColor.getGreen(), modeColor.getBlue(), 230));
        g2d.fillRect(0, 48, getWidth(), bannerHeight);
        
        // Banner text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(modeText, (getWidth() - fm.stringWidth(modeText)) / 2, 48 + 24);
        
        // Draw hover indicator showing nearest node
        if (nearestNodeCoordUnderMouse != null && nearestNodeIdUnderMouse != -1) {
            Point2D.Double nodePos = converter.latLonToPixel(nearestNodeCoordUnderMouse[0], nearestNodeCoordUnderMouse[1]);
            int nx = (int) nodePos.x;
            int ny = (int) nodePos.y;
            
            // Pulsing effect
            float pulse = (float) (1.0 + 0.3 * Math.sin(System.currentTimeMillis() / 150.0));
            int pulseSize = (int) (20 * pulse);
            
            // Draw outer pulsing ring
            g2d.setColor(new Color(modeColor.getRed(), modeColor.getGreen(), modeColor.getBlue(), 80));
            g2d.setStroke(new BasicStroke(3));
            g2d.draw(new Ellipse2D.Double(nx - pulseSize, ny - pulseSize, pulseSize * 2, pulseSize * 2));
            
            // Draw crosshair at node position
            g2d.setColor(modeColor);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(nx - 15, ny, nx - 5, ny);
            g2d.drawLine(nx + 5, ny, nx + 15, ny);
            g2d.drawLine(nx, ny - 15, nx, ny - 5);
            g2d.drawLine(nx, ny + 5, nx, ny + 15);
            
            // Draw center dot
            g2d.setColor(modeColor);
            g2d.fill(new Ellipse2D.Double(nx - 4, ny - 4, 8, 8));
            g2d.setColor(Color.WHITE);
            g2d.fill(new Ellipse2D.Double(nx - 2, ny - 2, 4, 4));
            
            // Draw node ID hover tooltip
            String hoverText = "Node " + nearestNodeIdUnderMouse;
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            fm = g2d.getFontMetrics();
            int tooltipWidth = fm.stringWidth(hoverText) + 12;
            int tooltipHeight = 22;
            int tooltipX = nx - tooltipWidth / 2;
            int tooltipY = ny + 25;
            
            // Keep tooltip on screen
            if (tooltipX < 5) tooltipX = 5;
            if (tooltipX + tooltipWidth > getWidth() - 5) tooltipX = getWidth() - tooltipWidth - 5;
            
            // Tooltip background
            g2d.setColor(new Color(modeColor.getRed(), modeColor.getGreen(), modeColor.getBlue(), 240));
            g2d.fillRoundRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 8, 8);
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRoundRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 8, 8);
            
            // Tooltip text
            g2d.drawString(hoverText, tooltipX + 6, tooltipY + 16);
        }
        
        // Trigger repaint for animation
        if (selectionMode != NodeSelectionMode.NONE) {
            SwingUtilities.invokeLater(() -> {
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                repaint();
            });
        }
    }
    
    private void renderTiles(Graphics2D g2d) {
        int zoom = converter.getZoomLevel();
        int[] tileRange = converter.getVisibleTileRange();
        
        int minTileX = Math.max(0, tileRange[0]);
        int maxTileX = Math.min((1 << zoom) - 1, tileRange[1]);
        int minTileY = Math.max(0, tileRange[2]);
        int maxTileY = Math.min((1 << zoom) - 1, tileRange[3]);
        
        for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
            for (int tileY = minTileY; tileY <= maxTileY; tileY++) {
                BufferedImage tile = tileProvider.getTile(tileX, tileY, zoom);
                
                // Calculate screen position for this tile
                double tileLon = TileProvider.tileXToLon(tileX, zoom);
                double tileLat = TileProvider.tileYToLat(tileY, zoom);
                Point2D.Double screenPos = converter.latLonToPixel(tileLat, tileLon);
                
                if (tile != null) {
                    g2d.drawImage(tile, (int) screenPos.x, (int) screenPos.y, 
                                  TileProvider.TILE_SIZE, TileProvider.TILE_SIZE, null);
                } else {
                    // Draw placeholder
                    g2d.setColor(new Color(240, 240, 240));
                    g2d.fillRect((int) screenPos.x, (int) screenPos.y, 
                                 TileProvider.TILE_SIZE, TileProvider.TILE_SIZE);
                    g2d.setColor(new Color(200, 200, 200));
                    g2d.drawRect((int) screenPos.x, (int) screenPos.y, 
                                 TileProvider.TILE_SIZE, TileProvider.TILE_SIZE);
                    
                    // Loading indicator
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    g2d.drawString("Loading...", (int) screenPos.x + 5, (int) screenPos.y + 15);
                }
            }
        }
    }
    
    private void renderGrid(Graphics2D g2d) {
        g2d.setColor(new Color(100, 100, 100, 50));
        g2d.setStroke(new BasicStroke(1));
        
        // Lat/lon grid lines
        double[] topLeft = converter.pixelToLatLon(0, 0);
        double[] bottomRight = converter.pixelToLatLon(getWidth(), getHeight());
        
        double latStep = getGridStep(bottomRight[0] - topLeft[0]);
        double lonStep = getGridStep(bottomRight[1] - topLeft[1]);
        
        // Longitude lines (vertical)
        double lon = Math.floor(topLeft[1] / lonStep) * lonStep;
        while (lon < bottomRight[1]) {
            Point2D.Double p = converter.latLonToPixel(topLeft[0], lon);
            g2d.drawLine((int) p.x, 0, (int) p.x, getHeight());
            lon += lonStep;
        }
        
        // Latitude lines (horizontal)
        double lat = Math.floor(bottomRight[0] / latStep) * latStep;
        while (lat < topLeft[0]) {
            Point2D.Double p = converter.latLonToPixel(lat, topLeft[1]);
            g2d.drawLine(0, (int) p.y, getWidth(), (int) p.y);
            lat += latStep;
        }
    }
    
    private double getGridStep(double range) {
        double[] steps = {0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1, 5, 10};
        for (double step : steps) {
            if (range / step < 20) {
                return step;
            }
        }
        return 10;
    }
    
    private void renderSubgraph(Graphics2D g2d) {
        // Draw subgraph edges
        g2d.setColor(new Color(150, 180, 210, 80));
        g2d.setStroke(new BasicStroke(1.0f));
        
        for (int[] edge : subgraphEdges) {
            if (edge[0] < subgraphNodes.size() && edge[1] < subgraphNodes.size()) {
                double[] c1 = subgraphNodes.get(edge[0]);
                double[] c2 = subgraphNodes.get(edge[1]);
                Point2D.Double p1 = converter.latLonToPixel(c1[0], c1[1]);
                Point2D.Double p2 = converter.latLonToPixel(c2[0], c2[1]);
                g2d.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
            }
        }
        
        // Draw subgraph nodes
        g2d.setColor(new Color(100, 140, 200, 100));
        for (double[] coord : subgraphNodes) {
            Point2D.Double p = converter.latLonToPixel(coord[0], coord[1]);
            g2d.fillOval((int) p.x - 2, (int) p.y - 2, 4, 4);
        }
    }
    
    private void renderEmptyState(Graphics2D g2d) {
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fill(new RoundRectangle2D.Double(cx - 150, cy - 60, 300, 120, 20, 20));
        
        g2d.setColor(ACCENT_COLOR);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 24));
        FontMetrics fm = g2d.getFontMetrics();
        String title = "FlexiRoute Navigator";
        g2d.drawString(title, cx - fm.stringWidth(title) / 2, cy - 15);
        
        g2d.setColor(TEXT_SECONDARY);
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fm = g2d.getFontMetrics();
        String hint = "Enter source and destination to find routes";
        g2d.drawString(hint, cx - fm.stringWidth(hint) / 2, cy + 20);
    }
    
    private void renderInfoOverlay(Graphics2D g2d) {
        // Info box at top-left
        g2d.setColor(OVERLAY_BG);
        g2d.fill(new RoundRectangle2D.Double(10, 50, 170, 80, 10, 10));
        g2d.setColor(new Color(200, 200, 200));
        g2d.draw(new RoundRectangle2D.Double(10, 50, 170, 80, 10, 10));
        
        g2d.setColor(TEXT_PRIMARY);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2d.drawString("Map Info", 20, 70);
        
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2d.setColor(TEXT_SECONDARY);
        g2d.drawString("Zoom: " + converter.getZoomLevel(), 20, 90);
        g2d.drawString(String.format("Center: %.4f, %.4f", 
            converter.getCenterLat(), converter.getCenterLon()), 20, 106);
        
        if (!pathCoordinates.isEmpty()) {
            g2d.drawString("Path nodes: " + pathCoordinates.size(), 20, 122);
        }
    }
    
    private void renderProgressOverlay(Graphics2D g2d) {
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        
        // Background
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Dialog
        g2d.setColor(Color.WHITE);
        g2d.fill(new RoundRectangle2D.Double(cx - 150, cy - 50, 300, 100, 15, 15));
        
        g2d.setColor(ACCENT_COLOR);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g2d.drawString("Finding Route...", cx - 60, cy - 20);
        
        // Progress bar
        g2d.setColor(new Color(230, 230, 230));
        g2d.fill(new RoundRectangle2D.Double(cx - 120, cy, 240, 20, 10, 10));
        g2d.setColor(ACCENT_COLOR);
        g2d.fill(new RoundRectangle2D.Double(cx - 120, cy, 240 * progressValue / 100.0, 20, 10, 10));
        
        g2d.setColor(TEXT_SECONDARY);
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2d.drawString(progressMessage, cx - 100, cy + 40);
    }
    
    private void exportImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("flexiroute_map.png"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                paint(img.getGraphics());
                javax.imageio.ImageIO.write(img, "PNG", chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Map exported successfully!", "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(), "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // === PUBLIC API ===
    
    /**
     * Set the node selection listener
     */
    public void setNodeSelectionListener(NodeSelectionListener listener) {
        this.nodeSelectionListener = listener;
    }
    
    /**
     * Set the nearest node finder (required for node selection to work)
     */
    public void setNearestNodeFinder(NearestNodeFinder finder) {
        this.nearestNodeFinder = finder;
    }
    
    /**
     * Get current node selection mode
     */
    public NodeSelectionMode getSelectionMode() {
        return selectionMode;
    }
    
    /**
     * Set node selection mode programmatically
     */
    public void setSelectionMode(NodeSelectionMode mode) {
        this.selectionMode = mode;
        if (selectSourceBtn != null && selectDestBtn != null) {
            selectSourceBtn.setSelected(mode == NodeSelectionMode.SELECT_SOURCE);
            selectDestBtn.setSelected(mode == NodeSelectionMode.SELECT_DEST);
        }
        if (mode == NodeSelectionMode.NONE) {
            setCursor(Cursor.getDefaultCursor());
            hoverPoint = null;
        } else {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }
        repaint();
    }
    
    /**
     * Get selected source node ID
     */
    public Integer getSelectedSourceNodeId() {
        return selectedSourceNodeId;
    }
    
    /**
     * Get selected destination node ID
     */
    public Integer getSelectedDestNodeId() {
        return selectedDestNodeId;
    }
    
    /**
     * Set selected source node (from external input like QueryPanel)
     * @param nodeId The node ID
     * @param lat Latitude of the node
     * @param lon Longitude of the node
     */
    public void setSelectedSource(int nodeId, double lat, double lon) {
        this.selectedSourceNodeId = nodeId;
        this.selectedSourceCoord = new double[] { lat, lon };
        repaint();
    }
    
    /**
     * Set selected destination node (from external input like QueryPanel)
     * @param nodeId The node ID
     * @param lat Latitude of the node
     * @param lon Longitude of the node
     */
    public void setSelectedDestination(int nodeId, double lat, double lon) {
        this.selectedDestNodeId = nodeId;
        this.selectedDestCoord = new double[] { lat, lon };
        repaint();
    }
    
    /**
     * Clear all node selections
     */
    public void clearNodeSelections() {
        this.selectedSourceNodeId = null;
        this.selectedDestNodeId = null;
        this.selectedSourceCoord = null;
        this.selectedDestCoord = null;
        this.selectionMode = NodeSelectionMode.NONE;
        if (selectSourceBtn != null) selectSourceBtn.setSelected(false);
        if (selectDestBtn != null) selectDestBtn.setSelected(false);
        setCursor(Cursor.getDefaultCursor());
        hoverPoint = null;
        repaint();
    }
    
    /**
     * Set the route path to display
     */
    public void setPath(List<Integer> nodes, List<Integer> wideEdgeIndices, List<double[]> coordinates) {
        this.pathCoordinates = coordinates != null ? new ArrayList<>(coordinates) : new ArrayList<>();
        this.wideEdges = wideEdgeIndices != null ? new ArrayList<>(wideEdgeIndices) : new ArrayList<>();
        
        // Clear Pareto paths when single path is set
        this.paretoPaths.clear();
        this.showParetoPaths = false;
        
        // Clear preview when path is set
        this.previewSourceCoord = null;
        this.previewDestCoord = null;
        
        // Clear route info popup
        this.showRouteInfoPopup = false;
        this.selectedPath = null;
        
        // Auto-fit view to path
        if (!this.pathCoordinates.isEmpty()) {
            fitToPath();
        }
        
        repaint();
    }
    
    /**
     * Set multiple Pareto optimal paths for hybrid mode visualization
     * Paths are color-coded: green for max wideness, orange for min turns, purple for others
     */
    public void setParetoPaths(List<RouteOverlayRenderer.ParetoPathInfo> paths) {
        this.paretoPaths = paths != null ? new ArrayList<>(paths) : new ArrayList<>();
        this.showParetoPaths = !this.paretoPaths.isEmpty();
        
        // Clear single path when Pareto paths are set
        if (this.showParetoPaths) {
            this.pathCoordinates.clear();
            this.wideEdges.clear();
        }
        
        // Clear preview
        this.previewSourceCoord = null;
        this.previewDestCoord = null;
        
        // Clear route info popup
        this.showRouteInfoPopup = false;
        this.selectedPath = null;
        
        // Auto-fit view to paths
        if (!this.paretoPaths.isEmpty()) {
            fitToParetoPaths();
        }
        
        repaint();
    }
    
    /**
     * Fit view to show all Pareto paths
     */
    private void fitToParetoPaths() {
        if (paretoPaths.isEmpty()) return;
        
        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;
        
        for (RouteOverlayRenderer.ParetoPathInfo path : paretoPaths) {
            for (double[] coord : path.coordinates) {
                minLat = Math.min(minLat, coord[0]);
                maxLat = Math.max(maxLat, coord[0]);
                minLon = Math.min(minLon, coord[1]);
                maxLon = Math.max(maxLon, coord[1]);
            }
        }
        
        // Add padding
        double latPad = (maxLat - minLat) * 0.15 + 0.001;
        double lonPad = (maxLon - minLon) * 0.15 + 0.001;
        minLat -= latPad;
        maxLat += latPad;
        minLon -= lonPad;
        maxLon += lonPad;
        
        // Center on bounds
        double centerLat = (minLat + maxLat) / 2;
        double centerLon = (minLon + maxLon) / 2;
        converter.setCenter(centerLat, centerLon);
        converter.resetPan();
        
        // Calculate zoom level to fit all paths
        int zoom = CoordinateConverter.calculateZoomToFit(minLat, maxLat, minLon, maxLon, getWidth(), getHeight());
        converter.setZoomLevel(Math.max(1, zoom - 1)); // Slightly zoomed out for context
    }
    
    /**
     * Set path with subgraph context
     */
    public void setPathWithContext(List<Integer> nodes, List<Integer> wideEdgeIndices, 
                                   List<double[]> coordinates, List<double[]> contextCoords, 
                                   List<int[]> contextEdges, List<int[]> pathToContext) {
        setPath(nodes, wideEdgeIndices, coordinates);
    }
    
    /**
     * Set path with subgraph for visualization
     */
    public void setPathWithContextAndSubgraph(List<Integer> nodes, List<Integer> wideEdgeIndices,
                                              List<double[]> coordinates, List<double[]> contextCoords,
                                              List<int[]> contextEdges, List<int[]> pathToContext,
                                              List<double[]> subgraphCoords, List<int[]> subgraphEdgeList) {
        this.subgraphNodes = subgraphCoords != null ? new ArrayList<>(subgraphCoords) : new ArrayList<>();
        this.subgraphEdges = subgraphEdgeList != null ? new ArrayList<>(subgraphEdgeList) : new ArrayList<>();
        setPath(nodes, wideEdgeIndices, coordinates);
    }
    
    /**
     * Set query preview markers
     */
    public void setQueryPreview(int source, int dest, double[] sourceCoord, double[] destCoord) {
        this.previewSourceCoord = sourceCoord;
        this.previewDestCoord = destCoord;
        
        // Center view on preview
        if (sourceCoord != null && destCoord != null) {
            double centerLat = (sourceCoord[0] + destCoord[0]) / 2;
            double centerLon = (sourceCoord[1] + destCoord[1]) / 2;
            converter.setCenter(centerLat, centerLon);
            converter.resetPan();
            
            // Calculate appropriate zoom
            int zoom = CoordinateConverter.calculateZoomToFit(
                Math.min(sourceCoord[0], destCoord[0]),
                Math.max(sourceCoord[0], destCoord[0]),
                Math.min(sourceCoord[1], destCoord[1]),
                Math.max(sourceCoord[1], destCoord[1]),
                getWidth(), getHeight()
            );
            converter.setZoomLevel(Math.max(10, zoom - 1)); // Slightly zoomed out
        }
        
        repaint();
    }
    
    /**
     * Set query preview with subgraph
     */
    public void setQueryPreviewWithSubgraph(int source, int dest, double[] sourceCoord, double[] destCoord,
                                            List<double[]> subgraphCoords, List<int[]> subgraphEdgeList) {
        this.subgraphNodes = subgraphCoords != null ? new ArrayList<>(subgraphCoords) : new ArrayList<>();
        this.subgraphEdges = subgraphEdgeList != null ? new ArrayList<>(subgraphEdgeList) : new ArrayList<>();
        setQueryPreview(source, dest, sourceCoord, destCoord);
    }
    
    /**
     * Set subgraph preview
     */
    public void setSubgraphPreview(List<double[]> nodeCoordinates, List<int[]> edges) {
        this.subgraphNodes = nodeCoordinates != null ? new ArrayList<>(nodeCoordinates) : new ArrayList<>();
        this.subgraphEdges = edges != null ? new ArrayList<>(edges) : new ArrayList<>();
        repaint();
    }
    
    /**
     * Clear query preview
     */
    public void clearQueryPreview() {
        this.previewSourceCoord = null;
        this.previewDestCoord = null;
        this.subgraphNodes.clear();
        this.subgraphEdges.clear();
        repaint();
    }
    
    /**
     * Clear the map
     */
    public void clearMap() {
        this.pathCoordinates.clear();
        this.wideEdges.clear();
        this.paretoPaths.clear();
        this.showParetoPaths = false;
        this.subgraphNodes.clear();
        this.subgraphEdges.clear();
        
        // Clear route info popup
        this.showRouteInfoPopup = false;
        this.selectedPath = null;
        
        clearQueryPreview();
        showProgress = false;
        repaint();
    }
    
    /**
     * Set search progress
     */
    public void setSearchProgress(int progress, String message) {
        this.progressValue = progress;
        this.progressMessage = message;
        this.showProgress = progress < 100;
        repaint();
    }
    
    /**
     * Clear search frontier/progress
     */
    public void clearSearchFrontier() {
        this.showProgress = false;
        repaint();
    }
    
    /**
     * Fit view to show entire path
     */
    public void fitToPath() {
        if (pathCoordinates.isEmpty()) return;
        
        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;
        
        for (double[] coord : pathCoordinates) {
            minLat = Math.min(minLat, coord[0]);
            maxLat = Math.max(maxLat, coord[0]);
            minLon = Math.min(minLon, coord[1]);
            maxLon = Math.max(maxLon, coord[1]);
        }
        
        // Include subgraph in bounds
        for (double[] coord : subgraphNodes) {
            minLat = Math.min(minLat, coord[0]);
            maxLat = Math.max(maxLat, coord[0]);
            minLon = Math.min(minLon, coord[1]);
            maxLon = Math.max(maxLon, coord[1]);
        }
        
        // Set center
        converter.setCenter((minLat + maxLat) / 2, (minLon + maxLon) / 2);
        converter.resetPan();
        
        // Calculate zoom to fit
        int zoom = CoordinateConverter.calculateZoomToFit(minLat, maxLat, minLon, maxLon, 
                                                          getWidth(), getHeight());
        converter.setZoomLevel(zoom);
        
        repaint();
    }
    
    /**
     * Reset view to default
     */
    public void resetView() {
        if (!pathCoordinates.isEmpty()) {
            fitToPath();
        } else if (!subgraphNodes.isEmpty()) {
            fitToSubgraph();
        } else {
            // Default to London (where the default dataset is located)
            converter.setCenter(51.5074, -0.1278);
            converter.setZoomLevel(12);
            converter.resetPan();
        }
        repaint();
    }
    
    /**
     * Fit view to show subgraph bounds
     */
    public void fitToSubgraph() {
        if (subgraphNodes.isEmpty()) return;
        
        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;
        
        for (double[] coord : subgraphNodes) {
            minLat = Math.min(minLat, coord[0]);
            maxLat = Math.max(maxLat, coord[0]);
            minLon = Math.min(minLon, coord[1]);
            maxLon = Math.max(maxLon, coord[1]);
        }
        
        converter.setCenter((minLat + maxLat) / 2, (minLon + maxLon) / 2);
        converter.resetPan();
        
        int zoom = CoordinateConverter.calculateZoomToFit(minLat, maxLat, minLon, maxLon, 
                                                          getWidth(), getHeight());
        converter.setZoomLevel(zoom);
        repaint();
    }
    
    /**
     * Center map on specific coordinates with given zoom
     */
    public void centerOn(double lat, double lon, int zoom) {
        converter.setCenter(lat, lon);
        converter.setZoomLevel(zoom);
        converter.resetPan();
        repaint();
    }
    
    /**
     * Set map center
     */
    public void setCenter(double lat, double lon) {
        converter.setCenter(lat, lon);
        repaint();
    }
    
    /**
     * Set zoom level
     */
    public void setZoomLevel(int zoom) {
        converter.setZoomLevel(zoom);
        repaint();
    }
    
    /**
     * Zoom in or out by the given amount
     * @param amount positive to zoom in, negative to zoom out
     */
    public void zoom(int amount) {
        int currentZoom = converter.getZoomLevel();
        int newZoom = Math.max(1, Math.min(19, currentZoom + amount));
        converter.setZoomLevel(newZoom);
        repaint();
    }
    
    /**
     * Set the tile server for OSM tiles
     */
    public void setTileServer(TileProvider.TileServer server) {
        tileProvider.setTileServer(server);
        tileProvider.clearCache();
        repaint();
    }
    
    /**
     * Get current render mode
     */
    public RenderMode getRenderMode() {
        return currentMode;
    }
    
    /**
     * Set render mode
     */
    public void setRenderMode(RenderMode mode) {
        this.currentMode = mode;
        tileProvider.setTileServer(mode.getServer());
        tileProvider.clearCache();
        repaint();
    }
    
    // TileLoadListener implementation
    
    @Override
    public void onTileLoaded(int x, int y, int z, BufferedImage tile) {
        // Repaint when a tile loads
        SwingUtilities.invokeLater(this::repaint);
    }
    
    @Override
    public void onTileError(int x, int y, int z, Exception e) {
        System.err.println("Failed to load tile " + z + "/" + x + "/" + y + ": " + e.getMessage());
    }
    
    /**
     * Shutdown tile provider
     */
    public void shutdown() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        tileProvider.shutdown();
    }
}
