#!/bin/bash
# =============================================================================
# FlexRoute Navigator - World Class Edition
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
echo "║       🗺️  FlexRoute Navigator - World Class Edition        ║"
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

# Check if WorldClassGuiLauncher.class exists
if [ ! -f "target/classes/WorldClassGuiLauncher.class" ]; then
    echo -e "${YELLOW}Compiling sources...${NC}"
    
    # Compile all sources
    javac -d target/classes -sourcepath src \
        src/ui/panels/ResultData.java \
        src/ui/panels/WorldClassMapPanel.java \
        src/ui/panels/WorldClassQueryPanel.java \
        src/ui/panels/WorldClassResultsPanel.java \
        src/ui/components/WorldClassSplashScreen.java \
        src/WorldClassGuiLauncher.java 2>&1
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}ERROR: Compilation failed${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓${NC} Compilation successful"
else
    echo -e "${GREEN}✓${NC} Classes already compiled"
fi

# Check for dataset
echo -e "${BLUE}[3/3]${NC} Checking dataset..."
if [ -f "dataset/nodes_21048.txt" ] && [ -f "dataset/edges_21048.txt" ]; then
    echo -e "${GREEN}✓${NC} Dataset found (21048 nodes)"
elif [ -f "dataset/nodes_264346.txt" ] && [ -f "dataset/edges_264346.txt" ]; then
    echo -e "${GREEN}✓${NC} Dataset found (264346 nodes)"
else
    echo -e "${YELLOW}⚠${NC} Dataset files may need to be downloaded"
    echo "  The application will attempt to load from the dataset/ directory"
fi

echo ""
echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}Launching FlexRoute Navigator...${NC}"
echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
echo ""

# Launch the application with optimal JVM settings
java -Xmx2g \
     -Dsun.java2d.opengl=true \
     -Dawt.useSystemAAFontSettings=on \
     -Dswing.aatext=true \
     -cp target/classes \
     WorldClassGuiLauncher

# Check exit code
EXIT_CODE=$?
if [ $EXIT_CODE -ne 0 ]; then
    echo ""
    echo -e "${RED}Application exited with code: $EXIT_CODE${NC}"
fi

echo ""
echo -e "${CYAN}Thank you for using FlexRoute Navigator!${NC}"
