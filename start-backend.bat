@echo off
title SmartHire ^| Backend (port 8080)
color 0A
echo.
echo ===================================================
echo   SmartHire  ^>  Backend  (port 8080)
echo ===================================================
echo.
echo   Health:  http://localhost:8080/actuator/health
echo   API:     http://localhost:8080/api/resumes
echo   H2 UI:   http://localhost:8080/h2-console
echo.
cd /d "%~dp0backend"

set SPRING_DATASOURCE_URL=jdbc:h2:mem:smarthire;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
set SPRING_DATASOURCE_USERNAME=sa
set SPRING_DATASOURCE_PASSWORD=
set SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.h2.Driver
set ML_SERVICE_URL=http://localhost:8001
set UPLOAD_DIRECTORY=%~dp0uploaded_resumes
set CORS_ALLOWED_ORIGINS=http://localhost:3000

call "C:\Users\DELL\Downloads\apache-maven-3.9.9-bin\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run
pause
