package com.shortmovie.model

/**
 * 用户行为数据模型（对应behavior_record表）
 * 适配公共模块：继承原BehaviorModel逻辑，仅改类名+包名
 * @param userId 用户ID
 * @param videoId 视频ID
 * @param behaviorType 行为类型（PLAY/LIKE/COMMENT/COLLECT）
 * @param playDuration 播放时长（秒）
 * @param isCompleted 是否完播（0-否，1-是）
 */
case class UserBehavior(
                         userId: Long,
                         videoId: Long,
                         behaviorType: String,
                         playDuration: Option[Int],  // 可选字段（允许为null）
                         isCompleted: Option[Int]    // 可选字段
                       )