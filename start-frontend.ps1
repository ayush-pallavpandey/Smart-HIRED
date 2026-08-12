#!/usr/bin/env pwsh
# ── SmartHire: Start Frontend ─────────────────────────────────────────────────
Set-Location "$PSScriptRoot\frontend"

$env:PROXY_TARGET    = "http://localhost:8080"
$env:BROWSER         = "none"   # prevent auto-opening browser (open manually)

Write-Host ""
Write-Host "╔══════════════════════════════════════════════════╗" -ForegroundColor Magenta
Write-Host "║   SmartHire  ▶  Frontend  (port 3000)           ║" -ForegroundColor Magenta
Write-Host "╚══════════════════════════════════════════════════╝" -ForegroundColor Magenta
Write-Host ""
Write-Host "  → http://localhost:3000  (React App)" -ForegroundColor Yellow
Write-Host ""
npm start
