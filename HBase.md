# HBase原理总结

在总结Spark读写HBase的同时，也顺便学习了一下HBase的原理，同样做个简单的记录。事实上，相关的总结网上超级多，写的已经很详细了，重在理解。

我自己觉得学习大数据原理时最麻烦的就是逻辑概念和物理概念的交叉混叠。每一套组件里都涉及到很多新的相关名词，很容易就搞混弄乱。学习原理最重要的还是集中注意力，参考多份资料，仔细反复阅读，效果会好些。

## 1.HBase简介

HBase是Hadoop生态圈中的非关系型数据库，其最大的特点是面向列存储、可以实现在超大规模数据集上的实时读写和随机访问，可以说是对HDFS的有益补充。

传统的行存储是将完整的数据行存储在磁盘中，查询时会读取该行的所有数据列。但有些应用场景，只需要一小部分数据列，这种方式就很浪费IO。

列存储就是将同一个数据列的各个值存放在一起，也就是说插入某行数据时，该行的各个数据列的值会存放到不同的地方。好处就是需要某几列数据时，可以很方便读取。


## 2.HBase数据模型

HBase同数据库一样，也是以表的形式存储数据。表由行和列构成，若干列可以组成一个列族(Column family)。

### 2.1 HBase逻辑数据模型

![](/resource/hbase1.jpg?raw=true)

基本概念：
	- RowKey:每条记录的“主键”
	- Column Family:列族，包含一个或者多个相关列，列族是表的Schema的一部分
	- Column:属于某一个columnfamily，familyName:columnName，每条记录在同一个列族中可以有不同的列组成
	- Version Number：版本，类型为Long，默认值是系统时间戳timestamp，也可由用户自定义
	- Value：值

### 2.2 HBase物理数据模型

![](/resource/hbase2.jpg?raw=true)

- 对于任意值，都是按照<key, columnfamily, columnname, timestamp, value>这种多级索引结构存储的
- 每个column family存储在HDFS上的一个单独文件中，空值不会被保存






## 3.HBase体系架构

## 4.HBase读写过程

## 5.HA机制






## 参考
- https://www.cnblogs.com/csyuan/p/6543018.html
- https://www.jianshu.com/p/20aff7d85e95