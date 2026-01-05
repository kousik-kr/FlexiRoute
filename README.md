# FlexiRoute Navigator

![Java](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=flat&logo=openjdk&logoColor=white)
![React](https://img.shields.io/badge/React-18-61DAFB?style=flat&logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-5-646CFF?style=flat&logo=vite&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green.svg)

FlexiRoute is a **professional pathfinding analysis platform** featuring:
- **Java 21 LTS** backend with bidirectional A* algorithm
- **Enterprise-Grade Swing GUI** with premium design system
- **React + Vite frontend** for web-based visualization (Optional)
- **REST API** for programmatic access

## ✨ Features

### 🎨 **Premium Design System**
✨ **6 Professional Themes**: Light, Dark, Auto, Oceanic, Forest, Sunset  
✨ **Glassmorphism UI**: Modern translucent panels with blur effects  
✨ **Animated Components**: Smooth 60 FPS Material Design transitions  
✨ **Accessibility**: WCAG 2.1 AA compliant with full keyboard navigation  

### 🗺️ **Advanced Visualization**
✨ **Interactive Map**: Zoom (0.1x-10x), pan, minimap, node search  
✨ **5 Render Modes**: Classic, Neon Glow, Gradient Flow, 3D, Minimal  
✨ **Export Capabilities**: High-res PNG screenshots with Ctrl+S  
✨ **Real-Time Tooltips**: Hover for node information  

### 🎯 **Smart Query System**
✨ **Input Validation**: Real-time feedback with visual indicators  
✨ **Recent History**: Last 10 queries with one-click reload  
✨ **Quick Actions**: Swap source/dest (S↔D button)  
✨ **Node Search**: Quick lookup by ID or name  

### 📊 **Real-Time Analytics**
✨ **4 Metric Cards**: Total queries, avg time, success rate, throughput  
✨ **3 Chart Types**: Line (trend), Pie (ratio), Bar (distribution)  
✨ **Live Updates**: 1-second refresh with smooth animations  
✨ **Data Export**: PDF reports, CSV data, chart screenshots  

### ⚡ **Search Strategies**
✨ **Aggressive Mode**: Faster search with frontier threshold of 10 (more pruning)  
✨ **Balanced Mode**: Thorough search with frontier threshold of 50 (balanced exploration)  

### ⌨️ **Power User Features**
✨ **20+ Keyboard Shortcuts**: Ctrl+Enter (run), Ctrl+T (theme), Ctrl+S (export)  
✨ **Splash Screen**: Professional animated startup  
✨ **Toast Notifications**: Success, error, warning, info messages  
✨ **Tabbed Interface**: Results, Visualization, Metrics, History  

📚 **[🚀 Quick Start Guide →](QUICK_START_GUIDE.md)**  
📚 **[🎨 Design System →](DESIGN_SYSTEM.md)**  

## Prerequisites
- **Java 21+ JDK** (LTS version recommended).
- **Maven** (for building the Java project).
- **Node.js 18+ and npm** (optional, for the Vite frontend).
- **curl** (optional, for API testing).
- **Python 3 + gdown** (optional, for automatic dataset download).

Example install on Ubuntu/Debian:
```bash
sudo apt update
sudo apt install openjdk-21-jdk maven nodejs npm curl python3-pip

# Optional: For automatic dataset download
pip install gdown
```

Windows users can install Java 21 JDK from [Adoptium](https://adoptium.net/) and Maven from [Apache Maven](https://maven.apache.org/).

## 📦 Dataset Setup

The application requires graph dataset files to run. On first launch, the application will:
1. Check for dataset files in the `dataset/` folder
2. Automatically attempt to download them if missing (requires `gdown`)
3. Provide manual download instructions if auto-download fails

**Manual Download (if needed)**:
```bash
# Install gdown
pip install gdown

# Run the download script
./download_dataset.sh
```

**Or download directly**:
- Visit: https://drive.google.com/drive/folders/1l3NG641rHeshkYW7aDxpb7RhUy0kRuiP
- Download all files and place them in the `dataset/` folder

📚 **[Dataset Setup Guide →](GOOGLE_DRIVE_SETUP.md)**

## 📁 Project Layout
- `src/` – Java sources for API server, GUI application, and bidirectional A* implementation
  - `GuiLauncher.java` – Main desktop GUI application
  - `models/` – Data models (QueryResult, RoutingMode)
  - `managers/` – Business logic (ThemeManager, QueryHistoryManager, MetricsCollector)
  - `ui/components/` – Reusable UI components (SplashScreen)
  - `ui/panels/` – UI panels (QueryPanel, MapPanel, ResultsPanel, MetricsDashboard, QueryHistoryPanel, ResultData)
  - `ApiServer.java` – REST API server
  - `BidirectionalAstar.java` – Core pathfinding algorithm
  - `BidirectionalLabeling.java` – Label-based search with pruning strategies
- `frontend/` – React + Vite web UI (optional)
- `run.sh` / `run.bat` / `run.ps1` – Helper scripts for Linux/Mac/Windows
- `QUICK_START_GUIDE.md` – Quick start documentation
- `DESIGN_SYSTEM.md` – Design system documentation

## 🚀 Quick Start

### Desktop GUI (Recommended)

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

**Features**:
- 📊 Tabbed interface (Results, Visualization, Metrics, History)
- 🎨 5 creative visualization modes (Classic, Neon Glow, Gradient Flow, 3D, Pulse)
- 📈 Real-time performance metrics with live charts
- 🕐 Query history with analytics
- 🎯 Pre-query input visualization
- 📄 Graph pagination for large datasets
- ⚡ Configurable search strategies (Aggressive/Balanced)
- 🎨 Professional themes and design system

### Web Frontend (Optional)

For the complete web-based experience:

```bash
run.bat
```

This compiles the Java sources, starts the API on port **8080**, and launches the Vite dev server on **5173** with `VITE_API_BASE` pointed at the API. Visit http://localhost:5173 to use the web UI.

Options:
- `BACKEND_PORT` and `FRONTEND_PORT` environment variables override the defaults
- `--port` and `--frontend-port` flags set the ports explicitly

## 🎨 Visualization Modes

The desktop GUI offers 5 creative visualization modes:

1. **Classic** - Traditional node-edge rendering with color coding
2. **Neon Glow** - Futuristic glowing effects with cyan palette
3. **Gradient Flow** - Smooth blue-to-orange color transitions
4. **3D Effect** - Pseudo-3D with shadows and highlights
5. **Pulse Animation** - Animated traveling marker along path

## ⚡ Search Strategies

Choose between two search optimization modes:
- **Aggressive** - Faster search with frontier threshold of 10 (more pruning)
- **Balanced** - Thorough search with frontier threshold of 50 (balanced exploration)

## 🏗️ Architecture

FlexiRoute follows a modular design with clear separation of concerns:

**Design Patterns**: Builder, Observer, Strategy  
**Thread Safety**: Lock-free atomic counters, SwingWorker, ExecutorService  
**UI Framework**: Java Swing with Material Design principles

### Package Structure
- `models/` - Data models (QueryResult, RoutingMode)
- `managers/` - Business logic & state management (ThemeManager, QueryHistoryManager, MetricsCollector)
- `ui/components/` - Reusable UI components (SplashScreen)
- `ui/panels/` - Complex composite views (QueryPanel, MapPanel, ResultsPanel, MetricsDashboard, QueryHistoryPanel)

---

## 📚 Documentation

- [Quick Start Guide](QUICK_START_GUIDE.md)
- [Design System](DESIGN_SYSTEM.md)

---

## 🐛 Troubleshooting

### Common Issues

**Graph not loading in GUI**
- Check [Properties.java](src/Properties.java) for correct graph file paths
- Ensure graph files exist in configured directory
- Verify dataset files are properly downloaded

**Build errors**
- Verify Java 21 is installed: `java -version`
- Clean and rebuild: `mvn clean compile`

**Dataset download issues**
- If `run.sh` reports missing commands, install the prerequisite packages
- Use manual download from Google Drive if auto-download fails

**Port conflicts**
- Use `--api-base` when the frontend and backend are on different hosts/ports
- Change ports with `--port` and `--frontend-port` flags

---

## 🚀 Performance

- **Thread-Safe Metrics**: Lock-free atomic counters
- **Async Query Execution**: Non-blocking UI with SwingWorker
- **Pagination**: Efficient rendering for large graphs (10-500 nodes/page)
- **Double Buffering**: Smooth animations and transitions

---

## 📄 License & Credits

FlexiRoute Navigator - Advanced Pathfinding Analysis System

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

**Status**: Production Ready | **Java**: 21 LTS Required

---

## 🤝 Contributing

Contributions are welcome! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

**Enjoy FlexiRoute Navigator!** 🚀