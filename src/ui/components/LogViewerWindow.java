package ui.components;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import managers.LogManager;

/**
 * 📜 FlexiRoute Log Viewer Window
 * 
 * A beautiful, modern dialog for viewing query log files with:
 * - Syntax highlighting for different sections
 * - Collapsible sections
 * - Search functionality
 * - Export and copy options
 * - Light theme with colorful text
 */
public class LogViewerWindow extends JDialog {
    
    // Light color scheme with colorful accents
    private static final Color BG_DARK = new Color(250, 250, 252);       // Light gray background
    private static final Color BG_PANEL = new Color(240, 242, 245);      // Slightly darker panel
    private static final Color BG_CODE = new Color(255, 255, 255);       // White code background
    private static final Color TEXT_PRIMARY = new Color(40, 44, 52);     // Dark text
    private static final Color TEXT_SECONDARY = new Color(100, 105, 115); // Gray secondary text
    private static final Color ACCENT_BLUE = new Color(0, 102, 204);     // Vibrant blue
    private static final Color ACCENT_GREEN = new Color(22, 163, 74);    // Forest green
    private static final Color ACCENT_PURPLE = new Color(139, 92, 246);  // Rich purple
    private static final Color ACCENT_PINK = new Color(219, 39, 119);    // Vivid pink
    private static final Color ACCENT_ORANGE = new Color(234, 88, 12);   // Bright orange
    private static final Color ACCENT_YELLOW = new Color(180, 130, 0);   // Golden yellow (readable on white)
    private static final Color BORDER_COLOR = new Color(209, 213, 219);  // Light gray border
    
    private JTextPane contentPane;
    private JTextField searchField;
    private JLabel filePathLabel;
    private JLabel statusLabel;
    private String currentLogPath;
    private String originalContent;
    
    public LogViewerWindow(Frame parent, String logFilePath) {
        super(parent, "📜 FlexiRoute Query Log Viewer", true);
        this.currentLogPath = logFilePath;
        
        initializeUI();
        loadLogFile(logFilePath);
        
        setSize(900, 700);
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(parent);
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_DARK);
        
        // Header Panel
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Content Panel with log viewer
        add(createContentPanel(), BorderLayout.CENTER);
        
        // Footer Panel with controls
        add(createFooterPanel(), BorderLayout.SOUTH);
        
        // Keyboard shortcuts
        setupKeyboardShortcuts();
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Light gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(248, 250, 252),
                    getWidth(), 0, new Color(241, 245, 249)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Bottom border accent
                g2d.setColor(ACCENT_PURPLE);
                g2d.fillRect(0, getHeight() - 2, getWidth(), 2);
                
                g2d.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        // Title and file path
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("📜 Query Log Viewer");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        filePathLabel = new JLabel("No file loaded");
        filePathLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        filePathLabel.setForeground(TEXT_SECONDARY);
        
        titlePanel.add(titleLabel);
        titlePanel.add(filePathLabel);
        
