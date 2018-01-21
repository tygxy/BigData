# 《Spark快速大数据分析》

## 执行spark程序
- scala
```
spark-submit --master yarn-cluster  --driver-memory 8G --num-executors 100 --executor-cores 5 --executor-memory 4G  --class NewsRecommend  ./NewsRecommend.jar
```

## 第一章 Spark数据分析导论

- Spark各组件的组成 
	- SparkSQL/Spark Streaming/MLib/GraphX

## 第二章 Spark下载与入门

- Spark应用通过驱动器程序(driver program)发起集群上的操作，管理多个执行器(executor)节点，一般是SparkContext
- bin/spark-sumbit 运行相关jar包
- 初始化SparkContext的方法(python版)
	- from pyspark import SparkConf, SparkContext
	- conf = SparkConf().setMaster("local").setAppName("MyApp") # 设置集群URL和应用名

## 第三章 RDD编程

- 对RDD的操作为转换操作(transformation)和行动操作(action)
- 创建RDD 
	- 读取外部数据
		- text = sc.text("file:///usr/local/spark/mycode/word.txt") # 本地
		- text = sc.text("/user/hadoop/word.txt") # HDFS
	- 创建集合传入sc.parallelize()中
	- 写入外部
		- text.saveAsTextFile('/user/hadoop/outputFile')
- RDD的转换操作
	- RDD.map(fun)
	- RDD.filter(fun)
	- RDD.faltMap(fun) 
	- RDD1.union(RDD2) # 必须类型数据相同
	- RDD1.intersection(RDD2) # 交集，必须类型数据相同
	- RDD.distinct() # 去重
- RDD的行动操作
	- RDD.reduce(fun)
	- RDD.fold(zero)(fun)
	- RDD.aggregate(zeroValue)(seqOp,combOp)
	- RDD.collect() # 收集
	- RDD.take(num) 
	- RDD.foreach(fun)
	- RDD.count()
- RDD的持久化
	- RDD.persist() # 缓存在内存中

## 第四章 键值对RDD的操作

- 创建PairRDDr
	- RDD.map(lambda x: (x.split(" ")[0], x.split(" ")[1])) # 通过map创建键值对
- 常见操作
	- reduceByKey(func)
	- groupByKey()
	- mapValues(func)
	- keys()
	- values()
	- sortByKey()
	- combineByKey()
	- join(otherRDD)

- 数据分区
- example
```python

# wordCount
text = sc.textFile('test.md')
word = text.flatMap(lambda x: x.split(" "))
count = word.map(lambda x: (x, 1)).reduceByKey(lambda x, y: x + y)
count.saveAsTextFile('/user/hadoop/outputFile1')

# 其他操作
text = sc.textFile('test1')
pairRDD = text.map(lambda x: (x.split(",")[0], int(x.split(",")[1])))

# mapValues reduceByKey
result = pairRDD.mapValues(lambda x: (x, 1)).reduceByKey(lambda x, y: (x[0] + y[0], x[1] + y[1]))

# groupByKey
result = pairRDD.groupByKey().map(lambda x: (x[0], list(x[1]))).collect()

result.saveAsTextFile('/user/hadoop/outputFile2')

# 计算平均值
data = sc.parallelize(Array(("spark",2), ("hadoop",6), ("hadoop",4), ("spark",6)))

data1 = data.mapValues(lambda x: (x, 1)).reduceByKey(lambda x, y: x[0] + y[0], y[1] + y[1])

result = data1.mapValues(lambda x: x[0]/ x[1]).collect()

for line in result:
	print line
```

# 《Spark技术内幕》

