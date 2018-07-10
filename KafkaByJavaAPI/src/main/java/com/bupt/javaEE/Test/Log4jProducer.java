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
