package ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Modern search bar with real-time filtering and clear button
 */
public class SearchBar extends JPanel {
    private final JTextField searchField;
    private final JButton clearButton;
    private Consumer<String> onSearchCallback;
    
    public SearchBar(String placeholder) {
        setLayout(new BorderLayout(5, 0));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        // Search icon
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        // Search field
        searchField = new JTextField();
        searchField.setBorder(null);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBackground(Color.WHITE);
        
        // Placeholder text
        searchField.setText(placeholder);
        searchField.setForeground(Color.GRAY);
        
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(placeholder)) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText(placeholder);
                    searchField.setForeground(Color.GRAY);
                }
            }
        });
        
        // Real-time search
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { notifySearch(); }
            @Override
            public void removeUpdate(DocumentEvent e) { notifySearch(); }
            @Override
            public void changedUpdate(DocumentEvent e) { notifySearch(); }
        });
        
        // Clear button
        clearButton = new JButton("✕");
        clearButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        clearButton.setBorderPainted(false);
        clearButton.setContentAreaFilled(false);
        clearButton.setFocusPainted(false);
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearButton.setVisible(false);
        clearButton.addActionListener(e -> clearSearch());
        
        add(searchIcon, BorderLayout.WEST);
        add(searchField, BorderLayout.CENTER);
        add(clearButton, BorderLayout.EAST);
        
        // Update clear button visibility
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                clearButton.setVisible(!searchField.getText().isEmpty() && 
                                       !searchField.getText().equals(placeholder));
            }
        });
    }
    
    public void setOnSearch(Consumer<String> callback) {
        this.onSearchCallback = callback;
    }
    
    private void notifySearch() {
        if (onSearchCallback != null) {
            String text = searchField.getText();
            if (!text.isEmpty() && !text.equals(searchField.getToolTipText())) {
                onSearchCallback.accept(text);
            }
        }
    }
    
    public void clearSearch() {
        searchField.setText("");
        searchField.requestFocus();
        if (onSearchCallback != null) {
            onSearchCallback.accept("");
        }
    }
    
    public String getSearchText() {
        String text = searchField.getText();
        return text.equals(searchField.getToolTipText()) ? "" : text;
    }
}
