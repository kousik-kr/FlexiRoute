package managers;

import javax.swing.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Advanced Theme Manager with multiple themes, smooth transitions, and live updates
 * Supports Light, Dark, Auto (system-based), and custom color schemes
 */
public class ThemeManager {
    public enum Theme {
        LIGHT("Light", "Clean and bright interface"),
        DARK("Dark", "Easy on the eyes"),
        AUTO("Auto", "Follows system preferences"),
        OCEANIC("Oceanic", "Deep blue professional theme"),
        FOREST("Forest", "Nature-inspired green theme"),
        SUNSET("Sunset", "Warm orange and purple theme");
        
        private final String displayName;
        private final String description;
        
        Theme(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    private Theme currentTheme = Theme.LIGHT;
    private final Map<Theme, Map<String, Color>> themeColors = new HashMap<>();
    private final List<Consumer<Theme>> themeChangeListeners = new ArrayList<>();
    private javax.swing.Timer transitionTimer;
    private boolean animateTransitions = true;

    public ThemeManager() {
        initializeThemes();
        detectSystemTheme();
    }

    private void initializeThemes() {
        // Light theme - Clean and modern
        Map<String, Color> light = new HashMap<>();
        light.put("primary", new Color(33, 150, 243));
        light.put("primaryDark", new Color(25, 118, 210));
        light.put("primaryLight", new Color(100, 181, 246));
        light.put("accent", new Color(255, 87, 34));
        light.put("success", new Color(76, 175, 80));
        light.put("error", new Color(244, 67, 54));
        light.put("warning", new Color(255, 193, 7));
        light.put("info", new Color(3, 169, 244));
        light.put("background", new Color(250, 250, 250));
        light.put("backgroundElevated", Color.WHITE);
        light.put("foreground", new Color(33, 33, 33));
        light.put("foregroundSecondary", new Color(117, 117, 117));
        light.put("panel", new Color(245, 245, 245));
        light.put("panelElevated", Color.WHITE);
        light.put("border", new Color(224, 224, 224));
        light.put("borderLight", new Color(238, 238, 238));
        light.put("hover", new Color(245, 245, 245));
        light.put("selected", new Color(227, 242, 253));
        light.put("shadow", new Color(0, 0, 0, 20));
        themeColors.put(Theme.LIGHT, light);

        // Dark theme - Professional dark mode
        Map<String, Color> dark = new HashMap<>();
        dark.put("primary", new Color(66, 165, 245));
        dark.put("primaryDark", new Color(41, 121, 255));
        dark.put("primaryLight", new Color(100, 181, 246));
        dark.put("accent", new Color(255, 112, 67));
        dark.put("success", new Color(102, 187, 106));
        dark.put("error", new Color(239, 83, 80));
        dark.put("warning", new Color(255, 202, 40));
        dark.put("info", new Color(41, 182, 246));
        dark.put("background", new Color(18, 18, 18));
        dark.put("backgroundElevated", new Color(30, 30, 30));
        dark.put("foreground", new Color(238, 238, 238));
        dark.put("foregroundSecondary", new Color(158, 158, 158));
        dark.put("panel", new Color(33, 33, 33));
        dark.put("panelElevated", new Color(48, 48, 48));
        dark.put("border", new Color(66, 66, 66));
        dark.put("borderLight", new Color(55, 55, 55));
        dark.put("hover", new Color(48, 48, 48));
        dark.put("selected", new Color(25, 39, 52));
        dark.put("shadow", new Color(0, 0, 0, 50));
        themeColors.put(Theme.DARK, dark);
        
        // Oceanic theme - Deep blue professional
        Map<String, Color> oceanic = new HashMap<>();
        oceanic.put("primary", new Color(0, 150, 199));
        oceanic.put("primaryDark", new Color(0, 120, 160));
        oceanic.put("primaryLight", new Color(41, 182, 246));
        oceanic.put("accent", new Color(255, 138, 101));
        oceanic.put("success", new Color(102, 187, 106));
        oceanic.put("error", new Color(239, 83, 80));
        oceanic.put("warning", new Color(255, 193, 7));
        oceanic.put("info", new Color(79, 195, 247));
        oceanic.put("background", new Color(15, 32, 39));
        oceanic.put("backgroundElevated", new Color(23, 48, 60));
        oceanic.put("foreground", new Color(236, 239, 241));
        oceanic.put("foregroundSecondary", new Color(176, 190, 197));
        oceanic.put("panel", new Color(23, 48, 60));
        oceanic.put("panelElevated", new Color(31, 64, 80));
        oceanic.put("border", new Color(55, 71, 79));
        oceanic.put("borderLight", new Color(38, 50, 56));
        oceanic.put("hover", new Color(31, 64, 80));
        oceanic.put("selected", new Color(0, 96, 100));
        oceanic.put("shadow", new Color(0, 0, 0, 60));
        themeColors.put(Theme.OCEANIC, oceanic);
        
        // Forest theme - Nature-inspired
        Map<String, Color> forest = new HashMap<>();
        forest.put("primary", new Color(56, 142, 60));
        forest.put("primaryDark", new Color(46, 125, 50));
        forest.put("primaryLight", new Color(102, 187, 106));
        forest.put("accent", new Color(255, 152, 0));
        forest.put("success", new Color(139, 195, 74));
        forest.put("error", new Color(239, 83, 80));
        forest.put("warning", new Color(255, 193, 7));
        forest.put("info", new Color(3, 169, 244));
        forest.put("background", new Color(27, 38, 29));
        forest.put("backgroundElevated", new Color(38, 50, 40));
        forest.put("foreground", new Color(232, 245, 233));
        forest.put("foregroundSecondary", new Color(174, 213, 129));
        forest.put("panel", new Color(38, 50, 40));
        forest.put("panelElevated", new Color(51, 68, 53));
        forest.put("border", new Color(76, 175, 80));
        forest.put("borderLight", new Color(56, 142, 60));
        forest.put("hover", new Color(51, 68, 53));
        forest.put("selected", new Color(27, 94, 32));
        forest.put("shadow", new Color(0, 0, 0, 60));
        themeColors.put(Theme.FOREST, forest);
        
        // Sunset theme - Warm and inviting
        Map<String, Color> sunset = new HashMap<>();
        sunset.put("primary", new Color(244, 81, 30));
        sunset.put("primaryDark", new Color(230, 74, 25));
        sunset.put("primaryLight", new Color(255, 138, 101));
        sunset.put("accent", new Color(156, 39, 176));
        sunset.put("success", new Color(102, 187, 106));
        sunset.put("error", new Color(198, 40, 40));
        sunset.put("warning", new Color(251, 192, 45));
        sunset.put("info", new Color(3, 169, 244));
        sunset.put("background", new Color(33, 22, 25));
        sunset.put("backgroundElevated", new Color(48, 32, 35));
        sunset.put("foreground", new Color(255, 245, 238));
        sunset.put("foregroundSecondary", new Color(255, 204, 188));
        sunset.put("panel", new Color(48, 32, 35));
        sunset.put("panelElevated", new Color(62, 42, 46));
        sunset.put("border", new Color(255, 87, 34));
        sunset.put("borderLight", new Color(255, 112, 67));
        sunset.put("hover", new Color(62, 42, 46));
        sunset.put("selected", new Color(191, 54, 12));
        sunset.put("shadow", new Color(0, 0, 0, 60));
        themeColors.put(Theme.SUNSET, sunset);
        
        // Auto theme uses light for now (can be enhanced with system detection)
        themeColors.put(Theme.AUTO, light);
    }
    
    private void detectSystemTheme() {
        // Detect system dark mode preference
        // This is a simplified version - full implementation would use JNA or system calls
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) {
                // macOS dark mode detection would go here
            } else if (os.contains("win")) {
                // Windows dark mode detection would go here
            }
        } catch (Exception e) {
            // Fallback to light theme
        }
    }

