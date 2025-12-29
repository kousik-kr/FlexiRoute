@echo off
title Wide-Path Navigator - World Class Edition
echo.
echo ====================================================
echo        Wide-Path Navigator v3.0
echo        World Class Edition
echo ====================================================
echo.

REM Check if compiled
if not exist "target\classes\WorldClassGuiLauncher.class" (
    echo [*] Compiling application...
    call mvn compile -q
    if errorlevel 1 (
        echo [!] Compilation failed. Please check your Java installation.
        pause
        exit /b 1
    )
)

echo [*] Starting Wide-Path Navigator...
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
