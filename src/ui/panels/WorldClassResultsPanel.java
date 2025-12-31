package ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Optional;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * World-Class Results Dashboard
 * - Animated result cards
 * - Path statistics visualization
 * - Export options
 * - Detailed breakdown
 */
public class WorldClassResultsPanel extends JPanel {
    
    // 🌈 VIBRANT RAINBOW COLOR PALETTE - Maximum Visual Impact!
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
    
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BG_SURFACE = new Color(248, 250, 252);
    
    // Components
    private JPanel cardsContainer;
    private JTextArea pathDetailsArea;
    private JProgressBar widePathBar;
    private JLabel statusLabel, timeLabel, nodesLabel, costLabel;
    private JButton exportJsonButton, exportCsvButton, copyButton, clearButton;
    
    // State
    private ResultData currentResult;
    private Timer animationTimer;
    private float animProgress = 0f;
    
    public WorldClassResultsPanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(BG_SURFACE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
    }
    
    private void initComponents() {
        // Header
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);
        
        // Main content
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        
        // Stats cards
        cardsContainer = createStatsCards();
        content.add(cardsContainer);
        content.add(Box.createVerticalStrut(15));
        
        // Wide path progress
        JPanel progressSection = createProgressSection();
        content.add(progressSection);
        content.add(Box.createVerticalStrut(15));
        
        // Path details
        JPanel detailsSection = createDetailsSection();
        content.add(detailsSection);
        content.add(Box.createVerticalStrut(15));
        
        // Export options
        JPanel exportSection = createExportSection();
        content.add(exportSection);
        
        content.add(Box.createVerticalGlue());
        
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
        
