#!/usr/bin/env pwsh
# ── SmartHire: Start Backend ─────────────────────────────────────────────────
Set-Location "$PSScriptRoot\backend"

# Use H2 in-memory DB for local dev (no Postgres needed)
$env:SPRING_DATASOURCE_URL            = "jdbc:h2:mem:smarthire;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
$env:SPRING_DATASOURCE_USERNAME       = "sa"
$env:SPRING_DATASOURCE_PASSWORD       = ""
$env:SPRING_DATASOURCE_DRIVER_CLASS_NAME = "org.h2.Driver"
$env:ML_SERVICE_URL                   = "http://localhost:8001"
$env:UPLOAD_DIRECTORY                 = "$PSScriptRoot\uploaded_resumes"
$env:CORS_ALLOWED_ORIGINS             = "http://localhost:3000"

Write-Host ""
Write-Host "╔══════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║   SmartHire  ▶  Backend  (port 8080)            ║" -ForegroundColor Green
Write-Host "╚══════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Write-Host "  → http://localhost:8080/actuator/health" -ForegroundColor Yellow
Write-Host "  → http://localhost:8080/api/resumes" -ForegroundColor Yellow
Write-Host "  → http://localhost:8080/h2-console  (H2 DB UI)" -ForegroundColor Yellow
Write-Host ""
mvn spring-boot:run
