# FlexRoute Pro - Quick Start Guide

## 🚀 Getting Started in 5 Minutes

### 1. Launch the Application

#### Option A: Using the GUI Launcher (Recommended)
```bash
# On Linux/Mac
./run_gui.sh

# On Windows
run_with_guide.bat
```

#### Option B: Direct Java Execution
```bash
# Compile first
mvn clean package

# Run
java -cp target/classes GuiLauncher
```

### 2. First Launch

When you first launch FlexRoute Pro, you'll see:

1. **Animated Splash Screen** 🎨
   - Modern gradient background
   - Loading progress indicator
   - Version information

2. **Welcome Screen** 👋
   - Quick tour option
   - Sample query suggestions
   - Documentation links

### 3. Understanding the Interface

The main window is divided into 4 sections:

```
┌─────────────────────────────────────────────────┐
│  Menu Bar (File, View, Help)                   │
├──────────────┬──────────────────────────────────┤
│              │  📊 Results Tab                  │
│  Query Panel │  🗺️ Visualization Tab            │
│              │  📈 Metrics Tab                  │
│   (Left)     │  🕐 History Tab                  │
│              │                                  │
│              │       (Main Content)             │
├──────────────┴──────────────────────────────────┤
│  Status Bar (Ready, Zoom: 100%, Theme: Light)  │
└─────────────────────────────────────────────────┘
```

### 4. Running Your First Query

#### Step-by-Step:

1. **Set Parameters** (Left Panel):
   ```
   🎯 Source Node:        10
   🏁 Destination Node:   50
   🕐 Departure Time:     450 minutes
   ⏱ Interval Duration:  360 minutes
   💰 Budget:            45 minutes
   ```

2. **Run the Query**:
   - Click **▶ Run Query** button
   - OR press `Ctrl+Enter`

3. **View Results**:
   - Results appear in the **📊 Results** tab
   - Path visualization in **🗺️ Visualization** tab
   - Metrics update in **📈 Metrics** tab

### 5. Exploring Visualization

#### Interactive Map Features:

**Zoom & Pan** 🔍
- **Zoom In**: `Ctrl` + `+` or mouse wheel up
- **Zoom Out**: `Ctrl` + `-` or mouse wheel down
- **Pan**: Hold `Ctrl` + drag with left mouse
- **Reset**: `Ctrl` + `0`

**Render Modes** 🎨
1. Classic - Traditional view
2. Neon Glow - Futuristic effects
3. Gradient Flow - Smooth colors
4. 3D Effect - Depth perception
5. Minimal - Clean view

**Minimap** 📍
- Toggle with 📍 button or `Ctrl+M`
- Shows overview of entire graph
- Navigate large graphs easily

**Export** 💾
- Click **💾 Export** button
- Save current view as PNG
- High-resolution output

### 6. Using Smart Features

#### Recent Queries 📋
```
1. Click "📋 Recent" button
2. Select from last 10 queries
3. Parameters auto-load
```

#### Swap Source/Dest ⇄
```
1. Enter source and destination
2. Click "⇄ Swap" to exchange
3. Quick reverse path testing
```

### 7. Customizing Your Experience

#### Change Theme 🎨

**Method 1: Menu**
```
View → Select Theme → Choose from:
- Light (default)
- Dark
- Auto (system)
- Oceanic
- Forest
- Sunset
```

**Method 2: Keyboard**
```
Press Ctrl+T to toggle Light/Dark
```

#### Adjust Map Settings
```
1. Go to Visualization tab
2. Use toolbar controls:
   - Zoom controls
   - Render mode selector
   - Minimap toggle
   - Export button
```

### 8. Monitoring Performance

#### Metrics Dashboard 📊

The dashboard shows:

1. **Total Queries** - Number executed
2. **Avg Response Time** - Performance metric
3. **Success Rate** - Reliability indicator
4. **Throughput** - Queries per minute

#### Real-Time Charts

- **Line Chart**: Execution time trend
- **Pie Chart**: Success vs. failure ratio
- **Bar Chart**: Response time distribution

### 9. Keyboard Shortcuts Cheatsheet

#### Essential Shortcuts
| Action | Shortcut |
|--------|----------|
| Run Query | `Ctrl+Enter` |
| New Query | `Ctrl+N` |
| Clear Fields | `Ctrl+L` |
| Toggle Theme | `Ctrl+T` |
| Zoom In | `Ctrl++` |
| Zoom Out | `Ctrl+-` |
| Reset View | `Ctrl+0` |
| Search | `Ctrl+F` |
| Export | `Ctrl+S` |
| Help | `F1` |
| Quit | `Ctrl+Q` |

### 10. Tips & Tricks

#### 💡 Pro Tips:

1. **Validate Before Running**
   - Watch for validation messages
   - Green ✓ = Good to go
   - Yellow ⚠ = Warning but allowed

2. **Compare Results**
   - Use History tab
   - Run similar queries
   - Analyze patterns

4. **Export for Reports**
   - Capture visualizations
   - Export metrics data
   - Share with team

5. **Customize Your Workspace**
   - Choose your favorite theme
   - Adjust window layout
   - Set preferred zoom level

#### ⚡ Efficiency Hacks:

1. **Keyboard-Only Workflow**:
   ```
   Enter values → Ctrl+Enter → Tab through results
   ```

2. **Quick Comparison**:
   ```
   Run query → ⇄ Swap → Run again → Compare
   ```

3. **Theme Matching**:
   ```
   Working at night? → Ctrl+T (Dark mode)
   ```

### 11. Troubleshooting

#### Common Issues:

**Graph Won't Load**
```
Check: Graph files in correct directory
Fix: Update path in Properties.java
```

**Slow Performance**
```
Check: Graph size, zoom level
Fix: Use pagination, reset view
```

**Can't See Path**
```
Check: Query parameters valid
Fix: Increase budget, check node IDs
```

**Theme Not Applying**
```
Fix: Restart application
OR: View → Reset Theme
```

### 12. Getting Help

#### Resources:

- **Documentation**: Press `F1` or Help menu
- **Features Guide**: `WORLD_CLASS_GUI_FEATURES.md`
- **Design System**: `DESIGN_SYSTEM.md`
- **README**: `README.md`
- **Issues**: Report on GitHub

#### In-App Help:

- Hover over buttons for tooltips
- Check status bar for messages
- Watch validation feedback

---

## 🎯 Next Steps

Now that you're familiar with the basics:

1. ✅ Explore different visualization modes
2. ✅ Try all the themes
3. ✅ Experiment with keyboard shortcuts
4. ✅ Analyze metrics and patterns
5. ✅ Export and share results

**Enjoy your world-class pathfinding experience!** 🚀

---

*FlexRoute Pro - Making pathfinding visualization extraordinary.*
