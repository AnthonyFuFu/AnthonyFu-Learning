package com.anthonyfu.designPattern.structural.composite;

interface Component {
    void operation();
    void add(Component component);
    void remove(Component component);
    Component getChild(int index);
}
