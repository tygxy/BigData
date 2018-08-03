package com.bupt.fastjson.Bean;

import java.util.List;

/**
 * Created by guoxingyu on 2018/8/1.
 */
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
