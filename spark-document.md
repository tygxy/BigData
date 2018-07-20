# Spark Official Document

## 1.快速入门

## 2.Programming Guide

- 必须创建SparkContext对象,是spark程序的入口
```
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf

val conf = new SparkConf().setAppName(appName).setMaster(master) 
val sc = new SparkContext(conf)
```
- master类型有四个:local,yarn,standalone,mesos
- 创建RDD方式有两种，Parallelized和外部数据源
	- 标准读取代码
	```
	val distFile = sc.textFile(URLPath)
	```
	- 创建的partitions分区数和HDFS文件数相同，也可以设置更高的数量
- RDD操作
	- 两种类型操作，transformations和actions，transformations是惰性的
	- RDDs.collect()集中在driver上
	- transformations的算子：map,filter,flatmap,sample,union,distinct,groupByKey,reduceByKey,sortByKey,join,coalesce,repatition
	- action的算子:reduce,collect,count,take,saveAsText,countByKey,foreach
	- 有shuffle过程的算子：coalesce,repatition,groupByKey,reduceByKey,join
- RDD持久化
	- persist(),可以设置存储级别
	- cache() 保存到内存
- 共享变量
	- Broadcast variables,保存一个只读的变量在executor中
	```
	val broadcastVar = sc.broadcast(RDD)
	```
	- Accumulators
	```
	val accum = sc.longAccumulator("My Accumulator")

	// 只能在driver读取累加器的值
	accum.value
	```



## 3. Spark Streaming

### 3.1 快速上手实例，实时统计时间批次内的wordcount
```
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}


object NetworkWordCount {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[2]").setAppName("NetworkWordCount")
    val ssc = new StreamingContext(sparkConf,Seconds(5))

    val lines = ssc.socketTextStream("localhost",6789)
    val result = lines.flatMap(_.split(" ")).map((_,1)).reduceByKey(_+_)
    result.print()

    ssc.start()
    ssc.awaitTermination()
  }
}
```
### 3.2 高级数据源

- kafka 0.8 版本
	- 基于Receiver，可能会丢失数据，解决办法是Write Ahead Logs，这个日志会接受kafka来的数据入HDFS
	```
	object KafKaReceiverWordCount {
	  def main(args: Array[String]): Unit = {
	    if (args.length != 4) {
	      System.err.println("Usage:KafKaReceiverWordCount <zkQuorum> <group> <topics> <numThreads>")
	      System.exit(1)
	    }
	    val sparkConf = new SparkConf().setAppName("KafKaReceiverWordCount").setMaster("local[2]")
	    val ssc = new StreamingContext(sparkConf,Seconds(5));
	    val Array(zkQuorum,group,topics,numThreads) = args

	    val topicMap = topics.split(",").map((_,numThreads.toInt)).toMap

	    val messages = KafkaUtils.createStream(ssc, zkQuorum, group, topicMap)
			
		messages.map(_._2).flatMap(_.split(" ")).map((_,1)).reduceByKey(_+_).print()

	    ssc.start()
	    ssc.awaitTermination()
	  }
	}

	```

	- 直接接收，优点是建立kafka分区和RDD的一一对应，提高并行度；不需要Write Ahead Logs备份数据，交给了kafka；Offset不依赖zk,保存在checkpoint，保障Record只消费一次。
	```
	/**
	  * Created by guoxingyu on 2018/1/23.
	  * Spark Streaming对接Kafka方式二：Direct方式
	  * 版本需要匹配，kafka的版本是0.8.2.2
	  * localhost:9092 hello_topic,注意输入的参数是brokerlist
	  */
	object KafKaDirectWordCount {
		  def main(args: Array[String]): Unit = {
		    if (args.length != 2) {
		      System.err.println("Usage:KafKaDirectWordCount <brokers> <topics>")
		      System.exit(1)
		    }
		    val sparkConf = new SparkConf().setAppName("KafKaDirectWordCount").setMaster("local[2]")
		    val ssc = new StreamingContext(sparkConf,Seconds(5));
		    ssc.checkpoint(".")



		    val Array(brokers,topics) = args
		    val kafkaParams = Map[String,String]("metadata.broker.list" -> brokers,"group.id" -> "test")
		    val topicsSet = topics.split(",").toSet

		    val messages = KafkaUtils.createDirectStream[String,String,StringDecoder,StringDecoder](ssc,kafkaParams,topicsSet)

		    messages.map(_._2).flatMap(_.split(" ")).map((_,1)).reduceByKey(_+_).print()


		    ssc.start()
		    ssc.awaitTermination()
		  }
		}

	```


### 3.3 DStreams的算子

