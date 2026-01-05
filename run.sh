#!/bin/bash
# =============================================================================
# FlexiRoute Navigator
# Launch Script for Ubuntu/Linux
# =============================================================================

# Colors for terminal output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${CYAN}"
echo "╔════════════════════════════════════════════════════════════╗"
echo "║             🗺️  FlexiRoute Navigator                       ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Check for Java
echo -e "${BLUE}[1/3]${NC} Checking Java installation..."
if ! command -v java &> /dev/null; then
    echo -e "${RED}ERROR: Java is not installed or not in PATH${NC}"
    echo "Please install Java 17 or higher:"
    echo "  sudo apt update && sudo apt install openjdk-17-jdk"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
echo -e "${GREEN}✓${NC} Java found (version $JAVA_VERSION)"

# Check/compile sources
echo -e "${BLUE}[2/3]${NC} Checking compiled classes..."

mkdir -p target/classes

# Check if GuiLauncher.class exists
if [ ! -f "target/classes/GuiLauncher.class" ]; then
    echo -e "${YELLOW}Compiling source files...${NC}"
    
    # Compile models first (other classes depend on them)
    javac -d target/classes \
        src/models/*.java 2>&1
    
    # Compile core classes
    javac -d target/classes -cp target/classes \
        src/Node.java src/Edge.java src/Properties.java src/Cluster.java \
        src/Graph.java src/Label.java src/Function.java src/BreakPoint.java \
        src/Query.java src/Result.java src/BidirectionalLabeling.java \
        src/BidirectionalAstar.java src/BidirectionalDriver.java \
        src/DatasetDownloader.java src/GoogleDriveConfigHelper.java \
        src/GoogleDriveDatasetLoader.java 2>&1
    
    # Compile managers
    javac -d target/classes -cp target/classes \
        src/managers/*.java 2>&1
    
    # Compile UI panels
    javac -d target/classes -cp target/classes \
        src/ui/panels/QueryPanel.java \
        src/ui/panels/MapPanel.java \
        src/ui/panels/ResultsPanel.java \
        src/ui/panels/ResultData.java \
        src/ui/panels/QueryHistoryPanel.java \
        src/ui/panels/MetricsDashboard.java 2>&1
    
    # Compile UI components
    javac -d target/classes -cp target/classes \
        src/ui/components/SplashScreen.java 2>&1
    
    # Compile launcher
    javac -d target/classes -cp target/classes \
        src/GuiLauncher.java 2>&1
    
    if [ ! -f "target/classes/GuiLauncher.class" ]; then
        echo -e "${RED}ERROR: Compilation failed${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓${NC} Compilation successful"
else
    echo -e "${GREEN}✓${NC} Classes already compiled"
fi

# Launch application
echo -e "${BLUE}[3/3]${NC} Launching FlexiRoute Navigator..."
echo ""
echo -e "${CYAN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║${NC}                                                            ${CYAN}║${NC}"
echo -e "${CYAN}║${NC}     🗺️  FlexiRoute Navigator                               ${CYAN}║${NC}"
echo -e "${CYAN}║${NC}                                                            ${CYAN}║${NC}"
echo -e "${CYAN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

java -Dsun.java2d.uiScale=1.0 \
     -Dswing.aatext=true \
     -Dawt.useSystemAAFontSettings=on \
     -Xmx2g \
     -cp "target/classes" \
     GuiLauncher

EXIT_CODE=$?
if [ $EXIT_CODE -ne 0 ]; then
    echo -e "${RED}Application exited with code: $EXIT_CODE${NC}"
fi
