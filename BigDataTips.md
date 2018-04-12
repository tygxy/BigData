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
 
- 在driver运行Spark Application，启动SparkContext，driver节点可以需要与executor通信，rpc等，所以需要配置内存
- SparkContext向yarn申请Executor资源
- SparkContext将应用程序分发给Executor
- Executor向SparkContext申请Task
- SparkContext构建DAG图，由DAG调度器根据shuffle将DAG图分解成Stage，包含了TaskSet，由Task调度器将TaskSet发送给executor
- task在Executor上运行，运行完释放所有资源

 __4.spark内存管理__
 
- 1.6之前是静态管理
	- executor分为堆内/堆外内存，堆外内存是序列化后的
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
- 两个分区函数继承抽样类partitioner,实现两个方法，获取分区数和根据key计算出属于哪个分区
- HashPartitioner分区的原理很简单，对于给定的key，计算其hashCode，并除分区的个数取余
![](resource/hashpartition.jpg?raw=true)
- RangePartitioner分区则尽量保证每个分区中数据量的均匀，而且分区与分区之间是有序的,简单的说就是将一定范围内的数映射到某一个分区内，主要用在sortByKey这种需要RDD数据排序的分区,该分区器要求RDD中的KEY类型必须是可以排序的
	- 两个步骤，先重整个RDD中抽取出样本数据，将样本数据排序，计算出每个分区的最大key值，形成一个Array[KEY]类型的数组变量rangeBounds
	- 判断key在rangeBounds中所处的范围，给出该key值在下一个RDD中的分区id下标
- 参考 https://www.cnblogs.com/liuming1992/p/6377540.html

__6.数据倾斜__

- join
	- 将reduce join转为map join
	- 使用随机前缀和扩容RDD进行join
- 聚合
	- 两阶段聚合（局部聚合+全局聚合）
	- 去重+聚合转为reduceByKey

__7.Hadoop和Spark区别__
	- MapReduce中计算结果需要落地，Spark把中间数据放到内存中，迭代运算效率高。
	- Spark算子更丰富
	- Spark容错性高，Spark引进了弹性分布式数据集RDD的抽象，如果数据集一部分丢失，则可以根据“血统”（即充许基于数据衍生过程）对它们进行重建。另外在RDD计算时可以通过CheckPoint来实现容错，而CheckPoint有两种方式：CheckPoint Data，和Logging The Updates，用户可以控制采用哪种方式来实现容错

__8.spark和Mapreduce为什么快__
	- 内存计算，带来了更高的迭代运算效率，多个任务之间数据通信可以不需要借助硬盘而是通过内存，而hadoop由于本身的模型特点，多个任务之间数据通信是必须借助硬盘落地的。
	- 基于DAG的任务调度执行机制 
	- 多线程调度，Hadoop每次MapReduce操作，启动一个Task便会启动一次JVM，基于进程的操作。而Spark每次MapReduce操作是基于线程的，只在启动Executor是启动一次JVM，内存的Task操作是在线程复用的。


 ## Hadoop
 __1.Hadoop1.0和2.0的区别__
 
 - Hadoop1.0，由HDFS和MapReduce组成，其中HDFS由一个NameNode和多个DateNode组成，MapReduce由一个JobTracker和多个TaskTracker组成。JobTracker负责任务调度管理，资源的分配和机器的运行情况，任务重。
 - Hadoop2.0为克服Hadoop1.0中的不足：针对Hadoop1.0单NameNode制约HDFS的扩展性问题，提出HDFS Federation，它让多个NameNode分管不同的目录进而实现访问隔离和横向扩展，同时彻底解决了NameNode单点故障问题；针对Hadoop1.0中的MapReduce在扩展性和多框架支持等方面的不足，它将JobTracker中的资源管理和作业控制分开，分别由ResourceManager（负责所有应用程序的资源分配）和ApplicationMaster（负责管理一个应用程序）实现，即引入了资源管理框架Yarn。同时Yarn作为Hadoop2.0中的资源管理系统，它是一个通用的资源管理模块，可以运行spark,storm,MR等应用。
 - 三个区别
	 - NameNode可以以集群方式部署，增强了NameNode的水平扩展能力
	 - jobTracker的功能拆分成资源管理RM，作业控制AM
	 - yarn是一个通用的资源管理模块
