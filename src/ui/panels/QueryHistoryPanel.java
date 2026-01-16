package ui.panels;

import models.QueryResult;
import managers.QueryHistoryManager;
import ui.components.LogViewerWindow;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.time.format.DateTimeFormatter;

/**
 * 📜 Enhanced Query History Panel
 * 
 * Displays comprehensive query history with:
 * - Optimal departure time column
 * - Log file reference with click-to-view functionality
 * - Beautiful modern design with custom cell renderers
 * - Advanced sorting and filtering
 */
public class QueryHistoryPanel extends JPanel {
    
    // Modern color scheme
    private static final Color BG_PRIMARY = new Color(250, 252, 255);
    private static final Color BG_HEADER = new Color(240, 242, 248);
    private static final Color ACCENT_BLUE = new Color(66, 133, 244);
    private static final Color ACCENT_GREEN = new Color(52, 168, 83);
    private static final Color ACCENT_PURPLE = new Color(156, 39, 176);
    private static final Color ACCENT_ORANGE = new Color(255, 152, 0);
    private static final Color TEXT_PRIMARY = new Color(32, 33, 36);
    private static final Color TEXT_SECONDARY = new Color(95, 99, 104);
    private static final Color BORDER = new Color(218, 220, 224);
    private static final Color ROW_HOVER = new Color(232, 240, 254);
    private static final Color ROW_ALT = new Color(248, 249, 250);
    
    private final QueryHistoryManager historyManager;
    private final JTable historyTable;
    private final DefaultTableModel tableModel;
    private JLabel statsLabel;  // Not final - initialized in createHeaderPanel()
    private int hoveredRow = -1;

