#!/usr/bin/env pwsh
# ── SmartHire: Start ML Service ─────────────────────────────────────────────
Set-Location "$PSScriptRoot\ml_service"
$env:MODEL_NAME = "all-mpnet-base-v2"
Write-Host ""
Write-Host "╔══════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   SmartHire  ▶  ML Service  (port 8001)         ║" -ForegroundColor Cyan
Write-Host "╚══════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "  → http://localhost:8001/docs" -ForegroundColor Yellow
Write-Host ""
python -m uvicorn model_server:app --host 0.0.0.0 --port 8001 --reload
