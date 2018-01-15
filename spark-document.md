# Spark Official Document

## 1. 快速入门

## 2. 编程指南

### 2.1 spark的初始化
- 必须创建SparkContext对象
```
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf

val conf = new SparkConf().setAppName(appName).setMaster(master) 
val sc = new SparkContext(conf)
```
### 2.2 RDDs

- 并行集合
	- sc.parallelize()创建并行集合
	```
	val data = Array(1, 2, 3, 4, 5)
	val distData = sc.parallelize(data)
	```
- 外部数据集
	- sc.textFile()
	```
	val distFile = sc.textFile(URLPath)
	```
- RDD操作
	- 基础
		- 两种类型操作，transformations和actions
		- RDDs.collect()集中在一个节点上
		- transformations的方法
		- action的方法
	- RDD持久化
		- persist(),可以设置存储级别
		- cache()
- 共享变量
	- Broadcast variables(广播变量)
	```
	val newRDDName = sc.broadcast(RDDName)
	```
	- Accumulators(累加器)

## 3. Spark Streaming

## 4. Spark SQL

### 4.1 概述
- Dataset是一个分布式数据集合，DataFrame是一个Dataset组织成的指定列，相当与表

### 4.2 入门指南

### 4.3 数据源

### 4.4 性能调优

### 4.5 分布式SQL引擎


## 5. MLlib



