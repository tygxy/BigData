package com.bupt.fastjson.Bean;

/**
 * Created by guoxingyu on 2018/8/1.
 */
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