        // Show empty state initially
        showEmptyState();
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JLabel icon = new JLabel("🌟");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        
        JLabel title = new JLabel("📊 Results Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(VIVID_PURPLE);
        
        statusLabel = new JLabel("🚀 Run a query to see results");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 19));
        statusLabel.setForeground(TEXT_SECONDARY);
        
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(statusLabel);
        
        header.add(icon, BorderLayout.WEST);
        header.add(Box.createHorizontalStrut(15));
        header.add(titlePanel, BorderLayout.CENTER);
        
        return header;
    }
    
    private JPanel createStatsCards() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 18, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(createStatCard("⏱️", "Time", "--", ELECTRIC_BLUE));
        panel.add(createStatCard("📍", "Nodes", "--", NEON_GREEN));
        panel.add(createStatCard("💰", "Cost", "--", SUNSET_ORANGE));
        panel.add(createStatCard("🛣️", "Wide %", "--", HOT_PINK));
        
        return panel;
    }
    
    private JPanel createStatCard(String icon, String label, String value, Color color) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient card background with color tint
                java.awt.GradientPaint bgGradient = new java.awt.GradientPaint(
                    0, 0, Color.WHITE,
                    0, getHeight(), new Color(color.getRed(), color.getGreen(), color.getBlue(), 15)
                );
                g2d.setPaint(bgGradient);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));
                
                // Colorful gradient accent at top
                java.awt.GradientPaint topAccent = new java.awt.GradientPaint(
                    0, 0, color,
                    getWidth(), 0, new Color(
                        Math.min(255, color.getRed() + 50),
                        Math.min(255, color.getGreen() + 50),
                        Math.min(255, color.getBlue() + 50)
                    )
                );
                g2d.setPaint(topAccent);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 6, 6, 6));
                
                // Colored border
                g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60));
                g2d.setStroke(new java.awt.BasicStroke(2));
                g2d.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 16, 16));
                
                g2d.dispose();
            }
        };
        
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(18, 18, 18, 18)
        ));
        
        JLabel iconLabel = new JLabel(icon + " " + label);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        iconLabel.setForeground(TEXT_SECONDARY);
        iconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueLabel.setName(label);  // Use name for identification
        
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);
        
        return card;
    }
    
    private JPanel createProgressSection() {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        JLabel label = new JLabel("🛣️ Wide Path Coverage");
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setForeground(LIME_GREEN);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(label);
        section.add(Box.createVerticalStrut(12));
        
        widePathBar = new JProgressBar(0, 100) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background track
                g2d.setColor(new Color(226, 232, 240));
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                
                // Gradient fill
                int fillWidth = (int) ((getValue() / 100.0) * getWidth());
                if (fillWidth > 0) {
                    java.awt.GradientPaint gp = new java.awt.GradientPaint(
                        0, 0, NEON_GREEN,
                        getWidth(), 0, OCEAN_TEAL
                    );
                    g2d.setPaint(gp);
                    g2d.fill(new RoundRectangle2D.Float(0, 0, fillWidth, getHeight(), 16, 16));
                }
                
                // Text
                g2d.setColor(fillWidth > getWidth()/2 ? Color.WHITE : TEXT_PRIMARY);
                g2d.setFont(getFont());
                java.awt.FontMetrics fm = g2d.getFontMetrics();
                String text = getString();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(text, x, y);
                
                g2d.dispose();
            }
        };
        widePathBar.setValue(0);
        widePathBar.setStringPainted(true);
        widePathBar.setString("0% wide roads");
        widePathBar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        widePathBar.setBorder(null);
        widePathBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        widePathBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        widePathBar.setPreferredSize(new Dimension(0, 32));
        
        section.add(widePathBar);
        
        return section;
    }
    
    private JPanel createDetailsSection() {
        JPanel section = new JPanel(new BorderLayout(0, 12));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel label = new JLabel("📋 Path Details");
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setForeground(ROYAL_INDIGO);
        section.add(label, BorderLayout.NORTH);
        
        pathDetailsArea = new JTextArea(10, 40);
        pathDetailsArea.setFont(new Font("Consolas", Font.PLAIN, 18));
        pathDetailsArea.setEditable(false);
        pathDetailsArea.setLineWrap(true);
        pathDetailsArea.setWrapStyleWord(true);
        pathDetailsArea.setBackground(new Color(248, 250, 255));
        pathDetailsArea.setForeground(TEXT_PRIMARY);
        pathDetailsArea.setBorder(new EmptyBorder(16, 16, 16, 16));
        
        JScrollPane scroll = new JScrollPane(pathDetailsArea);
        scroll.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(0, 0, 0, 0)
        ));
        scroll.setPreferredSize(new Dimension(0, 220));
        
        section.add(scroll, BorderLayout.CENTER);
        
        return section;
    }
    
    private JPanel createExportSection() {
        JPanel section = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        
        exportJsonButton = createColorfulExportButton("📄 Export JSON", ELECTRIC_BLUE, e -> exportAsJson());
        exportCsvButton = createColorfulExportButton("📊 Export CSV", NEON_GREEN, e -> exportAsCsv());
        copyButton = createColorfulExportButton("📋 Copy", VIVID_PURPLE, e -> copyToClipboard());
        clearButton = createColorfulExportButton("🗑️ Clear", CORAL_PINK, e -> clearOutput());
        clearButton.setEnabled(true);
        
        section.add(exportJsonButton);
        section.add(exportCsvButton);
        section.add(copyButton);
        section.add(clearButton);
        
        return section;
    }
    
    private JButton createColorfulExportButton(String text, Color color, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color bgColor = isEnabled() ? 
                    (getModel().isRollover() ? color : new Color(color.getRed(), color.getGreen(), color.getBlue(), 35)) :
                    new Color(200, 200, 200, 50);
                g2d.setColor(bgColor);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                
                // Border
                g2d.setColor(isEnabled() ? color : new Color(180, 180, 180));
                g2d.setStroke(new java.awt.BasicStroke(2));
                g2d.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 12, 12));
                
                g2d.dispose();
                
                // Text
                g.setColor(isEnabled() ? (getModel().isRollover() ? Color.WHITE : color) : new Color(150, 150, 150));
                g.setFont(getFont());
                java.awt.FontMetrics fm = g.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g.drawString(getText(), x, y);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 48));
        btn.addActionListener(action);
        btn.setEnabled(false);
        return btn;
    }
    
    private void showEmptyState() {
        pathDetailsArea.setText("""
            ╔══════════════════════════════════════════════════════════╗
            ║                                                          ║
            ║     🗺️  Welcome to FlexRoute Navigator!                   ║
            ║                                                          ║
            ║     Configure your query in the left panel and click     ║
            ║     "Find FlexRoute" to discover the optimal route.      ║
            ║                                                          ║
            ║     The algorithm will find a path that maximizes        ║
            ║     wide road usage within your travel budget.           ║
            ║                                                          ║
            ╚══════════════════════════════════════════════════════════╝
            
            Quick Tips:
            • Enable 'Best Bounds' heuristic for faster results
            • Increase budget to explore more route options
            """);
    }
    
    // === PUBLIC API ===
    
    public void displayResult(ResultData result) {
        this.currentResult = result;
        
        if (result == null) {
            showEmptyState();
            return;
        }
        
        // Update status based on routing mode
        if (result.hasParetoOptimalPaths()) {
            statusLabel.setText("✅ Found " + result.getParetoPathCount() + " Pareto optimal paths!");
            statusLabel.setForeground(VIVID_PURPLE);
        } else {
            statusLabel.setText(result.isPathFound() ? "✅ Path found successfully!" : "❌ No path found");
            statusLabel.setForeground(result.isPathFound() ? NEON_GREEN : new Color(244, 67, 54));
        }
        
        // Update stat cards
        updateStatCard("Time", String.format("%.2f ms", result.getExecutionTime()));
        updateStatCard("Nodes", String.valueOf(result.getPathLength()));
        updateStatCard("Cost", String.format("%.1f", result.getTotalCost()));
        
        int widePercent = result.getPathLength() > 0 
            ? (int) (100.0 * result.getWideEdgeCount() / Math.max(1, result.getPathLength() - 1))
            : 0;
        updateStatCard("Wide %", widePercent + "%");
        
        // Update progress bar
        widePathBar.setValue(widePercent);
        widePathBar.setString(widePercent + "% wide roads (" + result.getWideEdgeCount() + " edges)");
        
        // Update details
        StringBuilder details = new StringBuilder();
        details.append("═══════════════════ QUERY RESULT ═══════════════════\n\n");
        
        // Show routing mode
        if (result.getRoutingModeName() != null) {
            details.append("🎯 ROUTING MODE: ").append(result.getRoutingModeName()).append("\n\n");
        }
        
        details.append("📍 ROUTE SUMMARY\n");
        details.append("   Source:       ").append(result.getSource()).append("\n");
        details.append("   Destination:  ").append(result.getDestination()).append("\n");
        details.append("   Budget:       ").append(result.getBudget()).append(" units\n");
        int depTime = result.getDepartureTime();
        details.append("   Departure:    ").append(String.format("%02d:%02d", depTime/60, depTime%60)).append("\n");
        
        // Display suggested departure time from algorithm
        double suggestedDep = result.getSuggestedDepartureTime();
        if (suggestedDep > 0) {
            int sugHours = (int)(suggestedDep / 60);
            int sugMins = (int)(suggestedDep % 60);
            details.append("   ⭐ Optimal Departure: ").append(String.format("%02d:%02d", sugHours, sugMins))
                   .append(" (").append(String.format("%.1f", suggestedDep)).append(" mins)\n");
        }
        details.append("\n");
        
        details.append("📊 STATISTICS\n");
        details.append("   Path Length:  ").append(result.getPathLength()).append(" nodes\n");
        details.append("   Total Cost:   ").append(String.format("%.2f", result.getTotalCost())).append(" units\n");
        details.append("   Wide Edges:   ").append(result.getWideEdgeCount()).append("\n");
        details.append("   Wide Score:   ").append(String.format("%.4f", result.getWideScore())).append("\n");
        details.append("   Right Turns:  ").append(result.getRightTurns()).append("\n");
        details.append("   Exec Time:    ").append(String.format("%.2f", result.getExecutionTime())).append(" ms\n\n");
        
        // Display Pareto optimal paths if available
        if (result.hasParetoOptimalPaths()) {
            details.append("═══════════════════════════════════════════════════════\n");
            details.append("🎯 ALL PARETO OPTIMAL PATHS (").append(result.getParetoPathCount()).append(" paths)\n");
            details.append("═══════════════════════════════════════════════════════\n\n");
            details.append("These paths represent the trade-off between maximizing\n");
            details.append("wideness and minimizing right turns. No path is strictly\n");
            details.append("better than another in both objectives.\n\n");
            
            int pathNum = 1;
            for (ResultData paretoPath : result.getParetoOptimalPaths()) {
                int paretoWidePercent = paretoPath.getPathLength() > 0 
                    ? (int) (100.0 * paretoPath.getWideEdgeCount() / Math.max(1, paretoPath.getPathLength() - 1))
                    : 0;
                
                details.append("┌─────────────────────────────────────────────────┐\n");
                details.append("│ PATH #").append(pathNum++).append("\n");
                details.append("├─────────────────────────────────────────────────┤\n");
                details.append("│  Wide Score:   ").append(String.format("%.4f", paretoPath.getWideScore()));
                details.append(" (").append(paretoWidePercent).append("% wide roads)\n");
                details.append("│  Right Turns:  ").append(paretoPath.getRightTurns()).append("\n");
                details.append("│  Path Length:  ").append(paretoPath.getPathLength()).append(" nodes\n");
                details.append("│  Total Cost:   ").append(String.format("%.2f", paretoPath.getTotalCost())).append("\n");
                
                // Show path nodes (abbreviated)
                List<Integer> pathNodes = paretoPath.getPathNodes();
                if (pathNodes != null && !pathNodes.isEmpty()) {
                    details.append("│  Path:        ");
                    if (pathNodes.size() <= 5) {
                        for (int i = 0; i < pathNodes.size(); i++) {
                            details.append(pathNodes.get(i));
                            if (i < pathNodes.size() - 1) details.append(" → ");
                        }
                    } else {
                        details.append(pathNodes.get(0)).append(" → ")
                               .append(pathNodes.get(1)).append(" → ... → ")
                               .append(pathNodes.get(pathNodes.size() - 2)).append(" → ")
                               .append(pathNodes.get(pathNodes.size() - 1));
                    }
                    details.append("\n");
                }
                details.append("└─────────────────────────────────────────────────┘\n\n");
            }
        }
        
        details.append("🛤️ PRIMARY PATH NODES (").append(result.getPathLength()).append(" total)\n");
        List<Integer> path = result.getPathNodes();
        List<double[]> coords = result.getPathCoordinates();
        if (path != null && !path.isEmpty()) {
            // Show node IDs with coordinates
            details.append("   [Step] NodeID → (Latitude, Longitude)\n");
            details.append("   ────────────────────────────────────\n");
            for (int i = 0; i < path.size(); i++) {
                int nodeId = path.get(i);
                String coordStr = "";
                if (coords != null && i < coords.size()) {
                    double[] coord = coords.get(i);
                    coordStr = String.format(" → (%.6f, %.6f)", coord[0], coord[1]);
                }
                String marker = "";
                if (i == 0) marker = " [START]";
                else if (i == path.size() - 1) marker = " [END]";
                
                details.append(String.format("   [%3d] %d%s%s\n", i, nodeId, coordStr, marker));
            }
        }
        details.append("\n");
        
        if (result.getPathCoordinates() != null && !result.getPathCoordinates().isEmpty()) {
            details.append("🌍 COORDINATES (lat, lon)\n");
            int count = 0;
            for (double[] coord : result.getPathCoordinates()) {
                details.append(String.format("   [%d] %.6f, %.6f\n", count++, coord[0], coord[1]));
                if (count > 20 && result.getPathCoordinates().size() > 25) {
                    details.append("   ... and ").append(result.getPathCoordinates().size() - 21).append(" more coordinates\n");
                    break;
                }
            }
        }
        
        details.append("\n═══════════════════════════════════════════════════════\n");
        
        pathDetailsArea.setText(details.toString());
        pathDetailsArea.setCaretPosition(0);
        
        // Enable export buttons
        exportJsonButton.setEnabled(true);
        exportCsvButton.setEnabled(true);
        copyButton.setEnabled(true);
    }
    
    private void updateStatCard(String name, String value) {
        findStatCardLabel(cardsContainer, name).ifPresent(label -> label.setText(value));
    }
    
    private Optional<JLabel> findStatCardLabel(Container container, String name) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel label && name.equals(label.getName())) {
                return Optional.of(label);
            }
            if (comp instanceof Container) {
                Optional<JLabel> found = findStatCardLabel((Container) comp, name);
                if (found.isPresent()) return found;
            }
        }
        return Optional.empty();
    }
    
    private void clearOutput() {
        currentResult = null;
        showEmptyState();
        
        // Reset stat cards
        updateStatCard("Time", "--");
        updateStatCard("Nodes", "--");
        updateStatCard("Cost", "--");
        updateStatCard("Wide %", "--");
        
        // Reset progress bar
        widePathBar.setValue(0);
        widePathBar.setString("0% wide roads");
        
        // Reset status
        statusLabel.setText("🚀 Run a query to see results");
        statusLabel.setForeground(TEXT_SECONDARY);
        
        // Disable export buttons
        exportJsonButton.setEnabled(false);
        exportCsvButton.setEnabled(false);
        copyButton.setEnabled(false);
    }
    
    private void exportAsJson() {
        if (currentResult == null) return;
        
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("path_result.json"));
        
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                StringBuilder json = new StringBuilder();
                json.append("{\n");
                json.append("  \"source\": ").append(currentResult.getSource()).append(",\n");
                json.append("  \"destination\": ").append(currentResult.getDestination()).append(",\n");
                json.append("  \"pathLength\": ").append(currentResult.getPathLength()).append(",\n");
                json.append("  \"totalCost\": ").append(currentResult.getTotalCost()).append(",\n");
                json.append("  \"wideEdges\": ").append(currentResult.getWideEdgeCount()).append(",\n");
                json.append("  \"executionTimeMs\": ").append(currentResult.getExecutionTime()).append(",\n");
                json.append("  \"path\": ").append(currentResult.getPathNodes()).append("\n");
                json.append("}\n");
                
                java.nio.file.Files.writeString(chooser.getSelectedFile().toPath(), json.toString());
                JOptionPane.showMessageDialog(this, "Exported successfully!", "Export", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void exportAsCsv() {
        if (currentResult == null) return;
        
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("path_coordinates.csv"));
        
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                StringBuilder csv = new StringBuilder();
                csv.append("index,node_id,latitude,longitude\n");
                
                List<Integer> path = currentResult.getPathNodes();
                List<double[]> coords = currentResult.getPathCoordinates();
                
                if (path != null && coords != null) {
                    for (int i = 0; i < path.size() && i < coords.size(); i++) {
                        csv.append(i).append(",");
                        csv.append(path.get(i)).append(",");
                        csv.append(coords.get(i)[0]).append(",");
                        csv.append(coords.get(i)[1]).append("\n");
                    }
                }
                
                java.nio.file.Files.writeString(chooser.getSelectedFile().toPath(), csv.toString());
                JOptionPane.showMessageDialog(this, "Exported successfully!", "Export", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void copyToClipboard() {
        if (currentResult == null) return;
        
        String text = pathDetailsArea.getText();
        java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(text);
        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        
        JOptionPane.showMessageDialog(this, "Copied to clipboard!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void showLoading() {
        statusLabel.setText("⏳ Processing query...");
        statusLabel.setForeground(VIVID_PURPLE);
        pathDetailsArea.setText("""
            
            ╔════════════════════════════════════════════════════════╗
            ║                                                        ║
            ║     ⏳  Finding the optimal FlexRoute path...            ║
            ║                                                        ║
            ║     The bidirectional labeling algorithm is            ║
            ║     exploring possible routes to maximize              ║
            ║     wide road coverage within your budget.             ║
            ║                                                        ║
            ╚════════════════════════════════════════════════════════╝
            """);
    }
    
    public void showError(String message) {
        statusLabel.setText("❌ Query failed");
        statusLabel.setForeground(new Color(244, 67, 54));
        pathDetailsArea.setText("ERROR: " + message);
    }
}
