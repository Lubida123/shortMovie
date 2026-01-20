param(
    [Parameter(Mandatory = $false)]
    [string]$Root = ".",

    [Parameter(Mandatory = $false)]
    [switch]$ShowClean = $false
)

$ErrorActionPreference = "Continue"

if (-not (Get-Command rg -ErrorAction SilentlyContinue)) {
    Write-Host "Error: ripgrep (rg) not found." -ForegroundColor Red
    Write-Host "Please install it (e.g., 'choco install ripgrep')."
    exit 1
}

$RootPath = (Resolve-Path $Root).Path
$TotalFindings = 0

Write-Host ""
Write-Host "Frontend Quick Audit" -ForegroundColor Cyan
Write-Host "Root: $RootPath"
Write-Host "--------------------------------------------------"

function Run-Rg {
    param(
        [string]$Title,
        [string]$Pattern,
        [string]$Glob = "src/**"
    )

    $args = @("-n", "--hidden", "-g", $Glob, $Pattern, $RootPath)
    $output = & rg @args 2>$null
    $count = if ($output) { $output.Count } else { 0 }

    if ($count -gt 0) {
        Write-Host "WARN: $Title ($count matches)" -ForegroundColor Yellow
        Write-Host "Pattern: $Pattern" -ForegroundColor DarkGray
        $output | ForEach-Object { Write-Host "  $_" }
        $global:TotalFindings += $count
    } elseif ($ShowClean) {
        Write-Host "OK: $Title" -ForegroundColor Green
    }
    Write-Host ""
}

Run-Rg -Title "Build/Runtime Errors" -Pattern "Failed to resolve import|Failed to resolve component|Duplicate attribute"
Run-Rg -Title "Debug Leftovers" -Pattern "console\.(log|debug|warn)|debugger;"
Run-Rg -Title "TODO/FIXME" -Pattern "TODO|FIXME|HACK"
Run-Rg -Title "Auth/Storage Usage" -Pattern "Authorization|Bearer|localStorage|sessionStorage"
Run-Rg -Title "Routing/Guards" -Pattern "beforeEach|router\.push\(|router\.replace\("
Run-Rg -Title "API Calls" -Pattern "http\.(get|post|put|delete)\(" -Glob "src/api/**"
Run-Rg -Title "Video/Playback Hotspots" -Pattern "@wheel|deltaY|requestFullscreen|exitFullscreen|timeupdate|loadedmetadata|@error"

Write-Host "--------------------------------------------------"
if ($TotalFindings -gt 0) {
    Write-Host "Audit Complete. Found $TotalFindings potential issues." -ForegroundColor Yellow
} else {
    Write-Host "Audit Complete. Codebase looks clean based on these patterns." -ForegroundColor Green
}