- UpdateStateByKey
	- 实现功能：针对是<K,V>的数据对，让每一个K维持一份state，并不断更新该stata。
	- code
	```
	/**
	  * Created by guoxingyu on 2018/1/22.
	  * 统计截止目前为止的词频
	  */
	object StatefulWordCount {
		  def main(args: Array[String]): Unit = {
		    val sparkConf = new SparkConf().setMaster("local[2]").setAppName("StatefulWordCount")
		    val ssc = new StreamingContext(sparkConf,Seconds(5))
		    // 如果使用带状态的算子，必须设置Checkpoint
		    // 在生产环境中，Checkpoint保存在HDFS中，这里是存到了本地路径中
		    ssc.checkpoint(".")
		    // ssc.checkpoint("hdfs://localhost:8020/worldcount_checkpoint") //生成环境参考

		    val lines = ssc.socketTextStream("localhost",6789)
		    val result = lines.flatMap(_.split(" ")).map((_,1))
		    val state = result.updateStateByKey(updateFunction _)
		    state.print()

		    ssc.start()
		    ssc.awaitTermination()
		    }

		  /**
		    * 把当前的数据去更新已有的或者老的数据
		    * @param currentValues
		    * @param preValues
		    * @return
		    */
		  def updateFunction(currentValues: Seq[Int], preValues: Option[Int]): Option[Int] = {
		    val current = currentValues.sum  
		    val pre = preValues.getOrElse(0)   

		    Some(current + pre)

		  }
	}
	```

- transform
	- 功能：实现RDD到RDD的任意操作
	- code
	```
	/**
	  * Created by guoxingyu on 2018/1/22.
	  * 黑名单过滤
	  * 输入数据比如1,ls则被过滤，2,hello就没有
	  */
	object TransformApp {
		  def main(args: Array[String]): Unit = {
		    val sparkConf = new SparkConf().setMaster("local[2]").setAppName("NetworkWordCount")
		    val ssc = new StreamingContext(sparkConf,Seconds(5))

		    // 构建黑名单
		    val blacks = List("zs","ls")
		    val blacksRDD = ssc.sparkContext.parallelize(blacks).map(x => (x,true))


		    val lines = ssc.socketTextStream("localhost",6789)

		    val clicklog = lines.map(x => (x.split(",")(1),x)).transform(rdd => {
		      rdd.leftOuterJoin(blacksRDD).filter(x => x._2._2.getOrElse(false) != true).map(x=>x._2._1)
		    })

		    clicklog.print()

		    ssc.start()
		    ssc.awaitTermination()
		  }
	}
	```

- window窗口函数
	- 窗口函数需要设置窗口长度和滑动距离，这两个值必须是DStreams的时间间隔的整数倍
	- code
	```
	object ForeachRDDApp {
		  def main(args: Array[String]): Unit = {
		    val sparkConf = new SparkConf().setMaster("local[2]").setAppName("ForeachRDDApp")
		    val ssc = new StreamingContext(sparkConf,Seconds(5))

		    val lines = ssc.socketTextStream("localhost",6789)
		    val result = lines.flatMap(_.split(" ")).map((_,1))

		    val windowedWordCount = result.reduceByKeyAndWindow((a:Int,b:Int) => (a+b),Seconds(30),Seconds(10))

		    windowedWordCount.print()

		    ssc.start()
		    ssc.awaitTermination()
		    }
	}
	```

- join函数
	- val joinedStream = stream1.leftOuterjoin(stream2)


### 3.4 DStreams的输出

- 输出到mysql
```
object ForeachRDDApp {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[2]").setAppName("ForeachRDDApp")
    val ssc = new StreamingContext(sparkConf,Seconds(5))

    val lines = ssc.socketTextStream("localhost",6789)
    val result = lines.flatMap(_.split(" ")).map((_,1))


    result.foreachRDD(RDD => {
      RDD.foreachPartition(partitionofRecords => {
        val connection = createConnection()
        partitionofRecords.foreach(record => {
          val sql = "insert into wordcount(word, wordcount) values('" + record._1 + "'," + record._2 + ")"
          connection.createStatement().execute(sql)
        })
        connection.close()
      })
    })


    ssc.start()
    ssc.awaitTermination()
    }

  /**
    * 获取MySQL的连接
    * @return
    */
  def createConnection() = {
    DriverManager.getConnection("jdbc:mysql://localhost:3306/imooc_project?user=root&password=xxxxxx")
  }
}

```

### 3.5 DStreams 的SQL解决方案
```
/**
  * Created by guoxingyu on 2018/7/20.
  * spark streaming 转换成DF，通过SQL命令操作
  */
object WordCountBySQL {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[2]").setAppName("ForeachRDDApp")
    val ssc = new StreamingContext(sparkConf,Seconds(5))

    val lines = ssc.socketTextStream("localhost",6789)
    val result = lines.flatMap(_.split(" "))

    result.foreachRDD({ rdd =>
      val spark = SparkSession.builder().config(rdd.sparkContext.getConf).getOrCreate()
      import spark.implicits._

      val wordsDataFrame = rdd.toDF("word")

      wordsDataFrame.createOrReplaceTempView("words")

      val wordCountsDataFrame = spark.sql("select word ,count(1) as total from words group by word")

      wordCountsDataFrame.show()
    })

    ssc.start()
    ssc.awaitTermination()
  }
}
```

