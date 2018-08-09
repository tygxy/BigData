# Streamsets

## 简介

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

3. Hadoop FS 
	- 基本使用

## 操作

## 输出地

## 实例

## 参考内容
- https://streamsets.com/
- https://cloud.tencent.com/developer/column/2264/tag-2448