- 改进
	- HDFS单一名称节点，存在单点失效问题；设计了HDFS HA，提供名称节点热备机制
	- HDFS单一命名空间，无法实现资源隔离；设计了HDFS Federation，管理多个命名空间
	- MR资源管理效率低，设计了新的资源管理框架YARN

- HDFS2.0 HA
	- HA集群设置两个名称节点，“活跃（Active）”和“待命（Standby”，两种名称节点的状态同步，可以借助于一个共享存储系统来实现(NFS，QJM等)，一旦活跃名称节点出现故障，就可以立即切换到待命名称节点，Zookeeper确保一个名称节点在对外服务，名称节点维护映射信息，数据节点同时向两个名称节点汇报信息

- HDFS Federation
	- 在HDFS Federation中，设计了多个相互独立的名称节点，使得HDFS的命名服务能够水平扩展，这些名称节点分别进行各自命名空间和块的管理，相互之间是联盟（Federation）关系，不需要彼此协调。并且向后兼容
	- HDFS Federation中，所有名称节点会共享底层的数据节点存储资源，数据节点向所有名称节点汇报
	- 属于同一个命名空间的块构成一个“块池”


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

__5.HDFS读写操作__

- 文件读取过程，一般的文件读取操作包括：open 、read、close等
	- 使用HDFS提供的客户端开发库，向远程的Namenode发起RPC请求
	- Namenode会视情况返回文件的部分或者全部block列表，对于每个block，Namenode都会返回持有该block拷贝的datanode地址
	- 客户端开发库会选取离客户端最接近的datanode来读取block
	- 读取完当前block的数据后，关闭与当前的datanode连接，并为读取下一个block寻找最佳的datanode
	- 当读完列表的block后，且文件读取还没有结束，客户端开发库会继续向Namenode获取下一批的block列表
	- 读取完一个block都会进行checksum验证，如果读取datanode时出现错误，客户端会通知Namenode，然后再从下一个拥有该block拷贝的datanode继续读
- 写文件过程，数据的写入过程 一般文件写入操作不外乎create、write、close几种
	- 使用HDFS提供的客户端开发库，向远程的Namenode发起RPC请求；
	- Namenode会检查要创建的文件是否已经存在，创建者是否有权限进行操作，成功则会为文件创建一个记录，否则会让客户端抛出异常；
	- 当客户端开始写入文件的时候，开发库会将文件切分成多个packets（信息包），并在内部以"data queue"的形式管理这些packets，并向Namenode申请新的blocks，获取用来存储replicas（复制品）的合适的datanodes列表，列表的大小根据在Namenode中对replication的设置而定。
	- 开始以pipeline（管道）的形式将packet写入所有的replicas中。开发库把packet以流的方式写入第一个datanode，该datanode把该packet存储之后，再将其传递给在此pipeline（管道）中的下一个datanode，直到最后一个datanode，这种写数据的方式呈流水线的形式。
	- 最后一个datanode成功存储之后会返回一个ack packet，在pipeline里传递至客户端，在客户端的开发库内部维护着"ack queue"，成功收到datanode返回的ack packet后会从"ack queue"移除相应的packet。
	- 如果传输过程中，有某个datanode出现了故障，那么当前的pipeline会被关闭，出现故障的datanode会从当前的pipeline中移除，剩余的block会继续剩下的datanode中继续以pipeline的形式传输，同时Namenode会分配一个新的datanode，保持replicas设定的数量。


__6.HDFS分布式文件系统NameNode和Secondary NameNode__

- NameNode启动时如何维护元数据
	- Edits文件：NameNode在本地操作系统的文件都会保存在Edits日志文件中。也就是说当文件系统中的任何元数据产生操作时，都会记录在Edits日志文件中
	- FsImage映像文件：整个文件系统的名字空间，包括数据块到文件的映射，文件的属性等等，都存储在一个称为FsImage的文件中，这个文件也是放在NameNode所在的文件系统中。
	- 流程介绍：
		- 加载fsimage映像文件到内存
		- 加载edits文件到内存
		- 在内存将fsimage映像文件和edits文件进行合并
		- 将合并后的文件写入到fsimage中
		- 清空原先edits中的数据，使用一个空的edits文件进行正常操作

