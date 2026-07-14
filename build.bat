@echo off
setlocal
cd /d "%~dp0"

set "JARFILE=PacMan.jar"
set "JARCMD="

where jar >nul 2>nul
if not errorlevel 1 set "JARCMD=jar"

if not defined JARCMD if exist "C:\Program Files\Java" (
    for /r "C:\Program Files\Java" %%J in (*.exe) do (
        if /I "%%~nxJ"=="jar.exe" if not defined JARCMD set "JARCMD=%%J"
    )
)
if not defined JARCMD if exist "C:\Program Files\Eclipse Adoptium" (
    for /r "C:\Program Files\Eclipse Adoptium" %%J in (*.exe) do (
        if /I "%%~nxJ"=="jar.exe" if not defined JARCMD set "JARCMD=%%J"
    )
)

if not defined JARCMD (
    echo [ERREUR] Impossible de trouver l'outil "jar" du JDK.
    echo Verifie que le JDK ^(pas seulement le JRE^) est installe.
    pause
    exit /b 1
)

echo Compilation des sources Java...
javac -d bin src\*.java
if errorlevel 1 (
    echo [ERREUR] La compilation a echoue.
    pause
    exit /b 1
)

echo Reconstruction de %JARFILE%...
pushd bin
"%JARCMD%" cfe ..\%JARFILE% App .
popd

echo Termine : %JARFILE% est a jour.
pause
