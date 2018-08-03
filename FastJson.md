# FastJson使用范例(Java、Scala版)

## 0.目录

- FastJson简介

- FastJson三个核心类

- Maven

- Java API
	- 反序列化
		- 反序列化一个简单Json字符串
		- 反序列化一个简单JSON字符串成Java对象组
		- 反序列化一个复杂的JSON字符串
	- 序列化
	- 序列化和反序列化日期
	- JsonObject的一些操作
	- jsonArray的一些操作

- Scala API
	- 反序列化
		- demo日志内容
		- 反序列化简单json字符串
		- 反序列化简单json字符串组
			- String处理
			- List处理
	- 序列化

## 1. FastJson简介

JSON协议在日常开发中很常用，包括前后端的数据接口，日志字段的保存等，通常都采用JSON协议。FastJson是阿里的开源框架，很好用，估计开发的同学都有使用过。这里做一个简单的用法总结，配一些demo。除了Java版本外，由于在Spark也经常清洗日志，所以配上了Scala版本，方便日后查询使用。完整代码可以参考Github:https://github.com/tygxy/BigData

## 2. FastJson三个核心类
- JSON：fastjson的解析器，用于json字符串和javaBean、Json对象的转换
- JSONObject：fastJson提供的json对象 
- JSONArray：fastJson提供json数组对象

## 3. Maven
```
	<dependency>
	    <groupId>com.alibaba</groupId>
	    <artifactId>fastjson</artifactId>
	    <version>1.2.47</version>
	</dependency>
```

## 4. Java API

### 4.1 反序列化

- 反序列化一个简单Json字符串
	- 创建JavaBean的User类
	```
	public class User {
	    private String name;
	    private int age;

	    public String getName() {
	        return name;
	    }

	    public void setName(String name) {
	        this.name = name;
	    }

	    public int getAge() {
	        return age;
	    }

	    public void setAge(int age) {
	        this.age = age;
	    }
	}
	```
	- 反序列化操作
	```
    String jsonString = "{\"name\":\"张三\",\"age\":50}";
    User user= JSON.parseObject(jsonString,User.class);
    System.out.println("name:"+user.getName()+" age:"+user.getAge());

    // 输出结果 name:张三 age:50
	```

- 反序列化一个简单JSON字符串成Java对象组
```
String jsonArrayString = "[{\"name\":\"张三\",\"age\":50},{\"name\":\"李四\",\"age\":51}]";
List<User> userList = JSON.parseArray(jsonArrayString,User.class);
Iterator it = userList.iterator();
while (it.hasNext()) {
    User u = (User)it.next();
    System.out.println("name:"+u.getName()+" age:"+u.getAge());
}

// 输出结果  name:张三 age:50
            name:李四 age:51
```

- 反序列化一个复杂的JSON字符串
	- 分别创建JavaBean的Course类、Student类、Teacher类
	```
	public class Course {
	    private String courseName;
	    private String code;

	    public Course (String courseName, String code){
	        this.setCourseName(courseName);
	        this.setCode(code);
	    }

	    public String getCourseName() {
	        return courseName;
	    }

	    public void setCourseName(String courseName) {
	        this.courseName = courseName;
	    }

	    public String getCode() {
	        return code;
	    }

	    public void setCode(String code) {
	        this.code = code;
	    }
	}

	public class Student {
	    private int id;
	    private String studentName;
	    private int age;

	    public Student(int id, String studentName, int age) {
	        this.setId(id);
	        this.setStudentName(studentName);
	        this.setAge(age);
	    }

	    public int getId() {
	        return id;
	    }

	    public void setId(int id) {
	        this.id = id;
	    }

	    public String getStudentName() {
	        return studentName;
	    }

	    public void setStudentName(String studentName) {
	        this.studentName = studentName;
	    }

	    public int getAge() {
	        return age;
	    }

	    public void setAge(int age) {
	        this.age = age;
	    }
	}

	public class Teacher {
	    private String teacherName;
	    private int age;
	    private Course course;
	    private List<Student> students;

	    public Teacher(String teacherName, int age, Course course, List<Student> students) {
	        this.setTeacherName(teacherName);
	        this.setAge(age);
	        this.setCourse(course);
	        this.setStudents(students);
	    }

	    public String getTeacherName() {
	        return teacherName;
	    }

	    public void setTeacherName(String teacherName) {
	        this.teacherName = teacherName;
	    }

	    public int getAge() {
	        return age;
	    }

	    public void setAge(int age) {
	        this.age = age;
	    }

	    public Course getCourse() {
	        return course;
	    }

	    public void setCourse(Course course) {
	        this.course = course;
	    }

	    public List<Student> getStudents() {
	        return students;
	    }

	    public void setStudents(List<Student> students) {
	        this.students = students;
	    }
	}
	```
	- 反序列化操作
	```
	String complexJsonString = "{\"teacherName\":\"crystall\",\"age\":27,\"course\":{\"courseName\":\"english\",\"code\":1270},\"students\":[{\"id\":1,\"studentName\":\"lily\",\"age\":12},{\"id\":2,\"studentName\":\"lucy\",\"age\":15}]}";
    Teacher teacher = JSON.parseObject(complexJsonString,Teacher.class);
	```

### 4.2 序列化

- 序列化一个Java Bean对象
```
User u = new User();
u.setName("王五");
u.setAge(30);
System.out.println(JSON.toJSONString(u));

// 输出结果 {"age":30,"name":"王五"}

User u1 = new User();
u1.setAge(30);
System.out.println(JSON.toJSONString(u1,SerializerFeature.WriteMapNullValue)); // 输出null，输出结果 {"age":30,"name":null}
System.out.println(JSON.toJSONString(u1,SerializerFeature.WriteNullStringAsEmpty)); // 输出""，输出结果 {"age":30,"name":""}
```