- Secondary NameNode和NameNode区别
	- NameNode：存储文件的metadata，运行时所有数据都保存在内存中，这个的HDFS可存储的文件受限于NameNode的内存。NameNode失效则整个HDFS都失效了，所以要保证NameNode的可用性
	- Secondary NameNode：SecondNamenode是对主Namenode的一个补充，它会周期的执行对HDFS元数据的检查点。定时与NameNode进行同步，定期的将fsimage映像文件和Edits日志文件进行合并，并将合并后的传入给NameNode，替换其镜像，并清空编辑日志。如果NameNode失效，需要手动的将其设置成主机。NameNode可以在需要的时候应用Secondary NameNode上的检查点镜像
	- Namenode的edits文件过大的问题，也就是SecondeNamenode要解决的主要问题。SecondNamenode会按照一定规则被唤醒，然后进行fsimage文件与edits文件的合并，防止edits文件过大，导致Namenode启动时间过长。
	- checkpoint流程
		- NameNode通知Secondary NameNode进行checkpoint。
		- Secondary NameNode通知NameNode切换edits日志文件，使用一个空的。
		- Secondary NameNode通过Http获取NmaeNode上的fsimage映像文件和切换前的edits日志文件。
		- Secondary NameNode在内容中合并fsimage和Edits文件。
		- Secondary NameNode将合并之后的fsimage文件发送给NameNode。
		- NameNode用Secondary NameNode 传来的fsImage文件替换原先的fsImage文件。

- Hadoop 2.0 HA
	- 主备切换控制器 ZKFailoverController：ZKFailoverController 作为独立的进程运行，对 NameNode 的主备切换进行总体控制。ZKFailoverController 能及时检测到NameNode的健康状况，在主NameNode故障时借助Zookeeper 实现自动的主备选举和切换，当然NameNode目前也支持不依赖于Zookeeper的手动主备切换
	- 同时启动NameNode,其中一个处于active工作状态，另外一个处于随时待命standby状态。这样，当一个NameNode所在的服务器宕机时，可以在数据不丢失的情况下，手工或者自动切换到另一个NameNode提供服务。
	- 这些NameNode之间通过共享数据，保证数据的状态一致。多个NameNode之间共享数据，可以通过Network File System或者Quorum Journal Node。前者是通过Linux共享的文件系统，属于操作系统的配置；后者是Ｈadoop自身的东西，属于软件的配置.
	- Quorum Journal Node:会通过一组称作JournalNodes的独立进程进行相互通信。当active状态的NameNode的命名空间有任何修改时，会告知大部分的JournalNodes进程。standby状态的NameNode有能力读取JNs中的变更信息，并且一直监控edit log的变化，把变化应用于自己的命名空间。standby可以确保在集群出错时，命名空间状态已经完全同步了.
	- 为了确保快速切换，standby状态的NameNode有必要知道集群中所有数据块的位置。为了做到这点，所有的datanodes必须配置两个NameNode的地址，发送数据块位置信息和心跳给他们两个。





