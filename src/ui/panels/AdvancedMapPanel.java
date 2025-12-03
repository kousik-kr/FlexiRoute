package ui.panels;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.Timer;

import ui.components.ModernButton;

/**
 * Advanced Map Panel with multiple visualization modes and pagination
 */
public class AdvancedMapPanel extends JPanel {
    
    // Visualization Modes
    public enum VisualizationMode {
        CLASSIC("Classic", "Traditional node and edge rendering"),
        NEON_GLOW("Neon Glow", "Futuristic glowing effects"),
        GRADIENT_FLOW("Gradient Flow", "Smooth color transitions"),
        THREE_D_EFFECT("3D Effect", "Pseudo-3D depth perception"),
        PULSE_ANIMATION("Pulse Animation", "Animated path traversal");
        
        private final String displayName;
        private final String description;
        
        VisualizationMode(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    // Rendering state
    private List<Integer> pathNodes = Collections.emptyList();
    private List<Integer> wideEdges = Collections.emptyList();
    private VisualizationMode currentMode = VisualizationMode.CLASSIC;
    private boolean showPathOnly = false;
    
    // Graph data for full/partial visualization
    private boolean showFullGraph = false;
    private static final int MAX_GRAPH_SIZE = 6000; // Maximum nodes to visualize
    private List<Integer> graphNodesToShow = new ArrayList<>();
    
    // Query preview fields
    private Integer querySourceNode = null;
    private Integer queryDestNode = null;
    private boolean showQueryPreview = false;
    
    // Pagination
    private int nodesPerPage = 50;
    private int currentPage = 0;
    private int totalPages = 1;
    
    // Animation
    private Timer animationTimer;
    private int animationFrame = 0;
    private double animationProgress = 0.0;
    
    // UI Controls
    private JComboBox<VisualizationMode> modeSelector;
    private JSlider pageSlider;
    private JLabel pageLabel;
    private JCheckBox pathOnlyCheckbox;
    private ModernButton prevButton;
    private ModernButton nextButton;
    private ModernButton showAllButton;
    
    public AdvancedMapPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);
        
        createControls();
        createMapCanvas();
        
        // Animation timer for pulse mode
        animationTimer = new Timer(100, e -> {
            if (currentMode == VisualizationMode.PULSE_ANIMATION && !pathNodes.isEmpty()) {
                animationFrame++;
                animationProgress = (animationProgress + 0.05) % 1.0;
                repaint();
            }
        });
    }
    
