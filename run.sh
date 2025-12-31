#!/bin/bash

# FlexRoute GUI Launcher Script for Linux/Ubuntu
# This script compiles and runs the FlexRoute application

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}FlexRoute Pro - Launch Script${NC}"
echo -e "${CYAN}========================================${NC}"
echo ""

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR/src"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo -e "${RED}✗ ERROR: Java is not installed or not in PATH${NC}"
    echo -e "${YELLOW}Please install Java 21 or higher${NC}"
    echo "  Ubuntu/Debian: sudo apt install openjdk-21-jdk"
    echo "  Fedora/RHEL: sudo dnf install java-21-openjdk"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1)
echo -e "${GREEN}✓ Java found: $JAVA_VERSION${NC}"
echo ""

# Check if javac is installed
if ! command -v javac &> /dev/null; then
    echo -e "${RED}✗ ERROR: javac (Java compiler) is not installed${NC}"
    echo -e "${YELLOW}Please install Java JDK (not just JRE)${NC}"
    echo "  Ubuntu/Debian: sudo apt install openjdk-21-jdk"
    echo "  Fedora/RHEL: sudo dnf install java-21-openjdk-devel"
    exit 1
fi

# Check if dataset files exist
if [ ! -f "dataset/nodes_264346.txt" ]; then
    echo -e "${YELLOW}⚠ WARNING: Dataset files not found in src/dataset/${NC}"
    echo -e "${YELLOW}The application will prompt you to download them from Google Drive${NC}"
    echo ""
fi

# Compile Java files if needed
if [ ! -f "GuiLauncher.class" ]; then
    echo -e "${CYAN}Compiling Java files...${NC}"
    
    javac -d . \
        managers/*.java \
        models/*.java \
        ui/components/*.java \
        ui/panels/*.java \
        GoogleDriveConfigHelper.java \
        GoogleDriveDatasetLoader.java \
        GuiLauncher.java \
        BidirectionalAstar.java \
        Graph.java \
        Node.java \
        Edge.java \
        Label.java \
        Result.java \
        Query.java \
        Properties.java \
        Cluster.java \
        Function.java \
        BreakPoint.java \
        BidirectionalLabeling.java \
        BidirectionalDriver.java
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}✗ ERROR: Compilation failed${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ Compilation successful!${NC}"
    echo ""
fi

# Launch the GUI
echo -e "${CYAN}Launching FlexRoute Pro GUI...${NC}"
echo ""
java GuiLauncher

# Check exit code
EXIT_CODE=$?
if [ $EXIT_CODE -ne 0 ]; then
    echo ""
    echo -e "${RED}Application exited with error code: $EXIT_CODE${NC}"
fi
