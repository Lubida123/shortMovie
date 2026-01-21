# Start Realtime Recommendation Service
# This script compiles and starts the realtime recommendation service

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Starting Realtime Recommendation Service" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Check prerequisites
Write-Host "Checking prerequisites..." -ForegroundColor Yellow

# Check if Kafka is running
Write-Host "1. Checking Kafka..." -ForegroundColor Yellow
$kafkaCheck = netstat -an | Select-String "9092.*LISTENING"
if ($kafkaCheck) {
    Write-Host "   [OK] Kafka is running on port 9092" -ForegroundColor Green
} else {
    Write-Host "   [ERROR] Kafka is not running!" -ForegroundColor Red
    Write-Host "   Please start Kafka first using: .\kafka-setup.bat" -ForegroundColor Yellow
    exit 1
}

# Check if Redis is running
Write-Host "2. Checking Redis..." -ForegroundColor Yellow
$redisCheck = netstat -an | Select-String "6379.*LISTENING"
if ($redisCheck) {
    Write-Host "   [OK] Redis is running on port 6379" -ForegroundColor Green
} else {
    Write-Host "   [ERROR] Redis is not running!" -ForegroundColor Red
    Write-Host "   Please start Redis first" -ForegroundColor Yellow
    exit 1
}

# Check if MySQL is running
Write-Host "3. Checking MySQL..." -ForegroundColor Yellow
$mysqlCheck = netstat -an | Select-String "3306.*LISTENING"
if ($mysqlCheck) {
    Write-Host "   [OK] MySQL is running on port 3306" -ForegroundColor Green
} else {
    Write-Host "   [ERROR] MySQL is not running!" -ForegroundColor Red
    Write-Host "   Please start MySQL first" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "All prerequisites met!" -ForegroundColor Green
Write-Host ""

# Compile the project
Write-Host "Compiling realtime recommendation module..." -ForegroundColor Yellow
Set-Location shortmovie-recommender

# Use maven-assembly-plugin to create fat JAR with all dependencies
$compileOutput = mvn clean package -pl recommender-realtime -am -DskipTests 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] Compilation successful" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Compilation failed!" -ForegroundColor Red
    Write-Host $compileOutput
    Set-Location ..
    exit 1
}

Set-Location ..
Write-Host ""

# Find the JAR file (look for jar-with-dependencies first)
$jarWithDeps = "shortmovie-recommender/recommender-realtime/target/recommender-realtime-1.0-SNAPSHOT-jar-with-dependencies.jar"
$regularJar = "shortmovie-recommender/recommender-realtime/target/recommender-realtime-1.0-SNAPSHOT.jar"
$commonJar = "shortmovie-recommender/recommender-common/target/recommender-common-1.0-SNAPSHOT.jar"

if (Test-Path $jarWithDeps) {
    $jarPath = $jarWithDeps
    Write-Host "[OK] Fat JAR file found: $jarPath" -ForegroundColor Green
} elseif ((Test-Path $regularJar) -and (Test-Path $commonJar)) {
    $jarPath = $regularJar
    Write-Host "[OK] JAR files found, will use --jars option" -ForegroundColor Green
} else {
    Write-Host "[ERROR] JAR file not found!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Starting Realtime Recommendation Service..." -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "The service will:" -ForegroundColor Yellow
Write-Host "1. Sync historical user interactions to Redis" -ForegroundColor White
Write-Host "2. Consume user behavior messages from Kafka" -ForegroundColor White
Write-Host "3. Generate realtime recommendations" -ForegroundColor White
Write-Host "4. Cache recommendations to Redis" -ForegroundColor White
Write-Host ""
Write-Host "Press Ctrl+C to stop the service" -ForegroundColor Yellow
Write-Host ""

# 关键修改：切换到shortmovie目录，确保相对路径正确
Set-Location shortmovie

# Start the service
if (Test-Path "../$jarWithDeps") {
    # Use fat JAR (includes all dependencies)
    spark-submit `
        --class com.shortmovie.realtime.RealtimeRecommendApp `
        --master local[2] `
        --executor-memory 2g `
        --driver-memory 1g `
        "../$jarPath"
} else {
    # Use regular JAR with --jars option to include dependencies
    spark-submit `
        --class com.shortmovie.realtime.RealtimeRecommendApp `
        --master local[2] `
        --executor-memory 2g `
        --driver-memory 1g `
        --jars "../$commonJar" `
        "../$jarPath"
}
