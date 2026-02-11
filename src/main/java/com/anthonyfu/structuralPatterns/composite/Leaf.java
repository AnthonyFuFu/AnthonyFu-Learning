package com.anthonyfu.structuralPatterns.composite;

class Leaf implements Component {
    private String name;
    
    public Leaf(String name) {
        this.name = name;
    }
    
    @Override
    public void operation() {
        System.out.println("Leaf " + name + ": operation");
    }
    
    @Override
    public void add(Component component) {
        System.out.println("Cannot add to a leaf");
    }
    
    @Override
    public void remove(Component component) {
        System.out.println("Cannot remove from a leaf");
    }
    
    @Override
    public Component getChild(int index) {
        System.out.println("Cannot get child from a leaf");
        return null;
    }
}