package com.shortmovie.realtime.util

import com.shortmovie.realtime.config.RealtimeConfig
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}
import scala.collection.JavaConverters._

/**
 * Redis工具类（最终无标红版）
 */
object RedisUtil {

  // 1. 初始化连接池配置（优化性能）
  private val poolConfig = new JedisPoolConfig()
  poolConfig.setMaxTotal(RealtimeConfig.REDIS_MAX_TOTAL)
  poolConfig.setMaxIdle(RealtimeConfig.REDIS_MAX_IDLE)
  poolConfig.setMinIdle(RealtimeConfig.REDIS_MIN_IDLE)
  poolConfig.setTestOnBorrow(true)  // 获取连接时测试可用性
  poolConfig.setTestOnReturn(false)
  poolConfig.setTestWhileIdle(true)  // 空闲时测试连接
  poolConfig.setMinEvictableIdleTimeMillis(60000)  // 最小空闲时间60秒
  poolConfig.setTimeBetweenEvictionRunsMillis(30000)  // 每30秒检查一次空闲连接
  poolConfig.setNumTestsPerEvictionRun(-1)  // 每次检查所有空闲连接

  // 2. 处理密码逻辑
  private val redisPassword = if (RealtimeConfig.REDIS_PASSWORD != null && RealtimeConfig.REDIS_PASSWORD.trim.nonEmpty) {
    RealtimeConfig.REDIS_PASSWORD
  } else {
    null
  }

  // 3. 创建连接池（使用优化的超时配置）
  private lazy val jedisPool = new JedisPool(
    poolConfig,
    RealtimeConfig.REDIS_HOST,
    RealtimeConfig.REDIS_PORT,
    RealtimeConfig.REDIS_TIMEOUT,  // 使用配置的超时时间
    redisPassword,
    RealtimeConfig.REDIS_DB
  )

  // 获取 Jedis 连接
  def getJedis(): Jedis = {
    jedisPool.getResource
  }

  // 关闭连接池
  def closePool(): Unit = {
    if (jedisPool != null && !jedisPool.isClosed) {
      jedisPool.close()
    }
  }

  /**
   * 缓存推荐结果到 Redis
   */
  def cacheRecommendResult(userId: Long, recommendList: List[Long]): Unit = {
    val jedis = getJedis()
    try {
      val key = s"recommend:user:$userId"
      jedis.del(key)
      if (recommendList.nonEmpty) {
        recommendList.foreach(videoId => jedis.rpush(key, videoId.toString))
      }
      jedis.expire(key, 3600L)
    } catch {
      case e: Exception => println(s"缓存推荐结果失败：${e.getMessage}")
    } finally {
      jedis.close()
    }
  }

  /**
   * 缓存用户行为评分到 Redis
   */
  def cacheUserScore(userId: Long, videoId: Long, score: Double): Unit = {
    val jedis = getJedis()
    try {
      val key = s"user:score:$userId"
      jedis.hset(key, videoId.toString, score.toString)
    } catch {
      case e: Exception => println(s"缓存用户评分失败：${e.getMessage}")
    } finally {
      jedis.close()
    }
  }

  /**
   * 将用户交互的视频ID加入Redis集合
   */
  def addUserInteractedVideo(userId: Long, videoId: Long): Unit = {
    val jedis = getJedis()
    try {
      val key = s"user:interacted:$userId"
      jedis.sadd(key, videoId.toString)
      jedis.expire(key, 86400 * 7L)
    } catch {
      case e: Exception => println(s"新增用户已交互视频失败：userId=$userId, videoId=$videoId, err=${e.getMessage}")
    } finally {
      jedis.close()
    }
  }

  /**
   * 从Redis获取用户已交互的视频ID列表
   */
  def getUserInteractedVideos(jedis: Jedis, userId: Long): Set[Long] = {
    try {
      val key = s"user:interacted:$userId"
      jedis.smembers(key).asScala.map(_.toLong).toSet
    } catch {
      case e: Exception =>
        println(s"获取用户已交互视频失败：userId=$userId, err=${e.getMessage}")
        Set.empty[Long]
    }
  }

  /**
   * 批量同步历史用户交互视频到Redis（彻底修复sadd标红）
   */
  def batchSyncHistoryInteractedVideos(historyBehaviorDF: DataFrame): Unit = {
    import org.apache.spark.sql.Row
    val userVideoGroupDF = historyBehaviorDF
      .select(
        col("user_id").cast("long").alias("userId"),
        col("video_id").cast("long").alias("videoId")
      )
      .distinct()
      .groupBy("userId")
      .agg(collect_list("videoId").alias("videoIds"))

    userVideoGroupDF.foreachPartition { partitionIter: Iterator[Row] =>
      val jedis = getJedis()
      try {
        partitionIter.foreach { row: Row =>
          val userId = row.getAs[Long]("userId")
          val videoIds: Seq[Long] = row.getAs[Seq[Long]]("videoIds")
          val key = s"user:interacted:$userId"

          // 关键修复：转换为Java的String数组，匹配Jedis.sadd的参数类型
          val videoIdStrs: Array[String] = videoIds.map(_.toString).toArray
          val javaStrs: Array[String] = scala.collection.JavaConverters.seqAsJavaListConverter(videoIdStrs).asJava.toArray(new Array[String](0))

          if (javaStrs.nonEmpty) {
            jedis.sadd(key, javaStrs: _*)
            jedis.expire(key, 86400 * 7L)
          }
        }
      } catch {
        case e: Exception => println(s"批量同步历史交互视频失败：${e.getMessage}")
      } finally {
        jedis.close()
      }
    }
    println("✅ 历史用户交互视频已同步到Redis")
  }
}