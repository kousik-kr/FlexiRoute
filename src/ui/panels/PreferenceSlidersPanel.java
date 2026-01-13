package ui.panels;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.Hashtable;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JComboBox;
import models.RoutingMode;
import javax.swing.JComboBox;
import models.RoutingMode;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

/**
 * Preference Sliders Panel - Controls for routing preferences and visualization options.
 * 
 * Architecture:
 * +--------------------------+
 * | PreferenceSlidersPanel   |
 * |  - Wideness preference   |
 * |  - Time preference       |
 * |  - Distance preference   |
 * |  - Visualization opts    |
 * +--------------------------+
 * 
 * Allows users to adjust routing preferences that influence the cost function
 * in the custom routing engine.
 */
public class PreferenceSlidersPanel extends JPanel {
    
    // === COLORS ===
    private static final Color ELECTRIC_BLUE = new Color(59, 130, 246);
    private static final Color VIVID_PURPLE = new Color(168, 85, 247);
    private static final Color NEON_GREEN = new Color(16, 185, 129);
    private static final Color SUNSET_ORANGE = new Color(251, 146, 60);
    private static final Color HOT_PINK = new Color(236, 72, 153);
    private static final Color OCEAN_TEAL = new Color(20, 184, 166);
    
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BG_SURFACE = new Color(248, 250, 252);
    private static final Color CARD_BG = new Color(255, 255, 255);
    
    // === COMPONENTS ===
    private JComboBox<RoutingMode> routingModeCombo;
    
    private JToggleButton prioritizeWideness;
    private JToggleButton prioritizeTime;
    private JToggleButton balancedMode;
    
    // === CALLBACKS ===
    private Consumer<PreferenceValues> onPreferenceChange;
    
    // === STATE ===
    private PreferenceValues currentValues = new PreferenceValues();
    
