package ui.panels;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Map Visualization Panel with Advanced Features
 * Works with coordinates directly (no dependency on Node class)
 */
public class MapPanel extends JPanel {
    
    public enum RenderMode {
        SATELLITE("🛰️ Satellite View", "Realistic map-style rendering"),
        NEON("🌟 Neon Glow", "Futuristic glowing path effects"),
        HEATMAP("🔥 Heat Map", "Traffic density visualization"),
        BLUEPRINT("📐 Blueprint", "Technical schematic view"),
        MINIMAL("⚪ Minimal", "Clean, simple visualization");
        
        private final String name;
        private final String desc;
        RenderMode(String name, String desc) { this.name = name; this.desc = desc; }
        public String getName() { return name; }
    }
    
    private List<Integer> pathNodes = Collections.emptyList();
    private List<Integer> wideEdges = Collections.emptyList();
    private List<double[]> pathCoordinates = new ArrayList<>();
    // Context feature removed
    private RenderMode currentMode = RenderMode.SATELLITE;
    
    // Pareto paths for hybrid mode
    private List<ParetoPathData> paretoPaths = new ArrayList<>();
    private boolean showParetoPaths = false;
    
    private double zoomLevel = 1.0;
    private double panX = 0, panY = 0;
    private Point dragStart = null;
    private boolean showLabels = true;
    private boolean showGrid = false;
    private boolean animatePath = false;
    
    private Timer animationTimer;
    private float pulsePhase = 0f;
    
    private Integer previewSource = null;
    private Integer previewDest = null;
    private double[] previewSourceCoord = null;
    private double[] previewDestCoord = null;
    
    // Subgraph preview data
    private List<double[]> subgraphNodeCoordinates = new ArrayList<>();
    private List<int[]> subgraphEdges = new ArrayList<>();  // [fromIdx, toIdx]
    private boolean showSubgraphPreview = false;
    
    private int searchProgress = 0;
    private String progressMessage = "";
    private boolean showSearchProgress = false;
    
    private double minLat, maxLat, minLon, maxLon;
    private boolean boundsCalculated = false;
    
