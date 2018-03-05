# BigDataTips

## Spark 

__1.SparkConf,SparkContext,SparkSession区别与联系__

 - SparkConf包含集群配置参数
 - SparkContext是Spark程序入口 
 - SparkSession是SQLContext和HiveContext的组合，并且封装了SparkContext
 - StreamingContext是Spark Streaming的入口，并没有封装SparkContext

 __2.DataFrame,DataSet,RDD区别和联系__
 
- RDD是弹性分布式数据集，创建的是Java对象，例如RDD[person]
- DataFrame是RDD基础上加入scheme，可以使用sql语句，但是与RDD相比丢失编译类型检查和面向对象;DataFrame表示为Dataset[Row]，即Dataset的子集
- DataSet,引入了Encoder,此外DataSet[T]中T是强类型的，需要指定
- Starting in Spark 2.0, Dataset takes on two distinct APIs characteristics: a strongly-typed API and an untyped API, as shown in the table below. Conceptually, consider DataFrame as an alias for a collection of generic objects Dataset[Row], where a Row is a generic untyped JVM object. Dataset, by contrast, is a collection of strongly-typed JVM objects, dictated by a case class you define in Scala or a class in Java.

 __3.spark运行流程__
 
- 在driver运行Spark Application，启动SparkContext
- SparkContext向yarn申请Executor资源
- SparkContext将应用程序分发给Executor
- Executor向SparkContext申请Task
- SparkContext构建DAG图，由DAG调度器根据shuffle将DAG图分解成Stage，包含了TaskSet，由Task调度器将TaskSet发送给executor
- task在Executor上运行，运行完释放所有资源

 __4.spark内存管理__
 
- 1.6之前是静态管理
	- executor分为堆内/堆外内存
	- 堆内内存的大小，由Spark应用程序启动时的–executor-memory设置，Task共享这部分内存。
	- 内存空间分配分为三个部分，Storage内存(0.6)：缓存RDD和广播变量;Exection内存(0.2):缓存Shuffle中的中间数据；其他(0.2),用户定义的数据结构，Spark内部数据等
	- 堆外内存是在工作节点的系统内存直接开辟，存储经过序列化的二进制序列，划分只有Storage内存和Exection内存，各占0.5。
 - 1.6之后是统一管理
	- Storage内存和Exection内存共享同一块空间，可以动态占用对方的空闲区域，两部分总共占比0.6，其他占比0.4
	- 堆外保持不变

__5.spark shuffle两种方法__ 

- Hash Based Shuffle
	- 0.8之前的Shuffle，会产生大量小文件，文件数量是number(map) * number(reduce)
	- 1.2后，引入Consolidate机制，每个core会产生一个文件，同一个core的Map Task任务产生的数据追加到这个文件中，则会产生number(cores) * number(reduce)
- Sort Based Shuffle(默认，从1.2开始)
	- 每个mapTask会按照key所对应的partition ID进行Sort，如果属于同一个Partition的Key，本身不进行Sort。如果内存不够用，他就会把那些已经排序的内容写到外部disk，结束的时候再进行归并排序，同时生成一个Index文件，reducer通过index取得所要的数据，产生文件2*Map
- 由于Hash Shuffle不排序，小数据量的时候比较快，用在数据量小不需要排序的场景

__5.Spark分区器HashPartitioner和RangePartitioner__

- 分区函数，决定RDD中分区的个数，也决定了Reduce个数，只在key/value中有用，主要提供了每个RDD有几个分区（numPartitions）以及对于给定的值返回一个分区ID（0~numPartitions-1），也就是决定这个值是属于那个分区的。
- HashPartitioner分区的原理很简单，对于给定的key，计算其hashCode，并除分区的个数取余
- RangePartitioner分区则尽量保证每个分区中数据量的均匀，而且分区与分区之间是有序的,简单的说就是将一定范围内的数映射到某一个分区内
	- 水塘抽样
	- 




 ## Hadoop
 __1.Hadoop1.0和2.0的区别__
 
 - Hadoop1.0，由HDFS和MapReduce组成，其中HDFS由一个NameNode和多个DateNode组成，MapReduce由一个JobTracker和多个TaskTracker组成。JobTracker负责任务调度管理，资源的分配和机器的运行情况，任务重。
 - Hadoop2.0为克服Hadoop1.0中的不足：针对Hadoop1.0单NameNode制约HDFS的扩展性问题，提出HDFS Federation，它让多个NameNode分管不同的目录进而实现访问隔离和横向扩展，同时彻底解决了NameNode单点故障问题；针对Hadoop1.0中的MapReduce在扩展性和多框架支持等方面的不足，它将JobTracker中的资源管理和作业控制分开，分别由ResourceManager（负责所有应用程序的资源分配）和ApplicationMaster（负责管理一个应用程序）实现，即引入了资源管理框架Yarn。同时Yarn作为Hadoop2.0中的资源管理系统，它是一个通用的资源管理模块，可以运行spark,storm,MR等应用。
 - 三个区别
	 - NameNode可以以集群方式部署，增强了NameNode的水平扩展能力
	 - jobTracker的功能拆分成资源管理RM，作业控制AM
	 - yarn是一个通用的资源管理模块

 __2.MapReduce2.0提交任务详解__
 
- 在client端提交job作业
- 找到一个NodeManager启动ApplicationMaster，并向ResourceManager(yarn)申请资源
- 资源以container封装，其他节点的NodeManager负责启动，监控管理容器，向RM汇报
- Task任务在其他container中执行

 __3.MapReduce原理过程__
 
- Input,Map，combine,Shuffle,reduce
- combine的作用是map端的reduce聚合;partition的作用是分区，知道key到哪一个reduce，原理是Hash值%reduce数目取模

 __4.HDFS系统结构__
 
- 采用Master-slaver模式，NameNode负责维护文件系统树，文件目录，数据集群的管理；dataNode存储数据，并定时向NN心跳反馈，发生存储数据的列表
- 客户端从NN获取元数据，与DN获取数据

 __5.ZK的leader选举算法__
 


 ## Hive
 __1.Hive SQL转化为MR的过程__
 
 - 主要步骤
	- 使用antlr完成对SQL的语法解析，将SQL转化为抽象语法树AST Tree
	- 遍历AST Tree，生成执行操作树OperatorTree，hive操作符是hive对表数据的逻辑处理
	- 逻辑层优化器对OperatorTree进行优化，与物理优化相比，一是对操作符级别的调整，二是优化不针对特定计算引擎
	- 遍历OperatorTree，划分成若干Task，翻译成MR任务
	- 物理优化器根据各计算引擎的特定，对MR任务优化，最终生成执行计划，执行Task任务

__2.sort by和order by区别__

- order by是全局排序，只有一个reducer;sort by不是全局排序，只在数据进入reducer之前完成排序



 

