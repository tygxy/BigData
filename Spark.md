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
	



