package com.example.shortmovie.service;

import com.example.shortmovie.entity.BehaviorRecord;

/**
 * kafka生产者服务接口
 */
public interface KafkaMessageProducer {

    /**
     * 发送行为对象到Kafka
     * @param behavior 行为记录对象
     */
    void sendBehaviorObject(BehaviorRecord behavior);

    /**
     * 发送字符串消息到Kafka
     * @param message 字符串消息
     */
    void sendMessageAsync(String message);
}
