package ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import ui.components.ModernButton;

/**
 * Modern input panel with validation and styling
 */
public class QueryInputPanel extends JPanel {
    private final JSpinner sourceSpinner;
    private final JSpinner destSpinner;
    private final JSpinner departureSpinner;
    private final JSpinner intervalSpinner;
    private final JSpinner budgetSpinner;
    private final JComboBox<String> heuristicModeCombo;
    private final ModernButton runButton;
    private final ModernButton clearButton;
    private final ModernButton visualizeButton;
    private final ModernButton resetButton;
    private final ModernButton exitButton;
    private final int maxNodeId;
    private JPanel actionPanel;
    private boolean queryExecuted = false;
    private final Consumer<QueryPreview> visualizeQueryConsumer;

    public QueryInputPanel(int maxNodeId, Consumer<QueryParameters> onRunQuery, Consumer<QueryPreview> visualizeQuery, Runnable onReset, Runnable onExit) {
        this.maxNodeId = maxNodeId;
        this.visualizeQueryConsumer = visualizeQuery;
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(33, 150, 243), 2),
                " Query Parameters ",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(33, 150, 243)
            ),
            BorderFactory.createEmptyBorder(10, 15, 15, 15)
        ));

        // Input fields panel
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        // Create spinners with appropriate ranges
        sourceSpinner = createStyledSpinner(0, 0, maxNodeId, 1);
        destSpinner = createStyledSpinner(0, 0, maxNodeId, 1);
        departureSpinner = createStyledSpinner(450, 0, 1440, 15);
        intervalSpinner = createStyledSpinner(360, 1, 1440, 30);
        budgetSpinner = createStyledSpinner(45, 1, 500, 5);

        // Heuristic mode combo box
        String[] heuristicModes = {"🎯 Aggressive (10 frontiers)", "⚖️ Balanced (50 frontiers)"};
        heuristicModeCombo = new JComboBox<>(heuristicModes);
        heuristicModeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        heuristicModeCombo.setSelectedIndex(0); // Default: Aggressive
        
        fieldsPanel.add(createFieldRow("🎯 Source Node ID:", sourceSpinner), gbc);
        fieldsPanel.add(createFieldRow("🏁 Destination Node ID:", destSpinner), gbc);
        fieldsPanel.add(createFieldRow("🕐 Departure Time (min):", departureSpinner), gbc);
        fieldsPanel.add(createFieldRow("⏱ Interval Duration (min):", intervalSpinner), gbc);
        fieldsPanel.add(createFieldRow("💰 Budget (min):", budgetSpinner), gbc);
        fieldsPanel.add(createFieldRow("🧠 Heuristic Mode:", heuristicModeCombo), gbc);

        // Buttons panel - Initial state
        JPanel buttonsPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        runButton = new ModernButton("▶ Run Query", new Color(76, 175, 80));
        visualizeButton = new ModernButton("👁 Visualize Query", new Color(156, 39, 176));
        clearButton = new ModernButton("🔄 Clear Fields", new Color(158, 158, 158));

        runButton.setPreferredSize(new Dimension(200, 40));
        visualizeButton.setPreferredSize(new Dimension(200, 40));
        clearButton.setPreferredSize(new Dimension(200, 40));
        
        visualizeButton.setToolTipText("Preview source and destination nodes on map");

        runButton.addActionListener(e -> {
            QueryParameters params = new QueryParameters(
                (Integer) sourceSpinner.getValue(),
                (Integer) destSpinner.getValue(),
                (Integer) departureSpinner.getValue(),
                (Integer) intervalSpinner.getValue(),
                (Integer) budgetSpinner.getValue(),
                heuristicModeCombo.getSelectedIndex() == 0 ? "Aggressive" : "Balanced"
            );
            onRunQuery.accept(params);
            queryExecuted = true;
        });

        clearButton.addActionListener(e -> clearFields());
        
        visualizeButton.addActionListener(e -> {
            int source = (Integer) sourceSpinner.getValue();
            int dest = (Integer) destSpinner.getValue();
            visualizeQueryConsumer.accept(new QueryPreview(source, dest));
        });

        buttonsPanel.add(runButton);
        buttonsPanel.add(visualizeButton);
        buttonsPanel.add(clearButton);

        // Action panel - Appears after query execution
        actionPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        resetButton = new ModernButton("🔄 New Query", new Color(33, 150, 243));
        exitButton = new ModernButton("🚪 Exit System", new Color(244, 67, 54));

        resetButton.setPreferredSize(new Dimension(200, 40));
        exitButton.setPreferredSize(new Dimension(200, 40));

        resetButton.addActionListener(e -> {
            onReset.run();
            resetToInitialState();
        });

        exitButton.addActionListener(e -> onExit.run());

        actionPanel.add(resetButton);
        actionPanel.add(exitButton);
        actionPanel.setVisible(false);

        // Container for switching between button panels
        JPanel buttonContainer = new JPanel(new BorderLayout());
        buttonContainer.add(buttonsPanel, BorderLayout.NORTH);
        buttonContainer.add(actionPanel, BorderLayout.CENTER);

        add(fieldsPanel, BorderLayout.CENTER);
        add(buttonContainer, BorderLayout.SOUTH);
    }

    private JPanel createFieldRow(String label, JSpinner spinner) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(spinner, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createFieldRow(String label, JComboBox<String> comboBox) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(comboBox, BorderLayout.CENTER);
        return panel;
    }

    private JSpinner createStyledSpinner(int value, int min, int max, int step) {
        SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, step);
        JSpinner spinner = new JSpinner(model);
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(10);
        return spinner;
    }

    private void clearFields() {
        sourceSpinner.setValue(0);
        destSpinner.setValue(0);
        departureSpinner.setValue(450);
        intervalSpinner.setValue(360);
        budgetSpinner.setValue(45);
    }

    public void setRunEnabled(boolean enabled) {
        runButton.setEnabled(enabled);
    }

    public void showActionButtons() {
        // Disable input controls
        sourceSpinner.setEnabled(false);
        destSpinner.setEnabled(false);
        departureSpinner.setEnabled(false);
        intervalSpinner.setEnabled(false);
        budgetSpinner.setEnabled(false);
        heuristicModeCombo.setEnabled(false);
        runButton.setVisible(false);
        visualizeButton.setVisible(false);
        clearButton.setVisible(false);
        actionPanel.setVisible(true);
        
        // Animate the transition
        Timer animationTimer = new Timer(10, null);
        final float[] alpha = {0.0f};
        animationTimer.addActionListener(e -> {
            alpha[0] += 0.1f;
            if (alpha[0] >= 1.0f) {
                alpha[0] = 1.0f;
                ((Timer) e.getSource()).stop();
            }
            actionPanel.repaint();
        });
        animationTimer.start();
    }

    public void resetToInitialState() {
        // Re-enable input controls
        sourceSpinner.setEnabled(true);
        destSpinner.setEnabled(true);
        departureSpinner.setEnabled(true);
        intervalSpinner.setEnabled(true);
        budgetSpinner.setEnabled(true);
        heuristicModeCombo.setEnabled(true);
        runButton.setVisible(true);
        visualizeButton.setVisible(true);
        clearButton.setVisible(true);
        actionPanel.setVisible(false);
        queryExecuted = false;
        
        // Clear fields
        clearFields();
    }

    public static class QueryParameters {
        public final int source;
        public final int destination;
        public final int departure;
        public final int interval;
        public final int budget;
        public final String heuristicMode;

        public QueryParameters(int source, int destination, int departure, int interval, int budget, String heuristicMode) {
            this.source = source;
            this.destination = destination;
            this.departure = departure;
            this.interval = interval;
            this.budget = budget;
            this.heuristicMode = heuristicMode;
        }
    }
    
    public static class QueryPreview {
        public final int source;
        public final int destination;
        
        public QueryPreview(int source, int destination) {
            this.source = source;
            this.destination = destination;
        }
    }
    
    public void disableHeuristicMode() {
        heuristicModeCombo.setEnabled(false);
    }
}
