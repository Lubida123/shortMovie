package com.shortmovie.util

import com.shortmovie.model.UserBehavior

/**
 * 行为评分转换工具类（适配公共模块）
 * 只保留UserBehavior的评分方法，删除旧BehaviorModel的兼容逻辑
 */
object ScoreUtil {
  /**
   * 计算行为评分（0-5分）
   * @param behavior 行为数据（UserBehavior）
   * @return 评分
   */
  def calculateScore(behavior: UserBehavior): Double = {
    behavior.behaviorType match {
      case "PLAY" =>
        // 播放行为：完播得3分，未完成按播放时长比例（0-3分）
        behavior.isCompleted match {
          case Some(1) => 3.0
          case Some(0) =>
            behavior.playDuration match {
              case Some(duration) if duration > 0 => math.min(3.0, duration / 10.0)  // 10秒以上播放得满分3分
              case _ => 1.0  // 播放但时长未知，得1分
            }
          case _ => 1.0
        }
      case "LIKE" => 4.0  // 点赞得4分
      case "COLLECT" => 5.0  // 收藏得5分（权重最高）
      case "COMMENT" => 3.5  // 评论得3.5分
      case _ => 0.0  // 未知行为得0分
    }
  }
}