__7.HDFS可靠性保障__ 

	- 冗余备份，默认三份
	- 副本存放位置策略，本地机架节点，同一个机架的另一个节点上，不同机架的节点上
	- 心跳检测，NameNode周期性地从集群中的每个DataNode接受心跳包和块报告
	- 数据完整性检测，NameNode在创建HDFS文件时，会计算每个数据的校验和并储存起来。当客户端从DataNode获取数据时，他会将获取的数据的校验和与之前储存的校验和进行对比。
	- 空间回收：从HDFS中删除的文件会首先被放入到/trash中
	- 核心文件备份，HDFS的核心文件是映像文件（FsImage）和事务日志（Edit），系统支持对这两个文件的备份，以确保NameNode宕机后的恢复。


 __8.一致性哈希__
 	- 应用场景是分布式集群中
 	- 环形Hash空间，0~2^32-1的环形空间，key根据hash算法映射到环上，此外把机器(的ip)也hash到环上，根据顺时针，把key放到最近的机器节点上。
 	- 参考http://blog.csdn.net/cywosp/article/details/23397179/


 __9.Map,reduce数量怎么确定
 	- Map个数取决于文件分块的个数，以及文件分块的大小。可以手动设置Map的数量，但是必须不能小于文件分块的数量。比如有两个文件，一个是129M，一个是20M，那么Hadoop2版本，会分成三个map task
 	- reducer个数是由partition个数决定,mapper产生的中间数据经过shuffer过程，根据我们的业务把数据分成若干partition，每个partition的数据由对应的一个reducer来处理。通过调用JobConf.class的实例中的job.setNumReduceTask(n)确定
 

 ## Hive
 __1.Hive SQL转化为MR的过程__

 - hive工作原理
 	- 用户提交查询等任务给Driver。
	- 编译器获得该用户的任务Plan。
	- 编译器Compiler根据用户任务去MetaStore中获取需要的Hive的元数据信息。
	- 编译器Compiler得到元数据信息，对任务进行编译，先将HiveQL转换为抽象语法树，然后将抽象语法树转换成查询块，将查询块转化为逻辑的查询计划，重写逻辑查询计划，将逻辑计划转化为物理的计划（MapReduce）, 最后选择最佳的策略。
	- 将最终的计划提交给Driver。
	- Driver将计划Plan转交给ExecutionEngine去执行，获取元数据信息，提交给JobTracker或者SourceManager执行该任务，任务会直接读取HDFS中文件进行相应的操作。
	- 获取执行的结果。
	- 取得并返回执行结果。

 - hiveSQL转换成MapReduce的执行计划包括如下几个步骤：HiveSQL ->AST(抽象语法树) -> QB(查询块) ->OperatorTree（操作树）->优化后的操作树->mapreduce任务树->优化后的mapreduce任务树

 - 主要步骤
	- 使用antlr完成对SQL的语法解析，将SQL转化为抽象语法树AST Tree
	- 遍历AST Tree，抽象出查询的基本组成单元QueryBlock,QueryBlock是一条SQL最基本的组成单元，包括三个部分：输入源，计算过程，输出。简单来讲一个QueryBlock就是一个子查询。
	- 遍历QueryBlock，生成执行操作树OperatorTree，hive操作符是hive对表数据的逻辑处理，基本的操作符包括TableScanOperator，SelectOperator，FilterOperator等等
	- 逻辑层优化器对OperatorTree进行优化，与物理优化相比，一是对操作符级别的调整，二是优化不针对特定计算引擎
	- 遍历OperatorTree，划分成若干Task，翻译成MR任务
	- 物理优化器根据各计算引擎的特定，对MR任务优化，最终生成执行计划，执行Task任务

__2.sort by和order by区别__

- order by是全局排序，只有一个reducer;sort by不是全局排序，只在数据进入reducer之前完成排序

__3.Top N__
```
select
	cookieid,
	pv,
	rank() over(partition by cookieid order by pv desc) as rn
	from tableName
	where rn <= N
```
__4.行列转换__
![](resource/sql.jpg?raw=true)
```
select 年, 
sum(case when 季度=1 then 销售量 else 0 end) as 一季度, 
sum(case when 季度=2 then 销售量 else 0 end) as 二季度, 
sum(case when 季度=3 then 销售量 else 0 end) as 三季度, 
sum(case when 季度=4 then 销售量 else 0 end) as 四季度 
from sales group by 年;
```


```
         select dt,domain,
                max(case when alertlevel = 'HIGH' then num else 0 end) as high,
                max(case when alertlevel = 'MEDIUM' then num else 0 end) as mediums,
                max(case when alertlevel = 'LOW' then num else 0 end) as low
                from (
                 select dt,domain,alertlevel,count(1) as num from default.t_waf_log_websec where dt = '2017-07-14' group by dt,domain,alertlevel
                ) t
               group by dt,domain

```
__5.hive架构__
	- 用户接口：CLI（命令行）、JDBC/ODBC客户端、web GUI
	- metaStore: hive 的元数据结构描述信息库，可选用不同的关系型数据库来存储，通过配置文件修改、查看数据库配置信息
	- Driver: 解释器、编译器、优化器完成HQL查询语句从词法分析、语法分析、编译、优化以及查询计划的生成。生成的查询计划存储在HDFS中，并在随后由MapReduce调用执行
	- Hive的数据存储在HDFS中，大部分的查询、计算由MapReduce完成
	





