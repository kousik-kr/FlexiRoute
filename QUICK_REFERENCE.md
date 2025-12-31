# FlexRoute Pro - Quick Reference Guide

## 🚀 Quick Start

### Build & Run
```bash
# Compile
mvn clean compile

# Run
java -cp target/classes GuiLauncher

# Or using Maven
mvn exec:java -Dexec.mainClass="GuiLauncher"
```

---

## 📦 Package Structure

```
models/          → Data structures (QueryResult)
managers/        → Business logic (Theme, History, Metrics)
ui/components/   → Reusable UI elements (StatusBar, ModernButton)
ui/panels/       → Complex views (Input, Metrics, History, Map)
```

---

## 🎨 Visualization Modes

| Mode | Description | Use Case |
|------|-------------|----------|
| **Classic** | Traditional rendering | Default, clear view |
| **Neon Glow** | Glowing cyan effects | Eye-catching demos |
| **Gradient Flow** | Blue-to-orange transition | Smooth aesthetics |
| **3D Effect** | Shadow & highlights | Professional look |
| **Pulse Animation** | Traveling marker | Path traversal demo |

---

## 🔧 Common Tasks

### Add New Visualization Mode

1. **Update VisualizationMode enum** in `AdvancedMapPanel.java`:
```java
WIREFRAME("Wireframe", "Minimalist view");
```

2. **Implement render method**:
```java
private void renderWireframe(Graphics2D g2d) {
    // Your rendering logic
}
```

3. **Add to switch**:
```java
case WIREFRAME -> renderWireframe(g2d);
```

### Customize Colors

Edit `ThemeManager.java`:
```java
// Light theme
lightColors.put("primary", new Color(33, 150, 243));

// Dark theme  
darkColors.put("primary", new Color(66, 165, 245));
```

### Add Metrics

Edit `MetricsCollector.java`:
```java
private final AtomicLong newMetric = new AtomicLong(0);

public void incrementNewMetric() {
    newMetric.incrementAndGet();
}

public long getNewMetric() {
    return newMetric.get();
}
```

Then update `MetricsDashboard.java` to display it.

---

## 🎯 Key Components API

### QueryInputPanel
```java
// Create with max node ID and callback
QueryInputPanel input = new QueryInputPanel(1000, params -> {
    System.out.printf("Query: %d → %d%n", 
        params.source, params.destination);
});

// Enable/disable run button
input.setRunEnabled(false);
```

### MetricsDashboard
```java
// Create with metrics collector
MetricsDashboard dashboard = new MetricsDashboard(metricsCollector);

// Cleanup when done
dashboard.dispose();
```

### QueryHistoryPanel
```java
// Create with history manager
QueryHistoryPanel history = new QueryHistoryPanel(historyManager);

// Refresh display
history.refreshTable();
```

### AdvancedMapPanel
```java
// Create panel
AdvancedMapPanel map = new AdvancedMapPanel();

// Set path data
map.setPath(pathNodes, wideEdges);
```

### StatusBar
```java
// Create status bar
StatusBar status = new StatusBar();

// Set messages
status.setMessage("Ready", StatusBar.MessageType.INFO);
status.setMessage("Success!", StatusBar.MessageType.SUCCESS);
status.setMessage("Warning", StatusBar.MessageType.WARNING);
status.setMessage("Error!", StatusBar.MessageType.ERROR);

// Cleanup
status.dispose();
```

### ModernButton
```java
// Create with text and color
ModernButton btn = new ModernButton("Click Me", Color.BLUE);

// Add action
btn.addActionListener(e -> System.out.println("Clicked!"));
```

---

## 📊 Data Models

### QueryResult Builder
```java
QueryResult result = new QueryResult.Builder()
    .setSourceNode(10)
    .setDestinationNode(50)
    .setDepartureTime(120)
    .setIntervalDuration(15)
    .setBudget(200)
    .setScore(95.5)
    .setTravelTime(145.0)
    .setPathNodes(Arrays.asList(10, 15, 30, 50))
    .setSuccess(true)
    .setExecutionTimeMs(150)
    .build();

// Access data
boolean success = result.isSuccess();
List<Integer> path = result.getPathNodes();
long time = result.getExecutionTimeMs();
```

---

## 🔄 Managers