    private void createControls() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout(10, 10));
        controlPanel.setBackground(new Color(245, 245, 245));
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Top row: Mode selection and options
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topRow.setOpaque(false);
        
        JLabel modeLabel = new JLabel("Visualization Mode:");
        modeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        topRow.add(modeLabel);
        
        modeSelector = new JComboBox<>(VisualizationMode.values());
        modeSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof VisualizationMode) {
                    setText(((VisualizationMode) value).getDisplayName());
                }
                return this;
            }
        });
        modeSelector.addActionListener(e -> {
            currentMode = (VisualizationMode) modeSelector.getSelectedItem();
            if (currentMode == VisualizationMode.PULSE_ANIMATION) {
                animationTimer.start();
            } else {
                animationTimer.stop();
            }
            repaint();
        });
        topRow.add(modeSelector);
        
        topRow.add(Box.createHorizontalStrut(20));
        
        pathOnlyCheckbox = new JCheckBox("Show Path Only");
        pathOnlyCheckbox.setOpaque(false);
        pathOnlyCheckbox.addActionListener(e -> {
            showPathOnly = pathOnlyCheckbox.isSelected();
            repaint();
        });
        topRow.add(pathOnlyCheckbox);
        
        topRow.add(Box.createHorizontalStrut(20));
        
        // Graph visualization controls
        ModernButton showGraphButton = new ModernButton("📍 Show Graph Sample", new Color(156, 39, 176));
        showGraphButton.setToolTipText("Display a sample of the loaded graph");
        showGraphButton.addActionListener(e -> showGraphVisualization());
        topRow.add(showGraphButton);
        
        ModernButton clearGraphButton = new ModernButton("🗑 Clear Graph", new Color(158, 158, 158));
        clearGraphButton.setToolTipText("Clear graph visualization");
        clearGraphButton.addActionListener(e -> clearGraphVisualization());
        topRow.add(clearGraphButton);
        
        // Bottom row: Pagination controls
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bottomRow.setOpaque(false);
        
        prevButton = new ModernButton("◀ Previous", new Color(33, 150, 243));
        prevButton.addActionListener(e -> previousPage());
        bottomRow.add(prevButton);
        
        pageLabel = new JLabel("Page 1 of 1 (50 nodes/page)");
        pageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bottomRow.add(pageLabel);
        
        nextButton = new ModernButton("Next ▶", new Color(33, 150, 243));
        nextButton.addActionListener(e -> nextPage());
        bottomRow.add(nextButton);
        
        bottomRow.add(Box.createHorizontalStrut(20));
        
        showAllButton = new ModernButton("Show All", new Color(76, 175, 80));
        showAllButton.addActionListener(e -> showAllPages());
        bottomRow.add(showAllButton);
        
        bottomRow.add(Box.createHorizontalStrut(20));
        
        JLabel sliderLabel = new JLabel("Nodes/Page:");
        sliderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        bottomRow.add(sliderLabel);
        
        pageSlider = new JSlider(10, 500, nodesPerPage);
        pageSlider.setPreferredSize(new Dimension(150, 30));
        pageSlider.setMajorTickSpacing(100);
        pageSlider.setPaintTicks(true);
        pageSlider.addChangeListener(e -> {
            if (!pageSlider.getValueIsAdjusting()) {
                nodesPerPage = pageSlider.getValue();
                updatePagination();
            }
        });
        bottomRow.add(pageSlider);
        
        JLabel sliderValue = new JLabel(String.valueOf(nodesPerPage));
        pageSlider.addChangeListener(e -> sliderValue.setText(String.valueOf(pageSlider.getValue())));
        bottomRow.add(sliderValue);
        
        controlPanel.add(topRow, BorderLayout.NORTH);
        controlPanel.add(bottomRow, BorderLayout.SOUTH);
        
        add(controlPanel, BorderLayout.NORTH);
    }
    
    private void createMapCanvas() {
        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                
                // Enable antialiasing
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                renderVisualization(g2d);
            }
        };
        canvas.setBackground(Color.WHITE);
        canvas.setPreferredSize(new Dimension(800, 600));
        
        JScrollPane scrollPane = new JScrollPane(canvas);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void renderVisualization(Graphics2D g2d) {
        // Priority: Query preview > Graph sample > Path > Empty state
        if (showQueryPreview && querySourceNode != null && queryDestNode != null) {
            renderQueryPreview(g2d);
            return;
        }
        
        // Show graph visualization if no path is set but graph nodes are available
        if (pathNodes.isEmpty() && !graphNodesToShow.isEmpty()) {
            renderGraphSample(g2d);
            return;
        }
        
        if (pathNodes.isEmpty()) {
            drawEmptyState(g2d);
            return;
        }
        
        switch (currentMode) {
            case CLASSIC -> renderClassic(g2d);
            case NEON_GLOW -> renderNeonGlow(g2d);
            case GRADIENT_FLOW -> renderGradientFlow(g2d);
            case THREE_D_EFFECT -> render3DEffect(g2d);
            case PULSE_ANIMATION -> renderPulseAnimation(g2d);
        }
    }
    
    private void drawEmptyState(Graphics2D g2d) {
        String message = "No path to display";
        String hint = "Run a query to visualize results";
        
        g2d.setColor(new Color(150, 150, 150));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 24));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(message)) / 2;
        int y = getHeight() / 2;
        g2d.drawString(message, x, y);
        
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fm = g2d.getFontMetrics();
        x = (getWidth() - fm.stringWidth(hint)) / 2;
        g2d.drawString(hint, x, y + 30);
    }
    
    private void renderClassic(Graphics2D g2d) {
        try {
            // Get all nodes using reflection
            Class<?> graphClass = Class.forName("Graph");
            java.lang.reflect.Method getNodesMethod = graphClass.getMethod("get_nodes");
            @SuppressWarnings("unchecked")
            java.util.Map<Integer, Object> allNodes = (java.util.Map<Integer, Object>) getNodesMethod.invoke(null);
            
            if (allNodes == null || pathNodes.isEmpty()) return;
            
            // Calculate bounds for normalization
            double minLat = Double.MAX_VALUE, maxLat = Double.MIN_VALUE;
            double minLon = Double.MAX_VALUE, maxLon = Double.MIN_VALUE;
            
            for (Integer nodeId : pathNodes) {
                Object node = allNodes.get(nodeId);
                if (node != null) {
                    double lat = (Double) node.getClass().getMethod("get_latitude").invoke(node);
                    double lon = (Double) node.getClass().getMethod("get_longitude").invoke(node);
                    minLat = Math.min(minLat, lat);
                    maxLat = Math.max(maxLat, lat);
                    minLon = Math.min(minLon, lon);
                    maxLon = Math.max(maxLon, lon);
                }
            }
            
            int width = getWidth();
            int height = getHeight();
            int padding = 60;
            
            // Draw directed edges with arrows
            for (int i = 0; i < pathNodes.size() - 1; i++) {
                Object node1 = allNodes.get(pathNodes.get(i));
                Object node2 = allNodes.get(pathNodes.get(i + 1));
                
                if (node1 != null && node2 != null) {
                    double lat1 = (Double) node1.getClass().getMethod("get_latitude").invoke(node1);
                    double lon1 = (Double) node1.getClass().getMethod("get_longitude").invoke(node1);
                    double lat2 = (Double) node2.getClass().getMethod("get_latitude").invoke(node2);
                    double lon2 = (Double) node2.getClass().getMethod("get_longitude").invoke(node2);
                    
                    int x1 = padding + (int)((lon1 - minLon) / Math.max(0.0001, maxLon - minLon) * (width - 2 * padding));
                    int y1 = height - padding - (int)((lat1 - minLat) / Math.max(0.0001, maxLat - minLat) * (height - 2 * padding));
                    int x2 = padding + (int)((lon2 - minLon) / Math.max(0.0001, maxLon - minLon) * (width - 2 * padding));
                    int y2 = height - padding - (int)((lat2 - minLat) / Math.max(0.0001, maxLat - minLat) * (height - 2 * padding));
                    
                    boolean isWideEdge = wideEdges != null && wideEdges.contains(i);
                    
                    // Draw edge
                    g2d.setStroke(new BasicStroke(isWideEdge ? 4 : 2));
                    g2d.setColor(isWideEdge ? new Color(255, 87, 34) : new Color(100, 100, 100));
                    g2d.drawLine(x1, y1, x2, y2);
                    
                    // Draw arrow head
                    drawArrowHead(g2d, x1, y1, x2, y2, isWideEdge ? new Color(255, 87, 34) : new Color(100, 100, 100));
                }
            }
            
            // Draw nodes on top
            for (int i = 0; i < pathNodes.size(); i++) {
                Object node = allNodes.get(pathNodes.get(i));
                if (node != null) {
                    double lat = (Double) node.getClass().getMethod("get_latitude").invoke(node);
                    double lon = (Double) node.getClass().getMethod("get_longitude").invoke(node);
                    
                    int x = padding + (int)((lon - minLon) / Math.max(0.0001, maxLon - minLon) * (width - 2 * padding));
                    int y = height - padding - (int)((lat - minLat) / Math.max(0.0001, maxLat - minLat) * (height - 2 * padding));
                    
                    boolean isStart = i == 0;
                    boolean isEnd = i == pathNodes.size() - 1;
                    
                    Color nodeColor = isStart ? new Color(76, 175, 80) : 
                                     isEnd ? new Color(244, 67, 54) : 
                                     new Color(33, 150, 243);
                    
                    g2d.setColor(nodeColor);
                    g2d.fillOval(x - 8, y - 8, 16, 16);
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawOval(x - 8, y - 8, 16, 16);
                    
                    // Node label (only for start/end)
                    if (isStart || isEnd) {
                        String label = isStart ? "S" : "E";
                        g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        FontMetrics fm = g2d.getFontMetrics();
                        int textX = x - fm.stringWidth(label) / 2;
                        int textY = y + fm.getAscent() / 2 - 1;
                        g2d.drawString(label, textX, textY);
                    }
                }
            }
        } catch (Exception e) {
            g2d.setColor(Color.RED);
            g2d.drawString("Error rendering path: " + e.getMessage(), 10, 20);
        }
    }
    
    private void drawArrowHead(Graphics2D g2d, int x1, int y1, int x2, int y2, Color color) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowSize = 10;
        
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];
        
        xPoints[0] = x2;
        yPoints[0] = y2;
        xPoints[1] = (int) (x2 - arrowSize * Math.cos(angle - Math.PI / 6));
        yPoints[1] = (int) (y2 - arrowSize * Math.sin(angle - Math.PI / 6));
        xPoints[2] = (int) (x2 - arrowSize * Math.cos(angle + Math.PI / 6));
        yPoints[2] = (int) (y2 - arrowSize * Math.sin(angle + Math.PI / 6));
        
        g2d.setColor(color);
        g2d.fillPolygon(xPoints, yPoints, 3);
    }
    
    private void renderNeonGlow(Graphics2D g2d) {
        // Render using classic mode as base (with geographic coordinates)
        renderClassic(g2d);
    }
    
    private void renderGradientFlow(Graphics2D g2d) {
        // Render using classic mode as base (with geographic coordinates)
        renderClassic(g2d);
    }
    
    private void render3DEffect(Graphics2D g2d) {
        // Render using classic mode as base (with geographic coordinates)
        renderClassic(g2d);
    }
    
    private void renderPulseAnimation(Graphics2D g2d) {
        // Render using classic mode as base (with geographic coordinates)
        renderClassic(g2d);
    }
    
    public void setPath(List<Integer> pathNodes, List<Integer> wideEdges) {
        this.pathNodes = new ArrayList<>(pathNodes);
        this.wideEdges = wideEdges != null ? new ArrayList<>(wideEdges) : Collections.emptyList();
        updatePagination();
        repaint();
    }
    
    private void updatePagination() {
        if (pathNodes.isEmpty()) {
            totalPages = 1;
            currentPage = 0;
        } else {
            totalPages = (int) Math.ceil((double) pathNodes.size() / nodesPerPage);
            currentPage = Math.min(currentPage, totalPages - 1);
        }
        
        pageLabel.setText(String.format("Page %d of %d (%d nodes/page)", 
            currentPage + 1, totalPages, nodesPerPage));
        
        prevButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(currentPage < totalPages - 1);
        
        repaint();
    }
    
    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            updatePagination();
        }
    }
    
    private void nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            updatePagination();
        }
    }
    
    private void showAllPages() {
        nodesPerPage = Math.max(pathNodes.size(), 50);
        pageSlider.setValue(nodesPerPage);
        currentPage = 0;
        updatePagination();
    }
    
    private void showGraphVisualization() {
        try {
            // Get nodes from the Graph class using reflection
            Class<?> graphClass = Class.forName("Graph");
            java.lang.reflect.Method getNodesMethod = graphClass.getMethod("get_nodes");
            @SuppressWarnings("unchecked")
            java.util.Map<Integer, Object> allNodes = (java.util.Map<Integer, Object>) getNodesMethod.invoke(null);
            
            if (allNodes == null || allNodes.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "No graph data loaded. Please load a dataset first.",
                    "Graph Not Loaded",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Check if graph is too large
            List<Integer> nodeIds = new ArrayList<>(allNodes.keySet());
            int totalNodes = nodeIds.size();
            
            System.out.println("Graph visualization: Total nodes = " + totalNodes + ", MAX_GRAPH_SIZE = " + MAX_GRAPH_SIZE);
            
            if (totalNodes > MAX_GRAPH_SIZE) {
                System.out.println("Graph too large! Showing warning dialog.");
                JOptionPane.showMessageDialog(this,
                    String.format("Graph is too large to visualize!\n\n" +
                        "Total nodes: %,d\n" +
                        "Maximum allowed: %,d\n\n" +
                        "Visualizing such a large graph would be too congested.\n" +
                        "Please use the query visualization feature instead.",
                        totalNodes, MAX_GRAPH_SIZE),
                    "Large Graph Warning",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Visualize full graph (all nodes)
            graphNodesToShow.clear();
            graphNodesToShow.addAll(nodeIds);
            Collections.sort(graphNodesToShow);
            
            JOptionPane.showMessageDialog(this,
                String.format("Visualizing full graph with %d nodes.",
                    graphNodesToShow.size()),
                "Graph Visualization",
                JOptionPane.INFORMATION_MESSAGE);
            
            repaint();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading graph visualization: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearGraphVisualization() {
        graphNodesToShow.clear();
        repaint();
    }
    
    public void setQueryPreview(int sourceNode, int destNode) {
        this.querySourceNode = sourceNode;
        this.queryDestNode = destNode;
        this.showQueryPreview = true;
        repaint();
    }
    
    public void clearQueryPreview() {
        this.querySourceNode = null;
        this.queryDestNode = null;
        this.showQueryPreview = false;
        repaint();
    }
    
    private void renderGraphSample(Graphics2D g2d) {
        try {
            // Get nodes from the Graph class using reflection
            Class<?> graphClass = Class.forName("Graph");
            java.lang.reflect.Method getNodesMethod = graphClass.getMethod("get_nodes");
            @SuppressWarnings("unchecked")
            java.util.Map<Integer, Object> allNodes = (java.util.Map<Integer, Object>) getNodesMethod.invoke(null);
            
            if (allNodes == null || graphNodesToShow.isEmpty()) {
                return;
            }
            
            // Calculate bounds for normalization using reflection to call Node methods
            double minLat = Double.MAX_VALUE, maxLat = Double.MIN_VALUE;
            double minLon = Double.MAX_VALUE, maxLon = Double.MIN_VALUE;
            
            for (Integer nodeId : graphNodesToShow) {
                Object node = allNodes.get(nodeId);
                if (node != null) {
                    double lat = (Double) node.getClass().getMethod("get_latitude").invoke(node);
                    double lon = (Double) node.getClass().getMethod("get_longitude").invoke(node);
                    minLat = Math.min(minLat, lat);
                    maxLat = Math.max(maxLat, lat);
                    minLon = Math.min(minLon, lon);
                    maxLon = Math.max(maxLon, lon);
                }
            }
            
            int width = getWidth();
            int height = getHeight();
            int padding = 40;
            
            // Draw nodes
            g2d.setColor(new Color(33, 150, 243, 180));
            for (Integer nodeId : graphNodesToShow) {
                Object node = allNodes.get(nodeId);
                if (node != null) {
                    double lat = (Double) node.getClass().getMethod("get_latitude").invoke(node);
                    double lon = (Double) node.getClass().getMethod("get_longitude").invoke(node);
                    
                    // Normalize coordinates to canvas
                    int x = padding + (int)((lon - minLon) / (maxLon - minLon) * (width - 2 * padding));
                    int y = height - padding - (int)((lat - minLat) / (maxLat - minLat) * (height - 2 * padding));
                    
                    g2d.fillOval(x - 3, y - 3, 6, 6);
                }
            }
            
            // Draw edges
            g2d.setColor(new Color(158, 158, 158, 100));
            g2d.setStroke(new BasicStroke(1.0f));
            for (Integer nodeId : graphNodesToShow) {
                Object node = allNodes.get(nodeId);
                if (node != null) {
                    double nodeLat = (Double) node.getClass().getMethod("get_latitude").invoke(node);
                    double nodeLon = (Double) node.getClass().getMethod("get_longitude").invoke(node);
                    
                    // Get outgoing edges using reflection
                    @SuppressWarnings("unchecked")
                    java.util.Map<Integer, Object> outgoingEdges = 
                        (java.util.Map<Integer, Object>) node.getClass().getMethod("get_outgoing_edges").invoke(node);
                    
                    if (outgoingEdges != null) {
                        for (Object edge : outgoingEdges.values()) {
                            Integer targetId = (Integer) edge.getClass().getMethod("get_destination").invoke(edge);
                            if (graphNodesToShow.contains(targetId)) {
                                Object targetNode = allNodes.get(targetId);
                                if (targetNode != null) {
                                    double targetLat = (Double) targetNode.getClass().getMethod("get_latitude").invoke(targetNode);
                                    double targetLon = (Double) targetNode.getClass().getMethod("get_longitude").invoke(targetNode);
                                    
                                    int x1 = padding + (int)((nodeLon - minLon) / (maxLon - minLon) * (width - 2 * padding));
                                    int y1 = height - padding - (int)((nodeLat - minLat) / (maxLat - minLat) * (height - 2 * padding));
                                    int x2 = padding + (int)((targetLon - minLon) / (maxLon - minLon) * (width - 2 * padding));
                                    int y2 = height - padding - (int)((targetLat - minLat) / (maxLat - minLat) * (height - 2 * padding));
                                    g2d.drawLine(x1, y1, x2, y2);
                                }
                            }
                        }
                    }
                }
            }
            
            // Draw info text
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
            String info = String.format("Graph Sample: %d nodes (out of %d total)", 
                graphNodesToShow.size(), allNodes.size());
            g2d.drawString(info, 10, 20);
            
        } catch (Exception e) {
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2d.drawString("Error rendering graph: " + e.getMessage(), 10, 20);
        }
    }
    
    private void renderQueryPreview(Graphics2D g2d) {
        try {
            // Get nodes from the Graph class using reflection
            Class<?> graphClass = Class.forName("Graph");
            java.lang.reflect.Method getNodesMethod = graphClass.getMethod("get_nodes");
            @SuppressWarnings("unchecked")
            java.util.Map<Integer, Object> allNodes = (java.util.Map<Integer, Object>) getNodesMethod.invoke(null);
            
            Object sourceNode = allNodes.get(querySourceNode);
            Object destNode = allNodes.get(queryDestNode);
            
            if (sourceNode == null || destNode == null) {
                g2d.setColor(Color.RED);
                g2d.drawString("Invalid source or destination node!", 50, 50);
                return;
            }
            
            // Get coordinates using reflection
            double sourceLat = (Double) sourceNode.getClass().getMethod("get_latitude").invoke(sourceNode);
            double sourceLon = (Double) sourceNode.getClass().getMethod("get_longitude").invoke(sourceNode);
            double destLat = (Double) destNode.getClass().getMethod("get_latitude").invoke(destNode);
            double destLon = (Double) destNode.getClass().getMethod("get_longitude").invoke(destNode);
            
            // Calculate bounds
            double minLat = Math.min(sourceLat, destLat);
            double maxLat = Math.max(sourceLat, destLat);
            double minLon = Math.min(sourceLon, destLon);
            double maxLon = Math.max(sourceLon, destLon);
            
            // Add padding to bounds (20% on each side)
            double latRange = Math.max(0.001, maxLat - minLat);
            double lonRange = Math.max(0.001, maxLon - minLon);
            minLat -= latRange * 0.2;
            maxLat += latRange * 0.2;
            minLon -= lonRange * 0.2;
            maxLon += lonRange * 0.2;
            
            int width = getWidth();
            int height = getHeight();
            int padding = 80;
            
            // Normalize coordinates
            int sourceX = padding + (int)((sourceLon - minLon) / Math.max(0.0001, maxLon - minLon) * (width - 2*padding));
            int sourceY = height - padding - (int)((sourceLat - minLat) / Math.max(0.0001, maxLat - minLat) * (height - 2*padding));
            int destX = padding + (int)((destLon - minLon) / Math.max(0.0001, maxLon - minLon) * (width - 2*padding));
            int destY = height - padding - (int)((destLat - minLat) / Math.max(0.0001, maxLat - minLat) * (height - 2*padding));
            
            // Draw dashed curved line between source and destination
            g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{10.0f, 6.0f}, 0.0f));
            g2d.setColor(new Color(100, 100, 100, 180));
            
            // Calculate control point for quadratic curve (offset perpendicular to line)
            int midX = (sourceX + destX) / 2;
            int midY = (sourceY + destY) / 2;
            double angle = Math.atan2(destY - sourceY, destX - sourceX);
            double perpAngle = angle + Math.PI / 2;
            int offset = 60; // Curve offset
            int ctrlX = midX + (int)(offset * Math.cos(perpAngle));
            int ctrlY = midY + (int)(offset * Math.sin(perpAngle));
            
            // Draw quadratic curve
            java.awt.geom.QuadCurve2D curve = new java.awt.geom.QuadCurve2D.Float(sourceX, sourceY, ctrlX, ctrlY, destX, destY);
            g2d.draw(curve);
            
            // Draw arrow head at destination
            g2d.setStroke(new BasicStroke(2));
            double arrowAngle = Math.atan2(destY - ctrlY, destX - ctrlX);
            int arrowSize = 15;
            int[] arrowX = new int[3];
            int[] arrowY = new int[3];
            arrowX[0] = destX;
            arrowY[0] = destY;
            arrowX[1] = destX - (int)(arrowSize * Math.cos(arrowAngle - Math.PI/6));
            arrowY[1] = destY - (int)(arrowSize * Math.sin(arrowAngle - Math.PI/6));
            arrowX[2] = destX - (int)(arrowSize * Math.cos(arrowAngle + Math.PI/6));
            arrowY[2] = destY - (int)(arrowSize * Math.sin(arrowAngle + Math.PI/6));
            g2d.fillPolygon(arrowX, arrowY, 3);
            
            // Draw source node (green)
            g2d.setColor(new Color(76, 175, 80));
            g2d.fillOval(sourceX - 12, sourceY - 12, 24, 24);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString("S", sourceX - fm.stringWidth("S")/2, sourceY + fm.getAscent()/2 - 2);
            
            // Draw destination node (red)
            g2d.setColor(new Color(244, 67, 54));
            g2d.fillOval(destX - 12, destY - 12, 24, 24);
            g2d.setColor(Color.WHITE);
            g2d.drawString("D", destX - fm.stringWidth("D")/2, destY + fm.getAscent()/2 - 2);
            
            // Draw labels with node IDs
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2d.setColor(new Color(76, 175, 80));
            g2d.drawString("Source: " + querySourceNode, sourceX - 30, sourceY - 20);
            g2d.setColor(new Color(244, 67, 54));
            g2d.drawString("Destination: " + queryDestNode, destX - 30, destY + 30);
            
            // Draw title
            g2d.setColor(new Color(33, 150, 243));
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g2d.drawString("Query Preview - Source to Destination", 20, 30);
            
        } catch (Exception e) {
            g2d.setColor(Color.RED);
            g2d.drawString("Error rendering query preview: " + e.getMessage(), 50, 50);
        }
    }
}
