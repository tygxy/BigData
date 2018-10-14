# Spark Streaming+Kafka的offset管理方法

最近实习需要开发一套Spark Streaming的实时流处理项目，内心还是很期待的。说来惭愧，做大数据开发实习一年有余了，都是离线批处理的任务，还没亲自操刀部署上线一套流处理项目。正好有这样的机会，补一补自己的知识短板。

Spark Streaming的基础知识在之前在公众号里有过介绍，是实习小伙伴沙利民同学总结的，写的很不错，需要参考的同学可以点击学习

本篇主要介绍一下Spark Streaming在消费Kafka过程中，当出现程序挂掉重启后，找到上次消费过的最后一次数据，确保kafka数据精确消费一次(exactly-once)的目的。


## 1. 背景介绍

首先先说下kafka三种消息传递保证：
	- at most once，消息至多会被发送一次，但如果产生网络延迟等原因消息就会有丢失
	- at least once，消息至少会被发送一次，上面既然有消息会丢失，那么给它加一个消息确认机制即可解决，但是消息确认阶段也还会出现同样问题，这样消息就有可能被发送两次。
	- exactly once，消息只会被发送一次，这是我们想要的效果

对于数据的消费者，自然希望最后一种情况。kafka通过offset记录每个topic中的每个partition的消息的位置信息，如果程序挂掉重启的话，程序可以找到上次最后一次消费消息的offset位置，从下一个开始继续消费数据。如果没有保存每个分区已经读取的offset，那么Spark Streaming就没有办法从上次断开（停止或者报错导致）的位置继续读取消息。

## 2. 常见offset管理方法介绍

常见的offset管理办法随着kafka的完善不断改进的，offset可以通过多种方式管理，一般的步骤如下：
	- DStream初始化的时候，需要指定一个包含每个topic的每个分区的offset用于让DStream从指定位置读取数据
	- 消费数据
	- 更新offsets并保存

![](/resource/kafkaoffset0.jpg?raw=true)

### 2.1 checkpoints

Spark Streaming的checkpoints是最基本的存储状态信息的方式，一般是保存在HDFS中。但是最大的问题是如果streaming程序升级的话，checkpoints的数据无法使用，所以几乎没人使用。

### 2.2 Zookeeper

Spark Streaming任务在启动时会去Zookeeper中读取每个分区的offsets。如果有新的分区出现，那么他的offset将会设置在最开始的位置。在每批数据处理完之后，用户需要可以选择存储已处理数据的一个offset或者最后一个offset来保存。这种办法需要消费者频繁的去与Zookeeper进行交互，如果期间 Zookeeper 集群发生变化，那 Kafka 集群的吞吐量也跟着受影响。

### 2.3 一些外部数据库(HBase,Redis等)

可以借助一些可靠的外部数据库，比如HBase,Redis保存offset信息，Spark Streaming可以通过读取这些外部数据库，获取最新的消费信息。

## 2.4 kafka自身

Apache Spark 2.1.x以及spark-streaming-kafka-0-10使用新的的消费者API即异步提交API。你可以在你确保你处理后的数据已经妥善保存之后使用commitAsync API（异步提交API来向Kafka提交offsets。新的消费者API会以消费者组id作为唯一标识来提交offsets。

## 3. 实例demo

本文通过两个例子，展示Streaming管理offset的方法。

### 3.1 使用kafka自身保存offset

Kafka版本0.10.1.1，已默认将消费的offset迁入到了Kafka一个名为__consumer_offsets的Topic中。所以我们读写offset的对象正是这个topic，实际上，一切都已经封装好了，直接调用相关API即可。

![](/resource/kafkaoffset1.jpg?raw=true)

### 3.2 实例使用redis保存offset

根据官网推荐的使用步骤，其实也就两部分，一是从外部数据库中读取offset，第二是完成一个批次的操作后，更新库里的offset值。本demo以保存在redis为例，简要列出相关代码。

#### 3.2.1 RedisUtils

基本的redis工具类

![](/resource/kafkaoffset2.jpg?raw=true)

#### 3.2.2 StreamingTest

前面的配置spark,kafka与之前一样，之后首先配置redis信息，并从redis读取topic各分区对应的lastoffset

![](/resource/kafkaoffset3.jpg?raw=true)

再创建stream流，每个partition处理完成后，需要更新这个partition的offset值

![](/resource/kafkaoffset4.jpg?raw=true)

## 4. 测试

两个都已经亲测可以正常使用，这里就简单拿offset保存在kafka这个例子做个测试。我的测试版本是spark 1.2

首先启动streaming程序，在kafka producer终端打进几个测试数据

![](/resource/kafkaoffset5.jpg?raw=true)

可以看到确实消费了三条数据，把程序终止。

![](/resource/kafkaoffset6.jpg?raw=true)

再向这个topic打进三条数据，打完后重启streaming程序

![](/resource/kafkaoffset7.jpg?raw=true)

我们可以看到，确实是从最新的三条数据开始消费的，之前的数据没有被消费。做到了exactly onece

![](/resource/kafkaoffset8.jpg?raw=true)

## 5. 总结

相对于离线批处理，流处理需要考虑的地方更多一些，对程序的鲁棒性要求也更高。对offset的管理只是其中最基本的一环，鉴于有些坑还没踩过，关于Spark Streaming也不是很熟悉，如有纰漏或不对的地方，请多指教。我也将继续更新这部分内容，补齐自己的知识短板。

## 6. 参考文献
- https://www.cnblogs.com/lianxuan1768/p/8127553.html 《KAFKA OFFSET的存储问题》
- https://blog.csdn.net/sand_clock/article/details/68486599 《kafka中的offset》
- https://www.jianshu.com/p/ef3f15cf400d 《Spark Streaming 管理 Kafka Offsets 的方式探讨》
- http://spark.apache.org/docs/latest/streaming-kafka-0-10-integration.html  《Spark官网》
- http://lxw1234.com/archives/2018/02/901.htm 《实时流计算、Spark Streaming、Kafka、Redis、Exactly-once、实时去重》
- https://github.com/Talefairy/sparkStreaming-offset-to-zk