## 第1章 spark简介
- 深入理解driver(主控程序，提交job,将job转换成Task,协调executor中Task调度),cluster manager,executor,job,stages,task的关系
- applicaiton(用户写的应用程序),SparkContext(向cluster manager申请资源)
## 第2章 spark学习环境的搭建
## 第3张 rdd实现详解
- 分布式数据集的容错性有两种方式：数据检查点和记录数据的更新（17页）
- RDD是只读，分区的集合；一个分区就是一个Task处理的基本单元，分区决定并行计算的颗粒，partition和task数量一致
- partitioner是RDD的分区函数，一种是基于哈希的HashPartitioner;一种是基于范围的RangePartitioner,并且分区函数只对key-value的RDD
- reduceByKey和groupByKey区别
	- ReduceByKey有本地merge，但是groupByKey是所有键值对都需要移动；此外groupByKey不能接受自定义函数，只能分组后再map,但是ReduceByKey后面可以接自定义函数
- 常见算子
	- map,filter,flatMap,union,distinct,groupByKey,reduceByKey,sortByKey,join
	- reduce,collect,count,take,saveAsTextFile,countByKey,foreach
- 根据计算逻辑生成DAG
- 宽依赖，窄依赖
	- 每个parent RDD的partition最多被子RDD的一个partition使用，即为窄依赖
	- 子RDD的partition依赖多个parent RDD的partition，即为宽依赖，需要shuffle
- 划分stage的依据 是否有shuffle过程(是否有宽依赖)，
- 划分job的依据 是否有action动作
## 第4章 Scheduler(任务调度)模块详解
- stage划分
	- 划分依据 shuffle
	- 划分过程从一个job的最后一个RDD开始，根据它的依赖关系，倒着往前推
- Task是集群运行的基本单元，有ShuffleMapTask和ResultTask
## 第5章 Deploy模块详解
- Cluster Manager部署方式有standalone,local,yarn,EC2,Mesos等
- yarn Cluster模式，yarn Client模式，区别在于用户提交的application的spark Context在本机运行，适合application与本地有交互的场景
## 第6章 Executor模块详解
- 对于同一个application，在一个worker上只有一个executor,带不代表一个物理节点只有一个executor，可以在一个物理节点部署多个worker
- 参数设置 spark.executor.memory 最多使用内存大小，每个executor上支持的任务数量取决于executor持有的CPU core的数量
- 一个Executor内同一时刻可以并行执行的Task数由总CPU数／每个Task占用的CPU数决定，即spark.executor.cores / spark.task.cpus
## 第7章 Shuffle模块详解
- spark.shuffle.manage
	- 两种shuffle方式，hash based shuffle和sort based shuffle
- spark.shuffle.spill
	- 默认是true，指shuffle过程中如果内存中数据超过阈值，是否将部分数据临时存入外部存储
- spark.shuffle.memoryFraction
	- 当shuffle过程中内存达到总内存的多少比例时开始spill，相当于设置shuffle站内存大小
## 第8章 Storage模块详解
- 存储级别
	- disk_only,memory_only,memory_only_ser,memory_and_disk,memory_and_disk_ser
- spark.storage.memoryFraction
	- 决定内存中有多少用于RDD cache，默认0.6

# 《Spark性能优化指南-初级篇/高级篇》
- 初级篇 https://tech.meituan.com/spark-tuning-basic.html
- 高级篇 https://tech.meituan.com/spark-tuning-pro.html
## 初级篇
- 开发调优
	- 持久化选择的策略 memory -> memory_ser -> memory_and_disk_ser
	- 尽量少用shuffle算子，尽量使用有预聚合的shuffle算子(reduceByKey)
	- 使用高性能的算子
		- 使用reduceByKey/aggregateByKey替代groupByKey
		- 使用filter之后进行coalesce操作
		- 使用repartitionAndSortWithinPartitions替代repartition与sort类操作(官方建议，如果需要在repartition重分区之后，还要进行排序)
		- 使用mapPartitions替代普通map/使用foreachPartitions替代foreach(谨慎使用)
	- 广播大变量 算子使用外部大变量时，使用广播保证每个executor有一份变量由task共用