    public QueryHistoryPanel(QueryHistoryManager historyManager) {
        this.historyManager = historyManager;
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header Panel
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Table with enhanced columns including Optimal Departure and Log File
        String[] columns = {
            "⏰ Time", 
            "📍 Source", 
            "🎯 Dest", 
            "🚗 Departure",
            "⭐ Optimal Dep.",  // New: Optimal departure time
            "💰 Budget", 
            "⏱️ Travel Time",
            "📏 Distance",      // New: Total distance
            "✅ Status", 
            "⚡ Exec (ms)",
            "📜 Log"            // New: Log file reference
        };
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 10; // Only Log column is clickable
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                return String.class; // All columns are strings for simplicity
            }
        };

        historyTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                
                if (!isRowSelected(row)) {
                    if (row == hoveredRow) {
                        comp.setBackground(ROW_HOVER);
                    } else {
                        comp.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                    }
                }
                
                return comp;
            }
        };
        
        setupTable();
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        add(scrollPane, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        refreshTable();
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(20, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(250, 252, 255),
                    getWidth(), 0, new Color(240, 245, 255)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                g2d.dispose();
            }
        };
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Title with icon
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel("📜 Query History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(ACCENT_PURPLE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Click on a log file to view detailed results");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitleLabel);
        
        header.add(titlePanel, BorderLayout.WEST);
        
        // Stats panel
        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statsLabel.setForeground(TEXT_SECONDARY);
        header.add(statsLabel, BorderLayout.EAST);
        
        return header;
    }
    
    private void setupTable() {
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        historyTable.setRowHeight(40);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setShowGrid(false);
        historyTable.setIntercellSpacing(new Dimension(0, 0));
        historyTable.setAutoCreateRowSorter(true);
        historyTable.setSelectionBackground(new Color(66, 133, 244, 40));
        historyTable.setSelectionForeground(TEXT_PRIMARY);
        
        // Header styling
        JTableHeader header = historyTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(BG_HEADER);
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_PURPLE));
        
        // Column widths
        TableColumnModel columnModel = historyTable.getColumnModel();
        int[] widths = {80, 70, 70, 85, 95, 70, 90, 80, 85, 80, 80};
        for (int i = 0; i < widths.length && i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setPreferredWidth(widths[i]);
        }
        
        // Custom cell renderers
        historyTable.setDefaultRenderer(Object.class, new ModernCellRenderer());
        
        // Log column renderer and editor
        TableColumn logColumn = columnModel.getColumn(10);
        logColumn.setCellRenderer(new LogButtonRenderer());
        logColumn.setCellEditor(new LogButtonEditor());
        
        // Status column renderer
        columnModel.getColumn(8).setCellRenderer(new StatusCellRenderer());
        
        // Optimal departure column renderer
        columnModel.getColumn(4).setCellRenderer(new OptimalDepartureCellRenderer());
        
        // Hover effect
        historyTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = historyTable.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    historyTable.repaint();
                }
            }
        });
        
        historyTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                historyTable.repaint();
            }
        });
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 15));
        panel.setOpaque(false);
        
        JButton refreshButton = createModernButton("🔄 Refresh", ACCENT_BLUE);
        JButton clearButton = createModernButton("🗑️ Clear", new Color(244, 67, 54));
        JButton exportCsvButton = createModernButton("📊 CSV", ACCENT_GREEN);
        JButton exportJsonButton = createModernButton("📄 JSON", ACCENT_ORANGE);
        
        refreshButton.addActionListener(e -> refreshTable());
        clearButton.addActionListener(e -> clearHistory());
        exportCsvButton.addActionListener(e -> exportAsCsv());
        exportJsonButton.addActionListener(e -> exportAsJSON());
        
        panel.add(refreshButton);
        panel.add(exportCsvButton);
        panel.add(exportJsonButton);
        panel.add(clearButton);
        
        return panel;
    }
    
    private JButton createModernButton(String text, Color accentColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color bgColor = getModel().isRollover() ? accentColor : 
                               getModel().isPressed() ? accentColor.darker() :
                               new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 25);
                
                g2d.setColor(bgColor);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                
                g2d.setColor(accentColor);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 8, 8));
                
                g2d.dispose();
                
                g.setColor(getModel().isRollover() ? Color.WHITE : accentColor);
                g.setFont(getFont());
                FontMetrics fm = g.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g.drawString(getText(), x, y);
            }
        };
        
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 36));
        
        return btn;
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        
        for (QueryResult result : historyManager.getHistory()) {
            Object[] row = {
                result.getTimestamp().format(formatter),
                result.getSourceNode(),
                result.getDestinationNode(),
                String.format("%.1f", result.getDepartureTime()),
                result.getFormattedOptimalDepartureTime(),  // Optimal departure
                String.format("%.1f", result.getBudget()),
                result.isSuccess() ? String.format("%.2f", result.getTravelTime()) : "N/A",
                result.isSuccess() ? String.format("%.1f", result.getTotalDistance()) : "N/A",  // Distance
                result.isSuccess() ? "✓ Success" : "✗ Failed",
                result.getExecutionTimeMs(),
                result.getLogFilePath() != null ? "📜 View" : "--"  // Log file
            };
            tableModel.addRow(row);
        }

        updateStats();
    }

    private void updateStats() {
        int total = historyManager.getHistorySize();
        double successRate = historyManager.getSuccessRate();
        double avgTime = historyManager.getAverageExecutionTime();
        statsLabel.setText(String.format(
            "<html><span style='color: #5f6368;'>Total: <b>%d</b></span> &nbsp;│&nbsp; " +
            "<span style='color: #34a853;'>Success: <b>%.1f%%</b></span> &nbsp;│&nbsp; " +
            "<span style='color: #4285f4;'>Avg: <b>%.1fms</b></span></html>", 
            total, successRate, avgTime));
    }

    private void clearHistory() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to clear all query history?",
            "Clear History",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            historyManager.clearHistory();
            refreshTable();
        }
    }

    private void exportAsCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Query History as CSV");
        fileChooser.setSelectedFile(new File("query_history.csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Header
                writer.println("Timestamp,Source,Destination,Departure Time,Optimal Departure,Budget,Travel Time,Distance,Status,Execution Time (ms),Log File");
                
                // Data
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                for (QueryResult result : historyManager.getHistory()) {
                    writer.printf("%s,%d,%d,%.2f,%s,%.2f,%s,%s,%s,%d,\"%s\"%n",
                        result.getTimestamp().format(formatter),
                        result.getSourceNode(),
                        result.getDestinationNode(),
                        result.getDepartureTime(),
                        result.getFormattedOptimalDepartureTime(),
                        result.getBudget(),
                        result.isSuccess() ? String.format("%.2f", result.getTravelTime()) : "N/A",
                        result.isSuccess() ? String.format("%.2f", result.getTotalDistance()) : "N/A",
                        result.isSuccess() ? "Success" : "Failed",
                        result.getExecutionTimeMs(),
                        result.getLogFilePath() != null ? result.getLogFilePath() : ""
                    );
                }
                
                JOptionPane.showMessageDialog(this,
                    "Query history exported successfully!",
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Error exporting history: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportAsJSON() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Query History as JSON");
        fileChooser.setSelectedFile(new File("query_history.json"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("{");
                writer.println("  \"queryHistory\": [");
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                var history = historyManager.getHistory();
                for (int i = 0; i < history.size(); i++) {
                    QueryResult result = history.get(i);
                    writer.println("    {");
                    writer.printf("      \"timestamp\": \"%s\",%n", result.getTimestamp().format(formatter));
                    writer.printf("      \"source\": %d,%n", result.getSourceNode());
                    writer.printf("      \"destination\": %d,%n", result.getDestinationNode());
                    writer.printf("      \"departureTime\": %.2f,%n", result.getDepartureTime());
                    writer.printf("      \"optimalDepartureTime\": \"%s\",%n", result.getFormattedOptimalDepartureTime());
                    writer.printf("      \"budget\": %.2f,%n", result.getBudget());
                    writer.printf("      \"success\": %s,%n", result.isSuccess());
                    if (result.isSuccess()) {
                        writer.printf("      \"travelTime\": %.2f,%n", result.getTravelTime());
                        writer.printf("      \"totalDistance\": %.2f,%n", result.getTotalDistance());
                        writer.printf("      \"pathLength\": %d,%n", result.getPathNodes().size());
                        writer.printf("      \"wideRoadPercentage\": %.2f,%n", result.getWideRoadPercentage());
                    } else {
                        writer.println("      \"travelTime\": null,");
                        writer.println("      \"totalDistance\": null,");
                        writer.println("      \"pathLength\": 0,");
                        writer.println("      \"wideRoadPercentage\": 0,");
                    }
                    writer.printf("      \"executionTimeMs\": %d,%n", result.getExecutionTimeMs());
                    writer.printf("      \"logFilePath\": %s%n", 
                        result.getLogFilePath() != null ? 
                        "\"" + result.getLogFilePath().replace("\\", "\\\\") + "\"" : "null");
                    writer.print("    }");
                    if (i < history.size() - 1) {
                        writer.println(",");
                    } else {
                        writer.println();
                    }
                }
                
                writer.println("  ],");
                writer.printf("  \"totalQueries\": %d,%n", historyManager.getHistorySize());
                writer.printf("  \"successRate\": %.2f,%n", historyManager.getSuccessRate());
                writer.printf("  \"averageExecutionTime\": %.2f%n", historyManager.getAverageExecutionTime());
                writer.println("}");
                
                JOptionPane.showMessageDialog(this,
                    "Query history exported successfully!",
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Error exporting history: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // === Custom Cell Renderers ===
    
    /**
     * Modern cell renderer with subtle styling
     */
    private class ModernCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            
            if (!isSelected) {
                label.setForeground(TEXT_PRIMARY);
            }
            
            return label;
        }
    }
    
    /**
     * Status column renderer with colored badges
     */
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            
            String status = value != null ? value.toString() : "";
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            
            if (status.contains("Success")) {
                label.setForeground(ACCENT_GREEN);
            } else {
                label.setForeground(new Color(244, 67, 54));
            }
            
            return label;
        }
    }
    
    /**
     * Optimal departure column renderer with star highlight
     */
    private class OptimalDepartureCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            
            String depTime = value != null ? value.toString() : "--";
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
            
            if (!depTime.equals("--")) {
                label.setForeground(ACCENT_ORANGE);
                label.setText("⭐ " + depTime);
            } else {
                label.setForeground(TEXT_SECONDARY);
            }
            
            return label;
        }
    }
    
    /**
     * Log file button renderer
     */
    private class LogButtonRenderer extends JButton implements TableCellRenderer {
        public LogButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            String text = value != null ? value.toString() : "--";
            setText(text);
            
            if (text.contains("View")) {
                setForeground(ACCENT_PURPLE);
                setBackground(new Color(156, 39, 176, 20));
                setBorder(BorderFactory.createLineBorder(new Color(156, 39, 176, 60), 1));
            } else {
                setForeground(TEXT_SECONDARY);
                setBackground(Color.WHITE);
                setBorder(BorderFactory.createEmptyBorder());
            }
            
            return this;
        }
    }
    
    /**
     * Log file button editor - handles click to open log viewer
     */
    private class LogButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String logFilePath;
        private boolean isPushed;
        
        public LogButtonEditor() {
            super(new JCheckBox());
            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("Segoe UI", Font.BOLD, 11));
            button.addActionListener(e -> fireEditingStopped());
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            
            // Get the log file path from the corresponding QueryResult
            int modelRow = table.convertRowIndexToModel(row);
            var history = historyManager.getHistory();
            if (modelRow >= 0 && modelRow < history.size()) {
                logFilePath = history.get(modelRow).getLogFilePath();
            } else {
                logFilePath = null;
            }
            
            String text = value != null ? value.toString() : "--";
            button.setText(text);
            
            if (text.contains("View")) {
                button.setForeground(ACCENT_PURPLE);
                button.setBackground(new Color(156, 39, 176, 30));
            } else {
                button.setForeground(TEXT_SECONDARY);
                button.setBackground(Color.WHITE);
            }
            
            isPushed = true;
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            if (isPushed && logFilePath != null && !logFilePath.isEmpty()) {
                // Open the log viewer window
                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(QueryHistoryPanel.this);
                LogViewerWindow.showLogViewer(parentFrame, logFilePath);
            }
            isPushed = false;
            return logFilePath != null ? "📜 View" : "--";
        }
        
        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}