    public Color getColor(String key) {
        Theme effectiveTheme = currentTheme == Theme.AUTO ? detectEffectiveTheme() : currentTheme;
        return themeColors.getOrDefault(effectiveTheme, themeColors.get(Theme.LIGHT))
                         .getOrDefault(key, Color.GRAY);
    }
    
    private Theme detectEffectiveTheme() {
        // Auto-detect based on system preferences
        // For now, return Light (can be enhanced with system calls)
        return Theme.LIGHT;
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    public void setTheme(Theme theme) {
        if (this.currentTheme != theme) {
            Theme oldTheme = this.currentTheme;
            this.currentTheme = theme;
            notifyThemeChange(theme);
            
            if (animateTransitions) {
                animateThemeTransition(oldTheme, theme);
            }
        }
    }

    public void toggleTheme() {
        currentTheme = currentTheme == Theme.LIGHT ? Theme.DARK : Theme.LIGHT;
        notifyThemeChange(currentTheme);
    }
    
    public void addThemeChangeListener(Consumer<Theme> listener) {
        themeChangeListeners.add(listener);
    }
    
    public void removeThemeChangeListener(Consumer<Theme> listener) {
        themeChangeListeners.remove(listener);
    }
    
    private void notifyThemeChange(Theme newTheme) {
        for (Consumer<Theme> listener : themeChangeListeners) {
            listener.accept(newTheme);
        }
    }
    
    private void animateThemeTransition(Theme oldTheme, Theme newTheme) {
        // Smooth fade animation between themes
        if (transitionTimer != null && transitionTimer.isRunning()) {
            transitionTimer.stop();
        }
        
        transitionTimer = new javax.swing.Timer(16, null);
        final int[] frame = {0};
        final int totalFrames = 20;
        
        transitionTimer.addActionListener(e -> {
            frame[0]++;
            if (frame[0] >= totalFrames) {
                transitionTimer.stop();
            }
            // Trigger repaint of all listeners
            notifyThemeChange(currentTheme);
        });
        
        transitionTimer.start();
    }
    
    public void applyThemeToComponent(Component component) {
        if (component == null) return;
        
        component.setBackground(getColor("background"));
        component.setForeground(getColor("foreground"));
        
        if (component instanceof JPanel) {
            component.setBackground(getColor("panel"));
        } else if (component instanceof JButton) {
            component.setBackground(getColor("primary"));
            component.setForeground(Color.WHITE);
        }
        
        // Recursively apply to children
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                applyThemeToComponent(child);
            }
        }
    }
    
    public boolean isDarkTheme() {
        Theme effective = currentTheme == Theme.AUTO ? detectEffectiveTheme() : currentTheme;
        return effective == Theme.DARK || effective == Theme.OCEANIC || 
               effective == Theme.FOREST || effective == Theme.SUNSET;
    }
    
    public void setAnimateTransitions(boolean animate) {
        this.animateTransitions = animate;
    }
    
    public Map<String, Color> getCurrentThemeColors() {
        Theme effectiveTheme = currentTheme == Theme.AUTO ? detectEffectiveTheme() : currentTheme;
        return new HashMap<>(themeColors.getOrDefault(effectiveTheme, themeColors.get(Theme.LIGHT)));
    }
}
