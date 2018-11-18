package com.bupt.spark.conf;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by guoxingyu on 2018/10/9.
 */
public class ConfigrationManager {

    /**
     * 从配置文件中读取参数
     * @param propName
     * @return
     */
    public Properties loadParameterFromFile(String propName) {
        Properties prop = new Properties();
        try {
            String path = Thread.currentThread().getContextClassLoader().getResource(propName).getPath();
            prop.load(new FileInputStream(path));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return prop;
        }
    }

}
