# 《kafka入门与实践》

## 第一章 kafka简介
- 基本概念
	- topic
	- message，消息顺序写入磁盘，即.log结尾的日志
	- 分区与副本，一个topic分成一个或多个分区，每个分区由一系列有序，不可变对的消息组织。每个分区在物理上对应一个文件夹；副本就是分区里的数据存多份
	- leader副本和follow副本，leader副本对外负责客户端的读写请求，follow副本与之同步
	- 偏移量，相当于message的唯一有序编号
	- broker kafka集群中的服务器，需要唯一id区别
- 特性 消息持久化，高吞吐，拓展性等优点

## 第三章 kafka核心组件

## 第四章 kafka核心流程分析

## 第五章 kafka基本操作实战
- 启动kafka单个节点
	- 启动zk bin/zkServer.sh start  登陆到zk: bin/zkCli.sh -server 127.0.0.1:2181
	- 启动kafka bin/kafka-server-start.sh [-daemon] $KAFKA_HOME/config/server.properties -daemon是后台启动的可选项
	- 关闭kafka bin/kafka-server-stop.sh
- 操作主题
    + 创建单个节点下的主题 kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test
	- 创建多节点下的主题 kafka-topics.sh --create --zookeeper server-1:2181,server-2:2181,server-3:2181
--replication-factor 2 --partitions 3 --topic kafka-action(多节点的kafka需要在zookeeper-zoo.cfg中配置)
	- 查看主题信息
        - kafka-topics.sh --list --zookeeper localhost:2181  列出当前端口的主题
		- kafka-topics.sh --list --zookeeper server-1 :2181, server-2:2181
		- kafka-topics.sh --describe --zookeeper server-1:2181,server-2:2181 --topic streamingtopic 列出指定主题的信息
	- 删除主题 kafka-topics --delete --zookeeper server-1:2181,server-2:2181  kafka-action
- 生产者基本操作
	- 启动生产者  kafka-console-producer.sh --broker-list localhost:9092 --topic hello_topic
- 消费者基础操作
    + 简单单节点 kafka-console-consumer.sh --zookeeper localhost:2181 --topic test --from-beginning
	- 启动旧版消费者 ./kafka-console-consurner.sh --zookeeper server-1:2181,server-2:2181,server-2:2181 --topic kafka-action --consumer-property group.id=old-consumer-test --consumer-property consumer.id=old- consumer-cl --from-beginning --delete-consumer-offsets
		- consumer-property参数设置消费者组、消费者名称等信息
		- from-beginning 旧版消费者只能从头或者从最新开始消费，不支持指定偏移量消费
- 单播与多播
	- 单播:一条消息只能被某一个消费者消费,只需这些消费者属于同一个消费者组即可
	- 多播:一条消息被多个消费者消费，只需这些消费者属于不同消费者组即可
- 查看消费偏移量 查看某个消费者组消费偏移量的脚本
- 分区操作

