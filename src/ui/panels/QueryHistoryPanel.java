package ui.panels;

import models.QueryResult;
import managers.QueryHistoryManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
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

    private void exportHistory() {
        JOptionPane.showMessageDialog(this, 
            "Export functionality will be implemented with CSV/JSON export options.",
            "Export", JOptionPane.INFORMATION_MESSAGE);
    }
}
