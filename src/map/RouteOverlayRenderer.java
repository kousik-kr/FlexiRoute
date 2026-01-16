package map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Route Overlay Renderer - Draws paths, markers, and route information on top of map tiles.
 * 
 * Architecture:
 * +--------------------------+
 * | RouteOverlayRenderer     |
 * |  - Path rendering        |
 * |  - Marker drawing        |
 * |  - Direction arrows      |
 * |  - Width visualization   |
 * |  - Animation support     |
 * +--------------------------+
 * 
 * This renderer converts geographic coordinates to screen pixels
 * and draws styled overlays for routing visualization.
 */
public class RouteOverlayRenderer {
    
    // === COLOR PALETTE ===
    // Primary route colors
    public static final Color PATH_PRIMARY = new Color(59, 130, 246);     // Electric Blue
    public static final Color PATH_WIDE = new Color(16, 185, 129);        // Emerald Green
    public static final Color PATH_NARROW = new Color(239, 68, 68);       // Red warning
    public static final Color PATH_GLOW = new Color(59, 130, 246, 80);    // Glow effect
    
    // Pareto path colors
    public static final Color PARETO_MAX_WIDENESS = new Color(34, 197, 94);   // Green - max wideness
    public static final Color PARETO_MIN_TURNS = new Color(251, 146, 60);     // Orange - min turns
    public static final Color PARETO_OTHER = new Color(168, 85, 247);         // Purple - other Pareto paths
    
    // Marker colors
    public static final Color SOURCE_COLOR = new Color(34, 197, 94);      // Green
    public static final Color DEST_COLOR = new Color(239, 68, 68);        // Red
    public static final Color WAYPOINT_COLOR = new Color(251, 146, 60);   // Orange
    public static final Color NODE_COLOR = new Color(99, 102, 241);       // Indigo
    
    // UI colors
    public static final Color LABEL_BG = new Color(255, 255, 255, 230);
    public static final Color LABEL_BORDER = new Color(100, 100, 100, 100);
    public static final Color SHADOW = new Color(0, 0, 0, 40);
    
    // Rendering styles
    public enum PathStyle {
        SOLID,          // Simple solid line
        GLOW,           // Line with outer glow
        GRADIENT,       // Gradient along path
        DASHED,         // Dashed line
        ANIMATED,       // Animated flowing line
        WIDTH_BASED     // Color varies by road width
    }
    
    // Marker styles
    public enum MarkerStyle {
        CIRCLE,
        PIN,
        FLAG,
        PULSE
    }
    
    private final CoordinateConverter converter;
    private PathStyle pathStyle = PathStyle.GLOW;
    private MarkerStyle markerStyle = MarkerStyle.PIN;
    private float pathWidth = 4.0f;
    private float widePathWidth = 6.0f;
    private boolean showDirectionArrows = true;
    private boolean showDistanceLabels = true;
    private boolean showNodeMarkers = false;
    private float animationPhase = 0f;
    
    public RouteOverlayRenderer(CoordinateConverter converter) {
        this.converter = converter;
    }
    
    /**
     * Render a complete route with path and endpoints
     */
    public void renderRoute(Graphics2D g2d, List<double[]> coordinates, List<Integer> wideEdges) {
        if (coordinates == null || coordinates.size() < 2) return;
        
        Graphics2D g = (Graphics2D) g2d.create();
        setupRenderingHints(g);
        
        try {
            // Convert coordinates to screen points
            List<Point2D.Double> screenPoints = new ArrayList<>();
            for (double[] coord : coordinates) {
                screenPoints.add(converter.latLonToPixel(coord[0], coord[1]));
            }
            
            // Render based on style
            switch (pathStyle) {
                case GLOW -> renderGlowPath(g, screenPoints, wideEdges);
                case GRADIENT -> renderGradientPath(g, screenPoints, wideEdges);
                case DASHED -> renderDashedPath(g, screenPoints);
                case ANIMATED -> renderAnimatedPath(g, screenPoints, wideEdges);
                case WIDTH_BASED -> renderWidthBasedPath(g, screenPoints, wideEdges);
                default -> renderSolidPath(g, screenPoints, wideEdges);
            }
            
            // Direction arrows
            if (showDirectionArrows) {
                renderDirectionArrows(g, screenPoints, wideEdges);
            }
            
            // Intermediate nodes
            if (showNodeMarkers) {
                renderIntermediateNodes(g, screenPoints);
            }
            
            // Source and destination markers
            renderMarker(g, screenPoints.get(0), SOURCE_COLOR, "S", true);
            renderMarker(g, screenPoints.get(screenPoints.size() - 1), DEST_COLOR, "D", true);
            
        } finally {
            g.dispose();
        }
    }
    
