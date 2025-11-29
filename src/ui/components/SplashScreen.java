package ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Modern splash screen with animated logo and progress indicator
 */
public class SplashScreen extends JWindow {
    private final JProgressBar progressBar;
    private final JLabel statusLabel;
    private int progress = 0;
    
    public SplashScreen() {
        setSize(600, 400);
        setLocationRelativeTo(null);
        
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(33, 150, 243),
                    getWidth(), getHeight(), new Color(66, 165, 245)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Logo/Title with shadow
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 48));
                g2d.drawString("Wide-Path Pro", 153, 153);
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 48));
                g2d.drawString("Wide-Path Pro", 150, 150);
                
                // Version
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                g2d.drawString("Version 3.0 - World-Class Edition", 170, 180);
                
                g2d.dispose();
            }
        };
        
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createLineBorder(new Color(33, 150, 243), 3));
        
        // Center content
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(220, 50, 50, 50));
        
        // Status label
        statusLabel = new JLabel("Initializing...", SwingConstants.CENTER);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(Color.WHITE);
        progressBar.setBackground(new Color(255, 255, 255, 100));
        progressBar.setMaximumSize(new Dimension(500, 25));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        centerPanel.add(statusLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        centerPanel.add(progressBar);
        
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        
        setContentPane(contentPanel);
    }
    
    public void updateProgress(int value, String status) {
        progress = value;
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(progress);
            statusLabel.setText(status);
        });
    }
    
    public void close() {
        SwingUtilities.invokeLater(() -> {
            setVisible(false);
            dispose();
        });
    }
}
