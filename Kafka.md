# Kafka

## 参考资料
- https://blog.csdn.net/tangdong3415/article/details/53432166

## Producer
- Scala版本
```
package com.bupt.kafkaTest

import java.util.Properties
import java.util.concurrent.Future

import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord, RecordMetadata}

import scala.io.Source

/**
  * Created by guoxingyu on 2018/5/24.
  */
object KafkaProducerTest {

    def main(args: Array[String]): Unit = {

      // 读取文件,写入kafka
      val fileName = "/Users/guoxingyu/Documents/work/spark/kafkaTest/src/main/resources/log.txt"
      val source = Source.fromFile(fileName)
      val lines = source.getLines

      // 设置kafka参数
      val brokers = "localhost:9092"
      val topic = "streamingtopic"
      val props = new Properties()

      props.put("metadata.broker.list", brokers)
      props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
      props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
      props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)

      val producer = new KafkaProducer[String, String](props)

      var i = 0
      while(true){
          // 有metadata信息
          val ret: Future[RecordMetadata] = producer.send(new ProducerRecord(topic, "key-" + i))
          // 无metadata信息
          producer.send(new ProducerRecord(topic, "value-" + i))
          // 打印出 metadata
          val metadata = ret.get
          println("i=" + i + ",  offset=" + metadata.offset() + ",  partition=" + metadata.partition())


          i = i + 1
          Thread.sleep(2000)
      }

//      文件内容写入kafka
//      for (line <- lines){
//        producer.send(new ProducerRecord(topic, line))
//        Thread.sleep(2000)
//      }
//      source.close;  //记得要关闭source


      producer.close
    }


}
```
