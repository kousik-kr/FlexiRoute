package ui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Animated card component with hover effects and smooth transitions
 */
public class AnimatedCard extends JPanel {
    private float elevation = 2.0f;
    private float targetElevation = 2.0f;
    private javax.swing.Timer animationTimer;
    private Color accentColor;
    private boolean isHovered = false;
    
    public AnimatedCard() {
        this(new Color(33, 150, 243));
    }
    
    public AnimatedCard(Color accentColor) {
        this.accentColor = accentColor;
        setOpaque(false);
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Animation timer for smooth elevation changes
        animationTimer = new javax.swing.Timer(16, e -> {
            if (Math.abs(elevation - targetElevation) > 0.1f) {
                elevation += (targetElevation - elevation) * 0.3f;
                repaint();
            } else {
                elevation = targetElevation;
                animationTimer.stop();
            }
        });
        
        // Hover effects
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                targetElevation = 8.0f;
                animationTimer.start();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                targetElevation = 2.0f;
                animationTimer.start();
            }
        });
    }
    
    public void setAccentColor(Color color) {
        this.accentColor = color;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int shadowOffset = (int) elevation;
        
        // Draw shadow
        for (int i = 0; i < shadowOffset; i++) {
            int alpha = (int) (30 * (1 - (float) i / shadowOffset));
            g2d.setColor(new Color(0, 0, 0, alpha));
            RoundRectangle2D shadow = new RoundRectangle2D.Float(
                i, i, getWidth() - i * 2 - 1, getHeight() - i * 2 - 1, 15, 15
            );
            g2d.fill(shadow);
        }
        
        // Draw card background
        g2d.setColor(getBackground());
        RoundRectangle2D card = new RoundRectangle2D.Float(
            shadowOffset, shadowOffset, 
            getWidth() - shadowOffset * 2 - 1, 
            getHeight() - shadowOffset * 2 - 1, 
            15, 15
        );
        g2d.fill(card);
        
        // Draw accent border on hover
        if (isHovered) {
            g2d.setColor(accentColor);
            g2d.setStroke(new BasicStroke(2));
            g2d.draw(card);
        } else {
            g2d.setColor(new Color(224, 224, 224));
            g2d.setStroke(new BasicStroke(1));
            g2d.draw(card);
        }
        
        g2d.dispose();
        super.paintComponent(g);
    }
}
