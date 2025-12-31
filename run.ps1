# FlexRoute GUI Launcher Script for PowerShell
# This script compiles and runs the FlexRoute application

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "FlexRoute Pro - Launch Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Change to source directory
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location "$scriptDir\src"

# Check if Java is installed
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "✓ Java found: $javaVersion" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "✗ ERROR: Java is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Java 21 or higher" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

# Check if dataset files exist
if (-not (Test-Path "dataset\nodes_264346.txt")) {
    Write-Host "⚠ WARNING: Dataset files not found in src\dataset\" -ForegroundColor Yellow
    Write-Host "The application will prompt you to download them from Google Drive" -ForegroundColor Yellow
    Write-Host ""
}

# Compile Java files if needed
if (-not (Test-Path "GuiLauncher.class")) {
    Write-Host "Compiling Java files..." -ForegroundColor Cyan
    
    javac -d . `
        managers\*.java `
        models\*.java `
        ui\components\*.java `
        ui\panels\*.java `
        GoogleDriveConfigHelper.java `
        GoogleDriveDatasetLoader.java `
        GuiLauncher.java `
        BidirectionalAstar.java `
        Graph.java `
        Node.java `
        Edge.java `
        Label.java `
        Result.java `
        Query.java `
        Properties.java `
        Cluster.java `
        Function.java `
        BreakPoint.java `
        BidirectionalLabeling.java `
        BidirectionalDriver.java
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "✗ ERROR: Compilation failed" -ForegroundColor Red
        Read-Host "Press Enter to exit"
        exit 1
    }
    Write-Host "✓ Compilation successful!" -ForegroundColor Green
    Write-Host ""
}

# Launch the GUI
Write-Host "Launching FlexRoute Pro GUI..." -ForegroundColor Cyan
Write-Host ""
java GuiLauncher

# Check exit code
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "Application exited with error code: $LASTEXITCODE" -ForegroundColor Red
    Read-Host "Press Enter to exit"
}
