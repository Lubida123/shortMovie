# Run Offline Recommendation Training
# This script trains the ALS model and generates recommendations

Write-Host "=== Offline Recommendation Training ===" -ForegroundColor Cyan
Write-Host ""

# Check if JAR files exist
$offlineJar = "shortmovie-recommender\recommender-offline\target\recommender-offline-1.0-SNAPSHOT.jar"
$commonJar = "shortmovie-recommender\recommender-common\target\recommender-common-1.0-SNAPSHOT.jar"

if (-not (Test-Path $offlineJar)) {
    Write-Host "[ERROR] Offline recommendation JAR file not found" -ForegroundColor Red
    Write-Host "Please compile the project first:" -ForegroundColor Yellow
    Write-Host "  cd shortmovie-recommender" -ForegroundColor Green
    Write-Host "  mvn clean package -pl recommender-offline -am -DskipTests" -ForegroundColor Green
    exit 1
}

if (-not (Test-Path $commonJar)) {
    Write-Host "[ERROR] Common module JAR file not found" -ForegroundColor Red
    Write-Host "Please compile the project first:" -ForegroundColor Yellow
    Write-Host "  cd shortmovie-recommender" -ForegroundColor Green
    Write-Host "  mvn clean package -pl recommender-offline -am -DskipTests" -ForegroundColor Green
    exit 1
}

Write-Host "[OK] Found offline recommendation JAR file" -ForegroundColor Green
Write-Host "[OK] Found common module JAR file" -ForegroundColor Green
Write-Host ""

# Check behavior_record data
Write-Host "Checking training data..." -ForegroundColor Yellow
$recordCount = mysql -u root -proot -D video_platform -e "SELECT COUNT(*) as total FROM behavior_record;" 2>&1 | Select-String "total" | ForEach-Object { $_ -replace '.*total.*\|', '' -replace '\|', '' -replace ' ', '' }

if ($recordCount -eq "0" -or $recordCount -eq $null -or $recordCount -eq "") {
    Write-Host "[ERROR] behavior_record table is empty, cannot train" -ForegroundColor Red
    Write-Host "Please run first:" -ForegroundColor Yellow
    Write-Host "  Get-Content insert-test-behavior-data.sql | mysql -u root -proot -D video_platform" -ForegroundColor Green
    exit 1
}

Write-Host "[OK] Found $recordCount training records" -ForegroundColor Green
Write-Host ""

# Run offline training
Write-Host "Starting offline recommendation training..." -ForegroundColor Yellow
Write-Host "This may take a few minutes, please wait..." -ForegroundColor Cyan
Write-Host ""

try {
    # Use spark-submit to run
    $sparkSubmit = "spark-submit"
    
    # Check if spark-submit is available
    $sparkCheck = Get-Command spark-submit -ErrorAction SilentlyContinue
    if (-not $sparkCheck) {
        Write-Host "[ERROR] spark-submit command not found" -ForegroundColor Red
        Write-Host "Please ensure Spark is installed and added to PATH environment variable" -ForegroundColor Yellow
        exit 1
    }
    
    # Run training with common JAR included
    & spark-submit `
        --class com.shortmovie.offline.Main `
        --master "local[*]" `
        --driver-memory 2g `
        --executor-memory 2g `
        --conf "spark.sql.shuffle.partitions=10" `
        --jars $commonJar `
        $offlineJar
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "=== Training Complete ===" -ForegroundColor Green
        Write-Host ""
        
        # Verify results
        Write-Host "Verifying training results..." -ForegroundColor Yellow
        
        # Check recommendation_result table
        Write-Host ""
        Write-Host "1. recommendation_result table:" -ForegroundColor Cyan
        mysql -u root -proot -D video_platform -e "SELECT COUNT(*) as total_recommendations FROM recommendation_result;" 2>&1 | Select-String "total"
        
        # Check model_params table
        Write-Host ""
        Write-Host "2. model_params table:" -ForegroundColor Cyan
        mysql -u root -proot -D video_platform -e "SELECT model_id, rank, rmse, training_time FROM model_params ORDER BY training_time DESC LIMIT 1;" 2>&1 | Select-String "\|"
        
        # Check model files
        Write-Host ""
        Write-Host "3. Model files:" -ForegroundColor Cyan
        if (Test-Path "data\models") {
            $modelDirs = Get-ChildItem "data\models" -Directory | Select-Object -First 3
            if ($modelDirs) {
                Write-Host "[OK] Model saved to data\models\" -ForegroundColor Green
                $modelDirs | ForEach-Object { Write-Host "  - $($_.Name)" }
            } else {
                Write-Host "[WARN] data\models directory is empty" -ForegroundColor Yellow
            }
        } else {
            Write-Host "[WARN] data\models directory not found" -ForegroundColor Yellow
        }
        
        Write-Host ""
        Write-Host "=== Next Steps ===" -ForegroundColor Cyan
        Write-Host "1. Start realtime recommendation service: .\start-realtime-recommender.ps1" -ForegroundColor Green
        Write-Host "2. Test recommendation API" -ForegroundColor Green
        
    } else {
        Write-Host ""
        Write-Host "[ERROR] Training failed, exit code: $LASTEXITCODE" -ForegroundColor Red
        Write-Host "Please check the error logs above" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host ""
    Write-Host "[ERROR] Execution failed: $($_.Exception.Message)" -ForegroundColor Red
}
