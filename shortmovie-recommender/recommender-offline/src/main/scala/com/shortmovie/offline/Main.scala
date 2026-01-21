package com.shortmovie.offline

import com.shortmovie.offline.config.OfflineConfig
import com.shortmovie.offline.service.ALSService
import org.apache.spark.sql.SparkSession

/**
 * 离线推荐程序入口
 * 复用你原有的离线推荐启动逻辑
 */
object Main {
  def main(args: Array[String]): Unit = {
    // 1. 创建SparkSession（添加性能优化配置）
    val spark = SparkSession.builder()
      .appName("ShortMovieOfflineRecommender")
      .master("local[*]")  // 本地测试用，生产环境改为yarn
      .config("spark.sql.warehouse.dir", "spark-warehouse")
      // 性能优化配置
      .config("spark.executor.memory", OfflineConfig.SPARK_EXECUTOR_MEMORY)
      .config("spark.driver.memory", OfflineConfig.SPARK_DRIVER_MEMORY)
      .config("spark.executor.cores", OfflineConfig.SPARK_EXECUTOR_CORES)
      .config("spark.default.parallelism", OfflineConfig.SPARK_DEFAULT_PARALLELISM)
      .config("spark.sql.shuffle.partitions", OfflineConfig.SPARK_SQL_SHUFFLE_PARTITIONS)
      .config("spark.memory.fraction", OfflineConfig.SPARK_MEMORY_FRACTION)
      .config("spark.memory.storageFraction", OfflineConfig.SPARK_MEMORY_STORAGE_FRACTION)
      // 序列化优化
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.kryo.registrationRequired", "false")
      .getOrCreate()

    // 关闭日志冗余输出
    spark.sparkContext.setLogLevel("WARN")

    try {
      // 2. 执行离线推荐流程
      val alsService = ALSService(spark)
      alsService.run()
    } catch {
      case e: Exception =>
        println(s"❌ 离线推荐任务执行失败：${e.getMessage}")
        e.printStackTrace()
    } finally {
      // 3. 关闭SparkSession
      spark.stop()
    }
  }
}