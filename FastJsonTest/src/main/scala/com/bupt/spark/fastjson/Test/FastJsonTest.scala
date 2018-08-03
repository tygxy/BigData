package com.bupt.spark.fastjson.Test

import com.alibaba.fastjson.serializer.SerializerFeature
import org.apache.spark.sql.SparkSession
import com.alibaba.fastjson.{JSON, JSONArray, JSONObject}
import com.bupt.spark.fastjson.Bean.{User, UserTest}
import org.apache.spark.rdd.RDD

/**
  * Created by guoxingyu on 2018/8/2.
  */
object FastJsonTest {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().master("local[2]").appName("FastJsonTest").getOrCreate()

    // 反序列化简单json字符串
    val input1 = "file:///Users/guoxingyu/Documents/work/spark/LogToHDFSAPP/data.log"
    val jsonRDD1 = spark.sparkContext.textFile(input1)

    val dataRDD1 = jsonRDD1.map(json => {
      val jsonObject = JSON.parseObject(json)
      val name = jsonObject.getOrDefault("name",null)
      val age = jsonObject.getOrDefault("age",null)
      (name,age)
    })

    dataRDD1.foreach(println)


    // 反序列化json字符串组两种方法
    val input2 = "file:///Users/guoxingyu/Documents/work/spark/LogToHDFSAPP/data1.log"
    val jsonRDD2 = spark.sparkContext.textFile(input2)

    val dataRDD2 = jsonRDD2.map(json => {
      JSON.parseObject(json).getJSONArray("data").toString
    }).map(x => x.substring(1,x.length-1).replace("},{","}---{"))
      .flatMap(x => x.split("---"))
      .map(x => JSON.parseObject(x))

    val data2 = dataRDD2.map(jsonObject => {
      val version = jsonObject.getOrDefault("version",null)
      val label = jsonObject.getOrDefault("label",null)
      val acc = jsonObject.getOrDefault("acc",null)
      (version,label,acc)
    })

    data2.foreach(println)

    val dataRDD3 = jsonRDD2.flatMap(json => {
      val jsonArray = JSON.parseObject(json).getJSONArray("data")
      var dataList : List[String] = List()
      for (i <- 0 to jsonArray.size()-1) {
        dataList = jsonArray.get(i).toString :: dataList
      }
      dataList
    }).map(x => JSON.parseObject(x))

    val data3 = dataRDD3.map(jsonObject => {
      val version = jsonObject.getOrDefault("version",null)
      val label = jsonObject.getOrDefault("label",null)
      val acc = jsonObject.getOrDefault("acc",null)
      (version,label,acc)
    })

    data3.foreach(println)

    // 序列化json
    val arr = Seq("tom:10", "bob:14", "hurry:9")
    val dataRdd = spark.sparkContext.parallelize(arr)

    val dataString = dataRdd.map(x => {
      val arr = x.split(":")
      val name = arr(0)
      val age = arr(1).toInt
      val u = new UserTest(name,age)
      u
    }).map(x => {
      JSON.toJSONString(x,SerializerFeature.WriteMapNullValue)
    })

    dataString.foreach(println)









  }

}