- 资源调优
	- 一个CPU core同一时间只能执行一个线程。而每个Executor进程上分配到的多个task，都是以每个task一条线程的方式，多线程并发运行的
	- 每一台host上面可以并行N个worker，每一个worker下面可以并行M个executor，task们会被分配到executor上面 去执行
	- num-executors 设置executor数量，50-100
	- executor-memory executor内存，4-8G
	- executor-cores executor核数 2-4 决定了每个Executor进程并行执行task线程的能力
	- driver-memory driver进程的内存
	- spark.default.parallelism 500-1000 该参数用于设置每个stage的默认task数量，设置该参数为num-executors * executor-cores的2~3倍较为合适
	- spark.storage.memoryFraction RDD持久化数据在Executor内存中能占的比例
	- spark.shuffle.memoryFraction shuffle占内存的比例
	- spark.sql.shuffle.partitions sql的并行度设置
	- 参考
	```
	./bin/spark-submit \
	  --master yarn-cluster \
	  --num-executors 100 \
	  --executor-memory 6G \
	  --executor-cores 4 \
	  --driver-memory 1G \
	  --conf spark.default.parallelism=1000 \
	  --conf spark.storage.memoryFraction=0.5 \
	  --conf spark.shuffle.memoryFraction=0.3 \
	```
## 高级篇
- 数据倾斜 根据stage定位到出现倾斜的位置，通过抽样查看key的分布(rdd1.sample(false, 0.1).countByKey())，来确定解决方案
	- 使用Hive ETL预处理数据(将shuffle过程转给ETL)
	- 过滤少数导致倾斜的key
	- 提高shuffle操作的并行度，设置多task去执行任务，但是如果某个key量特别大，那还是会在一个task中执行任务，没法根本解决数据倾斜
	- 【聚合类】两阶段聚合（局部聚合+全局聚合）
		- 试用场景：对RDD执行reduceByKey等聚合类shuffle算子或者在Spark SQL中使用group by语句进行分组聚合时
		- 解决思路：对key赋值随机前缀 -> 聚合 -> 去掉前缀 -> 聚合
	- 【join类】将reduce join转为map join
		- 试用场景：join操作中的一个RDD或表的数据量比较小（比如几百M或者一两G）
		- 解决思路：driver去collect较小的rdd1 -> Broadcast(rdd1) -> 大rdd2去map匹配广播变量(首先读取广播变量->创建map1保存rdd1的值->获取当前rdd2的k/v->从map1取出)
		- 注：上述仅仅适用于小rdd中的key没有重复，全部是唯一的场景，如果rdd1中有多个相同的key，那么就得用flatMap类的操作，在进行join的时候不能用map，而是得遍历rdd1所有数据进行join，rdd2中每条数据都可能会返回多条join后的数据
	- 【join类】采样倾斜key并分拆join操作
		- 试用场景：两个rdd都比较大，其中一个rdd少数key量大，另外一个rdd分布均匀
		- 解决思路：rdd1中出现倾斜的key单独取出->随机前缀->rdd2中这些key也取出->每条数据膨胀n倍，按顺序加前缀->join后，去掉前缀->其他key正常join->union
	- 【join类】使用随机前缀和扩容RDD进行join
	 	- 试用场景：两个rdd都比较大，一个rdd大数key都倾斜，另外一个rdd分布均匀
		- 解决思路：同上
	- 【聚合类】去重+聚合转为reduceByKey
		- 试用场景：按key分组，组内需要对type做去重后再聚合，某些key数据倾斜
		- 解决思路：构造(key+type,1)->reduceBykey->(key,1)->reduceBykey
- shuffle调优
	- shuffle计算引擎分为HashShuffleManager和SortShuffleManager，区别在于前者会产生大量中间磁盘文件，进而由大量的磁盘IO操作影响了性能；后者会合并成一个磁盘文件，在下一个Stage中的Shuffle Read拉数据时，可以索引部分数据即可
	

# 《Spark Official Document》

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



# 《以慕课网日志分析为例 进入大数据Spark SQL的世界》
## 1.初探大数据
- hadoop包括HDFS，yarn(资源调度)，mapreduce
- hdfs 
   - NN：
  1）负责客户端请求的响应
  2）负责元数据（文件的名称、副本系数、Block存放的DN）的管理
   - DN：
  1）存储用户的文件对应的数据块(Block)
  2）要定期向NN发送心跳信息，汇报本身及其所有的block信息，健康状况
