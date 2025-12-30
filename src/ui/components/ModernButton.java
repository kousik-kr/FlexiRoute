package ui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

/**
 * Modern Material Design inspired button with hover effects
 */
public class ModernButton extends JButton {
    private Color baseColor;
    private Color hoverColor;
    private Color pressedColor;
    private boolean isHovered = false;
    private boolean isPressed = false;

    public ModernButton(String text, Color color) {
        super(text);
        this.baseColor = color;
        this.hoverColor = brighten(color, 0.15f);
        this.pressedColor = darken(color, 0.15f);
        
        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI", Font.BOLD, 19));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                isPressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bgColor = isPressed ? pressedColor : (isHovered ? hoverColor : baseColor);
        
        if (!isEnabled()) {
            bgColor = new Color(200, 200, 200);
        }

        // Gradient fill for vibrant effect
        java.awt.GradientPaint gp = new java.awt.GradientPaint(
            0, 0, bgColor,
            0, getHeight(), darken(bgColor, 0.15f)
        );
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
        
        // Shine effect at top
        g2.setColor(new Color(255, 255, 255, 50));
        g2.fillRoundRect(2, 2, getWidth()-4, getHeight()/2 - 2, 14, 14);

        g2.dispose();
        super.paintComponent(g);
    }

    private Color brighten(Color color, float factor) {
        int r = Math.min(255, (int)(color.getRed() * (1 + factor)));
        int g = Math.min(255, (int)(color.getGreen() * (1 + factor)));
        int b = Math.min(255, (int)(color.getBlue() * (1 + factor)));
        return new Color(r, g, b);
    }

    private Color darken(Color color, float factor) {
        int r = (int)(color.getRed() * (1 - factor));
        int g = (int)(color.getGreen() * (1 - factor));
        int b = (int)(color.getBlue() * (1 - factor));
        return new Color(r, g, b);
    }

    public void setBaseColor(Color color) {
        this.baseColor = color;
        this.hoverColor = brighten(color, 0.1f);
        this.pressedColor = darken(color, 0.1f);
        repaint();
    }
}
