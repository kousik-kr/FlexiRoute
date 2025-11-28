package managers;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages application themes (Light/Dark mode)
 */
public class ThemeManager {
    public enum Theme {
        LIGHT, DARK
    }

    private Theme currentTheme = Theme.LIGHT;
    private final Map<String, Color> lightColors = new HashMap<>();
    private final Map<String, Color> darkColors = new HashMap<>();

    public ThemeManager() {
        initializeColors();
    }

    private void initializeColors() {
        // Light theme colors
        lightColors.put("primary", new Color(33, 150, 243));
        lightColors.put("accent", new Color(255, 87, 34));
        lightColors.put("success", new Color(76, 175, 80));
        lightColors.put("error", new Color(244, 67, 54));
        lightColors.put("warning", new Color(255, 193, 7));
        lightColors.put("background", Color.WHITE);
        lightColors.put("foreground", Color.BLACK);
        lightColors.put("panel", new Color(245, 245, 245));
        lightColors.put("border", new Color(224, 224, 224));

        // Dark theme colors
        darkColors.put("primary", new Color(66, 165, 245));
        darkColors.put("accent", new Color(255, 112, 67));
        darkColors.put("success", new Color(102, 187, 106));
        darkColors.put("error", new Color(239, 83, 80));
        darkColors.put("warning", new Color(255, 202, 40));
        darkColors.put("background", new Color(33, 33, 33));
        darkColors.put("foreground", new Color(238, 238, 238));
        darkColors.put("panel", new Color(48, 48, 48));
        darkColors.put("border", new Color(66, 66, 66));
    }

    public Color getColor(String key) {
        return currentTheme == Theme.LIGHT ? 
            lightColors.getOrDefault(key, Color.GRAY) : 
            darkColors.getOrDefault(key, Color.GRAY);
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    public void setTheme(Theme theme) {
        this.currentTheme = theme;
    }

    public void toggleTheme() {
        currentTheme = currentTheme == Theme.LIGHT ? Theme.DARK : Theme.LIGHT;
    }
}
