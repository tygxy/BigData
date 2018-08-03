package com.bupt.fastjson.Bean;

/**
 * Created by guoxingyu on 2018/8/1.
 */
public class User {
    private String name;
    private int age = -1;

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
