# FlexiRoute Navigator - PowerShell Launch Script

Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "         FlexiRoute Navigator" -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host ""

# Get script directory
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

# Check if Java is installed
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "[OK] Java found: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Java is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Java 17 or higher" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

# Create target directory if needed
if (-not (Test-Path "target\classes")) {
    New-Item -ItemType Directory -Path "target\classes" -Force | Out-Null
}

Write-Host "[*] Checking compiled classes..." -ForegroundColor Cyan

# Compile if needed
if (-not (Test-Path "target\classes\GuiLauncher.class")) {
    Write-Host "[*] Compiling source files..." -ForegroundColor Yellow
    
    # Compile models first (other classes depend on them)
    javac -d target/classes `
        src/models/*.java 2>&1
    
    # Compile core classes
    javac -d target/classes -cp target/classes `
        src/Node.java src/Edge.java src/Properties.java src/Cluster.java `
        src/Graph.java src/Label.java src/Function.java src/BreakPoint.java `
        src/Query.java src/Result.java src/BidirectionalLabeling.java `
        src/BidirectionalAstar.java src/BidirectionalDriver.java `
        src/DatasetDownloader.java src/GoogleDriveConfigHelper.java `
        src/GoogleDriveDatasetLoader.java 2>&1
    
    # Compile managers
    javac -d target/classes -cp target/classes `
        src/managers/*.java 2>&1
    
    # Compile map components (OSM support)
    javac -d target/classes -cp target/classes `
        src/map/*.java 2>&1
    
    # Compile UI panels
    javac -d target/classes -cp target/classes `
        src/ui/panels/QueryPanel.java `
        src/ui/panels/MapPanel.java `
        src/ui/panels/ResultsPanel.java `
        src/ui/panels/ResultData.java `
        src/ui/panels/QueryHistoryPanel.java `
        src/ui/panels/MetricsDashboard.java `
        src/ui/panels/PreferenceSlidersPanel.java 2>&1
    
    # Compile UI components
    javac -d target/classes -cp target/classes `
        src/ui/components/SplashScreen.java 2>&1
    
    # Compile launcher
    javac -d target/classes -cp target/classes `
        src/GuiLauncher.java 2>&1
    
    if (-not (Test-Path "target\classes\GuiLauncher.class")) {
        Write-Host "[ERROR] Compilation failed" -ForegroundColor Red
        Read-Host "Press Enter to exit"
        exit 1
    }
    Write-Host "[OK] Compilation successful" -ForegroundColor Green
} else {
    Write-Host "[OK] Classes already compiled" -ForegroundColor Green
}

Write-Host ""
Write-Host "[*] Starting FlexiRoute Navigator..." -ForegroundColor Cyan
Write-Host ""

java -Dsun.java2d.uiScale=1.0 `
     -Dswing.aatext=true `
     -Dawt.useSystemAAFontSettings=on `
     -Xmx2g `
     -cp "target/classes" `
     GuiLauncher

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "[!] Application exited with code: $LASTEXITCODE" -ForegroundColor Red
    Read-Host "Press Enter to exit"
}
