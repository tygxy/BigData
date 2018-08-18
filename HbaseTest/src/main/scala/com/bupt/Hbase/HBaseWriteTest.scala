package com.bupt.Hbase

import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapred.TableOutputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.mapred.JobConf
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by guoxingyu on 2018/8/17.
  * 通过TableOutputFormat向HBase写数据
  */
object HBaseWriteTest {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("HBaseWriteTest").setMaster("local[2]")
    val sc = new SparkContext(sparkConf)

    val tableName = "imooc_course_clickcount"
    val quorum = "localhost"
    val port = "2181"

    // 配置相关信息
    val conf = HBaseUtils.getHBaseConfiguration(quorum,port,tableName)
    conf.set(TableOutputFormat.OUTPUT_TABLE,tableName)

    val jobConf = new JobConf()
    jobConf.setOutputFormat(classOf[TableOutputFormat])
    jobConf.set(TableOutputFormat.OUTPUT_TABLE,tableName)

    // 写入数据到HBase
    val indataRDD = sc.makeRDD(Array("20180723_02,10","20180723_03,10","20180818_03,50"))

    val rdd = indataRDD.map(_.split(",")).map{arr => {
      val put = new Put(Bytes.toBytes(arr(0)))
      put.add(Bytes.toBytes("info"),Bytes.toBytes("clict_count"),Bytes.toBytes(arr(1)))
      (new ImmutableBytesWritable,put)
    }}.saveAsHadoopDataset(jobConf)

    sc.stop()

  }

}
