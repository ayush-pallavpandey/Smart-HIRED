#!/usr/bin/env pwsh
# ══════════════════════════════════════════════════════════════════
#   SmartHire — Master Launcher
#   Opens 3 separate PowerShell terminal windows:
#     1. ML Service   → http://localhost:8001
#     2. Backend      → http://localhost:8080
#     3. Frontend     → http://localhost:3000
# ══════════════════════════════════════════════════════════════════

$root = $PSScriptRoot

Write-Host ""
Write-Host "╔══════════════════════════════════════════════════════════╗" -ForegroundColor White
Write-Host "║            SmartHire  —  Launching All Services         ║" -ForegroundColor White
Write-Host "╚══════════════════════════════════════════════════════════╝" -ForegroundColor White
Write-Host ""

# ── 1. ML Service ──────────────────────────────────────────────────
Write-Host "  [1/3] Starting ML Service  (port 8001)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-File", "$root\start-ml.ps1" `
    -WindowStyle Normal

Start-Sleep -Seconds 3

# ── 2. Backend ─────────────────────────────────────────────────────
Write-Host "  [2/3] Starting Backend     (port 8080)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-File", "$root\start-backend.ps1" `
    -WindowStyle Normal

Start-Sleep -Seconds 5

# ── 3. Frontend ────────────────────────────────────────────────────
Write-Host "  [3/3] Starting Frontend    (port 3000)..." -ForegroundColor Magenta
Start-Process powershell -ArgumentList "-NoExit", "-File", "$root\start-frontend.ps1" `
    -WindowStyle Normal

Write-Host ""
Write-Host "  All services launched in separate windows." -ForegroundColor White
Write-Host ""
Write-Host "  URLs:" -ForegroundColor White
Write-Host "    Frontend  →  http://localhost:3000" -ForegroundColor Yellow
Write-Host "    Backend   →  http://localhost:8080/actuator/health" -ForegroundColor Yellow
Write-Host "    ML API    →  http://localhost:8001/docs" -ForegroundColor Yellow
Write-Host "    H2 DB UI  →  http://localhost:8080/h2-console" -ForegroundColor Yellow
Write-Host ""
Write-Host "  Wait ~30 s for the ML model to load on first run." -ForegroundColor DarkGray
Write-Host ""
