@echo off
REM ===================================================================
REM  Wide-Path Pro - Launch Script with Feature Highlights
REM ===================================================================
REM
REM  This script launches Wide-Path Pro and displays information
REM  about the new Reset & Exit features
REM
REM ===================================================================

echo.
echo ============================================================
echo   WIDE-PATH PRO v2.0 - Advanced Pathfinding Analysis
echo ============================================================
echo.
echo  NEW FEATURES:
echo   - Reset Query: Start fresh queries with one click
echo   - Exit System: Graceful shutdown with confirmation
echo   - Success Animations: Visual feedback for operations
echo   - Welcome Screen: Professional startup experience
echo.
echo ============================================================
echo.
echo  Java Version Check...
java -version 2>&1 | findstr "version"
if errorlevel 1 (
    echo ERROR: Java not found! Please install Java 21 LTS.
    pause
    exit /b 1
)
echo.
echo  Building project...
call mvn clean compile -q
if errorlevel 1 (
    echo ERROR: Build failed! Check console for details.
    pause
    exit /b 1
)
echo  Build successful!
echo.
echo ============================================================
echo   HOW TO TEST NEW FEATURES:
echo ============================================================
echo.
echo  RESET QUERY WORKFLOW:
echo   1. Enter query parameters
echo   2. Click "Run Query" button
echo   3. View results and visualizations
echo   4. Click "New Query" button (Blue)
echo   5. System resets - ready for next query!
echo.
echo  EXIT SYSTEM WORKFLOW:
echo   1. After viewing results
echo   2. Click "Exit System" button (Red)
echo   3. Confirm in dialog
echo   4. Watch graceful fade-out animation
echo.
echo ============================================================
echo   KEYBOARD SHORTCUTS:
echo ============================================================
echo.
echo   Ctrl+Enter  - Run current query
echo   Alt+F4      - Exit application (with confirmation)
echo.
echo ============================================================
echo   Launching Wide-Path Pro...
echo ============================================================
echo.

REM Launch the application
mvn exec:java -Dexec.mainClass="GuiLauncher"

echo.
echo Application closed.
echo Thank you for using Wide-Path Pro!
pause
