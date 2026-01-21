package com.shortmovie.offline.config

import com.shortmovie.constant.RecConstants

/**
 * 离线推荐配置类
 * 适配你的历史模型参数（rank=10, regParam=0.01）
 */
object OfflineConfig {
  // ALS算法参数（匹配你的model_params表历史数据）
  val ALS_RANK = 10          // 隐因子数量
  val ALS_REG_PARAM = 0.01   // 正则化参数（你的历史数据是0.01，不是0.1）
  val ALS_MAX_ITER = 10      // 最大迭代次数

  // 推荐数量（复用公共常量）
  val TOP_N = RecConstants.OFFLINE_TOP_N

  // 模型存储路径（本地测试用，确保路径存在）
  val MODEL_SAVE_PATH = "data/models/als_model"
  
  // Spark性能优化配置
  val SPARK_EXECUTOR_MEMORY = "2g"      // Executor内存
  val SPARK_DRIVER_MEMORY = "1g"        // Driver内存
  val SPARK_EXECUTOR_CORES = "2"        // Executor核心数
  val SPARK_DEFAULT_PARALLELISM = "8"   // 默认并行度
  val SPARK_SQL_SHUFFLE_PARTITIONS = "8" // Shuffle分区数
  val SPARK_MEMORY_FRACTION = "0.8"     // 内存分配比例
  val SPARK_MEMORY_STORAGE_FRACTION = "0.5" // 存储内存比例
}