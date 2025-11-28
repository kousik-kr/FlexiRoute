package ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Professional status bar with icon support and color-coded messages
 */
public class StatusBar extends JPanel {
    public enum MessageType {
        INFO("ℹ", new Color(33, 150, 243)),
        SUCCESS("✓", new Color(76, 175, 80)),
        WARNING("⚠", new Color(255, 193, 7)),
        ERROR("✗", new Color(244, 67, 54));

        private final String icon;
        private final Color color;

        MessageType(String icon, Color color) {
            this.icon = icon;
            this.color = color;
        }

        public String getIcon() { return icon; }
        public Color getColor() { return color; }
    }

    private final JLabel messageLabel;
    private final JLabel memoryLabel;
    private final JLabel timeLabel;
    private Timer updateTimer;

    public StatusBar() {
        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        messageLabel = new JLabel(" Ready");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        memoryLabel = new JLabel();
        memoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        memoryLabel.setForeground(Color.GRAY);

        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(Color.GRAY);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.add(memoryLabel);
        rightPanel.add(timeLabel);

        add(messageLabel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);

        startUpdateTimer();
    }

    public void setMessage(String message, MessageType type) {
        SwingUtilities.invokeLater(() -> {
            messageLabel.setText(type.getIcon() + " " + message);
            messageLabel.setForeground(type.getColor());
        });
    }

    private void startUpdateTimer() {
        updateTimer = new Timer(1000, e -> updateSystemInfo());
        updateTimer.start();
    }

    private void updateSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        
        memoryLabel.setText(String.format("Memory: %d/%d MB", usedMemory, maxMemory));
        timeLabel.setText(java.time.LocalTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
        ));
    }

    public void dispose() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
    }
}
