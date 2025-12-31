#!/bin/bash
# FlexRoute Dataset Downloader
# Downloads dataset from Google Drive using gdown

set -e

DATASET_DIR="dataset"
DRIVE_URL="https://drive.google.com/drive/folders/1l3NG641rHeshkYW7aDxpb7RhUy0kRuiP"

echo "=== FlexRoute Dataset Downloader ==="
echo ""

# Create dataset directory
mkdir -p "$DATASET_DIR"

# Check if gdown is installed
if ! command -v gdown &> /dev/null; then
    echo "❌ gdown is not installed"
    echo ""
    echo "Please install gdown using:"
    echo "  pip install gdown"
    echo ""
    echo "Or install using:"
    echo "  pip3 install gdown"
    echo ""
    echo "After installation, run this script again."
    echo ""
    echo "Alternatively, download manually from:"
    echo "  $DRIVE_URL"
    echo ""
    exit 1
fi

echo "✓ gdown is installed"
echo ""
echo "Downloading dataset from Google Drive..."
echo "This may take several minutes depending on file size."
echo ""

# Download entire folder
cd "$DATASET_DIR"
gdown --folder "$DRIVE_URL" --remaining-ok

echo ""
echo "✓ Download complete!"
echo ""
echo "Dataset files:"
ls -lh

echo ""
echo "Dataset location: $(pwd)"
