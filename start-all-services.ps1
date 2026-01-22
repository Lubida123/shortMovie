# ========================================
# Start All Recommendation System Services
# ========================================

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Starting All Recommendation Services" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check base services
Write-Host "Step 1: Checking base services..." -ForegroundColor Yellow
Write-Host ""

# Check MySQL
$mysqlCheck = netstat -an | Select-String "3306.*LISTENING"
if ($mysqlCheck) {
    Write-Host "  [OK] MySQL is running (3306)" -ForegroundColor Green
} else {
    Write-Host "  [ERROR] MySQL is not running" -ForegroundColor Red
    Write-Host "     Please start MySQL: net start MySQL80" -ForegroundColor Yellow
    exit 1
}

# Check Redis
$redisCheck = netstat -an | Select-String "6379.*LISTENING"
if ($redisCheck) {
    Write-Host "  [OK] Redis is running (6379)" -ForegroundColor Green
} else {
    Write-Host "  [ERROR] Redis is not running" -ForegroundColor Red
    Write-Host "     Please start Redis: redis-server" -ForegroundColor Yellow
    exit 1
}

# Check Kafka
$kafkaCheck = netstat -an | Select-String "9092.*LISTENING"
if ($kafkaCheck) {
    Write-Host "  [OK] Kafka is running (9092)" -ForegroundColor Green
} else {
    Write-Host "  [ERROR] Kafka is not running" -ForegroundColor Red
    Write-Host "     Please start Zookeeper and Kafka first" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Step 2: Check backend service
Write-Host "Step 2: Checking backend service..." -ForegroundColor Yellow
$backendCheck = netstat -an | Select-String "8080.*LISTENING"
if ($backendCheck) {
    Write-Host "  [OK] Backend service is running (8080)" -ForegroundColor Green
} else {
    Write-Host "  [WARNING] Backend service is not running" -ForegroundColor Yellow
    Write-Host "     Please start backend service manually" -ForegroundColor Cyan
}

Write-Host ""

# Step 3: Check training data
Write-Host "Step 3: Checking training data..." -ForegroundColor Yellow
try {
    $result = mysql -u root -proot -D video_platform -e "SELECT COUNT(*) FROM behavior_record;" 2>&1
    $count = ($result | Select-String "^\d+$" | Select-Object -First 1).ToString().Trim()
    
    if ($count -and [int]$count -ge 10) {
        Write-Host "  [OK] Sufficient behavior data ($count records)" -ForegroundColor Green
    } else {
        Write-Host "  [WARNING] Insufficient behavior data ($count records)" -ForegroundColor Yellow
        Write-Host "     Recommend at least 10 records" -ForegroundColor Cyan
    }
} catch {
    Write-Host "  [WARNING] Cannot check data" -ForegroundColor Yellow
}

Write-Host ""

# Step 4: Ask to run offline training
Write-Host "Step 4: Offline Recommendation Training" -ForegroundColor Yellow
$runOffline = Read-Host "Run offline recommendation training? (Y/n)"

if ($runOffline -ne "n" -and $runOffline -ne "N") {
    Write-Host "  Running offline training..." -ForegroundColor Cyan
    Write-Host ""
    
    & .\run-offline-training.ps1
    
    # Check if model was actually created (more reliable than exit code on Windows)
    if (Test-Path "data\models\als_model") {
        Write-Host ""
        Write-Host "  [OK] Offline training completed successfully" -ForegroundColor Green
        Write-Host "     Model saved to: data\models\als_model" -ForegroundColor Cyan
    } else {
        Write-Host ""
        Write-Host "  [ERROR] Offline training failed - model not found" -ForegroundColor Red
        Write-Host "     Please check logs above" -ForegroundColor Yellow
    }
} else {
    Write-Host "  Skipped offline training" -ForegroundColor Cyan
}

Write-Host ""

# Step 5: Ask to start realtime recommendation
Write-Host "Step 5: Realtime Recommendation Service" -ForegroundColor Yellow
$runRealtime = Read-Host "Start realtime recommendation service? (Y/n)"

if ($runRealtime -ne "n" -and $runRealtime -ne "N") {
    Write-Host "  Starting realtime recommendation service..." -ForegroundColor Cyan
    Write-Host ""
    Write-Host "  Note: Service will start in a new window" -ForegroundColor Yellow
    Write-Host "  Please keep that window open" -ForegroundColor Yellow
    Write-Host ""
    
    Start-Sleep -Seconds 2
    
    # Start realtime recommendation in new window
    Start-Process powershell -ArgumentList "-NoExit", "-Command", ".\start-realtime-recommender.ps1"
    
    Write-Host "  [OK] Realtime recommendation service started in new window" -ForegroundColor Green
} else {
    Write-Host "  Skipped realtime recommendation service" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "Startup Process Completed!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# Display service status
Write-Host "Current Service Status:" -ForegroundColor Cyan
Write-Host "  [OK] MySQL (3306)" -ForegroundColor Green
Write-Host "  [OK] Redis (6379)" -ForegroundColor Green
Write-Host "  [OK] Kafka (9092)" -ForegroundColor Green

if ($backendCheck) {
    Write-Host "  [OK] Backend Service (8080)" -ForegroundColor Green
} else {
    Write-Host "  [WARNING] Backend Service (8080) - Not running" -ForegroundColor Yellow
}

if ($runRealtime -ne "n" -and $runRealtime -ne "N") {
    Write-Host "  [OK] Realtime Recommendation Service - Starting" -ForegroundColor Green
}

Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "  1. Access frontend application" -ForegroundColor White
Write-Host "  2. Test recommendation features" -ForegroundColor White
Write-Host "  3. View recommendation results" -ForegroundColor White
Write-Host ""
