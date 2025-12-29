package ui.components;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.Timer;

/**
 * Modern animated splash screen with progress indication
 */
public class WorldClassSplashScreen extends JWindow {
    
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private Timer pulseTimer;
    private float pulsePhase = 0f;
    private JPanel contentPanel;
    
    public WorldClassSplashScreen() {
        initUI();
    }
    
    private void initUI() {
        setSize(500, 350);
        setLocationRelativeTo(null);
        
        contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(30, 60, 114),
                    0, getHeight(), new Color(42, 82, 152)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Animated circles
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                for (int i = 0; i < 5; i++) {
                    float offset = (pulsePhase + i * 0.5f) % (float)(2 * Math.PI);
                    int size = 100 + (int)(50 * Math.sin(offset));
                    g2d.setColor(Color.WHITE);
                    g2d.fillOval(50 + i * 80, 50 + (int)(20 * Math.sin(offset)), size, size);
                }
                
                g2d.dispose();
            }
        };
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createLineBorder(new Color(60, 100, 160), 2, true));
        
        // Center content
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        
        // Logo/Icon
        JLabel iconLabel = new JLabel("🗺️");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 72));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Title
        JLabel titleLabel = new JLabel("Wide-Path Navigator");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Version
        JLabel versionLabel = new JLabel("v3.0 — World Class Edition");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        versionLabel.setForeground(new Color(180, 200, 220));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Advanced Pathfinding with Wide Road Optimization");
        subtitleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        subtitleLabel.setForeground(new Color(160, 180, 200));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        centerPanel.add(Box.createVerticalStrut(50));
        centerPanel.add(iconLabel);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(versionLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(subtitleLabel);
        
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel with progress
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 30, 40));
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(false);
        progressBar.setForeground(new Color(100, 180, 255));
        progressBar.setBackground(new Color(40, 70, 130));
        progressBar.setBorder(BorderFactory.createEmptyBorder());
        progressBar.setPreferredSize(new Dimension(420, 6));
        progressBar.setMaximumSize(new Dimension(420, 6));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        statusLabel = new JLabel("Initializing...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(150, 170, 190));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        bottomPanel.add(progressBar);
        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(statusLabel);
        
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        setContentPane(contentPanel);
        
        // Animation timer
        pulseTimer = new Timer(50, e -> {
            pulsePhase += 0.1f;
            contentPanel.repaint();
        });
    }
    
    public void showSplash() {
        setVisible(true);
        pulseTimer.start();
    }
    
    public void setProgress(int percent, String status) {
        progressBar.setValue(percent);
        statusLabel.setText(status);
    }
    
    public void closeSplash() {
        pulseTimer.stop();
        
        // Fade out effect
        Timer fadeTimer = new Timer(30, null);
        final float[] opacity = {1.0f};
        fadeTimer.addActionListener(e -> {
            opacity[0] -= 0.1f;
            if (opacity[0] <= 0) {
                fadeTimer.stop();
                dispose();
            } else {
                setOpacity(opacity[0]);
            }
        });
        fadeTimer.start();
    }
}
