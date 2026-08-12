@echo off
title SmartHire ^| ML Service (port 8001)
color 0B
echo.
echo ===================================================
echo   SmartHire  ^>  ML Service  (port 8001)
echo ===================================================
echo.
echo   Swagger UI: http://localhost:8001/docs
echo.
cd /d "%~dp0ml_service"
set MODEL_NAME=all-mpnet-base-v2
python -m uvicorn model_server:app --host 0.0.0.0 --port 8001 --reload
pause
