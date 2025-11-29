# Wide-Path Pro v3.0 - World-Class Edition

Wide-Path is a **world-class pathfinding analysis platform** featuring:
- **Java 21 LTS** backend with bidirectional A* algorithm
- **Enterprise-Grade Swing GUI** with premium design system
- **React + Vite frontend** for web-based visualization (Optional)
- **REST API** for programmatic access

## ✨ What's New in v3.0 - WORLD-CLASS REDESIGN

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
✨ **Query Presets**: Quick Test, Long Distance, Morning Rush  
✨ **Input Validation**: Real-time feedback with visual indicators  
✨ **Recent History**: Last 10 queries with one-click reload  
✨ **Quick Actions**: Random generator, swap source/dest  

### 📊 **Real-Time Analytics**
✨ **4 Metric Cards**: Total queries, avg time, success rate, throughput  
✨ **3 Chart Types**: Line (trend), Pie (ratio), Bar (distribution)  
✨ **Live Updates**: 1-second refresh with smooth animations  
✨ **Data Export**: PDF reports, CSV data, chart screenshots  

### ⌨️ **Power User Features**
✨ **20+ Keyboard Shortcuts**: Ctrl+Enter (run), Ctrl+T (theme), Ctrl+R (random)  
✨ **Splash Screen**: Professional animated startup  
✨ **Toast Notifications**: Success, error, warning, info messages  
✨ **Status Bar**: Real-time feedback and system info  

📚 **[🚀 Quick Start Guide →](QUICK_START_GUIDE.md)**  
📚 **[✨ World-Class Features →](WORLD_CLASS_GUI_FEATURES.md)**  
📚 **[🎨 Design System →](DESIGN_SYSTEM.md)**  
📚 **[📋 Complete Redesign Summary →](GUI_REDESIGN_SUMMARY.md)**

## 🎯 Previous Versions

### v2.0 Features (Still Available)
✨ Modular Architecture with 11 classes across 4 packages  
✨ Query History with last 100 queries  
✨ Graph Pagination for large datasets  
✨ Reset Query & Exit System  

📚 **[v2.0 Transformation Summary →](TRANSFORMATION_SUMMARY.md)**  
📚 **[Reset & Exit Features →](QUERY_RESET_EXIT_FEATURES.md)**  
📚 **[Visual Workflow →](VISUAL_WORKFLOW.md)**

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

📚 **[Dataset Setup Guide →](dataset/README.md)**

## 📁 Project Layout
- `src/` – Java sources for API server, GUI application, and bidirectional A* implementation.
  - `GuiLauncher.java` – Main desktop GUI application (NEW v2.0)
  - `models/` – Data models (QueryResult)
  - `managers/` – Business logic (Theme, History, Metrics)
  - `ui/components/` – Reusable UI components (StatusBar, ModernButton)
  - `ui/panels/` – Complex UI panels (Input, Metrics, History, Visualization)
  - `ApiServer.java` – REST API server
  - `BidirectionalAstar.java` – Core pathfinding algorithm
- `frontend/` – React + Vite web UI (optional)
- `run.sh` / `run_gui.sh` – Helper scripts for Linux/Mac
- `MODULAR_ARCHITECTURE.md` – Complete architecture documentation (NEW)
- `QUICK_REFERENCE.md` – Developer quick reference (NEW)
- `TRANSFORMATION_SUMMARY.md` – What changed in v2.0 (NEW)

## 🚀 Quick Start

### Option 1: Desktop GUI Application (Recommended)

Run the modern Swing GUI application:

```bash
# Build the project
mvn clean compile

# Run the GUI
java -cp target/classes GuiLauncher
```

Or using Maven directly:
```bash
mvn exec:java -Dexec.mainClass="GuiLauncher"
```

**Features**:
- 📊 Tabbed interface (Results, Visualization, Metrics, History)
- 🎨 5 creative visualization modes
- 📈 Real-time performance metrics
- 🕐 Query history with analytics
- 🎯 Pre-query input visualization
- 📄 Graph pagination for large datasets

**[Read GUI Quick Reference →](QUICK_REFERENCE.md)**

### Option 2: Full Stack (API + Web Frontend)

For the complete web-based experience:

```bash
./run.sh
```

This compiles the Java sources, starts the API on port **8080**, and launches the Vite dev server on **5173** with `VITE_API_BASE` pointed at the API. Visit http://localhost:5173 to use the web UI.

Options:
- `BACKEND_PORT` and `FRONTEND_PORT` environment variables override the defaults.
- `--port` and `--frontend-port` flags set the ports explicitly.

## 📚 Documentation

- **[MODULAR_ARCHITECTURE.md](MODULAR_ARCHITECTURE.md)** - Complete architecture guide
  - Package structure and component details
  - Design patterns and best practices
  - API documentation for all classes
  - Usage examples and code snippets

