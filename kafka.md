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
	- 启动kafka bin/kafka-server-start.sh $KAFKA_HOME/config/server.properties
	- 关闭kafka bin/kafka-server-stop.sh 
- 操作主题
	- 创建主题 kafka-topics.sh --create --zookeeper server-1:2181,server-2:2181,server-3:2181
--replication-factor 2 --partitions 3 --topic kafka-action
	- 查看主题信息 
		- kafka-topics --list --zookeeper server-1 :2181, server-2:2181
		- kafka-topics.sh --describe --zookeeper server-1:2181,server-2:2181 --topic streamingtopic
	- 删除主题 kafka-topics --delete --zookeeper server-1:2181,server-2:2181  kafka-action
- 生产者基本操作
	- 启动生产者  kafka-console-producer.sh --broker-list localhost:9092 --topic hello_topic
- 消费者基础操作
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
### 6.2 消费者 Java API
- 