        header.add(titlePanel, BorderLayout.WEST);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setOpaque(false);
        
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBackground(BG_CODE);
        searchField.setForeground(TEXT_PRIMARY);
        searchField.setCaretColor(TEXT_PRIMARY);
        searchField.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "🔍 Search...");
        searchField.addActionListener(e -> performSearch());
        
        JButton searchBtn = createModernButton("Find", ACCENT_BLUE);
        searchBtn.addActionListener(e -> performSearch());
        
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        
        header.add(searchPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createContentPanel() {
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(BG_DARK);
        contentWrapper.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        // Create styled text pane
        contentPane = new JTextPane() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                super.paintComponent(g);
                g2d.dispose();
            }
        };
        contentPane.setEditable(false);
        contentPane.setBackground(BG_CODE);
        contentPane.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JScrollPane scrollPane = new JScrollPane(contentPane);
        scrollPane.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(0, 0, 0, 0)
        ));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(BG_CODE);
        scrollPane.getViewport().setBackground(BG_CODE);
        
        // Modern scrollbar styling
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
        
        contentWrapper.add(scrollPane, BorderLayout.CENTER);
        
        return contentWrapper;
    }
    
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(BG_PANEL);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(BORDER_COLOR);
                g2d.fillRect(0, 0, getWidth(), 1);
                g2d.dispose();
            }
        };
        footer.setPreferredSize(new Dimension(0, 60));
        footer.setBorder(new EmptyBorder(12, 20, 12, 20));
        
        // Status label
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_SECONDARY);
        
        footer.add(statusLabel, BorderLayout.WEST);
        
        // Action buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);
        
        JButton copyBtn = createModernButton("📋 Copy All", ACCENT_GREEN);
        copyBtn.addActionListener(e -> copyToClipboard());
        
        JButton exportBtn = createModernButton("💾 Export", ACCENT_ORANGE);
        exportBtn.addActionListener(e -> exportLog());
        
        JButton openFolderBtn = createModernButton("📁 Open Folder", ACCENT_PURPLE);
        openFolderBtn.addActionListener(e -> openLogFolder());
        
        JButton closeBtn = createModernButton("✖️ Close", ACCENT_PINK);
        closeBtn.addActionListener(e -> dispose());
        
        buttonsPanel.add(copyBtn);
        buttonsPanel.add(exportBtn);
        buttonsPanel.add(openFolderBtn);
        buttonsPanel.add(closeBtn);
        
        footer.add(buttonsPanel, BorderLayout.EAST);
        
        return footer;
    }
    
    private JButton createModernButton(String text, Color accentColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color bgColor = getModel().isRollover() ? accentColor : 
                               getModel().isPressed() ? accentColor.darker() :
                               new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 40);
                
                g2d.setColor(bgColor);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                
                g2d.setColor(accentColor);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 8, 8));
                
                g2d.dispose();
                
                g.setColor(getModel().isRollover() ? Color.WHITE : accentColor);
                g.setFont(getFont());
                FontMetrics fm = g.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g.drawString(getText(), x, y);
            }
        };
        
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 36));
        
        return btn;
    }
    
    private void setupKeyboardShortcuts() {
        // Escape to close
        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        // Ctrl+F to focus search
        getRootPane().registerKeyboardAction(
            e -> searchField.requestFocus(),
            KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        // Ctrl+C to copy
        getRootPane().registerKeyboardAction(
            e -> copyToClipboard(),
            KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }
    
    private void loadLogFile(String path) {
        if (path == null || path.isEmpty()) {
            showError("No log file specified");
            return;
        }
        
        try {
            originalContent = LogManager.getInstance().readLogFile(path);
            filePathLabel.setText("📂 " + path);
            applyStyledContent(originalContent);
            statusLabel.setText("✅ Loaded successfully • " + originalContent.length() + " characters");
        } catch (Exception e) {
            showError("Failed to load log file: " + e.getMessage());
        }
    }
    
    private void applyStyledContent(String content) {
        StyledDocument doc = contentPane.getStyledDocument();
        
        // Define styles
        Style defaultStyle = contentPane.addStyle("default", null);
        StyleConstants.setForeground(defaultStyle, TEXT_PRIMARY);
        StyleConstants.setFontFamily(defaultStyle, "JetBrains Mono");
        StyleConstants.setFontSize(defaultStyle, 13);
        
        Style headerStyle = contentPane.addStyle("header", defaultStyle);
        StyleConstants.setForeground(headerStyle, ACCENT_PURPLE);
        StyleConstants.setBold(headerStyle, true);
        StyleConstants.setFontSize(headerStyle, 14);
        
        Style sectionStyle = contentPane.addStyle("section", defaultStyle);
        StyleConstants.setForeground(sectionStyle, ACCENT_BLUE);
        StyleConstants.setBold(sectionStyle, true);
        
        Style successStyle = contentPane.addStyle("success", defaultStyle);
        StyleConstants.setForeground(successStyle, ACCENT_GREEN);
        StyleConstants.setBold(successStyle, true);
        
        Style errorStyle = contentPane.addStyle("error", defaultStyle);
        StyleConstants.setForeground(errorStyle, ACCENT_PINK);
        StyleConstants.setBold(errorStyle, true);
        
        Style labelStyle = contentPane.addStyle("label", defaultStyle);
        StyleConstants.setForeground(labelStyle, ACCENT_ORANGE);
        
        Style valueStyle = contentPane.addStyle("value", defaultStyle);
        StyleConstants.setForeground(valueStyle, TEXT_PRIMARY);
        
        Style borderStyle = contentPane.addStyle("border", defaultStyle);
        StyleConstants.setForeground(borderStyle, new Color(148, 163, 184));  // Lighter gray for borders
        
        Style emojiStyle = contentPane.addStyle("emoji", defaultStyle);
        StyleConstants.setForeground(emojiStyle, ACCENT_ORANGE);
        
        // Clear existing content
        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException e) {
            // ignore
        }
        
        // Process and style content line by line
        String[] lines = content.split("\n");
        for (String line : lines) {
            try {
                Style lineStyle = defaultStyle;
                
                // Determine appropriate style based on content
                if (line.contains("═══") || line.contains("╔") || line.contains("╚") || 
                    line.contains("╠") || line.contains("║") || line.contains("┌") || 
                    line.contains("├") || line.contains("└") || line.contains("│")) {
                    lineStyle = borderStyle;
                    // Check for embedded content in bordered lines
                    if (line.contains("SUCCESS")) {
                        insertStyledLine(doc, line, "SUCCESS", successStyle, borderStyle);
                        continue;
                    } else if (line.contains("FAILED")) {
                        insertStyledLine(doc, line, "FAILED", errorStyle, borderStyle);
                        continue;
                    }
                } else if (line.contains("F L E X I R O U T E") || line.contains("FLEXIROUTE")) {
                    lineStyle = headerStyle;
                } else if (line.contains("QUERY PARAMETERS") || line.contains("ROUTING MODE") || 
                           line.contains("PATH STATISTICS") || line.contains("EXECUTION METRICS") ||
                           line.contains("PARETO OPTIMAL") || line.contains("PATH NODES")) {
                    lineStyle = sectionStyle;
                } else if (line.contains(":") && !line.contains("│")) {
                    // Label: Value format
                    int colonIdx = line.indexOf(":");
                    if (colonIdx > 0 && colonIdx < line.length() - 1) {
                        doc.insertString(doc.getLength(), line.substring(0, colonIdx + 1), labelStyle);
                        doc.insertString(doc.getLength(), line.substring(colonIdx + 1) + "\n", valueStyle);
                        continue;
                    }
                }
                
                doc.insertString(doc.getLength(), line + "\n", lineStyle);
                
            } catch (BadLocationException e) {
                // ignore
            }
        }
        
        contentPane.setCaretPosition(0);
    }
    
    private void insertStyledLine(StyledDocument doc, String line, String keyword, 
                                   Style keywordStyle, Style defaultStyle) throws BadLocationException {
        int idx = line.indexOf(keyword);
        if (idx >= 0) {
            doc.insertString(doc.getLength(), line.substring(0, idx), defaultStyle);
            doc.insertString(doc.getLength(), keyword, keywordStyle);
            doc.insertString(doc.getLength(), line.substring(idx + keyword.length()) + "\n", defaultStyle);
        } else {
            doc.insertString(doc.getLength(), line + "\n", defaultStyle);
        }
    }
    
    private void performSearch() {
        String searchText = searchField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            applyStyledContent(originalContent);
            statusLabel.setText("Search cleared");
            return;
        }
        
        // Highlight matches
        String content = contentPane.getText();
        int count = 0;
        int lastIndex = 0;
        
        StyledDocument doc = contentPane.getStyledDocument();
        Style highlightStyle = contentPane.addStyle("highlight", null);
        StyleConstants.setBackground(highlightStyle, new Color(255, 255, 0, 100));
        StyleConstants.setForeground(highlightStyle, Color.BLACK);
        StyleConstants.setBold(highlightStyle, true);
        
        while ((lastIndex = content.toLowerCase().indexOf(searchText, lastIndex)) != -1) {
            try {
                doc.setCharacterAttributes(lastIndex, searchText.length(), highlightStyle, false);
                count++;
                lastIndex += searchText.length();
            } catch (Exception e) {
                break;
            }
        }
        
        statusLabel.setText("🔍 Found " + count + " matches for \"" + searchText + "\"");
        
        // Scroll to first match
        if (count > 0) {
            int firstMatch = content.toLowerCase().indexOf(searchText);
            if (firstMatch >= 0) {
                contentPane.setCaretPosition(firstMatch);
            }
        }
    }
    
    private void copyToClipboard() {
        String text = originalContent != null ? originalContent : contentPane.getText();
        java.awt.datatransfer.StringSelection selection = 
            new java.awt.datatransfer.StringSelection(text);
        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        statusLabel.setText("✅ Copied to clipboard!");
    }
    
    private void exportLog() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("exported_log.txt"));
        
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.nio.file.Files.writeString(
                    chooser.getSelectedFile().toPath(), 
                    originalContent != null ? originalContent : contentPane.getText()
                );
                statusLabel.setText("✅ Exported successfully to " + chooser.getSelectedFile().getName());
            } catch (Exception e) {
                showError("Export failed: " + e.getMessage());
            }
        }
    }
    
    private void openLogFolder() {
        try {
            String logDir = LogManager.getInstance().getLogDirectory();
            Desktop.getDesktop().open(new java.io.File(logDir));
            statusLabel.setText("📁 Opened log folder");
        } catch (Exception e) {
            showError("Could not open folder: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        statusLabel.setText("❌ " + message);
        statusLabel.setForeground(ACCENT_PINK);
    }
    
    /**
     * Static factory method to show the log viewer
     */
    public static void showLogViewer(Frame parent, String logFilePath) {
        SwingUtilities.invokeLater(() -> {
            LogViewerWindow viewer = new LogViewerWindow(parent, logFilePath);
            viewer.setVisible(true);
        });
    }
}
