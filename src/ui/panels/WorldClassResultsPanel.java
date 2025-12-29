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

import javax.swing.BorderFactory;
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
    
    // Colors
    private static final Color PRIMARY = new Color(33, 150, 243);
    private static final Color SUCCESS = new Color(76, 175, 80);
    private static final Color WARNING = new Color(255, 152, 0);
    private static final Color INFO = new Color(156, 39, 176);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER = new Color(230, 230, 230);
    
    // Components
    private JPanel cardsContainer;
    private JTextArea pathDetailsArea;
    private JProgressBar widePathBar;
    private JLabel statusLabel, timeLabel, nodesLabel, costLabel;
    private JButton exportJsonButton, exportCsvButton, copyButton;
    
    // State
    private ResultData currentResult;
    private Timer animationTimer;
    private float animProgress = 0f;
    
    public WorldClassResultsPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        
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
        
        JLabel icon = new JLabel("📊");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        
        JLabel title = new JLabel("Results Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(40, 40, 40));
        
        statusLabel = new JLabel("Run a query to see results");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(120, 120, 120));
        
        titlePanel.add(title);
        titlePanel.add(statusLabel);
        
        header.add(icon, BorderLayout.WEST);
        header.add(Box.createHorizontalStrut(15));
        header.add(titlePanel, BorderLayout.CENTER);
        
        return header;
    }
    
    private JPanel createStatsCards() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 12, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(createStatCard("⏱️", "Time", "--", PRIMARY));
        panel.add(createStatCard("📍", "Nodes", "--", SUCCESS));
        panel.add(createStatCard("💰", "Cost", "--", WARNING));
        panel.add(createStatCard("🛤️", "Wide %", "--", INFO));
        
        return panel;
    }
    
    private JPanel createStatCard(String icon, String label, String value, Color color) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_BG);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                
                // Color accent at top
                g2d.setColor(color);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 4, 4, 4));
                
                g2d.dispose();
            }
        };
        
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel iconLabel = new JLabel(icon + " " + label);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        iconLabel.setForeground(new Color(100, 100, 100));
        iconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueLabel.setName(label);  // Use name for identification
        
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        
        return card;
    }
    
    private JPanel createProgressSection() {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        JLabel label = new JLabel("🛤️ Wide Path Coverage");
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(80, 80, 80));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(label);
        section.add(Box.createVerticalStrut(8));
        
        widePathBar = new JProgressBar(0, 100);
        widePathBar.setValue(0);
        widePathBar.setStringPainted(true);
        widePathBar.setString("0% wide roads");
        widePathBar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        widePathBar.setForeground(SUCCESS);
        widePathBar.setBackground(new Color(230, 230, 230));
        widePathBar.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        widePathBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        widePathBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        section.add(widePathBar);
        
        return section;
    }
    
    private JPanel createDetailsSection() {
        JPanel section = new JPanel(new BorderLayout(0, 8));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel label = new JLabel("📋 Path Details");
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(80, 80, 80));
        section.add(label, BorderLayout.NORTH);
        
        pathDetailsArea = new JTextArea(10, 40);
        pathDetailsArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        pathDetailsArea.setEditable(false);
        pathDetailsArea.setLineWrap(true);
        pathDetailsArea.setWrapStyleWord(true);
        pathDetailsArea.setBackground(new Color(250, 250, 250));
        pathDetailsArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scroll = new JScrollPane(pathDetailsArea);
        scroll.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(0, 0, 0, 0)
        ));
        scroll.setPreferredSize(new Dimension(0, 200));
        
        section.add(scroll, BorderLayout.CENTER);
        
        return section;
    }
    
    private JPanel createExportSection() {
        JPanel section = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        exportJsonButton = createExportButton("📄 Export JSON", e -> exportAsJson());
        exportCsvButton = createExportButton("📊 Export CSV", e -> exportAsCsv());
        copyButton = createExportButton("📋 Copy to Clipboard", e -> copyToClipboard());
        
        section.add(exportJsonButton);
        section.add(exportCsvButton);
        section.add(copyButton);
        
        return section;
    }
    
    private JButton createExportButton(String text, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(new Color(245, 245, 245));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btn.addActionListener(action);
        btn.setEnabled(false);
        return btn;
    }
    
    private void showEmptyState() {
        pathDetailsArea.setText("""
            ╔══════════════════════════════════════════════════════════╗
            ║                                                          ║
            ║     🗺️  Welcome to Wide-Path Navigator!                   ║
            ║                                                          ║
            ║     Configure your query in the left panel and click     ║
            ║     "Find Wide Path" to discover the optimal route.      ║
            ║                                                          ║
            ║     The algorithm will find a path that maximizes        ║
            ║     wide road usage within your travel budget.           ║
            ║                                                          ║
            ╚══════════════════════════════════════════════════════════╝
            
            Quick Tips:
            • Use presets for common journey types
            • Enable 'Best Bounds' heuristic for faster results
            • Increase budget to explore more route options
            • Use Random to discover interesting paths
            """);
    }
    
    // === PUBLIC API ===
    
    public void displayResult(ResultData result) {
        this.currentResult = result;
        
        if (result == null) {
            showEmptyState();
            return;
        }
        
        // Update status
        statusLabel.setText(result.isPathFound() ? "✅ Path found successfully!" : "❌ No path found");
        statusLabel.setForeground(result.isPathFound() ? SUCCESS : new Color(244, 67, 54));
        
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
        
        details.append("📍 ROUTE SUMMARY\n");
        details.append("   Source:       ").append(result.getSource()).append("\n");
        details.append("   Destination:  ").append(result.getDestination()).append("\n");
        details.append("   Budget:       ").append(result.getBudget()).append(" units\n\n");
        
        details.append("📊 STATISTICS\n");
        details.append("   Path Length:  ").append(result.getPathLength()).append(" nodes\n");
        details.append("   Total Cost:   ").append(String.format("%.2f", result.getTotalCost())).append(" units\n");
        details.append("   Wide Edges:   ").append(result.getWideEdgeCount()).append("\n");
        details.append("   Exec Time:    ").append(String.format("%.2f", result.getExecutionTime())).append(" ms\n\n");
        
        details.append("🛤️ PATH NODES (").append(result.getPathLength()).append(" total)\n");
        details.append("   ");
        List<Integer> path = result.getPathNodes();
        if (path != null) {
            for (int i = 0; i < path.size(); i++) {
                details.append(path.get(i));
                if (i < path.size() - 1) {
                    details.append(" → ");
                    if ((i + 1) % 8 == 0) {
                        details.append("\n   ");
                    }
                }
            }
        }
        details.append("\n\n");
        
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
        statusLabel.setForeground(PRIMARY);
        pathDetailsArea.setText("""
            
            ╔════════════════════════════════════════════════════════╗
            ║                                                        ║
            ║     ⏳  Finding the optimal wide path...                ║
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
