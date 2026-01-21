package com.shortmovie.realtime.entity

/**
 * 用户行为实体类（对应Kafka消息）
 * @param userId 用户ID
 * @param videoId 视频ID
 * @param behaviorType 行为类型：PLAY/LIKE/COLLECT/COMMENT
 * @param timestamp 行为时间戳
 */
case class UserBehavior(
                         userId: Long,
                         videoId: Long,
                         behaviorType: String,
                         timestamp: Long
                       )