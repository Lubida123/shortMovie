package com.shortmovie.constant

/**
 * 推荐系统公共常量（离线/实时共用）
 * 复用你之前离线推荐的表名、推荐类型等配置
 */
object RecConstants {
  // ========== 复用离线推荐的配置 ==========
  // 推荐类型（离线/实时）
  val REC_TYPE_OFFLINE = "OFFLINE"
  val REC_TYPE_REALTIME = "REAL_TIME"

  // 推荐数量（离线Top20，实时Top5）
  val OFFLINE_TOP_N = 20
  val REALTIME_TOP_N = 5

  // MySQL表名（和你离线推荐的表一致）
  val TABLE_USER_BEHAVIOR = "user_behavior"       // 用户行为表
  val TABLE_REC_RESULT = "recommendation_result"  // 推荐结果表
  val TABLE_MODEL_PARAMS = "model_params"         // 模型参数表
}