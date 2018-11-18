package com.bupt.spark.Utils

import redis.clients.jedis.{JedisPool}
import org.apache.commons.pool2.impl.GenericObjectPoolConfig

/**
  * Created by guoxingyu on 2018/10/12.
  */
object RedisUtils extends Serializable {
  @transient private var pool : JedisPool = null

  /**
    * 创建JedisPool池
    * @param redisHost
    * @param redisPort
    * @param redisTimeout
    * @param maxTotal
    * @param maxIdle
    * @param minIdle
    * @param onBorrow
    * @param onReturn
    * @param maxWaitMillis
    */
  def makePool(redisHost: String, redisPort: Int, redisTimeout: Int,
               maxTotal: Int, maxIdle: Int, minIdle: Int, onBorrow: Boolean,
               onReturn: Boolean, maxWaitMillis: Long): Unit = {
    if(pool == null) {
      val poolConfig = new GenericObjectPoolConfig()
      poolConfig.setMaxTotal(maxTotal)
      poolConfig.setMaxIdle(maxIdle)
      poolConfig.setMinIdle(minIdle)
      poolConfig.setTestOnBorrow(onBorrow)
      poolConfig.setTestOnReturn(onReturn)
      poolConfig.setMaxWaitMillis(maxWaitMillis)
      pool = new JedisPool(poolConfig, redisHost, redisPort, redisTimeout)

      val hook = new Thread{
        override def run = pool.destroy()
      }
      sys.addShutdownHook(hook.run)
    }
  }

  /**
    * 获取JedisPool池
    * @return
    */
  def getPool : JedisPool = {
    assert(pool != null)
    pool
  }


}
