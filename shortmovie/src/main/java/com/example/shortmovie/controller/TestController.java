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
            BehaviorRecord behaviorRecord = new BehaviorRecord();
            behaviorRecord.setBehaviorType("PLAY");

            // 1. 把对象转成 JSON 字符串
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonStr = objectMapper.writeValueAsString(behaviorRecord);
            log.info("messages == ", jsonStr);
            kafkaMessageProducer.sendMessageAsync(jsonStr);
            return R.ok("发送成功！");
        } catch (Exception e) {
            return R.error("发送失败！"+e.getMessage());
        }
    }
}
