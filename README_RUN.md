# FlexRoute Pro - Quick Start Guide

## Running the Application

### Windows Users

#### Option 1: Double-click to run (Easiest)
Simply double-click `run.bat` in the project root directory.

#### Option 2: Using Command Prompt or PowerShell
```cmd
run.bat
```

### Linux/Ubuntu Users

#### Option 1: Using the shell script
```bash
chmod +x run.sh  # Make executable (first time only)
./run.sh
```

#### Option 2: Direct execution
```bash
bash run.sh
```

### What the script does:
1. ✓ Checks if Java is installed
2. ✓ Checks if dataset files are present
3. ✓ Compiles Java files (if not already compiled)
4. ✓ Launches the FlexRoute Pro GUI

### First-Time Setup

If dataset files are not found, the application will:
1. Prompt you to download from Google Drive
2. Show a configuration dialog for Google Drive file IDs
3. Download all 4 required files:
   - `nodes_264346.txt`
   - `edges_264346.txt`
   - `clusters_264346.txt`
   - `width_dist_264346.txt`

### Requirements

- **Java 21 or higher** (LTS version recommended)
  - Windows: Download from [Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/)
  - Ubuntu/Debian: `sudo apt install openjdk-21-jdk`
  - Fedora/RHEL: `sudo dnf install java-21-openjdk-devel`
- Internet connection (for first-time dataset download)
- Minimum 2GB RAM
- Display resolution: 1600x900 or higher recommended

### Troubleshooting

#### "Java is not installed or not in PATH"
**Windows:**
- Install Java 21 from [Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/)
- Ensure `java` command is available in your PATH

**Linux/Ubuntu:**
```bash
# Install OpenJDK 21
sudo apt update
sudo apt install openjdk-21-jdk

# Verify installation
java -version
```

#### "Dataset files not found"
- The application will guide you through the download process
- Alternatively, manually download files and place them in `src\dataset\`

#### "Compilation failed"
- Ensure all Java source files are present in `src\` directory
- Check that you have write permissions in the project directory

#### Application doesn't start
- Check if another instance is already running
- Review console output for error messages
- Ensure you have sufficient memory available

### Manual Compilation (Advanced)

If you need to manually compile:

**Windows:**
```cmd
cd src
javac GuiLauncher.java
java GuiLauncher
```

**Linux/Ubuntu:**
```bash
cd src
javac GuiLauncher.java
java GuiLauncher
```

### Running Tests

To run the pathfinding test:

**Windows:**
```cmd
cd src
javac TestPathfinding.java
java TestPathfinding
```

**Linux/Ubuntu:**
```bash
cd src
javac TestPathfinding.java
java TestPathfinding
```

### Project Structure

```
FlexRoute/
├── run.bat              # Windows Batch launcher
├── run.ps1              # PowerShell launcher
├── run.sh               # Linux/Ubuntu Bash launcher
├── src/                 # Java source files
│   ├── dataset/         # Dataset files (auto-downloaded)
│   ├── managers/        # Manager classes
│   ├── models/          # Data models
│   └── ui/              # UI components
├── frontend/            # Web frontend (optional)
└── GOOGLE_DRIVE_SETUP.md  # Google Drive setup guide
```

### Additional Resources

- **Google Drive Setup**: See `GOOGLE_DRIVE_SETUP.md`
- **Architecture**: See `MODULAR_ARCHITECTURE.md`
- **Transformation Summary**: See `TRANSFORMATION_SUMMARY.md`

### Support

For issues or questions:
1. Check the console output for error messages
2. Review the documentation files
3. Open an issue on GitHub
