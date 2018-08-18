package com.bupt.Hbase

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.hadoop.hbase.mapreduce.{HFileOutputFormat2, LoadIncrementalHFiles}
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.mapred.TableOutputFormat
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.hbase.KeyValue


/**
  * Created by guoxingyu on 2018/8/18.
  * 通过bulkload向HBase写数据
  */
object HBaseWriteTest2 {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("HBaseReadTest").setMaster("local[2]")
    val sc = new SparkContext(sparkConf)

    val tableName = "imooc_course_clickcount"
    val quorum = "localhost"
    val port = "2181"

    // 配置相关信息
    val conf = HBaseUtils.getHBaseConfiguration(quorum,port,tableName)
    conf.set(TableOutputFormat.OUTPUT_TABLE,tableName)

    val table = HBaseUtils.getTable(conf,tableName)

    val job = Job.getInstance(conf)
    job.setMapOutputKeyClass(classOf[ImmutableBytesWritable])
    job.setMapOutputKeyClass(classOf[KeyValue])

    HFileOutputFormat2.configureIncrementalLoadMap(job, table)


    // inputRDD data
    val indataRDD = sc.makeRDD(Array("20180723_02,13","20180723_03,13","20180818_03,13"))
    val rdd = indataRDD.map(x => {
      val arr = x.split(",")
      val kv = new KeyValue(Bytes.toBytes(arr(0)),"info".getBytes,"clict_count".getBytes,arr(1).getBytes)
      (new ImmutableBytesWritable(Bytes.toBytes(arr(0))),kv)
    })

    // 保存Hfile to HDFS
    rdd.saveAsNewAPIHadoopFile("hdfs://localhost:8020/tmp/hbase",classOf[ImmutableBytesWritable],classOf[KeyValue],classOf[HFileOutputFormat2],conf)

    // Bulk写Hfile to HBase
    val bulkLoader = new LoadIncrementalHFiles(conf)
    bulkLoader.doBulkLoad(new Path("hdfs://localhost:8020/tmp/hbase"),table)

  }
}
