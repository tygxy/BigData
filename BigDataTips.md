# BigDataTips

## Spark 

__1.SparkConf,SparkContext,SparkSession区别与联系__
 - SparkConf包含集群配置参数
 - SparkContext是Spark程序入口
 - SparkSession是SQLContext和HiveContext的组合，并且封装了SparkContext

 __2.DataFrame,DataSet,RDD区别和联系
 - RDD是弹性分布式数据集，创建的是Java对象，例如RDD[person]
 - DataFrame是RDD基础上加入scheme，可以使用sql语句，但是与RDD相比丢失编译类型检查和面向对象;DataFrame表示为Dataset[Row]，即Dataset的子集
 - DataSet,引入了Encoder
