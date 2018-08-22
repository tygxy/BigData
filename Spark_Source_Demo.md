# Spark读写外部数据源

Spark经常会读写一些外部数据源，常见的有HDFS、HBase、JDBC、Redis、Kafka等。这些都是Spark的常见操作，做一个简单的Demo总结，方便后续开发查阅。

## 1.Spark读写HBase

### 1.1 maven依赖
```
 <properties>
    <hadoop.version>2.6.0-cdh5.7.0</hadoop.version>
    <hbase.version>1.2.0-cdh5.7.0</hbase.version>
 </properties>

<dependencies>
    <dependency>
          <groupId>org.apache.hadoop</groupId>
          <artifactId>hadoop-client</artifactId>
          <version>${hadoop.version}</version>
    </dependency>
	<dependency>
	    <groupId>org.apache.hbase</groupId>
	    <artifactId>hbase-client</artifactId>
	    <version>${hbase.version}</version>
	</dependency>
	<dependency>
	    <groupId>org.apache.hbase</groupId>
	    <artifactId>hbase-common</artifactId>
	    <version>${hbase.version}</version>
	</dependency>
	<dependency>
	    <groupId>org.apache.hbase</groupId>
	    <artifactId>hbase-server</artifactId>
	    <version>${hbase.version}</version>
	</dependency>
</dependencies>
```

### 1.2 HBaseUtils
```
package com.bupt.Hbase

import org.apache.hadoop.hbase.{HBaseConfiguration, HTableDescriptor, TableName}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.{HBaseAdmin, HTable}

object HBaseUtils {

  /**
    * 设置HBaseConfiguration
    * @param quorum
    * @param port
    * @param tableName
    */
  def getHBaseConfiguration(quorum:String, port:String, tableName:String) = {
    val conf = HBaseConfiguration.create()
    conf.set("hbase.zookeeper.quorum",quorum)
    conf.set("hbase.zookeeper.property.clientPort",port)

    conf
  }

  /**
    * 返回或新建HBaseAdmin
    * @param conf
    * @param tableName
    * @return
    */
  def getHBaseAdmin(conf:Configuration,tableName:String) = {
    val admin = new HBaseAdmin(conf)
    if (!admin.isTableAvailable(tableName)) {
      val tableDesc = new HTableDescriptor(TableName.valueOf(tableName))
      admin.createTable(tableDesc)
    }

    admin
  }

  /**
    * 返回HTable
    * @param conf
    * @param tableName
    * @return
    */
  def getTable(conf:Configuration,tableName:String) = {
    new HTable(conf,tableName)
  }
}
```

### 1.3 Spark读HBase
```
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
```

### 1.4 Spark写HBase

- 通过Put每次写一条
```
package com.bupt.Hbase

import java.util

import org.apache.hadoop.hbase.client.{HTable, Put}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapred.TableOutputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.mapred.JobConf
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by guoxingyu on 2018/8/17.
  * 通过HTable中的Put向HBase写数据
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


    val indataRDD = sc.makeRDD(Array("002,10","003,10","004,50"))

    indataRDD.foreachPartition(x=> {
      val conf = HBaseUtils.getHBaseConfiguration(quorum,port,tableName)
      conf.set(TableOutputFormat.OUTPUT_TABLE,tableName)

      val htable = HBaseUtils.getTable(conf,tableName)

      x.foreach(y => {
        val arr = y.split(",")
        val key = arr(0)
        val value = arr(1)

        val put = new Put(Bytes.toBytes(key))
        put.add(Bytes.toBytes("info"),Bytes.toBytes("clict_count"),Bytes.toBytes(value))
        htable.put(put)
      })
    })

    sc.stop()

  }
}
```

- 通过TableOutputFormat向HBase写数据
```
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
```

- 通过bulkload向HBase写数据，效率更高，推荐
```
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
```

### 1.5 参考
- https://blog.csdn.net/qq_25954159/article/details/52848947?locationNum=12&fps=1
- https://www.iteblog.com/archives/1891.html
- https://www.iteblog.com/archives/1889.html
- https://blog.csdn.net/gpwner/article/details/73530134