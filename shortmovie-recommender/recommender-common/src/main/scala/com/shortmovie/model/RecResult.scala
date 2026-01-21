package com.shortmovie.model

/**
 * 推荐结果统一实体类（离线/实时共用）
 * 对应MySQL的recommendation_result表，字段和你离线推荐的一致
 */
case class RecResult(
                      userId: Long,        // 用户ID
                      movieId: Long,       // 视频ID（和你离线推荐的movie_id字段对应）
                      score: Double,       // 推荐评分（0-1）
                      rank: Int,           // 推荐排名
                      recType: String,     // 推荐类型（OFFLINE/REAL_TIME）
                      modelId: String,     // 模型ID（UUID）
                      createTime: String   // 创建时间（格式：yyyy-MM-dd HH:mm:ss）
                    )