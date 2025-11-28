package ui.panels;

import managers.MetricsCollector;
import javax.swing.*;
import java.awt.*;

/**
 * Dashboard displaying real-time performance metrics
 */
public class MetricsDashboard extends JPanel {
    private final MetricsCollector metricsCollector;
    private final JLabel totalQueriesLabel;
    private final JLabel avgExecutionTimeLabel;
    private final JLabel successRateLabel;
    private final JLabel successfulQueriesLabel;
    private final JLabel failedQueriesLabel;
    private final Timer updateTimer;

    public MetricsDashboard(MetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title
        JLabel titleLabel = new JLabel("Performance Metrics", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(titleLabel, gbc);

        // Metrics panels
        totalQueriesLabel = createMetricLabel("Total Queries", "0");
        avgExecutionTimeLabel = createMetricLabel("Avg Execution Time", "0 ms");
        successRateLabel = createMetricLabel("Success Rate", "0%");
        successfulQueriesLabel = createMetricLabel("Successful", "0");
        failedQueriesLabel = createMetricLabel("Failed", "0");

        add(createMetricPanel("Total Queries Executed", totalQueriesLabel, new Color(33, 150, 243)), gbc);
        add(createMetricPanel("Average Execution Time", avgExecutionTimeLabel, new Color(156, 39, 176)), gbc);
        add(createMetricPanel("Success Rate", successRateLabel, new Color(76, 175, 80)), gbc);
        
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        statsPanel.add(createMetricPanel("Successful Queries", successfulQueriesLabel, new Color(76, 175, 80)));
        statsPanel.add(createMetricPanel("Failed Queries", failedQueriesLabel, new Color(244, 67, 54)));
        add(statsPanel, gbc);

        // Update timer
        updateTimer = new Timer(1000, e -> updateMetrics());
        updateTimer.start();
    }

    private JLabel createMetricLabel(String text, String value) {
        JLabel label = new JLabel(value, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 32));
        return label;
    }

    private JPanel createMetricPanel(String title, JLabel valueLabel, Color accentColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accentColor, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);

        return panel;
    }

    private void updateMetrics() {
        totalQueriesLabel.setText(String.valueOf(metricsCollector.getTotalQueries()));
        avgExecutionTimeLabel.setText(String.format("%.2f ms", metricsCollector.getAverageExecutionTime()));
        successRateLabel.setText(String.format("%.1f%%", metricsCollector.getSuccessRate()));
        successfulQueriesLabel.setText(String.valueOf(metricsCollector.getSuccessfulQueries()));
        failedQueriesLabel.setText(String.valueOf(metricsCollector.getFailedQueries()));
    }

    public void dispose() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
    }
}
