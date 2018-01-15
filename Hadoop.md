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
            

 
