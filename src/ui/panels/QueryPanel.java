package ui.panels;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import models.RoutingMode;

/**
 * Query Panel - 500px width with large readable fonts
 */
public class QueryPanel extends JPanel {
    
    // 🌈 VIBRANT RAINBOW COLOR PALETTE
    private static final Color CORAL_PINK = new Color(255, 107, 107);
    private static final Color ELECTRIC_BLUE = new Color(59, 130, 246);
    private static final Color VIVID_PURPLE = new Color(168, 85, 247);
    private static final Color NEON_GREEN = new Color(16, 185, 129);
    private static final Color SUNSET_ORANGE = new Color(251, 146, 60);
    private static final Color HOT_PINK = new Color(236, 72, 153);
    private static final Color CYBER_YELLOW = new Color(250, 204, 21);
    private static final Color OCEAN_TEAL = new Color(20, 184, 166);
    private static final Color ROYAL_INDIGO = new Color(99, 102, 241);
    private static final Color LIME_GREEN = new Color(132, 204, 22);
    
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BG_SURFACE = new Color(248, 250, 252);
    
    // Components
    private JTextField sourceField, destField;
    private JSlider departureSlider, intervalSlider, budgetSlider;
    private JLabel departureValue, intervalValue, budgetValue;
    private JComboBox<String> heuristicCombo;
    private JButton runButton;
    private JLabel statusLabel;
    
    // Callbacks
    private Runnable onRunQuery;
    private java.util.function.BiConsumer<Integer, Integer> onPreviewChange;
    
    private int maxNodeId = 21048;
    
    // Routing mode selector
    private JComboBox<RoutingMode> routingModeCombo;
    private javax.swing.JTextArea routingModeDescriptionArea;
    
    // Frontier threshold mode selector
    private JComboBox<String> frontierThresholdCombo;
    private JLabel frontierThresholdDescription;
    
