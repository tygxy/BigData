package com.bupt.spark.fastjson.Bean;

/**
 * Created by guoxingyu on 2018/8/3.
 */
public class UserTest {
    private String name;
    private int age;

    public UserTest(String name,int age) {
        this.setAge(age);
        this.setName(name);
    }

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
