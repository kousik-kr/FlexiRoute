package ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Toast notification with auto-dismiss and smooth animations
 */
public class NotificationToast extends JWindow {
    public enum Type {
        SUCCESS(new Color(76, 175, 80), "✓"),
        ERROR(new Color(244, 67, 54), "✕"),
        WARNING(new Color(255, 193, 7), "⚠"),
        INFO(new Color(33, 150, 243), "ℹ");
        
        final Color color;
        final String icon;
        
        Type(Color color, String icon) {
            this.color = color;
            this.icon = icon;
        }
    }
    
    private float opacity = 0.0f;
    private final Timer fadeTimer;
    private final Type type;
    
    public NotificationToast(JFrame parent, String message, Type type, int durationMs) {
        super(parent);
        this.type = type;
        
        // Content panel
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background with rounded corners
                g2d.setColor(type.color);
                RoundRectangle2D bg = new RoundRectangle2D.Float(
                    0, 0, getWidth(), getHeight(), 10, 10
                );
                g2d.fill(bg);
                
                g2d.dispose();
            }
        };
        
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BorderLayout(10, 0));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // Icon
        JLabel iconLabel = new JLabel(type.icon);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        iconLabel.setForeground(Color.WHITE);
        
        // Message
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(Color.WHITE);
        
        contentPanel.add(iconLabel, BorderLayout.WEST);
        contentPanel.add(messageLabel, BorderLayout.CENTER);
        
        setContentPane(contentPanel);
        pack();
        
        // Position at top-right of parent
        if (parent != null) {
            int x = parent.getX() + parent.getWidth() - getWidth() - 20;
            int y = parent.getY() + 80;
            setLocation(x, y);
        }
        
        // Fade in/out animation
        fadeTimer = new Timer(50, new ActionListener() {
            private int elapsed = 0;
            private boolean fadingIn = true;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                elapsed += 50;
                
                if (fadingIn) {
                    opacity = Math.min(1.0f, opacity + 0.1f);
                    if (opacity >= 0.95f) {
                        fadingIn = false;
                    }
                } else if (elapsed > durationMs) {
                    opacity = Math.max(0.0f, opacity - 0.1f);
                    if (opacity <= 0.05f) {
                        fadeTimer.stop();
                        dispose();
                    }
                }
                
                setOpacity(opacity);
            }
        });
        
        setOpacity(0.0f);
        fadeTimer.start();
    }
    
    public static void show(JFrame parent, String message, Type type) {
        show(parent, message, type, 3000);
    }
    
    public static void show(JFrame parent, String message, Type type, int durationMs) {
        SwingUtilities.invokeLater(() -> {
            NotificationToast toast = new NotificationToast(parent, message, type, durationMs);
            toast.setVisible(true);
        });
    }
}
