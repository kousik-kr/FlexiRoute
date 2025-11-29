#!/bin/bash
# Wide-Path Dataset Direct Downloader
# Downloads dataset files directly from accessible URLs

set -e

DATASET_DIR="dataset"
DRIVE_FOLDER_URL="https://drive.google.com/drive/folders/1l3NG641rHeshkYW7aDxpb7RhUy0kRuiP"

echo "============================================================"
echo "Wide-Path Dataset Direct Download"
echo "============================================================"
echo ""

# Create dataset directory
mkdir -p "$DATASET_DIR"

echo "Dataset will be downloaded to: $(pwd)/$DATASET_DIR"
echo ""
echo "Please follow these steps:"
echo ""
echo "1. Open this Google Drive link in your browser:"
echo "   $DRIVE_FOLDER_URL"
echo ""
echo "2. Download all files (.txt files) from the folder"
echo ""
echo "3. Move/Copy the downloaded files to:"
echo "   $(pwd)/$DATASET_DIR/"
echo ""
echo "Expected files:"
echo "  - nodes_264346.txt"
echo "  - edges_264346.txt"
echo "  - graph_264346.txt (optional)"
echo ""
echo "After downloading, verify with:"
echo "  ls -lh $DATASET_DIR/"
echo ""
echo "Then launch the application:"
echo "  java -cp build GuiLauncher"
echo ""
echo "============================================================"

# Check if any files exist
if [ "$(ls -A $DATASET_DIR/*.txt 2>/dev/null)" ]; then
    echo ""
    echo "Current dataset files:"
    ls -lh "$DATASET_DIR"/*.txt 2>/dev/null || true
    echo ""
fi

# Try to open the browser (optional)
if command -v xdg-open &> /dev/null; then
    read -p "Open Google Drive link in browser now? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        xdg-open "$DRIVE_FOLDER_URL" 2>/dev/null || echo "Could not open browser"
    fi
fi
