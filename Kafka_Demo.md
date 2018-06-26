# Kafka Demo

## 参考资料
- https://blog.csdn.net/tangdong3415/article/details/53432166

## Producer
### Scala版本
- pom.xml
```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.bupt.kafkaTest</groupId>
    <artifactId>kafkaTest</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <scala.version>2.11.8</scala.version>
        <kafka.version>0.8.2.2</kafka.version>
    </properties>

    <repositories>
        <repository>
            <id>scala-tools.org</id>
            <name>Scala-Tools Maven2 Repository</name>
            <url>http://scala-tools.org/repo-releases</url>
        </repository>

        <repository>
            <id>cloudera</id>
            <url>https://repository.cloudera.com/artifactory/cloudera-repos</url>
        </repository>
    </repositories>

    <pluginRepositories>
        <pluginRepository>
            <id>scala-tools.org</id>
            <name>Scala-Tools Maven2 Repository</name>
            <url>http://scala-tools.org/repo-releases</url>
        </pluginRepository>
    </pluginRepositories>

    <dependencies>
        <dependency>
            <groupId>org.scala-lang</groupId>
            <artifactId>scala-library</artifactId>
            <version>${scala.version}</version>
        </dependency>

        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka_2.11</artifactId>
            <version>${kafka.version}</version>
        </dependency>
    </dependencies>

    <build>
        <sourceDirectory>src/main/scala</sourceDirectory>
        <testSourceDirectory>src/test/java</testSourceDirectory>
        <plugins>
            <plugin>
                <groupId>org.scala-tools</groupId>
                <artifactId>maven-scala-plugin</artifactId>
                <executions>
                    <execution>
                        <goals>
                            <goal>compile</goal>
                            <goal>testCompile</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <scalaVersion>${scala.version}</scalaVersion>
                    <args>
                        <arg>-target:jvm-1.5</arg>
                    </args>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-eclipse-plugin</artifactId>
                <configuration>
                    <downloadSources>true</downloadSources>
                    <buildcommands>
                        <buildcommand>ch.epfl.lamp.sdt.core.scalabuilder</buildcommand>
                    </buildcommands>
                    <additionalProjectnatures>
                        <projectnature>ch.epfl.lamp.sdt.core.scalanature</projectnature>
                    </additionalProjectnatures>
                    <classpathContainers>
                        <classpathContainer>org.eclipse.jdt.launching.JRE_CONTAINER</classpathContainer>
                        <classpathContainer>ch.epfl.lamp.sdt.launching.SCALA_CONTAINER</classpathContainer>
                    </classpathContainers>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```