## 第六章 kafka JAVA API编程实战
- pom
```
<dependencies>
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka_2.11</artifactId>
        <version>0.8.2.2</version>
    </dependency>
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-clients</artifactId>
        <version>0.8.2.2</version>
    </dependency>
</dependencies>
```
### 6.2 生产者 Java API
- 详细例子请看/KafkaByJavaAPI
- 单线程
```
public class QuotaitonProducer {
    private static final Logger LOG = Logger.getLogger(QuotaitonProducer.class);

    // 设置实例生成消息的总数
    private static final int MSG_SIZE = 100;

    // topic
    private static final String TOPIC = "hello_topic";

    // kafka集群
    private static final String BROKER_LIST = "localhost:9092";
    private static KafkaProducer<String,String> producer = null;

    static {
        // 构造用于实例化KafkaProducer的Properties信息
        Properties configs = initConfig();
        // 初始化一个KafkaProducer
        producer = new KafkaProducer<String,String >(configs);
    }

    /**
     * 初始化Kafka配置
     * @return
     */
    private static Properties initConfig() {
        Properties properties = new Properties();
        // kafka broker列表
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,BROKER_LIST);
        // 设置序列化类
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return properties;
    }

    /**
     * 模拟生产股票行情信息
     * @return
     */
    private static StockQuotationInfo createQuotaitonInfo() {
        StockQuotationInfo quotationInfo = new StockQuotationInfo();

        // 随机产生范围在1-10的随机数，与600100组成股票代码
        Random r = new Random();
        Integer stockCode = 600100 + r.nextInt(10);

        // 随机产生一个0到1之间的浮点数
        float random = (float) Math.random();
        // 设置涨跌规则
        if (random / 2 < 0.5) {
            random = - random;
        }

        DecimalFormat decimalFormat = new DecimalFormat(".00"); // 设置保留两位有效数字
        quotationInfo.setCurrentPrice(Float.valueOf(decimalFormat.format(11 + random))); // 设置最新价格在11元浮动
        quotationInfo.setPreClosePrice(11.50f); // 设置收盘价
        quotationInfo.setOpenPrice(11.75f); // 设置开盘价
        quotationInfo.setLowPrice(11.50f); // 设置最低价
        quotationInfo.setHighPrice(12.50f); // 设置最高价
        quotationInfo.setStockCode(stockCode.toString()); // 设置股票代码
        quotationInfo.setTradeTime(System.currentTimeMillis()); // 设置交易时间
        quotationInfo.setStockName("股票-"+stockCode);
        return quotationInfo;
    }


    public static void main(String[] args) {
        ProducerRecord<String , String> record = null;
        StockQuotationInfo quotationInfo = null; // 这里是需要写入的内容，可以采用Bean模式，这里是一个例子
        try {
            int num = 0;
            for (int i = 0; i < MSG_SIZE; i++) {
                quotationInfo = createQuotaitonInfo();
                record = new ProducerRecord<String, String>(TOPIC,null,quotationInfo.getStockCode(),quotationInfo.toString());
                producer.send(record); // 异步发送消息
                if (num++ % 10 == 0) {
                    Thread.sleep(2000L); // 休眠2s
                }
            }
        } catch (InterruptedException e) {
            LOG.error("send message occurs exception",e);
        } finally {
            producer.close();
        }
    }
}
```
### 6.3 消费者 Java API
- 详细例子请看/KafkaByJavaAPI
```
package com.bupt.javaEE.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;

/**
 * Created by guoxingyu on 2018/6/27.
 * 新版消费者API
 */
public class KafkaSimpleConsumer {

    private final ConsumerConnector consumer;

    // topic
    private static final String TOPIC = "hello_topic";


    private KafkaSimpleConsumer() {
        Properties props = new Properties();
        //zookeeper 配置
        props.put("zookeeper.connect", "localhost:2181");

        //group 代表一个消费组
        props.put("group.id", "test-group");

        //zk连接超时
        props.put("zookeeper.session.timeout.ms", "4000");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "smallest");

        //序列化类
        props.put("serializer.class", "kafka.serializer.StringEncoder");

        ConsumerConfig config = new ConsumerConfig(props);

        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(config);
    }

    private void consume() {
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(TOPIC, new Integer(1));

        StringDecoder keyDecoder = new StringDecoder(new VerifiableProperties());
        StringDecoder valueDecoder = new StringDecoder(new VerifiableProperties());

        Map<String, List<KafkaStream<String, String>>> consumerMap =
                consumer.createMessageStreams(topicCountMap,keyDecoder,valueDecoder);
        KafkaStream<String, String> stream = consumerMap.get(TOPIC).get(0);
        ConsumerIterator<String, String> it = stream.iterator();
        while (it.hasNext()) {
            System.out.println(it.next().message());
        }
    }

    public static void main(String[] args) {
        new KafkaSimpleConsumer().consume();
    }
}
```
## 第八章 kafka数据采集应用

### 8.1 kafka + Log4j
- 详细例子请看/KafkaByJavaAPI
![](/resource/kafka_log4j.jpg?raw=true)
- pom
```
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-log4j-appender</artifactId>
    <version>0.10.2.1</version>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-log4j12</artifactId>
    <version>1.7.5</version>
</dependency>
```
- log4j.properties
```
log4j.rootLogger=INFO,console,KAFKA
## appender KAFKA
log4j.appender.KAFKA=kafka.producer.KafkaLog4jAppender
log4j.appender.KAFKA.topic=kafka-log4j
log4j.appender.KAFKA.brokerList=127.0.0.1:9092

log4j.appender.KAFKA.compressionType=none
log4j.appender.KAFKA.syncSend=true
log4j.appender.KAFKA.layout=org.apache.log4j.PatternLayout
log4j.appender.KAFKA.ThresholdFilter.level=INFO
log4j.appender.KAFKA.ThresholdFilter.onMatch=ACCEPT
log4j.appender.KAFKA.ThresholdFilter.onMismatch=DENY

log4j.appender.KAFKA.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L %% - %m%n

## appender console
log4j.appender.console=org.apache.log4j.ConsoleAppender
log4j.appender.console.target=System.out
log4j.appender.console.layout=org.apache.log4j.PatternLayout
log4j.appender.console.layout.ConversionPattern=%d (%t) [%p - %l] %m%n
```
- Log4jProducer
```
package com.bupt.javaEE.Test;

import org.apache.log4j.Logger;

/**
 * Created by guoxingyu on 2018/7/8.
 * 实现功能：kafka + log4j
 */
public class Log4jProducer {
    private static final Logger LOG = Logger.getLogger(Log4jProducer.class);

    public static void main(String[] args) throws InterruptedException {
        for(int i = 0;i <= 10; i++) {
            LOG.info("This is Message [" + i + "] from log4j producer .. ");
            Thread.sleep(1000);
        }
    }
}

```


