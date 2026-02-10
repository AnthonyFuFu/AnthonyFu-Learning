package com.AnthonyFuLearning.creationalPatterns.prototype;

//原型接口
interface Prototype {
    Prototype clone();
    void setProperty(String property);
    String getProperty();
}