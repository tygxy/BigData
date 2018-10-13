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



