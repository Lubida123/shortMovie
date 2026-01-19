package com.example.shortmovie.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.example.shortmovie.entity.BehaviorRecord;
import com.example.shortmovie.service.KafkaMessageProducer;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;


/**
 * kafka生产者服务实现
 */
@Slf4j
@Service
public class KafkaMessageProducerImpl implements KafkaMessageProducer{

    @Resource
    private KafkaTemplate<String, BehaviorRecord> kafkaTemplate;

    @Resource
    private KafkaTemplate<String, byte[]> kafkaTemplate1;

    @Value("${kafka.topic.behavior}")
    private String behaviorTopic;
    
    @Value("${kafka.retry.max-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${kafka.retry.delay-ms:1000}")
    private long retryDelayMs;

    /**
     *  发送对象到Kafka（使用CompletableFuture替代ListenableFuture）
     *  实现容错机制：
     *  1. 异常处理：捕获所有异常并记录警告日志
     *  2. 不阻塞用户操作：异步发送，失败不影响主流程
     *  3. 可选重试机制：失败后自动重试
     */
    @Override
    public void sendBehaviorObject(BehaviorRecord behavior){
        sendBehaviorObjectWithRetry(behavior, 0);
    }
    
    /**
     * 带重试机制的发送方法
     * @param behavior 行为记录对象
     * @param attemptCount 当前尝试次数
     */
    private void sendBehaviorObjectWithRetry(BehaviorRecord behavior, int attemptCount) {
        try {
            // 发送消息，返回CompletableFuture
            CompletableFuture<SendResult<String, BehaviorRecord>> future = kafkaTemplate.send(behaviorTopic, behavior);
            
            // 使用CompletableFuture的回调方法
            future.whenComplete((result, ex) -> {
                if(ex == null){
                    // 发送成功
                    log.info("行为记录消息发送成功 - Topic: {}, Partition: {}, Offset: {}, BehaviorType: {}, UserId: {}, VideoId: {}", 
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        behavior.getBehaviorType(),
                        behavior.getUserId(),
                        behavior.getVideoId());
                    
                    if (attemptCount > 0) {
                        log.info("消息重试成功，尝试次数: {}", attemptCount);
                    }
                } else {
                    // 发送失败
                    handleSendFailure(behavior, ex, attemptCount);
                }
            });
        } catch (Exception e) {
            // 捕获同步异常（如Kafka连接失败）
            log.warn("Kafka消息发送异常 - BehaviorType: {}, UserId: {}, VideoId: {}, 错误: {}", 
                behavior.getBehaviorType(),
                behavior.getUserId(),
                behavior.getVideoId(),
                e.getMessage());
            
            // 尝试重试
            retryIfPossible(behavior, e, attemptCount);
        }
    }
    
    /**
     * 处理发送失败
     * @param behavior 行为记录对象
     * @param ex 异常
     * @param attemptCount 当前尝试次数
     */
    private void handleSendFailure(BehaviorRecord behavior, Throwable ex, int attemptCount) {
        log.warn("Kafka消息发送失败 - BehaviorType: {}, UserId: {}, VideoId: {}, 尝试次数: {}, 错误: {}", 
            behavior.getBehaviorType(),
            behavior.getUserId(),
            behavior.getVideoId(),
            attemptCount + 1,
            ex.getMessage());
        
        // 尝试重试
        retryIfPossible(behavior, ex, attemptCount);
    }
    
    /**
     * 如果可能则重试
     * @param behavior 行为记录对象
     * @param ex 异常
     * @param attemptCount 当前尝试次数
     */
    private void retryIfPossible(BehaviorRecord behavior, Throwable ex, int attemptCount) {
        if (attemptCount < maxRetryAttempts) {
            log.info("准备重试发送Kafka消息，延迟{}ms后进行第{}次重试", retryDelayMs, attemptCount + 1);
            
            // 延迟后重试
            CompletableFuture.delayedExecutor(retryDelayMs, TimeUnit.MILLISECONDS)
                .execute(() -> sendBehaviorObjectWithRetry(behavior, attemptCount + 1));
        } else {
            // 达到最大重试次数，记录错误但不抛出异常
            log.error("Kafka消息发送失败，已达到最大重试次数({}) - BehaviorType: {}, UserId: {}, VideoId: {}, 最终错误: {}", 
                maxRetryAttempts,
                behavior.getBehaviorType(),
                behavior.getUserId(),
                behavior.getVideoId(),
                ex.getMessage());
            
            // 可选：将失败的消息记录到数据库或文件，供后续补偿处理
            log.warn("建议：可以实现补偿机制，将失败的消息持久化以便后续重新发送");
        }
    }

    /**
     * 发送字符串到Kafka（同样用CompletableFuture）
     * 实现容错机制：异常处理和日志记录
     */
    @Override
    public void sendMessageAsync(String message){
        try {
            // 把字符串转成 UTF-8 编码的字节数组
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            
            //发送字符串
            CompletableFuture<SendResult<String, byte[]>> future = kafkaTemplate1.send(behaviorTopic, messageBytes);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("字符串消息发送成功 - Topic: {}, Partition: {}, Offset: {}", 
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                } else {
                    log.warn("字符串消息发送失败 - 错误: {}", ex.getMessage());
                }
            });
        } catch (Exception e) {
            // 捕获同步异常，记录警告日志但不影响主流程
            log.warn("Kafka字符串消息发送异常 - 错误: {}", e.getMessage());
        }
    }
}
