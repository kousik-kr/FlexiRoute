package ui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Glassmorphism-style panel with blur effect and transparency
 */
public class GlassPanel extends JPanel {
    private final float transparency;
    private final int cornerRadius;
    private Color glassColor;
    private boolean blurBackground;
    
    public GlassPanel() {
        this(0.85f, 20);
    }
    
    public GlassPanel(float transparency, int cornerRadius) {
        this.transparency = transparency;
        this.cornerRadius = cornerRadius;
        this.glassColor = new Color(255, 255, 255);
        this.blurBackground = true;
        
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    }
    
    public void setGlassColor(Color color) {
        this.glassColor = color;
        repaint();
    }
    
    public void setBlurBackground(boolean blur) {
        this.blurBackground = blur;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Glass background with transparency
        int alpha = (int) (transparency * 255);
        g2d.setColor(new Color(glassColor.getRed(), glassColor.getGreen(), glassColor.getBlue(), alpha));
        
        RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(
            0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius
        );
        
        g2d.fill(roundedRectangle);
        
        // Border with slight glow
        g2d.setColor(new Color(255, 255, 255, 120));
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(roundedRectangle);
        
        // Inner highlight for depth
        g2d.setColor(new Color(255, 255, 255, 40));
        g2d.setStroke(new BasicStroke(1));
        RoundRectangle2D innerHighlight = new RoundRectangle2D.Float(
            1, 1, getWidth() - 3, getHeight() / 2f, cornerRadius, cornerRadius
        );
        g2d.draw(innerHighlight);
        
        g2d.dispose();
        super.paintComponent(g);
    }
}
