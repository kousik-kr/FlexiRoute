@echo off
title FlexRoute Navigator - World Class Edition
echo.
echo ====================================================
echo        FlexRoute Navigator
echo        World Class Edition
echo ====================================================
echo.

REM Create target directory if needed
if not exist "target\classes" mkdir target\classes

echo [*] Compiling application...

REM Compile in correct order - core classes first, then UI panels, then launcher
javac -d target/classes src/Node.java src/Edge.java src/Properties.java src/Cluster.java src/Graph.java src/Label.java src/Function.java src/BreakPoint.java src/Query.java src/Result.java src/BidirectionalLabeling.java src/BidirectionalAstar.java src/BidirectionalDriver.java src/DatasetDownloader.java src/GoogleDriveConfigHelper.java src/GoogleDriveDatasetLoader.java 2>nul

REM Compile UI panels
javac -d target/classes -cp target/classes src/ui/panels/WorldClassQueryPanel.java src/ui/panels/WorldClassMapPanel.java src/ui/panels/WorldClassResultsPanel.java src/ui/panels/ResultData.java 2>nul

REM Compile UI components
javac -d target/classes -cp target/classes src/ui/components/WorldClassSplashScreen.java 2>nul

REM Compile the launcher
javac -d target/classes -cp target/classes src/WorldClassGuiLauncher.java 2>nul

if not exist "target\classes\WorldClassGuiLauncher.class" (
    echo [!] Compilation failed. Please check Java installation.
    pause
    exit /b 1
)

echo [*] Starting FlexRoute Navigator...
echo.

REM Run with high-DPI support and additional memory
java -Dsun.java2d.uiScale=1.0 ^
     -Dswing.aatext=true ^
     -Dawt.useSystemAAFontSettings=on ^
     -Xmx2g ^
     -cp "target/classes" ^
     WorldClassGuiLauncher

if errorlevel 1 (
    echo.
    echo [!] Application exited with an error.
    pause
)