- yarn
   - ResourceManager的职责：一个集群active状态的RM只有一个，负责整个集群的资源管理和调度
     1）处理客户端的请求(启动/杀死)
     2）启动/监控ApplicationMaster(一个作业对应一个AM)
     3）监控NM
     4）系统的资源分配和调度
   - NodeManager：整个集群中有N个，负责单个节点的资源管理和使用以及task的运行情况
        1）定期向RM汇报本节点的资源使用请求和各个Container的运行状态
        2）接收并处理RM的container启停的各种命令
        3）单个节点的资源管理和任务管理
   - ApplicationMaster：每个应用/作业对应一个，负责应用程序的管理
        1）数据切分
        2）为应用程序向RM申请资源(container)，并分配给内部任务
        3）与NM通信以启停task， task是运行在container中的
        4）task的监控和容错
   - YARN执行流程
        1）用户向YARN提交作业
        2）RM为该作业分配第一个container(AM)
        3）RM会与对应的NM通信，要求NM在这个container上启动应用程序的AM
        4) AM首先向RM注册，然后AM将为各个任务申请资源，并监控运行情况
        5）AM采用轮训的方式通过RPC协议向RM申请和领取资源
        6）AM申请到资源以后，便和相应的NM通信，要求NM启动任务
        7）NM启动我们作业对应的task
       
## 2.Spark及其生态圈概述
- MapReduce的局限性：
        1）代码繁琐；
        2）只能够支持map和reduce方法；
        3）执行效率低下，中间数据要落磁盘；
        4）不适合迭代多次、交互式、流式的处理；
        
## 3.DataFrame和DataSet
- 概念
    - A Dataset is a distributed collection of data：分布式的数据集
    - A DataFrame is a Dataset organized into named columns. （RDD With schema）以列（列名、列的类型、列值）的形式构成的分布式数据集，按照列赋予不同的名称
- API
    - printSchema,select,filter,show,groupBy,count,sort,take
    - DataFrame和RDD互操作的两种方式：
            1）反射：case class   前提：事先需要知道你的字段、字段类型    
            2）编程：Row          如果第一种情况不能满足你的要求（事先不知道列）



# 《Spark Streaming实时流处理项目实战》
## 1.初识实时流处理
- 主流实时流处理框架：Storm,Spark Streaming,Kafka,Flink
- 实时流式处理框架 app/web -> webServer -> Flume -> Kafka -> Storm/Spark -> RDBMS/NoSQL

