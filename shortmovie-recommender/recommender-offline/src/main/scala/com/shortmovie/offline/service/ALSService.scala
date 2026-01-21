package com.shortmovie.offline.service

import com.shortmovie.constant.RecConstants
import com.shortmovie.model.{ModelParamModel, UserBehavior}
import com.shortmovie.offline.config.OfflineConfig
import com.shortmovie.util.{MysqlUtil, ScoreUtil}
import org.apache.spark.ml.recommendation.ALS
import org.apache.spark.ml.recommendation.ALSModel
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window
import java.util.UUID
import java.text.SimpleDateFormat

/**
 * ALS离线推荐核心服务
 * 最终修复版：
 * 1. 解决RMSE计算的函数别名冲突问题
 * 2. 新增过滤用户已交互视频的核心去重逻辑
 */
class ALSService(spark: SparkSession) {
  import spark.implicits._

  // 时间格式化工具
  private val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  /**
   * 步骤1：加载并预处理用户行为数据
   * 适配数据库字段：user_id/video_id/behavior_type/play_duration/is_completed
   */
  def loadAndProcessBehaviorData(): DataFrame = {
    // 1. 从MySQL读取用户行为数据（使用数据库实际字段名）
    val behaviorDF = MysqlUtil.readUserBehavior(spark)
      .select(
        col("user_id").cast("long"),        // 数据库字段：user_id
        col("video_id").cast("long"),       // 数据库字段：video_id
        col("behavior_type"),               // 数据库字段：behavior_type
        col("play_duration").cast("int"),   // 数据库字段：play_duration
        col("is_completed").cast("int")     // 数据库字段：is_completed
      )

    // 2. 转换为UserBehavior实体类，计算评分
    val scoreDF = behaviorDF
      .map(row => {
        val behavior = UserBehavior(
          row.getAs[Long]("user_id"),
          row.getAs[Long]("video_id"),
          row.getAs[String]("behavior_type"),
          Option(row.getAs[Int]("play_duration")),
          Option(row.getAs[Int]("is_completed"))
        )
        val score = ScoreUtil.calculateScore(behavior)
        (behavior.userId, behavior.videoId, score)
      })
      .toDF("userId", "videoId", "score")
      .filter(col("score") > 0)  // 过滤0分行为

    scoreDF
  }

  /**
   * 新增：加载所有用户的已交互视频列表（用于去重）
   * 作用：查询每个用户所有有过行为（PLAY/LIKE/COLLECT/COMMENT）的视频ID
   */
  private def loadUserInteractedVideos(): DataFrame = {
    // 从behavior_record表读取所有用户的已交互视频，去重
    MysqlUtil.readUserBehavior(spark)
      .select(
        col("user_id").cast("long").alias("userId"),
        col("video_id").cast("long").alias("interactedVideoId")
      )
      .distinct() // 去重：同一用户+同一视频仅保留一条
  }

  /**
   * 步骤2：训练ALS模型
   * 修复RMSE计算逻辑：避免函数别名冲突
   */
  def trainALSModel(scoreDF: DataFrame): (ALSModel, ModelParamModel) = {
    // 1. 构建ALS模型
    val als = new ALS()
      .setUserCol("userId")
      .setItemCol("videoId")
      .setRatingCol("score")
      .setRank(OfflineConfig.ALS_RANK)
      .setRegParam(OfflineConfig.ALS_REG_PARAM)
      .setMaxIter(OfflineConfig.ALS_MAX_ITER)
      .setColdStartStrategy("drop")  // 冷启动策略：丢弃无推荐结果的用户

    // 2. 训练模型
    val model = als.fit(scoreDF)

    // 3. 评估模型（修复RMSE计算逻辑）
    val predictions = model.transform(scoreDF)
      // 计算误差平方，并给列起明确别名
      .withColumn("squared_error", pow(col("score") - col("prediction"), 2))

    // 计算平均误差平方（MSE），再开根号得到RMSE
    val mse = predictions.select(avg(col("squared_error"))).first().getDouble(0)
    val rmse = math.sqrt(mse)

    // 4. 构建模型参数（适配model_params表字段）
    val modelParam = ModelParamModel(
      modelId = UUID.randomUUID().toString,
      rank = OfflineConfig.ALS_RANK,
      regParam = OfflineConfig.ALS_REG_PARAM,
      maxIter = OfflineConfig.ALS_MAX_ITER,
      trainingTime = sdf.format(System.currentTimeMillis()),
      modelPath = OfflineConfig.MODEL_SAVE_PATH,
      rmse = rmse
    )

    // 5. 保存模型到本地（先删除已有路径，避免冲突）
    import java.io.File
    val modelDir = new File(OfflineConfig.MODEL_SAVE_PATH)
    if (modelDir.exists()) {
      org.apache.commons.io.FileUtils.deleteDirectory(modelDir)
    }
    model.save(OfflineConfig.MODEL_SAVE_PATH)

    (model, modelParam)
  }

