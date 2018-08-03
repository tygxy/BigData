package com.bupt.fastjson.Bean;

/**
 * Created by guoxingyu on 2018/8/1.
 */
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
