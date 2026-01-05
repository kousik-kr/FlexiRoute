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
- **Interactive Map**: Zoom (0.1x-10x), pan, minimap, node search
- **5 Render Modes**: Classic, Neon Glow, Gradient Flow, 3D, Minimal
- **Export Capabilities**: High-res PNG screenshots with Ctrl+S
- **Real-Time Tooltips**: Hover for node information

### 🎯 **Smart Query System**
- **Input Validation**: Real-time feedback with visual indicators
- **Recent History**: Last 10 queries with one-click reload
- **Quick Actions**: Swap source/dest (S↔D button)
- **Node Search**: Quick lookup by ID or name

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

The application requires graph dataset files to run. On first launch, the application will:
1. Check for dataset files in the `dataset/` folder
2. Automatically attempt to download them if missing (requires `gdown`)
3. Provide manual download instructions if auto-download fails

**Manual Download (if needed)**:
```bash
pip install gdown
./download_dataset.sh
```

**Or download directly**: [Google Drive](https://drive.google.com/drive/folders/1l3NG641rHeshkYW7aDxpb7RhUy0kRuiP) → Download all files → Place in `dataset/` folder

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
│   └── ui/                        # UI components and panels
│       ├── components/            # Reusable components (SplashScreen)
│       └── panels/                # Main panels (Query, Map, Results, Metrics)
├── dataset/                       # Graph data files (gitignored)
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

# FlexiRoute

FlexiRoute is a high-performance, open-source Java application for advanced route planning and network analysis. It features:

- Fast bidirectional pathfinding algorithms (Aggressive/Balanced modes)
- Interactive desktop GUI built with Java Swing
- Material-inspired design with multiple themes
- Support for large-scale datasets (road networks, clusters, POIs)
- Easy-to-use query panel, live metrics, and results visualization
- MIT licensed for academic and commercial use

## Key Features
- **BidirectionalLabeling**: Efficient shortest-path search with dynamic pruning
- **Aggressive/Balanced Modes**: User-selectable frontier threshold for search speed vs. accuracy
- **GUI-Only**: No web frontend, focused on desktop usability
- **Extensible**: Modular codebase for custom algorithms and data sources

## Getting Started
- Launch with `run.bat` (Windows) or `run.sh` (Linux/Mac)
- See `README.md` for setup, dataset formats, and usage instructions

## License
MIT License — free for personal, academic, and commercial use.

---

For more details, see the [README.md](README.md) and [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md).
