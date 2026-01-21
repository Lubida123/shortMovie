package com.example.shortmovie.config;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据库初始化组件
 * 在应用启动时自动执行schema.sql脚本创建推荐系统所需的表
 */
@Slf4j
@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化推荐系统数据库表...");
        
        try {
            // 读取schema.sql文件
            ClassPathResource resource = new ClassPathResource("schema.sql");
            String sql;
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                sql = reader.lines()
                        .filter(line -> !line.trim().startsWith("--"))  // 过滤注释行
                        .collect(Collectors.joining("\n"));
            }
            
            // 分割SQL语句（按分号分割，但保留分号前的内容）
            String[] sqlStatements = sql.split(";");
            
            // 执行每条SQL语句
            for (String statement : sqlStatements) {
                String trimmedStatement = statement.trim();
                if (!trimmedStatement.isEmpty()) {
                    try {
                        jdbcTemplate.execute(trimmedStatement);
                        // 提取表名用于日志
                        String tableName = extractTableName(trimmedStatement);
                        log.info("成功创建表: {}", tableName);
                    } catch (Exception e) {
                        // 如果表已存在，记录日志但不抛出异常（幂等性）
                        if (e.getMessage() != null && 
                            (e.getMessage().contains("already exists") || 
                             e.getMessage().contains("Table") && e.getMessage().contains("already exists"))) {
                            String tableName = extractTableName(trimmedStatement);
                            log.info("表已存在，跳过创建: {}", tableName);
                        } else {
                            log.error("执行SQL语句失败: {}", e.getMessage());
                            throw e;
                        }
                    }
                }
            }
            
            log.info("推荐系统数据库表初始化完成");
            
        } catch (Exception e) {
            log.error("初始化推荐系统数据库表失败", e);
            throw e;
        }
    }
    
    /**
     * 从CREATE TABLE语句中提取表名
     */
    private String extractTableName(String sql) {
        try {
            String upperSql = sql.toUpperCase();
            int tableIndex = upperSql.indexOf("TABLE");
            if (tableIndex != -1) {
                int ifNotExistsIndex = upperSql.indexOf("IF NOT EXISTS", tableIndex);
                int startIndex = ifNotExistsIndex != -1 ? 
                        ifNotExistsIndex + "IF NOT EXISTS".length() : tableIndex + "TABLE".length();
                String remaining = sql.substring(startIndex).trim();
                int spaceIndex = remaining.indexOf(' ');
                int parenIndex = remaining.indexOf('(');
                int endIndex = Math.min(
                        spaceIndex == -1 ? Integer.MAX_VALUE : spaceIndex,
                        parenIndex == -1 ? Integer.MAX_VALUE : parenIndex
                );
                if (endIndex != Integer.MAX_VALUE) {
                    return remaining.substring(0, endIndex).trim();
                }
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
        return "unknown";
    }
}
