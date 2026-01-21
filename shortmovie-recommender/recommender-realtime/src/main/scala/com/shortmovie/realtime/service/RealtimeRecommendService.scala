package com.shortmovie.realtime.service

import com.alibaba.fastjson.JSON
import com.shortmovie.realtime.config.RealtimeConfig
import com.shortmovie.realtime.entity.UserBehavior
import com.shortmovie.realtime.util.{KafkaUtil, RedisUtil}
import org.apache.spark.ml.recommendation.ALSModel
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

/**
 * 实时推荐核心服务
 * 【Checkpoint 修复版 + 去重版】
 * 1. 使用伴生对象持有模型和静态方法，彻底切断 foreachRDD 对类实例(this)的依赖。
 * 2. 新增实时推荐去重逻辑：过滤用户已交互的视频。
 */
class RealtimeRecommendService(ssc: StreamingContext) {

  /**
   * 核心处理逻辑
   */
  def processKafkaMessage(): Unit = {
    val kafkaDStream = KafkaUtil.createKafkaDStream(ssc)

    // 1. 数据转换
    val behaviorDStream: DStream[UserBehavior] = kafkaDStream
      .map(record => {
        try {
          JSON.parseObject(record.value(), classOf[UserBehavior])
        } catch {
          case _: Exception => null
        }
      })
      // 使用伴生对象常量，不引用 this
      .filter(x => x != null && RealtimeRecommendService.behaviorScoreMap.contains(x.behaviorType))

    // 2. 微批处理 (Driver 端调度)
    behaviorDStream.foreachRDD { rdd =>
      if (!rdd.isEmpty()) {

        // 这一步必须在 Driver 端执行，获取 SparkSession
        val spark = SparkSession.builder.config(rdd.context.getConf).getOrCreate()
        import spark.implicits._

        rdd.cache()

        /* * 任务 A：写入用户评分到 Redis (Executor 端) */
        rdd.foreachPartition { partitionIter =>
          val jedis = RedisUtil.getJedis()
          try {
            partitionIter.foreach { behavior =>
              // 引用静态对象
              val score = RealtimeRecommendService.behaviorScoreMap.getOrElse(behavior.behaviorType, 0.0)
              RedisUtil.cacheUserScore(behavior.userId, behavior.videoId, score)
              // 新增：同步更新用户已交互视频列表到Redis（方便后续去重）
              RedisUtil.addUserInteractedVideo(behavior.userId, behavior.videoId)
            }
          } finally {
            jedis.close()
          }
        }

        /* * 任务 B：实时推荐 (Driver 端计算 -> Executor 端写入) */
        try {
          // 准备用户数据
          val distinctUsersDF = rdd.map(_.userId.toInt).distinct().toDF("userId")

          // 【核心修改】调用静态方法获取模型
          val model = RealtimeRecommendService.getModel(RealtimeConfig.MODEL_PATH)

          if (model != null) {
            // 核心修改1：生成双倍候选推荐数（过滤后仍能保证RECOMMEND_NUM）
            val recommendationsDF = model.recommendForUserSubset(distinctUsersDF, RealtimeConfig.RECOMMEND_NUM * 2)

            // 处理推荐结果（新增去重逻辑）
            recommendationsDF.foreachPartition { (partitionIter: Iterator[Row]) =>
              val jedis = RedisUtil.getJedis()
              try {
                partitionIter.foreach { row =>
                  val userId = row.getAs[Int]("userId").toLong
                  val recs = row.getAs[Seq[Row]]("recommendations")
                  // 原始推荐视频ID列表
                  val rawVideoIds = recs.map(r => r.getAs[Int]("videoId").toLong).toList

                  // 核心修改2：获取用户已交互视频列表（从Redis读取）
                  val interactedVideoIds = RedisUtil.getUserInteractedVideos(jedis, userId)

                  // 核心修改3：过滤已交互视频，保留未交互的
                  val filteredVideoIds = rawVideoIds.filterNot(interactedVideoIds.contains)

                  // 核心修改4：截取前RECOMMEND_NUM个，保证推荐数量
                  val finalVideoIds = filteredVideoIds.take(RealtimeConfig.RECOMMEND_NUM)

                  // 缓存去重后的推荐结果
                  RedisUtil.cacheRecommendResult(userId, finalVideoIds)
                }
              } catch {
                case e: Exception => e.printStackTrace()
              } finally {
                jedis.close()
              }
            }
            println(s"✅ [Realtime] 批次处理完成，更新了 ${recommendationsDF.count()} 位用户的推荐（已去重）。")
          }
        } catch {
          case e: Exception =>
            println(s"⚠️ 推荐计算失败: ${e.getMessage}")
            e.printStackTrace()
        }

        rdd.unpersist()
      }
    }
  }
}

/**
 * 伴生对象：单例容器
 * 存放所有需要跨节点传输或在闭包中使用的静态资源
 */
object RealtimeRecommendService {

  // 1. 静态评分表
  val behaviorScoreMap = Map(
    "PLAY" -> 3.0,
    "LIKE" -> 5.0,
    "COLLECT" -> 4.0,
    "COMMENT" -> 3.5
  )

  // 2. 静态模型变量 (使用 @transient 标记，不参与序列化)
  @transient private var alsModel: ALSModel = _

  // 3. 静态获取模型方法 (懒加载模式)
  def getModel(path: String): ALSModel = {
    if (alsModel == null) {
      // 双重检查锁 (虽在 Driver 端主要是单线程调度，但保持严谨)
      synchronized {
        if (alsModel == null) {
          try {
            println(s"🔄 正在加载 ALS 模型：$path ...")
            // ALSModel.load 会自动使用当前上下文中的 SparkSession
            alsModel = ALSModel.load(path)
            println("✅ 模型加载成功")
          } catch {
            case e: Exception =>
              println(s"❌ 模型加载失败: ${e.getMessage}")
              e.printStackTrace()
              null
          }
        }
      }
    }
    alsModel
  }

  def apply(ssc: StreamingContext): RealtimeRecommendService = {
    new RealtimeRecommendService(ssc)
  }
}