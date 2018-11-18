package com.bupt.spark

/**
  * Created by guoxingyu on 2018/9/22.
  */

import com.bupt.spark.conf.ConfigrationManager
import com.bupt.spark.Utils.RedisUtils
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.spark.{SparkConf, TaskContext}
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.slf4j.LoggerFactory
import redis.clients.jedis.Pipeline

object StreamingTest {

  val appName = "StreamingTest"
  val LOG = LoggerFactory.getLogger(StreamingTest.getClass)

  def main(args: Array[String]): Unit = {

    if (args.length != 1) {
      System.err.println("Usage:StreamingTest <propsName>")
      System.exit(1)
    }

    LOG.info("########## Stremaing Start ##########")

    // 读取配置文件信息
    val propName = args(0)
    val prop = new ConfigrationManager().loadParameterFromFile(propName + ".properties")

    // 初始化Spark
    val sparkConf = new SparkConf().setAppName(this.appName).setMaster("local[2]")
    sparkConf.set("spark.streaming.kafka.maxRatePerPartition", "200")

    val ssc = new StreamingContext(sparkConf, Seconds(prop.getProperty("streaming.interval.ms").toInt))

    // Kafka参数
    val topics = Array(prop.getProperty("topics"))

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> prop.getProperty("bootstrap.servers"),
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> prop.getProperty("group.id"),
      "auto.offset.reset" -> "earliest",
      //      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )
    
    // 方法一,使用Kafka

    // 创建stream流
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )
    LOG.info("########## create Stream success ##########")

    // stream流处理逻辑
    stream.foreachRDD(rdd => {
      val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      rdd.foreachPartition(iter => {
        iter.foreach(line => {
          // 简单的输出
          println(line.value())
        })
      })
      stream.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges)
    })




//     方法二，使用redis外部数据库

//     redis 配置参数
//        val maxTotal = 10
//        val maxIdle = 10
//        val minIdle = 1
//        val redisHost = "127.0.0.1"
//        val redisPort = 6379
//        val redisTimeOut = 30000
//        val maxWaitMillis = 10000
//
//        //默认db，用户存放Offset
//        val dbDefaultIndex = 8
//
//        // kafka相关的参数
//        val partition : Int = 0 //测试topic只有一个分区
//
//        // 从Redis获取上一次存储的offset
//        RedisUtils.makePool(redisHost,redisPort,redisTimeOut,maxTotal,maxIdle,minIdle,false,false,maxWaitMillis)
//        val jedis = RedisUtils.getPool.getResource
//        jedis.select(dbDefaultIndex)
//        val topic_partition_key = topics(0) + "_" + partition  // 构造查询的key，可以按照topic_partition的格式
//        var lastOffset = 0l
//        val lastSavedOffset = jedis.get(topic_partition_key)   // 从redis查询
//
//        if(lastSavedOffset != null) {
//          try {
//            lastOffset = lastSavedOffset.toLong
//          } catch {
//            case e : Exception => println(e.getMessage)
//              println("get lastSavedOffset error, lastSavedOffset from redis [" + lastSavedOffset + "] ")
//              System.exit(1)
//          }
//        }
//        RedisUtils.getPool.returnResource(jedis)
//        LOG.info(s"########## lastOffset from redis is $lastOffset ##########")
//
//        //设置每个分区起始的Offset
//        val fromOffsets = Map{new TopicPartition(topics(0), partition) -> lastOffset}
//
//        // 创建stream流
//        val stream = KafkaUtils.createDirectStream[String,String](
//          ssc,
//          LocationStrategies.PreferConsistent,
//          ConsumerStrategies.Assign[String, String](fromOffsets.keys.toList, kafkaParams, fromOffsets)
//        )
//        LOG.info("########## create Stream success ##########")
//
//        // stream流处理逻辑
//        stream.foreachRDD { rdd =>
//            val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
//            val jedis = RedisUtils.getPool.getResource
//            val p1 : Pipeline = jedis.pipelined();
//            p1.select(dbDefaultIndex)
//            p1.multi() //开启事务
//
//            //逐条处理消息
//            rdd.foreachPartition { iter =>
//              iter.foreach(line => {
//                // 简单的输出
//                println(line.value())
//              })
//            }
//
//            //更新Offset
//            offsetRanges.foreach { offsetRange =>
//              println("partition : " + offsetRange.partition + " fromOffset:  " + offsetRange.fromOffset + " untilOffset: " + offsetRange.untilOffset)
//              val topic_partition_key = offsetRange.topic + "_" + offsetRange.partition
//              p1.set(topic_partition_key, offsetRange.untilOffset + "")
//            }
//
//            p1.exec();//提交事务
//            p1.sync();//关闭pipeline
//
//            RedisUtils.getPool.returnResource(jedis)
//        }


        ssc.start()
        ssc.awaitTermination()
  }


}
