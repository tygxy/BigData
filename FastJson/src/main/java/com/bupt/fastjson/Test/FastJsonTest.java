package com.bupt.fastjson.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.bupt.fastjson.Bean.Course;
import com.bupt.fastjson.Bean.Student;
import com.bupt.fastjson.Bean.Teacher;
import com.bupt.fastjson.Bean.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Date;

/**
 * Created by guoxingyu on 2018/8/1.
 * FastJSon Demo
 */
public class FastJsonTest {
    public static void main(String[] args) {

        // 反序列化一个简单JSON字符串成Java对象
        String jsonString = "{\"name\":\"张三\",\"age\":50}";
        User user= JSON.parseObject(jsonString,User.class);
        System.out.println("反序列化一个简单JSON字符串成Java对象");
        System.out.println("name:"+user.getName()+" age:"+user.getAge());


        // 反序列化一个复杂的JSON字符串成Java对象
        String complexJsonString = "{\"teacherName\":\"crystall\",\"age\":27,\"course\":{\"courseName\":\"english\",\"code\":1270},\"students\":[{\"id\":1,\"studentName\":\"lily\",\"age\":12},{\"id\":2,\"studentName\":\"lucy\",\"age\":15}]}";
        Teacher teacher = JSON.parseObject(complexJsonString,Teacher.class);
        System.out.println("反序列化一个复杂的JSON字符串成Java对象");
        System.out.println("teacherName: "+ teacher.getTeacherName() + " age: " + teacher.getAge());
        System.out.println("courseName: " + teacher.getCourse().getCourseName() + " code: " + teacher.getCourse().getCode());
        List<Student> studentsList = teacher.getStudents();
        Iterator it1 = studentsList.iterator();
        while (it1.hasNext()) {
            Student s = (Student)it1.next();
            System.out.println("id: "+ s.getId() + " name: "+s.getStudentName()+" age:"+s.getAge());
        }


        // 反序列化一个简单JSON字符串组成Java对象组
        String jsonArrayString = "[{\"name\":\"张三\",\"age\":50},{\"name\":\"李四\",\"age\":51}]";
        List<User> userList = JSON.parseArray(jsonArrayString,User.class);
        System.out.println("反序列化一个简单JSON字符串组成Java对象组");
        Iterator it = userList.iterator();
        while (it.hasNext()) {
            User u = (User)it.next();
            System.out.println("name:"+u.getName()+" age:"+u.getAge());
        }

        // 序列化一个简单对象成json字符串
        User u = new User();
        u.setName("王五");
        u.setAge(30);
        System.out.println("序列化一个简单对象成json字符串");
        System.out.println(JSON.toJSONString(u));

        // 序列化一个复杂对象成json字符串
        Course c = new Course("math","1000");
        Student s1 = new Student(1,"学生甲",16);
        Student s2 = new Student(2,"学生乙",15);
        List<Student> studentList = new ArrayList<Student>();
        studentList.add(s1);
        studentList.add(s2);
        System.out.println("序列化一个复杂对象成json字符串");
        Teacher t = new Teacher("老师",31,c,studentList);
        System.out.println(JSON.toJSONString(t));

        // 序列化一个对象成json字符串，要求输出空值
        User u1 = new User();
        u1.setAge(30);
        System.out.println("序列化一个对象成json字符串，要求输出空值");
        System.out.println(JSON.toJSONString(u1,SerializerFeature.WriteMapNullValue)); // 输出null
        System.out.println(JSON.toJSONString(u1,SerializerFeature.WriteNullStringAsEmpty)); // 输出""

        // 序列化和反序列化日期
        Date date = new Date();

        String dateString = JSON.toJSONStringWithDateFormat(date, "yyyy-MM-dd HH:mm:ss");
        System.out.println(dateString);

        String dateString1 = "{\"time\":\"2018-08-01 22:22:22\"}";
        System.out.println(JSON.parseObject(dateString1));

        // jsonObject的一些操作
        String jsonString1 = "{\"name\":\"张三\",\"age\":50}";
        JSONObject jsonObject = JSON.parseObject(jsonString1);
        System.out.println("jsonObject的一些操作");
        System.out.println(jsonObject.keySet());
        if(jsonObject.containsKey("sex")) {
            System.out.println(true);
        } else {
            System.out.println(false);
        }
        jsonObject.put("sex","man");
        System.out.println(jsonObject);
        if (jsonObject.containsValue("man")) {
            System.out.println(true);
        } else {
            System.out.println(false);
        }

        // jsonArray的一些操作
        String jsonArrayString1 = "[{\"id\":1,\"studentName\":\"lily\",\"age\":12},{\"id\":2,\"studentName\":\"lucy\",\"age\":15}]";
        JSONArray jsonArray = JSON.parseArray(jsonArrayString1);
        System.out.println("jsonArray的一些操作");
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObj= jsonArray.getJSONObject(i);
            System.out.println(jsonObj);
        }
        Student s3 = new Student(3,"学生乙",15);
        jsonArray.add(s3);
        System.out.println(jsonArray.size());
        if(jsonArray.contains(s3)) {
            System.out.println(true);
        } else {
            System.out.println(false);
        }
        
    }
}
