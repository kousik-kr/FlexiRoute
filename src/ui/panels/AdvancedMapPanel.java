package ui.panels;

import ui.components.ModernButton;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

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
        int startIdx = currentPage * nodesPerPage;
        int endIdx = Math.min(startIdx + nodesPerPage, pathNodes.size());
        
        for (int i = startIdx; i < endIdx - 1; i++) {
            int x1 = 50 + (i - startIdx) * 60;
            int y1 = 300;
            int x2 = 50 + (i - startIdx + 1) * 60;
            int y2 = 300;
            
            boolean isWideEdge = wideEdges != null && wideEdges.contains(i);
            
            // Draw edge
            g2d.setStroke(new BasicStroke(isWideEdge ? 4 : 2));
            g2d.setColor(isWideEdge ? new Color(255, 87, 34) : new Color(100, 100, 100));
            g2d.drawLine(x1, y1, x2, y2);
        }
        
        // Draw nodes
        for (int i = startIdx; i < endIdx; i++) {
            int x = 50 + (i - startIdx) * 60;
            int y = 300;
            
            boolean isStart = i == 0;
            boolean isEnd = i == pathNodes.size() - 1;
            
            Color nodeColor = isStart ? new Color(76, 175, 80) : 
                             isEnd ? new Color(244, 67, 54) : 
                             new Color(33, 150, 243);
            
            g2d.setColor(nodeColor);
            g2d.fillOval(x - 15, y - 15, 30, 30);
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(x - 15, y - 15, 30, 30);
            
            // Node label
            String label = isStart ? "S" : isEnd ? "E" : String.valueOf(pathNodes.get(i));
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x - fm.stringWidth(label) / 2;
            int textY = y + fm.getAscent() / 2 - 2;
            g2d.drawString(label, textX, textY);
        }
    }
    
    private void renderNeonGlow(Graphics2D g2d) {
        // Similar to classic but with glow effects
        for (int i = 0; i < pathNodes.size() - 1; i++) {
            int x1 = 50 + i * 60;
            int y1 = 300;
            int x2 = 50 + (i + 1) * 60;
            int y2 = 300;
            
            // Glow effect (multiple overlays)
            for (int j = 6; j >= 2; j--) {
                int alpha = 40 - j * 5;
                g2d.setStroke(new BasicStroke(j * 2));
                g2d.setColor(new Color(0, 255, 255, alpha));
                g2d.drawLine(x1, y1, x2, y2);
            }
        }
        
        // Glowing nodes
        for (int i = 0; i < pathNodes.size(); i++) {
            int x = 50 + i * 60;
            int y = 300;
            
            // Outer glow
            for (int r = 25; r >= 15; r--) {
                int alpha = 150 - (25 - r) * 10;
                g2d.setColor(new Color(0, 255, 255, alpha));
                g2d.fillOval(x - r, y - r, r * 2, r * 2);
            }
            
            // Core node
            g2d.setColor(new Color(0, 255, 255));
            g2d.fillOval(x - 12, y - 12, 24, 24);
        }
    }
    
    private void renderGradientFlow(Graphics2D g2d) {
        for (int i = 0; i < pathNodes.size() - 1; i++) {
            int x1 = 50 + i * 60;
            int y1 = 300;
            int x2 = 50 + (i + 1) * 60;
            int y2 = 300;
            
            float ratio = (float) i / pathNodes.size();
            Color c1 = new Color(33, 150, 243);
            Color c2 = new Color(255, 87, 34);
            
            GradientPaint gradient = new GradientPaint(x1, y1, c1, x2, y2, c2);
            g2d.setPaint(gradient);
            g2d.setStroke(new BasicStroke(4));
            g2d.drawLine(x1, y1, x2, y2);
        }
        
        // Gradient nodes
        for (int i = 0; i < pathNodes.size(); i++) {
            int x = 50 + i * 60;
            int y = 300;
            
            float ratio = (float) i / (pathNodes.size() - 1);
            int r = (int) (33 + ratio * (255 - 33));
            int g = (int) (150 - ratio * (150 - 87));
            int b = (int) (243 - ratio * (243 - 34));
            
            g2d.setColor(new Color(r, g, b));
            g2d.fillOval(x - 15, y - 15, 30, 30);
        }
    }
    
    private void render3DEffect(Graphics2D g2d) {
        // Similar to classic with shadow effects
        int shadowOffset = 3;
        
        // Draw shadows first
        for (int i = 0; i < pathNodes.size(); i++) {
            int x = 50 + i * 60;
            int y = 300;
            
            g2d.setColor(new Color(0, 0, 0, 60));
            g2d.fillOval(x - 15 + shadowOffset, y - 15 + shadowOffset, 30, 30);
        }
        
        // Draw nodes with 3D effect
        for (int i = 0; i < pathNodes.size(); i++) {
            int x = 50 + i * 60;
            int y = 300;
            
            // Base
            g2d.setColor(new Color(33, 150, 243));
            g2d.fillOval(x - 15, y - 15, 30, 30);
            
            // Highlight
            GradientPaint highlight = new GradientPaint(
                x - 10, y - 10, new Color(150, 200, 255),
                x + 10, y + 10, new Color(33, 150, 243)
            );
            g2d.setPaint(highlight);
            g2d.fillOval(x - 12, y - 12, 24, 24);
        }
    }
    
    private void renderPulseAnimation(Graphics2D g2d) {
        // Animated traveling marker
        double progress = animationProgress * (pathNodes.size() - 1);
        int currentIndex = (int) progress;
        double fraction = progress - currentIndex;
        
        // Draw path
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(2));
        for (int i = 0; i < pathNodes.size() - 1; i++) {
            int x1 = 50 + i * 60;
            int y1 = 300;
            int x2 = 50 + (i + 1) * 60;
            int y2 = 300;
            g2d.drawLine(x1, y1, x2, y2);
        }
        
        // Draw nodes
        for (int i = 0; i < pathNodes.size(); i++) {
            int x = 50 + i * 60;
            int y = 300;
            
            boolean isStart = i == 0;
            boolean isEnd = i == pathNodes.size() - 1;
            
            Color nodeColor = isStart ? new Color(76, 175, 80) : 
                             isEnd ? new Color(244, 67, 54) : 
                             new Color(150, 150, 150);
            
            g2d.setColor(nodeColor);
            g2d.fillOval(x - 10, y - 10, 20, 20);
        }
        
        // Animated marker
        if (currentIndex < pathNodes.size() - 1) {
            int x1 = 50 + currentIndex * 60;
            int x2 = 50 + (currentIndex + 1) * 60;
            int markerX = (int) (x1 + (x2 - x1) * fraction);
            int markerY = 300;
            
            // Pulsing glow
            int pulseSize = (int) (20 + 10 * Math.sin(animationFrame * 0.2));
            g2d.setColor(new Color(255, 215, 0, 100));
            g2d.fillOval(markerX - pulseSize/2, markerY - pulseSize/2, pulseSize, pulseSize);
            
            g2d.setColor(new Color(255, 215, 0));
            g2d.fillOval(markerX - 8, markerY - 8, 16, 16);
        }
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
}
