package com.shortmovie.realtime

import com.shortmovie.realtime.config.RealtimeConfig
import com.shortmovie.realtime.service.RealtimeRecommendService
import com.shortmovie.realtime.util.{KafkaUtil, RedisUtil}
import com.shortmovie.util.MysqlUtil
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.StreamingContext

/**
 * 实时推荐应用启动类
 * 新增：启动时同步历史交互视频到Redis
 * 优化：添加性能配置
 */
object RealtimeRecommendApp {
  def main(args: Array[String]): Unit = {
    // 1. 创建StreamingContext（添加性能优化配置）
    val ssc = KafkaUtil.createStreamingContext()
    ssc.checkpoint("data/checkpoint")
    
    // 添加Spark Streaming性能优化配置
    ssc.sparkContext.getConf
      .set("spark.streaming.backpressure.enabled", RealtimeConfig.SPARK_STREAMING_BACKPRESSURE_ENABLED)
      .set("spark.streaming.kafka.maxRatePerPartition", RealtimeConfig.SPARK_STREAMING_KAFKA_MAX_RATE_PER_PARTITION)
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

    // 2. 核心新增：同步历史交互视频到Redis
    val spark = SparkSession.builder.config(ssc.sparkContext.getConf).getOrCreate()
    try {
      println("🔄 开始同步历史用户交互视频到Redis...")
      // 读取MySQL中behavior_record表的所有历史行为数据
      val historyBehaviorDF = MysqlUtil.readUserBehavior(spark)
      // 批量同步到Redis
      RedisUtil.batchSyncHistoryInteractedVideos(historyBehaviorDF)
    } catch {
      case e: Exception =>
        println(s"⚠️ 历史交互视频同步失败：${e.getMessage}")
        e.printStackTrace()
    }

    try {
      // 3. 创建实时推荐服务
      val recommendService = RealtimeRecommendService(ssc)

      // 4. 处理Kafka消息，生成实时推荐
      recommendService.processKafkaMessage()

      // 5. 启动服务
      ssc.start()
      println("🚀 实时推荐服务已启动...")

      // 6. 等待服务停止
      ssc.awaitTermination()
    } catch {
      case e: Exception =>
        println(s"❌ 实时推荐服务启动失败：${e.getMessage}")
        e.printStackTrace()
        ssc.stop(stopSparkContext = true, stopGracefully = true)
    }
  }
}