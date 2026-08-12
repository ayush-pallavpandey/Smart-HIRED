@echo off
title SmartHire ^| Frontend (port 3000)
color 0D
echo.
echo ===================================================
echo   SmartHire  ^>  Frontend  (port 3000)
echo ===================================================
echo.
echo   App:  http://localhost:3000
echo.
cd /d "%~dp0frontend"

set PROXY_TARGET=http://localhost:8080
set BROWSER=none

call npm start
pause