    public PreferenceSlidersPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_SURFACE);
        setBorder(new EmptyBorder(12, 12, 12, 12));
        initComponents();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);
        
        // Header
        mainPanel.add(createHeader());
        mainPanel.add(Box.createVerticalStrut(16));
        
        // Quick presets
        mainPanel.add(createPresetsPanel());
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Routing Mode Selection
        mainPanel.add(createRoutingModeCard());
        
        mainPanel.add(Box.createVerticalGlue());
        
        // Summary card
        mainPanel.add(Box.createVerticalStrut(16));
        mainPanel.add(createSummaryCard());
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel title = new JLabel("⚙️ Routing Preferences");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(VIVID_PURPLE);
        header.add(title, BorderLayout.WEST);
        
        return header;
    }
    
    private JPanel createPresetsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel label = new JLabel("Quick Mode: ");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_SECONDARY);
        panel.add(label);
        panel.add(Box.createHorizontalStrut(8));
        
        prioritizeWideness = createPresetButton("🛣️ Wide", NEON_GREEN);
        prioritizeWideness.addActionListener(e -> {
            if (prioritizeWideness.isSelected()) {
                prioritizeTime.setSelected(false);
                balancedMode.setSelected(false);
                routingModeCombo.setSelectedItem(RoutingMode.WIDENESS_ONLY);
            }
        });
        panel.add(prioritizeWideness);
        panel.add(Box.createHorizontalStrut(6));
        
        prioritizeTime = createPresetButton("⚡ Fast", ELECTRIC_BLUE);
        prioritizeTime.addActionListener(e -> {
            if (prioritizeTime.isSelected()) {
                prioritizeWideness.setSelected(false);
                balancedMode.setSelected(false);
                routingModeCombo.setSelectedItem(RoutingMode.MIN_TURNS_ONLY);
            }
        });
        panel.add(prioritizeTime);
        panel.add(Box.createHorizontalStrut(6));
        
        balancedMode = createPresetButton("⚖️ Balanced", SUNSET_ORANGE);
        balancedMode.setSelected(true);
        balancedMode.addActionListener(e -> {
            if (balancedMode.isSelected()) {
                prioritizeWideness.setSelected(false);
                prioritizeTime.setSelected(false);
                routingModeCombo.setSelectedItem(RoutingMode.WIDENESS_AND_TURNS);
            }
        });
        panel.add(balancedMode);
        
        return panel;
    }
    
    private JToggleButton createPresetButton(String text, Color color) {
        JToggleButton btn = new JToggleButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isSelected()) {
                    g2d.setColor(color);
                    g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                    g2d.setColor(Color.WHITE);
                } else {
                    g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
                    g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                    g2d.setColor(color);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.draw(new RoundRectangle2D.Double(1, 1, getWidth()-2, getHeight()-2, 10, 10));
                }
                
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
                
                g2d.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(95, 36));
        btn.setMaximumSize(new Dimension(95, 36));
        return btn;
    }
    
    private JPanel createRoutingModeCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Title
        JLabel titleLabel = new JLabel("🎯 Route Optimization Mode");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);
        
        card.add(Box.createVerticalStrut(8));
        
        // Description
        JLabel descLabel = new JLabel("Select route optimization strategy");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(descLabel);
        
        card.add(Box.createVerticalStrut(12));
        
        // Dropdown
        routingModeCombo = new JComboBox<>(RoutingMode.values());
        routingModeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        routingModeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        routingModeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        routingModeCombo.addActionListener(e -> updatePreferences());
        card.add(routingModeCombo);
        
        return card;
    }
    
    private JPanel createSliderCard(String title, String description, Color accentColor, 
                                    int min, int max, int initial, Consumer<JSlider> sliderConfig) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_BG);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 50));
                g2d.setStroke(new BasicStroke(2));
                g2d.draw(new RoundRectangle2D.Double(1, 1, getWidth()-2, getHeight()-2, 14, 14));
                g2d.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Title row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(accentColor.darker());
        titleRow.add(titleLabel, BorderLayout.WEST);
        
        JLabel valueLabel = new JLabel(initial + "%");
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        valueLabel.setForeground(accentColor);
        titleRow.add(valueLabel, BorderLayout.EAST);
        
        card.add(titleRow);
        
        // Description
        JLabel desc = new JLabel(description);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        desc.setForeground(TEXT_SECONDARY);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(desc);
        card.add(Box.createVerticalStrut(10));
        
        // Slider
        JSlider slider = new JSlider(min, max, initial);
        slider.setOpaque(false);
        slider.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        slider.setAlignmentX(Component.LEFT_ALIGNMENT);
        slider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        // Custom labels
        Hashtable<Integer, JLabel> labels = new Hashtable<>();
        JLabel lowLabel = new JLabel("Low");
        lowLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lowLabel.setForeground(TEXT_SECONDARY);
        labels.put(0, lowLabel);
        
        JLabel highLabel = new JLabel("High");
        highLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        highLabel.setForeground(TEXT_SECONDARY);
        labels.put(100, highLabel);
        
        slider.setLabelTable(labels);
        slider.setPaintLabels(true);
        
        // Update value label on change
        slider.addChangeListener(e -> {
            valueLabel.setText(slider.getValue() + "%");
            // Deselect presets when manually adjusting
            prioritizeWideness.setSelected(false);
            prioritizeTime.setSelected(false);
            balancedMode.setSelected(false);
        });
        
        sliderConfig.accept(slider);
        card.add(slider);
        
        return card;
    }
    
    private JPanel createSummaryCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(248, 250, 255),
                    0, getHeight(), new Color(240, 245, 255)
                );
                g2d.setPaint(gp);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                
                g2d.setColor(new Color(VIVID_PURPLE.getRed(), VIVID_PURPLE.getGreen(), VIVID_PURPLE.getBlue(), 80));
                g2d.setStroke(new BasicStroke(2));
                g2d.draw(new RoundRectangle2D.Double(1, 1, getWidth()-2, getHeight()-2, 14, 14));
                g2d.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel title = new JLabel("📊 Current Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(VIVID_PURPLE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        
        JLabel summary = new JLabel("<html>Routes will prioritize <b style='color:#10B981'>wider roads</b> " +
            "while maintaining reasonable <b style='color:#3B82F6'>travel time</b></html>");
        summary.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        summary.setForeground(TEXT_SECONDARY);
        summary.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(summary);
        
        return card;
    }
    
    private void updatePreferences() {
        if (routingModeCombo == null) {
            return;
        }
        
        RoutingMode mode = (RoutingMode) routingModeCombo.getSelectedItem();
        
        // Set weights based on routing mode
        if (mode == RoutingMode.WIDENESS_ONLY) {
            currentValues.widenessWeight = 1.0;
            currentValues.timeWeight = 0.0;
            currentValues.distanceWeight = 0.0;
            currentValues.rightTurnWeight = 0.0;
        } else if (mode == RoutingMode.MIN_TURNS_ONLY) {
            currentValues.widenessWeight = 0.0;
            currentValues.timeWeight = 0.0;
            currentValues.distanceWeight = 0.0;
            currentValues.rightTurnWeight = 1.0;
        } else { // WIDENESS_AND_TURNS
            currentValues.widenessWeight = 0.5;
            currentValues.timeWeight = 0.0;
            currentValues.distanceWeight = 0.0;
            currentValues.rightTurnWeight = 0.5;
        }
        
        if (onPreferenceChange != null) {
            onPreferenceChange.accept(currentValues);
        }
    }
    
    // === PUBLIC API ===
    
    public void setOnPreferenceChange(Consumer<PreferenceValues> callback) {
        this.onPreferenceChange = callback;
    }
    
    public PreferenceValues getPreferences() {
        return currentValues;
    }
    
    /**
     * Preference values container
     */
    public static class PreferenceValues {
        public double widenessWeight = 0.7;
        public double timeWeight = 0.5;
        public double distanceWeight = 0.3;
        public double rightTurnWeight = 0.2;
        
        @Override
        public String toString() {
            return String.format("Preferences[wideness=%.1f%%, time=%.1f%%, distance=%.1f%%, turns=%.1f%%]",
                widenessWeight * 100, timeWeight * 100, distanceWeight * 100, rightTurnWeight * 100);
        }
    }
}
