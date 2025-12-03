package ui.panels;

import models.QueryResult;
import managers.QueryHistoryManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.*;
import java.time.format.DateTimeFormatter;

/**
 * Panel displaying query history with filtering and sorting
 */
public class QueryHistoryPanel extends JPanel {
    private final QueryHistoryManager historyManager;
    private final JTable historyTable;
    private final DefaultTableModel tableModel;
    private final JLabel statsLabel;

    public QueryHistoryPanel(QueryHistoryManager historyManager) {
        this.historyManager = historyManager;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title and stats
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Query History", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statsLabel.setForeground(Color.GRAY);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statsLabel, BorderLayout.EAST);

        // Table
        String[] columns = {"Time", "Source", "Dest", "Departure", "Budget", "Travel Time", "Status", "Execution (ms)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setFont(new Font("Consolas", Font.PLAIN, 12));
        historyTable.setRowHeight(25);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(historyTable);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("🔄 Refresh");
        JButton clearButton = new JButton("🗑 Clear History");
        JButton exportButton = new JButton("💾 Export");
        
        refreshButton.addActionListener(e -> refreshTable());
        clearButton.addActionListener(e -> clearHistory());
        exportButton.addActionListener(e -> exportHistory());

        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exportButton);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        refreshTable();
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
                String.format("%.1f", result.getBudget()),
                result.isSuccess() ? String.format("%.2f", result.getTravelTime()) : "N/A",
                result.isSuccess() ? "✓ Success" : "✗ Failed",
                result.getExecutionTimeMs()
            };
            tableModel.addRow(row);
        }

        updateStats();
    }

    private void updateStats() {
        int total = historyManager.getHistorySize();
        double successRate = historyManager.getSuccessRate();
        double avgTime = historyManager.getAverageExecutionTime();
        statsLabel.setText(String.format("Total: %d | Success Rate: %.1f%% | Avg Time: %.2f ms", 
            total, successRate, avgTime));
    }

    private void clearHistory() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to clear the history?",
            "Confirm Clear",
            JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            historyManager.clearHistory();
            refreshTable();
        }
    }

    public void exportHistory() {
        if (historyManager.getHistorySize() == 0) {
            JOptionPane.showMessageDialog(this,
                "No query history to export.",
                "Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Ask user for export format
        String[] options = {"CSV", "JSON", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this,
            "Choose export format:",
            "Export Query History",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);

        if (choice == 0) {
            exportAsCSV();
        } else if (choice == 1) {
            exportAsJSON();
        }
    }

    private void exportAsCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Query History as CSV");
        fileChooser.setSelectedFile(new File("query_history.csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Write header
                writer.println("Timestamp,Source,Destination,Departure Time,Interval,Budget,Travel Time,Status,Execution Time (ms),Error Message");
                
                // Write data
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                for (QueryResult result : historyManager.getHistory()) {
                    writer.printf("%s,%d,%d,%.2f,%.2f,%.2f,%s,%s,%d,\"%s\"%n",
                        result.getTimestamp().format(formatter),
                        result.getSourceNode(),
                        result.getDestinationNode(),
                        result.getDepartureTime(),
                        result.getIntervalDuration(),
                        result.getBudget(),
                        result.isSuccess() ? String.format("%.2f", result.getTravelTime()) : "N/A",
                        result.isSuccess() ? "Success" : "Failed",
                        result.getExecutionTimeMs(),
                        result.getErrorMessage() != null ? result.getErrorMessage().replace("\"", "\"\"") : ""
                    );
                }
                
                JOptionPane.showMessageDialog(this,
                    "Query history exported successfully to:\n" + file.getAbsolutePath(),
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
                    writer.printf("      \"interval\": %.2f,%n", result.getIntervalDuration());
                    writer.printf("      \"budget\": %.2f,%n", result.getBudget());
                    writer.printf("      \"success\": %s,%n", result.isSuccess());
                    if (result.isSuccess()) {
                        writer.printf("      \"travelTime\": %.2f,%n", result.getTravelTime());
                        writer.printf("      \"pathLength\": %d,%n", result.getPathNodes().size());
                    } else {
                        writer.println("      \"travelTime\": null,");
                        writer.println("      \"pathLength\": 0,");
                    }
                    writer.printf("      \"executionTimeMs\": %d,%n", result.getExecutionTimeMs());
                    writer.printf("      \"errorMessage\": %s%n", 
                        result.getErrorMessage() != null ? 
                        "\"" + result.getErrorMessage().replace("\"", "\\\"") + "\"" : "null");
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
                    "Query history exported successfully to:\n" + file.getAbsolutePath(),
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
}
