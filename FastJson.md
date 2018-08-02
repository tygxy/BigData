# FastJson使用范例(Java、Scala版)

## 1.FastJson简介

JSON协议在日常开发中很常用，包括前后端的数据接口，日志字段的保存等，通常都采用JSON协议。FastJson是阿里的开源框架，很好用，估计开发的同学都有使用过。这里做一个简单的用法总结，配一些demo，包括Java版本和Scala版本，方便日后查询使用。

## 2.FastJson三个核心类
- JSON：fastjson的解析器，用于json字符串和javaBean、Json对象的转换
- JSONObject：fastJson提供的json对象 
- JSONArray：fastJson提供json数组对象

## 3.Maven
```
	<dependency>
	    <groupId>com.alibaba</groupId>
	    <artifactId>fastjson</artifactId>
	    <version>1.2.47</version>
	</dependency>
```

## 4.Java API

### 4.1反序列化

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
	```

- 反序列化一个简单JSON字符串组成Java对象数组
```
String jsonArrayString = "[{\"name\":\"张三\",\"age\":50},{\"name\":\"李四\",\"age\":51}]";
List<User> userList = JSON.parseArray(jsonArrayString,User.class);
Iterator it = userList.iterator();
while (it.hasNext()) {
    User u = (User)it.next();
    System.out.println("name:"+u.getName()+" age:"+u.getAge());
}
```

- 反序列化一个复杂的JSON字符串
	- 分别创建JavaBean的Teacher类、Course类、Student类
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

### 4.2序列化

- 序列化一个Java Bean对象
```
User u = new User();
u.setName("王五");
u.setAge(30);
System.out.println(JSON.toJSONString(u));

User u1 = new User();
u1.setAge(30);
System.out.println(JSON.toJSONString(u1,SerializerFeature.WriteMapNullValue)); // 输出null
System.out.println(JSON.toJSONString(u1,SerializerFeature.WriteNullStringAsEmpty)); // 输出""
```

### 4.3序列化和反序列日期
```
Date date = new Date();

String dateString = JSON.toJSONStringWithDateFormat(date, "yyyy-MM-dd HH:mm:ss");
System.out.println(dateString);

String dateString1 = "{\"time\":\"2018-08-01 22:22:22\"}";
System.out.println(JSON.parseObject(dateString1).get("time"));
```

### 4.4jsonObject的一些操作
```
String jsonString1 = "{\"name\":\"张三\",\"age\":50}";
JSONObject jsonObject = JSON.parseObject(jsonString1);

System.out.println(jsonObject.keySet()); // 输出key集合

if(jsonObject.containsKey("sex")) { // 判断key是否存在
    System.out.println(true);
} else {
    System.out.println(false);
}

jsonObject.put("sex","man"); // 添加k/v键值对

if (jsonObject.containsValue("man")) { // 判断value是否存在
    System.out.println(true);
} else {
    System.out.println(false);
}

``` 

### 4.5jsonArray的一些操作
```
String jsonArrayString1 = "[{\"id\":1,\"studentName\":\"lily\",\"age\":12},{\"id\":2,\"studentName\":\"lucy\",\"age\":15}]";
JSONArray jsonArray = JSON.parseArray(jsonArrayString1);

for (int i = 0; i < jsonArray.size(); i++) { // 遍历输出
    JSONObject jsonObj= jsonArray.getJSONObject(i);
    System.out.println(jsonObj.get("id"));
}

Student s3 = new Student(3,"学生乙",15);
jsonArray.add(s3); // 添加新jsonobject对象

if(jsonArray.contains(s3)) { // 判断是否存在
    System.out.println(true);
} else {
    System.out.println(false);
}
```
## 5.Scala API




## 6.参考 
- https://segmentfault.com/a/1190000011212806
- https://github.com/alibaba/fastjson