- **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** - Developer quick reference
  - Common tasks and code examples
  - Color palette and styling guide
  - Performance tips
  - Debugging help

- **[TRANSFORMATION_SUMMARY.md](TRANSFORMATION_SUMMARY.md)** - v2.0 changes
  - Before/after comparison
  - New features overview
  - Migration guide
  - Achievement summary

## 🎨 Visualization Modes

The desktop GUI offers 5 creative visualization modes:

1. **Classic** - Traditional node-edge rendering with color coding
2. **Neon Glow** - Futuristic glowing effects with cyan palette
3. **Gradient Flow** - Smooth blue-to-orange color transitions
4. **3D Effect** - Pseudo-3D with shadows and highlights
5. **Pulse Animation** - Animated traveling marker along path

## 🏗️ Architecture

Wide-Path v2.0 uses a **modular architecture** with clear separation of concerns:

```
models/          → Immutable data structures
managers/        → Business logic & state management
ui/components/   → Reusable UI elements
ui/panels/       → Complex composite views
```

**Design Patterns**: Builder, Observer, Strategy  
**Thread Safety**: AtomicLong/AtomicInteger, SwingWorker, ExecutorService  
**UI Framework**: Java Swing with Material Design principles

---

## 🌐 API Server & Web Frontend

### Running without the frontend

Run only the Java API server (good for headless benchmarking or hooking up a different client):
```bash
./run.sh --backend-only --port 9000
```
The server seeds a tiny in-memory network so the endpoints respond immediately even without a dataset on disk.

### Running only the web frontend

Point the web UI at an existing API host:
```bash
./run.sh --frontend-only --api-base "http://localhost:8080/api" --frontend-port 5173
```
Alternatively, run the commands manually:
```bash
cd frontend
npm install
VITE_API_BASE="http://localhost:8080/api" npm run dev -- --host 0.0.0.0 --port 5173
```
You can also copy `frontend/.env.example` to `.env` and edit `VITE_API_BASE`.

### Manual backend build/run

Compile and start the API directly:
```bash
javac -d build src/*.java
java -cp build ApiServer 8080
```

Or using Maven:
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="ApiServer" -Dexec.args="8080"
```

The server exposes:
- `GET /api/nodes?search=<id substring>` – node search
- `GET /api/network/meta` – vertex count and bounding box
- `POST /api/queries/run` – run bidirectional A* query
- `GET /api/metrics/live` – JVM memory snapshot

### Production frontend build

Build static files for the web UI:
```bash
cd frontend
npm install
npm run build
```
Output lands in `frontend/dist/` for serving by any static file server.

---

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=YourTestClass

# Build with tests
mvn clean package
```

## 🔧 Development

### Building the Project

```bash
# Clean and compile
mvn clean compile

# Package as JAR
mvn package

# Skip tests
mvn package -DskipTests
```

### Running the GUI

```bash
# Direct Java execution
java -cp target/classes GuiLauncher

# Using Maven
mvn exec:java -Dexec.mainClass="GuiLauncher"
```

### Adding a New Visualization Mode

1. Edit `src/ui/panels/AdvancedMapPanel.java`
2. Add enum value to `VisualizationMode`
3. Implement render method
4. Add to switch statement

See [QUICK_REFERENCE.md](QUICK_REFERENCE.md#adding-visualization-mode) for details.

## 🐛 Troubleshooting
- If `run.sh` reports missing commands, install the prerequisite packages above.
- Use `--api-base` when the frontend and backend are on different hosts/ports.
- Stop the script with `Ctrl+C`; it will shut down the background Java process when started in full-stack mode.


### Common Issues

**Graph not loading in GUI**
- Check Properties.java for correct graph file paths
- Ensure graph files exist in configured directory

**Build errors**
- Verify Java 21 is installed: `java -version`
- Clean and rebuild: `mvn clean compile`

---

##  Performance

- **Thread-Safe Metrics**: Lock-free atomic counters
- **Async Query Execution**: Non-blocking UI with SwingWorker
- **Pagination**: Efficient rendering for large graphs (10-500 nodes/page)
- **Double Buffering**: Smooth animations and transitions

##  License & Credits

Wide-Path Pro v2.0 - Advanced Pathfinding Analysis System

**Architecture**: Modular design with separation of concerns
**Patterns**: Builder, Observer, Strategy
**UI Design**: Material Design principles

---

##  Additional Resources

- [Complete Architecture Guide](MODULAR_ARCHITECTURE.md)
- [Developer Quick Reference](QUICK_REFERENCE.md)
- [v2.0 Transformation Summary](TRANSFORMATION_SUMMARY.md)

**Version**: 2.0 | **Status**: Production Ready  | **Java**: 21 LTS Required

 **Enjoy Wide-Path Pro!** 