### 3.6 checkpointing
- 两种数据类型会被checkpoint，数据保存在HDFS中
	- 元数据：包括conf,DStream的算子操作，为完成的批次
	- 数据：一些有状态的RDD，比如有window，state之类的算子操作的RDD
- 使用checkpoint的前提还有就是需要driver失败后自动重启，这部分需要在部署时配置
- 业务逻辑需要写在functionToCreateContext函数，否则下次重启后报错
- 修改业务逻辑代码后，需要删除HDFS文件中checkpoint开头的文件 hadoop fs -rm /xx/check_point/checkpoint*
- checkpoint刷新时间是批处理时间间隔的5-10倍

### 3.7 累加器和广播变量
- 不可被checkpoint，解决办法是实例化，详情参看官网





## 4. Spark SQL, DataFrames and Datasets Guide

### 4.1 概述
- SparkSQL执行SQL的方式有两种:sql 和 DF API,其中无论使用SQL，DF API，DS API，底层执行引擎相同
- SQL,执行SQL语句会返还DF/DS
- Dataset也是一个分布式数据集合，会使用Encoder去序列化Java对象，Encoder可以保障一些操作如filter可以不用反序列化就可以进行
- DataFrame是一个Dataset组织成的指定列，相当与表 ？？

### 4.2 入门指南
- 标准操作代码
```
// 创建SparkSession
import org.apache.spark.sql.SparkSession
val spark = SparkSession
  .builder()
  .appName("Spark SQL basic example")
  .config("spark.some.config.option", "some-value")
  .enableHiveSupport()
  .getOrCreate()

// 创建DF
val df = spark.read.json("examples/src/main/resources/people.json")
df.show()

// 通过sql操作df
df.createOrReplaceTempView("people")

val sqlDF = spark.sql("SELECT * FROM people")
sqlDF.show()

//

```
- 创建temporary views，在这个session结束后会消失，但是global temporary views表会在spark application结束才会消失
- RDD转变为DataFrame的方法 ？？
	- 反射推测Schema，利用反射来推断包含特定类型对象的RDD的schema
	```
	import spark.implicits._

	case class Person(name: String, age: Long)

	val peopleDF = spark.sparkContext
		  .textFile("examples/src/main/resources/people.txt")
		  .map(_.split(","))
		  .map(attributes => Person(attributes(0), attributes(1).trim.toInt))
		  .toDF()

	```
	- 编程接口方式指定Schema,构造一个schema并将其应用在已知的RDD上，可以动态生成,不知道列和列的类型
	```
	步骤
	// 从RDD创建row RDD
    // 创建StructType类型的Schema
    // row RDD和Schema匹配

	import org.apache.spark.sql.types._

	val peopleRDD = spark.sparkContext.textFile("examples/src/main/resources/people.txt")

	val schemaString = "name age"

	val fields = schemaString.split(" ")
	  .map(fieldName => StructField(fieldName, StringType, nullable = true))
	val schema = StructType(fields)

	val rowRDD = peopleRDD
	  .map(_.split(","))
	  .map(attributes => Row(attributes(0), attributes(1).trim))

	val peopleDF = spark.createDataFrame(rowRDD, schema)

	```
### 4.3 数据源
- 标准读写外部数据源的方法
```
val peopleDF = spark.read.format("json").load("examples/src/main/resources/people.json")

peopleDF.select("name", "age").write.format("parquet").save("namesAndAges.parquet")
```
- SaveMode的四种模式,error,append,overwrite,ignore
- partiton中，如果read到partiton目录，就没有partiton这列，如果到上一级，就有partiton这列
- Hive
	- 使用hive首先在conf中配置hive-site.xml,其次在SparkSession中加入enableHiveSupport,还要加入MySQL的驱动
	- 基本操作
	```
	val spark = SparkSession
	  .builder()
	  .appName("Spark Hive Example")
	  .config("spark.sql.warehouse.dir", warehouseLocation)
	  .enableHiveSupport()
	  .getOrCreate()

	spark.sql("CREATE TABLE IF NOT EXISTS src (key INT, value STRING)")

	spark.sql("LOAD DATA LOCAL INPATH 'examples/src/main/resources/kv1.txt' INTO TABLE src")

	spark.sql("select * from src")

	```
- 通过JDBC读取其他databases
```
// 读取MySQL数据
val jdbcDF = spark.read
  .format("jdbc")
  .option("url", "jdbc:postgresql:dbserver")
  .option("dbtable", "schema.tablename")
  .option("user", "username")
  .option("password", "password")
  .load()

// 写入MySQL数据，参考ImoocLogApp的操作方法  

```
### 4.4 性能调优
- dataFrame.cache()缓存DF
- spark.sql.shuffle.partitions 设置partitions数

## 5. MLlib



