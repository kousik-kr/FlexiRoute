@echo off
title FlexiRoute Navigator
echo.
echo ====================================================
echo        FlexiRoute Navigator
echo ====================================================
echo.

REM Create target directory if needed
if not exist "target\classes" mkdir target\classes

echo [*] Compiling application...

REM Compile models first (other classes depend on them)
javac -d target/classes src/models/*.java 2>nul

REM Compile core classes
javac -d target/classes -cp target/classes src/Node.java src/Edge.java src/Properties.java src/Cluster.java src/Graph.java src/Label.java src/Function.java src/BreakPoint.java src/Query.java src/Result.java src/RushHour.java src/LabelCache.java src/BidirectionalLabeling.java src/BidirectionalAstar.java src/BidirectionalDriver.java 2>nul

REM Compile managers
javac -d target/classes -cp target/classes src/managers/*.java 2>nul

REM Compile map components (OSM support)
javac -d target/classes -cp target/classes src/map/*.java 2>nul

REM Compile UI components first (LogViewerWindow is needed by QueryHistoryPanel)
javac -d target/classes -cp target/classes src/ui/components/SplashScreen.java src/ui/components/LogViewerWindow.java 2>nul

REM Compile UI panels
javac -d target/classes -cp target/classes src/ui/panels/QueryPanel.java src/ui/panels/MapPanel.java src/ui/panels/ResultsPanel.java src/ui/panels/ResultData.java src/ui/panels/QueryHistoryPanel.java src/ui/panels/MetricsDashboard.java src/ui/panels/PreferenceSlidersPanel.java 2>nul

REM Compile the launcher
javac -d target/classes -cp target/classes src/GuiLauncher.java 2>nul

if not exist "target\classes\GuiLauncher.class" (
    echo [!] Compilation failed. Please check Java installation.
    pause
    exit /b 1
)

echo [*] Starting FlexiRoute Navigator...
echo.

REM Run with high-DPI support and additional memory
java -Dsun.java2d.uiScale=1.0 ^
     -Dswing.aatext=true ^
     -Dawt.useSystemAAFontSettings=on ^
     -Xmx2g ^
     -cp "target/classes" ^
     GuiLauncher

if errorlevel 1 (
    echo.
    echo [!] Application exited with an error.
    pause
)