- demo
```
package com.bupt.kafkaTest

import java.util.Properties
import java.util.concurrent.Future

import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord, RecordMetadata}

import scala.io.Source

/**
  * Created by guoxingyu on 2018/5/24.
  */
object KafkaProducerTest {

    def main(args: Array[String]): Unit = {

      // 读取文件,写入kafka
      val fileName = "/Users/guoxingyu/Documents/work/spark/kafkaTest/src/main/resources/log.txt"
      val source = Source.fromFile(fileName)
      val lines = source.getLines

      // 设置kafka参数
      val brokers = "localhost:9092"
      val topic = "streamingtopic"
      val props = new Properties()

      props.put("metadata.broker.list", brokers)
      props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
      props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
      props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)

      val producer = new KafkaProducer[String, String](props)

      var i = 0
      while(true){
          // 有metadata信息
          val ret: Future[RecordMetadata] = producer.send(new ProducerRecord(topic, "key-" + i))
          // 无metadata信息
          producer.send(new ProducerRecord(topic, "value-" + i))
          // 打印出 metadata
          val metadata = ret.get
          println("i=" + i + ",  offset=" + metadata.offset() + ",  partition=" + metadata.partition())


          i = i + 1
          Thread.sleep(2000)
      }

//      文件内容写入kafka
      for (line <- lines){
        producer.send(new ProducerRecord(topic, line))
        Thread.sleep(2000)
      }
      source.close;  //记得要关闭source


      producer.close
    }


}
```
## Kafka + Spark Streaming
- pom.xml
```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.dianxin.sparkstreaming</groupId>
    <artifactId>KafkaToHDFS</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <scala.version>2.11.8</scala.version>
        <kafka.version>0.8.2.2</kafka.version>
        <spark.version>2.1.2</spark.version>
        <hadoop.version>2.6.0-cdh5.7.0</hadoop.version>
    </properties>

    <repositories>
        <repository>
            <id>scala-tools.org</id>
            <name>Scala-Tools Maven2 Repository</name>
            <url>http://scala-tools.org/repo-releases</url>
        </repository>

        <repository>
            <id>cloudera</id>
            <url>https://repository.cloudera.com/artifactory/cloudera-repos</url>
        </repository>
    </repositories>

    <pluginRepositories>
        <pluginRepository>
            <id>scala-tools.org</id>
            <name>Scala-Tools Maven2 Repository</name>
            <url>http://scala-tools.org/repo-releases</url>
        </pluginRepository>
    </pluginRepositories>

    <dependencies>
        <dependency>
            <groupId>org.scala-lang</groupId>
            <artifactId>scala-library</artifactId>
            <version>${scala.version}</version>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka_2.11</artifactId>
            <version>${kafka.version}</version>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-client</artifactId>
            <version>${hadoop.version}</version>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>org.apache.spark</groupId>
            <artifactId>spark-streaming_2.11</artifactId>
            <version>${spark.version}</version>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>com.fasterxml.jackson.module</groupId>
            <artifactId>jackson-module-scala_2.11</artifactId>
            <version>2.6.5</version>
        </dependency>

        <dependency>
            <groupId>net.jpountz.lz4</groupId>
            <artifactId>lz4</artifactId>
            <version>1.3.0</version>
        </dependency>

        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>3.5</version>
        </dependency>

        <dependency>
            <groupId>org.apache.spark</groupId>
            <artifactId>spark-streaming-kafka-0-8_2.11</artifactId>
            <version>${spark.version}</version>
            <scope>provided</scope>
        </dependency>







    </dependencies>

    <build>
        <sourceDirectory>src/main/scala</sourceDirectory>
        <plugins>
            <plugin>
                <groupId>org.scala-tools</groupId>
                <artifactId>maven-scala-plugin</artifactId>
                <executions>
                    <execution>
                        <goals>
                            <goal>compile</goal>
                            <goal>testCompile</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <scalaVersion>${scala.version}</scalaVersion>
                    <args>
                        <arg>-target:jvm-1.5</arg>
                    </args>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-eclipse-plugin</artifactId>
                <configuration>
                    <downloadSources>true</downloadSources>
                    <buildcommands>
                        <buildcommand>ch.epfl.lamp.sdt.core.scalabuilder</buildcommand>
                    </buildcommands>
                    <additionalProjectnatures>
                        <projectnature>ch.epfl.lamp.sdt.core.scalanature</projectnature>
                    </additionalProjectnatures>
                    <classpathContainers>
                        <classpathContainer>org.eclipse.jdt.launching.JRE_CONTAINER</classpathContainer>
                        <classpathContainer>ch.epfl.lamp.sdt.launching.SCALA_CONTAINER</classpathContainer>
                    </classpathContainers>
                </configuration>
            </plugin>

            <plugin>
                <artifactId>maven-assembly-plugin</artifactId>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass></mainClass>
                        </manifest>
                    </archive>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                </configuration>
            </plugin>


        </plugins>
    </build>
    <reporting>
        <plugins>
            <plugin>
                <groupId>org.scala-tools</groupId>
                <artifactId>maven-scala-plugin</artifactId>
                <configuration>
                    <scalaVersion>${scala.version}</scalaVersion>
                </configuration>
            </plugin>
        </plugins>
    </reporting>
</project>
```
- demo
```
package com.dianxin.sparkstreaming

import java.text.SimpleDateFormat

import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import java.util.Calendar

/**
  * Created by guoxingyu on 2018/6/19.
  */
object KafkaToHDFS {
  def main(args: Array[String]): Unit = {
    // localhost:2181 test streamingtopic 1
    if (args.length != 4) {
      System.err.println("Usage:KafkaToHDFS <zkQuorum> <group> <topics> <numThreads>")
      System.exit(1)
    }

    val sparkConf = new SparkConf().setAppName("KafkaToHDFS").setMaster("local[2]")
    val ssc = new StreamingContext(sparkConf,Seconds(300));
    val Array(zkQuorum,group,topics,numThreads) = args

    val topicMap = topics.split(",").map((_,numThreads.toInt)).toMap

    val messages = KafkaUtils.createStream(ssc, zkQuorum, group, topicMap)

    val cal = Calendar.getInstance()
    val day = new SimpleDateFormat("yyyy-MM-dd")
    cal.add(Calendar.DATE, -1)
    val today = day.format(cal.getTime())

    val dt = today.substring(2,10)


//    messages.map(_._2).repartition(1).saveAsTextFiles("hdfs://localhost:8020/user/syslog/"+dt+"/")
    messages.map(_._2).repartition(1).saveAsTextFiles("/project/syslog1/"+dt+"/")

    ssc.start()
    ssc.awaitTermination()

  }
}
```

