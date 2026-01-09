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
public class SplashScreen extends JWindow {
    
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private Timer pulseTimer;
    private float pulsePhase = 0f;
    private JPanel contentPanel;
    
    public SplashScreen() {
        initUI();
    }
    
    private void initUI() {
        setSize(750, 500);
        setLocationRelativeTo(null);
        
        contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // 🌈 RAINBOW gradient background - Pink to Purple to Blue
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(236, 72, 153),  // Hot Pink
                    getWidth(), getHeight(), new Color(99, 102, 241)  // Royal Indigo
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
                
                // Animated rainbow circles
                Color[] colors = {
                    new Color(255, 107, 107, 40),  // Coral
                    new Color(250, 204, 21, 40),   // Yellow
                    new Color(16, 185, 129, 40),   // Green
                    new Color(59, 130, 246, 40),   // Blue
                    new Color(168, 85, 247, 40)    // Purple
                };
                for (int i = 0; i < 5; i++) {
                    float offset = (pulsePhase + i * 0.6f) % (float)(2 * Math.PI);
                    int size = 120 + (int)(60 * Math.sin(offset));
                    g2d.setColor(colors[i]);
                    g2d.fillOval(30 + i * 100, 40 + (int)(30 * Math.sin(offset)), size, size);
                }
                
                // Sparkle effect
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                for (int i = 0; i < 8; i++) {
                    float twinkle = (float) Math.abs(Math.sin(pulsePhase * 2 + i));
                    g2d.setColor(new Color(255, 255, 255, (int)(twinkle * 200)));
                    int x = 50 + (i * 70) % (getWidth() - 100);
                    int y = 30 + (i * 50) % (getHeight() - 100);
                    g2d.fillOval(x, y, 8, 8);
                }
                
                g2d.dispose();
            }
        };
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(250, 204, 21), 3, true),
            BorderFactory.createLineBorder(new Color(236, 72, 153), 2, true)
        ));
        
        // Center content
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        
        // Logo/Icon - Rainbow emoji
        JLabel iconLabel = new JLabel("🌈🗺️🌟");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Title with sparkle
        JLabel titleLabel = new JLabel("✨ FlexiRoute Navigator ✨");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 52));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Version with rainbow styling
        // JLabel versionLabel = new JLabel("🌟 FlexiRoute Navigator 🌟");
        // versionLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        // versionLabel.setForeground(new Color(250, 204, 21));
        // versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("🚀 Advanced Pathfinding with Wide Road Optimization 🚀");
        subtitleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 20));
        subtitleLabel.setForeground(new Color(255, 220, 255));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        centerPanel.add(Box.createVerticalStrut(60));
        centerPanel.add(iconLabel);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(titleLabel);
        // centerPanel.add(Box.createVerticalStrut(10));
        // centerPanel.add(versionLabel);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(subtitleLabel);
        
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel with progress
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 55, 40, 55));
        
        progressBar = new JProgressBar(0, 100) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Background
                g2d.setColor(new Color(255, 255, 255, 60));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                // Rainbow progress gradient
                int w = (int) ((getValue() / 100.0) * getWidth());
                if (w > 0) {
                    GradientPaint gp = new GradientPaint(
                        0, 0, new Color(250, 204, 21),  // Yellow
                        getWidth(), 0, new Color(16, 185, 129)  // Green
                    );
                    g2d.setPaint(gp);
                    g2d.fillRoundRect(0, 0, w, getHeight(), 12, 12);
                }
                g2d.dispose();
            }
        };
        progressBar.setValue(0);
        progressBar.setStringPainted(false);
        progressBar.setBorder(null);
        progressBar.setPreferredSize(new Dimension(490, 12));
        progressBar.setMaximumSize(new Dimension(490, 12));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        statusLabel = new JLabel("✨ Initializing magic...");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        statusLabel.setForeground(new Color(255, 240, 255));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        bottomPanel.add(progressBar);
        bottomPanel.add(Box.createVerticalStrut(12));
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