## 2.分布式日志收集框架Flume
- Flume是分布式的日志收集，聚合，移动系统
- Flume架构及核心组件  agent:source(收集),channel(聚集，缓存),sink（输出）
- 安装过程
```
安装jdk
	下载
	解压到~/app
	将java配置系统环境变量中: ~/.bash_profile	
		export JAVA_HOME=/home/hadoop/app/jdk1.8.0_144
		export PATH=$JAVA_HOME/bin:$PATH
	source下让其配置生效
	检测: java  -version

安装Flume
	下载
	解压到~/app
	将java配置系统环境变量中: ~/.bash_profile	
		export FLUME_HOME=/home/hadoop/app/apache-flume-1.6.0-cdh5.7.0-bin
		export PATH=$FLUME_HOME/bin:$PATH
	source下让其配置生效	
	flume-env.sh的配置：export JAVA_HOME=/home/hadoop/app/jdk1.8.0_144
	检测: flume-ng version
```
- Flume的使用
	- 需要配置source,channel,sink,再把三个串起来
	- a1: agent名称 r1: source的名称 k1: sink的名称 c1: channel的名称
	- 例子1：从指定网络端口收集数据输出到控制台
	```
	// 配置文件例子

	# Name the components on this agent
	a1.sources = r1
	a1.sinks = k1
	a1.channels = c1

	# Describe/configure the source
	a1.sources.r1.type = netcat
	a1.sources.r1.bind = localhost
	a1.sources.r1.port = 44444

	# Describe the sink
	a1.sinks.k1.type = logger

	# Use a channel which buffers events in memory
	a1.channels.c1.type = memory

	# Bind the source and sink to the channel
	a1.sources.r1.channels = c1
	a1.sinks.k1.channel = c1

	// 启动agent
	flume-ng agent --name a1 --conf $FLUME_HOME/conf --conf-file $FLUME_HOME/conf/example.conf -Dflume.root.logger=INFO,console
	```
	- 例子2：A服务器的日志实时采集到B服务器
	```
	// 两份conf配置
    // 配置一
	exec-memory-avro.conf

	exec-memory-avro.sources = exec-source
	exec-memory-avro.sinks = avro-sink
	exec-memory-avro.channels = memory-channel

	exec-memory-avro.sources.exec-source.type = exec
	exec-memory-avro.sources.exec-source.command = tail -F /home/hadoop/data/data.log
	exec-memory-avro.sources.exec-source.shell = /bin/sh -c

	exec-memory-avro.sinks.avro-sink.type = avro
	exec-memory-avro.sinks.avro-sink.hostname = hadoop000
	exec-memory-avro.sinks.avro-sink.port = 44444

	exec-memory-avro.channels.memory-channel.type = memory

	exec-memory-avro.sources.exec-source.channels = memory-channel
	exec-memory-avro.sinks.avro-sink.channel = memory-channel
 
    // 配置二
    avro-memory-logger.conf

	avro-memory-logger.sources = avro-source
	avro-memory-logger.sinks = logger-sink
	avro-memory-logger.channels = memory-channel

	avro-memory-logger.sources.avro-source.type = avro
	avro-memory-logger.sources.avro-source.bind = hadoop000
	avro-memory-logger.sources.avro-source.port = 44444

	avro-memory-logger.sinks.logger-sink.type = logger

	avro-memory-logger.channels.memory-channel.type = memory

	avro-memory-logger.sources.avro-source.channels = memory-channel
	avro-memory-logger.sinks.logger-sink.channel = memory-channel

	// 启动agent

	//先启动avro-memory-logger
	flume-ng agent \
	--name avro-memory-logger  \
	--conf $FLUME_HOME/conf  \
	--conf-file $FLUME_HOME/conf/avro-memory-logger.conf \
	-Dflume.root.logger=INFO,console

    // 再启动exec-memory-avro.conf
	flume-ng agent \
	--name exec-memory-avro  \
	--conf $FLUME_HOME/conf  \
	--conf-file $FLUME_HOME/conf/exec-memory-avro.conf \
	-Dflume.root.logger=INFO,console
	```
- event是flume数据传输的基本单元，由可选的header+ byte array构成，例如Event: { headers:{} body: 68 65 6C 6C 6F 0D hello.}

## 3.分布式消息队列Kafka
- kafka架构,可以运行在集群中
	- producer:生产者
	- consumer:消费者
	- broker：一个kafka
	- topic：主题，给消息一个标签

- 安装配置kafka
	- 安装zookeeper,配置环境变量，修改conf/zk-cfg中的dataDir
	- 安装kafka,配置环境变量,修改config/server.properties中的broker.id,listeners,host.name,log.dirs,zookeeper.connect

- 单节点单broker的部署和使用
	- 启动kafka kafka-server-start.sh $KAFKA_HOME/config/server.properties
	- 创建topic(zk)  kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic hello_topic
	- 查看topic(zk)  kafka-topics.sh --list --zookeeper localhost:2181
	- 发送消息(broker)  kafka-console-producer.sh --broker-list hadoop000:9092 --topic hello_topic
	- 消费消息(zk)  kafka-console-consumer.sh --zookeeper hadoop000:2181 --topic hello_topic 
	
