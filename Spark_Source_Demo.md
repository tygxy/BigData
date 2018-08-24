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

### 1.2 Project结构


### 1.3 HBaseUtils
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

### 1.4 Spark读HBase
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

### 1.5 Spark写HBase

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

### 1.6 参考
- https://blog.csdn.net/qq_25954159/article/details/52848947?locationNum=12&fps=1
- https://www.iteblog.com/archives/1891.html
- https://www.iteblog.com/archives/1889.html
- https://blog.csdn.net/gpwner/article/details/73530134




## 2.Spark读写JDBC

### 2.1 maven依赖
```
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.30</version>
</dependency>

```

### 2.2 Project结构

基本按照Java经典的DAO模式组织
![](/resource/spark_jdbc.jpg?raw=true)

### 2.3 MySQLUtils 

```
package com.bupt.jdbc.Utils

import java.sql.{Connection, DriverManager, PreparedStatement}

/**
  * Created by guoxingyu on 2018/8/22.
  * MySQL工具类
  */

object MySQLUtils {
  /**
    * 获取MySQL链接
    */
  def getConnection() = {
    DriverManager.getConnection("jdbc:mysql://localhost:3306/guoxingyutest?user=root&password=302313")
  }

  /**
    * 释放数据库等资源
    * @param connection
    * @param pstmt
    */
  def release(connection:Connection, pstmt:PreparedStatement) = {
    try {
      if (pstmt != null) {
        pstmt.close()
      }
    } catch {
      case e : Exception => e.printStackTrace()
    } finally {
      if (connection != null) {
        connection.close()
      }
    }
  }

}
```
### 2.4 RDD2Mysql

- 首先构造Bean
```
package com.bupt.jdbc.Bean

/**
  * Created by guoxingyu on 2018/8/22.
  */
case class UserInfoAccess (userName:String,age:Int)

``` 

- 其次写DAO
```
package com.bupt.jdbc.DAO

import com.bupt.jdbc.Bean.UserInfoAccess
import java.sql.{Connection, PreparedStatement}
import com.bupt.jdbc.Utils.MySQLUtils
import com.mysql.jdbc.Statement

import scala.collection.mutable.ListBuffer
/**
  * Created by guoxingyu on 2018/8/22.
  */
object StatDAO {

  /**
    * 批量保存到数据库userInfo
    * @param list
    */
  def insertUserInfo(list: ListBuffer[UserInfoAccess]) = {
    var connection : Connection = null
    var pstmt : PreparedStatement = null

    // 插入新数据
    try {
      connection = MySQLUtils.getConnection()
      val sql = "insert into guoxingyutest.userInfo(user_name,age) values (?,?)"
      pstmt = connection.prepareStatement(sql)
      connection.setAutoCommit(false) // 设置手动提交

      for (ele <- list) {
        pstmt.setString(1, ele.userName)
        pstmt.setInt(2, ele.age)

        pstmt.addBatch()
      }

      pstmt.executeBatch()
      connection.commit()
    } catch {
      case e : Exception => e.printStackTrace()
    } finally {
      MySQLUtils.release(connection,pstmt)
    }
  }

  /**
    * 删除数据
    * @param id
    */
  def deleteUserInfo(id:Int) = {
    var connection : Connection = null
    var pstmt : PreparedStatement = null

    try {
      connection = MySQLUtils.getConnection()
      val sql_del = "delete from guoxingyutest.userInfo where id = ?"
      pstmt = connection.prepareStatement(sql_del)

      pstmt.setInt(1,id)
      pstmt.executeUpdate() > 0
    } catch {
      case e : Exception => e.printStackTrace()
    } finally {
      MySQLUtils.release(connection,pstmt)
    }
  }

}
```

- 最后在RDD中使用
```
package com.bupt.jdbc

import com.bupt.jdbc.Bean.UserInfoAccess
import com.bupt.jdbc.DAO.StatDAO
import org.apache.spark.sql.SparkSession

import scala.collection.mutable.ListBuffer

/**
  * Created by guoxingyu on 2018/8/22.
  */

object JDBCTest {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().appName("JDBCTest").master("local[2]").getOrCreate()

    val data = spark.sparkContext.makeRDD(List("tom,23","bob,24"))

    // 删除数据
    try {
      StatDAO.deleteUserInfo(1)
    } catch {
      case e : Exception => e.printStackTrace()
    }


    // 数据存入MySQL数据库
    try {
      data.foreachPartition(partitionOfRecords => {
        val list = new ListBuffer[UserInfoAccess]
        partitionOfRecords.foreach(info => {
          val arr = info.split(",")
          val userName = arr(0)
          val age = arr(1).toInt

          list.append(UserInfoAccess(userName,age))
        })
        StatDAO.insertUserInfo(list)
      })
    } catch {
      case e : Exception => e.printStackTrace()
    }

    spark.close()
  }
}
```





