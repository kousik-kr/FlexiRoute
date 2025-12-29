package ui.panels;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import ui.components.ModernButton;

/**
 * World-Class Query Input Panel with enhanced UX
 * - Smart input validation
 * - Quick presets
 * - Visual feedback
 * - City/Dataset selector
 * - Recent queries
 */
public class WorldClassQueryPanel extends JPanel {
    
    // Colors
    private static final Color PRIMARY = new Color(33, 150, 243);
    private static final Color PRIMARY_DARK = new Color(25, 118, 210);
    private static final Color SUCCESS = new Color(76, 175, 80);
    private static final Color ERROR = new Color(244, 67, 54);
    private static final Color WARNING = new Color(255, 152, 0);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER = new Color(230, 230, 230);
    
    // Components
    private JTextField sourceField, destField;
    private JSlider departureSlider, intervalSlider, budgetSlider;
    private JLabel departureValue, intervalValue, budgetValue;
    private JComboBox<String> heuristicCombo, datasetCombo;
    private JButton runButton, randomButton, swapButton, clearButton;
    private JPanel presetPanel;
    private JTextArea validationArea;
    private JLabel statusIcon;
    
    // Callbacks
    private Runnable onRunQuery;
    private Runnable onRandomQuery;
    private java.util.function.BiConsumer<Integer, Integer> onPreviewChange;
    
    // State
    private int maxNodeId = 21048;
    
