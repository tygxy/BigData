# Spark Steaming + Kafka + Hbase项目实战

同学们在学习Spark Steaming的过程中，可能缺乏一个练手的项目，这次通过一个有实际背景的小项目，把学过的Spark Steaming、Hbase、Kafka都串起来。

## 1.项目介绍

### 1.1 项目流程

Spark Streaming读取kafka数据源发来的json格式的数据流，在批次内完成数据的清洗和过滤，再从HBase读取补充数据，拼接成新的json字符串写进下游kafka。

## 1.2 项目详解

- 上游kafka topic为kafka_streaming_topic，内容是json格式的数据流，例如{"id":"001","name":"郭大宝","subject":"语文","score":"60"}
- spark streaming 从kafka读取数据，完成数据清洗，并筛选出分数>=60分的数据
- 通过id作为rowkey,批量从Hbase中查询学生信息数据，例如{"id":"001","name":"郭大宝","sex":"男","age":"26"}
- 两个json完成拼接，并写入下游topic hello_topic

## 2.环境准备

### 2.1 组件安装

首先需要安装必要的大数据组件，安装的版本信息如下，安装方法自行百度
	- Spark 2.1.2
	- kafka 0.10.0.1
	- HBase 1.2.0
	- Zookeeper 3.4.5

### 2.2 HBase Table的创建

- Hbase创建table student，列族名为cf
	```
	create table 'student','cf'
	```
- 存入两条数据
	```
	put 'student','001','cf:info','{"id":"001","name":"郭大宝","sex":"男","age":"26"}'
	put 'student','002','cf:info','{"id":"002","name":"郭星宇","sex":"男","age":"26"}'
	```

### 2.3 Kafka Topic的创建

- 创建kafka的两个topic，分别是kafka_streaming_topic、hello_topic
	```
	kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic kafka_streaming_topic
	kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic hello_topic
	```

## 3.Code

### 3.1 项目结构

![](/resource/streamingDemo_mulu.jpg?raw=true)

简单解释一下：
- Output、Score、Output三个是Java Bean
- MsgHandler完成对数据流的操作，包括json格式判断、必备字段检查、成绩>=60筛选、json to Bean、合并Bean等操作
- ConfigManager读取配置参数
- conf.properties 配置信息
- StreamingDemo是程序主函数
- HBaseUtils Hbase工具类
- StreamingDemoTest 测试类

### 3.2 主函数

![](/resource/streamingDemo_code1.jpg?raw=true)

![](/resource/streamingDemo_code2.jpg?raw=true)


## 4.结果验证

- 开启kafka producer shell, 向kafka_streaming_topic写数据

![](/resource/streamingDemo_inputTopic.jpg?raw=true)

- 开启kafka consumer shell, 消费hello_topic

![](/resource/streamingDemo_outputTopic.jpg?raw=true)






