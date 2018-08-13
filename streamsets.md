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

1.

## 输出地

## 实例

## 参考内容
- https://streamsets.com/
- https://cloud.tencent.com/developer/column/2264/tag-2448