- Flume+kafka整合
	- 启动zookeeper  bin/zkServer.sh start 
	- 启动kafka kafka-server-start.sh $KAFKA_HOME/config/server.properties
	- 启动Flume
	```
    // 技术选型
       exec - memeory - avro
       avro - memeory - kafka

	// Flume 1: exec-memory-avro.conf

	exec-memory-avro.sources = exec-source
	exec-memory-avro.sinks = avro-sink
	exec-memory-avro.channels = memory-channel

	exec-memory-avro.sources.exec-source.type = exec
	exec-memory-avro.sources.exec-source.command = tail -F /Users/guoxingyu/data/data.log
	exec-memory-avro.sources.exec-source.shell = /bin/sh -c

	exec-memory-avro.sinks.avro-sink.type = avro
	exec-memory-avro.sinks.avro-sink.hostname = localhost
	exec-memory-avro.sinks.avro-sink.port = 44444

	exec-memory-avro.channels.memory-channel.type = memory

	exec-memory-avro.sources.exec-source.channels = memory-channel
	exec-memory-avro.sinks.avro-sink.channel = memory-channel

	// Flume 2: avro-memory-kafka.conf

	avro-memory-kafka.sources = avro-source
	avro-memory-kafka.sinks = kafka-sink
	avro-memory-kafka.channels = memory-channel

	avro-memory-kafka.sources.avro-source.type = avro
	avro-memory-kafka.sources.avro-source.bind = localhost
	avro-memory-kafka.sources.avro-source.port = 44444

	#avro-memory-kafka.sinks.kafka-sink.type = logger
	avro-memory-kafka.sinks.kafka-sink.type = org.apache.flume.sink.kafka.KafkaSink
	avro-memory-kafka.sinks.kafka-sink.brokerList = localhost:9092
	avro-memory-kafka.sinks.kafka-sink.topic = hello_topic  
	avro-memory-kafka.sinks.kafka-sink.batchSize = 5
	avro-memory-kafka.sinks.kafka-sink.requireAcks =1


	avro-memory-kafka.channels.memory-channel.type = memory

	avro-memory-kafka.sources.avro-source.channels = memory-channel
	avro-memory-kafka.sinks.kafka-sink.channel = memory-channel

    // 按顺序启动flume 

    flume-ng agent \
	--name avro-memory-kafka  \
	--conf $FLUME_HOME/conf  \
	--conf-file $FLUME_HOME/conf/avro-memory-kafka.conf \
	-Dflume.root.logger=INFO,console


	flume-ng agent \
	--name exec-memory-avro  \
	--conf $FLUME_HOME/conf  \
	--conf-file $FLUME_HOME/conf/exec-memory-avro.conf \
	-Dflume.root.logger=INFO,console

	```
	- 启动一个消费者监听hello_topic  
	       - kafka-console-consumer.sh --zookeeper hadoop000:2181 --topic hello_topic 

- 安装HBase
	- 配置conf/hbase-env.sh 中java地址和修改HBASE_MANAGES_ZK=false
	- 配置conf/hbase-xml
	```
	<configuration>
        <property>
            <name>hbase.rootdir</name>
            <value>hdfs://localhost:8020/hbase</value>
        </property>

        <property>
            <name>hbase.cluster.distributed</name>
            <value>true</value>
        </property>

        <property>
            <name>hbase.zookeeper.quorum</name>
            <value>localhost:2181</value>
        </property>
	</configuration>
	```
	- 配置conf/regionservers改为localhost

## 4.Spark Streaming
- 工作原理
	- StreamingContext会启动一些Executor作为receiver去接收实时数据流，把数据按照指定的时间段切成一片片小的数据块，然后把小的数据块传给SparkContext处理RDD
- 核心概念
	- StreamingContext是Streaming程序入口，指定conf和批次间隔
	- DStreams代表一系列的RDDs,每个RDD都包含一个时间间隔内的数据；对DStreams做操作，本质是对每个RDDs做操作
	- Input DStream和Receivers，除了文件系统，每个Input DStream都需要和一个Receivers相关联，用来接受数据源的数据并存在内存中
- DStreams的算子
	- 带状态的算子






