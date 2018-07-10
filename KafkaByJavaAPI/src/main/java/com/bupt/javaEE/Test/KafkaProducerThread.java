package com.bupt.javaEE.Test;


import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.log4j.Logger;



/**
 * Created by guoxingyu on 2018/6/27.
 * Kafka Java API 多线程生成者
 * 没实现，不知道哪里出问题
 */
public class KafkaProducerThread implements Runnable{
    private static final Logger LOG = Logger.getLogger(KafkaProducerThread.class);

    private KafkaProducer<String,String> producer = null;
    private ProducerRecord<String,String> record = null;

    public KafkaProducerThread(KafkaProducer<String,String> producer,ProducerRecord<String,String> record) {
        this.producer = producer;
        this.record = record;
    }


    public void run() {
        producer.send(record, new Callback() {
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if (null != e) {
                    LOG.error("send message occurs exception",e);
                }
                if (null != recordMetadata) {
                    LOG.info(String.format("offset:%s,partition:%s",recordMetadata.offset(),recordMetadata.partition()));
                }
            }
        });
    }
}
