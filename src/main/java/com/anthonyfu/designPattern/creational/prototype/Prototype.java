package com.anthonyfu.designPattern.creational.prototype;

//原型接口
interface Prototype {
    Prototype clone();
    void setProperty(String property);
    String getProperty();
}