  /**
   * 步骤3：生成离线推荐结果
   * 核心修改：新增过滤用户已交互视频的去重逻辑
   * 适配recommendation_result表字段：user_id/movie_id/score/rank/type/model_id/create_time
   */
  def generateRecommendations(model: ALSModel, modelParam: ModelParamModel): DataFrame = {
    // 1. 为每个用户生成更多候选推荐（从TOP_N改为TOP_N*2，避免过滤后数量不足）
    val recDF = model.recommendForAllUsers(OfflineConfig.TOP_N * 2) // 修改：候选数翻倍

    // 2. 解析原始推荐结果
    val rawRecDF = recDF
      .select(col("userId"), explode(col("recommendations")).alias("rec"))
      .select(
        col("userId"),
        col("rec.videoId").alias("videoId"),
        col("rec.rating").alias("score")
      )
      // 原有去重：按用户+视频分组，保留最高评分
      .groupBy("userId", "videoId")
      .agg(max("score").alias("score"))
      // 归一化评分（0-1）
      .withColumn("score", col("score") / 5.0)

    // 3. 新增核心逻辑：加载用户已交互视频列表
    val userInteractedDF = loadUserInteractedVideos()

    // 4. 新增核心逻辑：左连接后过滤已交互视频
    val deduplicatedRecDF = rawRecDF
      .join(
        userInteractedDF,
        rawRecDF("userId") === userInteractedDF("userId") && rawRecDF("videoId") === userInteractedDF("interactedVideoId"),
        "left_anti" // 左反连接：只保留未交互的视频
      )
      // 重新生成排名（过滤后重新排序）
      .withColumn("rank", row_number().over(Window.partitionBy("userId").orderBy(col("score").desc)))
      .filter(col("rank") <= OfflineConfig.TOP_N) // 保留TOP_N

    // 5. 转换为MySQL表结构（字段名完全匹配）
    val recommendationDF = deduplicatedRecDF
      .select(
        col("userId").cast("bigint").alias("user_id"),       // 对应user_id
        col("videoId").cast("bigint").alias("movie_id"),     // 对应movie_id
        col("score").cast("double").alias("score"),          // 对应score
        col("rank").cast("int").alias("rank"),               // 对应rank
        lit(RecConstants.REC_TYPE_OFFLINE).alias("type"),    // 对应type
        lit(modelParam.modelId).alias("model_id"),           // 对应model_id
        lit(modelParam.trainingTime).cast("timestamp").alias("create_time")  // 对应create_time
      )

    recommendationDF
  }

  /**
   * 步骤4：执行完整的离线推荐流程
   */
  def run(): Unit = {
    try {
      // 1. 加载并预处理数据
      val scoreDF = loadAndProcessBehaviorData()
      println("✅ 完成用户行为数据加载与预处理")

      // 2. 训练ALS模型
      val (model, modelParam) = trainALSModel(scoreDF)
      println(s"✅ ALS模型训练完成，RMSE：${modelParam.rmse}")

      // 3. 写入模型参数到MySQL
      MysqlUtil.writeModelParams(modelParam, spark)
      println("✅ 模型参数已写入MySQL（model_params表）")

      // 4. 生成并写入推荐结果
      val recDF = generateRecommendations(model, modelParam)
      MysqlUtil.writeRecResult(recDF)
      println("✅ 离线推荐结果已写入MySQL（recommendation_result表）")

      println("========== 离线推荐任务执行完成 ==========")
    } catch {
      case e: Exception =>
        println(s"❌ 离线推荐流程执行失败：${e.getMessage}")
        e.printStackTrace()
        throw e  // 抛出异常，让主程序感知失败
    }
  }
}

// 伴生对象：方便调用
object ALSService {
  def apply(spark: SparkSession): ALSService = new ALSService(spark)
}