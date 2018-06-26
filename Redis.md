# Redis

参考文献 http://www.runoob.com/redis/redis-tutorial.html

## 1.数据类型
- 支持的数据类型String,Hash,List,set,zset(有序集合)
	- String 
	```
	set name "guoxingyu"
	get name
	```
	- Hash
	```
	hmset userInfo name "guoxingyu" age "26" sex "M"
	hget userInfo name
	```
	- List
	```
	lpush runoob redis
	lpush runoob mongodb
	lrange runoob 0 10
	```
	- Set
	```
	sadd runoob1 redis
	sadd runoob1 mongodb
	smembers runoob1
	```
	- zset

## 2.常用命令
- 远程登录 redis-cli -h host -p port -a password
- 具体操作命令参考http://www.runoob.com/redis/redis-tutorial.html
- 发布订阅
```
// 创建了订阅频道名为 redisChat,并持续接收
subscribe redisChat
// 在其他终端上，像指定频道发布消息
publish redisChat "i am guoxingyu"

```
- 事物 单个Redis命令是原子性，但是Redis的事物不是原子性
```
multi  // 开头
hmset userInfo phone 13161319166
hkeys userInfo
exec   // 结束
```

## 3.高级
- 数据备份
	- save //该命令将在 redis 安装目录中创建dump.rdb文件
	- 如果需要恢复数据，只需将备份文件 (dump.rdb) 移动到 redis 安装目录并启动服务即可
- 安全
	- auth password 通过密码才能访问
- 分区
	- 分区是分割数据到多个Redis实例的处理过程，因此每个实例只保存key的一个子集
	- 常见分区方式是范围分区和Hash分区

## 4.Java+Redis
- pom
```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.bupt.javaEE</groupId>
    <artifactId>Redis</artifactId>
    <version>1.0-SNAPSHOT</version>

    <dependencies>
        <dependency>
            <groupId>redis.clients</groupId>
            <artifactId>jedis</artifactId>
            <version>2.9.0</version>
        </dependency>
    </dependencies>
</project>
```
- RedisUtils
```
package com.bupt.javaEE;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by guoxingyu on 2018/6/26.
 */
public class RedisUtils {
    private static String ADDR = "127.0.0.1";
    private static int PORT = 6379;
    private static String AUTH = "XXXXXX";

    private static int MAX_ACTIVE = 1024;
    private static int MAX_IDLE = 200;
    private static int MAX_WAIT = 10000;
    private static int TIMEOUT = 10000;

    private static JedisPool jedisPool = null;

    /**
     * 初始化Redis连接池
     */
    static {
        try {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(MAX_ACTIVE);
            jedisPool = new JedisPool(config,ADDR,PORT,TIMEOUT,AUTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取Jedis实例
     * @return
     */
    public synchronized static Jedis getJedis() {
        try {
            if (jedisPool != null) {
                Jedis resource = jedisPool.getResource();
                return resource;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void returnResource(final Jedis jedis) {
        if (jedis != null) {
            jedisPool.returnResource(jedis);
        }
    }


}

```
- demo
```
package com.bupt.javaEE;

import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Created by guoxingyu on 2018/6/26.
 */
public class TestRedis {
    private Jedis jedis;

    /**
     * 连接redis服务器
     */
    public void connectRedis() {
        jedis = RedisUtils.getJedis();
    }

    /**
     * redis操作String
     */
    public void testString() {
        jedis.set("name","guoxingyu");  // 添加数据
        System.out.println(jedis.get("name"));  // 取数据

        jedis.append("name",".com");  // 拼接字符串
        System.out.println(jedis.get("name"));

        jedis.del("name");  // 删除

        jedis.mset("name","guoxingyu","age","26","sex","Men");  // 设置多个键值对
        System.out.println(jedis.get("name")+"-"+jedis.get("age")+"-"+jedis.get("sex"));
    }

    /**
     * redis操作Hash
     */
    public void testHash() {
        // 添加数据
        Map<String ,String> map = new HashMap<String, String>();
        map.put("name","guoxingyu");
        map.put("age","26");
        map.put("sex","Men");
        jedis.hmset("userInfo",map);

        // 取数据
        List<String> rsmap = jedis.hmget("userInfo","name","age","sex");
        System.out.println(rsmap);

        // 删除某个键
        jedis.hdel("userInfo","sex");

        // 返回值的个数
        System.out.println(jedis.hlen("userInfo"));

        // 判断是否存在key
        System.out.println(jedis.exists("userInfo"));

        // 返回map所有key
        System.out.println(jedis.hkeys("userInfo"));

        // 返回map所有value
        System.out.println(jedis.hvals("userInfo"));

        // 迭代输出指定key的所有value
        Iterator<String> iter = jedis.hkeys("userInfo").iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            System.out.println(key+":"+jedis.hget("userInfo",key));
        }
    }


    /**
     * redis操作List
     */
    public void testList() {
        // 移除所有数据
        jedis.del("java framework");

        // 添加方式一，栈类似的添加方式，先进后出
        jedis.lpush("java framework1","spring");
        jedis.lpush("java framework1","struts");

        // 取数,第一个是key，第二个是起始位置，第三个是结束位置， -1表示取得所有
        System.out.println(jedis.lrange("java framework1",0,-1));

        // 添加方式二，队列类似的添加方式，先进先出
        jedis.rpush("java framework2","spring");
        jedis.rpush("java framework2","struts");
        System.out.println(jedis.lrange("java framework2",0,-1));
    }


    /**
     * redis操作Set
     */
    public void testSet() {
        // 添加数据
        jedis.sadd("family","guoxingyu");
        jedis.sadd("family","guojianjun");
        jedis.sadd("family","guowenping");
        jedis.sadd("family","dog");

        // 移除数据
        jedis.srem("family","dog");

        // 获取数据
        System.out.println(jedis.smembers("family"));

        // 判断是否在集合中
        System.out.println(jedis.sismember("family","dog"));

        // 随机取一个
        System.out.println(jedis.srandmember("family"));

        // 个数
        System.out.println(jedis.scard("family"));
    }

    /**
     * redis排序
     */
    public void testSort() {
        jedis.del("num");
        jedis.rpush("num","1");
        jedis.rpush("num","10");
        jedis.rpush("num","7");
        jedis.rpush("num","25");
        System.out.println(jedis.lrange("num",0,-1));
        System.out.println(jedis.sort("num"));  // 排序
    }




    public static void main(String[] args) {
        TestRedis test = new TestRedis();
        test.connectRedis();

//        test.testString();
//        test.testHash();
//        test.testList();
//        test.testSet();
//        test.testSort();

        RedisUtils.returnResource(test.jedis);
    }
}
```