### ThemeManager
```java
ThemeManager theme = new ThemeManager();

// Get colors
Color bg = theme.getColor("background");
Color fg = theme.getColor("foreground");

// Switch themes
theme.setTheme(ThemeManager.Theme.DARK);
theme.toggleTheme();
```

### QueryHistoryManager
```java
QueryHistoryManager history = new QueryHistoryManager();

// Add queries
history.addQuery(queryResult);

// Get stats
double successRate = history.getSuccessRate();
double avgTime = history.getAverageExecutionTime();

// Get history
List<QueryResult> all = history.getHistory();
```

### MetricsCollector
```java
MetricsCollector metrics = new MetricsCollector();

// Record query
metrics.recordQuery(success, executionTime, nodesExplored);

// Get metrics
long total = metrics.getTotalQueries();
long successful = metrics.getSuccessfulQueries();
long failed = metrics.getFailedQueries();
double avgTime = metrics.getAverageExecutionTime();
long avgNodes = metrics.getAverageNodesExplored();
```

---

## 🎨 Color Palette

### Primary Colors
```java
Color PRIMARY = new Color(33, 150, 243);    // Blue
Color ACCENT = new Color(255, 87, 34);      // Orange
Color SUCCESS = new Color(76, 175, 80);     // Green
Color ERROR = new Color(244, 67, 54);       // Red
Color WARNING = new Color(255, 193, 7);     // Amber
```

### Material Design Colors
```java
// Blues
new Color(33, 150, 243)   // Blue 500
new Color(21, 101, 192)   // Blue 700

// Greens
new Color(76, 175, 80)    // Green 500
new Color(67, 160, 71)    // Green 600

// Reds
new Color(244, 67, 54)    // Red 500
new Color(211, 47, 47)    // Red 600

// Purples
new Color(156, 39, 176)   // Purple 500
new Color(142, 36, 170)   // Purple 600
```

---

## ⚡ Performance Tips

### Pagination
```java
// Adjust nodes per page for large graphs
int nodesPerPage = 100; // Default is 50
pageSlider.setValue(nodesPerPage);
```

### Threading
```java
// All UI updates must be on EDT
SwingUtilities.invokeLater(() -> {
    // Update UI components here
});

// Long operations in background
SwingWorker<Result, Void> worker = new SwingWorker<>() {
    @Override
    protected Result doInBackground() {
        // Long operation
        return result;
    }
    
    @Override
    protected void done() {
        // Update UI with result
    }
};
worker.execute();
```

---

## 🐛 Debugging

### Common Issues

**Graph not loading**
```java
// Check Properties configuration
System.out.println(Properties.get_serialized_graph_path());
```

**UI not updating**
```java
// Use EDT for UI updates
SwingUtilities.invokeLater(() -> component.repaint());
```

**Metrics not showing**
```java
// Ensure recordQuery is called
metricsCollector.recordQuery(true, 150L, 25);
```

---

## 📝 Code Style

### Naming Conventions
- Classes: `PascalCase`
- Methods: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Packages: `lowercase`

### Documentation
```java
/**
 * Brief description of class/method.
 * 
 * Detailed explanation if needed.
 * 
 * @param name Parameter description
 * @return Return value description
 */
```

---

## 🧪 Testing

### Unit Test Example
```java
@Test
public void testQueryResult() {
    QueryResult result = new QueryResult.Builder()
        .setSourceNode(1)
        .setDestinationNode(2)
        .setSuccess(true)
        .build();
    
    assertTrue(result.isSuccess());
    assertEquals(1, result.getSourceNode());
}
```

---

## 📚 Resources

- **Full Documentation**: `MODULAR_ARCHITECTURE.md`
- **Backup**: `src/GuiLauncher.java.backup`
- **Old Version**: `src/GuiLauncher.java.old`

---

## 🎯 Cheat Sheet

```bash
# Build
mvn clean compile

# Run
java -cp target/classes GuiLauncher

# Test
mvn test

# Package
mvn package
```

**Key Files**:
- Main: `src/GuiLauncher.java`
- Visualization: `src/ui/panels/AdvancedMapPanel.java`
- Metrics: `src/managers/MetricsCollector.java`

**Key Classes**:
- `QueryResult` - Data model
- `AdvancedMapPanel` - Visualization
- `MetricsCollector` - Performance tracking
- `QueryHistoryManager` - History management

---

**Version**: 2.0  
**Last Updated**: 2025

