@echo off
REM Wide-Path GUI Launcher Script for Windows
REM This script compiles and runs the Wide-Path application

echo ========================================
echo Wide-Path Pro - Launch Script
echo ========================================
echo.

REM Change to source directory
cd /d "%~dp0src"

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 21 or higher
    pause
    exit /b 1
)

REM Check if dataset files exist
if not exist "dataset\nodes_264346.txt" (
    echo WARNING: Dataset files not found in src\dataset\
    echo The application will prompt you to download them from Google Drive
    echo.
)

REM Compile Java files if needed
echo Checking for compiled classes...
if not exist "GuiLauncher.class" (
    echo Compiling Java files...
    javac -d . ^
        managers\*.java ^
        models\*.java ^
        ui\components\*.java ^
        ui\panels\*.java ^
        GoogleDriveConfigHelper.java ^
        GoogleDriveDatasetLoader.java ^
        GuiLauncher.java ^
        BidirectionalAstar.java ^
        Graph.java ^
        Node.java ^
        Edge.java ^
        Label.java ^
        Result.java ^
        Query.java ^
        Properties.java ^
        Cluster.java ^
        Function.java ^
        BreakPoint.java ^
        BidirectionalLabeling.java ^
        BidirectionalDriver.java
    
    if %errorlevel% neq 0 (
        echo ERROR: Compilation failed
        pause
        exit /b 1
    )
    echo Compilation successful!
    echo.
)

REM Launch the GUI
echo Launching Wide-Path Pro GUI...
echo.
java GuiLauncher

REM Check exit code
if %errorlevel% neq 0 (
    echo.
    echo Application exited with error code: %errorlevel%
    pause
)
