# 运行离线推荐训练
# 这个脚本会训练ALS模型并生成推荐结果

Write-Host "=== 离线推荐训练 ===" -ForegroundColor Cyan
Write-Host ""

# 检查JAR文件是否存在
$jarPath = "shortmovie-recommender\recommender-offline\target\recommender-offline-1.0-SNAPSHOT.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "❌ 找不到离线推荐JAR文件" -ForegroundColor Red
    Write-Host "请先编译项目：" -ForegroundColor Yellow
    Write-Host "  cd shortmovie-recommender" -ForegroundColor Green
    Write-Host "  mvn clean package -pl recommender-offline -am -DskipTests" -ForegroundColor Green
    exit 1
}

Write-Host "✅ 找到离线推荐JAR文件" -ForegroundColor Green
Write-Host ""

# 检查behavior_record数据
Write-Host "检查训练数据..." -ForegroundColor Yellow
$recordCount = mysql -u root -proot -D video_platform -e "SELECT COUNT(*) as total FROM behavior_record;" 2>&1 | Select-String "^\|.*\|$" | Select-Object -Skip 1 | ForEach-Object { $_.ToString().Trim() -replace '\|', '' -replace ' ', '' }

if ($recordCount -eq "0" -or $recordCount -eq $null) {
    Write-Host "❌ behavior_record表为空，无法训练" -ForegroundColor Red
    Write-Host "请先运行：" -ForegroundColor Yellow
    Write-Host "  Get-Content insert-test-behavior-data.sql | mysql -u root -proot -D video_platform" -ForegroundColor Green
    exit 1
}

Write-Host "✅ 找到 $recordCount 条训练数据" -ForegroundColor Green
Write-Host ""

# 运行离线训练
Write-Host "开始离线推荐训练..." -ForegroundColor Yellow
Write-Host "这可能需要几分钟时间，请耐心等待..." -ForegroundColor Cyan
Write-Host ""

try {
    # 使用spark-submit运行
    $sparkSubmit = "spark-submit"
    
    # 检查spark-submit是否可用
    $sparkCheck = Get-Command spark-submit -ErrorAction SilentlyContinue
    if (-not $sparkCheck) {
        Write-Host "❌ 找不到spark-submit命令" -ForegroundColor Red
        Write-Host "请确保Spark已安装并添加到PATH环境变量" -ForegroundColor Yellow
        exit 1
    }
    
    # 运行训练
    & spark-submit `
        --class com.shortmovie.offline.Main `
        --master "local[*]" `
        --driver-memory 2g `
        --executor-memory 2g `
        --conf "spark.sql.shuffle.partitions=10" `
        $jarPath
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "=== 训练完成 ===" -ForegroundColor Green
        Write-Host ""
        
        # 验证结果
        Write-Host "验证训练结果..." -ForegroundColor Yellow
        
        # 检查recommendation_result表
        Write-Host ""
        Write-Host "1. recommendation_result 表：" -ForegroundColor Cyan
        mysql -u root -proot -D video_platform -e "SELECT COUNT(*) as total_recommendations FROM recommendation_result;" 2>&1 | Select-String "^\|"
        
        # 检查model_params表
        Write-Host ""
        Write-Host "2. model_params 表：" -ForegroundColor Cyan
        mysql -u root -proot -D video_platform -e "SELECT model_id, rank, rmse, training_time FROM model_params ORDER BY training_time DESC LIMIT 1;" 2>&1 | Select-String "^\|"
        
        # 检查模型文件
        Write-Host ""
        Write-Host "3. 模型文件：" -ForegroundColor Cyan
        if (Test-Path "data\models") {
            $modelDirs = Get-ChildItem "data\models" -Directory | Select-Object -First 3
            if ($modelDirs) {
                Write-Host "✅ 模型已保存到 data\models\" -ForegroundColor Green
                $modelDirs | ForEach-Object { Write-Host "  - $($_.Name)" }
            } else {
                Write-Host "⚠️ data\models 目录为空" -ForegroundColor Yellow
            }
        } else {
            Write-Host "⚠️ 找不到 data\models 目录" -ForegroundColor Yellow
        }
        
        Write-Host ""
        Write-Host "=== 下一步 ===" -ForegroundColor Cyan
        Write-Host "1. 启动实时推荐服务：.\start-realtime-recommender.ps1" -ForegroundColor Green
        Write-Host "2. 测试推荐接口" -ForegroundColor Green
        
    } else {
        Write-Host ""
        Write-Host "❌ 训练失败，退出码：$LASTEXITCODE" -ForegroundColor Red
        Write-Host "请查看上面的错误日志" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host ""
    Write-Host "❌ 执行失败：$($_.Exception.Message)" -ForegroundColor Red
}