### 8.2 kafka + Flume
- 详细例子请看/Users/guoxingyu/app/apache-flume-1.6.0-cdh5.7.0-bin/project/flume-kafka-test.conf
![](/resource/kafka_flume.jpg?raw=true)
- Flume结构是数据源source,通道channel,接收器sink，在源和接收器之间可以加拦截器进行数据处理，此外还提供了负载均衡和故障转移。
- flume 源是linux命令 + 通道内存 + kafka接受
    - 执行命令 flume-ng agent --name agent --conf $FLUME_HOME/conf --conf-file $FLUME_HOME/project/flume-kafka-test.conf -Dflume.root.logger=INFO,console
    - 配置
    ```
    agent.sources = sc
    agent.sinks = sk
    agent.channels = chl

    #指定源类型是linux命令
    agent.sources.sc.type = exec
    agent.sources.sc.command = tail -f /Users/guoxingyu/data/test.log
    agent.sources.sc.fileHeader = false

    agent.sinks.sk.type = org.apache.flume.sink.kafka.KafkaSink
    agent.sinks.sk.brokerList = localhost:9092
    agent.sinks.sk.topic = hello_topic
    agent.sinks.sk.batchSize = 5
    agent.sinks.sk.requireAcks =1
    agent.sinks.sk.custom.encoding=UTF-8


    agent.channels.chl.type = memory
    #指定通过中停留的最大事件数
    agent.channels.chl.capacity = 1000

    agent.sources.sc.channels = chl
    agent.sinks.sk.channel = chl
    ```
- flume 源是kafka + 通道内存 + HDFS接收
    - 执行命令 flume-ng agent --name agent --conf $FLUME_HOME/conf --conf-file $FLUME_HOME/project/test.conf -Dflume.root.logger=INFO,console
    - 配置
    ```
    agent.sources = kafkaSource
    agent.channels = memoryChannel
    agent.sinks = hdfsSink


    # The channel can be defined as follows.
    agent.sources.kafkaSource.channels = memoryChannel
    agent.sources.kafkaSource.type=org.apache.flume.source.kafka.KafkaSource
    agent.sources.kafkaSource.zookeeperConnect=127.0.0.1:2181
    agent.sources.kafkaSource.topic=hello_topic
    #agent.sources.kafkaSource.groupId=flume
    agent.sources.kafkaSource.kafka.consumer.timeout.ms=100

    agent.channels.memoryChannel.type=memory
    agent.channels.memoryChannel.capacity=1000
    agent.channels.memoryChannel.transactionCapacity=100


    # the sink of hdfs
    agent.sinks.hdfsSink.type=hdfs
    agent.sinks.hdfsSink.channel = memoryChannel
    agent.sinks.hdfsSink.hdfs.path=hdfs://localhost:8020/project/syslog/dt=%Y%m%d
    agent.sinks.hdfsSink.hdfs.writeFormat=Text
    agent.sinks.hdfsSink.hdfs.fileType=DataStream
    agent.sinks.hdfsSink.hdfs.rollSize=0
    agent.sinks.hdfsSink.hdfs.rollCount=0
    agent.sinks.hdfsSink.hdfs.rollInterval=60
    agent.sinks.hdfsSink.hdfs.threadsPoolSize=15
    ```

# 《Apache Kafka 实战》

## 1. 认识Apache Kafka

### 1.2 消息引擎系统概述
    - 经典的引擎系统需要考虑两个方面：消息的设计、传输协议的设计
    - 消息的设计一般以结构化形式呈现，比如xml，json等
    - 消息引擎范型包括：消息队列模型和发表/订阅模型
        - 消息队列模型：定义消费队列、发送者、接受者，采用P2P方式，发送者发送到指定队列，接受者从该队列消费，消费后队列删除该消息；发/接一一对应关系。

### 1.3 Kafka概要设计
    - Kafka需要在设计是满足以下四个要求
    - 高吞吐量/低延迟
        - 大量使用操作系统页缓存，内存操作速度快且命中率高
        - Kafka不直接参与物理IO操作，交给擅长此事的操作系统完成
        - 使用追加的写入方式，避免磁盘随机读写操作
        - 使用sendfile为代表的零拷贝技术加强网络间数据传输效率
    - 消息持久化
        - 目的是消息的发送和接受的解耦、实现灵活的消息消费
        - Kafka是数据立即写入文件系统的持久化日志中，节省内存给页缓存。（？跟写入页缓存有矛盾吧）
    - 负载均衡和故障转移
        - 负载均衡：智能化分区领导者选举策略
        - 故障转移：会话机制。Kafka服务器以会话形式注册到zookeeper
    - 伸缩性
        - 依赖zk保存Kafka每台服务器的状态，方便扩容

### 1.4 Kafka基本术语
    - 消息：
        - 消息的格式对于普通用户需要知道：Key,Value,TimeStamp
        - 消息使用二进制数组ByteBuffer保存数据，极大节约内存
    - topic，partition，offset(此offset是消息在partition分配的位移值，用于唯一确定消息在partition的位置)
    - 消费者offset
    - replica 
    - leader和follower，只有leader对外提供服务
    - ISR，与leader replica保持同步的replica集合



