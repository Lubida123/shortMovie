package com.example.shortmovie;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan(basePackages = "com.example.shortmovie.mapper")
@SpringBootApplication
public class ShortmovieApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortmovieApplication.class, args);
    }

}
