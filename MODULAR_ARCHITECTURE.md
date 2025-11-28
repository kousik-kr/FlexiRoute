# Wide-Path Pro - Modular Architecture Documentation

## Overview
This document describes the comprehensive modular architecture redesign of the Wide-Path GUI application, transforming it from a monolithic single-file design into a professional, maintainable, world-class application.

## Architecture Principles

### 1. Separation of Concerns
- **Models**: Immutable data transfer objects
- **Managers**: Business logic and state management
- **UI Components**: Reusable visual elements
- **UI Panels**: Complex composite views

### 2. Design Patterns Applied
- **Builder Pattern**: QueryResult construction
- **Observer Pattern**: Timer-based updates
- **Strategy Pattern**: Multiple visualization modes
- **Singleton-like**: Manager instances

### 3. Java 21 Features Utilized
- Records and enhanced switch expressions ready
- Modern API usage
- Thread-safe concurrent utilities
- Enhanced graphics rendering

## Package Structure

```
src/
├── GuiLauncher.java              # Main application orchestrator
├── models/
│   └── QueryResult.java          # Immutable query result data
├── managers/
│   ├── ThemeManager.java         # Theme switching (Light/Dark)
│   ├── QueryHistoryManager.java  # History persistence & analytics
│   └── MetricsCollector.java     # Thread-safe metrics collection
└── ui/
    ├── components/
    │   ├── StatusBar.java        # Professional status bar
    │   └── ModernButton.java     # Material Design button
    └── panels/
        ├── QueryInputPanel.java   # Modern input form
        ├── MetricsDashboard.java  # Performance metrics display
        ├── QueryHistoryPanel.java # History table with sorting
        └── AdvancedMapPanel.java  # Multi-mode visualization

```

## Component Details

### Models Package

#### QueryResult.java
**Purpose**: Immutable data transfer object for query results

**Key Features**:
- 18 fields capturing complete query state
- Builder pattern for flexible construction
- Immutable design for thread safety
- Timestamp tracking

**Fields**:
- sourceNode, destinationNode
- departureTime, actualDepartureTime
- intervalDuration, budget
- score, travelTime
- rightTurns, sharpTurns
- pathNodes, wideEdgeIndices
- executionTimeMs, timestamp
- success, errorMessage

**Usage**:
```java
QueryResult result = new QueryResult.Builder()
    .setSourceNode(10)
    .setDestinationNode(50)
    .setSuccess(true)
    .build();
```

---

### Managers Package

#### ThemeManager.java
**Purpose**: Centralized theme management

**Features**:
- Light/Dark theme switching
- Color palette management
- Theme persistence

**Color Scheme**:
- Light Mode: White backgrounds, dark text
- Dark Mode: Dark backgrounds, light text

**API**:
```java
themeManager.setTheme(Theme.DARK);
Color bg = themeManager.getColor("background");
themeManager.toggleTheme();
```

#### QueryHistoryManager.java
**Purpose**: Query history storage and analytics

**Features**:
- Circular buffer (max 100 queries)
- Success rate calculation
- Average execution time tracking
- Stream-based analytics

**API**:
```java
historyManager.addQuery(result);
double successRate = historyManager.getSuccessRate();
double avgTime = historyManager.getAverageExecutionTime();
List<QueryResult> history = historyManager.getHistory();
```

#### MetricsCollector.java
**Purpose**: Thread-safe real-time metrics collection

**Features**:
- Atomic counters for thread safety
- Total queries, execution time tracking
- Success/failure counts
- Nodes explored tracking

**Metrics**:
- totalQueriesExecuted (AtomicLong)
- totalExecutionTime (AtomicLong)
- successfulQueries (AtomicInteger)
- failedQueries (AtomicInteger)
- totalNodesExplored (AtomicLong)

**API**:
```java
metricsCollector.recordQuery(true, 150L, 25);
long total = metricsCollector.getTotalQueries();
double avgTime = metricsCollector.getAverageExecutionTime();
```

---

### UI Components Package

#### StatusBar.java
**Purpose**: Professional status bar with system information

**Features**:
- Message types: INFO, SUCCESS, WARNING, ERROR
- Color-coded messages
- Memory usage display
- Current time display
- Auto-refresh timer (1 second)

**API**:
```java
statusBar.setMessage("Ready", StatusBar.MessageType.INFO);
statusBar.dispose(); // Stop timer
```

#### ModernButton.java
**Purpose**: Material Design styled button

**Features**:
- Hover effects with color transition
- Press state feedback
- Rounded corners (8px)
- Hand cursor
- Anti-aliased rendering

**Usage**:
```java
ModernButton btn = new ModernButton("Run Query", new Color(33, 150, 243));
btn.addActionListener(e -> executeQuery());
```

---

### UI Panels Package

#### QueryInputPanel.java
**Purpose**: Modern input form with validation

