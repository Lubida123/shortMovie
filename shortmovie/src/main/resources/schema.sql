-- 推荐结果表
CREATE TABLE IF NOT EXISTS recommendation_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    movie_id BIGINT NOT NULL COMMENT '视频ID',
    score DOUBLE NOT NULL COMMENT '推荐分数',
    `rank` INT NOT NULL COMMENT '排名',
    `type` VARCHAR(20) NOT NULL COMMENT '推荐类型：OFFLINE/REAL_TIME',
    model_id VARCHAR(50) COMMENT '模型ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    -- 优化索引：联合索引用于查询和排序
    INDEX idx_user_type_rank (user_id, `type`, `rank`),
    INDEX idx_create_time (create_time),
    -- 新增：用于按模型ID查询
    INDEX idx_model_id (model_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐结果表';

-- 模型参数表
CREATE TABLE IF NOT EXISTS model_params (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    model_id VARCHAR(50) NOT NULL UNIQUE COMMENT '模型ID',
    `rank` INT NOT NULL COMMENT 'ALS隐因子数量',
    reg_param DOUBLE NOT NULL COMMENT '正则化参数',
    max_iter INT NOT NULL COMMENT '最大迭代次数',
    training_time TIMESTAMP NOT NULL COMMENT '训练时间',
    model_path VARCHAR(255) COMMENT '模型存储路径',
    rmse DOUBLE COMMENT '模型RMSE评估指标',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    -- 新增：用于按训练时间查询最新模型
    INDEX idx_training_time (training_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型参数表';
