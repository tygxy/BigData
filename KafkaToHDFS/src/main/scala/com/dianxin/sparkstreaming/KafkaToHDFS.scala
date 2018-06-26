package com.dianxin.sparkstreaming

import java.text.SimpleDateFormat

import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import java.util.Calendar

/**
  * Created by guoxingyu on 2018/6/19.
  */
object KafkaToHDFS {
  def main(args: Array[String]): Unit = {
    // localhost:2181 test streamingtopic 1
    if (args.length != 4) {
      System.err.println("Usage:KafkaToHDFS <zkQuorum> <group> <topics> <numThreads>")
      System.exit(1)
    }

    val sparkConf = new SparkConf().setAppName("KafkaToHDFS").setMaster("local[2]")
    val ssc = new StreamingContext(sparkConf,Seconds(300));
    val Array(zkQuorum,group,topics,numThreads) = args

    val topicMap = topics.split(",").map((_,numThreads.toInt)).toMap

    val messages = KafkaUtils.createStream(ssc, zkQuorum, group, topicMap)

    val cal = Calendar.getInstance()
    val day = new SimpleDateFormat("yyyy-MM-dd")
    cal.add(Calendar.DATE, -1)
    val today = day.format(cal.getTime())

    val dt = today.substring(2,10)


//    messages.map(_._2).repartition(1).saveAsTextFiles("hdfs://localhost:8020/user/syslog/"+dt+"/")
    messages.map(_._2).repartition(1).saveAsTextFiles("/project/syslog1/"+dt+"/")

    ssc.start()
    ssc.awaitTermination()

  }
}