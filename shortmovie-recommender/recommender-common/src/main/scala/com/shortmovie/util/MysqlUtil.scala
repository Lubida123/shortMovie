package com.shortmovie.util

import com.shortmovie.constant.RecConstants
import com.shortmovie.model.ModelParamModel
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._  // 关键：导入col等Spark SQL函数
import java.util.Properties

/**
 * MySQL操作工具类（适配公共模块）
 * 复用原逻辑：保留所有方法，仅调整表名引用为公共常量
 */
object MysqlUtil {
  // MySQL配置（复用你原有的配置，无需修改）
  private val mysqlUrl = "jdbc:mysql://localhost:3306/video_platform?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
  private val mysqlUser = "root"       // 替换为你的MySQL用户名
  private val mysqlPwd = "root"        // 替换为你的MySQL密码
  private val mysqlDriver = "com.mysql.cj.jdbc.Driver"

  private val props = new Properties()
  props.setProperty("user", mysqlUser)
  props.setProperty("password", mysqlPwd)
  props.setProperty("driver", mysqlDriver)

  // 读取用户行为数据（复用原方法，表名改为behavior_record）
  def readUserBehavior(spark: SparkSession): DataFrame = {
    spark.read
      .jdbc(mysqlUrl, "behavior_record", props)  // 原表名是behavior_record，保持不变
  }

  // 写入推荐结果到MySQL（复用原方法，适配公共常量）
  def writeRecResult(df: DataFrame): Unit = {
    df.write
      .mode("append")
      .jdbc(mysqlUrl, RecConstants.TABLE_REC_RESULT, props)
  }

  // 新增：直接写入模型参数DataFrame（复用原逻辑）
  def writeModelParamsDF(df: DataFrame): Unit = {
    df.write
      .mode("append")
      .jdbc(mysqlUrl, RecConstants.TABLE_MODEL_PARAMS, props)
  }

  // 兼容方法：处理ModelParamModel对象（复用原逻辑）
  def writeModelParams(modelParam: ModelParamModel, spark: SparkSession): Unit = {
    import spark.implicits._
    val df = Seq(modelParam).toDF()
      .select(
        col("modelId").alias("model_id"),
        col("rank"),
        col("regParam").alias("reg_param"),
        col("maxIter").alias("max_iter"),
        col("trainingTime").alias("training_time"),
        col("modelPath").alias("model_path"),
        col("rmse")  // 修正拼写错误：rmse（不是rms/rmss）
      )
    writeModelParamsDF(df)
  }

  // 兼容原方法名（避免离线代码迁移后报错）
  def readBehaviorData(spark: SparkSession): DataFrame = readUserBehavior(spark)
  def writeRecommendationResult(df: DataFrame): Unit = writeRecResult(df)
}