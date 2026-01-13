# FlexiRoute Navigator

![Java](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/Swing-GUI-blue?style=flat)
![License](https://img.shields.io/badge/License-MIT-green.svg)

FlexiRoute is a **professional pathfinding analysis platform** featuring:
- **Java 21 LTS** with bidirectional A* algorithm
- **Enterprise-Grade Swing GUI** with premium design system
- **Real-time analytics** and visualization

## ✨ Features

### 🎨 **Premium Design System**
- **6 Professional Themes**: Light, Dark, Auto, Oceanic, Forest, Sunset
- **Glassmorphism UI**: Modern translucent panels with blur effects
- **Animated Components**: Smooth 60 FPS Material Design transitions
- **Accessibility**: WCAG 2.1 AA compliant with full keyboard navigation

### 🗺️ **Advanced Visualization**
- **Dual Map Modes**: 
  - **Coordinate-Based**: Traditional node-edge rendering with 5 render modes
  - **OSM Tiles**: Real OpenStreetMap tiles with dynamic centering on loaded dataset
- **Interactive Controls**: Zoom, pan, minimap, fit-to-path, reset view
- **Multiple Tile Servers**: OpenStreetMap, OpenTopoMap, CartoDB Dark/Light
- **5 Render Modes**: Classic, Neon Glow, Gradient Flow, 3D, Minimal
- **Export Capabilities**: High-res PNG screenshots with Ctrl+S
- **Real-Time Tooltips**: Hover for node information

### 🎯 **Smart Query System**
- **Dataset Selection**: Choose between default (London) or custom datasets on startup
- **Input Validation**: Real-time feedback with visual indicators
- **Recent History**: Last 10 queries with one-click reload
- **Quick Actions**: Swap source/dest (S↔D button)
- **Node Search**: Quick lookup by ID or name
- **Dynamic Map**: Automatically centers map on loaded dataset coordinates

### 📊 **Real-Time Analytics**
- **4 Metric Cards**: Total queries, avg time, success rate, throughput
- **3 Chart Types**: Line (trend), Pie (ratio), Bar (distribution)
- **Live Updates**: 1-second refresh with smooth animations

### ⚡ **Search Strategies**
- **Aggressive Mode**: Faster search with frontier threshold of 10 (more pruning)
- **Balanced Mode**: Thorough search with frontier threshold of 50 (balanced exploration)

### ⌨️ **Power User Features**
- **20+ Keyboard Shortcuts**: Ctrl+Enter (run), Ctrl+T (theme), Ctrl+S (export)
- **Splash Screen**: Professional animated startup
- **Toast Notifications**: Success, error, warning, info messages
- **Tabbed Interface**: Results, Visualization, Metrics, History

📚 **[🚀 Quick Start Guide →](QUICK_START_GUIDE.md)**  
📚 **[🎨 Design System →](DESIGN_SYSTEM.md)**

## Prerequisites

- **Java 21+ JDK** (LTS version recommended)
- **Maven** (for building the Java project)
- **Python 3 + gdown** (optional, for automatic dataset download)

**Windows**: Install Java 21 JDK from [Adoptium](https://adoptium.net/) and Maven from [Apache Maven](https://maven.apache.org/).

**Ubuntu/Debian**:
```bash
sudo apt update
sudo apt install openjdk-21-jdk maven python3-pip

# Optional: For automatic dataset download
pip install gdown
```

## 📦 Dataset Setup

FlexiRoute now supports multiple datasets with automatic organization:

### 🌍 Available Datasets

**1. London Dataset (Default)** - 288,016 nodes, 744,610 edges
- Location: `dataset/London/`
- Files: `nodes_288016.txt`, `edges_288016.txt`
- Coverage: Greater London road network with time-dependent costs
- Created from London_Edgelist.csv with British National Grid coordinates

**2. California Dataset** - 21,048 nodes
- Location: `dataset/California/`
- Files: `nodes_21048.txt`, `edges_21048.txt`
- Coverage: California road network subset

### 📥 Getting Started

**On first launch**, the application will prompt you to:
- Load the **Default Dataset** (London) from `dataset/London/`
- Or choose a **Custom Dataset** directory

The map will automatically center on the loaded dataset's geographic coordinates.

### 📝 Converting Your Own Dataset

To convert a London format dataset:
```bash
python3 scripts/convert_london.py path/to/London_Edgelist.csv
```

See `scripts/README_LONDON.md` for detailed conversion instructions.

📚 **[Dataset Setup Guide →](GOOGLE_DRIVE_SETUP.md)**

## 📁 Project Layout

```
FlexiRoute/
├── src/                           # Java source files
│   ├── GuiLauncher.java          # Main desktop GUI application
│   ├── BidirectionalAstar.java   # Core pathfinding algorithm
│   ├── BidirectionalLabeling.java # Label-based search with pruning
│   ├── models/                    # Data models (QueryResult, RoutingMode)
│   ├── managers/                  # Business logic (Theme, History, Metrics)
│   ├── map/                       # OSM map components
│   │   ├── OSMMapComponent.java  # OpenStreetMap tile viewer
│   │   ├── CoordinateConverter.java # Lat/lon to pixel conversion
│   │   ├── TileProvider.java     # Tile server management
│   │   └── RouteOverlayRenderer.java # Path rendering on map
│   └── ui/                        # UI components and panels
│       ├── components/            # Reusable components (SplashScreen)
│       └── panels/                # Main panels (Query, Map, Results, Metrics)
├── dataset/                       # Graph data files (organized by region)
│   ├── London/                    # London dataset (default, 288K nodes)
│   │   ├── nodes_288016.txt
│   │   └── edges_288016.txt
│   └── California/                # California dataset (21K nodes)
│       ├── nodes_21048.txt
│       └── edges_21048.txt
├── scripts/                       # Utility scripts
│   ├── convert_london.py         # Convert London CSV to FlexiRoute format
│   └── README_LONDON.md          # Conversion documentation
├── run.bat / run.sh / run.ps1    # Launch scripts
└── docs/                          # Documentation
```

## 🚀 Quick Start

**Windows**:
```bash
run.bat
```

**Linux/Mac**:
```bash
./run.sh
```

**Or using Java directly**:
```bash
java -cp target/classes GuiLauncher
```

**Or using Maven**:
```bash
mvn exec:java -Dexec.mainClass="GuiLauncher"
```

## 🎨 Visualization Modes

1. **Classic** - Traditional node-edge rendering with color coding
2. **Neon Glow** - Futuristic glowing effects with cyan palette
3. **Gradient Flow** - Smooth blue-to-orange color transitions
4. **3D Effect** - Pseudo-3D with shadows and highlights
5. **Pulse Animation** - Animated traveling marker along path

## 🏗️ Architecture

FlexiRoute follows a modular design with clear separation of concerns:

**Design Patterns**: Builder, Observer, Strategy  
**Thread Safety**: Lock-free atomic counters, SwingWorker, ExecutorService  
**UI Framework**: Java Swing with Material Design principles

### Package Structure
- `models/` - Data models (QueryResult, RoutingMode)
- `managers/` - Business logic & state management
- `ui/components/` - Reusable UI components
- `ui/panels/` - Main application panels

## 🐛 Troubleshooting

**Graph not loading**
- Check [Properties.java](src/Properties.java) for correct file paths
- Verify dataset files are properly downloaded

**Build errors**
- Verify Java 21: `java -version`
- Clean rebuild: `mvn clean compile`

## 🚀 Performance

- **Thread-Safe Metrics**: Lock-free atomic counters
- **Async Query Execution**: Non-blocking UI with SwingWorker
- **Pagination**: Efficient rendering for large graphs
- **Double Buffering**: Smooth animations

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

---

**Enjoy FlexiRoute Navigator!** 🚀


- Launch with `run.bat` (Windows) or `run.sh` (Linux/Mac)
- See `README.md` for setup, dataset formats, and usage instructions

## License
MIT License — free for personal, academic, and commercial use.

---

For more details, see the [README.md](README.md) and [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md).
