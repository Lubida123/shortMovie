package com.shortmovie.constant

/**
 * Redis缓存常量（实时推荐核心）
 */
object CacheConstants {
  // Redis Key前缀（格式：前缀 + ID）
  val REDIS_KEY_VIDEO_RELATED = "video_related:"        // 视频关联关系：video_related:1001 → Map(1002:0.9)
  val REDIS_KEY_USER_RECENT_BEHAVIOR = "user_recent_behavior:"  // 用户最近行为：user_recent_behavior:2302 → List(1001,1002)
  val REDIS_KEY_USER_REALTIME_REC = "user_realtime_rec:"        // 实时推荐结果缓存：user_realtime_rec:2302 → JSON字符串

  // Redis过期时间（秒）
  val REDIS_EXPIRE_REALTIME_REC = 3600    // 实时推荐结果缓存1小时
  val REDIS_EXPIRE_USER_BEHAVIOR = 1800  // 用户最近行为缓存30分钟
}