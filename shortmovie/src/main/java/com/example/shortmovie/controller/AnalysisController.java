package com.example.shortmovie.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/analysis")
@RestController
public class AnalysisController {
    // 替换成你自己的 Spark 安装路径（bin 目录下的 spark-submit.cmd）
    private static final String SPARK_SUBMIT_PATH = "D:\\spark\\bin\\spark-submit.cmd";
    
    // JAR文件路径 - 需要修改为你的实际路径
    private static final String JAR_PATH = "D:\\实训项目-短视频\\shortmovie\\shortmovie\\spark-analysis\\target\\spark-behavior-analysis-1.0-SNAPSHOT-jar-with-dependencies.jar";
    
    //这是接口地址：http://localhost:8080/analysis/movie
    @Scheduled(cron = "0 0 2 * * *") //当程序运行的情况下，每日2点会运行该方法
    @GetMapping("/movie")
    public String runAnalysis() throws Exception {
        // 构建 spark-submit 命令（指定完整路径）
        Process process = new ProcessBuilder(
                SPARK_SUBMIT_PATH,
                "--class", "com.example.spark.ShortVideoBehaviorAnalysis",
                "--master", "local[*]",
                "--driver-memory", "2g",
                JAR_PATH
        )
                // 可选：重定向错误输出，方便排查问题
                .redirectErrorStream(true)
                .start();

        // 可选：读取命令执行的输出（便于调试）
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder output = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        int exitCode = process.waitFor();
        // 返回执行结果+输出，方便调试
        return "Exit code: " + exitCode + "\nOutput: " + output.toString();
    }
}