    public QueryPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_SURFACE);
        setBorder(new EmptyBorder(12, 12, 12, 12));
        initComponents();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);
        
        // === HEADER ===
        mainPanel.add(createHeader());
        mainPanel.add(Box.createVerticalStrut(14));
        
        // === SOURCE & DESTINATION (single row) ===
        mainPanel.add(createSourceDestRow());
        mainPanel.add(Box.createVerticalStrut(12));
        
        // === ALGORITHM (full width) ===
        mainPanel.add(createLabeledCombo("Algorithm", VIVID_PURPLE, new String[]{"Best", "Euclidean", "Manhattan"}, false));
        mainPanel.add(Box.createVerticalStrut(14));
        
        // === ROUTING MODE ===
        mainPanel.add(createRoutingModePanel());
        mainPanel.add(Box.createVerticalStrut(14));
        
        // === FRONTIER THRESHOLD MODE ===
        mainPanel.add(createFrontierThresholdPanel());
        mainPanel.add(Box.createVerticalStrut(14));
        
        // === QUICK ACTIONS (2 buttons) ===
        mainPanel.add(createActionsPanel());
        mainPanel.add(Box.createVerticalStrut(14));
        
        // === SLIDERS ===
        mainPanel.add(createSlidersPanel());
        mainPanel.add(Box.createVerticalStrut(16));
        
        // === RUN BUTTON ===
        mainPanel.add(createRunPanel());
        mainPanel.add(Box.createVerticalStrut(10));
        
        // === STATUS ===
        statusLabel = new JLabel("Enter source and destination");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        statusLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(statusLabel);
        
        mainPanel.add(Box.createVerticalGlue());
        
        // Wrap in scroll pane
        JScrollPane scroll = new JScrollPane(mainPanel);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }
    
    /**
     * Create the routing mode selection panel with dropdown and description
     */
    private JPanel createRoutingModePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Label
        JLabel label = new JLabel("🎯 Routing Mode");
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setForeground(CYBER_YELLOW);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        
        // Combo box with routing modes
        routingModeCombo = new JComboBox<>(RoutingMode.values());
        routingModeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        routingModeCombo.setBackground(Color.WHITE);
        routingModeCombo.setBorder(BorderFactory.createLineBorder(CYBER_YELLOW, 2, true));
        routingModeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        routingModeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        routingModeCombo.setSelectedItem(RoutingMode.WIDENESS_ONLY); // Default
        
        // Custom renderer for nice display
        routingModeCombo.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index, 
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof RoutingMode) {
                    RoutingMode mode = (RoutingMode) value;
                    setText(mode.getDisplayName());
                    if (isSelected) {
                        setBackground(CYBER_YELLOW);
                        setForeground(Color.WHITE);
                    }
                }
                return this;
            }
        });
        
        panel.add(routingModeCombo);
        panel.add(Box.createVerticalStrut(4));
        
        // Description using JTextArea for proper text wrapping
        routingModeDescriptionArea = new javax.swing.JTextArea(RoutingMode.WIDENESS_ONLY.getDescription());
        routingModeDescriptionArea.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        routingModeDescriptionArea.setForeground(TEXT_SECONDARY);
        routingModeDescriptionArea.setBackground(BG_SURFACE);
        routingModeDescriptionArea.setWrapStyleWord(true);
        routingModeDescriptionArea.setLineWrap(true);
        routingModeDescriptionArea.setEditable(false);
        routingModeDescriptionArea.setFocusable(false);
        routingModeDescriptionArea.setBorder(null);
        routingModeDescriptionArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Constrain width to ~300px so long text wraps within the query panel
        routingModeDescriptionArea.setMaximumSize(new Dimension(320, 60));
        routingModeDescriptionArea.setPreferredSize(new Dimension(320, 40));
        panel.add(routingModeDescriptionArea);
        
        // Update description when selection changes
        routingModeCombo.addActionListener(e -> {
            RoutingMode selected = (RoutingMode) routingModeCombo.getSelectedItem();
            if (selected != null) {
                routingModeDescriptionArea.setText(selected.getDescription());
                // Highlight Pareto mode description
                if (selected.isParetoMode()) {
                    routingModeDescriptionArea.setForeground(CYBER_YELLOW.darker());
                } else {
                    routingModeDescriptionArea.setForeground(TEXT_SECONDARY);
                }
            }
        });
        
        return panel;
    }
    
    /**
     * Create the frontier threshold mode selection panel with dropdown and description
     */
    private JPanel createFrontierThresholdPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Label
        JLabel label = new JLabel("⚡ Search Strategy");
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setForeground(OCEAN_TEAL);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        
        // Combo box with frontier threshold modes
        String[] modes = {"Aggressive (Frontier: 10)", "Balanced (Frontier: 50)"};
        frontierThresholdCombo = new JComboBox<>(modes);
        frontierThresholdCombo.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        frontierThresholdCombo.setBackground(Color.WHITE);
        frontierThresholdCombo.setBorder(BorderFactory.createLineBorder(OCEAN_TEAL, 2, true));
        frontierThresholdCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        frontierThresholdCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        frontierThresholdCombo.setSelectedIndex(0); // Default: Aggressive
        
        // Custom renderer for consistent yellow highlighting
        frontierThresholdCombo.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index, 
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    setBackground(OCEAN_TEAL);
                    setForeground(Color.WHITE);
                }
                return this;
            }
        });
        
        panel.add(frontierThresholdCombo);
        panel.add(Box.createVerticalStrut(4));
        
        // Description label
        frontierThresholdDescription = new JLabel("Faster search, more aggressive pruning");
        frontierThresholdDescription.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        frontierThresholdDescription.setForeground(TEXT_SECONDARY);
        frontierThresholdDescription.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(frontierThresholdDescription);
        
        // Update description when selection changes
        frontierThresholdCombo.addActionListener(e -> {
            int selected = frontierThresholdCombo.getSelectedIndex();
            if (selected == 0) {
                frontierThresholdDescription.setText("Faster search, more aggressive pruning");
            } else {
                frontierThresholdDescription.setText("Thorough search, balanced exploration");
            }
        });
        
        return panel;
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel title = new JLabel("Query Builder");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(ROYAL_INDIGO);
        header.add(title, BorderLayout.WEST);
        
        return header;
    }
    
    /**
     * Creates a single row with Source and Destination fields side by side
     */
    private JPanel createSourceDestRow() {
        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.Y_AXIS));
        rowPanel.setOpaque(false);
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
        rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Label row
        JPanel labelRow = new JPanel(new GridLayout(1, 2, 10, 0));
        labelRow.setOpaque(false);
        labelRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        labelRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel srcLabel = new JLabel("📍 Source");
        srcLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        srcLabel.setForeground(NEON_GREEN);
        labelRow.add(srcLabel);
        
        JLabel destLabel = new JLabel("🎯 Destination");
        destLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        destLabel.setForeground(CORAL_PINK);
        labelRow.add(destLabel);
        
        rowPanel.add(labelRow);
        rowPanel.add(Box.createVerticalStrut(4));
        
        // Field row
        JPanel fieldRow = new JPanel(new GridLayout(1, 2, 10, 0));
        fieldRow.setOpaque(false);
        fieldRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        fieldRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Source field
        sourceField = new JTextField();
        sourceField.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        sourceField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(NEON_GREEN, 2, true),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        sourceField.setBackground(new Color(220, 252, 231)); // Light green
        sourceField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateStatus();
            }
        });
        fieldRow.add(sourceField);
        
        // Destination field
        destField = new JTextField();
        destField.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        destField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CORAL_PINK, 2, true),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        destField.setBackground(new Color(255, 228, 230)); // Light pink
        destField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateStatus();
            }
        });
        fieldRow.add(destField);
        
        rowPanel.add(fieldRow);
        
        return rowPanel;
    }
    
    private JPanel createLabeledCombo(String labelText, Color color, String[] items, boolean isDataset) {
        JPanel panel = new JPanel(new BorderLayout(6, 4));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setForeground(color);
        panel.add(label, BorderLayout.NORTH);
        
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createLineBorder(color, 2, true));
        
        // Custom renderer for consistent color highlighting
        final Color highlightColor = color;
        combo.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index, 
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    setBackground(highlightColor);
                    setForeground(Color.WHITE);
                }
                return this;
            }
        });
        
        // Only heuristic combo is used now (dataset removed)
        heuristicCombo = combo;
        
        panel.add(combo, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createInputField(String labelText, Color color, boolean isSource) {
        JPanel panel = new JPanel(new BorderLayout(6, 4));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setForeground(color);
        panel.add(label, BorderLayout.NORTH);
        
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        field.setBackground(new Color(
            Math.min(255, color.getRed() + 200),
            Math.min(255, color.getGreen() + 200),
            Math.min(255, color.getBlue() + 200)
        ));
        
        if (isSource) {
            sourceField = field;
        } else {
            destField = field;
        }
        
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateStatus();

            }
        });
        
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(createSmallButton("Swap", ELECTRIC_BLUE, () -> {
            String temp = sourceField.getText();
            sourceField.setText(destField.getText());
            destField.setText(temp);
        }));
        
        panel.add(createSmallButton("Clear", SUNSET_ORANGE, () -> {
            sourceField.setText("");
            destField.setText("");
            departureSlider.setValue(0);
            intervalSlider.setValue(10);
            budgetSlider.setValue(60);
            updateStatus();
        }));
        
        return panel;
    }
    
    private JButton createSmallButton(String text, Color color, Runnable action) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color bg = getModel().isRollover() ? color : new Color(color.getRed(), color.getGreen(), color.getBlue(), 50);
                g2d.setColor(bg);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(2f));
                g2d.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 8, 8));
                g2d.dispose();
                
                g.setColor(getModel().isRollover() ? Color.WHITE : color.darker());
                g.setFont(getFont());
                FontMetrics fm = g.getFontMetrics();
                g.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        return btn;
    }
    
    private JPanel createSlidersPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel title = new JLabel("Parameters");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(HOT_PINK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        
        // Departure slider
        JPanel depPanel = createSliderRow("Depart", 0, 1440, 0, ELECTRIC_BLUE, 
            val -> String.format("%02d:%02d", val/60, val%60));
        departureSlider = (JSlider) depPanel.getClientProperty("slider");
        departureValue = (JLabel) depPanel.getClientProperty("value");
        panel.add(depPanel);
        panel.add(Box.createVerticalStrut(8));
        
        // Interval slider
        JPanel intPanel = createSliderRow("Interval", 1, 720, 10, SUNSET_ORANGE, val -> val + "m");
        intervalSlider = (JSlider) intPanel.getClientProperty("slider");
        intervalValue = (JLabel) intPanel.getClientProperty("value");
        panel.add(intPanel);
        panel.add(Box.createVerticalStrut(8));
        
        // Budget slider
        JPanel budPanel = createSliderRow("Budget", 5, 60, 30, LIME_GREEN, val -> String.valueOf(val));
        budgetSlider = (JSlider) budPanel.getClientProperty("slider");
        budgetValue = (JLabel) budPanel.getClientProperty("value");
        panel.add(budPanel);
        
        return panel;
    }
    
    private JPanel createSliderRow(String labelText, int min, int max, int value, Color color, 
                                   java.util.function.IntFunction<String> formatter) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setForeground(color);
        label.setPreferredSize(new Dimension(90, 30));
        row.add(label, BorderLayout.WEST);
        
        JSlider slider = new JSlider(min, max, value);
        slider.setOpaque(false);
        slider.setFocusable(false);
        row.add(slider, BorderLayout.CENTER);
        
        JLabel valLabel = new JLabel(formatter.apply(value));
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valLabel.setForeground(color);
        valLabel.setPreferredSize(new Dimension(60, 30));
        valLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(valLabel, BorderLayout.EAST);
        
        slider.addChangeListener(e -> valLabel.setText(formatter.apply(slider.getValue())));
        
        row.putClientProperty("slider", slider);
        row.putClientProperty("value", valLabel);
        
        return row;
    }
    
    private JPanel createRunPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Add glue for centering
        panel.add(Box.createHorizontalGlue());
        
        // Preview button
        JButton previewButton = new JButton("👁 Preview") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                java.awt.GradientPaint gp;
                if (getModel().isPressed()) {
                    gp = new java.awt.GradientPaint(0, 0, OCEAN_TEAL.darker(), getWidth(), 0, ELECTRIC_BLUE.darker());
                } else if (getModel().isRollover()) {
                    gp = new java.awt.GradientPaint(0, 0, new Color(30, 204, 186), getWidth(), 0, new Color(79, 150, 246));
                } else {
                    gp = new java.awt.GradientPaint(0, 0, OCEAN_TEAL, getWidth(), 0, ELECTRIC_BLUE);
                }
                g2d.setPaint(gp);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                
                // Shine
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.fill(new RoundRectangle2D.Float(2, 2, getWidth()-4, getHeight()/2-2, 12, 12));
                g2d.dispose();
                
                g.setColor(Color.WHITE);
                g.setFont(new Font("Segoe UI", Font.BOLD, 20));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        previewButton.setPreferredSize(new Dimension(200, 55));
        previewButton.setMinimumSize(new Dimension(200, 55));
        previewButton.setMaximumSize(new Dimension(200, 55));
        previewButton.setFocusPainted(false);
        previewButton.setBorderPainted(false);
        previewButton.setContentAreaFilled(false);
        previewButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        previewButton.addActionListener(e -> {
            if (validateInputs()) {
                triggerPreview();
            }
        });
        
        // Run button
        runButton = new JButton("▶ Run") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                java.awt.GradientPaint gp;
                if (getModel().isPressed()) {
                    gp = new java.awt.GradientPaint(0, 0, HOT_PINK.darker(), getWidth(), 0, VIVID_PURPLE.darker());
                } else if (getModel().isRollover()) {
                    gp = new java.awt.GradientPaint(0, 0, new Color(244, 114, 182), getWidth(), 0, new Color(192, 132, 252));
                } else {
                    gp = new java.awt.GradientPaint(0, 0, HOT_PINK, getWidth(), 0, VIVID_PURPLE);
                }
                g2d.setPaint(gp);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                
                // Shine
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.fill(new RoundRectangle2D.Float(2, 2, getWidth()-4, getHeight()/2-2, 12, 12));
                g2d.dispose();
                
                g.setColor(Color.WHITE);
                g.setFont(new Font("Segoe UI", Font.BOLD, 24));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        runButton.setPreferredSize(new Dimension(200, 55));
        runButton.setMinimumSize(new Dimension(200, 55));
        runButton.setMaximumSize(new Dimension(200, 55));
        runButton.setFocusPainted(false);
        runButton.setBorderPainted(false);
        runButton.setContentAreaFilled(false);
        runButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        runButton.addActionListener(e -> {
            if (onRunQuery != null && validateInputs()) {
                onRunQuery.run();
            }
        });
        
        panel.add(previewButton);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(runButton);
        panel.add(Box.createHorizontalGlue());
        return panel;
    }
    
    private void updateStatus() {
        try {
            String srcText = sourceField.getText().trim();
            String dstText = destField.getText().trim();
            
            if (srcText.isEmpty() || dstText.isEmpty()) {
                statusLabel.setText("Enter source and destination");
                statusLabel.setForeground(TEXT_SECONDARY);
            } else {
                int src = Integer.parseInt(srcText);
                int dst = Integer.parseInt(dstText);
                
                if (src < 1 || src > maxNodeId || dst < 1 || dst > maxNodeId) {
                    statusLabel.setText("Node ID: 1-" + maxNodeId);
                    statusLabel.setForeground(CORAL_PINK);
                } else if (src == dst) {
                    statusLabel.setText("Source = Destination");
                    statusLabel.setForeground(SUNSET_ORANGE);
                } else {
                    statusLabel.setText("Ready! Click Run to find route");
                    statusLabel.setForeground(NEON_GREEN);
                }
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Enter valid numbers");
            statusLabel.setForeground(CORAL_PINK);
        }
    }
    
    private boolean validateInputs() {
        try {
            int src = Integer.parseInt(sourceField.getText().trim());
            int dst = Integer.parseInt(destField.getText().trim());
            return src >= 1 && src <= maxNodeId && dst >= 1 && dst <= maxNodeId;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private void triggerPreview() {
        if (onPreviewChange != null) {
            try {
                int src = Integer.parseInt(sourceField.getText().trim());
                int dst = Integer.parseInt(destField.getText().trim());
                onPreviewChange.accept(src, dst);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
    }
    
    // === PUBLIC API ===
    
    public void setOnRunQuery(Runnable callback) { this.onRunQuery = callback; }
    public void setOnPreviewChange(java.util.function.BiConsumer<Integer, Integer> callback) { this.onPreviewChange = callback; }
    
    public int getSource() { return Integer.parseInt(sourceField.getText().trim()); }
    public int getDestination() { return Integer.parseInt(destField.getText().trim()); }
    public int getDeparture() { return departureSlider.getValue(); }
    public int getInterval() { return intervalSlider.getValue(); }
    public int getBudget() { return budgetSlider.getValue(); }
    
    public int getHeuristicMode() {
        int idx = heuristicCombo.getSelectedIndex();
        return idx == 0 ? 3 : (idx == 1 ? 1 : 2);
    }
    
    /**
     * Get the user-selected routing mode
     */
    public RoutingMode getRoutingMode() {
        return (RoutingMode) routingModeCombo.getSelectedItem();
    }
    
    /**
     * Set the routing mode programmatically
     */
    public void setRoutingMode(RoutingMode mode) {
        if (mode != null) {
            routingModeCombo.setSelectedItem(mode);
        }
    }
    
    /**
     * Get the selected frontier threshold mode (0 = Aggressive, 1 = Balanced)
     */
    public int getFrontierThresholdMode() {
        return frontierThresholdCombo.getSelectedIndex();
    }
    
    /**
     * Set the frontier threshold mode programmatically (0 = Aggressive, 1 = Balanced)
     */
    public void setFrontierThresholdMode(int mode) {
        if (mode >= 0 && mode <= 1) {
            frontierThresholdCombo.setSelectedIndex(mode);
        }
    }
    
    public void setSource(int value) { sourceField.setText(String.valueOf(value)); updateStatus(); }
    public void setDestination(int value) { destField.setText(String.valueOf(value)); updateStatus(); }
    public void setMaxNodeId(int max) { this.maxNodeId = max; updateStatus(); }
    
    public void setRunning(boolean running) {
        runButton.setEnabled(!running);
        runButton.setText(running ? "Processing..." : "▶ Run");
        statusLabel.setText(running ? "Running query..." : "Ready!");
    }
}
