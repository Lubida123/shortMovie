package com.shortmovie.model

/**
 * 模型参数实体类（对应model_params表）
 * 复用原离线推荐的模型参数逻辑
 */
case class ModelParamModel(
                            modelId: String,        // 模型唯一ID
                            rank: Int,              // ALS隐因子数量
                            regParam: Double,       // 正则化参数
                            maxIter: Int,           // 最大迭代次数
                            trainingTime: String,   // 训练时间（yyyy-MM-dd HH:mm:ss）
                            modelPath: String,      // 模型存储路径
                            rmse: Double            // 模型评估RMSE
                          )