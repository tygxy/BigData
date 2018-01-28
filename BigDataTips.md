# BigDataTips

## Spark 

__1.SparkConf,SparkContext,SparkSession区别与联系__
 - SparkConf包含集群配置参数
 - SparkContext是Spark程序入口
 - SparkSession是SQLContext和HiveContext的组合，并且封装了SparkContext
 - StreamingContext是Spark Streaming的入口，并没有封装SparkContext

 __2.DataFrame,DataSet,RDD区别和联系
 - RDD是弹性分布式数据集，创建的是Java对象，例如RDD[person]
 - DataFrame是RDD基础上加入scheme，可以使用sql语句，但是与RDD相比丢失编译类型检查和面向对象;DataFrame表示为Dataset[Row]，即Dataset的子集
 - DataSet,引入了Encoder,此外DataSet[T]中T是强类型的，需要指定
 - Starting in Spark 2.0, Dataset takes on two distinct APIs characteristics: a strongly-typed API and an untyped API, as shown in the table below. Conceptually, consider DataFrame as an alias for a collection of generic objects Dataset[Row], where a Row is a generic untyped JVM object. Dataset, by contrast, is a collection of strongly-typed JVM objects, dictated by a case class you define in Scala or a class in Java.

 ## Hadoop
 __1.Hadoop1.0和2.0的区别
 - Hadoop1.0，由HDFS和MapReduce组成，其中HDFS由一个NameNode和多个DateNode组成，MapReduce由一个JobTracker和多个TaskTracker组成。JobTracker负责任务调度管理，资源的分配和机器的运行情况，任务重。
 - Hadoop2.0为克服Hadoop1.0中的不足：针对Hadoop1.0单NameNode制约HDFS的扩展性问题，提出HDFS Federation，它让多个NameNode分管不同的目录进而实现访问隔离和横向扩展，同时彻底解决了NameNode单点故障问题；针对Hadoop1.0中的MapReduce在扩展性和多框架支持等方面的不足，它将JobTracker中的资源管理和作业控制分开，分别由ResourceManager（负责所有应用程序的资源分配）和ApplicationMaster（负责管理一个应用程序）实现，即引入了资源管理框架Yarn。同时Yarn作为Hadoop2.0中的资源管理系统，它是一个通用的资源管理模块，可以运行spark,storm,MR等应用。

