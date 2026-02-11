package com.anthonyfu.structuralPatterns.proxy;

class ProxyImage implements Image {
    private RealImage realImage;
    private String filename;
    
    public ProxyImage(String filename) {
        this.filename = filename;
    }
    
    @Override
    public void display() {
        // 懶加載 - 僅在需要時創建真實對象
        if (realImage == null) {
            realImage = new RealImage(filename);
        }
        realImage.display();
    }
}
