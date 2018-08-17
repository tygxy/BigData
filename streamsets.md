# Streamsets

最近在调研Streamsets，只搞明白了一些最简单的Demo应用。由于网络上相关资料非常少，做个简单的记录。

## 简介

Streamsets是一款大数据实时采集和ETL工具，可以实现不写一行代码完成数据的采集和流转。通过拖拽式的可视化界面，实现数据管道(Pipelines)的设计和定时任务调度。最大的特点有：
	- 可视化界面操作，不写代码完成数据的采集和流转
	- 内置监控，可是实时查看数据流传输的基本信息和数据的质量
	- 强大的整合力，对现有常用组件全力支持，包括50种数据源、44种数据操作、46种目的地。

对于Streamsets来说，最重要的概念就是数据源(Origins)、操作(Processors)、目的地(Destinations)。创建一个Pipelines管道配置也基本是这三个方面。

常见的Origins有Kafka、HTTP、UDP、JDBC、HDFS等；Processors可以实现对每个字段的过滤、更改、编码、聚合等操作；Destinations跟Origins差不多，可以写入Kafka、Flume、JDBC、HDFS、Redis等。

## 基本安装和基本操作 

查看https://cloud.tencent.com/developer/article/1078852

目前网上的中文资料中，也就这个专题介绍的比较详细，几个常用组件的配置介绍的还可以，我也是按照这个入门的。

## 数据源

1. kafka单主题单进程消费者
	- 基本使用，配置broker、zookeeper、consumer group、topic
	- kafka的properties可以在kafka configuration设置
	- offset管理，offset信息根据kafka版本保存在zookeeper或kafka里
		- 如果没保存offset，默认方式是接受通道启动后的数据，如果需要from begining,设置conf为auto.offset.reset:earliest
		- 如果之前保存了offset，从保存的offset+1开始处理
	- 数据类型包括avro、json、log、text、delimited等
	- Produce Single Record 如果勾选，json字符串包含多个对象也只生成一条记录；如果没勾选，会生成多条记录

2. kafka多主题多线程消费者
	- 基本使用，配置broker、consumer group、topic list、num of threads

3. Hadoop FS Standalone
	- 基本使用，配置Hadoop URI、File Directory、num of threads、File name parttern、File name parttern mode、read order等
	- 配置时要明确指明文件路径、文件名模式、文件名匹配模式、读取顺序
		- 文件名匹配模式有两种，glob mode(形如*.json)和正则表达式
		- 文件名模式，例如.json、.txt等
		- 读取顺序
			- Last Modified Timestamp,按照文件的最后修改时间的升序次序读取数据，而且是嵌套读取子目录的内容
			- Lexicographically Ascending File Names，按照文件名的字母升序读取数据
	- 多线程处理，一个线程建立一个pipeline实例，并行处理数据
	- 可以指定处理的第一份文件名称，那么之前的文件就不会处理了
	- Buffer Limit指定每条记录的大小，如果记录大于这个限制，会按该条信息error处理

4. JDBC
	- 首先安装jdbc driver
	- 基本使用，配置connection、SQL query、username、password、jdbc driver
	- 可以通过在where语句中设置指定列(offset column)的指定值(offset value)开始读取数据，比如从主键id大于10000的记录开始读取
	- 两种查询模式
		- 增量 从设置好的initial offset value开始读，会按一定的时间批次对append的数据自动更新，适用于append_only的场景，需要写明where和order by
		```
		select * from <tableName> where <offset column> > ${OFFSET} order by <offset column>
		```
		- 全量 会在时间间隔后重复执行query，会捕捉所有行的变化，不适合大规模的表，可以选择offset column和offset value
		```
		select * from <tableName>
		```

## 操作

1. Field Master 给指定字符串字段加密，可以选择Mast的类型
2. Stream Selector 对数据流做分流处理
3. Field Merger 支持数据合并，但只支持map、List结构的数据
4. Aggregator 在一段时间间隔内做聚合指标，比如sum、avg、max、min等
5. Delay 延迟一段时间
6. Field Flattener 拆分map、List结构的数据成没有嵌套的数据结构
7. Field Hasher 对指定字段进行encode,策略

## 目的地

基本配置跟数据源差不多。

## 一个简单的Demo 

实现kafka读入数据，Stream Selector对数据流做分流，再各自写入到kafka不同的topic中。

- 整体架构
![](/resource/Streamsets1.PNG?raw=true)

- 配置数据源
	- 配置kafka consumer的基本信息,包括broker、zk、topic、consumer group
	![](/resource/Streamsets2.jpg?raw=true)
	- 配置Data格式，这里选择的是json格式
	![](/resource/Streamsets3.PNG?raw=true)

- 配置操作
	- 配置Stream Selector，数据根据Version字段分为两类
	![](/resource/Streamsets4.PNG?raw=true)

- 配置目的地
	- 继续配置kafka producer
	![](/resource/Streamsets5.jpg?raw=true)


## 参考内容
- https://streamsets.com/
- https://cloud.tencent.com/developer/column/2264/tag-2448
