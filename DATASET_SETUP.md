# Dataset Configuration Summary

## ✅ What Was Done

### 1. Dataset Folder Structure
- Created `dataset/` folder in project root
- Added `dataset/.gitkeep` to track the folder in git
- Added `dataset/README.md` with comprehensive setup instructions

### 2. Git Configuration
Updated `.gitignore` to exclude dataset files:
- `dataset/` - All dataset files ignored
- `*.txt`, `*.dat`, `*.csv` - All data files ignored
- **Exception**: Configuration files and README preserved

### 3. Automatic Dataset Downloader
Created `src/DatasetDownloader.java`:
- **Auto-detects** missing dataset files on application startup
- **Shows helpful instructions** with Google Drive link
- **Attempts automatic download** using `gdown` if available
- **Lists missing files** with status indicators (✓/✗)
- **Provides manual download options** if auto-download fails

### 4. Project Configuration
Updated `src/BidirectionalAstar.java`:
- Changed default directory from hardcoded Windows path to `dataset/` folder
- Integrated dataset downloader in `configureDefaults()` method
- Auto-detects dataset on initialization
- Uses platform-independent paths (`System.getProperty("user.dir")`)

### 5. Download Scripts
Created `download_dataset.sh`:
- Bash script for easy manual download
- Checks for `gdown` installation
- Downloads entire Google Drive folder
- Shows download progress and file sizes

### 6. Documentation
Updated `README.md`:
- Added dataset setup section
- Included prerequisites for dataset download (`gdown`)
- Added manual download instructions
- Linked to dataset README

## 📦 Google Drive Dataset

**Link**: https://drive.google.com/drive/folders/1l3NG641rHeshkYW7aDxpb7RhUy0kRuiP

**Expected Files**:
- `nodes_264346.txt` - Node data
- `edges_264346.txt` - Edge data
- `graph_264346.txt` - Graph structure
- Variants: `*_old.txt` files

## 🚀 Usage

### First Time Setup

**Option 1: Automatic (Recommended)**
```bash
# Install gdown
pip install gdown

# Just run the application - it will handle the rest
java -cp build GuiLauncher
```

**Option 2: Using Download Script**
```bash
# Install gdown
pip install gdown

# Run download script
./download_dataset.sh

# Then run application
java -cp build GuiLauncher
```

**Option 3: Manual Download**
```bash
# 1. Visit the Google Drive link
# 2. Download all files
# 3. Place them in the dataset/ folder
# 4. Run the application
java -cp build GuiLauncher
```

### What Happens on First Run

When you launch the application:
1. ✓ Application initializes
2. ✓ Checks `dataset/` folder for required files
3. ✗ If files missing:
   - Shows Google Drive link
   - Lists which files are missing
   - Attempts automatic download if `gdown` is installed
   - Provides manual download instructions
4. ✓ Loads graph data and starts GUI

## 📋 Console Output Example

```
[Init] Defaults configured. Thresholds set and pool size=20
[Dataset] Some dataset files are missing.
[Dataset] Please download the dataset manually from:
[Dataset] https://drive.google.com/drive/folders/1l3NG641rHeshkYW7aDxpb7RhUy0kRuiP
[Dataset] 
[Dataset] Extract the files to: /home/gunturi/Wide-Path/dataset
[Dataset] 
[Dataset] Required files:
[Dataset]   ✗ Missing - edges_264346.txt
[Dataset]   ✗ Missing - nodes_264346.txt
[Dataset]   ...

[Dataset] Tip: Install gdown for automatic downloads:
[Dataset]   pip install gdown
```

## 🔧 Technical Details

### DatasetDownloader.java
- **Package**: Root (no package)
- **Methods**:
  - `ensureDatasetExists()` - Main entry point, returns dataset path
  - `checkRequiredFiles()` - Validates file presence
  - `isGdownAvailable()` - Checks for gdown installation
  - `downloadUsingGdown()` - Attempts automatic download
  - `getDatasetFilePath()` - Returns path to specific file
  - `listDatasetFiles()` - Lists all dataset files with sizes

### BidirectionalAstar.java Changes
```java
// Before:
private static final String currentDirectory = "C:\\Users\\kousi\\Wide-Path\\";

// After:
private static final String currentDirectory = System.getProperty("user.dir") + "/dataset/";
```

Added to `configureDefaults()`:
```java
// Ensure dataset exists (auto-download if missing)
try {
    String datasetPath = DatasetDownloader.ensureDatasetExists();
    if (datasetPath != null && !datasetPath.isEmpty()) {
        configuredGraphDataDir = datasetPath + "/";
        dataDirectory = configuredGraphDataDir;
    }
} catch (Exception e) {
    System.err.println("[Init] Warning: Could not verify dataset: " + e.getMessage());
}
```

## 📁 File Structure

```
Wide-Path/
├── dataset/                    # Dataset folder (git ignored)
│   ├── .gitkeep               # Tracks empty folder
│   ├── README.md              # Dataset setup guide
│   ├── nodes_264346.txt       # (Downloaded)
│   ├── edges_264346.txt       # (Downloaded)
│   └── ...                    # Other dataset files
├── src/
│   ├── DatasetDownloader.java # NEW: Auto-download utility
│   └── BidirectionalAstar.java # MODIFIED: Uses dataset folder
├── download_dataset.sh         # NEW: Download script
├── .gitignore                 # MODIFIED: Excludes dataset
└── README.md                  # MODIFIED: Added dataset section
```

## 🛠️ Troubleshooting

### "gdown: command not found"
```bash
pip install gdown
# or
pip3 install gdown
```

### Permission denied on download script
```bash
chmod +x download_dataset.sh
```

### Files downloaded to wrong location
Ensure you're in the project root when running download commands:
```bash
cd /path/to/Wide-Path
./download_dataset.sh
```

### Large file download timeout
- Use stable internet connection
- Download files individually from Drive
- Use Google Drive desktop client for very large files

## ✨ Benefits

1. **No Hardcoded Paths**: Works on any platform (Windows, Linux, Mac)
2. **Git-Friendly**: Dataset files never committed (saves repo size)
3. **User-Friendly**: Clear instructions and automatic setup
4. **Flexible**: Multiple download options (auto/script/manual)
5. **Informative**: Shows which files are missing and how to get them

## 📝 Notes

- Dataset files are typically several MB to GB in size
- First download may take 5-10 minutes depending on internet speed
- Files only need to be downloaded once
- Application will reuse existing files if present
- Safe to delete and re-download if needed

---

**Created**: November 29, 2025  
**Dataset Link**: https://drive.google.com/drive/folders/1l3NG641rHeshkYW7aDxpb7RhUy0kRuiP