    /**
     * Render path with glowing effect
     */
    private void renderGlowPath(Graphics2D g, List<Point2D.Double> points, List<Integer> wideEdges) {
        // Outer glow layers
        for (int glow = 4; glow >= 1; glow--) {
            for (int i = 0; i < points.size() - 1; i++) {
                Point2D.Double p1 = points.get(i);
                Point2D.Double p2 = points.get(i + 1);
                
                boolean isWide = wideEdges != null && wideEdges.contains(i);
                Color baseColor = isWide ? PATH_WIDE : PATH_PRIMARY;
                
                g.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 
                                    25 * glow));
                g.setStroke(new BasicStroke(pathWidth + glow * 4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Line2D.Double(p1, p2));
            }
        }
        
        // Core line
        renderSolidPath(g, points, wideEdges);
    }
    
    /**
     * Render solid path
     */
    private void renderSolidPath(Graphics2D g, List<Point2D.Double> points, List<Integer> wideEdges) {
        for (int i = 0; i < points.size() - 1; i++) {
            Point2D.Double p1 = points.get(i);
            Point2D.Double p2 = points.get(i + 1);
            
            boolean isWide = wideEdges != null && wideEdges.contains(i);
            
            // Shadow
            g.setColor(SHADOW);
            g.setStroke(new BasicStroke(isWide ? widePathWidth + 2 : pathWidth + 2, 
                                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(p1.x + 2, p1.y + 2, p2.x + 2, p2.y + 2));
            
            // Main line
            g.setColor(isWide ? PATH_WIDE : PATH_PRIMARY);
            g.setStroke(new BasicStroke(isWide ? widePathWidth : pathWidth, 
                                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(p1, p2));
        }
    }
    
    /**
     * Render gradient path (color changes along the route)
     */
    private void renderGradientPath(Graphics2D g, List<Point2D.Double> points, List<Integer> wideEdges) {
        for (int i = 0; i < points.size() - 1; i++) {
            Point2D.Double p1 = points.get(i);
            Point2D.Double p2 = points.get(i + 1);
            
            float progress = (float) i / Math.max(1, points.size() - 2);
            Color startColor = interpolateColor(SOURCE_COLOR, DEST_COLOR, progress);
            Color endColor = interpolateColor(SOURCE_COLOR, DEST_COLOR, progress + 1.0f / (points.size() - 1));
            
            boolean isWide = wideEdges != null && wideEdges.contains(i);
            
            GradientPaint gradient = new GradientPaint(
                (float) p1.x, (float) p1.y, startColor,
                (float) p2.x, (float) p2.y, endColor
            );
            
            g.setPaint(gradient);
            g.setStroke(new BasicStroke(isWide ? widePathWidth : pathWidth, 
                                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(p1, p2));
        }
    }
    
    /**
     * Render dashed path
     */
    private void renderDashedPath(Graphics2D g, List<Point2D.Double> points) {
        float[] dashPattern = {15, 10};
        g.setStroke(new BasicStroke(pathWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                    10f, dashPattern, 0f));
        g.setColor(PATH_PRIMARY);
        
        GeneralPath path = new GeneralPath();
        for (int i = 0; i < points.size(); i++) {
            if (i == 0) {
                path.moveTo(points.get(i).x, points.get(i).y);
            } else {
                path.lineTo(points.get(i).x, points.get(i).y);
            }
        }
        g.draw(path);
    }
    
    /**
     * Render animated flowing path
     */
    private void renderAnimatedPath(Graphics2D g, List<Point2D.Double> points, List<Integer> wideEdges) {
        // Static background line
        g.setColor(new Color(PATH_PRIMARY.getRed(), PATH_PRIMARY.getGreen(), PATH_PRIMARY.getBlue(), 100));
        g.setStroke(new BasicStroke(pathWidth + 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        GeneralPath bgPath = new GeneralPath();
        for (int i = 0; i < points.size(); i++) {
            if (i == 0) bgPath.moveTo(points.get(i).x, points.get(i).y);
            else bgPath.lineTo(points.get(i).x, points.get(i).y);
        }
        g.draw(bgPath);
        
        // Animated segments
        float[] dashPattern = {20, 30};
        g.setStroke(new BasicStroke(pathWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                    10f, dashPattern, animationPhase * 50));
        g.setColor(Color.WHITE);
        g.draw(bgPath);
    }
    
    /**
     * Render path with color based on road width
     */
    private void renderWidthBasedPath(Graphics2D g, List<Point2D.Double> points, List<Integer> wideEdges) {
        for (int i = 0; i < points.size() - 1; i++) {
            Point2D.Double p1 = points.get(i);
            Point2D.Double p2 = points.get(i + 1);
            
            boolean isWide = wideEdges != null && wideEdges.contains(i);
            Color color;
            float width;
            
            if (isWide) {
                color = PATH_WIDE;
                width = widePathWidth;
            } else {
                color = PATH_PRIMARY;
                width = pathWidth;
            }
            
            // Outer glow
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60));
            g.setStroke(new BasicStroke(width + 6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(p1, p2));
            
            // Border
            g.setColor(new Color(255, 255, 255, 200));
            g.setStroke(new BasicStroke(width + 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(p1, p2));
            
            // Core
            g.setColor(color);
            g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(p1, p2));
        }
    }
    
    /**
     * Render direction arrows along the path
     */
    private void renderDirectionArrows(Graphics2D g, List<Point2D.Double> points, List<Integer> wideEdges) {
        // Draw arrows every N segments
        int arrowInterval = Math.max(1, points.size() / 10);
        
        for (int i = arrowInterval; i < points.size() - 1; i += arrowInterval) {
            Point2D.Double p1 = points.get(i - 1);
            Point2D.Double p2 = points.get(i);
            
            boolean isWide = wideEdges != null && wideEdges.contains(i - 1);
            
            // Calculate midpoint and direction
            double midX = (p1.x + p2.x) / 2;
            double midY = (p1.y + p2.y) / 2;
            double angle = Math.atan2(p2.y - p1.y, p2.x - p1.x);
            
            drawArrow(g, midX, midY, angle, 8, isWide ? PATH_WIDE : PATH_PRIMARY);
        }
    }
    
    /**
     * Draw a single arrow
     */
    private void drawArrow(Graphics2D g, double x, double y, double angle, double size, Color color) {
        GeneralPath arrow = new GeneralPath();
        
        double x1 = x + size * Math.cos(angle);
        double y1 = y + size * Math.sin(angle);
        double x2 = x + size * 0.6 * Math.cos(angle + Math.PI * 0.7);
        double y2 = y + size * 0.6 * Math.sin(angle + Math.PI * 0.7);
        double x3 = x + size * 0.6 * Math.cos(angle - Math.PI * 0.7);
        double y3 = y + size * 0.6 * Math.sin(angle - Math.PI * 0.7);
        
        arrow.moveTo(x1, y1);
        arrow.lineTo(x2, y2);
        arrow.lineTo(x3, y3);
        arrow.closePath();
        
        g.setColor(Color.WHITE);
        g.fill(arrow);
        g.setColor(color);
        g.setStroke(new BasicStroke(1.5f));
        g.draw(arrow);
    }
    
    /**
     * Render intermediate node markers
     */
    private void renderIntermediateNodes(Graphics2D g, List<Point2D.Double> points) {
        for (int i = 1; i < points.size() - 1; i++) {
            Point2D.Double p = points.get(i);
            
            g.setColor(NODE_COLOR);
            g.fill(new Ellipse2D.Double(p.x - 3, p.y - 3, 6, 6));
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(1));
            g.draw(new Ellipse2D.Double(p.x - 3, p.y - 3, 6, 6));
        }
    }
    
    /**
     * Render a map marker
     */
    public void renderMarker(Graphics2D g, Point2D.Double point, Color color, String label, boolean showLabel) {
        switch (markerStyle) {
            case PIN -> renderPinMarker(g, point, color, label, showLabel);
            case FLAG -> renderFlagMarker(g, point, color, label, showLabel);
            case PULSE -> renderPulseMarker(g, point, color, label, showLabel);
            default -> renderCircleMarker(g, point, color, label, showLabel);
        }
    }
    
    private void renderCircleMarker(Graphics2D g, Point2D.Double p, Color color, String label, boolean showLabel) {
        // Outer glow
        for (int i = 3; i >= 1; i--) {
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40 * i));
            g.fill(new Ellipse2D.Double(p.x - 8 - i * 4, p.y - 8 - i * 4, 16 + i * 8, 16 + i * 8));
        }
        
        // White border
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(p.x - 10, p.y - 10, 20, 20));
        
        // Colored fill
        g.setColor(color);
        g.fill(new Ellipse2D.Double(p.x - 8, p.y - 8, 16, 16));
        
        // Label
        if (showLabel && label != null) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fm = g.getFontMetrics();
            int labelX = (int) p.x - fm.stringWidth(label) / 2;
            int labelY = (int) p.y + fm.getAscent() / 2 - 1;
            g.drawString(label, labelX, labelY);
        }
    }
    
    private void renderPinMarker(Graphics2D g, Point2D.Double p, Color color, String label, boolean showLabel) {
        // Shadow
        g.setColor(SHADOW);
        GeneralPath shadowPin = createPinShape(p.x + 2, p.y + 2, 24);
        g.fill(shadowPin);
        
        // Pin body
        GeneralPath pin = createPinShape(p.x, p.y, 24);
        
        // Gradient fill
        GradientPaint gradient = new GradientPaint(
            (float) p.x - 12, (float) p.y - 24, color.brighter(),
            (float) p.x + 12, (float) p.y, color.darker()
        );
        g.setPaint(gradient);
        g.fill(pin);
        
        // Border
        g.setColor(color.darker());
        g.setStroke(new BasicStroke(1.5f));
        g.draw(pin);
        
        // Inner circle (white)
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(p.x - 6, p.y - 20, 12, 12));
        
        // Label
        if (showLabel && label != null) {
            g.setColor(color);
            g.setFont(new Font("Segoe UI", Font.BOLD, 10));
            FontMetrics fm = g.getFontMetrics();
            int labelX = (int) p.x - fm.stringWidth(label) / 2;
            int labelY = (int) p.y - 12;
            g.drawString(label, labelX, labelY);
        }
    }
    
    private GeneralPath createPinShape(double x, double y, double height) {
        GeneralPath pin = new GeneralPath();
        double radius = height * 0.4;
        
        // Start at bottom point
        pin.moveTo(x, y);
        
        // Left side curve up to circle
        pin.curveTo(x - radius, y - height * 0.3, 
                    x - radius, y - height * 0.6,
                    x - radius, y - height + radius);
        
        // Top arc
        pin.curveTo(x - radius, y - height,
                    x, y - height - radius * 0.3,
                    x, y - height - radius * 0.3);
        pin.curveTo(x, y - height - radius * 0.3,
                    x + radius, y - height,
                    x + radius, y - height + radius);
        
        // Right side curve back to point
        pin.curveTo(x + radius, y - height * 0.6,
                    x + radius, y - height * 0.3,
                    x, y);
        
        pin.closePath();
        return pin;
    }
    
    private void renderFlagMarker(Graphics2D g, Point2D.Double p, Color color, String label, boolean showLabel) {
        // Pole
        g.setColor(new Color(60, 60, 60));
        g.setStroke(new BasicStroke(2));
        g.draw(new Line2D.Double(p.x, p.y, p.x, p.y - 30));
        
        // Flag
        GeneralPath flag = new GeneralPath();
        flag.moveTo(p.x, p.y - 30);
        flag.lineTo(p.x + 20, p.y - 25);
        flag.lineTo(p.x, p.y - 20);
        flag.closePath();
        
        g.setColor(color);
        g.fill(flag);
        g.setColor(color.darker());
        g.draw(flag);
        
        // Base dot
        g.setColor(color);
        g.fill(new Ellipse2D.Double(p.x - 4, p.y - 4, 8, 8));
    }
    
    private void renderPulseMarker(Graphics2D g, Point2D.Double p, Color color, String label, boolean showLabel) {
        // Animated pulse rings
        float pulse = (float) (Math.sin(animationPhase * Math.PI) + 1) / 2;
        
        for (int i = 3; i >= 1; i--) {
            float scale = 1 + pulse * i * 0.3f;
            int alpha = (int) (80 * (1 - pulse) / i);
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            int size = (int) (20 * scale);
            g.fill(new Ellipse2D.Double(p.x - size / 2, p.y - size / 2, size, size));
        }
        
        // Core marker
        renderCircleMarker(g, p, color, label, showLabel);
    }
    
    /**
     * Render info label on map
     */
    public void renderInfoLabel(Graphics2D g, int x, int y, String title, String... lines) {
        int width = 180;
        int lineHeight = 18;
        int height = 30 + lines.length * lineHeight;
        
        // Background
        g.setColor(LABEL_BG);
        g.fill(new RoundRectangle2D.Double(x, y, width, height, 10, 10));
        
        // Border
        g.setColor(LABEL_BORDER);
        g.setStroke(new BasicStroke(1));
        g.draw(new RoundRectangle2D.Double(x, y, width, height, 10, 10));
        
        // Title
        g.setColor(new Color(50, 50, 50));
        g.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g.drawString(title, x + 10, y + 20);
        
        // Lines
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.setColor(new Color(100, 100, 100));
        for (int i = 0; i < lines.length; i++) {
            g.drawString(lines[i], x + 10, y + 38 + i * lineHeight);
        }
    }
    
    /**
     * Render scale bar
     */
    public void renderScaleBar(Graphics2D g, int x, int y) {
        // Calculate scale based on zoom level
        int zoom = converter.getZoomLevel();
        double metersPerPixel = 156543.03392 * Math.cos(Math.toRadians(converter.getCenterLat())) / Math.pow(2, zoom);
        
        // Find nice round number for scale bar
        double[] distances = {10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000};
        double targetWidth = 100; // pixels
        
        double selectedDistance = distances[0];
        for (double d : distances) {
            if (d / metersPerPixel >= 50 && d / metersPerPixel <= 150) {
                selectedDistance = d;
                break;
            }
        }
        
        int barWidth = (int) (selectedDistance / metersPerPixel);
        
        // Draw scale bar
        g.setColor(new Color(255, 255, 255, 200));
        g.fillRect(x, y, barWidth + 10, 25);
        
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawLine(x + 5, y + 15, x + 5 + barWidth, y + 15);
        g.drawLine(x + 5, y + 10, x + 5, y + 20);
        g.drawLine(x + 5 + barWidth, y + 10, x + 5 + barWidth, y + 20);
        
        // Label
        String label = selectedDistance >= 1000 ? 
            String.format("%.0f km", selectedDistance / 1000) : 
            String.format("%.0f m", selectedDistance);
        
        g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, x + 5 + (barWidth - fm.stringWidth(label)) / 2, y + 10);
    }
    
    /**
     * Pareto path info for rendering multiple paths
     */
    public static class ParetoPathInfo {
        public final List<double[]> coordinates;
        public final List<Integer> wideEdges;
        public final double wideRoadPercentage;
        public final int rightTurns;
        public final int pathIndex;
        
        public ParetoPathInfo(List<double[]> coordinates, List<Integer> wideEdges, 
                              double wideRoadPercentage, int rightTurns, int pathIndex) {
            this.coordinates = coordinates;
            this.wideEdges = wideEdges;
            this.wideRoadPercentage = wideRoadPercentage;
            this.rightTurns = rightTurns;
            this.pathIndex = pathIndex;
        }
    }
    
    /**
     * Render multiple Pareto optimal paths with color coding:
     * - Green: path with maximum wideness
     * - Orange: path with minimum turns
     * - Purple: other Pareto optimal paths
     */
    public void renderParetoPaths(Graphics2D g2d, List<ParetoPathInfo> paretoPaths) {
        if (paretoPaths == null || paretoPaths.isEmpty()) return;
        
        Graphics2D g = (Graphics2D) g2d.create();
        setupRenderingHints(g);
        
        try {
            // Find max wideness and min turns indices
            int maxWidenessIdx = 0;
            int minTurnsIdx = 0;
            double maxWideness = -1;
            int minTurns = Integer.MAX_VALUE;
            
            for (int i = 0; i < paretoPaths.size(); i++) {
                ParetoPathInfo path = paretoPaths.get(i);
                if (path.wideRoadPercentage > maxWideness) {
                    maxWideness = path.wideRoadPercentage;
                    maxWidenessIdx = i;
                }
                if (path.rightTurns < minTurns) {
                    minTurns = path.rightTurns;
                    minTurnsIdx = i;
                }
            }
            
            System.out.println("[Render] Pareto paths: " + paretoPaths.size() + 
                ", maxWidenessIdx=" + maxWidenessIdx + ", minTurnsIdx=" + minTurnsIdx);
            
            // Render ALL paths - first the "other" paths (purple), then special ones on top
            // Use different line offsets to make overlapping paths visible
            int purpleCount = 0;
            for (int i = paretoPaths.size() - 1; i >= 0; i--) {
                if (i != maxWidenessIdx && i != minTurnsIdx) {
                    // Render purple paths with slight offset to make them distinguishable
                    renderParetoPathWithOffset(g, paretoPaths.get(i), PARETO_OTHER, 3.0f, 0.7f, purpleCount * 3);
                    purpleCount++;
                }
            }
            
            // Then render min turns path (orange) if different from max wideness
            if (minTurnsIdx != maxWidenessIdx) {
                renderParetoPathWithOffset(g, paretoPaths.get(minTurnsIdx), PARETO_MIN_TURNS, 4.0f, 0.9f, 0);
            }
            
            // Finally render max wideness path (green) on top
            renderParetoPathWithOffset(g, paretoPaths.get(maxWidenessIdx), PARETO_MAX_WIDENESS, 5.0f, 1.0f, 0);
            
            // Render source and destination markers (from first path)
            if (!paretoPaths.isEmpty() && paretoPaths.get(0).coordinates.size() >= 2) {
                List<double[]> firstPath = paretoPaths.get(0).coordinates;
                Point2D.Double srcPoint = converter.latLonToPixel(firstPath.get(0)[0], firstPath.get(0)[1]);
                Point2D.Double dstPoint = converter.latLonToPixel(
                    firstPath.get(firstPath.size() - 1)[0], 
                    firstPath.get(firstPath.size() - 1)[1]);
                renderMarker(g, srcPoint, SOURCE_COLOR, "S", true);
                renderMarker(g, dstPoint, DEST_COLOR, "D", true);
            }
            
            // Render legend for Pareto paths
            renderParetoLegend(g, maxWideness, minTurns, paretoPaths.size());
            
        } finally {
            g.dispose();
        }
    }
    
    /**
     * Render a single Pareto path with specified color, width, and perpendicular offset
     * The offset helps distinguish overlapping paths
     */
    private void renderParetoPathWithOffset(Graphics2D g, ParetoPathInfo pathInfo, Color color, float width, float alpha, int offset) {
        if (pathInfo.coordinates == null || pathInfo.coordinates.size() < 2) {
            System.out.println("[Render] Path " + pathInfo.pathIndex + " skipped - coords=" + 
                (pathInfo.coordinates == null ? "null" : pathInfo.coordinates.size()));
            return;
        }
        
        List<Point2D.Double> screenPoints = new ArrayList<>();
        for (double[] coord : pathInfo.coordinates) {
            Point2D.Double pt = converter.latLonToPixel(coord[0], coord[1]);
            screenPoints.add(pt);
        }
        
        String colorName = color.equals(PARETO_MAX_WIDENESS) ? "GREEN" : 
                          color.equals(PARETO_MIN_TURNS) ? "ORANGE" : "PURPLE";
        System.out.println("[Render] Path " + pathInfo.pathIndex + ": " + screenPoints.size() + 
            " points, color=" + colorName + ", wideRoad%=" + String.format("%.1f%%", pathInfo.wideRoadPercentage) + 
            ", turns=" + pathInfo.rightTurns);
        
        // Apply perpendicular offset if needed (to separate overlapping paths)
        if (offset != 0) {
            screenPoints = applyPerpendicularOffset(screenPoints, offset);
        }
        
        // Outer glow
        Color glowColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(40 * alpha));
        g.setColor(glowColor);
        g.setStroke(new BasicStroke(width + 6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < screenPoints.size() - 1; i++) {
            g.draw(new Line2D.Double(screenPoints.get(i), screenPoints.get(i + 1)));
        }
        
        // Border (white)
        g.setColor(new Color(255, 255, 255, (int)(180 * alpha)));
        g.setStroke(new BasicStroke(width + 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < screenPoints.size() - 1; i++) {
            g.draw(new Line2D.Double(screenPoints.get(i), screenPoints.get(i + 1)));
        }
        
        // Core path
        Color coreColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(255 * alpha));
        g.setColor(coreColor);
        g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < screenPoints.size() - 1; i++) {
            g.draw(new Line2D.Double(screenPoints.get(i), screenPoints.get(i + 1)));
        }
        
        // Draw endpoint markers (circles at start and end of each path)
        g.setColor(coreColor);
        Point2D.Double startPt = screenPoints.get(0);
        Point2D.Double endPt = screenPoints.get(screenPoints.size() - 1);
        g.fill(new Ellipse2D.Double(startPt.x - 5, startPt.y - 5, 10, 10));
        g.fill(new Ellipse2D.Double(endPt.x - 5, endPt.y - 5, 10, 10));
    }
    
    /**
     * Apply perpendicular offset to path points
     */
    private List<Point2D.Double> applyPerpendicularOffset(List<Point2D.Double> points, int offset) {
        if (points.size() < 2 || offset == 0) return points;
        
        List<Point2D.Double> offsetPoints = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            Point2D.Double p = points.get(i);
            double dx = 0, dy = 0;
            
            if (i < points.size() - 1) {
                Point2D.Double next = points.get(i + 1);
                dx = next.x - p.x;
                dy = next.y - p.y;
            } else {
                Point2D.Double prev = points.get(i - 1);
                dx = p.x - prev.x;
                dy = p.y - prev.y;
            }
            
            double len = Math.sqrt(dx * dx + dy * dy);
            if (len > 0) {
                // Perpendicular direction
                double px = -dy / len * offset;
                double py = dx / len * offset;
                offsetPoints.add(new Point2D.Double(p.x + px, p.y + py));
            } else {
                offsetPoints.add(p);
            }
        }
        return offsetPoints;
    }
    
    /**
     * Render a single Pareto path with specified color and width (no offset)
     */
    private void renderParetoPath(Graphics2D g, ParetoPathInfo pathInfo, Color color, float width, float alpha) {
        renderParetoPathWithOffset(g, pathInfo, color, width, alpha, 0);
    }
    
    /**
     * Render legend for Pareto paths
     */
    private void renderParetoLegend(Graphics2D g, double maxWideness, int minTurns, int pathCount) {
        int x = 10;
        int y = 10;
        int width = 200;
        int height = 100;
        
        // Background
        g.setColor(new Color(255, 255, 255, 230));
        g.fill(new RoundRectangle2D.Double(x, y, width, height, 10, 10));
        
        // Border
        g.setColor(new Color(100, 100, 100, 100));
        g.setStroke(new BasicStroke(1));
        g.draw(new RoundRectangle2D.Double(x, y, width, height, 10, 10));
        
        // Title
        g.setColor(new Color(50, 50, 50));
        g.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g.drawString("Pareto Paths (" + pathCount + ")", x + 10, y + 18);
        
        // Legend items
        g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        int itemY = y + 35;
        
        // Max wideness (green)
        g.setColor(PARETO_MAX_WIDENESS);
        g.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(x + 10, itemY, x + 35, itemY);
        g.setColor(new Color(50, 50, 50));
        g.drawString(String.format("Max Wide: %.2f%%", maxWideness), x + 45, itemY + 4);
        
        // Min turns (orange)
        itemY += 22;
        g.setColor(PARETO_MIN_TURNS);
        g.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(x + 10, itemY, x + 35, itemY);
        g.setColor(new Color(50, 50, 50));
        g.drawString(String.format("Min Turns: %d", minTurns), x + 45, itemY + 4);
        
        // Other paths (purple)
        itemY += 22;
        g.setColor(PARETO_OTHER);
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(x + 10, itemY, x + 35, itemY);
        g.setColor(new Color(50, 50, 50));
        g.drawString("Other Pareto paths", x + 45, itemY + 4);
    }
    
    /**
     * Render preview line between source and destination
     */
    public void renderPreviewLine(Graphics2D g, double[] sourceCoord, double[] destCoord) {
        Point2D.Double pSrc = converter.latLonToPixel(sourceCoord[0], sourceCoord[1]);
        Point2D.Double pDst = converter.latLonToPixel(destCoord[0], destCoord[1]);
        
        // Dashed line
        float[] dashPattern = {10, 8};
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 
                                    10f, dashPattern, animationPhase * 20));
        g.setColor(new Color(100, 100, 100, 180));
        g.draw(new Line2D.Double(pSrc, pDst));
        
        // Markers
        renderMarker(g, pSrc, SOURCE_COLOR, "S", true);
        renderMarker(g, pDst, DEST_COLOR, "D", true);
    }
    
    private void setupRenderingHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }
    
    private Color interpolateColor(Color c1, Color c2, float t) {
        t = Math.max(0, Math.min(1, t));
        int r = (int) (c1.getRed() + t * (c2.getRed() - c1.getRed()));
        int g = (int) (c1.getGreen() + t * (c2.getGreen() - c1.getGreen()));
        int b = (int) (c1.getBlue() + t * (c2.getBlue() - c1.getBlue()));
        return new Color(r, g, b);
    }
    
    // Setters for customization
    
    public void setPathStyle(PathStyle style) {
        this.pathStyle = style;
    }
    
    public void setMarkerStyle(MarkerStyle style) {
        this.markerStyle = style;
    }
    
    public void setPathWidth(float width) {
        this.pathWidth = width;
    }
    
    public void setWidePathWidth(float width) {
        this.widePathWidth = width;
    }
    
    public void setShowDirectionArrows(boolean show) {
        this.showDirectionArrows = show;
    }
    
    public void setShowDistanceLabels(boolean show) {
        this.showDistanceLabels = show;
    }
    
    public void setShowNodeMarkers(boolean show) {
        this.showNodeMarkers = show;
    }
    
    public void setAnimationPhase(float phase) {
        this.animationPhase = phase;
    }
    
    public PathStyle getPathStyle() {
        return pathStyle;
    }
    
    public MarkerStyle getMarkerStyle() {
        return markerStyle;
    }
}