### 4.3 序列化和反序列日期
```
Date date = new Date();

String dateString = JSON.toJSONStringWithDateFormat(date, "yyyy-MM-dd HH:mm:ss");
System.out.println(dateString);

// 输出结果 "2018-08-03 09:44:21"

String dateString1 = "{\"time\":\"2018-08-01 22:22:22\"}";
System.out.println(JSON.parseObject(dateString1));

// 输出结果 {"time":"2018-08-01 22:22:22"}
```

### 4.4 JsonObject的一些操作
```
String jsonString1 = "{\"name\":\"张三\",\"age\":50}";
JSONObject jsonObject = JSON.parseObject(jsonString1);

System.out.println(jsonObject.keySet()); // 输出key集合，输出结果 [name, age]

if(jsonObject.containsKey("sex")) { // 判断key是否存在，输出结果 false
    System.out.println(true);
} else {
    System.out.println(false);
}

jsonObject.put("sex","man"); // 添加k/v键值对，输出结果 {"sex":"man","name":"张三","age":50}
System.out.println(jsonObject);

if (jsonObject.containsValue("man")) { // 判断value是否存在，输出结果 false
    System.out.println(true);
} else {
    System.out.println(false);
}

``` 

### 4.5 jsonArray的一些操作
```
String jsonArrayString1 = "[{\"id\":1,\"studentName\":\"lily\",\"age\":12},{\"id\":2,\"studentName\":\"lucy\",\"age\":15}]";
JSONArray jsonArray = JSON.parseArray(jsonArrayString1);

for (int i = 0; i < jsonArray.size(); i++) { // 遍历输出
    JSONObject jsonObj= jsonArray.getJSONObject(i);
    System.out.println(jsonObj.get("id"));
}

Student s3 = new Student(3,"学生乙",15);
jsonArray.add(s3); // 添加新jsonobject对象，输出结果 3
System.out.println(jsonArray.size());

if(jsonArray.contains(s3)) { // 判断是否存在，输出结果 true
    System.out.println(true);
} else {
    System.out.println(false);
}
```

## 5.Scala API

### 5.1 反序列化

- demo日志内容
	- data.log
	```
	{"name":"张三","age":10}
	{"name":"李四","age":11}
	{"name":"李四"}
	{"age":11}
	```
	- data1.log
	```
	{"data":[{"label":"123","acc":1,"version":"4.3.1"}]}
	{"data":[{"label":"789","acc":1,"version":"4.3.1"},{"label":"78","acc":100,"version":"4.3.1"}]}
	{"data":[{"label":"5356","acc":1,"version":"4.3.1"}]}
	```

- 反序列化简单json字符串
```
val spark = SparkSession.builder().master("local[2]").appName("FastJsonTest").getOrCreate() 
val input1 = "data.log"

val jsonRDD1 = spark.sparkContext.textFile(input1)

val dataRDD1 = jsonRDD1.map(json => {
  val jsonObject = JSON.parseObject(json)
  val name = jsonObject.getOrDefault("name",null)
  val age = jsonObject.getOrDefault("age",null)
  (name,age)
})

dataRDD1.foreach(println)

// 输出结果 
(李四,null)
(null,11)
(张三,10)
(李四,11)
```

- 反序列化简单json字符串组，实现一行变多行地解析json字符串。这个我也没找到很好的方法，欢迎读者指教一下
	- 方法一：字符串处理
	```
	val input2 = "data1.log"
	val jsonRDD2 = spark.sparkContext.textFile(input2)

    val dataRDD2 = jsonRDD2.map(json => {
      JSON.parseObject(json).getJSONArray("data").toString
    }).map(x => x.substring(1,x.length-1).replace("},{","}---{"))  // 去掉字符串中的[]，并替换},{成}---{，目的是用于区分
      .flatMap(x => x.split("---"))  // 字符串按----拆分
      .map(x => JSON.parseObject(x))

    val data2 = dataRDD2.map(jsonObject => {
      val version = jsonObject.getOrDefault("version",null)
      val label = jsonObject.getOrDefault("label",null)
      val acc = jsonObject.getOrDefault("acc",null)
      (version,label,acc)
    })

    data2.foreach(println)

    // 输出结果
    (4.3.1,5356,1)
	(4.3.1,123,1)
	(4.3.1,789,1)
	(4.3.1,78,100)
	```
	- 方法二：List
	```
	val dataRDD3 = jsonRDD2.flatMap(json => {
      val jsonArray = JSON.parseObject(json).getJSONArray("data")
      var dataList : List[String] = List()  // 创建一个List
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

    // 输出结果
    (4.3.1,5356,1)
	(4.3.1,123,1)
	(4.3.1,789,1)
	(4.3.1,78,100)
	```

### 5.2 序列化

- 序列化一个简单java Bean对象
```
val arr = Seq("tom:10", "bob:14", "hurry:9")
val dataRdd = spark.sparkContext.parallelize(arr)

val dataString = dataRdd.map(x => {
  val arr = x.split(":")
  val name = arr(0)
  val age = arr(1).toInt
  val u = new User(name,age)
  u
}).map(x => {
  JSON.toJSONString(x,SerializerFeature.WriteMapNullValue)  // 这里需要显示SerializerFeature中的某一个，否则会报同时匹配两个方法的错误
})

dataString.foreach(println)

// 输出结果
{"age":10,"name":"tom"}
{"age":14,"name":"bob"}
{"age":9,"name":"hurry"}
```

## 6.参考 
- https://segmentfault.com/a/1190000011212806
- https://www.cnblogs.com/cdf-opensource-007/p/7106018.html
- https://github.com/alibaba/fastjson
- https://blog.csdn.net/universsky2015/article/details/77965563?locationNum=9&fps=1