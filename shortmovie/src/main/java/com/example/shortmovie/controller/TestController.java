package com.example.shortmovie.controller;

import com.example.shortmovie.entity.BehaviorRecord;
import com.example.shortmovie.service.KafkaMessageProducer;
import com.example.shortmovie.utils.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@RestController
public class TestController {

    @Resource
    private KafkaMessageProducer kafkaMessageProducer;

    @GetMapping("/sendmsg")
    public R sendBehavior(){
        try{
            Random random = new Random();
            String[] behaviorTypes = {"PLAY", "LIKE", "COMMENT", "COLLECT"};
            for (int i=1; i<500; i++) {
                //创建一个测试对象
                BehaviorRecord behaviorRecord = new BehaviorRecord();
                behaviorRecord.setId((long) random.nextInt(10000));
                behaviorRecord.setUserId((long) random.nextInt(100));
                behaviorRecord.setVideoId((long) random.nextInt(100));
                behaviorRecord.setBehaviorType(behaviorTypes[random.nextInt(4)]);
                behaviorRecord.setPlayDuration(random.nextInt(3000));
                behaviorRecord.setIsCompleted(random.nextInt(1));

                // 1. 把对象转成 JSON 字符串
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonStr = objectMapper.writeValueAsString(behaviorRecord);
                log.info(i+"messages == ", jsonStr);
                kafkaMessageProducer.sendMessageAsync(jsonStr);
            }
            return R.ok("发送成功！");
        } catch (Exception e) {
            return R.error("发送失败！"+e.getMessage());
        }
    }
}
