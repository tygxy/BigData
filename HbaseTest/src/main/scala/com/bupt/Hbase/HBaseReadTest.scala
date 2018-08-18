package com.bupt.Hbase

import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by guoxingyu on 2018/8/18.
  * 从HBase读取数据
  */
object HBaseReadTest {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("HBaseReadTest").setMaster("local[2]")
    val sc = new SparkContext(sparkConf)

    val tableName = "imooc_course_clickcount"
    val quorum = "localhost"
    val port = "2181"

    // 配置相关信息
    val conf = HBaseUtils.getHBaseConfiguration(quorum,port,tableName)
    conf.set(TableInputFormat.INPUT_TABLE,tableName)

    // HBase数据转成RDD
    val hBaseRDD = sc.newAPIHadoopRDD(conf,classOf[TableInputFormat],
      classOf[org.apache.hadoop.hbase.io.ImmutableBytesWritable],
      classOf[org.apache.hadoop.hbase.client.Result]).cache()

    // RDD数据操作
    val data = hBaseRDD.map(x => {
      val result = x._2
      val key = Bytes.toString(result.getRow)
      val value = Bytes.toString(result.getValue("info".getBytes,"click_count".getBytes))
      (key,value)
    })

    data.foreach(println)

    sc.stop()
  }
}
