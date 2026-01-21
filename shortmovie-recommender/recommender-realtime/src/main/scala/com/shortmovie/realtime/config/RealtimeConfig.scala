package com.shortmovie.realtime.config

/**
 * 实时推荐配置类
 */
object RealtimeConfig {
  // Kafka配置
  val KAFKA_BOOTSTRAP_SERVERS = "localhost:9092"
  val KAFKA_TOPIC = "short_video_behavior_topic"
  val KAFKA_GROUP_ID = "realtime_recommend_group"
  
  // Kafka消费者性能优化配置
  val KAFKA_MAX_POLL_RECORDS = "500"           // 单次拉取最大记录数
  val KAFKA_FETCH_MIN_BYTES = "1024"           // 最小拉取字节数（1KB）
  val KAFKA_FETCH_MAX_WAIT_MS = "500"          // 最大等待时间（毫秒）
  val KAFKA_SESSION_TIMEOUT_MS = "30000"       // 会话超时时间（30秒）
  val KAFKA_HEARTBEAT_INTERVAL_MS = "3000"     // 心跳间隔（3秒）
  val KAFKA_AUTO_OFFSET_RESET = "latest"       // 从最新位置开始消费

  // Redis配置
  val REDIS_HOST = "localhost"
  val REDIS_PORT = 6379
  val REDIS_PASSWORD = ""  // 无密码则为空
  val REDIS_DB = 0
  val REDIS_TIMEOUT = 5000  // 连接超时时间（毫秒）
  val REDIS_MAX_TOTAL = 32  // 最大连接数
  val REDIS_MAX_IDLE = 16   // 最大空闲连接数
  val REDIS_MIN_IDLE = 8    // 最小空闲连接数

  // 实时推荐参数
  val BATCH_DURATION = 5  // 批处理间隔（秒）
  val RECOMMEND_NUM = 10  // 实时推荐视频数量
  val MODEL_PATH = "data/models/als_model"  // 离线ALS模型路径
  
  // Spark Streaming性能优化配置
  val SPARK_EXECUTOR_MEMORY = "2g"
  val SPARK_DRIVER_MEMORY = "1g"
  val SPARK_EXECUTOR_CORES = "2"
  val SPARK_STREAMING_BACKPRESSURE_ENABLED = "true"  // 启用背压机制
  val SPARK_STREAMING_KAFKA_MAX_RATE_PER_PARTITION = "1000"  // 每个分区最大速率
}