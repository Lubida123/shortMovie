package com.shortmovie.util

import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}

/**
 * Redis操作工具类（实时推荐核心）
 * 单例连接池，避免频繁创建连接
 */
object RedisUtil {
  // ========== 替换为你自己的Redis配置 ==========
  private val redisHost = "localhost"    // Redis地址（本地测试用localhost）
  private val redisPort = 6379           // Redis端口（默认6379）
  private val redisPassword = ""         // Redis密码（无密码则为空）
  private val redisDb = 0                // Redis数据库编号（默认0）

  // Redis连接池配置
  private val poolConfig = new JedisPoolConfig()
  poolConfig.setMaxTotal(20)    // 最大连接数
  poolConfig.setMaxIdle(10)     // 最大空闲连接数
  poolConfig.setMinIdle(5)      // 最小空闲连接数

  // 创建Jedis连接池（单例）
  private val jedisPool = new JedisPool(poolConfig, redisHost, redisPort, 3000, redisPassword, redisDb)

  /**
   * 获取Jedis连接
   */
  def getJedis: Jedis = {
    jedisPool.getResource
  }

  /**
   * 关闭Jedis连接（归还到连接池）
   */
  def closeJedis(jedis: Jedis): Unit = {
    if (jedis != null) {
      jedis.close()
    }
  }

  /**
   * 关闭连接池（程序退出时调用）
   */
  def closePool(): Unit = {
    if (jedisPool != null) {
      jedisPool.close()
    }
  }
}