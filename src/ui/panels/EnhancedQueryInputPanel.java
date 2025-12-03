package ui.panels;

import ui.components.ModernButton;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;
import java.util.*;
import java.util.List;

/**
 * Enhanced query input panel with smart features, validation, and presets
 */
public class EnhancedQueryInputPanel extends JPanel {
    private final JSpinner sourceSpinner;
    private final JSpinner destSpinner;
    private final JSpinner departureSpinner;
    private final JSpinner intervalSpinner;
    private final JSpinner budgetSpinner;
    private final ModernButton runButton;
    private final ModernButton clearButton;
    private final ModernButton resetButton;
    private final ModernButton exitButton;
    private final int maxNodeId;
    private JPanel actionPanel;
    private boolean queryExecuted = false;
    
    // Smart features
    private JComboBox<QueryPreset> presetSelector;
    private JButton savePresetBtn;
    private JButton randomQueryBtn;
    private List<QueryPreset> recentQueries = new ArrayList<>();
    private JButton showRecentBtn;
    
    // Validation feedback
    private JLabel validationLabel;
    private javax.swing.Timer validationTimer;

    public EnhancedQueryInputPanel(int maxNodeId, Consumer<QueryParameters> onRunQuery, 
                                   Runnable onReset, Runnable onExit) {
        this.maxNodeId = maxNodeId;
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(33, 150, 243), 2),
                " Query Configuration ",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                new Color(33, 150, 243)
            ),
            BorderFactory.createEmptyBorder(15, 20, 20, 20)
        ));
        
        // Create spinners with appropriate ranges
        sourceSpinner = createStyledSpinner(0, 0, maxNodeId, 1);
        destSpinner = createStyledSpinner(0, 0, maxNodeId, 1);
        departureSpinner = createStyledSpinner(450, 0, 1440, 15);
        intervalSpinner = createStyledSpinner(360, 1, 1440, 30);
        budgetSpinner = createStyledSpinner(45, 1, 500, 5);
        
        // Add validation listeners
        addValidationListeners();
        
        // Create preset panel
        createPresetPanel();
        
        // Input fields panel
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        fieldsPanel.add(createFieldRow("🎯 Source Node:", sourceSpinner), gbc);
        fieldsPanel.add(createFieldRow("🏁 Destination Node:", destSpinner), gbc);
        fieldsPanel.add(createFieldRow("🕐 Departure Time (min):", departureSpinner), gbc);
        fieldsPanel.add(createFieldRow("⏱ Interval Duration (min):", intervalSpinner), gbc);
        fieldsPanel.add(createFieldRow("💰 Budget (min):", budgetSpinner), gbc);
        
        // Validation feedback
        validationLabel = new JLabel(" ");
        validationLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        validationLabel.setForeground(new Color(244, 67, 54));
        gbc.insets = new Insets(0, 0, 10, 0);
        fieldsPanel.add(validationLabel, gbc);

        // Buttons panel - Initial state
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 1, 0, 12));
        runButton = new ModernButton("▶ Run Query", new Color(76, 175, 80));
        clearButton = new ModernButton("🔄 Clear All", new Color(158, 158, 158));

        runButton.setPreferredSize(new Dimension(220, 45));
        clearButton.setPreferredSize(new Dimension(220, 45));
        
        // Enhanced keyboard shortcuts
        setupKeyboardShortcuts();

        runButton.addActionListener(e -> {
            if (validateInputs()) {
                QueryParameters params = new QueryParameters(
                    (Integer) sourceSpinner.getValue(),
                    (Integer) destSpinner.getValue(),
                    (Integer) departureSpinner.getValue(),
                    (Integer) intervalSpinner.getValue(),
                    (Integer) budgetSpinner.getValue()
                );
                saveToRecentQueries(params);
                onRunQuery.accept(params);
                queryExecuted = true;
            }
        });

        clearButton.addActionListener(e -> clearFields());

        buttonsPanel.add(runButton);
        buttonsPanel.add(clearButton);

        // Action panel - Appears after query execution
        actionPanel = new JPanel(new GridLayout(2, 1, 0, 12));
        resetButton = new ModernButton("🔄 New Query", new Color(33, 150, 243));
        exitButton = new ModernButton("🚪 Exit System", new Color(244, 67, 54));

        resetButton.setPreferredSize(new Dimension(220, 45));
        exitButton.setPreferredSize(new Dimension(220, 45));

        resetButton.addActionListener(e -> {
            onReset.run();
            resetToInitialState();
        });

        exitButton.addActionListener(e -> onExit.run());

        actionPanel.add(resetButton);
        actionPanel.add(exitButton);
        
        // Quick actions panel
        JPanel quickActionsPanel = createQuickActionsPanel();

        // Layout
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.add(fieldsPanel, BorderLayout.CENTER);
        centerPanel.add(quickActionsPanel, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    private void createPresetPanel() {
        JPanel presetPanel = new JPanel(new BorderLayout(5, 0));
        presetPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel presetLabel = new JLabel("Presets:");
        presetLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        
        presetSelector = new JComboBox<>(new QueryPreset[]{
            new QueryPreset("Custom", 0, 0, 450, 360, 45),
            new QueryPreset("Quick Test", 10, 50, 400, 300, 40),
            new QueryPreset("Long Distance", 0, 100, 600, 400, 80),
            new QueryPreset("Morning Rush", 25, 75, 480, 360, 60)
        });
        
        presetSelector.addActionListener(e -> {
            QueryPreset preset = (QueryPreset) presetSelector.getSelectedItem();
            if (preset != null && !preset.name.equals("Custom")) {
                applyPreset(preset);
            }
        });
        
        savePresetBtn = new JButton("💾");
        savePresetBtn.setToolTipText("Save current values as preset");
        savePresetBtn.setMargin(new Insets(2, 8, 2, 8));
        
        presetPanel.add(presetLabel, BorderLayout.WEST);
        presetPanel.add(presetSelector, BorderLayout.CENTER);
        presetPanel.add(savePresetBtn, BorderLayout.EAST);
        
        add(presetPanel, BorderLayout.NORTH);
    }
    
    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        randomQueryBtn = new JButton("🎲 Random");
        randomQueryBtn.setToolTipText("Generate random query parameters");
        randomQueryBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        randomQueryBtn.addActionListener(e -> generateRandomQuery());
        
        showRecentBtn = new JButton("📋 Recent");
        showRecentBtn.setToolTipText("Show recent queries");
        showRecentBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        showRecentBtn.addActionListener(e -> showRecentQueries());
        
        JButton swapBtn = new JButton("⇄ Swap");
        swapBtn.setToolTipText("Swap source and destination");
        swapBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        swapBtn.addActionListener(e -> swapSourceDest());
        
        panel.add(randomQueryBtn);
        panel.add(showRecentBtn);
        panel.add(swapBtn);
        
        return panel;
    }
    
    private void addValidationListeners() {
        ChangeListener validator = e -> scheduleValidation();
        
        sourceSpinner.addChangeListener(validator);
        destSpinner.addChangeListener(validator);
        departureSpinner.addChangeListener(validator);
        intervalSpinner.addChangeListener(validator);
        budgetSpinner.addChangeListener(validator);
    }
    
    private void scheduleValidation() {
        if (validationTimer != null && validationTimer.isRunning()) {
            validationTimer.restart();
        } else {
            validationTimer = new javax.swing.Timer(500, e -> {
                validateInputs();
                validationTimer.stop();
            });
            validationTimer.setRepeats(false);
            validationTimer.start();
        }
    }
    
    private boolean validateInputs() {
        int source = (Integer) sourceSpinner.getValue();
        int dest = (Integer) destSpinner.getValue();
        int budget = (Integer) budgetSpinner.getValue();
        
        if (source == dest) {
            validationLabel.setText("⚠ Source and destination must be different");
            validationLabel.setForeground(new Color(255, 193, 7));
            return false;
        }
        
        if (budget < 10) {
            validationLabel.setText("⚠ Budget might be too low for pathfinding");
            validationLabel.setForeground(new Color(255, 193, 7));
            return true; // Warning, but allow
        }
        
        validationLabel.setText("✓ Parameters look good");
        validationLabel.setForeground(new Color(76, 175, 80));
        return true;
    }
    
    private void applyPreset(QueryPreset preset) {
        sourceSpinner.setValue(preset.source);
        destSpinner.setValue(preset.dest);
        departureSpinner.setValue(preset.departure);
        intervalSpinner.setValue(preset.interval);
        budgetSpinner.setValue(preset.budget);
    }
    
    private void generateRandomQuery() {
        Random rand = new Random();
        sourceSpinner.setValue(rand.nextInt(maxNodeId + 1));
        destSpinner.setValue(rand.nextInt(maxNodeId + 1));
        departureSpinner.setValue(rand.nextInt(1440));
        intervalSpinner.setValue(180 + rand.nextInt(600));
        budgetSpinner.setValue(20 + rand.nextInt(200));
    }
    
    private void swapSourceDest() {
        int temp = (Integer) sourceSpinner.getValue();
        sourceSpinner.setValue(destSpinner.getValue());
        destSpinner.setValue(temp);
    }
    
    private void saveToRecentQueries(QueryParameters params) {
        recentQueries.add(0, new QueryPreset(
            "Recent " + (recentQueries.size() + 1),
            params.source, params.destination, params.departure,
            params.interval, params.budget
        ));
        if (recentQueries.size() > 10) {
            recentQueries.remove(recentQueries.size() - 1);
        }
    }
    
    private void showRecentQueries() {
        if (recentQueries.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No recent queries available", 
                "Recent Queries", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String[] options = recentQueries.stream()
            .map(q -> q.toString())
            .toArray(String[]::new);
        
        String selected = (String) JOptionPane.showInputDialog(
            this,
            "Select a recent query to load:",
            "Recent Queries",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        if (selected != null) {
            int index = Arrays.asList(options).indexOf(selected);
            if (index >= 0) {
                applyPreset(recentQueries.get(index));
            }
        }
    }
    
    private void setupKeyboardShortcuts() {
        // Ctrl+Enter to run query
        runButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "runQuery");
        runButton.getActionMap().put("runQuery", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runButton.doClick();
            }
        });
        
        // Ctrl+R for random query
        randomQueryBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK), "random");
        randomQueryBtn.getActionMap().put("random", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                randomQueryBtn.doClick();
            }
        });
    }

    private JSpinner createStyledSpinner(int initial, int min, int max, int step) {
        SpinnerNumberModel model = new SpinnerNumberModel(initial, min, max, step);
        JSpinner spinner = new JSpinner(model);
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(8);
        return spinner;
    }

    private JPanel createFieldRow(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        labelComp.setPreferredSize(new Dimension(200, 25));

        row.add(labelComp, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);

        return row;
    }

    private void clearFields() {
        sourceSpinner.setValue(0);
        destSpinner.setValue(0);
        departureSpinner.setValue(450);
        intervalSpinner.setValue(360);
        budgetSpinner.setValue(45);
        validationLabel.setText(" ");
    }

    private void resetToInitialState() {
        clearFields();
        queryExecuted = false;
        remove(actionPanel);
        // Re-add initial buttons if needed
        revalidate();
        repaint();
    }

    public void setRunEnabled(boolean enabled) {
        runButton.setEnabled(enabled);
    }

    public static class QueryParameters {
        public final int source;
        public final int destination;
        public final int departure;
        public final int interval;
        public final int budget;

        public QueryParameters(int source, int destination, int departure, int interval, int budget) {
            this.source = source;
            this.destination = destination;
            this.departure = departure;
            this.interval = interval;
            this.budget = budget;
        }
    }
    
    private static class QueryPreset {
        String name;
        int source, dest, departure, interval, budget;
        
        QueryPreset(String name, int source, int dest, int departure, int interval, int budget) {
            this.name = name;
            this.source = source;
            this.dest = dest;
            this.departure = departure;
            this.interval = interval;
            this.budget = budget;
        }
        
        @Override
        public String toString() {
            if (name.equals("Custom")) {
                return name;
            }
            return String.format("%s (%d→%d, dept:%d, intv:%d, bdgt:%d)", 
                name, source, dest, departure, interval, budget);
        }
    }
}
