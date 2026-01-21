package com.shortmovie.realtime.util

import com.shortmovie.realtime.config.RealtimeConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{StreamingContext, Seconds}
import org.apache.spark.SparkConf

/**
 * Kafka工具类：创建DStream
 */
object KafkaUtil {
  // 创建Kafka参数（添加性能优化配置）
  private val kafkaParams = Map[String, String](
    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> RealtimeConfig.KAFKA_BOOTSTRAP_SERVERS,
    ConsumerConfig.GROUP_ID_CONFIG -> RealtimeConfig.KAFKA_GROUP_ID,
    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> RealtimeConfig.KAFKA_AUTO_OFFSET_RESET,
    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer",
    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer",
    ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> "true",  // 自动提交偏移量
    // 性能优化配置
    ConsumerConfig.MAX_POLL_RECORDS_CONFIG -> RealtimeConfig.KAFKA_MAX_POLL_RECORDS,
    ConsumerConfig.FETCH_MIN_BYTES_CONFIG -> RealtimeConfig.KAFKA_FETCH_MIN_BYTES,
    ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG -> RealtimeConfig.KAFKA_FETCH_MAX_WAIT_MS,
    ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG -> RealtimeConfig.KAFKA_SESSION_TIMEOUT_MS,
    ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG -> RealtimeConfig.KAFKA_HEARTBEAT_INTERVAL_MS
  )

  // 创建Spark Streaming上下文（添加性能优化配置）
  def createStreamingContext(): StreamingContext = {
    val sparkConf = new SparkConf()
      .setAppName("ShortMovieRealtimeRecommend")
      .setMaster("local[2]")  // 本地运行，至少2个核心
      .set("spark.executor.memory", RealtimeConfig.SPARK_EXECUTOR_MEMORY)
      .set("spark.driver.memory", RealtimeConfig.SPARK_DRIVER_MEMORY)
      .set("spark.executor.cores", RealtimeConfig.SPARK_EXECUTOR_CORES)
    new StreamingContext(sparkConf, Seconds(RealtimeConfig.BATCH_DURATION))
  }

  // 创建Kafka DStream
  def createKafkaDStream(ssc: StreamingContext) = {
    KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](List(RealtimeConfig.KAFKA_TOPIC), kafkaParams)
    )
  }
}