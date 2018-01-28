# Hadoop相关生态圈技术

## 1.Hadoop的安装
- 安装jdk 
- 设置ip和hostname的映射关系,ssh等
- 配置文件
    - 	hadoop-env.sh 
    ```
        export JAVA_HOME=/home/hadoop/app/jdk1.7.0_51
    ```
    - core-site.xml
    ```
         <property>
                <name>fs.defaultFS</name>
                <value>hdfs://hadoop001:8020</value>
         </property>	
    	    <property>
                <name>hadoop.tmp.dir</name>
                <value>/home/hadoop/app/tmp</value>
    	    </property>	
    ```
    - hdfs-site.xml
    ```
         <property>
             <name>dfs.replication</name>
             <value>1</value>
         </property>
    ```
- yarn
    - yarn-site.xml
    ```
    	<property>
            <name>yarn.nodemanager.aux-services</name>
            <value>mapreduce_shuffle</value>
         </property>
    ```
    - mapred-site.xml
    ```
    	<property>
            <name>mapreduce.framework.name</name>
            <value>yarn</value>
        </property>
    ```

## 2.HDFS
- 块，默认128MB，抽象概念，操作都是以块为基本单元，默认备份数为三，NameNode管理文件系统命名空间，维护文件目录树和索引目录，DataNode具体存储文件
- 副本存放策略是本地机架节点，同一机架节点，不同机架节点
- 如何保证NameNode的安全，会备份NameNode持久化的元数据文件到其他文件系统中，系统同步运行SecondNameNode,周期性合并编辑日志中的命名镜像
- 文件的读取
  - DistributedFileSystem会通过RPC协议调用NameNode确定请求文件块所在位置，返回DataNode
  - 返回的DataNode会按照机器拓扑结构得出与客户端的距离，进行排序，读取最近DataNode的数据
- 文件写入
  - 以流的形式传给不同的DataNode节点，并且需要返回确认结果

 ## 3.Hive
 - Hive接收SQL语句，解析成MapReduce任务交至集群处理
 - Hive包含四类数据模型：表，外部表，分区(对应表下的一个目录)，桶(part-00000,根据哈希值切分)
    - 内部表和外部表区别：内部表数据加载时会移动到数据仓库目录，删除后元数据和数据同时删除；外部表仅指定外部表，删除表不删除元数据
 - Hive表元数据存储在RDBMS中
 - Hive对列的修改只会更改元数据，不会改变实际数据，要确保元数据定义和实际数据结构一致
 - 分区表需要两步，首先在创建表的时候指定分区，其次在使用前还需要alter table添加具体的分区目录才使用
 - 遇到数据倾斜，可以设置hive.groupby.skewindata=true。会有两个MR任务，第一个会将Map的结果随机分布到Reduce中做聚合；第二个MR再根据Key聚合

 ## 4.HBase
 - 基本操作 list create put scan get
 - HBase体系结构包含HBase Master服务器和HRegion Server服务器群，前者管理后者，数据也存储在后者中，通过zk协调处理
    - HRegion是表名+开始/结束主键+表内某段联系的数据，一张完整的表是保存在多个HRegion上的
    - HRegion服务器 由Hlog和多个HRegion组成;每个HRegion存储实际数据，由Store(一个列族下的数据)组成;每个Store包含MemStore,数据首先在保存在内存MemStore，达到阈值后更新StoreFile(磁盘)中;Hlog用于故障恢复
    - HBase Master服务器 告诉每个HRegion服务器负责哪些HRegion;HRegion服务器的负载均衡;管理用户对表的增删改查
    - ROOT表和META表，元数据META表保存HRegion标识符和实际HRegion服务器的映射关系;Root表保存META表
    - ZK存储的是HBase中Root表和META表的位置;监控各个机器的状态，及时汇报给HBase Master
- HBase数据模型
    - 每张表的索引是行关键字，列关键字和时间戳;列由列族和列构成;
    - 概念上讲HBase是大的映射表，是一个稀疏存储结构，但在物理存储上，是按照列存储
- Java API
    - HBaseAdmin,HBaseConfiguration,HTable,Put,Get

## 5.Zookeeper
- ZK是为分布式应用设计对的开源协调服务，为用户提供同步，配置管理，分组和命名等服务，实现一致性，组管理，leader选举以及某些协议

## 6.Avro
- 数据序列化系统，将数据结构或者对象转换成便于存储和传输的格式

## 7.Yarn
- MR2的核心是将jobTracjer承担的任务，集群资源管理和作业管理分成两部分，分别是resourceManager和applicationMaster
- 核心概念
    - 资源管理器，分为调度器(scheduler)和应用管理器(Application Manager)，前者只负责资源的分配，后者负责接收任务,协商获取第一个容器执行AMaster
    - 节点管理器(Node Manager)，监控容器的资源使用情况并汇报给调度器，根据容器的状态和应用执行情况
    - 应用主体(Application Master),负责与调度器协商资源，与节点管理器合作运行和监控task,任务失败重启等,一个应用只有一个AMaster
    - 资源容器(Container),包括内存，带宽，CPU等资源;container大小固定，地位相等
- 作业执行流程
![](resource/yarn.jpg?raw=true)
 
            

 