**Features**:
- JSpinners for numeric input
- Range validation (source: 0-maxNodeId, etc.)
- ModernButton integration
- Callback-based submission
- Clean layout with labels

**Parameters**:
- Source Node (0 to maxNodeId)
- Destination Node (0 to maxNodeId)
- Departure Time (0 to 1440 minutes)
- Interval Duration (1 to 1440 minutes)
- Budget (1 to 500 minutes)

**API**:
```java
QueryInputPanel inputPanel = new QueryInputPanel(maxNodeId, params -> {
    // Handle query execution
    System.out.println("Query: " + params.source + " -> " + params.destination);
});
```

#### MetricsDashboard.java
**Purpose**: Real-time performance metrics display

**Features**:
- 5 metric panels
- Color-coded backgrounds
- Large font for visibility
- Auto-refresh timer (1 second)
- GridLayout arrangement

**Metrics Displayed**:
1. Total Queries
2. Average Execution Time
3. Success Rate
4. Successful Queries
5. Failed Queries

**Colors**:
- Total: Blue (#2196F3)
- Avg Time: Purple (#9C27B0)
- Success Rate: Green (#4CAF50)
- Successful: Teal (#009688)
- Failed: Red (#F44336)

#### QueryHistoryPanel.java
**Purpose**: Tabular query history with sorting

**Features**:
- JTable with 8 columns
- Automatic row sorting
- DefaultTableModel for data
- Refresh/Clear/Export buttons
- Scrollable viewport

**Columns**:
1. Time (timestamp)
2. Source (node ID)
3. Destination (node ID)
4. Departure (minutes)
5. Budget (minutes)
6. Travel Time (minutes)
7. Status (✓/✗)
8. Execution (ms)

**API**:
```java
QueryHistoryPanel historyPanel = new QueryHistoryPanel(historyManager);
historyPanel.refreshTable(); // Update display
```

#### AdvancedMapPanel.java
**Purpose**: Multi-mode path visualization with pagination

**Features**:
- 5 visualization modes
- Pagination controls (10-500 nodes/page)
- Path-only view mode
- Animated path traversal
- Navigation controls

**Visualization Modes**:

1. **Classic**
   - Traditional node and edge rendering
   - Color-coded nodes (green=start, red=end, blue=intermediate)
   - Wide edges highlighted in orange
   - Simple and clear

2. **Neon Glow**
   - Futuristic glowing effects
   - Cyan color palette
   - Multi-layer glow rendering
   - Eye-catching appearance

3. **Gradient Flow**
   - Smooth color transitions
   - Blue to orange gradient
   - GradientPaint for edges
   - Visually flowing effect

4. **3D Effect**
   - Pseudo-3D depth perception
   - Drop shadows
   - Highlight gradients
   - Professional look

5. **Pulse Animation**
   - Animated path traversal
   - Golden traveling marker
   - Pulsing glow effect
   - Real-time progress indicator

**Pagination**:
- Configurable nodes per page (10-500)
- Current page tracking
- Total pages calculation
- Previous/Next navigation
- Show All option
- Slider control

**API**:
```java
AdvancedMapPanel mapPanel = new AdvancedMapPanel();
mapPanel.setPath(pathNodes, wideEdges);
```

---

### Main Application (GuiLauncher.java)

#### Architecture
**Orchestrator Pattern**: Coordinates all components

**Components**:
- ExecutorService (4-thread pool) for async operations
- QueryHistoryManager for history
- MetricsCollector for metrics
- ThemeManager for themes
- All UI panels integrated

#### UI Structure

```
JFrame "Wide-Path Pro"
├── MenuBar
│   ├── File (Export, Exit)
│   ├── View (Dark Mode)
│   └── Help (About)
├── Main Panel
│   └── JSplitPane (25% / 75%)
│       ├── QueryInputPanel (left)
│       └── JTabbedPane (right)
│           ├── Tab 1: Results (outputPane + progressBar)
│           ├── Tab 2: Visualization (AdvancedMapPanel)
│           ├── Tab 3: Metrics (MetricsDashboard)
│           └── Tab 4: History (QueryHistoryPanel)
└── StatusBar (bottom)
```

#### Key Features

1. **Async Query Execution**
   - SwingWorker for background processing
   - Progress indication
   - Non-blocking UI
   - Exception handling

2. **Input Parameter Display**
   - Pre-query parameter visualization
   - Formatted output
   - Clear presentation

3. **Results Display**
   - Comprehensive result formatting
   - Success/failure differentiation
   - Unicode icons (✓, ✗, 🎯, ⏱, etc.)
   - Scrollable output

4. **Keyboard Shortcuts**
   - Ctrl+Enter: Run query (planned)
   - Clean keyboard navigation

5. **Lifecycle Management**
   - Graceful shutdown
   - Confirmation dialog
   - Resource cleanup
   - Thread pool termination

#### Workflow

```
1. User enters parameters in QueryInputPanel
2. User clicks "Run Query" button
3. GuiLauncher validates and disables input
4. Shows progress bar
5. Executes query in background (SwingWorker)
6. Updates metrics (MetricsCollector)
7. Stores in history (QueryHistoryManager)
8. Displays results in output pane
9. Updates visualization (AdvancedMapPanel)
10. Refreshes history panel
11. Re-enables input for next query
```

---

## Benefits of Modular Architecture

### 1. Maintainability
- **Single Responsibility**: Each class has one clear purpose
- **Easy to Locate**: Functionality grouped logically
- **Reduced Complexity**: Smaller, focused classes

### 2. Testability
- **Unit Testing**: Each component testable independently
- **Mock Objects**: Easy to create test doubles
- **Isolation**: Changes don't ripple across entire codebase

### 3. Reusability
- **ModernButton**: Reusable across application
- **StatusBar**: Standard component
- **QueryResult**: Shared data structure

### 4. Scalability
- **New Features**: Easy to add new visualization modes
- **Extension Points**: Clear interfaces for enhancement
- **Parallel Development**: Multiple developers can work simultaneously

### 5. Code Quality
- **Readability**: Clear class names and structure
- **Documentation**: Each class self-documenting
- **Standards**: Consistent design patterns

---

## Usage Guide

### Running the Application

```bash
# Using Java directly
java -cp target/classes GuiLauncher

# Using Maven
mvn clean compile exec:java -Dexec.mainClass="GuiLauncher"
```

### Adding New Visualization Mode

1. Add enum value to `VisualizationMode`
2. Implement render method in `AdvancedMapPanel`
3. Add to switch statement in `renderVisualization()`

Example:
```java
// In VisualizationMode enum
WIREFRAME("Wireframe", "Minimalist wireframe view");

// In AdvancedMapPanel
private void renderWireframe(Graphics2D g2d) {
    // Implementation
}

// In renderVisualization()
case WIREFRAME -> renderWireframe(g2d);
```

### Customizing Theme Colors

Modify `ThemeManager.java`:
```java
// Add new color
colorScheme.put("highlight", Color.YELLOW);

// Use in component
Color highlight = themeManager.getColor("highlight");
```

---

## Performance Considerations

### Thread Safety
- **AtomicLong/AtomicInteger**: Lock-free metrics
- **CopyOnWriteArrayList**: Thread-safe history (if needed)
- **SwingWorker**: Proper EDT handling

### Memory Management
- **Circular Buffer**: History limited to 100 entries
- **Timer Cleanup**: Disposed in shutdown
- **ExecutorService**: Properly terminated

### Rendering Optimization
- **Anti-aliasing**: Smooth graphics
- **Double buffering**: Flicker-free updates
- **Pagination**: Large graphs handled efficiently

---

## Future Enhancements

### Planned Features
1. **Export Functionality**
   - CSV export for history
   - JSON export for results
   - Image export for visualizations

2. **Advanced Analytics**
   - Query comparison tools
   - Performance trends
   - Statistical analysis

3. **Configuration**
   - User preferences persistence
   - Customizable color schemes
   - Layout customization

4. **Accessibility**
   - Keyboard navigation
   - Screen reader support
   - High contrast mode

---

## Migration from Old GuiLauncher

### Backup Location
- Original file: `src/GuiLauncher.java.backup`
- Previous version: `src/GuiLauncher.java.old`

### Key Changes
1. **Monolithic → Modular**: Single file split into 11 classes
2. **Ad-hoc → Patterns**: Design patterns applied
3. **Basic → Professional**: Material Design UI
4. **Limited → Comprehensive**: Multiple visualization modes

### Compatibility
- **API Unchanged**: Still uses BidirectionalAstar backend
- **Data Format**: No changes to graph structure
- **Performance**: Equal or better

---

## Troubleshooting

### Build Issues
```bash
# Clean build
mvn clean compile

# Check Java version
java -version  # Should be 21
```

### Runtime Issues
- **Graph not loading**: Check Properties class configuration
- **UI not updating**: Ensure SwingUtilities.invokeLater used
- **Memory issues**: Adjust pagination size

---

## Credits

**Architecture Design**: Modular separation of concerns
**Design Inspiration**: Material Design, modern Java practices
**Java Version**: Java 21 LTS
**Build Tool**: Maven
**Graphics**: Java Swing/AWT

---

## Version History

### v2.0 (Current)
- Complete modular architecture redesign
- 5 visualization modes
- Real-time metrics dashboard
- Query history with analytics
- Material Design UI
- Java 21 features

### v1.0 (Previous)
- Monolithic design
- Basic visualization
- Limited features

---

## Contact & Support

For questions or issues:
1. Check this documentation
2. Review source code comments
3. Examine backup files for reference

---

**Last Updated**: 2025
**Documentation Version**: 1.0
**Application Version**: 2.0

