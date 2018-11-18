package com.bupt.spark.Utils

import org.apache.spark.streaming.StreamingContext
import org.slf4j.LoggerFactory
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path

/**
  * Created by guoxingyu on 2018/10/12.
  * Spark Streaming 停止策略
  * 1. 通过扫描hdfs
  *
  */
object StreamCloseUtils {

  val LOG = LoggerFactory.getLogger(StreamCloseUtils.getClass)

  /**
    * 通过扫描HDFS路径下的消息文件，判断是否需要停止程序
    * @param ssc
    */
    def stopByMarkFile(ssc : StreamingContext) = {
      val intervalMills = 10 * 1000
      var isStop = false
      val hdfsFilePath = "/user/spark/streaming/stop"
      while (!isStop) {
        isStop = ssc.awaitTerminationOrTimeout(intervalMills)
        if (! isStop && isExistsMarkFile(hdfsFilePath)) {
          LOG.info("5秒后关闭Spark Streaming程序.....")
          Thread.sleep(5000)
          ssc.stop(true, true)
        }
      }

    /**
      * 判断是否存在消息文件
      * @param hdfsFilePath
      * @return
      */
    def isExistsMarkFile(hdfsFilePath: String): Boolean = {
      val conf = new Configuration()
      val path = new Path(hdfsFilePath)
      val fs = path.getFileSystem(conf)
      fs.exists(path)
    }
  }
}