    public WorldClassQueryPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        
        initComponents();
    }
    
    private void initComponents() {
        // Main container with card style
        JPanel mainCard = createCard();
        mainCard.setLayout(new BoxLayout(mainCard, BoxLayout.Y_AXIS));
        
        // Header
        JPanel header = createHeader();
        mainCard.add(header);
        mainCard.add(Box.createVerticalStrut(15));
        mainCard.add(new JSeparator());
        mainCard.add(Box.createVerticalStrut(15));
        
        // Dataset selector
        JPanel datasetSection = createDatasetSection();
        mainCard.add(datasetSection);
        mainCard.add(Box.createVerticalStrut(15));
        
        // Source/Destination inputs
        JPanel nodeInputSection = createNodeInputSection();
        mainCard.add(nodeInputSection);
        mainCard.add(Box.createVerticalStrut(15));
        
        // Quick actions
        JPanel quickActions = createQuickActions();
        mainCard.add(quickActions);
        mainCard.add(Box.createVerticalStrut(15));
        
        // Sliders
        JPanel slidersSection = createSlidersSection();
        mainCard.add(slidersSection);
        mainCard.add(Box.createVerticalStrut(15));
        
        // Algorithm selector
        JPanel algoSection = createAlgorithmSection();
        mainCard.add(algoSection);
        mainCard.add(Box.createVerticalStrut(15));
        
        // Presets
        JPanel presetSection = createPresetSection();
        mainCard.add(presetSection);
        mainCard.add(Box.createVerticalStrut(20));
        
        // Run button
        JPanel buttonSection = createButtonSection();
        mainCard.add(buttonSection);
        mainCard.add(Box.createVerticalStrut(15));
        
        // Validation feedback
        JPanel validationSection = createValidationSection();
        mainCard.add(validationSection);
        
        mainCard.add(Box.createVerticalGlue());
        
        // Scroll pane
        JScrollPane scroll = new JScrollPane(mainCard);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }
    
    private JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_BG);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        return card;
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JLabel icon = new JLabel("🗺️");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        
        JLabel title = new JLabel("Query Builder");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(40, 40, 40));
        
        JLabel subtitle = new JLabel("Configure your pathfinding query");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(120, 120, 120));
        
        titlePanel.add(title);
        titlePanel.add(subtitle);
        
        header.add(icon, BorderLayout.WEST);
        header.add(Box.createHorizontalStrut(15));
        header.add(titlePanel, BorderLayout.CENTER);
        
        statusIcon = new JLabel("⚪");
        statusIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        statusIcon.setToolTipText("Ready");
        header.add(statusIcon, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createDatasetSection() {
        JPanel section = new JPanel(new BorderLayout(10, 5));
        section.setOpaque(false);
        
        JLabel label = createSectionLabel("📁 Dataset");
        section.add(label, BorderLayout.NORTH);
        
        datasetCombo = new JComboBox<>(new String[]{
            "21048 - Default Road Network",
            "California - Full State",
            "Custom Dataset..."
        });
        styleComboBox(datasetCombo);
        datasetCombo.addActionListener(e -> {
            String selected = (String) datasetCombo.getSelectedItem();
            if (selected != null && selected.contains("21048")) {
                maxNodeId = 21048;
            } else if (selected != null && selected.contains("California")) {
                maxNodeId = 100000; // Adjust as needed
            }
            updateValidation();
        });
        
        section.add(datasetCombo, BorderLayout.CENTER);
        return section;
    }
    
    private JPanel createNodeInputSection() {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        
        JLabel label = createSectionLabel("📍 Endpoints");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(label);
        section.add(Box.createVerticalStrut(10));
        
        // Source row
        JPanel sourceRow = createInputRow("Source Node", "🟢");
        sourceField = (JTextField) ((JPanel) sourceRow.getComponent(1)).getComponent(0);
        sourceRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(sourceRow);
        section.add(Box.createVerticalStrut(8));
        
        // Destination row
        JPanel destRow = createInputRow("Destination Node", "🔴");
        destField = (JTextField) ((JPanel) destRow.getComponent(1)).getComponent(0);
        destRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(destRow);
        
        // Add change listeners for preview
        sourceField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateValidation();
                triggerPreview();
            }
        });
        destField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateValidation();
                triggerPreview();
            }
        });
        
        return section;
    }
    
    private JPanel createInputRow(String label, String icon) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        JLabel iconLabel = new JLabel(icon + " " + label);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        iconLabel.setPreferredSize(new Dimension(130, 30));
        row.add(iconLabel, BorderLayout.WEST);
        
        JPanel inputWrapper = new JPanel(new BorderLayout());
        inputWrapper.setOpaque(false);
        
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        inputWrapper.add(field, BorderLayout.CENTER);
        row.add(inputWrapper, BorderLayout.CENTER);
        
        return row;
    }
    
    private JPanel createQuickActions() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        
        swapButton = createSmallButton("🔄 Swap", "Swap source and destination");
        swapButton.addActionListener(e -> {
            String temp = sourceField.getText();
            sourceField.setText(destField.getText());
            destField.setText(temp);
            triggerPreview();
        });
        
        randomButton = createSmallButton("🎲 Random", "Generate random endpoints");
        randomButton.addActionListener(e -> {
            if (onRandomQuery != null) {
                onRandomQuery.run();
            } else {
                Random rand = new Random();
                sourceField.setText(String.valueOf(rand.nextInt(maxNodeId) + 1));
                destField.setText(String.valueOf(rand.nextInt(maxNodeId) + 1));
                updateValidation();
                triggerPreview();
            }
        });
        
        clearButton = createSmallButton("🗑️ Clear", "Clear all inputs");
        clearButton.addActionListener(e -> {
            sourceField.setText("");
            destField.setText("");
            departureSlider.setValue(0);
            intervalSlider.setValue(10);
            budgetSlider.setValue(60);
            updateValidation();
        });
        
        panel.add(swapButton);
        panel.add(randomButton);
        panel.add(clearButton);
        
        return panel;
    }
    
    private JPanel createSlidersSection() {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        
        JLabel label = createSectionLabel("⚙️ Parameters");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(label);
        section.add(Box.createVerticalStrut(10));
        
        // Departure time
        JPanel depRow = createSliderRow("🕐 Departure Time", 0, 1440, 0, val -> {
            int hours = val / 60;
            int mins = val % 60;
            return String.format("%02d:%02d", hours, mins);
        });
        departureSlider = (JSlider) depRow.getComponent(1);
        departureValue = (JLabel) depRow.getComponent(2);
        section.add(depRow);
        section.add(Box.createVerticalStrut(8));
        
        // Time interval
        JPanel intRow = createSliderRow("⏱️ Time Interval", 1, 60, 10, val -> val + " min");
        intervalSlider = (JSlider) intRow.getComponent(1);
        intervalValue = (JLabel) intRow.getComponent(2);
        section.add(intRow);
        section.add(Box.createVerticalStrut(8));
        
        // Budget
        JPanel budgetRow = createSliderRow("💰 Travel Budget", 10, 500, 60, val -> val + " units");
        budgetSlider = (JSlider) budgetRow.getComponent(1);
        budgetValue = (JLabel) budgetRow.getComponent(2);
        section.add(budgetRow);
        
        return section;
    }
    
    private JPanel createSliderRow(String label, int min, int max, int value, java.util.function.IntFunction<String> formatter) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelComp.setPreferredSize(new Dimension(130, 30));
        row.add(labelComp, BorderLayout.WEST);
        
        JSlider slider = new JSlider(min, max, value);
        slider.setOpaque(false);
        slider.setFocusable(false);
        row.add(slider, BorderLayout.CENTER);
        
        JLabel valueLabel = new JLabel(formatter.apply(value));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        valueLabel.setForeground(PRIMARY);
        valueLabel.setPreferredSize(new Dimension(70, 30));
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(valueLabel, BorderLayout.EAST);
        
        slider.addChangeListener(e -> valueLabel.setText(formatter.apply(slider.getValue())));
        
        return row;
    }
    
    private JPanel createAlgorithmSection() {
        JPanel section = new JPanel(new BorderLayout(10, 5));
        section.setOpaque(false);
        
        JLabel label = createSectionLabel("🧠 Algorithm");
        section.add(label, BorderLayout.NORTH);
        
        heuristicCombo = new JComboBox<>(new String[]{
            "🚀 Best Bounds (Recommended)",
            "📏 Euclidean Distance",
            "🔲 Manhattan Distance",
            "⭕ No Heuristic"
        });
        styleComboBox(heuristicCombo);
        section.add(heuristicCombo, BorderLayout.CENTER);
        
        return section;
    }
    
    private JPanel createPresetSection() {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        
        JLabel label = createSectionLabel("⚡ Quick Presets");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(label);
        section.add(Box.createVerticalStrut(8));
        
        presetPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        presetPanel.setOpaque(false);
        presetPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        presetPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        addPreset("🏃 Short Trip", 1, 100, 30);
        addPreset("🚗 Medium Route", 1, 5000, 60);
        addPreset("✈️ Long Journey", 1, 15000, 120);
        addPreset("🎯 Cross City", 100, 20000, 200);
        
        section.add(presetPanel);
        return section;
    }
    
    private void addPreset(String name, int src, int dst, int budget) {
        JButton btn = new JButton(name);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(new Color(240, 245, 250));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(230, 240, 250));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(240, 245, 250));
            }
        });
        
        btn.addActionListener(e -> {
            sourceField.setText(String.valueOf(src));
            destField.setText(String.valueOf(dst));
            budgetSlider.setValue(budget);
            updateValidation();
            triggerPreview();
        });
        
        presetPanel.add(btn);
    }
    
    private JPanel createButtonSection() {
        JPanel section = new JPanel(new BorderLayout(10, 0));
        section.setOpaque(false);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        runButton = new JButton("🔍  Find Wide Path") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(PRIMARY_DARK.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(PRIMARY_DARK);
                } else {
                    g2d.setColor(PRIMARY);
                }
                
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2d.dispose();
                
                g.setColor(Color.WHITE);
                g.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g.drawString(text, x, y);
            }
        };
        
        runButton.setPreferredSize(new Dimension(0, 45));
        runButton.setFocusPainted(false);
        runButton.setBorderPainted(false);
        runButton.setContentAreaFilled(false);
        runButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        runButton.addActionListener(e -> {
            if (onRunQuery != null && validateInputs()) {
                onRunQuery.run();
            }
        });
        
        section.add(runButton, BorderLayout.CENTER);
        return section;
    }
    
    private JPanel createValidationSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setOpaque(false);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        validationArea = new JTextArea(2, 30);
        validationArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        validationArea.setEditable(false);
        validationArea.setOpaque(false);
        validationArea.setForeground(new Color(100, 100, 100));
        validationArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        validationArea.setLineWrap(true);
        validationArea.setWrapStyleWord(true);
        
        section.add(validationArea, BorderLayout.CENTER);
        return section;
    }
    
    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(80, 80, 80));
        return label;
    }
    
    private JButton createSmallButton(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setToolTipText(tooltip);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(new Color(245, 245, 245));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return btn;
    }
    
    private void styleComboBox(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }
    
    private void updateValidation() {
        StringBuilder sb = new StringBuilder();
        boolean valid = true;
        
        try {
            String srcText = sourceField.getText().trim();
            String dstText = destField.getText().trim();
            
            if (srcText.isEmpty() || dstText.isEmpty()) {
                sb.append("⚠️ Enter source and destination nodes to begin");
                statusIcon.setText("⚪");
                statusIcon.setToolTipText("Waiting for input");
            } else {
                int src = Integer.parseInt(srcText);
                int dst = Integer.parseInt(dstText);
                
                if (src < 1 || src > maxNodeId) {
                    sb.append("❌ Source must be between 1 and ").append(maxNodeId).append("\n");
                    valid = false;
                }
                if (dst < 1 || dst > maxNodeId) {
                    sb.append("❌ Destination must be between 1 and ").append(maxNodeId).append("\n");
                    valid = false;
                }
                if (src == dst) {
                    sb.append("⚠️ Source and destination are the same\n");
                }
                
                if (valid && sb.length() == 0) {
                    sb.append("✅ Ready to find the optimal wide path!");
                    statusIcon.setText("🟢");
                    statusIcon.setToolTipText("Ready to run");
                } else if (!valid) {
                    statusIcon.setText("🔴");
                    statusIcon.setToolTipText("Invalid input");
                } else {
                    statusIcon.setText("🟡");
                    statusIcon.setToolTipText("Warning");
                }
            }
        } catch (NumberFormatException e) {
            sb.append("❌ Please enter valid numeric node IDs");
            valid = false;
            statusIcon.setText("🔴");
            statusIcon.setToolTipText("Invalid input");
        }
        
        validationArea.setText(sb.toString());
        runButton.setEnabled(valid);
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
                // Invalid input, ignore preview
            }
        }
    }
    
    // === PUBLIC API ===
    
    public void setOnRunQuery(Runnable callback) {
        this.onRunQuery = callback;
    }
    
    public void setOnRandomQuery(Runnable callback) {
        this.onRandomQuery = callback;
    }
    
    public void setOnPreviewChange(java.util.function.BiConsumer<Integer, Integer> callback) {
        this.onPreviewChange = callback;
    }
    
    public int getSource() {
        return Integer.parseInt(sourceField.getText().trim());
    }
    
    public int getDestination() {
        return Integer.parseInt(destField.getText().trim());
    }
    
    public int getDeparture() {
        return departureSlider.getValue();
    }
    
    public int getInterval() {
        return intervalSlider.getValue();
    }
    
    public int getBudget() {
        return budgetSlider.getValue();
    }
    
    public int getHeuristicMode() {
        int index = heuristicCombo.getSelectedIndex();
        return index == 0 ? 3 : (index == 1 ? 1 : (index == 2 ? 2 : 0));
    }
    
    public void setSource(int value) {
        sourceField.setText(String.valueOf(value));
        updateValidation();
    }
    
    public void setDestination(int value) {
        destField.setText(String.valueOf(value));
        updateValidation();
    }
    
    public void setMaxNodeId(int max) {
        this.maxNodeId = max;
        updateValidation();
    }
    
    public void setRunning(boolean running) {
        runButton.setEnabled(!running);
        runButton.setText(running ? "⏳  Processing..." : "🔍  Find Wide Path");
        statusIcon.setText(running ? "🔵" : "🟢");
        statusIcon.setToolTipText(running ? "Running query..." : "Ready");
    }
}