    private static final Color PATH_COLOR = new Color(236, 72, 153);     // Hot Pink
    private static final Color WIDE_PATH_COLOR = new Color(16, 185, 129); // Neon Green  
    private static final Color SOURCE_COLOR = new Color(59, 130, 246);    // Electric Blue
    private static final Color DEST_COLOR = new Color(251, 146, 60);      // Sunset Orange
    private static final Color PATH_NODE_COLOR = new Color(25, 42, 86);   // Dark Navy Blue for intermediate nodes
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);      // Dark Slate
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139); // Cool Gray
    private static final Color BORDER = new Color(226, 232, 240);         // Light Border
    private static final Color BG_SURFACE = new Color(248, 250, 252);     // Off White
    
    // Pareto path colors
    private static final Color PARETO_MAX_WIDENESS = new Color(34, 197, 94);   // Green - max wideness
    private static final Color PARETO_MIN_TURNS = new Color(251, 146, 60);     // Orange - min turns  
    private static final Color PARETO_OTHER = new Color(168, 85, 247);         // Purple - other paths
    
    // Extra vibrant colors for UI
    private static final Color VIVID_PURPLE = new Color(168, 85, 247);
    private static final Color CORAL_PINK = new Color(255, 107, 107);
    private static final Color OCEAN_TEAL = new Color(20, 184, 166);
    private static final Color CYBER_YELLOW = new Color(250, 204, 21);
    private static final Color ROYAL_INDIGO = new Color(99, 102, 241);
    
    /**
     * Data class for Pareto path information
     */
    public static class ParetoPathData {
        public final List<double[]> coordinates;
        public final List<Integer> wideEdges;
        public final double wideScore;
        public final int rightTurns;
        public final int pathIndex;
        
        public ParetoPathData(List<double[]> coordinates, List<Integer> wideEdges,
                             double wideScore, int rightTurns, int pathIndex) {
            this.coordinates = coordinates;
            this.wideEdges = wideEdges;
            this.wideScore = wideScore;
            this.rightTurns = rightTurns;
            this.pathIndex = pathIndex;
        }
    }
    
    public MapPanel() {
        setBackground(BG_SURFACE);
        setPreferredSize(new Dimension(900, 650));
        setupInteraction();
        setupAnimation();
        setupControls();
    }
    
    private void setupInteraction() {
        addMouseWheelListener(e -> {
            double factor = e.getWheelRotation() < 0 ? 1.1 : 0.9;
            zoomLevel = Math.max(0.1, Math.min(10.0, zoomLevel * factor));
            repaint();
        });
        
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    dragStart = e.getPoint();
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }
            public void mouseReleased(MouseEvent e) {
                dragStart = null;
                setCursor(Cursor.getDefaultCursor());
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (dragStart != null) {
                    panX += e.getX() - dragStart.x;
                    panY += e.getY() - dragStart.y;
                    dragStart = e.getPoint();
                    repaint();
                }
            }
        });
    }
    
    private void setupAnimation() {
        animationTimer = new Timer(50, e -> {
            pulsePhase += 0.1f;
            if (pulsePhase > 2 * Math.PI) pulsePhase = 0;
            if (animatePath) repaint();
        });
        animationTimer.start();
    }
    
    private void setupControls() {
        setLayout(new BorderLayout());
        add(createToolbar(), BorderLayout.NORTH);
        add(createLegend(), BorderLayout.EAST);
    }
    
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                // Gradient toolbar background
                java.awt.GradientPaint gp = new java.awt.GradientPaint(
                    0, 0, new Color(255, 255, 255),
                    0, getHeight(), new Color(248, 250, 255)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, VIVID_PURPLE));
        
        JComboBox<RenderMode> modeCombo = new JComboBox<>(RenderMode.values());
        modeCombo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        modeCombo.setBackground(new Color(248, 250, 255));
        modeCombo.setBorder(BorderFactory.createLineBorder(VIVID_PURPLE, 2, true));
        modeCombo.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                if (value instanceof RenderMode) setText(((RenderMode) value).getName());
                return this;
            }
        });
        modeCombo.addActionListener(e -> { currentMode = (RenderMode) modeCombo.getSelectedItem(); repaint(); });
        
        toolbar.add(modeCombo);
        toolbar.add(Box.createHorizontalStrut(8));
        
        JButton zoomIn = createToolbarButton("➕ In", "Zoom In");
        zoomIn.addActionListener(e -> { zoomLevel = Math.min(10, zoomLevel * 1.2); repaint(); });
        toolbar.add(zoomIn);
        
        JButton zoomOut = createToolbarButton("➖ Out", "Zoom Out");
        zoomOut.addActionListener(e -> { zoomLevel = Math.max(0.1, zoomLevel / 1.2); repaint(); });
        toolbar.add(zoomOut);
        
        JButton reset = createToolbarButton("🔄 Reset", "Reset View");
        reset.addActionListener(e -> { zoomLevel = 1.0; panX = 0; panY = 0; repaint(); });
        toolbar.add(reset);
        
        toolbar.add(Box.createHorizontalStrut(8));
        
        JCheckBox labels = new JCheckBox("Labels", showLabels);
        labels.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        labels.setOpaque(false);
        labels.addActionListener(e -> { showLabels = labels.isSelected(); repaint(); });
        toolbar.add(labels);
        
        JCheckBox grid = new JCheckBox("Grid", showGrid);
        grid.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        grid.setOpaque(false);
        grid.addActionListener(e -> { showGrid = grid.isSelected(); repaint(); });
        toolbar.add(grid);
        
        JCheckBox anim = new JCheckBox("Animate", animatePath);
        anim.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        anim.setOpaque(false);
        anim.addActionListener(e -> animatePath = anim.isSelected());
        toolbar.add(anim);
        
        // Context checkbox removed
        
        toolbar.add(Box.createHorizontalStrut(8));
        
        JButton export = createToolbarButton("💾 Export", "Export Map Image");
        export.addActionListener(e -> exportImage());
        toolbar.add(export);
        
        JButton clear = createToolbarButton("🗑️ Clear", "Clear Map");
        clear.addActionListener(e -> clearMap());
        toolbar.add(clear);
        
        return toolbar;
    }
    
    private JButton createToolbarButton(String text, String tooltip) {
        Color buttonColor = text.contains("In") ? new Color(16, 185, 129) :
                           text.contains("Out") ? new Color(251, 146, 60) :
                           text.contains("Reset") ? new Color(59, 130, 246) :
                           text.contains("Clear") ? new Color(239, 68, 68) :
                           new Color(168, 85, 247);
        
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color bg = getModel().isRollover() ? buttonColor : 
                    new Color(buttonColor.getRed(), buttonColor.getGreen(), buttonColor.getBlue(), 40);
                g2d.setColor(bg);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                g2d.setColor(buttonColor);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                
                g2d.dispose();
                
                g.setColor(getModel().isRollover() ? Color.WHITE : buttonColor);
                g.setFont(getFont());
                FontMetrics fm = g.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g.drawString(getText(), x, y);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setToolTipText(tooltip);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(70, 32));
        return btn;
    }
    
    private JPanel createLegend() {
        JPanel legend = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                java.awt.GradientPaint gp = new java.awt.GradientPaint(
                    0, 0, new Color(255, 255, 255),
                    0, getHeight(), new Color(248, 245, 255)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
        legend.setOpaque(false);
        legend.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, VIVID_PURPLE),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        legend.setPreferredSize(new Dimension(175, 0));
        
        JLabel titleLabel = new JLabel("🎯 Legend");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 21));
        titleLabel.setForeground(VIVID_PURPLE);
        legend.add(titleLabel);
        legend.add(Box.createVerticalStrut(16));
        legend.add(createLegendItem("🔵 Source", SOURCE_COLOR));
        legend.add(Box.createVerticalStrut(8));
        legend.add(createLegendItem("🟠 Dest", DEST_COLOR));
        legend.add(Box.createVerticalStrut(8));
        legend.add(createLegendItem("━ Path", PATH_COLOR));
        legend.add(Box.createVerticalStrut(8));
        legend.add(createLegendItem("━ Wide", WIDE_PATH_COLOR));
        legend.add(Box.createVerticalGlue());
        
        return legend;
    }
    
    private JLabel createLegendItem(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font("Segoe UI", Font.BOLD, 19));
        return label;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int cx = getWidth() / 2, cy = getHeight() / 2;
        g2d.translate(cx + panX, cy + panY);
        g2d.scale(zoomLevel, zoomLevel);
        g2d.translate(-cx, -cy);
        
        if (previewSourceCoord != null && previewDestCoord != null) {
            renderQueryPreview(g2d);
        } else if (showParetoPaths && !paretoPaths.isEmpty()) {
            // Render Pareto paths with color coding
            renderParetoPaths(g2d);
        } else if (pathCoordinates.isEmpty()) {
            renderEmptyState(g2d);
        } else {
            switch (currentMode) {
                case SATELLITE -> renderSatelliteView(g2d);
                case NEON -> renderNeonView(g2d);
                case HEATMAP -> renderHeatmapView(g2d);
                case BLUEPRINT -> renderBlueprintView(g2d);
                case MINIMAL -> renderMinimalView(g2d);
            }
        }
        
        if (showSearchProgress) renderProgressOverlay(g2d);
        g2d.dispose();
    }
    
    private void renderEmptyState(Graphics2D g2d) {
        int cx = getWidth() / 2, cy = getHeight() / 2;
        
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(cx - 70, cy - 70, 140, 140);
        
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 34));
        g2d.setColor(PATH_COLOR);
        String title = "FlexiRoute Navigator";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(title, cx - fm.stringWidth(title) / 2, cy - 110);
        
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        g2d.setColor(TEXT_SECONDARY);
        String hint = "Run a query to visualize the path";
        fm = g2d.getFontMetrics();
        g2d.drawString(hint, cx - fm.stringWidth(hint) / 2, cy + 110);
    }
    
    private void calculateBounds() {
        if (boundsCalculated || pathCoordinates.isEmpty()) return;
        
        minLat = Double.MAX_VALUE; maxLat = -Double.MAX_VALUE;
        minLon = Double.MAX_VALUE; maxLon = -Double.MAX_VALUE;
        
        for (double[] c : pathCoordinates) {
            minLat = Math.min(minLat, c[0]); maxLat = Math.max(maxLat, c[0]);
            minLon = Math.min(minLon, c[1]); maxLon = Math.max(maxLon, c[1]);
        }
        
        // Context nodes removed
        
        // Include subgraph nodes in bounds calculation
        if (showSubgraphPreview && !subgraphNodeCoordinates.isEmpty()) {
            for (double[] c : subgraphNodeCoordinates) {
                minLat = Math.min(minLat, c[0]); maxLat = Math.max(maxLat, c[0]);
                minLon = Math.min(minLon, c[1]); maxLon = Math.max(maxLon, c[1]);
            }
        }
        
        double latPad = (maxLat - minLat) * 0.15;
        double lonPad = (maxLon - minLon) * 0.15;
        minLat -= latPad; maxLat += latPad;
        minLon -= lonPad; maxLon += lonPad;
        boundsCalculated = true;
    }
    
    private Point2D.Double toScreen(double lat, double lon, int w, int h, int pad) {
        double x = pad + (lon - minLon) / Math.max(0.0001, maxLon - minLon) * (w - 2 * pad);
        double y = h - pad - (lat - minLat) / Math.max(0.0001, maxLat - minLat) * (h - 2 * pad);
        return new Point2D.Double(x, y);
    }
    
    private void renderSatelliteView(Graphics2D g2d) {
        calculateBounds();
        int w = getWidth(), h = getHeight(), pad = 80;
        
        if (showGrid) drawGrid(g2d, w, h, pad);
        
        // Draw subgraph (larger map context) as the VERY FIRST background layer
        if (showSubgraphPreview && !subgraphNodeCoordinates.isEmpty()) {
            // Draw subgraph edges in very light color - no arrows
            g2d.setColor(new Color(160, 180, 210, 100));
            g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int[] edge : subgraphEdges) {
                if (edge[0] < subgraphNodeCoordinates.size() && edge[1] < subgraphNodeCoordinates.size()) {
                    double[] c1 = subgraphNodeCoordinates.get(edge[0]);
                    double[] c2 = subgraphNodeCoordinates.get(edge[1]);
                    Point2D.Double p1 = toScreen(c1[0], c1[1], w, h, pad);
                    Point2D.Double p2 = toScreen(c2[0], c2[1], w, h, pad);
                    g2d.draw(new Line2D.Double(p1, p2));
                }
            }
            
            // Draw subgraph nodes as small dots
            for (double[] coord : subgraphNodeCoordinates) {
                Point2D.Double p = toScreen(coord[0], coord[1], w, h, pad);
                g2d.setColor(new Color(140, 170, 210, 120));
                g2d.fill(new Ellipse2D.Double(p.x - 2, p.y - 2, 4, 4));
            }
        }
        
        // Context graph drawing removed
        
        // Draw path edges (main path on top)
        for (int i = 0; i < pathCoordinates.size() - 1; i++) {
            Point2D.Double p1 = toScreen(pathCoordinates.get(i)[0], pathCoordinates.get(i)[1], w, h, pad);
            Point2D.Double p2 = toScreen(pathCoordinates.get(i + 1)[0], pathCoordinates.get(i + 1)[1], w, h, pad);
            
            boolean isWide = wideEdges.contains(i);
            Color color = isWide ? WIDE_PATH_COLOR : PATH_COLOR;
            
            if (animatePath) {
                float alpha = (float) (0.5 + 0.5 * Math.sin(pulsePhase + i * 0.3));
                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(255 * alpha));
            }
            
            g2d.setColor(new Color(0, 0, 0, 30));
            g2d.setStroke(new BasicStroke(isWide ? 7 : 5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.draw(new Line2D.Double(p1.x + 2, p1.y + 2, p2.x + 2, p2.y + 2));
            
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(isWide ? 5 : 3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.draw(new Line2D.Double(p1, p2));
            
            // Draw directional arrow on path edge with lighter blue color to reduce visual congestion
            drawArrowHead(g2d, p1, p2, isWide ? 3 : 2.5, new Color(100, 150, 255, 180));
        }
        
        // Draw nodes
        for (int i = 0; i < pathCoordinates.size(); i++) {
            Point2D.Double p = toScreen(pathCoordinates.get(i)[0], pathCoordinates.get(i)[1], w, h, pad);
            
            if (i == 0 || i == pathCoordinates.size() - 1) {
                Color c = i == 0 ? SOURCE_COLOR : DEST_COLOR;
                g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 50));
                g2d.fill(new Ellipse2D.Double(p.x - 10, p.y - 10, 20, 20));
                g2d.setColor(c);
                g2d.fill(new Ellipse2D.Double(p.x - 6, p.y - 6, 12, 12));
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(1));
                g2d.draw(new Ellipse2D.Double(p.x - 6, p.y - 6, 12, 12));
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2d.drawString(i == 0 ? "S" : "D", (int) p.x - 3, (int) p.y + 3);
            } else {
                g2d.setColor(PATH_NODE_COLOR);
                g2d.fill(new Ellipse2D.Double(p.x - 1.5, p.y - 1.5, 3, 3));
            }
        }
        
        drawInfoOverlay(g2d);
    }
    
    private void renderNeonView(Graphics2D g2d) {
        g2d.setColor(new Color(20, 25, 35));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        calculateBounds();
        int w = getWidth(), h = getHeight(), pad = 80;
        
        for (int glow = 4; glow >= 1; glow--) {
            for (int i = 0; i < pathCoordinates.size() - 1; i++) {
                Point2D.Double p1 = toScreen(pathCoordinates.get(i)[0], pathCoordinates.get(i)[1], w, h, pad);
                Point2D.Double p2 = toScreen(pathCoordinates.get(i + 1)[0], pathCoordinates.get(i + 1)[1], w, h, pad);
                boolean isWide = wideEdges.contains(i);
                Color c = isWide ? new Color(255, 100, 50) : new Color(0, 255, 200);
                g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 40 * glow));
                g2d.setStroke(new BasicStroke(2 + glow * 3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.draw(new Line2D.Double(p1, p2));
            }
        }
        
        for (int i : new int[]{0, pathCoordinates.size() - 1}) {
            if (i >= pathCoordinates.size()) continue;
            Point2D.Double p = toScreen(pathCoordinates.get(i)[0], pathCoordinates.get(i)[1], w, h, pad);
            Color c = i == 0 ? new Color(0, 255, 100) : new Color(255, 50, 100);
            for (int glow = 5; glow >= 1; glow--) {
                int size = 8 + glow * 6;
                g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 30 * glow));
                g2d.fill(new Ellipse2D.Double(p.x - size/2, p.y - size/2, size, size));
            }
            g2d.setColor(Color.WHITE);
            g2d.fill(new Ellipse2D.Double(p.x - 6, p.y - 6, 12, 12));
        }
    }
    
    private void renderHeatmapView(Graphics2D g2d) {
        calculateBounds();
        int w = getWidth(), h = getHeight(), pad = 80;
        
        for (int i = 0; i < pathCoordinates.size(); i++) {
            Point2D.Double p = toScreen(pathCoordinates.get(i)[0], pathCoordinates.get(i)[1], w, h, pad);
            float hue = 0.3f * i / Math.max(1, pathCoordinates.size());
            Color heat = Color.getHSBColor(hue, 0.9f, 0.9f);
            for (int r = 30; r >= 5; r -= 5) {
                g2d.setColor(new Color(heat.getRed(), heat.getGreen(), heat.getBlue(), 30));
                g2d.fill(new Ellipse2D.Double(p.x - r, p.y - r, r * 2, r * 2));
            }
        }
        
        g2d.setColor(new Color(50, 50, 50, 200));
        g2d.setStroke(new BasicStroke(2));
        for (int i = 0; i < pathCoordinates.size() - 1; i++) {
            Point2D.Double p1 = toScreen(pathCoordinates.get(i)[0], pathCoordinates.get(i)[1], w, h, pad);
            Point2D.Double p2 = toScreen(pathCoordinates.get(i + 1)[0], pathCoordinates.get(i + 1)[1], w, h, pad);
            g2d.draw(new Line2D.Double(p1, p2));
        }
    }
    
    private void renderBlueprintView(Graphics2D g2d) {
        g2d.setColor(new Color(30, 60, 100));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        calculateBounds();
        int w = getWidth(), h = getHeight(), pad = 80;
        
        g2d.setColor(new Color(50, 80, 130));
        for (int x = pad; x < w - pad; x += 30) g2d.drawLine(x, pad, x, h - pad);
        for (int y = pad; y < h - pad; y += 30) g2d.drawLine(pad, y, w - pad, y);
        
        g2d.setColor(new Color(200, 220, 255));
        g2d.setStroke(new BasicStroke(2));
        for (int i = 0; i < pathCoordinates.size() - 1; i++) {
            Point2D.Double p1 = toScreen(pathCoordinates.get(i)[0], pathCoordinates.get(i)[1], w, h, pad);
            Point2D.Double p2 = toScreen(pathCoordinates.get(i + 1)[0], pathCoordinates.get(i + 1)[1], w, h, pad);
            g2d.draw(new Line2D.Double(p1, p2));
        }
    }
    
    private void renderMinimalView(Graphics2D g2d) {
        calculateBounds();
        int w = getWidth(), h = getHeight(), pad = 80;
        
        g2d.setColor(new Color(50, 50, 50));
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        GeneralPath path = new GeneralPath();
        for (int i = 0; i < pathCoordinates.size(); i++) {
            Point2D.Double p = toScreen(pathCoordinates.get(i)[0], pathCoordinates.get(i)[1], w, h, pad);
            if (i == 0) path.moveTo(p.x, p.y);
            else path.lineTo(p.x, p.y);
        }
        g2d.draw(path);
        
        if (!pathCoordinates.isEmpty()) {
            Point2D.Double ps = toScreen(pathCoordinates.get(0)[0], pathCoordinates.get(0)[1], w, h, pad);
            g2d.setColor(SOURCE_COLOR);
            g2d.fill(new Ellipse2D.Double(ps.x - 8, ps.y - 8, 16, 16));
            
            Point2D.Double pe = toScreen(pathCoordinates.get(pathCoordinates.size()-1)[0], pathCoordinates.get(pathCoordinates.size()-1)[1], w, h, pad);
            g2d.setColor(DEST_COLOR);
            g2d.fill(new Ellipse2D.Double(pe.x - 8, pe.y - 8, 16, 16));
        }
    }
    
    private void renderQueryPreview(Graphics2D g2d) {
        minLat = Math.min(previewSourceCoord[0], previewDestCoord[0]) - 0.01;
        maxLat = Math.max(previewSourceCoord[0], previewDestCoord[0]) + 0.01;
        minLon = Math.min(previewSourceCoord[1], previewDestCoord[1]) - 0.01;
        maxLon = Math.max(previewSourceCoord[1], previewDestCoord[1]) + 0.01;
        
        // Expand bounds to include subgraph nodes
        if (showSubgraphPreview && !subgraphNodeCoordinates.isEmpty()) {
            for (double[] coord : subgraphNodeCoordinates) {
                minLat = Math.min(minLat, coord[0]);
                maxLat = Math.max(maxLat, coord[0]);
                minLon = Math.min(minLon, coord[1]);
                maxLon = Math.max(maxLon, coord[1]);
            }
            // Add padding
            double latPad = (maxLat - minLat) * 0.1;
            double lonPad = (maxLon - minLon) * 0.1;
            minLat -= latPad; maxLat += latPad;
            minLon -= lonPad; maxLon += lonPad;
        }
        
        int w = getWidth(), h = getHeight(), pad = 100;
        
        // Draw subgraph edges first (background layer) - no arrows, just lines
        if (showSubgraphPreview && !subgraphEdges.isEmpty()) {
            g2d.setColor(new Color(140, 170, 210, 150));
            g2d.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int[] edge : subgraphEdges) {
                if (edge[0] < subgraphNodeCoordinates.size() && edge[1] < subgraphNodeCoordinates.size()) {
                    double[] c1 = subgraphNodeCoordinates.get(edge[0]);
                    double[] c2 = subgraphNodeCoordinates.get(edge[1]);
                    Point2D.Double p1 = toScreen(c1[0], c1[1], w, h, pad);
                    Point2D.Double p2 = toScreen(c2[0], c2[1], w, h, pad);
                    g2d.draw(new Line2D.Double(p1, p2));
                }
            }
        }
        
        // Draw subgraph nodes
        if (showSubgraphPreview && !subgraphNodeCoordinates.isEmpty()) {
            for (double[] coord : subgraphNodeCoordinates) {
                Point2D.Double p = toScreen(coord[0], coord[1], w, h, pad);
                // Outer glow
                g2d.setColor(new Color(150, 180, 220, 60));
                g2d.fill(new Ellipse2D.Double(p.x - 5, p.y - 5, 10, 10));
                // Fill
                g2d.setColor(new Color(80, 130, 190, 180));
                g2d.fill(new Ellipse2D.Double(p.x - 3, p.y - 3, 6, 6));
            }
        }
        
        Point2D.Double pSrc = toScreen(previewSourceCoord[0], previewSourceCoord[1], w, h, pad);
        Point2D.Double pDst = toScreen(previewDestCoord[0], previewDestCoord[1], w, h, pad);
        
        // Draw dashed line between source and destination
        g2d.setColor(new Color(100, 100, 100, 150));
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{15, 10}, 0f));
        QuadCurve2D curve = new QuadCurve2D.Double(pSrc.x, pSrc.y, (pSrc.x + pDst.x) / 2, (pSrc.y + pDst.y) / 2 - 50, pDst.x, pDst.y);
        g2d.draw(curve);
        
        // Draw arrow at destination end of the dashed line
        double ctrlX = (pSrc.x + pDst.x) / 2;
        double ctrlY = (pSrc.y + pDst.y) / 2 - 50;
        // Calculate tangent direction at the end of curve (from control point towards destination)
        Point2D.Double arrowFrom = new Point2D.Double(ctrlX, ctrlY);
        drawArrowHead(g2d, arrowFrom, pDst, 14, new Color(80, 80, 80, 200));
        
        // Draw source node
        g2d.setColor(SOURCE_COLOR);
        g2d.fill(new Ellipse2D.Double(pSrc.x - 15, pSrc.y - 15, 30, 30));
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g2d.drawString("S", (int) pSrc.x - 5, (int) pSrc.y + 5);
        
        // Draw destination node
        g2d.setColor(DEST_COLOR);
        g2d.fill(new Ellipse2D.Double(pDst.x - 15, pDst.y - 15, 30, 30));
        g2d.setColor(Color.WHITE);
        g2d.drawString("D", (int) pDst.x - 5, (int) pDst.y + 5);
        
        // Draw title
        g2d.setColor(PATH_COLOR);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g2d.drawString("Query Preview", 30, 40);
        
        // Draw subgraph info
        if (showSubgraphPreview && !subgraphNodeCoordinates.isEmpty()) {
            g2d.setColor(new Color(100, 140, 200));
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            g2d.drawString("Subgraph: " + subgraphNodeCoordinates.size() + " nodes, " + subgraphEdges.size() + " edges", 30, 65);
        }
    }
    
    private void renderProgressOverlay(Graphics2D g2d) {
        int w = getWidth(), h = getHeight();
        g2d.setColor(new Color(255, 255, 255, 230));
        g2d.fillRoundRect(w/2 - 170, h/2 - 55, 340, 110, 14, 14);
        g2d.setColor(PATH_COLOR);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(w/2 - 170, h/2 - 55, 340, 110, 14, 14);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g2d.drawString("Searching...", w/2 - 55, h/2 - 20);
        g2d.setColor(new Color(229, 231, 235));
        g2d.fillRoundRect(w/2 - 140, h/2, 280, 24, 12, 12);
        g2d.setColor(PATH_COLOR);
        g2d.fillRoundRect(w/2 - 140, h/2, (int)(280 * searchProgress / 100.0), 24, 12, 12);
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        g2d.setColor(TEXT_SECONDARY);
        g2d.drawString(progressMessage, w/2 - 120, h/2 + 48);
    }
    
    /**
     * Draw an arrowhead at the end of a line segment
     * @param g2d Graphics context
     * @param from Start point of the line
     * @param to End point of the line (where arrow points)
     * @param arrowSize Size of the arrowhead
     * @param color Color of the arrowhead
     */
    private void drawArrowHead(Graphics2D g2d, Point2D.Double from, Point2D.Double to, double arrowSize, Color color) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double length = Math.sqrt(dx * dx + dy * dy);
        
        if (length < 1) return; // Skip if points are too close
        
        // Normalize direction
        dx /= length;
        dy /= length;
        
        // Calculate arrow tip position (slightly before the end point)
        double tipX = to.x - dx * arrowSize * 0.5;
        double tipY = to.y - dy * arrowSize * 0.5;
        
        // Calculate the two base points of the arrow
        double perpX = -dy;
        double perpY = dx;
        
        double baseX = tipX - dx * arrowSize;
        double baseY = tipY - dy * arrowSize;
        
        double x1 = baseX + perpX * arrowSize * 0.4;
        double y1 = baseY + perpY * arrowSize * 0.4;
        double x2 = baseX - perpX * arrowSize * 0.4;
        double y2 = baseY - perpY * arrowSize * 0.4;
        
        // Draw filled arrow
        GeneralPath arrow = new GeneralPath();
        arrow.moveTo(tipX, tipY);
        arrow.lineTo(x1, y1);
        arrow.lineTo(x2, y2);
        arrow.closePath();
        
        g2d.setColor(color);
        g2d.fill(arrow);
    }
    
    private void drawGrid(Graphics2D g2d, int w, int h, int pad) {
        g2d.setColor(new Color(200, 200, 200, 50));
        for (int x = pad; x < w - pad; x += 50) g2d.drawLine(x, pad, x, h - pad);
        for (int y = pad; y < h - pad; y += 50) g2d.drawLine(pad, y, w - pad, y);
    }
    
    private void drawInfoOverlay(Graphics2D g2d) {
        int height = 80;
        g2d.setColor(new Color(255, 255, 255, 230));
        g2d.fillRoundRect(12, 12, 200, height, 12, 12);
        g2d.setColor(BORDER);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(12, 12, 200, height, 12, 12);
        g2d.setColor(TEXT_PRIMARY);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2d.drawString("Path Info", 24, 35);
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        g2d.setColor(TEXT_SECONDARY);
        g2d.drawString("Path Nodes: " + pathCoordinates.size(), 24, 55);
        g2d.drawString("Zoom: " + String.format("%.0f%%", zoomLevel * 100), 24, 75);
    }
    
    /**
     * Render multiple Pareto optimal paths with color coding:
     * - Green: path with maximum wideness
     * - Orange: path with minimum turns
     * - Purple: other Pareto optimal paths
     */
    private void renderParetoPaths(Graphics2D g2d) {
        if (paretoPaths.isEmpty()) return;
        
        // Calculate bounds for all Pareto paths
        calculateBoundsForParetoPaths();
        int w = getWidth(), h = getHeight(), pad = 80;
        
        if (showGrid) drawGrid(g2d, w, h, pad);
        
        // Find max wideness and min turns indices
        int maxWidenessIdx = 0;
        int minTurnsIdx = 0;
        double maxWideness = -1;
        int minTurns = Integer.MAX_VALUE;
        
        for (int i = 0; i < paretoPaths.size(); i++) {
            ParetoPathData path = paretoPaths.get(i);
            if (path.wideScore > maxWideness) {
                maxWideness = path.wideScore;
                maxWidenessIdx = i;
            }
            if (path.rightTurns < minTurns) {
                minTurns = path.rightTurns;
                minTurnsIdx = i;
            }
        }
        
        // Render paths in reverse order (so important paths are on top)
        // First render "other" paths (purple)
        for (int i = paretoPaths.size() - 1; i >= 0; i--) {
            if (i != maxWidenessIdx && i != minTurnsIdx) {
                renderSingleParetoPath(g2d, paretoPaths.get(i), PARETO_OTHER, 3.0f, 0.6f, w, h, pad);
            }
        }
        
        // Then render min turns path (orange) if different from max wideness
        if (minTurnsIdx != maxWidenessIdx) {
            renderSingleParetoPath(g2d, paretoPaths.get(minTurnsIdx), PARETO_MIN_TURNS, 4.0f, 0.85f, w, h, pad);
        }
        
        // Finally render max wideness path (green) on top
        renderSingleParetoPath(g2d, paretoPaths.get(maxWidenessIdx), PARETO_MAX_WIDENESS, 5.0f, 1.0f, w, h, pad);
        
        // Render source and destination markers (from first path)
        if (!paretoPaths.isEmpty() && paretoPaths.get(0).coordinates.size() >= 2) {
            List<double[]> firstPath = paretoPaths.get(0).coordinates;
            Point2D.Double srcPoint = toScreen(firstPath.get(0)[0], firstPath.get(0)[1], w, h, pad);
            Point2D.Double dstPoint = toScreen(
                firstPath.get(firstPath.size() - 1)[0], 
                firstPath.get(firstPath.size() - 1)[1], w, h, pad);
            
            // Source marker
            g2d.setColor(new Color(SOURCE_COLOR.getRed(), SOURCE_COLOR.getGreen(), SOURCE_COLOR.getBlue(), 50));
            g2d.fill(new Ellipse2D.Double(srcPoint.x - 12, srcPoint.y - 12, 24, 24));
            g2d.setColor(SOURCE_COLOR);
            g2d.fill(new Ellipse2D.Double(srcPoint.x - 8, srcPoint.y - 8, 16, 16));
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.draw(new Ellipse2D.Double(srcPoint.x - 8, srcPoint.y - 8, 16, 16));
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2d.drawString("S", (int) srcPoint.x - 4, (int) srcPoint.y + 4);
            
            // Destination marker
            g2d.setColor(new Color(DEST_COLOR.getRed(), DEST_COLOR.getGreen(), DEST_COLOR.getBlue(), 50));
            g2d.fill(new Ellipse2D.Double(dstPoint.x - 12, dstPoint.y - 12, 24, 24));
            g2d.setColor(DEST_COLOR);
            g2d.fill(new Ellipse2D.Double(dstPoint.x - 8, dstPoint.y - 8, 16, 16));
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.draw(new Ellipse2D.Double(dstPoint.x - 8, dstPoint.y - 8, 16, 16));
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2d.drawString("D", (int) dstPoint.x - 4, (int) dstPoint.y + 4);
        }
        
        // Draw Pareto legend
        drawParetoLegend(g2d, maxWideness, minTurns);
    }
    
    /**
     * Render a single Pareto path with specified color and width
     */
    private void renderSingleParetoPath(Graphics2D g2d, ParetoPathData pathData, Color color, 
                                        float width, float alpha, int w, int h, int pad) {
        if (pathData.coordinates == null || pathData.coordinates.size() < 2) return;
        
        // Outer glow
        Color glowColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(40 * alpha));
        g2d.setColor(glowColor);
        g2d.setStroke(new BasicStroke(width + 6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < pathData.coordinates.size() - 1; i++) {
            Point2D.Double p1 = toScreen(pathData.coordinates.get(i)[0], pathData.coordinates.get(i)[1], w, h, pad);
            Point2D.Double p2 = toScreen(pathData.coordinates.get(i + 1)[0], pathData.coordinates.get(i + 1)[1], w, h, pad);
            g2d.draw(new Line2D.Double(p1, p2));
        }
        
        // Border (white)
        g2d.setColor(new Color(255, 255, 255, (int)(180 * alpha)));
        g2d.setStroke(new BasicStroke(width + 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < pathData.coordinates.size() - 1; i++) {
            Point2D.Double p1 = toScreen(pathData.coordinates.get(i)[0], pathData.coordinates.get(i)[1], w, h, pad);
            Point2D.Double p2 = toScreen(pathData.coordinates.get(i + 1)[0], pathData.coordinates.get(i + 1)[1], w, h, pad);
            g2d.draw(new Line2D.Double(p1, p2));
        }
        
        // Core path
        Color coreColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(255 * alpha));
        g2d.setColor(coreColor);
        g2d.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < pathData.coordinates.size() - 1; i++) {
            Point2D.Double p1 = toScreen(pathData.coordinates.get(i)[0], pathData.coordinates.get(i)[1], w, h, pad);
            Point2D.Double p2 = toScreen(pathData.coordinates.get(i + 1)[0], pathData.coordinates.get(i + 1)[1], w, h, pad);
            g2d.draw(new Line2D.Double(p1, p2));
        }
    }
    
    /**
     * Calculate bounds for all Pareto paths
     */
    private void calculateBoundsForParetoPaths() {
        if (boundsCalculated || paretoPaths.isEmpty()) return;
        
        minLat = Double.MAX_VALUE; maxLat = -Double.MAX_VALUE;
        minLon = Double.MAX_VALUE; maxLon = -Double.MAX_VALUE;
        
        for (ParetoPathData path : paretoPaths) {
            for (double[] c : path.coordinates) {
                minLat = Math.min(minLat, c[0]); maxLat = Math.max(maxLat, c[0]);
                minLon = Math.min(minLon, c[1]); maxLon = Math.max(maxLon, c[1]);
            }
        }
        
        double latPad = (maxLat - minLat) * 0.15 + 0.001;
        double lonPad = (maxLon - minLon) * 0.15 + 0.001;
        minLat -= latPad; maxLat += latPad;
        minLon -= lonPad; maxLon += lonPad;
        boundsCalculated = true;
    }
    
    /**
     * Draw legend for Pareto paths
     */
    private void drawParetoLegend(Graphics2D g2d, double maxWideness, int minTurns) {
        int x = 12, y = 12;
        int width = 210;
        int height = 110;
        
        // Background
        g2d.setColor(new Color(255, 255, 255, 230));
        g2d.fillRoundRect(x, y, width, height, 12, 12);
        
        // Border
        g2d.setColor(BORDER);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(x, y, width, height, 12, 12);
        
        // Title
        g2d.setColor(TEXT_PRIMARY);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g2d.drawString("Pareto Paths (" + paretoPaths.size() + ")", x + 12, y + 22);
        
        // Legend items
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        int itemY = y + 42;
        
        // Max wideness (green)
        g2d.setColor(PARETO_MAX_WIDENESS);
        g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(x + 12, itemY, x + 40, itemY);
        g2d.setColor(TEXT_PRIMARY);
        g2d.drawString(String.format("Max Wide: %.2f%%", maxWideness), x + 50, itemY + 4);
        
        // Min turns (orange)
        itemY += 24;
        g2d.setColor(PARETO_MIN_TURNS);
        g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(x + 12, itemY, x + 40, itemY);
        g2d.setColor(TEXT_PRIMARY);
        g2d.drawString(String.format("Min Turns: %d", minTurns), x + 50, itemY + 4);
        
        // Other paths (purple)
        itemY += 24;
        g2d.setColor(PARETO_OTHER);
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(x + 12, itemY, x + 40, itemY);
        g2d.setColor(TEXT_PRIMARY);
        g2d.drawString("Other Pareto paths", x + 50, itemY + 4);
    }
    
    private void exportImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("flexiroute_map.png"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                paint(img.createGraphics());
                javax.imageio.ImageIO.write(img, "PNG", chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Exported!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // === PUBLIC API ===
    
    public void clearMap() {
        this.pathNodes = Collections.emptyList();
        this.wideEdges = Collections.emptyList();
        this.pathCoordinates = new ArrayList<>();
        this.paretoPaths = new ArrayList<>();
        this.showParetoPaths = false;
        this.boundsCalculated = false;
        clearQueryPreview();
        showSearchProgress = false;
        zoomLevel = 1.0;
        panX = 0;
        panY = 0;
        repaint();
    }
    
    public void setPath(List<Integer> nodes, List<Integer> wideEdgeIndices, List<double[]> coordinates) {
        this.pathNodes = nodes != null ? new ArrayList<>(nodes) : Collections.emptyList();
        this.wideEdges = wideEdgeIndices != null ? new ArrayList<>(wideEdgeIndices) : Collections.emptyList();
        this.pathCoordinates = coordinates != null ? new ArrayList<>(coordinates) : new ArrayList<>();
        this.paretoPaths.clear();
        this.showParetoPaths = false;
        this.boundsCalculated = false;
        clearQueryPreview();
        repaint();
    }
    
    /**
     * Set multiple Pareto optimal paths for hybrid mode visualization
     * Paths are color-coded: green for max wideness, orange for min turns, purple for others
     */
    public void setParetoPaths(List<map.RouteOverlayRenderer.ParetoPathInfo> paths) {
        this.paretoPaths.clear();
        if (paths != null) {
            for (map.RouteOverlayRenderer.ParetoPathInfo p : paths) {
                this.paretoPaths.add(new ParetoPathData(p.coordinates, p.wideEdges, p.wideScore, p.rightTurns, p.pathIndex));
            }
        }
        this.showParetoPaths = !this.paretoPaths.isEmpty();
        
        // Clear single path when Pareto paths are set
        if (this.showParetoPaths) {
            this.pathCoordinates.clear();
            this.pathNodes = Collections.emptyList();
            this.wideEdges = Collections.emptyList();
        }
        
        this.boundsCalculated = false;
        clearQueryPreview();
        repaint();
    }
    
    /**

     * Set path along with graph context in one call
     * @param pathToContextList Edges from path nodes to context nodes [pathNodeIndex, contextNodeIndex]
     */
    public void setPathWithContext(List<Integer> nodes, List<Integer> wideEdgeIndices, 
                                    List<double[]> coordinates, List<double[]> contextCoords, 
                                    List<int[]> contextEdgeList, List<int[]> pathToContextList) {
        this.pathNodes = nodes != null ? new ArrayList<>(nodes) : Collections.emptyList();
        this.wideEdges = wideEdgeIndices != null ? new ArrayList<>(wideEdgeIndices) : Collections.emptyList();
        this.pathCoordinates = coordinates != null ? new ArrayList<>(coordinates) : new ArrayList<>();
        this.boundsCalculated = false;
        clearQueryPreview();
        repaint();
    }
    
    /**
     * Set path along with subgraph for map-like visualization (context feature removed)
     */
    public void setPathWithContextAndSubgraph(List<Integer> nodes, List<Integer> wideEdgeIndices, 
                                    List<double[]> coordinates, List<double[]> contextCoords, 
                                    List<int[]> contextEdgeList, List<int[]> pathToContextList,
                                    List<double[]> subgraphCoords, List<int[]> subgraphEdgeList) {
        this.pathNodes = nodes != null ? new ArrayList<>(nodes) : Collections.emptyList();
        this.wideEdges = wideEdgeIndices != null ? new ArrayList<>(wideEdgeIndices) : Collections.emptyList();
        this.pathCoordinates = coordinates != null ? new ArrayList<>(coordinates) : new ArrayList<>();
        this.subgraphNodeCoordinates = subgraphCoords != null ? new ArrayList<>(subgraphCoords) : new ArrayList<>();
        this.subgraphEdges = subgraphEdgeList != null ? new ArrayList<>(subgraphEdgeList) : new ArrayList<>();
        this.showSubgraphPreview = !this.subgraphNodeCoordinates.isEmpty();
        this.boundsCalculated = false;
        clearQueryPreviewOnly();  // Clear preview but keep subgraph
        repaint();
    }
    
    /**
     * Clear only query preview data without affecting subgraph
     */
    private void clearQueryPreviewOnly() {
        previewSource = previewDest = null;
        previewSourceCoord = previewDestCoord = null;
    }
    
    public void setQueryPreview(int source, int dest, double[] sourceCoord, double[] destCoord) {
        this.previewSource = source;
        this.previewDest = dest;
        this.previewSourceCoord = sourceCoord;
        this.previewDestCoord = destCoord;
        repaint();
    }
    
    /**
     * Set subgraph preview data to show nodes and edges within the view area
     * @param nodeCoordinates List of [lat, lon] for subgraph nodes
     * @param edges List of [fromIdx, toIdx] for subgraph edges
     */
    public void setSubgraphPreview(List<double[]> nodeCoordinates, List<int[]> edges) {
        this.subgraphNodeCoordinates = nodeCoordinates != null ? new ArrayList<>(nodeCoordinates) : new ArrayList<>();
        this.subgraphEdges = edges != null ? new ArrayList<>(edges) : new ArrayList<>();
        this.showSubgraphPreview = !this.subgraphNodeCoordinates.isEmpty();
        repaint();
    }
    
    /**
     * Set query preview along with subgraph in one call
     */
    public void setQueryPreviewWithSubgraph(int source, int dest, double[] sourceCoord, double[] destCoord,
                                             List<double[]> subgraphNodes, List<int[]> subgraphEdgeList) {
        this.previewSource = source;
        this.previewDest = dest;
        this.previewSourceCoord = sourceCoord;
        this.previewDestCoord = destCoord;
        this.subgraphNodeCoordinates = subgraphNodes != null ? new ArrayList<>(subgraphNodes) : new ArrayList<>();
        this.subgraphEdges = subgraphEdgeList != null ? new ArrayList<>(subgraphEdgeList) : new ArrayList<>();
        this.showSubgraphPreview = !this.subgraphNodeCoordinates.isEmpty();
        repaint();
    }
    
    public void clearQueryPreview() {
        previewSource = previewDest = null;
        previewSourceCoord = previewDestCoord = null;
        subgraphNodeCoordinates = new ArrayList<>();
        subgraphEdges = new ArrayList<>();
        showSubgraphPreview = false;
        repaint();
    }
    
    public void setSearchProgress(int progress, String message) {
        this.searchProgress = progress;
        this.progressMessage = message;
        this.showSearchProgress = progress < 100;
        repaint();
    }
    
    public void clearSearchFrontier() {
        showSearchProgress = false;
        repaint();
    }
}
